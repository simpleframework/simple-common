package net.simpleframework.lib.org.mvel2.ast;

import static net.simpleframework.lib.org.mvel2.util.Soundex.soundex;

import net.simpleframework.lib.org.mvel2.CompileException;
import net.simpleframework.lib.org.mvel2.ParserContext;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;
import net.simpleframework.lib.org.mvel2.util.CompilerTools;

public class Soundslike extends ASTNode {
	private final ASTNode stmt;
	private final ASTNode soundslike;

	public Soundslike(final ASTNode stmt, final ASTNode clsStmt, final ParserContext pCtx) {
		super(pCtx);
		this.stmt = stmt;
		this.soundslike = clsStmt;
		CompilerTools.expectType(pCtx, clsStmt, String.class, true);
	}

	@Override
	public Object getReducedValueAccelerated(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		final String str1 = String
				.valueOf(soundslike.getReducedValueAccelerated(ctx, thisValue, factory));
		final String str2 = (String) stmt.getReducedValueAccelerated(ctx, thisValue, factory);
		return str1 == null ? str2 == null
				: (str2 == null ? false : soundex(str1).equals(soundex(str2)));
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
						stmt.getStart());
			}

			return soundex(i).equals(soundex(x));
		} catch (final ClassCastException e) {
			throw new CompileException("not a string: " + soundslike.getName(), soundslike.getExpr(),
					soundslike.getStart());
		}

	}

	@Override
	public Class getEgressType() {
		return Boolean.class;
	}

	public ASTNode getStatement() {
		return stmt;
	}

	public ASTNode getSoundslike() {
		return soundslike;
	}
}
