package net.simpleframework.lib.org.mvel2.ast;

import net.simpleframework.lib.org.mvel2.DataConversion;
import net.simpleframework.lib.org.mvel2.ParserContext;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;
import net.simpleframework.lib.org.mvel2.util.CompilerTools;

public class Convertable extends ASTNode {
	private final ASTNode stmt;
	private final ASTNode clsStmt;

	public Convertable(final ASTNode stmt, final ASTNode clsStmt, final ParserContext pCtx) {
		super(pCtx);
		this.stmt = stmt;
		this.clsStmt = clsStmt;
		CompilerTools.expectType(pCtx, clsStmt, Class.class, true);
	}

	@Override
	public Object getReducedValueAccelerated(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		final Object o = stmt.getReducedValueAccelerated(ctx, thisValue, factory);
		return o != null && DataConversion.canConvert(
				(Class) clsStmt.getReducedValueAccelerated(ctx, thisValue, factory), o.getClass());

	}

	@Override
	public Object getReducedValue(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		try {

			final Object o = stmt.getReducedValue(ctx, thisValue, factory);
			if (o == null) {
				return false;
			}

			final Class i = (Class) clsStmt.getReducedValue(ctx, thisValue, factory);
			if (i == null) {
				throw new ClassCastException();
			}

			return DataConversion.canConvert(i, o.getClass());
		} catch (final ClassCastException e) {
			throw new RuntimeException("not a class reference: " + clsStmt.getName());
		}

	}

	@Override
	public Class getEgressType() {
		return Boolean.class;
	}
}
