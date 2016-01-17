package net.simpleframework.ado.query;

import java.util.Iterator;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class IteratorDataQuery<T> extends IteratorNestDataQuery<T, T> {

	public IteratorDataQuery(final Iterator<T> it) {
		super(it);
	}
}
