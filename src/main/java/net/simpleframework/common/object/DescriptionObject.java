package net.simpleframework.common.object;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
@SuppressWarnings("unchecked")
public class DescriptionObject<T extends DescriptionObject<T>> extends ObjectEx {

	private String description;

	public String getDescription() {
		return description;
	}

	public T setDescription(final String description) {
		this.description = description;
		return (T) this;
	}
}
