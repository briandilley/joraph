package com.joraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.joraph.debug.DebugInfo;
import com.joraph.debug.JoraphDebug;
import com.joraph.schema.EntityDescriptor;
import com.joraph.schema.ForeignKey;
import com.joraph.schema.Property;
import com.joraph.schema.Schema;
import com.joraph.schema.SchemaUtil;
import com.joraph.schema.UnknownEntityDescriptorException;

import kotlin.jvm.functions.Function1;

/**
 * An execution context which brings together a {@link com.joraph.JoraphContext},
 * a single entity class, and the root objects.
 */
public class ExecutionContext {

	private final JoraphContext context;
	private final Schema schema;
	private final Query query;
	private final ObjectGraph objectGraph;
	private final KeysToLoad keysToLoad;

	/**
	 * Creates a new instance of ExecutionContext.
	 */
	public ExecutionContext(JoraphContext context, Query query) {
		this.context		= context;
		this.schema         = context.getSchema();
		this.query 			= query;
		this.keysToLoad		= new KeysToLoad();

		this.objectGraph = query.hasExistingGraph()
				? query.getExistingGraph()
				: new ObjectGraph(context.getSchema());
		
		addToResults(query.getRootObjects());
	}

	/**
	 * <p>Executes the plan, iterates the resulting operations, and returns the results.</p>
	 * <p>Subsequent calls to {@code execute} result in a cached {@link ObjectGraph}.</p>
	 * @return the results derived from loading the associated objects supplied in the root
	 * objects
	 */
	public ObjectGraph execute() {

		keysToLoad.clear();


		final Set<EntityDescriptor<?>> descriptors = query.getEntityClasses().stream()
				.flatMap((clazz) -> schema.getEntityDescriptors(clazz).stream())
				.collect(Collectors.toSet());


		// for each entity
		boolean keepLoading = true;
		while (keepLoading) {

			// get all of the FKs
			for (EntityDescriptor<?> desc : descriptors) {
				for (Map.Entry<Function1<?,?>, ForeignKey<?,?>> fk : desc.getForeignKeys().entrySet()) {
					gatherValuesForForeignKeysTo(fk.getValue().getForeignEntity());
				}
			}

			// figure out which entities to load based on the FKs
			Set<Class<?>> entitiesToLoad = keysToLoad.getEntitiesToLoad();

			// load the new entities
			if (entitiesToLoad.size() == 1) {
				loadEntities(entitiesToLoad.iterator().next());
			} else if (entitiesToLoad.size() > 1) {
				loadEntitiesInParallel(entitiesToLoad);
			}

			// add the new descriptors to the list
			boolean newDescriptors = descriptors.addAll(entitiesToLoad.stream()
					.flatMap((clazz) -> schema.getEntityDescriptors(clazz).stream())
					.collect(Collectors.toSet()));

			keepLoading = !entitiesToLoad.isEmpty() || newDescriptors;
		}

		JoraphDebug.addObjectGraph(objectGraph);

		return objectGraph;
	}

	private void addToResults(Iterable<?> objects) {
		if (objects == null) {
			return;
		}

		final Schema schema = Objects.requireNonNull(context.getSchema(), "Schema must not be null");

		for (Object object : objects) {
			if (object==null) {
				continue;
			}
			final EntityDescriptor<?> entityDescriptor = schema.getEntityDescriptors(object.getClass()).stream()
					.filter((d) -> d.getEntityClass().equals(object.getClass()))
					.findFirst()
					.orElseThrow(() -> new UnknownEntityDescriptorException(object.getClass()));

			final Property<?, ?> pk = entityDescriptor.getPrimaryKey();
			objectGraph.addResult(entityDescriptor.getGraphKey(), pk.read(object), object);
		}
	}

