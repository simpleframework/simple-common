/*
 * Copyright 2011 The Apache Software Foundation
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

package net.simpleframework.lib.net.sf.cglib.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.simpleframework.lib.net.sf.cglib.core.Signature;
import net.simpleframework.lib.org.objectweb.asm.ClassReader;
import net.simpleframework.lib.org.objectweb.asm.ClassVisitor;
import net.simpleframework.lib.org.objectweb.asm.MethodVisitor;
import net.simpleframework.lib.org.objectweb.asm.Opcodes;

/**
 * Uses bytecode reflection to figure out the targets of all bridge methods
 * that use invokespecial, so that we can later rewrite them to use
 * invokevirtual.
 * 
 * @author sberlin@gmail.com (Sam Berlin)
 */
class BridgeMethodResolver {

	private final Map/* <Class, Set<Signature> */ declToBridge;
	private final ClassLoader classLoader;

	public BridgeMethodResolver(final Map declToBridge, final ClassLoader classLoader) {
		this.declToBridge = declToBridge;
		this.classLoader = classLoader;
	}

	/**
	 * Finds all bridge methods that are being called with invokespecial &
	 * returns them.
	 */
	public Map/* <Signature, Signature> */ resolveAll() {
		final Map resolved = new HashMap();
		for (final Iterator entryIter = declToBridge.entrySet().iterator(); entryIter.hasNext();) {
			final Map.Entry entry = (Map.Entry) entryIter.next();
			final Class owner = (Class) entry.getKey();
			final Set bridges = (Set) entry.getValue();
			try {
				final InputStream is = classLoader
						.getResourceAsStream(owner.getName().replace('.', '/') + ".class");
				if (is == null) {
					return resolved;
				}
				try {
					new ClassReader(is).accept(new BridgedFinder(bridges, resolved),
							ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
				} finally {
					is.close();
				}
			} catch (final IOException ignored) {
			}
		}
		return resolved;
	}

	private static class BridgedFinder extends ClassVisitor {
		private final Map/* <Signature, Signature> */ resolved;
		private final Set/* <Signature> */ eligibleMethods;

		private Signature currentMethod = null;

		BridgedFinder(final Set eligibleMethods, final Map resolved) {
			super(Opcodes.ASM6);
			this.resolved = resolved;
			this.eligibleMethods = eligibleMethods;
		}

		@Override
		public void visit(final int version, final int access, final String name,
				final String signature, final String superName, final String[] interfaces) {
		}

		@Override
		public MethodVisitor visitMethod(final int access, final String name, final String desc,
				final String signature, final String[] exceptions) {
			final Signature sig = new Signature(name, desc);
			if (eligibleMethods.remove(sig)) {
				currentMethod = sig;
				return new MethodVisitor(Opcodes.ASM6) {
					@Override
					public void visitMethodInsn(final int opcode, final String owner, final String name,
							final String desc, final boolean itf) {
						if (opcode == Opcodes.INVOKESPECIAL && currentMethod != null) {
							final Signature target = new Signature(name, desc);
							// If the target signature is the same as the current,
							// we shouldn't change our bridge becaues invokespecial
							// is the only way to make progress (otherwise we'll
							// get infinite recursion). This would typically
							// only happen when a bridge method is created to widen
							// the visibility of a superclass' method.
							if (!target.equals(currentMethod)) {
								resolved.put(currentMethod, target);
							}
							currentMethod = null;
						}
					}
				};
			} else {
				return null;
			}
		}
	}

}
