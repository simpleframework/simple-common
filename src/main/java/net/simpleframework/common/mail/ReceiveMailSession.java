package net.simpleframework.common.mail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Enumeration;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimePart;

import net.simpleframework.common.IoUtils;

public class ReceiveMailSession {

	protected static final String DEFAULT_FOLDER = "INBOX";
	protected static final String STR_CHARSET = "charset=";

	protected final Session session;
	protected final Store store;

	public ReceiveMailSession(final Session session, final Store store) {
		this.session = session;
		this.store = store;
	}

	protected Folder folder;

	public void open() {
		try {
			store.connect();
		} catch (final MessagingException msex) {
			throw new MailException("Unable to open session", msex);
		}
	}

	// ---------------------------------------------------------------- folders

	public void useFolder(final String folderName) {
		closeFolderIfOpened();
		try {
			folder = store.getFolder(folderName);
		} catch (final MessagingException msex) {
			throw new MailException("Unable to connect to folder: " + folderName, msex);
		}
		try {
			folder.open(Folder.READ_WRITE);
		} catch (final MessagingException mex) {
			try {
				folder.open(Folder.READ_ONLY);
			} catch (final MessagingException msex) {
				throw new MailException("Unable to open folder: " + folderName, msex);
			}
		}
	}

	public void useDefaultFolder() {
		closeFolderIfOpened();
		useFolder(DEFAULT_FOLDER);
	}

	public int getMessageCount() {
		if (folder == null) {
			useDefaultFolder();
		}
		try {
			return folder.getMessageCount();
		} catch (final MessagingException mex) {
			throw new MailException("Unable to read number of messages", mex);
		}
	}

	public int getNewMessageCount() {
		if (folder == null) {
			useDefaultFolder();
		}
		try {
			return folder.getNewMessageCount();
		} catch (final MessagingException mex) {
			throw new MailException("Unable to read number of new messages", mex);
		}
	}

	// ---------------------------------------------------------------- receive
	// emails

	public ReceivedEmail[] receiveEmail(final boolean delete) {
		if (folder == null) {
			useDefaultFolder();
		}
		ReceivedEmail[] emails;
		try {
			final Message[] messages = folder.getMessages();
			if (messages.length == 0) {
				return null;
			}
			emails = new ReceivedEmail[messages.length];
			for (int i = 0; i < messages.length; i++) {
				final Message msg = messages[i];
				if (delete) {
					msg.setFlag(Flags.Flag.DELETED, true);
				}
				emails[i] = message2Email(msg);
			}
		} catch (final MessagingException msex) {
			throw new MailException("Unable to read messages", msex);
		} catch (final IOException ioex) {
			throw new MailException("Unable to read message content", ioex);
		}
		return emails;
	}

	@SuppressWarnings({ "unchecked" })
	protected ReceivedEmail message2Email(final Message msg) throws MessagingException, IOException {
		final ReceivedEmail email = new ReceivedEmail();
		// flags
		if (msg.isSet(Flags.Flag.ANSWERED)) {
			email.addFlags(ReceivedEmail.ANSWERED);
		}
		if (msg.isSet(Flags.Flag.DELETED)) {
			email.addFlags(ReceivedEmail.DELETED);
		}
		if (msg.isSet(Flags.Flag.DRAFT)) {
			email.addFlags(ReceivedEmail.DRAFT);
		}
		if (msg.isSet(Flags.Flag.FLAGGED)) {
			email.addFlags(ReceivedEmail.FLAGGED);
		}
		if (msg.isSet(Flags.Flag.RECENT)) {
			email.addFlags(ReceivedEmail.RECENT);
		}
		if (msg.isSet(Flags.Flag.SEEN)) {
			email.addFlags(ReceivedEmail.SEEN);
		}
		if (msg.isSet(Flags.Flag.USER)) {
			email.addFlags(ReceivedEmail.USER);
		}

		// msg no
		email.setMessageNumber(msg.getMessageNumber());

		// standard stuff
		email.setFrom(msg.getFrom()[0].toString());
		email.setTo(address2String(msg.getRecipients(Message.RecipientType.TO)));
		email.setCc(address2String(msg.getRecipients(Message.RecipientType.CC)));
		email.setBcc(address2String(msg.getRecipients(Message.RecipientType.BCC)));
		email.setSubject(msg.getSubject());
		Date recvDate = msg.getReceivedDate();
		if (recvDate == null) {
			recvDate = new Date();
		}
		email.setReceiveDate(recvDate);
		email.setSentDate(msg.getSentDate());

		// copy headers
		final Enumeration<Header> headers = msg.getAllHeaders();
		while (headers.hasMoreElements()) {
			final Header header = headers.nextElement();
			email.setHeader(header.getName(), header.getValue());
		}

		// content
		processPart(email, msg);

		return email;
	}

