package net.simpleframework.ado.query;

import java.util.Iterator;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class IteratorDataQuery<T> extends AbstractDataQuery<T> {

	private Iterator<T> it;

	private int count;

	public IteratorDataQuery() {
		this(null);
	}

	public IteratorDataQuery(final Iterator<T> it) {
		this(it, it instanceof IDataQueryCountAware ? ((IDataQueryCountAware) it).getCount() : 0);
	}

	public IteratorDataQuery(final Iterator<T> it, final int count) {
		this.it = it;
		this.count = count;
	}

	public Iterator<T> iterator() {
		return it;
	}

	@Override
	public T next() {
		return it != null && it.hasNext() ? it.next() : null;
	}

	@Override
	public int getCount() {
		return count;
	}

	@Override
	public int getFetchSize() {
		return 0;
	}

	@Override
	public IDataQuery<T> setFetchSize(final int fetchSize) {
		return this;
	}
}
