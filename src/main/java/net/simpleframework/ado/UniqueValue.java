package net.simpleframework.ado;

import net.simpleframework.ado.IParamsValue.AbstractParamsValue;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class UniqueValue extends AbstractParamsValue {
	private static final long serialVersionUID = 3290227392192678851L;

	public UniqueValue(final Object... values) {
		addValues(values);
	}

	@Override
	public String getKey() {
		return valuesToString();
	}
}
