/*
 * Copyright 2004 The Apache Software Foundation
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
package net.simpleframework.lib.net.sf.cglib.transform;

import net.simpleframework.lib.org.objectweb.asm.AnnotationVisitor;
import net.simpleframework.lib.org.objectweb.asm.Attribute;
import net.simpleframework.lib.org.objectweb.asm.ClassVisitor;
import net.simpleframework.lib.org.objectweb.asm.FieldVisitor;
import net.simpleframework.lib.org.objectweb.asm.MethodVisitor;

abstract public class AbstractClassFilterTransformer extends AbstractClassTransformer {
	private final ClassTransformer pass;
	private ClassVisitor target;

	@Override
	public void setTarget(final ClassVisitor target) {
		super.setTarget(target);
		pass.setTarget(target);
	}

	protected AbstractClassFilterTransformer(final ClassTransformer pass) {
		this.pass = pass;
	}

	abstract protected boolean accept(int version, int access, String name, String signature,
			String superName, String[] interfaces);

	@Override
	public void visit(final int version, final int access, final String name,
			final String signature, final String superName, final String[] interfaces) {
		target = accept(version, access, name, signature, superName, interfaces) ? pass : cv;
		target.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public void visitSource(final String source, final String debug) {
		target.visitSource(source, debug);
	}

	@Override
	public void visitOuterClass(final String owner, final String name, final String desc) {
		target.visitOuterClass(owner, name, desc);
	}

	@Override
	public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
		return target.visitAnnotation(desc, visible);
	}

	@Override
	public void visitAttribute(final Attribute attr) {
		target.visitAttribute(attr);
	}

	@Override
	public void visitInnerClass(final String name, final String outerName, final String innerName,
			final int access) {
		target.visitInnerClass(name, outerName, innerName, access);
	}

	@Override
	public FieldVisitor visitField(final int access, final String name, final String desc,
			final String signature, final Object value) {
		return target.visitField(access, name, desc, signature, value);
	}

	@Override
	public MethodVisitor visitMethod(final int access, final String name, final String desc,
			final String signature, final String[] exceptions) {
		return target.visitMethod(access, name, desc, signature, exceptions);
	}

	@Override
	public void visitEnd() {
		target.visitEnd();
		target = null; // just to be safe
	}
}
