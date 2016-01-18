package com.joraph.loader;

import java.util.List;

@FunctionalInterface
public interface LoaderFunction<A, T> {

	List<? extends T> load(A arguments, Iterable<?> ids);

}
