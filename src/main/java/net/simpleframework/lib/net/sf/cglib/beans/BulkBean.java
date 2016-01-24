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
package net.simpleframework.lib.net.sf.cglib.beans;

import java.security.ProtectionDomain;

import net.simpleframework.lib.net.sf.cglib.core.AbstractClassGenerator;
import net.simpleframework.lib.net.sf.cglib.core.KeyFactory;
import net.simpleframework.lib.net.sf.cglib.core.ReflectUtils;
import net.simpleframework.lib.org.objectweb.asm.ClassVisitor;

/**
 * @author Juozas Baliuka
 */
abstract public class BulkBean {
	private static final BulkBeanKey KEY_FACTORY = (BulkBeanKey) KeyFactory
			.create(BulkBeanKey.class);

	interface BulkBeanKey {
		public Object newInstance(String target, String[] getters, String[] setters, String[] types);
	}

	protected Class target;
	protected String[] getters, setters;
	protected Class[] types;

	protected BulkBean() {
	}

	abstract public void getPropertyValues(Object bean, Object[] values);

	abstract public void setPropertyValues(Object bean, Object[] values);

	public Object[] getPropertyValues(final Object bean) {
		final Object[] values = new Object[getters.length];
		getPropertyValues(bean, values);
		return values;
	}

	public Class[] getPropertyTypes() {
		return types.clone();
	}

	public String[] getGetters() {
		return getters.clone();
	}

	public String[] getSetters() {
		return setters.clone();
	}

	public static BulkBean create(final Class target, final String[] getters,
			final String[] setters, final Class[] types) {
		final Generator gen = new Generator();
		gen.setTarget(target);
		gen.setGetters(getters);
		gen.setSetters(setters);
		gen.setTypes(types);
		return gen.create();
	}

	public static class Generator extends AbstractClassGenerator {
		private static final Source SOURCE = new Source(BulkBean.class.getName());
		private Class target;
		private String[] getters;
		private String[] setters;
		private Class[] types;

		public Generator() {
			super(SOURCE);
		}

		public void setTarget(final Class target) {
			this.target = target;
		}

		public void setGetters(final String[] getters) {
			this.getters = getters;
		}

		public void setSetters(final String[] setters) {
			this.setters = setters;
		}

		public void setTypes(final Class[] types) {
			this.types = types;
		}

		@Override
		protected ClassLoader getDefaultClassLoader() {
			return target.getClassLoader();
		}

		@Override
		protected ProtectionDomain getProtectionDomain() {
			return ReflectUtils.getProtectionDomain(target);
		}

		public BulkBean create() {
			setNamePrefix(target.getName());
			final String targetClassName = target.getName();
			final String[] typeClassNames = ReflectUtils.getNames(types);
			final Object key = KEY_FACTORY.newInstance(targetClassName, getters, setters,
					typeClassNames);
			return (BulkBean) super.create(key);
		}

		@Override
		public void generateClass(final ClassVisitor v) throws Exception {
			new BulkBeanEmitter(v, getClassName(), target, getters, setters, types);
		}

		@Override
		protected Object firstInstance(final Class type) {
			final BulkBean instance = (BulkBean) ReflectUtils.newInstance(type);
			instance.target = target;

			final int length = getters.length;
			instance.getters = new String[length];
			System.arraycopy(getters, 0, instance.getters, 0, length);

			instance.setters = new String[length];
			System.arraycopy(setters, 0, instance.setters, 0, length);

			instance.types = new Class[types.length];
			System.arraycopy(types, 0, instance.types, 0, types.length);

			return instance;
		}

		@Override
		protected Object nextInstance(final Object instance) {
			return instance;
		}
	}
}
