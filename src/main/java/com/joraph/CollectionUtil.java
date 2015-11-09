package com.joraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CollectionUtil {

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
		Set<T> ret = new HashSet<>();
		if (itr!=null) {
			itr.forEach(ret::add);
		}
		return ret;
	}

}
