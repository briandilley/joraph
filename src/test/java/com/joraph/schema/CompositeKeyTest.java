package com.joraph.schema;

import static org.junit.Assert.*;

import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;

public class CompositeKeyTest {

	private CompositeKey<CPK> key;

	public Function<Object[], CPK> CONVERTER = (a) -> new CPK(a[0].toString(), a[1].toString());
	public Function<CPK, Object[]> CONVERTER_R = (a) -> new Object[] { a.from, a.to };

	@Before
	public void setUp()
			throws Exception {
		key = new CompositeKey<CompositeKeyTest.CPK>(
				UserFollow.class, CONVERTER, CONVERTER_R,
				"fromUserId", "toUserId");
	}

	@Test
	public void test() {
		
		UserFollow follow = new UserFollow();
		follow.setFromUserId("this is from");
		follow.setToUserId("this is to");
		
		CPK val = key.read(follow);
		assertNotNull(val);
		assertEquals(follow.getFromUserId(), val.from);
		assertEquals(follow.getToUserId(), val.to);
		
		UserFollow emptyFollow = new UserFollow();
		key.write(emptyFollow, val);
		assertEquals(follow.getFromUserId(), emptyFollow.getFromUserId());
		assertEquals(follow.getToUserId(), emptyFollow.getToUserId());
	}

	public class CPK {
		public String from;
		public String to;
		public CPK(String from, String to) {
			this.from = from;
			this.to = to;
		}
	}

}
