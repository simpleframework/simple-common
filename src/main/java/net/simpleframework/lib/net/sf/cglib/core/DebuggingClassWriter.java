/*
 * Copyright 2003,2004 The Apache Software Foundation
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
package net.simpleframework.lib.net.sf.cglib.core;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import net.simpleframework.lib.org.objectweb.asm.ClassReader;
import net.simpleframework.lib.org.objectweb.asm.ClassWriter;
import net.simpleframework.lib.org.objectweb.asm.util.TraceClassVisitor;

public class DebuggingClassWriter extends ClassWriter {

	public static final String DEBUG_LOCATION_PROPERTY = "cglib.debugLocation";

	private static String debugLocation;
	private static boolean traceEnabled;

	private String className;
	private String superName;

	static {
		debugLocation = System.getProperty(DEBUG_LOCATION_PROPERTY);
		if (debugLocation != null) {
			System.err.println("CGLIB debugging enabled, writing to '" + debugLocation + "'");
			try {
				Class.forName("org.objectweb.asm.util.TraceClassVisitor");
				traceEnabled = true;
			} catch (final Throwable ignore) {
			}
		}
	}

	public DebuggingClassWriter(final int flags) {
		super(flags);
	}

	@Override
	public void visit(final int version, final int access, final String name,
			final String signature, final String superName, final String[] interfaces) {
		className = name.replace('/', '.');
		this.superName = superName.replace('/', '.');
		super.visit(version, access, name, signature, superName, interfaces);
	}

	public String getClassName() {
		return className;
	}

	public String getSuperName() {
		return superName;
	}

	@Override
	public byte[] toByteArray() {

		return (byte[]) java.security.AccessController
				.doPrivileged(new java.security.PrivilegedAction() {
					@Override
					public Object run() {

						final byte[] b = DebuggingClassWriter.super.toByteArray();
						if (debugLocation != null) {
							final String dirs = className.replace('.', File.separatorChar);
							try {
								new File(debugLocation + File.separatorChar + dirs).getParentFile()
										.mkdirs();

								File file = new File(new File(debugLocation), dirs + ".class");
								OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
								try {
									out.write(b);
								} finally {
									out.close();
								}

								if (traceEnabled) {
									file = new File(new File(debugLocation), dirs + ".asm");
									out = new BufferedOutputStream(new FileOutputStream(file));
									try {
										final ClassReader cr = new ClassReader(b);
										final PrintWriter pw = new PrintWriter(new OutputStreamWriter(out));
										final TraceClassVisitor tcv = new TraceClassVisitor(null, pw);
										cr.accept(tcv, 0);
										pw.flush();
									} finally {
										out.close();
									}
								}
							} catch (final IOException e) {
								throw new CodeGenerationException(e);
							}
						}
						return b;
					}
				});

	}
}
