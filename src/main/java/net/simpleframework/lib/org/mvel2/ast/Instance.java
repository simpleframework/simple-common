package net.simpleframework.lib.org.mvel2.ast;

import net.simpleframework.lib.org.mvel2.ParserContext;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;
import net.simpleframework.lib.org.mvel2.util.CompilerTools;

public class Instance extends ASTNode {
	private final ASTNode stmt;
	private final ASTNode clsStmt;

	public Instance(final ASTNode stmt, final ASTNode clsStmt, final ParserContext pCtx) {
		super(pCtx);
		this.stmt = stmt;
		this.clsStmt = clsStmt;
		CompilerTools.expectType(clsStmt, Class.class, true);
	}

	@Override
	public Object getReducedValueAccelerated(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		return ((Class) clsStmt.getReducedValueAccelerated(ctx, thisValue, factory)).isInstance(stmt
				.getReducedValueAccelerated(ctx, thisValue, factory));
	}

	@Override
	public Object getReducedValue(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		try {
			final Class i = (Class) clsStmt.getReducedValue(ctx, thisValue, factory);
			if (i == null) {
				throw new ClassCastException();
			}

			return i.isInstance(stmt.getReducedValue(ctx, thisValue, factory));
		} catch (final ClassCastException e) {
			throw new RuntimeException("not a class reference: " + clsStmt.getName());
		}

	}

	@Override
	public Class getEgressType() {
		return Boolean.class;
	}

	public ASTNode getStatement() {
		return stmt;
	}

	public ASTNode getClassStatement() {
		return clsStmt;
	}
}
