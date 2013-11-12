package net.simpleframework.ado;

import java.io.Serializable;
import java.util.Date;

import net.simpleframework.common.Convert;
import net.simpleframework.common.object.ObjectUtils;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         http://code.google.com/p/simpleframework/
 *         http://www.simpleframework.net
 */
public class FilterItem implements Serializable {
	private EFilterRelation relation;

	private String column;

	private Object value, originalValue;

	private EFilterOpe ope;

	public FilterItem(final String column, final Object value) {
		this(column, null, value);
	}

	public FilterItem(final String column, final EFilterRelation relation, final Object value) {
		this.column = column;
		this.relation = relation;
		this.value = value;
	}

	public FilterItem(final String column) {
		this(column, null);
	}

	public EFilterRelation getRelation() {
		return relation == null ? EFilterRelation.equal : relation;
	}

	public FilterItem setRelation(final EFilterRelation relation) {
		this.relation = relation;
		return this;
	}

	public String getColumn() {
		return column;
	}

	public FilterItem setColumn(final String column) {
		this.column = column;
		return this;
	}

	public Object getValue() {
		return value;
	}

	public FilterItem setValue(final Object value) {
		this.value = value;
		return this;
	}

	public EFilterOpe getOpe() {
		return ope == null ? EFilterOpe.and : ope;
	}

	public FilterItem setOpe(final EFilterOpe ope) {
		this.ope = ope;
		return this;
	}

	public Object getOriginalValue() {
		return originalValue;
	}

	public FilterItem setOriginalValue(final Object originalValue) {
		this.originalValue = originalValue;
		return this;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public boolean isDelete(Object v, final Class<?> propertyType) {
		final EFilterRelation r = getRelation();
		Object v2 = getValue();
		if (Enum.class.isAssignableFrom(propertyType)) {
			v = Convert.toEnum((Class<Enum>) propertyType, v);
			v2 = Convert.toEnum((Class<Enum>) propertyType, v2);
		}

		if (r == EFilterRelation.equal && ObjectUtils.objectEquals(v, v2)) {
			return false;
		} else if (r == EFilterRelation.not_equal && !ObjectUtils.objectEquals(v, v2)) {
			return false;
		} else if (r == EFilterRelation.like && v != null && v2 != null
				&& Convert.toString(v).contains(Convert.toString(v2))) {
			return false;
		} else if (Number.class.isAssignableFrom(propertyType)) {
			final double d = Convert.toDouble(v, Double.MIN_VALUE);
			final double d2 = Convert.toDouble(v2, Double.MIN_VALUE);
			if ((r == EFilterRelation.gt && d > d2) || (r == EFilterRelation.gt_equal && d >= d2)
					|| (r == EFilterRelation.lt && d < d2) || (r == EFilterRelation.lt_equal && d <= d2)) {
				return false;
			}
		} else if (Date.class.isAssignableFrom(propertyType)) {
			final Date d = (Date) v;
			final Date d2 = (Date) v2;
			if (d != null && d2 != null) {
				if ((r == EFilterRelation.gt && d.after(d2))
						|| (r == EFilterRelation.gt_equal && (d.after(d2) || d.equals(d2)))
						|| (r == EFilterRelation.lt && d.before(d2))
						|| (r == EFilterRelation.lt_equal && (d.before(d2) || d.equals(d2)))) {
					return false;
				}
			}
		}
		return true;
	}

	private static final long serialVersionUID = 598446923126901786L;
}
