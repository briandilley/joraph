package com.joraph;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class ObjectGraphTest {

	private ObjectGraph subject;

	@Before
	public void setUp() {
		subject = new ObjectGraph();
	}

	@Test
	public void testGetListNeverReturnsNull() {
		List<String> strings = subject.getList(String.class);
		assertNotNull(strings);
		assertTrue(strings.isEmpty());
		
		subject.addResult(String.class, Integer.valueOf(1), "one");
		strings = subject.getList(String.class);
		assertNotNull(strings);
		assertFalse(strings.isEmpty());
		assertEquals(1, strings.size());
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testGetMapIsImmutableWithValues() {
		subject.addResult(String.class, Integer.valueOf(1), "one");
		subject.getMap(String.class).clear();
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testGetMapIsImmutableWithoutValues() {
		subject.getMap(String.class).clear();
	}

}
