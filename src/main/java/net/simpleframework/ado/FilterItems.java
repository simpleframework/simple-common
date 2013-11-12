package net.simpleframework.ado;

import net.simpleframework.common.coll.AbstractArrayListEx;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         http://code.google.com/p/simpleframework/
 *         http://www.simpleframework.net
 */
public class FilterItems extends AbstractArrayListEx<FilterItems, FilterItem> {

	public static FilterItems of(final FilterItem... item) {
		return new FilterItems().append(item);
	}

	public FilterItems addEqualItem(final String key, final Object val) {
		if (val != null) {
			add(new FilterItem(key, val));
		}
		return this;
	}

	private static final long serialVersionUID = 6573629383631874097L;
}
