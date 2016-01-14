package com.joraph.schema;

public class UserEx
		extends User {

	@Override
	public String getName() {
		return super.getName()+" EX!";
	}

	public boolean isEx() {
		return true;
	}

}
