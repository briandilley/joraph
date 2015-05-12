package com.joraph.schema;

import java.util.Arrays;

import com.google.common.base.Converter;

public class BasicCompositeKey {

	public static final Converter<Object[], BasicCompositeKey> CONVERTER
			= new Converter<Object[], BasicCompositeKey>() {
				@Override
				protected BasicCompositeKey doForward(Object[] a) {
					return new BasicCompositeKey(a);
				}
				@Override
				protected Object[] doBackward(BasicCompositeKey b) {
					return b.objects;
				}
			};

	private Object[] objects;

	public BasicCompositeKey(Object... objects) {
		this.objects = objects;
	}

	public BasicCompositeKey(int size) {
		this.objects = new Object[size];
	}

	public Object getPart(int index) {
		return this.objects[index];
	}

	public void setPart(int index, Object object) {
		this.objects[index] = object;
	}

	public int getSize() {
		return this.objects.length;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(objects);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BasicCompositeKey other = (BasicCompositeKey) obj;
		if (!Arrays.equals(objects, other.objects))
			return false;
		return true;
	}

}
