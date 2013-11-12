package net.simpleframework.common.coll;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         http://code.google.com/p/simpleframework/
 *         http://www.simpleframework.net
 */
public class ArrayListEx<T> extends AbstractArrayListEx<ArrayListEx<T>, T> {

	public static <T> ArrayListEx<T> of(final T... item) {
		return new ArrayListEx<T>().append(item);
	}

	private static final long serialVersionUID = 1504124590510948045L;
}
