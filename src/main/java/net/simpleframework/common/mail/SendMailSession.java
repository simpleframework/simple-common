package net.simpleframework.common.mail;

import java.util.Date;
import java.util.LinkedList;
import java.util.Map;

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class SendMailSession {

	private static final String ALTERNATIVE = "alternative";
	private static final String CHARSET = ";charset=";
	private static final String INLINE = "inline";

	protected final Session mailSession;
	protected final Transport mailTransport;

	public SendMailSession(final Session session, final Transport transport) {
		this.mailSession = session;
		this.mailTransport = transport;
	}

	public void open() {
		try {
			mailTransport.connect();
		} catch (final MessagingException msex) {
			throw new MailException("Unable to connect", msex);
		}
	}

	public void sendMail(final Email mail) {
		Message msg;
		try {
			msg = createMessage(mail, mailSession);
		} catch (final MessagingException mex) {
			throw new MailException("Unable to prepare email message: " + mail, mex);
		}
		try {
			mailTransport.sendMessage(msg, msg.getAllRecipients());
		} catch (final MessagingException mex) {
			throw new MailException("Unable to send email message: " + mail, mex);
		}
	}

	public void close() {
		try {
			mailTransport.close();
		} catch (final MessagingException mex) {
			throw new MailException("Unable to close session", mex);
		}
	}

	// ---------------------------------------------------------------- adapter

	protected Message createMessage(final Email email, final Session session)
			throws MessagingException {
		final Message msg = new MimeMessage(session);
		msg.setFrom(new InternetAddress(email.getFrom()));

		// to
		final int totalTo = email.getTo().length;
		InternetAddress[] address = new InternetAddress[totalTo];
		for (int i = 0; i < totalTo; i++) {
			address[i] = new InternetAddress(email.getTo()[i]);
		}
		msg.setRecipients(Message.RecipientType.TO, address);

		// replyTo
		if (email.getReplyTo() != null) {
			final int totalReplyTo = email.getReplyTo().length;
			address = new InternetAddress[totalReplyTo];
			for (int i = 0; i < totalReplyTo; i++) {
				address[i] = new InternetAddress(email.getReplyTo()[i]);
			}
			msg.setReplyTo(address);
		}

		// cc
		if (email.getCc() != null) {
			final int totalCc = email.getCc().length;
			address = new InternetAddress[totalCc];
			for (int i = 0; i < totalCc; i++) {
				address[i] = new InternetAddress(email.getCc()[i]);
			}
			msg.setRecipients(Message.RecipientType.CC, address);
		}

		// bcc
		if (email.getBcc() != null) {
			final int totalBcc = email.getBcc().length;
			address = new InternetAddress[totalBcc];
			for (int i = 0; i < totalBcc; i++) {
				address[i] = new InternetAddress(email.getBcc()[i]);
			}
			msg.setRecipients(Message.RecipientType.BCC, address);
		}

		// subject & date
		msg.setSubject(email.getSubject());
		Date date = email.getSentDate();
		if (date == null) {
			date = new Date();
		}
		msg.setSentDate(date);

		// headers
		final Map<String, String> headers = email.getAllHeaders();
		if (headers != null) {
			for (final Map.Entry<String, String> stringStringEntry : headers.entrySet()) {
				final String value = stringStringEntry.getValue();
				msg.setHeader(stringStringEntry.getKey(), value);
			}
		}

		// message data and attachments
		final LinkedList<EmailMessage> messages = email.getAllMessages();
		final LinkedList<EmailAttachment> attachments = email.getAttachments();
		final int totalMessages = messages.size();

		if ((attachments == null) && (totalMessages == 1)) {
			final EmailMessage emailMessage = messages.get(0);
			msg.setContent(emailMessage.getContent(), emailMessage.getMimeType() + CHARSET
					+ emailMessage.getEncoding());
		} else {
			final Multipart multipart = new MimeMultipart();
			Multipart msgMultipart = multipart;
			if (totalMessages > 1) {
				final MimeBodyPart body = new MimeBodyPart();
				msgMultipart = new MimeMultipart(ALTERNATIVE);
				body.setContent(msgMultipart);
				multipart.addBodyPart(body);
			}
			for (final EmailMessage emailMessage : messages) {
				final MimeBodyPart messageData = new MimeBodyPart();
				messageData.setContent(emailMessage.getContent(), emailMessage.getMimeType() + CHARSET
						+ emailMessage.getEncoding());
				msgMultipart.addBodyPart(messageData);
			}
			if (attachments != null) {
				for (final EmailAttachment att : attachments) {
					final MimeBodyPart attBodyPart = new MimeBodyPart();
					attBodyPart.setFileName(att.getName());
					attBodyPart.setDataHandler(new DataHandler(att.getDataSource()));
					if (att.isInline()) {
						attBodyPart.setContentID(att.getContentId());
						attBodyPart.setDisposition(INLINE);
					}
					multipart.addBodyPart(attBodyPart);
				}
			}
			msg.setContent(multipart);
		}
		return msg;
	}
}
