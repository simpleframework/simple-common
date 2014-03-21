package net.simpleframework.lib.net.minidev.asm.ex;

public class NoSuchFiledException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public NoSuchFiledException() {
		super();
	}

	public NoSuchFiledException(final String message) {
		super(message);
	}
}
