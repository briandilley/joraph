package com.joraph;

import java.util.concurrent.atomic.AtomicInteger;

public class TestArgs {

	private AtomicInteger arg1 = new AtomicInteger(0);
	private AtomicInteger arg2 = new AtomicInteger(0);
	private boolean loadFavoriteAuthors		= false;
	private boolean loadFavoriteLibraries	= false;

	public String incrementAndGetArg1() {
		return arg1.incrementAndGet()+"";
	}

	public Integer incrementAndGetArg2() {
		return arg2.incrementAndGet();
	}

	public AtomicInteger getArg1() {
		return arg1;
	}

	public void setArg1(AtomicInteger arg1) {
		this.arg1 = arg1;
	}

	public AtomicInteger getArg2() {
		return arg2;
	}

	public void setArg2(AtomicInteger arg2) {
		this.arg2 = arg2;
	}

	public boolean isLoadFavoriteAuthors() {
		return loadFavoriteAuthors;
	}

	public void setLoadFavoriteAuthors(boolean loadFavoriteAuthors) {
		this.loadFavoriteAuthors = loadFavoriteAuthors;
	}

	public boolean isLoadFavoriteLibraries() {
		return loadFavoriteLibraries;
	}

	public void setLoadFavoriteLibraries(boolean loadFavoriteLibraries) {
		this.loadFavoriteLibraries = loadFavoriteLibraries;
	}

}
