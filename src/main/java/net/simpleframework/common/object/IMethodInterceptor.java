package net.simpleframework.common.object;

import java.lang.reflect.Method;

import net.simpleframework.lib.net.sf.cglib.proxy.MethodProxy;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public interface IMethodInterceptor {

	/**
	 * 
	 * @param obj
	 * @param method
	 * @param args
	 * @return
	 * @throws Throwable
	 */
	MethodResult intercept(Object obj, Method method, Object[] args, MethodProxy proxy)
			throws Throwable;
}
