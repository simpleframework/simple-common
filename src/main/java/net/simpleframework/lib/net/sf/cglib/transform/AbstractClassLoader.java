/*
 * Copyright 2003,2004 The Apache Software Foundation
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
package net.simpleframework.lib.net.sf.cglib.transform;

import java.io.IOException;

import net.simpleframework.lib.net.sf.cglib.core.ClassGenerator;
import net.simpleframework.lib.net.sf.cglib.core.CodeGenerationException;
import net.simpleframework.lib.net.sf.cglib.core.DebuggingClassWriter;
import net.simpleframework.lib.org.objectweb.asm.Attribute;
import net.simpleframework.lib.org.objectweb.asm.ClassReader;
import net.simpleframework.lib.org.objectweb.asm.ClassWriter;

abstract public class AbstractClassLoader extends ClassLoader {
	private final ClassFilter filter;
	private final ClassLoader classPath;
	private static java.security.ProtectionDomain DOMAIN;

	static {

		DOMAIN = (java.security.ProtectionDomain) java.security.AccessController
				.doPrivileged(new java.security.PrivilegedAction() {
					@Override
					public Object run() {
						return AbstractClassLoader.class.getProtectionDomain();
					}
				});
	}

	protected AbstractClassLoader(final ClassLoader parent, final ClassLoader classPath,
			final ClassFilter filter) {
		super(parent);
		this.filter = filter;
		this.classPath = classPath;
	}

	@Override
	public Class loadClass(final String name) throws ClassNotFoundException {

		final Class loaded = findLoadedClass(name);

		if (loaded != null) {
			if (loaded.getClassLoader() == this) {
				return loaded;
			} // else reload with this class loader
		}

		if (!filter.accept(name)) {
			return super.loadClass(name);
		}
		ClassReader r;
		try {

			final java.io.InputStream is = classPath
					.getResourceAsStream(name.replace('.', '/') + ".class");

			if (is == null) {

				throw new ClassNotFoundException(name);

			}
			try {

				r = new ClassReader(is);

			} finally {

				is.close();

			}
		} catch (final IOException e) {
			throw new ClassNotFoundException(name + ":" + e.getMessage());
		}

		try {
			final DebuggingClassWriter w = new DebuggingClassWriter(ClassWriter.COMPUTE_FRAMES);
			getGenerator(r).generateClass(w);
			final byte[] b = w.toByteArray();
			final Class c = super.defineClass(name, b, 0, b.length, DOMAIN);
			postProcess(c);
			return c;
		} catch (final RuntimeException e) {
			throw e;
		} catch (final Error e) {
			throw e;
		} catch (final Exception e) {
			throw new CodeGenerationException(e);
		}
	}

	protected ClassGenerator getGenerator(final ClassReader r) {
		return new ClassReaderGenerator(r, attributes(), getFlags());
	}

	protected int getFlags() {
		return 0;
	}

	protected Attribute[] attributes() {
		return null;
	}

	protected void postProcess(final Class c) {
	}
}
