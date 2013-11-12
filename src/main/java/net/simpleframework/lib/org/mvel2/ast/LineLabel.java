/**
 * MVEL 2.0
 * Copyright (C) 2007 The Codehaus
 * Mike Brock, Dhanji Prasanna, John Graham, Mark Proctor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.simpleframework.lib.org.mvel2.ast;

import net.simpleframework.lib.org.mvel2.ParserContext;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;

/**
 * @author Christopher Brock
 */
public class LineLabel extends ASTNode {
	private String sourceFile;
	private int lineNumber;

	public LineLabel(final String sourceFile, final int lineNumber, final ParserContext pCtx) {
		super(pCtx);
		this.lineNumber = lineNumber;
		this.sourceFile = sourceFile;
		this.fields = -1;
	}

	public String getSourceFile() {
		return sourceFile;
	}

	public void setSourceFile(final String sourceFile) {
		this.sourceFile = sourceFile;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(final int lineNumber) {
		this.lineNumber = lineNumber;
	}

	@Override
	public Object getReducedValueAccelerated(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		return null;
	}

	@Override
	public Object getReducedValue(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		return null;
	}

	@Override
	public String toString() {
		return "[SourceLine:" + lineNumber + "]";
	}
}
