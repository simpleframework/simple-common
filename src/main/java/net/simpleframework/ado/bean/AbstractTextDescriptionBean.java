package net.simpleframework.ado.bean;

import net.simpleframework.ado.ColumnMeta;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
@SuppressWarnings("serial")
public abstract class AbstractTextDescriptionBean extends AbstractDescriptionBean
		implements ITextBeanAware {

	@ColumnMeta(columnText = "#(Text)")
	private String text;

	@Override
	public String getText() {
		return text != null ? text.trim() : null;
	}

	@Override
	public void setText(final String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return getText();
	}
}
