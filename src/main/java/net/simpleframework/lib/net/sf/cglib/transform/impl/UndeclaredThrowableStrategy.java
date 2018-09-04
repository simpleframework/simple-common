/*
 * Copyright 2003 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.simpleframework.lib.net.sf.cglib.transform.impl;

import net.simpleframework.lib.net.sf.cglib.core.ClassGenerator;
import net.simpleframework.lib.net.sf.cglib.core.DefaultGeneratorStrategy;
import net.simpleframework.lib.net.sf.cglib.core.GeneratorStrategy;
import net.simpleframework.lib.net.sf.cglib.core.TypeUtils;
import net.simpleframework.lib.net.sf.cglib.transform.ClassTransformer;
import net.simpleframework.lib.net.sf.cglib.transform.MethodFilter;
import net.simpleframework.lib.net.sf.cglib.transform.MethodFilterTransformer;
import net.simpleframework.lib.net.sf.cglib.transform.TransformingClassGenerator;

/**
 * A {@link GeneratorStrategy} suitable for use with
 * {@link net.simpleframework.lib.net.sf.cglib.Enhancer} which
 * causes all undeclared exceptions thrown from within a proxied method to be
 * wrapped
 * in an alternative exception of your choice.
 */
public class UndeclaredThrowableStrategy extends DefaultGeneratorStrategy {

	private final Class wrapper;

	/**
	 * Create a new instance of this strategy.
	 * 
	 * @param wrapper
	 *        a class which extends either directly or
	 *        indirectly from <code>Throwable</code> and which has at least one
	 *        constructor that takes a single argument of type
	 *        <code>Throwable</code>, for example
	 *        <code>java.lang.reflect.UndeclaredThrowableException.class</code>
	 */
	public UndeclaredThrowableStrategy(final Class wrapper) {
		this.wrapper = wrapper;
	}

	private static final MethodFilter TRANSFORM_FILTER = new MethodFilter() {
		@Override
		public boolean accept(final int access, final String name, final String desc,
				final String signature, final String[] exceptions) {
			return !TypeUtils.isPrivate(access) && name.indexOf('$') < 0;
		}
	};

	@Override
	protected ClassGenerator transform(final ClassGenerator cg) throws Exception {
		ClassTransformer tr = new UndeclaredThrowableTransformer(wrapper);
		tr = new MethodFilterTransformer(TRANSFORM_FILTER, tr);
		return new TransformingClassGenerator(cg, tr);
	}
}
