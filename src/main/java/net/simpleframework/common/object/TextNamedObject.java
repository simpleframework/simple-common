package net.simpleframework.common.object;

import java.util.Date;

import net.simpleframework.common.Convert;
import net.simpleframework.common.I18n;
import net.simpleframework.common.StringUtils;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
@SuppressWarnings("unchecked")
public abstract class TextNamedObject<T extends TextNamedObject<T>> extends NamedObject<T> {

	private String text;

	public String getText() {
		return StringUtils.hasText(text) ? text : getName();
	}

	public T setText(final String text) {
		this.text = I18n.replaceI18n(text);
		return (T) this;
	}

	public T setText(final Object text) {
		if (text instanceof Date) {
			setText(Convert.toDateString((Date) text));
		} else {
			setText(Convert.toString(text));
		}
		return (T) this;
	}

	@Override
	public String toString() {
		return getText();
	}
}
