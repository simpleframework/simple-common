package net.simpleframework.ado.bean;

import net.simpleframework.ado.ColumnMeta;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
@SuppressWarnings("serial")
public abstract class AbstractDescriptionBean extends AbstractIdBean
		implements IDescriptionBeanAware {

	@ColumnMeta(columnText = "#(Description)")
	private String description;

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(final String description) {
		this.description = description;
	}
}
