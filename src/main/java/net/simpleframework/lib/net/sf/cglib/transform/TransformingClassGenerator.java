/*
 * Copyright 2003 The Apache Software Foundation
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

import net.simpleframework.lib.net.sf.cglib.core.ClassGenerator;
import net.simpleframework.lib.org.objectweb.asm.ClassVisitor;

public class TransformingClassGenerator implements ClassGenerator {
	private final ClassGenerator gen;
	private final ClassTransformer t;

	public TransformingClassGenerator(final ClassGenerator gen, final ClassTransformer t) {
		this.gen = gen;
		this.t = t;
	}

	@Override
	public void generateClass(final ClassVisitor v) throws Exception {
		t.setTarget(v);
		gen.generateClass(t);
	}
}
