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

package net.simpleframework.lib.org.mvel2.ast;

import static net.simpleframework.lib.org.mvel2.util.ParseTools.findClassImportResolverFactory;
import net.simpleframework.lib.org.mvel2.CompileException;
import net.simpleframework.lib.org.mvel2.MVEL;
import net.simpleframework.lib.org.mvel2.ParserContext;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;
import net.simpleframework.lib.org.mvel2.integration.impl.ImmutableDefaultFactory;
import net.simpleframework.lib.org.mvel2.integration.impl.StackResetResolverFactory;
import net.simpleframework.lib.org.mvel2.util.ParseTools;

/**
 * @author Christopher Brock
 */
public class ImportNode extends ASTNode {
	private Class importClass;
	private boolean packageImport;
	private int _offset;
	ParserContext pCtx;

	private static final char[] WC_TEST = new char[] { '.', '*' };

	public ImportNode(final char[] expr, final int start, final int offset, final ParserContext pCtx) {
		super(pCtx);
		this.expr = expr;
		this.start = start;
		this.offset = offset;
		this.pCtx = pCtx;

		if (ParseTools.endsWith(expr, start, offset, WC_TEST)) {
			packageImport = true;
			_offset = (short) ParseTools.findLast(expr, start, offset, '.');
			if (_offset == -1) {
				_offset = 0;
			}
		} else {
			String clsName = new String(expr, start, offset);

			final ClassLoader classLoader = getClassLoader();

			try {
				this.importClass = Class.forName(clsName, true, classLoader);
			} catch (final ClassNotFoundException e) {
				int idx;
				clsName = (clsName.substring(0, idx = clsName.lastIndexOf('.')) + "$" + clsName
						.substring(idx + 1)).trim();

				try {
					this.importClass = Class.forName(clsName, true, classLoader);
				} catch (final ClassNotFoundException e2) {
					throw new CompileException("class not found: " + new String(expr), expr, start);
				}
			}
		}
	}

	@Override
	public Object getReducedValueAccelerated(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		if (!packageImport) {
			if (MVEL.COMPILER_OPT_ALLOCATE_TYPE_LITERALS_TO_SHARED_SYMBOL_TABLE) {
				factory.createVariable(importClass.getSimpleName(), importClass);
				return importClass;
			}
			return findClassImportResolverFactory(factory, pCtx).addClass(importClass);
		}

		// if the factory is an ImmutableDefaultFactory it means this import is
		// unused so we can skip it safely
		if (!(factory instanceof ImmutableDefaultFactory)
				&& !(factory instanceof StackResetResolverFactory && ((StackResetResolverFactory) factory)
						.getDelegate() instanceof ImmutableDefaultFactory)) {
			findClassImportResolverFactory(factory, pCtx).addPackageImport(
					new String(expr, start, _offset - start));
		}
		return null;
	}

	@Override
	public Object getReducedValue(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		return getReducedValueAccelerated(ctx, thisValue, factory);
	}

	public Class getImportClass() {
		return importClass;
	}

	public boolean isPackageImport() {
		return packageImport;
	}

	public void setPackageImport(final boolean packageImport) {
		this.packageImport = packageImport;
	}

	public String getPackageImport() {
		return new String(expr, start, _offset - start);
	}
}
