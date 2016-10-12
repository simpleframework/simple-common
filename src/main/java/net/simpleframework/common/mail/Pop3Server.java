package net.simpleframework.common.mail;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

public class Pop3Server implements ReceiveMailSessionProvider {

	protected static final String MAIL_POP3_PORT = "mail.pop3.port";
	protected static final String MAIL_POP3_HOST = "mail.pop3.host";
	protected static final String MAIL_POP3_AUTH = "mail.pop3.auth";

	protected static final String PROTOCOL_POP3 = "pop3";

	protected static final int DEFAULT_POP3_PORT = 110;

	protected final String host;
	protected final int port;
	protected final Authenticator authenticator;
	protected final Properties sessionProperties;

	public Pop3Server(final String host) {
		this(host, DEFAULT_POP3_PORT, null);
	}

	public Pop3Server(final String host, final int port) {
		this(host, port, null);
	}

	public Pop3Server(final String host, final Authenticator authenticator) {
		this(host, DEFAULT_POP3_PORT, authenticator);
	}

	public Pop3Server(final String host, final int port, final String username,
			final String password) {
		this(host, port, new SimpleAuthenticator(username, password));
	}

	public Pop3Server(final String host, final int port, final Authenticator authenticator) {
		this.host = host;
		this.port = port;
		this.authenticator = authenticator;
		sessionProperties = createSessionProperties();
	}

	protected Properties createSessionProperties() {
		final Properties props = new Properties();
		props.setProperty(MAIL_POP3_HOST, host);
		props.setProperty(MAIL_POP3_PORT, String.valueOf(port));
		if (authenticator != null) {
			props.setProperty(MAIL_POP3_AUTH, "true");
		}
		return props;
	}

	@Override
	public ReceiveMailSession createSession() {
		final Session session = Session.getInstance(sessionProperties, authenticator);
		Store store;
		try {
			store = getStore(session);
		} catch (final NoSuchProviderException nspex) {
			throw new MailException("Unable to create POP3 session", nspex);
		}
		return new ReceiveMailSession(session, store);
	}

	protected Store getStore(final Session session) throws NoSuchProviderException {
		return session.getStore(PROTOCOL_POP3);
	}

	// ---------------------------------------------------------------- getters

	public String getHost() {
		return host;
	}

	public Authenticator getAuthenticator() {
		return authenticator;
	}

	public int getPort() {
		return port;
	}
}