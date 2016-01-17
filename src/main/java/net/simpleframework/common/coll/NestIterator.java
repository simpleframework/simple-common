package net.simpleframework.common.coll;

import java.util.Iterator;

import net.simpleframework.common.coll.CollectionUtils.AbstractIterator;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class NestIterator<T, N> extends AbstractIterator<T> {
	protected Iterator<N> nest;

	public NestIterator(final Iterator<N> nest) {
		this.nest = nest;
	}

	@Override
	public boolean hasNext() {
		return nest.hasNext();
	}

	protected abstract T change(N n);

	@Override
	public T next() {
		return change(nest.next());
	}
}
