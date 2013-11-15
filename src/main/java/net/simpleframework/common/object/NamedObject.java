package net.simpleframework.common.object;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
@SuppressWarnings("unchecked")
public abstract class NamedObject<T extends NamedObject<T>> extends ObjectEx {
	private String name;

	public String getName() {
		return name;
	}

	public T setName(final String name) {
		this.name = name;
		return (T) this;
	}

	@Override
	public boolean equals(final Object obj) {
		if (name != null && obj instanceof NamedObject) {
			return name.equals(((NamedObject<?>) obj).getName());
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return name != null ? name.hashCode() : super.hashCode();
	}
}
