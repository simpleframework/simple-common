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

import net.simpleframework.lib.net.sf.cglib.core.Constants;
import net.simpleframework.lib.org.objectweb.asm.AnnotationVisitor;
import net.simpleframework.lib.org.objectweb.asm.Attribute;
import net.simpleframework.lib.org.objectweb.asm.Handle;
import net.simpleframework.lib.org.objectweb.asm.Label;
import net.simpleframework.lib.org.objectweb.asm.MethodVisitor;
import net.simpleframework.lib.org.objectweb.asm.TypePath;

public class MethodVisitorTee extends MethodVisitor {
	private final MethodVisitor mv1;
	private final MethodVisitor mv2;

	public MethodVisitorTee(final MethodVisitor mv1, final MethodVisitor mv2) {
		super(Constants.ASM_API);
		this.mv1 = mv1;
		this.mv2 = mv2;
	}

	@Override
	public void visitFrame(final int type, final int nLocal, final Object[] local, final int nStack,
			final Object[] stack) {
		mv1.visitFrame(type, nLocal, local, nStack, stack);
		mv2.visitFrame(type, nLocal, local, nStack, stack);
	}

	@Override
	public AnnotationVisitor visitAnnotationDefault() {
		return AnnotationVisitorTee.getInstance(mv1.visitAnnotationDefault(),
				mv2.visitAnnotationDefault());
	}

