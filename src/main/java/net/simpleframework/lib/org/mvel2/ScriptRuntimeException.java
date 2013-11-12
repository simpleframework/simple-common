package net.simpleframework.lib.org.mvel2;

/**
 * @author Mike Brock .
 */
public class ScriptRuntimeException extends RuntimeException {
	public ScriptRuntimeException() {
	}

	public ScriptRuntimeException(final String message) {
		super(message);
	}

	public ScriptRuntimeException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public ScriptRuntimeException(final Throwable cause) {
		super(cause);
	}
}
