package net.simpleframework.ado.query;

import java.util.Iterator;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class IteratorDataQuery<T> extends AbstractDataQuery<T> {

	private final Iterator<T> it;

	public IteratorDataQuery(final Iterator<T> it) {
		this.it = it;
	}

	public Iterator<T> iterator() {
		return it;
	}

	@Override
	public T next() {
		return it != null && it.hasNext() ? it.next() : null;
	}

	@SuppressWarnings("unchecked")
	public IDataQuery<T> getRawDataQuery() {
		return it instanceof IDataQueryAware ? ((IDataQueryAware<T>) it).getDataQuery() : this;
	}

	@Override
	public int getCount() {
		final IDataQuery<T> dq = getRawDataQuery();
		return dq != this ? dq.getCount() : count;
	}

	@Override
	public int getFetchSize() {
		final IDataQuery<T> dq = getRawDataQuery();
		return dq != this ? dq.getFetchSize() : 0;
	}

	@Override
	public IteratorDataQuery<T> setFetchSize(final int fetchSize) {
		final IDataQuery<T> dq = getRawDataQuery();
		if (dq != this) {
			dq.setFetchSize(fetchSize);
		}
		return this;
	}
}
