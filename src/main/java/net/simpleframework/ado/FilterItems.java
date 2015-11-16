package net.simpleframework.ado;

import net.simpleframework.common.coll.AbstractArrayListEx;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class FilterItems extends AbstractArrayListEx<FilterItems, FilterItem> {

	public static FilterItems of(final FilterItem... item) {
		return new FilterItems().append(item);
	}

	public static FilterItems of(final String key, final Object val) {
		return new FilterItems().addEqual(key, val);
	}

	public FilterItems addEqual(final String key, final Object val) {
		if (val != null) {
			add(new FilterItem(key, val));
			return this;
		} else {
			return addIsNull(key);
		}
	}

	public FilterItems addNotEqual(final String key, final Object val) {
		if (val != null) {
			add(new FilterItem(key, EFilterRelation.not_equal, val));
			return this;
		} else {
			return addNotNull(key);
		}
	}

	public FilterItems addLike(final String key, final Object val) {
		if (val != null) {
			add(new FilterItem(key, EFilterRelation.like, val));
			return this;
		} else {
			return addIsNull(key);
		}
	}

	public FilterItems addIsNull(final String key) {
		add(FilterItem.isNull(key));
		return this;
	}

	public FilterItems addNotNull(final String key) {
		add(new FilterItem(key).setRelation(EFilterRelation.isNotNull));
		return this;
	}

	private static final long serialVersionUID = 6573629383631874097L;
}
