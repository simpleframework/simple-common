package net.simpleframework.lib.org.mvel2.ast;

public class ReduceableCodeException extends RuntimeException {
	private final Object literal;

	public Object getLiteral() {
		return literal;
	}

	public ReduceableCodeException(final Object literal) {
		this.literal = literal;
	}
}
