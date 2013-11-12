package net.simpleframework.common.mail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

import net.simpleframework.common.FileUtils;
import net.simpleframework.common.IoUtils;

public abstract class EmailAttachment {

	protected final String name;

	protected final String contentId;

	protected EmailAttachment(final String name, final String contentId) {
		this.name = name;
		this.contentId = contentId;
	}

	public String getName() {
		return name;
	}

	public String getContentId() {
		return contentId;
	}

	public boolean isInline() {
		return contentId != null;
	}

	public abstract DataSource getDataSource();

	// ---------------------------------------------------------------- size

	protected int size = -1;

	public int getSize() {
		return size;
	}

	protected void setSize(final int size) {
		this.size = size;
	}

	// ---------------------------------------------------------------- content
	// methods

	public byte[] toByteArray() {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		writeToStream(out);
		return out.toByteArray();
	}

	public void writeToFile(final File destination) {
		InputStream in = null;
		try {
			in = getDataSource().getInputStream();
			FileUtils.copyFile(in, destination);
			// FileUtil.writeStream(destination, in);
		} catch (final IOException ioex) {
			throw new MailException(ioex);
		} finally {
			try {
				in.close();
			} catch (final IOException e) {
			}
		}
	}

	public void writeToStream(final OutputStream out) {
		InputStream in = null;
		try {
			in = getDataSource().getInputStream();
			IoUtils.copyStream(in, out);
		} catch (final IOException ioex) {
			throw new MailException(ioex);
		} finally {
			try {
				in.close();
			} catch (final IOException e) {
			}
		}
	}
}
