package com.joraph.schema;

public interface Property<T> {

	String getName();

	T read(Object obj);

	void write(Object obj, Object value);

}
