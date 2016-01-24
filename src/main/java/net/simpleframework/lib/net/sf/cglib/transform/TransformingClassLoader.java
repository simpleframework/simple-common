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

import net.simpleframework.lib.net.sf.cglib.core.ClassGenerator;
import net.simpleframework.lib.org.objectweb.asm.ClassReader;

public class TransformingClassLoader extends AbstractClassLoader {
	private final ClassTransformerFactory t;

	public TransformingClassLoader(final ClassLoader parent, final ClassFilter filter,
			final ClassTransformerFactory t) {
		super(parent, parent, filter);
		this.t = t;
	}

	@Override
	protected ClassGenerator getGenerator(final ClassReader r) {
		final ClassTransformer t2 = t.newInstance();
		return new TransformingClassGenerator(super.getGenerator(r), t2);
	}
}
