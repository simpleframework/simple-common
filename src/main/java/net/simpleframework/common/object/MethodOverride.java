package net.simpleframework.common.object;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface MethodOverride {

	/**
	 * 定义实际执行被覆盖方法的类
	 * 
	 * @return
	 */
	String impl();

	/**
	 * 
	 * @return
	 */
	String[] methods();
}
