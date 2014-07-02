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

import net.simpleframework.lib.org.objectweb.asm.ClassVisitor;
import net.simpleframework.lib.org.objectweb.asm.MethodVisitor;

public class MethodFilterTransformer extends AbstractClassTransformer {
	private final MethodFilter filter;
	private final ClassTransformer pass;
	private ClassVisitor direct;

	public MethodFilterTransformer(final MethodFilter filter, final ClassTransformer pass) {
		this.filter = filter;
		this.pass = pass;
		super.setTarget(pass);
	}

	@Override
	public MethodVisitor visitMethod(final int access, final String name, final String desc,
			final String signature, final String[] exceptions) {
		return (filter.accept(access, name, desc, signature, exceptions) ? pass : direct)
				.visitMethod(access, name, desc, signature, exceptions);
	}

	@Override
	public void setTarget(final ClassVisitor target) {
		pass.setTarget(target);
		direct = target;
	}
}
