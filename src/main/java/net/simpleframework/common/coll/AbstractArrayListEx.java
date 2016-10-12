package net.simpleframework.common.coll;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
@SuppressWarnings({ "serial", "unchecked" })
public abstract class AbstractArrayListEx<T extends AbstractArrayListEx<T, M>, M>
		extends ArrayList<M> {

	public T append(final M... elements) {
		if (elements != null) {
			for (final M element : elements) {
				add(element);
			}
		}
		return (T) this;
	}

	public T appendAll(final Collection<M> coll) {
		if (coll != null) {
			addAll(coll);
		}
		return (T) this;
	}

	@Override
	public boolean add(final M element) {
		if (element == null) {
			return false;
		}
		return super.add(element);
	}

	@Override
	public void add(final int index, final M element) {
		if (element == null) {
			return;
		}
		super.add(index, element);
	}

	@Override
	public T clone() {
		return (T) super.clone();
	}
}
