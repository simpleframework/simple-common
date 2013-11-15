package net.simpleframework.ado;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
@Target({ ElementType.FIELD, ElementType.METHOD /* get */})
@Retention(RetentionPolicy.RUNTIME)
public @interface ColumnMeta {
	/**
	 * 列的显示名
	 * 
	 * @return
	 */
	String columnText() default "";

	/**
	 * 列的映射名称。比如sqlcolumn
	 * 
	 * @return
	 */
	String columnMappingName() default "";

	/**
	 * 忽略持续化
	 * 
	 * @return
	 */
	boolean ignore() default false;
}
