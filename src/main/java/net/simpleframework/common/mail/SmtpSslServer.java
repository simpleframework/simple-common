package net.simpleframework.common.mail;

import java.util.Properties;

import javax.mail.Authenticator;

public class SmtpSslServer extends SmtpServer {

	protected static final String MAIL_SMTP_STARTTLS_ENABLE = "mail.smtp.starttls.enable";
	protected static final String MAIL_SMTP_SOCKET_FACTORY_PORT = "mail.smtp.socketFactory.port";
	protected static final String MAIL_SMTP_SOCKET_FACTORY_CLASS = "mail.smtp.socketFactory.class";
	protected static final String MAIL_SMTP_SOCKET_FACTORY_FALLBACK = "mail.smtp.socketFactory.fallback";
	protected static final int DEFAULT_SSL_PORT = 465;

	public SmtpSslServer(final String host, final Authenticator authenticator) {
		super(host, DEFAULT_SSL_PORT, authenticator);
	}

	public SmtpSslServer(final String host, final String username, final String password) {
		super(host, DEFAULT_SSL_PORT, username, password);
	}

	public SmtpSslServer(final String host, final int port, final Authenticator authenticator) {
		super(host, port, authenticator);
	}

	public SmtpSslServer(final String host, final int port, final String username,
			final String password) {
		super(host, port, username, password);
	}

	@Override
	protected Properties createSessionProperties() {
		final Properties props = super.createSessionProperties();
		props.setProperty(MAIL_SMTP_STARTTLS_ENABLE, "true");
		props.setProperty(MAIL_SMTP_SOCKET_FACTORY_PORT, String.valueOf(port));
		props.setProperty(MAIL_SMTP_PORT, String.valueOf(port));
		props.setProperty(MAIL_SMTP_SOCKET_FACTORY_CLASS, "javax.net.ssl.SSLSocketFactory");
		props.setProperty(MAIL_SMTP_SOCKET_FACTORY_FALLBACK, "false");
		props.setProperty(MAIL_HOST, host);
		return props;
	}
}