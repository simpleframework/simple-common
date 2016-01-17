package net.simpleframework.ado.query;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class NestDataQuery<T, N> extends AbstractDataQuery<T> {
	private final IDataQuery<N> nest;

	public NestDataQuery(final IDataQuery<N> dq) {
		this.nest = dq;
	}

	protected abstract T change(N n);

	@Override
	public T next() {
		return change(nest.next());
	}

	@Override
	public int getCount() {
		return nest.getCount();
	}

	@Override
	public int getFetchSize() {
		return nest.getFetchSize();
	}

	@Override
	public IDataQuery<T> setFetchSize(final int fetchSize) {
		nest.setFetchSize(fetchSize);
		return this;
	}

	public IDataQuery<N> getNest() {
		return nest;
	}
}
