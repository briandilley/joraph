package com.joraph.loader;

import static com.joraph.CollectionUtil.array;
import static com.joraph.CollectionUtil.collection;
import static com.joraph.CollectionUtil.toList;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

@FunctionalInterface
public interface LoaderFunction<A, I, R> {

	List<R> load(A arguments, Iterable<I> ids);



	public static <I, A, R> LoaderFunction<A, I, R> ofArrayItr(
			Function<I[], Iterable<R>> function,
			IntFunction<I[]> supplier) {
		return (arguments, ids) -> toList(function.apply(array(ids, supplier)));
	}

	public static <I, A, R> LoaderFunction<A, I, R> ofArrayArray(
			Function<I[], R[]> function,
			IntFunction<I[]> supplier) {
		return (arguments, ids) -> asList(function.apply(array(ids, supplier)));
	}

	public static <A, I, R, C extends Collection<I>> LoaderFunction<A, I, R> ofItrArray(
			Function<C, R[]> function,
			Supplier<C> supplier) {
		return (arguments, ids) -> asList(function.apply(collection(ids, supplier)));
	}

	public static <A, I, R, C extends Collection<I>> LoaderFunction<A, I, R> ofItrItr(
			Function<C, Iterable<R>> function,
			Supplier<C> supplier) {
		return (arguments, ids) -> toList(function.apply(collection(ids, supplier)));
	}

	public static <A, I, R> LoaderFunction<A, I, R> ofListArray(
			Function<List<I>, R[]> function) {
		return ofItrArray(function, ArrayList::new);
	}

	public static <A, I, R> LoaderFunction<A, I, R> ofListItr(
			Function<List<I>, Iterable<R>> function) {
		return ofItrItr(function, ArrayList::new);
	}

	public static <A, I, R> LoaderFunction<A, I, R> ofSetArray(
			Function<Set<I>, R[]> function) {
		return ofItrArray(function, HashSet::new);
	}

	public static <A, I, R> LoaderFunction<A, I, R> ofSetItr(
			Function<Set<I>, Iterable<R>> function) {
		return ofItrItr(function, HashSet::new);
	}



	public static <I, A, AA, R> LoaderFunction<A, I, R> ofArrayItr(
			BiFunction<AA, I[], Iterable<R>> function,
			IntFunction<I[]> supplier,
			Function<A, AA> argumentExtractor) {
		return (arguments, ids) -> toList(function.apply(arguments != null
				? argumentExtractor.apply(arguments)
				: null,
			array(ids, supplier)));
	}

	public static <I, A, AA, R> LoaderFunction<A, I, R> ofArrayArray(
			BiFunction<AA, I[], R[]> function,
			IntFunction<I[]> supplier,
			Function<A, AA> argumentExtractor) {
		return (arguments, ids) -> asList(function.apply(arguments != null
				? argumentExtractor.apply(arguments)
				: null,
			array(ids, supplier)));
	}

	public static <A, AA, I, R, C extends Collection<I>> LoaderFunction<A, I, R> ofItrArray(
			BiFunction<AA, C, R[]> function,
			Supplier<C> supplier,
			Function<A, AA> argumentExtractor) {
		return (arguments, ids) -> asList(function.apply(arguments != null
				? argumentExtractor.apply(arguments)
				: null,
			collection(ids, supplier)));
	}

	public static <A, AA, I, R, C extends Collection<I>> LoaderFunction<A, I, R> ofItrItr(
			BiFunction<AA, C, Iterable<R>> function,
			Supplier<C> supplier,
			Function<A, AA> argumentExtractor) {
		return (arguments, ids) -> toList(function.apply(arguments != null
				? argumentExtractor.apply(arguments)
				: null,
			collection(ids, supplier)));
	}

	public static <A, AA, I, R> LoaderFunction<A, I, R> ofListArray(
			BiFunction<AA, List<I>, R[]> function,
			Function<A, AA> argumentExtractor) {
		return ofItrArray(function, ArrayList::new, argumentExtractor);
	}

	public static <A, AA, I, R> LoaderFunction<A, I, R> ofListItr(
			BiFunction<AA, List<I>, Iterable<R>> function,
			Function<A, AA> argumentExtractor) {
		return ofItrItr(function, ArrayList::new, argumentExtractor);
	}

	public static <A, AA, I, R> LoaderFunction<A, I, R> ofSetArray(
			BiFunction<AA, Set<I>, R[]> function,
			Function<A, AA> argumentExtractor) {
		return ofItrArray(function, HashSet::new, argumentExtractor);
	}

	public static <A, AA, I, R> LoaderFunction<A, I, R> ofSetItr(
			BiFunction<AA, Set<I>, Iterable<R>> function,
			Function<A, AA> argumentExtractor) {
		return ofItrItr(function, HashSet::new, argumentExtractor);
	}

}
