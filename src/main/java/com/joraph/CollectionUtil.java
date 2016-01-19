package com.joraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class CollectionUtil {

	/**
	 * Converts an {@link Iterable} to a {@link Collection}.
	 * @param itr
	 * @param supplier
	 * @return
	 */
	public static <T, C extends Collection<T>> C collection(Iterable<? extends T> itr, Supplier<C> supplier) {
		return StreamSupport.stream(itr.spliterator(), false)
				.collect(supplier, Collection::add, Collection::addAll);
	}

	/**
	 * Converts an {@link Iterable} to an array.
	 * @param itr
	 * @param supplier
	 * @return
	 */
	public static <T> T[] array(Iterable<? extends T> itr, IntFunction<T[]> supplier) {
		return StreamSupport.stream(itr.spliterator(), false)
				.toArray(supplier);
	}

	/**
	 * Creats a stream from the given items.
	 * @param first the first
	 * @param ids the next
	 * @return the stream
	 */
	@SafeVarargs
	public static <T> Stream<T> asStream(T first, T... ids) {
		Stream<T> ret = Stream.of(first);
		if (ids!=null && ids.length>0) {
			ret = Stream.concat(ret, Stream.of(ids));
		}
		return ret;
	}

	/**
	 * Creates a list.
	 * @param first the first item
	 * @param ids the ids
	 * @return the list
	 */
	@SafeVarargs
	public static <T> List<T> asList(T firstId, T... ids) {
		return asStream(firstId, ids).collect(Collectors.toList());
	}

	/**
	 * Creates a set.
	 * @param first the first item
	 * @param ids the ids
	 * @return the list
	 */
	@SafeVarargs
	public static <T> Set<T> asSet(T firstId, T... ids) {
		return asStream(firstId, ids).collect(Collectors.toSet());
	}

	/**
	 * Creates a list.
	 * @param first the first item
	 * @param ids the ids
	 * @return the list
	 */
	public static <T> List<T> toList(Iterable<T> itr) {
		if (List.class.isInstance(itr)) {
			return (List<T>)itr;
		}
		List<T> ret = new ArrayList<>();
		if (itr!=null) {
			itr.forEach(ret::add);
		}
		return ret;
	}

	/**
	 * Creates a set.
	 * @param first the first item
	 * @param ids the ids
	 * @return the list
	 */
	public static <T> Set<T> toSet(Iterable<T> itr) {
		if (Set.class.isInstance(itr)) {
			return (Set<T>)itr;
		}
		Set<T> ret = new HashSet<>();
		if (itr!=null) {
			itr.forEach(ret::add);
		}
		return ret;
	}

	/**
	 * Converts an object to a set of objects, taking into
	 * consideration whether or not it's already a collection
	 * or array.
	 * @param val the object
	 * @return the Set
	 */
	@SuppressWarnings("unchecked")
	public static Set<Object> convertToSet(Object val) {
		if (Set.class.isInstance(val)) {
			return (Set<Object>)val;
		}
		Set<Object> ret;
		if (Collection.class.isInstance(val)) {
			ret = new HashSet<>((Collection<Object>)val);
		} else if (val.getClass().isArray()) {
			ret = asSet((Object[])val);
		} else {
			ret = asSet(val);
		}
		return ret;
	}

}
