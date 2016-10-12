package net.simpleframework.lib.org.mvel2.ast;

import static net.simpleframework.lib.org.mvel2.util.ParseTools.boxPrimitive;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

import net.simpleframework.lib.org.mvel2.CompileException;
import net.simpleframework.lib.org.mvel2.MVEL;
import net.simpleframework.lib.org.mvel2.ParserContext;
import net.simpleframework.lib.org.mvel2.compiler.ExecutableStatement;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;
import net.simpleframework.lib.org.mvel2.util.ParseTools;

public class Sign extends ASTNode {
	private Signer signer;
	private ExecutableStatement stmt;

	public Sign(final char[] expr, final int start, final int end, final int fields,
			final ParserContext pCtx) {
		super(pCtx);
		this.expr = expr;
		this.start = start + 1;
		this.offset = end - 1;
		this.fields = fields;

		if ((fields & COMPILE_IMMEDIATE) != 0) {
			stmt = (ExecutableStatement) ParseTools.subCompileExpression(expr, this.start, this.offset,
					pCtx);

			egressType = stmt.getKnownEgressType();

			if (egressType != null && egressType != Object.class) {
				initSigner(egressType);
			}
		}
	}

	public ExecutableStatement getStatement() {
		return stmt;
	}

	@Override
	public Object getReducedValueAccelerated(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		return sign(stmt.getValue(ctx, thisValue, factory));
	}

	@Override
	public Object getReducedValue(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		return sign(MVEL.eval(expr, start, offset, thisValue, factory));
	}

	private Object sign(final Object o) {
		if (o == null) {
			return null;
		}
		if (signer == null) {
			if (egressType == null || egressType == Object.class) {
				egressType = o.getClass();
			}
			initSigner(egressType);
		}
		return signer.sign(o);
	}

	private void initSigner(Class type) {
		if (Integer.class.isAssignableFrom(type = boxPrimitive(type))) {
			signer = new IntegerSigner();
		} else if (Double.class.isAssignableFrom(type)) {
			signer = new DoubleSigner();
		} else if (Long.class.isAssignableFrom(type)) {
			signer = new LongSigner();
		} else if (Float.class.isAssignableFrom(type)) {
			signer = new FloatSigner();
		} else if (Short.class.isAssignableFrom(type)) {
			signer = new ShortSigner();
		} else if (BigInteger.class.isAssignableFrom(type)) {
			signer = new BigIntSigner();
		} else if (BigDecimal.class.isAssignableFrom(type)) {
			signer = new BigDecSigner();
		} else {
			throw new CompileException("illegal use of '-': cannot be applied to: " + type.getName(),
					expr, start);
		}

	}

	private interface Signer extends Serializable {
		public Object sign(Object o);
	}

	private class IntegerSigner implements Signer {
		@Override
		public Object sign(final Object o) {
			return -((Integer) o);
		}
	}

	private class ShortSigner implements Signer {
		@Override
		public Object sign(final Object o) {
			return -((Short) o);
		}
	}

	private class LongSigner implements Signer {
		@Override
		public Object sign(final Object o) {
			return -((Long) o);
		}
	}

	private class DoubleSigner implements Signer {
		@Override
		public Object sign(final Object o) {
			return -((Double) o);
		}
	}

	private class FloatSigner implements Signer {
		@Override
		public Object sign(final Object o) {
			return -((Float) o);
		}
	}

	private class BigIntSigner implements Signer {
		@Override
		public Object sign(final Object o) {
			return new BigInteger(String.valueOf(-(((BigInteger) o).longValue())));
		}
	}

	private class BigDecSigner implements Signer {
		@Override
		public Object sign(final Object o) {
			return new BigDecimal(-((BigDecimal) o).doubleValue());
		}
	}

	@Override
	public boolean isIdentifier() {
		return false;
	}
}