	@Override
	public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
		return AnnotationVisitorTee.getInstance(mv1.visitAnnotation(desc, visible),
				mv2.visitAnnotation(desc, visible));
	}

	@Override
	public AnnotationVisitor visitParameterAnnotation(final int parameter, final String desc,
			final boolean visible) {
		return AnnotationVisitorTee.getInstance(
				mv1.visitParameterAnnotation(parameter, desc, visible),
				mv2.visitParameterAnnotation(parameter, desc, visible));
	}

	@Override
	public void visitAttribute(final Attribute attr) {
		mv1.visitAttribute(attr);
		mv2.visitAttribute(attr);
	}

	@Override
	public void visitCode() {
		mv1.visitCode();
		mv2.visitCode();
	}

	@Override
	public void visitInsn(final int opcode) {
		mv1.visitInsn(opcode);
		mv2.visitInsn(opcode);
	}

	@Override
	public void visitIntInsn(final int opcode, final int operand) {
		mv1.visitIntInsn(opcode, operand);
		mv2.visitIntInsn(opcode, operand);
	}

	@Override
	public void visitVarInsn(final int opcode, final int var) {
		mv1.visitVarInsn(opcode, var);
		mv2.visitVarInsn(opcode, var);
	}

	@Override
	public void visitTypeInsn(final int opcode, final String desc) {
		mv1.visitTypeInsn(opcode, desc);
		mv2.visitTypeInsn(opcode, desc);
	}

	@Override
	public void visitFieldInsn(final int opcode, final String owner, final String name,
			final String desc) {
		mv1.visitFieldInsn(opcode, owner, name, desc);
		mv2.visitFieldInsn(opcode, owner, name, desc);
	}

	@Override
	public void visitMethodInsn(final int opcode, final String owner, final String name,
			final String desc) {
		mv1.visitMethodInsn(opcode, owner, name, desc);
		mv2.visitMethodInsn(opcode, owner, name, desc);
	}

	@Override
	public void visitMethodInsn(final int opcode, final String owner, final String name,
			final String desc, final boolean itf) {
		mv1.visitMethodInsn(opcode, owner, name, desc, itf);
		mv2.visitMethodInsn(opcode, owner, name, desc, itf);
	}

	@Override
	public void visitJumpInsn(final int opcode, final Label label) {
		mv1.visitJumpInsn(opcode, label);
		mv2.visitJumpInsn(opcode, label);
	}

	@Override
	public void visitLabel(final Label label) {
		mv1.visitLabel(label);
		mv2.visitLabel(label);
	}

	@Override
	public void visitLdcInsn(final Object cst) {
		mv1.visitLdcInsn(cst);
		mv2.visitLdcInsn(cst);
	}

	@Override
	public void visitIincInsn(final int var, final int increment) {
		mv1.visitIincInsn(var, increment);
		mv2.visitIincInsn(var, increment);
	}

	@Override
	public void visitTableSwitchInsn(final int min, final int max, final Label dflt,
			final Label labels[]) {
		mv1.visitTableSwitchInsn(min, max, dflt, labels);
		mv2.visitTableSwitchInsn(min, max, dflt, labels);
	}

	@Override
	public void visitLookupSwitchInsn(final Label dflt, final int keys[], final Label labels[]) {
		mv1.visitLookupSwitchInsn(dflt, keys, labels);
		mv2.visitLookupSwitchInsn(dflt, keys, labels);
	}

	@Override
	public void visitMultiANewArrayInsn(final String desc, final int dims) {
		mv1.visitMultiANewArrayInsn(desc, dims);
		mv2.visitMultiANewArrayInsn(desc, dims);
	}

	@Override
	public void visitTryCatchBlock(final Label start, final Label end, final Label handler,
			final String type) {
		mv1.visitTryCatchBlock(start, end, handler, type);
		mv2.visitTryCatchBlock(start, end, handler, type);
	}

	@Override
	public void visitLocalVariable(final String name, final String desc, final String signature,
			final Label start, final Label end, final int index) {
		mv1.visitLocalVariable(name, desc, signature, start, end, index);
		mv2.visitLocalVariable(name, desc, signature, start, end, index);
	}

	@Override
	public void visitLineNumber(final int line, final Label start) {
		mv1.visitLineNumber(line, start);
		mv2.visitLineNumber(line, start);
	}

	@Override
	public void visitMaxs(final int maxStack, final int maxLocals) {
		mv1.visitMaxs(maxStack, maxLocals);
		mv2.visitMaxs(maxStack, maxLocals);
	}

	@Override
	public void visitEnd() {
		mv1.visitEnd();
		mv2.visitEnd();
	}

	@Override
	public void visitParameter(final String name, final int access) {
		mv1.visitParameter(name, access);
		mv2.visitParameter(name, access);
	}

	@Override
	public AnnotationVisitor visitTypeAnnotation(final int typeRef, final TypePath typePath,
			final String desc, final boolean visible) {
		return AnnotationVisitorTee.getInstance(
				mv1.visitTypeAnnotation(typeRef, typePath, desc, visible),
				mv2.visitTypeAnnotation(typeRef, typePath, desc, visible));
	}

	@Override
	public void visitInvokeDynamicInsn(final String name, final String desc, final Handle bsm,
			final Object... bsmArgs) {
		mv1.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
		mv2.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
	}

	@Override
	public AnnotationVisitor visitInsnAnnotation(final int typeRef, final TypePath typePath,
			final String desc, final boolean visible) {
		return AnnotationVisitorTee.getInstance(
				mv1.visitInsnAnnotation(typeRef, typePath, desc, visible),
				mv2.visitInsnAnnotation(typeRef, typePath, desc, visible));
	}

	@Override
	public AnnotationVisitor visitTryCatchAnnotation(final int typeRef, final TypePath typePath,
			final String desc, final boolean visible) {
		return AnnotationVisitorTee.getInstance(
				mv1.visitTryCatchAnnotation(typeRef, typePath, desc, visible),
				mv2.visitTryCatchAnnotation(typeRef, typePath, desc, visible));
	}

	@Override
	public AnnotationVisitor visitLocalVariableAnnotation(final int typeRef, final TypePath typePath,
			final Label[] start, final Label[] end, final int[] index, final String desc,
			final boolean visible) {
		return AnnotationVisitorTee.getInstance(
				mv1.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, desc, visible),
				mv2.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, desc, visible));
	}
}
