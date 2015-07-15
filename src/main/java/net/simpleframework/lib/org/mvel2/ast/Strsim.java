package net.simpleframework.lib.org.mvel2.ast;

import static net.simpleframework.lib.org.mvel2.util.ParseTools.similarity;
import net.simpleframework.lib.org.mvel2.CompileException;
import net.simpleframework.lib.org.mvel2.ParserContext;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;
import net.simpleframework.lib.org.mvel2.util.CompilerTools;

public class Strsim extends ASTNode {
	private final ASTNode stmt;
	private final ASTNode soundslike;

	public Strsim(final ASTNode stmt, final ASTNode clsStmt, final ParserContext pCtx) {
		super(pCtx);
		this.stmt = stmt;
		this.soundslike = clsStmt;
		CompilerTools.expectType(pCtx, clsStmt, String.class, true);
	}

	@Override
	public Object getReducedValueAccelerated(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		return similarity(
				String.valueOf(soundslike.getReducedValueAccelerated(ctx, thisValue, factory)),
				((String) stmt.getReducedValueAccelerated(ctx, thisValue, factory)));
	}

	@Override
	public Object getReducedValue(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		try {
			final String i = String.valueOf(soundslike.getReducedValue(ctx, thisValue, factory));
			if (i == null) {
				throw new ClassCastException();
			}

			final String x = (String) stmt.getReducedValue(ctx, thisValue, factory);
			if (x == null) {
				throw new CompileException("not a string: " + stmt.getName(), stmt.getExpr(),
						getStart());
			}

			return similarity(i, x);
		} catch (final ClassCastException e) {
			throw new CompileException("not a string: " + soundslike.getName(), soundslike.getExpr(),
					soundslike.getStart());
		}

	}

	@Override
	public Class getEgressType() {
		return Boolean.class;
	}
}