	protected void processPart(final ReceivedEmail email, final Part part) throws IOException,
			MessagingException {
		final Object content = part.getContent();

		if (content instanceof String) {
			final String stringContent = (String) content;

			final String disposition = part.getDisposition();
			if (disposition != null && disposition.equals(Part.ATTACHMENT)) {
				final String mimeType = extractMimeType(part.getContentType());
				final String fileName = part.getFileName();
				final String contentId = (part instanceof MimePart) ? ((MimePart) part).getContentID()
						: null;

				email.addAttachment(fileName, mimeType, contentId, stringContent.getBytes("utf-8"));
			} else {
				final String contentType = part.getContentType();
				email.addMessage(stringContent, extractMimeType(contentType),
						extractEncoding(contentType));
			}
		} else if (content instanceof Multipart) {
			final Multipart mp = (Multipart) content;
			final int count = mp.getCount();
			for (int i = 0; i < count; i++) {
				final Part innerPart = mp.getBodyPart(i);
				processPart(email, innerPart);
			}
		} else if (content instanceof InputStream) {
			final String fileName = part.getFileName();
			final String contentId = (part instanceof MimePart) ? ((MimePart) part).getContentID()
					: null;
			final String mimeType = extractMimeType(part.getContentType());

			final InputStream is = (InputStream) content;
			final ByteArrayOutputStream fbaos = new ByteArrayOutputStream();
			IoUtils.copyStream(is, fbaos);

			email.addAttachment(fileName, mimeType, contentId, fbaos.toByteArray());
		}
	}

	protected String extractMimeType(final String contentType) {
		final int ndx = contentType.indexOf(';');
		String mime;
		if (ndx != -1) {
			mime = contentType.substring(0, ndx);
		} else {
			mime = contentType;
		}
		return mime;
	}

	protected String extractEncoding(final String contentType) {
		int ndx = contentType.indexOf(';');
		final String charset = ndx != -1 ? contentType.substring(ndx + 1) : "";
		String encoding = null;

		ndx = charset.indexOf(STR_CHARSET);
		if (ndx != -1) {
			ndx += STR_CHARSET.length();
			final int len = charset.length();

			if (charset.charAt(ndx) == '"') {
				ndx++;
			}
			final int start = ndx;

			while (ndx < len) {
				final char c = charset.charAt(ndx);
				if ((c == '"') || (Character.isWhitespace(c) == true)) {
					break;
				}
				ndx++;
			}
			encoding = charset.substring(start, ndx);
		}
		return encoding;
	}

	protected String[] address2String(final Address[] addresses) {
		if (addresses == null) {
			return null;
		}
		if (addresses.length == 0) {
			return null;
		}
		final String[] res = new String[addresses.length];
		for (int i = 0; i < addresses.length; i++) {
			final Address address = addresses[i];
			res[i] = address.toString();
		}
		return res;
	}

	// ---------------------------------------------------------------- close

	protected void closeFolderIfOpened() {
		if (folder != null) {
			try {
				folder.close(true);
			} catch (final MessagingException ignore) {
			}
		}
	}

	public void close() {
		closeFolderIfOpened();
		try {
			store.close();
		} catch (final MessagingException mex) {
			throw new MailException("Unable to close session", mex);
		}
	}
}
