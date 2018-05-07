package net.simpleframework.common.object;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class MethodResult {
	public static MethodResult FAILURE = new MethodResult(null, false);

	public static MethodResult VOID_SUCCESS = new MethodResult(null, true);

	private final boolean success;

	private final Object value;

	public MethodResult(final Object value) {
		this(value, true);
	}

	public MethodResult(final Object value, final boolean success) {
		this.value = value;
		this.success = success;
	}

	public Object getValue() {
		return value;
	}

	public boolean isSuccess() {
		return success;
	}
}
