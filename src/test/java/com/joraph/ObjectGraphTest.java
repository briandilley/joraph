package com.joraph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ObjectGraphTest {

	private ObjectGraph subject;

	@BeforeEach
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

	@Test
	public void testGetMapIsImmutableWithValues() {
		assertThrows(UnsupportedOperationException.class, () -> {
			subject.addResult(String.class, Integer.valueOf(1), "one");
			subject.getMap(String.class).clear();
		});
	}

	@Test
	public void testGetMapIsImmutableWithoutValues() {
		assertThrows(UnsupportedOperationException.class, () -> {
			subject.getMap(String.class).clear();
		});
	}

}
