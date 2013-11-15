package net.simpleframework.ado;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public enum EFilterOpe {

	and,

	or;

	public static EFilterOpe get(final String key) {
		final EFilterOpe ope = EFilterOpe.valueOf(key);
		if (ope != null) {
			return ope;
		}
		return EFilterOpe.and;
	}
}
