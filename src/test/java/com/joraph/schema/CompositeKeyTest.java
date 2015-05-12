package com.joraph.schema;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Converter;

public class CompositeKeyTest {

	private CompositeKey<CPK> key;

	@Before
	public void setUp()
			throws Exception {
		key = new CompositeKey<CompositeKeyTest.CPK>(
				UserFollow.class, CONVERTER, "fromUserId", "toUserId");
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

	public Converter<Object[], CPK> CONVERTER
			= new Converter<Object[], CompositeKeyTest.CPK>() {
			
		@Override
		protected CPK doForward(Object[] a) {
			return new CPK(a[0].toString(), a[1].toString());
		}
		
		@Override
		protected Object[] doBackward(CPK b) {
			return new Object[] {
				b.from,
				b.to
			};
		}
	};

}
