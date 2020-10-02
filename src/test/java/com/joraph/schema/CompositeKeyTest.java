package com.joraph.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import kotlin.jvm.functions.Function1;


public class CompositeKeyTest {

	private CompositeKey<UserFollow, CPK> key;

	public Function1<Object[], CPK> CONVERTER = (a) -> new CPK(a[0].toString(), a[1].toString());

	@BeforeEach
	public void setUp() {
		key = new CompositeKey<>(CONVERTER,
				UserFollow::getFromUserId,
				UserFollow::getToUserId);
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
