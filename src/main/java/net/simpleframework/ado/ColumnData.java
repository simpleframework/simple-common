package net.simpleframework.ado;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import net.simpleframework.common.StringUtils;
import net.simpleframework.common.object.ObjectEx;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class ColumnData extends ObjectEx implements Serializable {
	public static ColumnData[] BLANK = new ColumnData[0];

	public static ColumnData ORDER = new ColumnData("oorder", EOrder.desc);

	public static ColumnData ASC(final String name) {
		return new ColumnData(name, EOrder.asc);
	}

	public static ColumnData DESC(final String name) {
		return new ColumnData(name, EOrder.desc);
	}

	/**
	 * 列名称及别名
	 */
	private String name, sqlName;

	/**
	 * 列的显示名称
	 */
	private String text;

	/**
	 * 列的数据类型
	 */
	private Class<?> propertyClass;

	private boolean visible = true;

	/**
	 * 排序
	 */
	private EOrder order;

	public Collection<FilterItem> filterItems;

	public ColumnData(final String name, final String text) {
		this(name, text, null);
	}

	public ColumnData(final String name) {
		this(name, (String) null);
	}

	public ColumnData(final String name, final String text, final Class<?> propertyClass) {
		this.name = name;
		this.text = text;
		this.propertyClass = propertyClass;
		enableAttributes();
	}

	public ColumnData(final String name, final EOrder order) {
		this(name);
		setOrder(order);
	}

	public String getName() {
		return name;
	}

	public ColumnData setName(final String name) {
		this.name = name;
		return this;
	}

	public String getText() {
		return StringUtils.hasText(text) ? text : getName();
	}

	public ColumnData setText(final String text) {
		this.text = text;
		return this;
	}

	public String getSqlName() {
		return StringUtils.hasText(sqlName) ? sqlName : getName();
	}

	public ColumnData setSqlName(final String sqlName) {
		this.sqlName = sqlName;
		return this;
	}

	public Class<?> getPropertyClass() {
		return propertyClass;
	}

	public ColumnData setPropertyClass(final Class<?> propertyClass) {
		this.propertyClass = propertyClass;
		return this;
	}

	public boolean isVisible() {
		return visible;
	}

	public ColumnData setVisible(final boolean visible) {
		this.visible = visible;
		return this;
	}

	public EOrder getOrder() {
		return order == null ? EOrder.normal : order;
	}

	public ColumnData setOrder(final EOrder order) {
		this.order = order;
		return this;
	}

	public Collection<FilterItem> getFilterItems() {
		if (filterItems == null) {
			filterItems = new ArrayList<FilterItem>();
		}
		return filterItems;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof ColumnData) {
			return toString().equals(((ColumnData) obj).toString());
		} else {
			return super.equals(obj);
		}
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	private static final long serialVersionUID = -2338071977267680196L;
}
