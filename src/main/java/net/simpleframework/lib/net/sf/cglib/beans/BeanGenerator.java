/*
 * Copyright 2003 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.simpleframework.lib.net.sf.cglib.beans;

import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.simpleframework.lib.net.sf.cglib.core.AbstractClassGenerator;
import net.simpleframework.lib.net.sf.cglib.core.ClassEmitter;
import net.simpleframework.lib.net.sf.cglib.core.Constants;
import net.simpleframework.lib.net.sf.cglib.core.EmitUtils;
import net.simpleframework.lib.net.sf.cglib.core.KeyFactory;
import net.simpleframework.lib.net.sf.cglib.core.ReflectUtils;
import net.simpleframework.lib.org.objectweb.asm.ClassVisitor;
import net.simpleframework.lib.org.objectweb.asm.Opcodes;
import net.simpleframework.lib.org.objectweb.asm.Type;

/**
 * @author Juozas Baliuka, Chris Nokleberg
 */
public class BeanGenerator extends AbstractClassGenerator {
	private static final Source SOURCE = new Source(BeanGenerator.class.getName());
	private static final BeanGeneratorKey KEY_FACTORY = (BeanGeneratorKey) KeyFactory
			.create(BeanGeneratorKey.class);

	interface BeanGeneratorKey {
		public Object newInstance(String superclass, Map props);
	}

	private Class superclass;
	private final Map props = new HashMap();
	private boolean classOnly;

	public BeanGenerator() {
		super(SOURCE);
	}

	/**
	 * Set the class which the generated class will extend. The class must not be
	 * declared as final, and must have a non-private no-argument constructor.
	 * 
	 * @param superclass
	 *           class to extend, or null to extend Object
	 */
	public void setSuperclass(Class superclass) {
		if (superclass != null && superclass.equals(Object.class)) {
			superclass = null;
		}
		this.superclass = superclass;
	}

	public void addProperty(final String name, final Class type) {
		if (props.containsKey(name)) {
			throw new IllegalArgumentException("Duplicate property name \"" + name + "\"");
		}
		props.put(name, Type.getType(type));
	}

	@Override
	protected ClassLoader getDefaultClassLoader() {
		if (superclass != null) {
			return superclass.getClassLoader();
		} else {
			return null;
		}
	}

	public Object create() {
		classOnly = false;
		return createHelper();
	}

	public Object createClass() {
		classOnly = true;
		return createHelper();
	}

	private Object createHelper() {
		if (superclass != null) {
			setNamePrefix(superclass.getName());
		}
		final String superName = (superclass != null) ? superclass.getName() : "java.lang.Object";
		final Object key = KEY_FACTORY.newInstance(superName, props);
		return super.create(key);
	}

	@Override
	public void generateClass(final ClassVisitor v) throws Exception {
		final int size = props.size();
		final String[] names = (String[]) props.keySet().toArray(new String[size]);
		final Type[] types = new Type[size];
		for (int i = 0; i < size; i++) {
			types[i] = (Type) props.get(names[i]);
		}
		final ClassEmitter ce = new ClassEmitter(v);
		ce.begin_class(Opcodes.V1_2, Opcodes.ACC_PUBLIC, getClassName(),
				superclass != null ? Type.getType(superclass) : Constants.TYPE_OBJECT, null, null);
		EmitUtils.null_constructor(ce);
		EmitUtils.add_properties(ce, names, types);
		ce.end_class();
	}

	@Override
	protected Object firstInstance(final Class type) {
		if (classOnly) {
			return type;
		} else {
			return ReflectUtils.newInstance(type);
		}
	}

	@Override
	protected Object nextInstance(final Object instance) {
		final Class protoclass = (instance instanceof Class) ? (Class) instance : instance.getClass();
		if (classOnly) {
			return protoclass;
		} else {
			return ReflectUtils.newInstance(protoclass);
		}
	}

	public static void addProperties(final BeanGenerator gen, final Map props) {
		for (final Iterator it = props.keySet().iterator(); it.hasNext();) {
			final String name = (String) it.next();
			gen.addProperty(name, (Class) props.get(name));
		}
	}

	public static void addProperties(final BeanGenerator gen, final Class type) {
		addProperties(gen, ReflectUtils.getBeanProperties(type));
	}

	public static void addProperties(final BeanGenerator gen, final PropertyDescriptor[] descriptors) {
		for (int i = 0; i < descriptors.length; i++) {
			gen.addProperty(descriptors[i].getName(), descriptors[i].getPropertyType());
		}
	}
}
