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
package net.simpleframework.lib.net.sf.cglib.transform;

import net.simpleframework.lib.org.objectweb.asm.AnnotationVisitor;
import net.simpleframework.lib.org.objectweb.asm.Attribute;
import net.simpleframework.lib.org.objectweb.asm.ClassVisitor;
import net.simpleframework.lib.org.objectweb.asm.FieldVisitor;
import net.simpleframework.lib.org.objectweb.asm.MethodVisitor;
import net.simpleframework.lib.org.objectweb.asm.Opcodes;
import net.simpleframework.lib.org.objectweb.asm.TypePath;

public class ClassVisitorTee extends ClassVisitor {
	private ClassVisitor cv1, cv2;

	public ClassVisitorTee(final ClassVisitor cv1, final ClassVisitor cv2) {
		super(Opcodes.ASM5);
		this.cv1 = cv1;
		this.cv2 = cv2;
	}

	@Override
	public void visit(final int version, final int access, final String name, final String signature,
			final String superName, final String[] interfaces) {
		cv1.visit(version, access, name, signature, superName, interfaces);
		cv2.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public void visitEnd() {
		cv1.visitEnd();
		cv2.visitEnd();
		cv1 = cv2 = null;
	}

	@Override
	public void visitInnerClass(final String name, final String outerName, final String innerName,
			final int access) {
		cv1.visitInnerClass(name, outerName, innerName, access);
		cv2.visitInnerClass(name, outerName, innerName, access);
	}

	@Override
	public FieldVisitor visitField(final int access, final String name, final String desc,
			final String signature, final Object value) {
		final FieldVisitor fv1 = cv1.visitField(access, name, desc, signature, value);
		final FieldVisitor fv2 = cv2.visitField(access, name, desc, signature, value);
		if (fv1 == null) {
			return fv2;
		}
		if (fv2 == null) {
			return fv1;
		}
		return new FieldVisitorTee(fv1, fv2);
	}

	@Override
	public MethodVisitor visitMethod(final int access, final String name, final String desc,
			final String signature, final String[] exceptions) {
		final MethodVisitor mv1 = cv1.visitMethod(access, name, desc, signature, exceptions);
		final MethodVisitor mv2 = cv2.visitMethod(access, name, desc, signature, exceptions);
		if (mv1 == null) {
			return mv2;
		}
		if (mv2 == null) {
			return mv1;
		}
		return new MethodVisitorTee(mv1, mv2);
	}

	@Override
	public void visitSource(final String source, final String debug) {
		cv1.visitSource(source, debug);
		cv2.visitSource(source, debug);
	}

	@Override
	public void visitOuterClass(final String owner, final String name, final String desc) {
		cv1.visitOuterClass(owner, name, desc);
		cv2.visitOuterClass(owner, name, desc);
	}

	@Override
	public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
		return AnnotationVisitorTee.getInstance(cv1.visitAnnotation(desc, visible),
				cv2.visitAnnotation(desc, visible));
	}

	@Override
	public void visitAttribute(final Attribute attrs) {
		cv1.visitAttribute(attrs);
		cv2.visitAttribute(attrs);
	}

	@Override
	public AnnotationVisitor visitTypeAnnotation(final int typeRef, final TypePath typePath,
			final String desc, final boolean visible) {
		return AnnotationVisitorTee.getInstance(
				cv1.visitTypeAnnotation(typeRef, typePath, desc, visible),
				cv2.visitTypeAnnotation(typeRef, typePath, desc, visible));
	}
}
