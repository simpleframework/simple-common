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
package net.simpleframework.lib.net.sf.cglib.util;

import java.util.Arrays;
import java.util.List;

import net.simpleframework.lib.net.sf.cglib.core.AbstractClassGenerator;
import net.simpleframework.lib.net.sf.cglib.core.ClassEmitter;
import net.simpleframework.lib.net.sf.cglib.core.CodeEmitter;
import net.simpleframework.lib.net.sf.cglib.core.Constants;
import net.simpleframework.lib.net.sf.cglib.core.EmitUtils;
import net.simpleframework.lib.net.sf.cglib.core.KeyFactory;
import net.simpleframework.lib.net.sf.cglib.core.ObjectSwitchCallback;
import net.simpleframework.lib.net.sf.cglib.core.ReflectUtils;
import net.simpleframework.lib.net.sf.cglib.core.Signature;
import net.simpleframework.lib.net.sf.cglib.core.TypeUtils;
import net.simpleframework.lib.org.objectweb.asm.ClassVisitor;
import net.simpleframework.lib.org.objectweb.asm.Label;
import net.simpleframework.lib.org.objectweb.asm.Opcodes;
import net.simpleframework.lib.org.objectweb.asm.Type;

/**
 * This class implements a simple String->int mapping for a fixed set of keys.
 */
abstract public class StringSwitcher {
	private static final Type STRING_SWITCHER = TypeUtils
			.parseType("net.simpleframework.lib.net.sf.cglib.util.StringSwitcher");
	private static final Signature INT_VALUE = TypeUtils.parseSignature("int intValue(String)");
	private static final StringSwitcherKey KEY_FACTORY = (StringSwitcherKey) KeyFactory
			.create(StringSwitcherKey.class);

	interface StringSwitcherKey {
		public Object newInstance(String[] strings, int[] ints, boolean fixedInput);
	}

	/**
	 * Helper method to create a StringSwitcher.
	 * For finer control over the generated instance, use a new instance of
	 * StringSwitcher.Generator
	 * instead of this static method.
	 * 
	 * @param strings
	 *        the array of String keys; must be the same length as the value
	 *        array
	 * @param ints
	 *        the array of integer results; must be the same length as the key
	 *        array
	 * @param fixedInput
	 *        if false, an unknown key will be returned from {@link #intValue} as
	 *        <code>-1</code>; if true,
	 *        the result will be undefined, and the resulting code will be faster
	 */
	public static StringSwitcher create(final String[] strings, final int[] ints,
			final boolean fixedInput) {
		final Generator gen = new Generator();
		gen.setStrings(strings);
		gen.setInts(ints);
		gen.setFixedInput(fixedInput);
		return gen.create();
	}

	protected StringSwitcher() {
	}

	/**
	 * Return the integer associated with the given key.
	 * 
	 * @param s
	 *        the key
	 * @return the associated integer value, or <code>-1</code> if the key is
	 *         unknown (unless
	 *         <code>fixedInput</code> was specified when this
	 *         <code>StringSwitcher</code> was created,
	 *         in which case the return value for an unknown key is undefined)
	 */
	abstract public int intValue(String s);

	public static class Generator extends AbstractClassGenerator {
		private static final Source SOURCE = new Source(StringSwitcher.class.getName());

		private String[] strings;
		private int[] ints;
		private boolean fixedInput;

		public Generator() {
			super(SOURCE);
		}

		/**
		 * Set the array of recognized Strings.
		 * 
		 * @param strings
		 *        the array of String keys; must be the same length as the value
		 *        array
		 * @see #setInts
		 */
		public void setStrings(final String[] strings) {
			this.strings = strings;
		}

		/**
		 * Set the array of integer results.
		 * 
		 * @param ints
		 *        the array of integer results; must be the same length as the key
		 *        array
		 * @see #setStrings
		 */
		public void setInts(final int[] ints) {
			this.ints = ints;
		}

		/**
		 * Configure how unknown String keys will be handled.
		 * 
		 * @param fixedInput
		 *        if false, an unknown key will be returned from {@link #intValue}
		 *        as <code>-1</code>; if true,
		 *        the result will be undefined, and the resulting code will be
		 *        faster
		 */
		public void setFixedInput(final boolean fixedInput) {
			this.fixedInput = fixedInput;
		}

		@Override
		protected ClassLoader getDefaultClassLoader() {
			return getClass().getClassLoader();
		}

		/**
		 * Generate the <code>StringSwitcher</code>.
		 */
		public StringSwitcher create() {
			setNamePrefix(StringSwitcher.class.getName());
			final Object key = KEY_FACTORY.newInstance(strings, ints, fixedInput);
			return (StringSwitcher) super.create(key);
		}

		@Override
		public void generateClass(final ClassVisitor v) throws Exception {
			final ClassEmitter ce = new ClassEmitter(v);
			ce.begin_class(Opcodes.V1_8, Opcodes.ACC_PUBLIC, getClassName(), STRING_SWITCHER, null,
					Constants.SOURCE_FILE);
			EmitUtils.null_constructor(ce);
			final CodeEmitter e = ce.begin_method(Opcodes.ACC_PUBLIC, INT_VALUE, null);
			e.load_arg(0);
			final List stringList = Arrays.asList(strings);
			final int style = fixedInput ? Constants.SWITCH_STYLE_HASHONLY
					: Constants.SWITCH_STYLE_HASH;
			EmitUtils.string_switch(e, strings, style, new ObjectSwitchCallback() {
				@Override
				public void processCase(final Object key, final Label end) {
					e.push(ints[stringList.indexOf(key)]);
					e.return_value();
				}

				@Override
				public void processDefault() {
					e.push(-1);
					e.return_value();
				}
			});
			e.end_method();
			ce.end_class();
		}

		@Override
		protected Object firstInstance(final Class type) {
			return ReflectUtils.newInstance(type);
		}

		@Override
		protected Object nextInstance(final Object instance) {
			return instance;
		}
	}
}
