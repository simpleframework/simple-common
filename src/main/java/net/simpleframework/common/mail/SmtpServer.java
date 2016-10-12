package net.simpleframework.common.mail;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;

public class SmtpServer implements SendMailSessionProvider {

	protected static final String MAIL_HOST = "mail.host";
	protected static final String MAIL_SMTP_HOST = "mail.smtp.host";
	protected static final String MAIL_SMTP_PORT = "mail.smtp.port";
	protected static final String MAIL_SMTP_AUTH = "mail.smtp.auth";
	protected static final String MAIL_TRANSPORT_PROTOCOL = "mail.transport.protocol";

	protected static final String PROTOCOL_SMTP = "smtp";

	protected static final int DEFAULT_SMTP_PORT = 25;

	protected final String host;
	protected final int port;
	protected final Authenticator authenticator;
	protected final Properties sessionProperties;

	protected String from;

	public SmtpServer(final String host) {
		this(host, DEFAULT_SMTP_PORT, null);
	}

	public SmtpServer(final String host, final int port) {
		this(host, port, null);
	}

	public SmtpServer(final String host, final Authenticator authenticator) {
		this(host, DEFAULT_SMTP_PORT, authenticator);
	}

	public SmtpServer(final String host, final int port, final String username,
			final String password) {
		this(host, port, new SimpleAuthenticator(username, password));
	}

	public SmtpServer(final String host, final String username, final String password) {
		this(host, DEFAULT_SMTP_PORT, new SimpleAuthenticator(username, password));
	}

	public SmtpServer(final String host, final int port, final Authenticator authenticator) {
		this.host = host;
		this.port = port;
		this.authenticator = authenticator;
		sessionProperties = createSessionProperties();
	}

	protected Properties createSessionProperties() {
		final Properties props = new Properties();
		props.setProperty(MAIL_TRANSPORT_PROTOCOL, PROTOCOL_SMTP);
		props.setProperty(MAIL_HOST, host);
		props.setProperty(MAIL_SMTP_HOST, host);
		props.setProperty(MAIL_SMTP_PORT, String.valueOf(port));
		if (authenticator != null) {
			props.setProperty(MAIL_SMTP_AUTH, "true");
		}
		return props;
	}

	@Override
	public SendMailSession createSession() {
		final Session mailSession = Session.getInstance(sessionProperties, authenticator);
		Transport mailTransport;
		try {
			mailTransport = getTransport(mailSession);
		} catch (final NoSuchProviderException nspex) {
			throw new MailException(nspex);
		}
		return new SendMailSession(mailSession, mailTransport);
	}

	protected Transport getTransport(final Session session) throws NoSuchProviderException {
		return session.getTransport(PROTOCOL_SMTP);
	}

	// ---------------------------------------------------------------- getters

	public String getHost() {
		return host;
	}

	public boolean isAuth() {
		return getAuthenticator() != null;
	}

	public Authenticator getAuthenticator() {
		return authenticator;
	}

	public int getPort() {
		return port;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(final String from) {
		this.from = from;
	}
}