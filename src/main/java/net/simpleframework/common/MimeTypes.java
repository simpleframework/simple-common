package net.simpleframework.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

public class MimeTypes {

	public static final String MIME_APPLICATION_ATOM_XML = "application/atom+xml";
	public static final String MIME_APPLICATION_JSON = "application/json";
	public static final String MIME_APPLICATION_OCTET_STREAM = "application/octet-stream";
	public static final String MIME_TEXT_PLAIN = "text/plain";
	public static final String MIME_TEXT_HTML = "text/html";

	private static final HashMap<String, String> MIME_TYPE_MAP;

	static {
		final Properties mimes = new Properties();

		final InputStream is = MimeTypes.class.getResourceAsStream(MimeTypes.class.getSimpleName()
				+ ".properties");
		if (is == null) {
			throw new IllegalStateException("Mime types file missing");
		}

		try {
			mimes.load(is);
		} catch (final IOException ioex) {
			throw new IllegalStateException(ioex.getMessage());
		} finally {
			try {
				is.close();
			} catch (final IOException ioex) {
			}
		}

		MIME_TYPE_MAP = new HashMap<String, String>(mimes.size() * 2);

		final Enumeration<?> keys = mimes.propertyNames();
		while (keys.hasMoreElements()) {
			String mimeType = (String) keys.nextElement();
			final String extensions = mimes.getProperty(mimeType);

			if (mimeType.startsWith("/")) {
				mimeType = "application" + mimeType;
			} else if (mimeType.startsWith("a/")) {
				mimeType = "audio" + mimeType.substring(1);
			} else if (mimeType.startsWith("i/")) {
				mimeType = "image" + mimeType.substring(1);
			} else if (mimeType.startsWith("t/")) {
				mimeType = "text" + mimeType.substring(1);
			} else if (mimeType.startsWith("v/")) {
				mimeType = "video" + mimeType.substring(1);
			}

			final String[] allExtensions = StringUtils.split(extensions, " ");

			for (final String extension : allExtensions) {
				if (MIME_TYPE_MAP.put(extension, mimeType) != null) {
					throw new IllegalArgumentException("Duplicated extension: " + extension);
				}
			}
		}
	}

	public static void registerMimeType(final String ext, final String mimeType) {
		MIME_TYPE_MAP.put(ext, mimeType);
	}

	public static String getMimeType(final String ext) {
		String mimeType = lookupMimeType(ext);
		if (mimeType == null) {
			mimeType = MIME_APPLICATION_OCTET_STREAM;
		}
		return mimeType;
	}

	public static String lookupMimeType(final String ext) {
		return MIME_TYPE_MAP.get(ext.toLowerCase());
	}
}