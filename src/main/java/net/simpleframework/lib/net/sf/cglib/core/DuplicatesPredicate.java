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
package net.simpleframework.lib.net.sf.cglib.core;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.simpleframework.lib.org.objectweb.asm.ClassReader;
import net.simpleframework.lib.org.objectweb.asm.ClassVisitor;
import net.simpleframework.lib.org.objectweb.asm.MethodVisitor;
import net.simpleframework.lib.org.objectweb.asm.Opcodes;

public class DuplicatesPredicate implements Predicate {
	private final Set unique;
	private final Set rejected;

	/**
	 * Constructs a DuplicatesPredicate that will allow subclass bridge methods
	 * to be preferred over
	 * superclass non-bridge methods.
	 */
	public DuplicatesPredicate() {
		unique = new HashSet();
		rejected = Collections.emptySet();
	}

	/**
	 * Constructs a DuplicatesPredicate that prefers using superclass non-bridge
	 * methods despite a
	 * subclass method with the same signtaure existing (if the subclass is a
	 * bridge method).
	 */
	public DuplicatesPredicate(final List allMethods) {
		rejected = new HashSet();
		unique = new HashSet();

		// Traverse through the methods and capture ones that are bridge
		// methods when a subsequent method (from a non-interface superclass)
		// has the same signature but isn't a bridge. Record these so that
		// we avoid using them when filtering duplicates.
		final Map scanned = new HashMap();
		final Map suspects = new HashMap();
		for (final Object o : allMethods) {
			final Method method = (Method) o;
			final Object sig = MethodWrapper.create(method);
			final Method existing = (Method) scanned.get(sig);
			if (existing == null) {
				scanned.put(sig, method);
			} else if (!suspects.containsKey(sig) && existing.isBridge() && !method.isBridge()) {
				// TODO: this currently only will capture a single bridge. it will
				// not work
				// if there's Child.bridge1 Middle.bridge2 Parent.concrete. (we'd
				// offer the 2nd bridge).
				// no idea if that's even possible tho...
				suspects.put(sig, existing);
			}
		}

		if (!suspects.isEmpty()) {
			final Set classes = new HashSet();
			final UnnecessaryBridgeFinder finder = new UnnecessaryBridgeFinder(rejected);
			for (final Object o : suspects.values()) {
				final Method m = (Method) o;
				classes.add(m.getDeclaringClass());
				finder.addSuspectMethod(m);
			}
			for (final Object o : classes) {
				final Class c = (Class) o;
				try {
					final ClassLoader cl = getClassLoader(c);
					if (cl == null) {
						continue;
					}
					final InputStream is = cl
							.getResourceAsStream(c.getName().replace('.', '/') + ".class");
					if (is == null) {
						continue;
					}
					try {
						new ClassReader(is).accept(finder,
								ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
					} finally {
						is.close();
					}
				} catch (final IOException ignored) {
				}
			}
		}
	}

	@Override
	public boolean evaluate(final Object arg) {
		return !rejected.contains(arg) && unique.add(MethodWrapper.create((Method) arg));
	}

	private static ClassLoader getClassLoader(final Class c) {
		ClassLoader cl = c.getClassLoader();
		if (cl == null) {
			cl = DuplicatesPredicate.class.getClassLoader();
		}
		if (cl == null) {
			cl = Thread.currentThread().getContextClassLoader();
		}
		return cl;
	}

	private static class UnnecessaryBridgeFinder extends ClassVisitor {
		private final Set rejected;

		private Signature currentMethodSig = null;
		private final Map methods = new HashMap();

		UnnecessaryBridgeFinder(final Set rejected) {
			super(Constants.ASM_API);
			this.rejected = rejected;
		}

		void addSuspectMethod(final Method m) {
			methods.put(ReflectUtils.getSignature(m), m);
		}

		@Override
		public void visit(final int version, final int access, final String name,
				final String signature, final String superName, final String[] interfaces) {
		}

		@Override
		public MethodVisitor visitMethod(final int access, final String name, final String desc,
				final String signature, final String[] exceptions) {
			final Signature sig = new Signature(name, desc);
			final Method currentMethod = (Method) methods.remove(sig);
			if (currentMethod != null) {
				currentMethodSig = sig;
				return new MethodVisitor(Constants.ASM_API) {
					@Override
					public void visitMethodInsn(final int opcode, final String owner, final String name,
							final String desc, final boolean itf) {
						if (opcode == Opcodes.INVOKESPECIAL && currentMethodSig != null) {
							final Signature target = new Signature(name, desc);
							if (target.equals(currentMethodSig)) {
								rejected.add(currentMethod);
							}
							currentMethodSig = null;
						}
					}
				};
			} else {
				return null;
			}
		}
	}
}
