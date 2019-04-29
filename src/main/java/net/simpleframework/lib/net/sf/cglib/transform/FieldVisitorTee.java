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
import net.simpleframework.lib.org.objectweb.asm.FieldVisitor;
import net.simpleframework.lib.org.objectweb.asm.TypePath;

public class FieldVisitorTee extends FieldVisitor {
	private final FieldVisitor fv1, fv2;

	public FieldVisitorTee(final FieldVisitor fv1, final FieldVisitor fv2) {
		super(Constants.ASM_API);
		this.fv1 = fv1;
		this.fv2 = fv2;
	}

	@Override
	public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
		return AnnotationVisitorTee.getInstance(fv1.visitAnnotation(desc, visible),
				fv2.visitAnnotation(desc, visible));
	}

	@Override
	public void visitAttribute(final Attribute attr) {
		fv1.visitAttribute(attr);
		fv2.visitAttribute(attr);
	}

	@Override
	public void visitEnd() {
		fv1.visitEnd();
		fv2.visitEnd();
	}

	@Override
	public AnnotationVisitor visitTypeAnnotation(final int typeRef, final TypePath typePath,
			final String desc, final boolean visible) {
		return AnnotationVisitorTee.getInstance(
				fv1.visitTypeAnnotation(typeRef, typePath, desc, visible),
				fv2.visitTypeAnnotation(typeRef, typePath, desc, visible));
	}
}
