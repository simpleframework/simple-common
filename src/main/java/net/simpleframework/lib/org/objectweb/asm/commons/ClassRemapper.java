/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000-2011 INRIA, France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.simpleframework.lib.org.objectweb.asm.commons;

import net.simpleframework.lib.org.objectweb.asm.AnnotationVisitor;
import net.simpleframework.lib.org.objectweb.asm.ClassVisitor;
import net.simpleframework.lib.org.objectweb.asm.FieldVisitor;
import net.simpleframework.lib.org.objectweb.asm.MethodVisitor;
import net.simpleframework.lib.org.objectweb.asm.Opcodes;
import net.simpleframework.lib.org.objectweb.asm.TypePath;

/**
 * A {@link ClassVisitor} for type remapping.
 * 
 * @author Eugene Kuleshov
 */
public class ClassRemapper extends ClassVisitor {

	protected final Remapper remapper;

	protected String className;

	public ClassRemapper(final ClassVisitor cv, final Remapper remapper) {
		this(Opcodes.ASM5, cv, remapper);
	}

	protected ClassRemapper(final int api, final ClassVisitor cv, final Remapper remapper) {
		super(api, cv);
		this.remapper = remapper;
	}

	@Override
	public void visit(final int version, final int access, final String name,
			final String signature, final String superName, final String[] interfaces) {
		this.className = name;
		super.visit(version, access, remapper.mapType(name), remapper.mapSignature(signature, false),
				remapper.mapType(superName), interfaces == null ? null : remapper.mapTypes(interfaces));
	}

	@Override
	public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
		final AnnotationVisitor av = super.visitAnnotation(remapper.mapDesc(desc), visible);
		return av == null ? null : createAnnotationRemapper(av);
	}

	@Override
	public AnnotationVisitor visitTypeAnnotation(final int typeRef, final TypePath typePath,
			final String desc, final boolean visible) {
		final AnnotationVisitor av = super.visitTypeAnnotation(typeRef, typePath,
				remapper.mapDesc(desc), visible);
		return av == null ? null : createAnnotationRemapper(av);
	}

	@Override
	public FieldVisitor visitField(final int access, final String name, final String desc,
			final String signature, final Object value) {
		final FieldVisitor fv = super.visitField(access,
				remapper.mapFieldName(className, name, desc), remapper.mapDesc(desc),
				remapper.mapSignature(signature, true), remapper.mapValue(value));
		return fv == null ? null : createFieldRemapper(fv);
	}

	@Override
	public MethodVisitor visitMethod(final int access, final String name, final String desc,
			final String signature, final String[] exceptions) {
		final String newDesc = remapper.mapMethodDesc(desc);
		final MethodVisitor mv = super.visitMethod(access,
				remapper.mapMethodName(className, name, desc), newDesc,
				remapper.mapSignature(signature, false),
				exceptions == null ? null : remapper.mapTypes(exceptions));
		return mv == null ? null : createMethodRemapper(mv);
	}

	@Override
	public void visitInnerClass(final String name, final String outerName, final String innerName,
			final int access) {
		// TODO should innerName be changed?
		super.visitInnerClass(remapper.mapType(name),
				outerName == null ? null : remapper.mapType(outerName), innerName, access);
	}

	@Override
	public void visitOuterClass(final String owner, final String name, final String desc) {
		super.visitOuterClass(remapper.mapType(owner),
				name == null ? null : remapper.mapMethodName(owner, name, desc), desc == null ? null
						: remapper.mapMethodDesc(desc));
	}

	protected FieldVisitor createFieldRemapper(final FieldVisitor fv) {
		return new FieldRemapper(fv, remapper);
	}

	protected MethodVisitor createMethodRemapper(final MethodVisitor mv) {
		return new MethodRemapper(mv, remapper);
	}

	protected AnnotationVisitor createAnnotationRemapper(final AnnotationVisitor av) {
		return new AnnotationRemapper(av, remapper);
	}
}