	private void gatherValuesForForeignKeysTo(Class<?> entityClass) {

		context.getSchema().getEntityDescriptors(entityClass).stream()
				.flatMap((entityDescriptor) -> context.getSchema().describeForeignKeysTo(entityClass).stream()
						.filter((fk) -> SchemaUtil.shouldLoad(fk, query.getArguments()))
						.flatMap((fk) -> objectGraph.stream(fk.getEntityClass())
								.filter((o) -> o.getClass().equals(fk.getEntityClass()))
								.map(fk::read)
								.filter(Objects::nonNull)
								.map(CollectionUtil::convertToSet)
								.flatMap(Set::stream)
								.filter(Objects::nonNull)
								.filter((id) -> !objectGraph.has(entityDescriptor.getEntityClass(), id))))
				.forEach(keysToLoad.getAddKeyToLoadFunction(entityClass));

	}

	private void loadEntities(Class<?> entityClass) {
		Set<Object> ids = keysToLoad.getKeysToLoad(entityClass);
		if (ids == null || ids.isEmpty()) {
			return;
		}

		List<?> objects = context.getLoaderContext().load(entityClass, query.getArguments(), ids);
		addToResults(objects);
		keysToLoad.addKeysLoaded(entityClass, ids);
	}

	private void loadEntitiesInParallel(Collection<Class<?>> entityClasses) {
		List<Future<?>> futures = new ArrayList<>();
		for (final Class<?> entityClass : entityClasses) {
			final DebugInfo info = JoraphDebug.getDebugInfo();
			futures.add(context.getExecutorService().submit(() -> {
				JoraphDebug.setThreadDebugInfo(info);
				loadEntities(entityClass);
				JoraphDebug.clearThreadDebugInfo();
			}));
			JoraphDebug.setThreadDebugInfo(info);
		}
		for (Future<?> future : futures) {
			try {
				future.get(context.getParallelExecutorDefaultTimeoutMillis(), TimeUnit.MILLISECONDS);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				throw new JoraphException(e);
			}
		}
	}

	/**
	 * @return the context
	 */
	public JoraphContext getContext() {
		return context;
	}

	/**
	 * @return the query
	 */
	public Query getQuery() {
		return query;
	}

	/**
	 * @return the objectGraph
	 */
	public ObjectGraph getObjectGraph() {
		return objectGraph;
	}

	/**
	 * Simple class for managing the keys that need to be loaded.
	 */
	private static class KeysToLoad {
		private final Map<Class<?>, Set<Object>> keysToLoad = new HashMap<>();
		private final Map<Class<?>, Set<Object>> keysLoaded = new HashMap<>();

		private Consumer<Object> getAddKeyToLoadFunction(Class<?> entityClass) {
			return (id) -> addKeyToLoad(entityClass, id);
		}

		private synchronized void addKeyToLoad(Class<?> entityClass, Object id) {
			if (!getKeysLoaded(entityClass).contains(id)) {
				getKeysToLoad(entityClass).add(id);
			}
		}

		private synchronized void addKeysLoaded(Class<?> entityClass, Collection<Object> ids) {
			getKeysLoaded(entityClass).addAll(ids);
			getKeysToLoad(entityClass).removeAll(ids);
		}

		private Set<Object> getKeysToLoad(Class<?> entityClass) {
			return keysToLoad.computeIfAbsent(entityClass, __ ->
					Collections.newSetFromMap(new ConcurrentHashMap<>()));
		}

		private Set<Object> getKeysLoaded(Class<?> entityClass) {
			return keysLoaded.computeIfAbsent(entityClass, __ ->
					Collections.newSetFromMap(new ConcurrentHashMap<>()));
		}

		private synchronized void clear() {
			keysToLoad.clear();
			keysLoaded.clear();
		}

		public synchronized boolean hasKeysToLoad() {
			return !getEntitiesToLoad().isEmpty();
		}

		public synchronized Set<Class<?>> getEntitiesToLoad() {
			return keysToLoad.entrySet().stream()
					.filter((e) -> !e.getValue().isEmpty())
					.map(Map.Entry::getKey)
					.collect(Collectors.toSet());
		}
	}

}
