package net.simpleframework.ado.query;

import java.util.Iterator;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class IteratorNestDataQuery<T, M> extends AbstractDataQuery<M> {

	private final Iterator<T> it;

	public IteratorNestDataQuery(final Iterator<T> it) {
		this.it = it;
	}

	public Iterator<T> iterator() {
		return it;
	}

	@SuppressWarnings("unchecked")
	protected M change(final T t) {
		return (M) t;
	}

	@Override
	public M next() {
		return it != null && it.hasNext() ? change(it.next()) : null;
	}

	@SuppressWarnings("unchecked")
	public IDataQuery<M> getRawDataQuery() {
		return it instanceof IDataQueryAware ? ((IDataQueryAware<M>) it).getDataQuery() : this;
	}

	@Override
	public int getCount() {
		final IDataQuery<M> dq = getRawDataQuery();
		return dq != this ? dq.getCount() : count;
	}

	@Override
	public int getFetchSize() {
		final IDataQuery<M> dq = getRawDataQuery();
		return dq != this ? dq.getFetchSize() : 0;
	}

	@Override
	public IDataQuery<M> setFetchSize(final int fetchSize) {
		final IDataQuery<M> dq = getRawDataQuery();
		if (dq != this) {
			dq.setFetchSize(fetchSize);
		}
		return this;
	}
}
