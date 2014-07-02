/**
 * MVEL 2.0
 * Copyright (C) 2007 The Codehaus
 * Mike Brock, Dhanji Prasanna, John Graham, Mark Proctor
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

package net.simpleframework.lib.org.mvel2.templates.res;

import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;
import net.simpleframework.lib.org.mvel2.templates.TemplateRuntime;
import net.simpleframework.lib.org.mvel2.templates.util.TemplateOutputStream;

public class CommentNode extends Node {
	public CommentNode() {
	}

	public CommentNode(final int begin, final String name, final char[] template, final int start,
			final int end) {
		this.name = name;
		this.end = this.cEnd = end;
	}

	public CommentNode(final int begin, final String name, final char[] template, final int start,
			final int end, final Node next) {
		this.begin = begin;
		this.end = this.cEnd = end;
		this.next = next;
	}

	@Override
	public Object eval(final TemplateRuntime runtime, final TemplateOutputStream appender,
			final Object ctx, final VariableResolverFactory factory) {
		if (next != null) {
			return next.eval(runtime, appender, ctx, factory);
		} else {
			return null;
		}
	}

	@Override
	public boolean demarcate(final Node terminatingNode, final char[] template) {
		return false;
	}
}
