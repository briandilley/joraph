package com.joraph.schema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;

public class CompositeKeyTest {

	private CompositeKey<UserFollow, CPK> key;

	public Function<Object[], CPK> CONVERTER = (a) -> new CPK(a[0].toString(), a[1].toString());

	@Before
	public void setUp()
			throws Exception {
		key = new CompositeKey<UserFollow, CompositeKeyTest.CPK>(CONVERTER,
				new PropertyDescriptorChain<>(UserFollow::getFromUserId),
				new PropertyDescriptorChain<>(UserFollow::getToUserId));
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
