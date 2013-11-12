package net.simpleframework.common.mail;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class SimpleAuthenticator extends Authenticator {

	protected final String username;

	protected final String password;

	public SimpleAuthenticator(final String username, final String password) {
		super();
		this.username = username;
		this.password = password;
	}

	@Override
	public PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(username, password);
	}
}
