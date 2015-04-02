package net.simpleframework.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class PropertiesEx extends Properties {
	private static final long serialVersionUID = 5011694856722313621L;

	private static final String keyValueSeparators = "=: \t\r\n\f";

	private static final String strictKeyValueSeparators = "=:";

	private static final String specialSaveChars = "=: \t\r\n\f#!";

	private static final String whiteSpaceChars = " \t\r\n\f";

	private final PropertiesContext context = new PropertiesContext();

	public PropertiesContext getContext() {
		return context;
	}

	@Override
	public synchronized void load(final InputStream inStream) throws IOException {

		BufferedReader in;

		in = new BufferedReader(new InputStreamReader(inStream, "8859_1"));
		while (true) {
			// Get next line
			String line = in.readLine();
			// intract property/comment string
			String intactLine = line;
			if (line == null) {
				return;
			}

			if (line.length() > 0) {

				// Find start of key
				int len = line.length();
				int keyStart;
				for (keyStart = 0; keyStart < len; keyStart++) {
					if (whiteSpaceChars.indexOf(line.charAt(keyStart)) == -1) {
						break;
					}
				}

				// Blank lines are ignored
				if (keyStart == len) {
					continue;
				}

				// Continue lines that end in slashes if they are not comments
				final char firstChar = line.charAt(keyStart);

				if ((firstChar != '#') && (firstChar != '!')) {
					while (continueLine(line)) {
						String nextLine = in.readLine();
						intactLine = intactLine + "\n" + nextLine;
						if (nextLine == null) {
							nextLine = "";
						}
						final String loppedLine = line.substring(0, len - 1);
						// Advance beyond whitespace on new line
						int startIndex;
						for (startIndex = 0; startIndex < nextLine.length(); startIndex++) {
							if (whiteSpaceChars.indexOf(nextLine.charAt(startIndex)) == -1) {
								break;
							}
						}
						nextLine = nextLine.substring(startIndex, nextLine.length());
						line = new String(loppedLine + nextLine);
						len = line.length();
					}

					// Find separation between key and value
					int separatorIndex;
					for (separatorIndex = keyStart; separatorIndex < len; separatorIndex++) {
						final char currentChar = line.charAt(separatorIndex);
						if (currentChar == '\\') {
							separatorIndex++;
						} else if (keyValueSeparators.indexOf(currentChar) != -1) {
							break;
						}
					}

					// Skip over whitespace after key if any
					int valueIndex;
					for (valueIndex = separatorIndex; valueIndex < len; valueIndex++) {
						if (whiteSpaceChars.indexOf(line.charAt(valueIndex)) == -1) {
							break;
						}
					}

					// Skip over one non whitespace key value separators if any
					if (valueIndex < len) {
						if (strictKeyValueSeparators.indexOf(line.charAt(valueIndex)) != -1) {
							valueIndex++;
						}
					}

					// Skip over white space after other separators if any
					while (valueIndex < len) {
						if (whiteSpaceChars.indexOf(line.charAt(valueIndex)) == -1) {
							break;
						}
						valueIndex++;
					}
					String key = line.substring(keyStart, separatorIndex);
					String value = (separatorIndex < len) ? line.substring(valueIndex, len) : "";

					// Convert then store key and value
					key = loadConvert(key);
					value = loadConvert(value);
					// memorize the property also with the whold string
					put(key, value, intactLine);
				} else {
					// memorize the comment string
					context.addCommentLine(intactLine);
				}
			} else {
				// memorize the string even the string is empty
				context.addCommentLine(intactLine);
			}
		}
	}

	/*
	 * Converts encoded &#92;uxxxx to unicode chars and changes special saved
	 * chars to their original forms
	 */
	private String loadConvert(final String theString) {
		char aChar;
		final int len = theString.length();
		final StringBuffer outBuffer = new StringBuffer(len);

		for (int x = 0; x < len;) {
			aChar = theString.charAt(x++);
			if (aChar == '\\') {
				aChar = theString.charAt(x++);
				if (aChar == 'u') {
					// Read the xxxx
					int value = 0;
					for (int i = 0; i < 4; i++) {
						aChar = theString.charAt(x++);
						switch (aChar) {
						case '0':
						case '1':
						case '2':
						case '3':
						case '4':
						case '5':
						case '6':
						case '7':
						case '8':
						case '9':
							value = (value << 4) + aChar - '0';
							break;
						case 'a':
						case 'b':
						case 'c':
						case 'd':
						case 'e':
						case 'f':
							value = (value << 4) + 10 + aChar - 'a';
							break;
						case 'A':
						case 'B':
						case 'C':
						case 'D':
						case 'E':
						case 'F':
							value = (value << 4) + 10 + aChar - 'A';
							break;
						default:
							throw new IllegalArgumentException("Malformed \\uxxxx encoding.");
						}
					}
					outBuffer.append((char) value);
				} else {
					if (aChar == 't') {
						outBuffer.append('\t'); /* ibm@7211 */
					} else if (aChar == 'r') {
						outBuffer.append('\r'); /* ibm@7211 */
					} else if (aChar == 'n') {
						/*
						 * ibm@8897 do not convert a \n to a line.separator
						 * because on some platforms line.separator is a String
						 * of "\r\n". When a Properties class is saved as a file
						 * (store()) and then restored (load()) the restored
						 * input MUST be the same as the output (so that
						 * Properties.equals() works).
						 */
						outBuffer.append('\n'); /* ibm@8897 ibm@7211 */
					} else if (aChar == 'f') {
						outBuffer.append('\f'); /* ibm@7211 */
					} else {
						/* ibm@7211 */
						outBuffer.append(aChar); /* ibm@7211 */
					}
				}
			} else {
				outBuffer.append(aChar);
			}
		}
		return outBuffer.toString();
	}

	@Override
	public synchronized void store(final OutputStream out, final String header) throws IOException {
		BufferedWriter awriter;
		awriter = new BufferedWriter(new OutputStreamWriter(out, "8859_1"));
		if (header != null) {
			writeln(awriter, "#" + header);
		}
		final List entrys = context.getCommentOrEntrys();
		for (final Iterator iter = entrys.iterator(); iter.hasNext();) {
			final Object obj = iter.next();
			if (obj.toString() != null) {
				writeln(awriter, obj.toString());
			}
		}
		awriter.flush();
	}

	private static void writeln(final BufferedWriter bw, final String s) throws IOException {
		bw.write(s);
		bw.newLine();
	}

	private boolean continueLine(final String line) {
		int slashCount = 0;
		int index = line.length() - 1;
		while ((index >= 0) && (line.charAt(index--) == '\\')) {
			slashCount++;
		}
		return (slashCount % 2 == 1);
	}

	/*
	 * Converts unicodes to encoded &#92;uxxxx and writes out any of the
	 * characters in specialSaveChars with a preceding slash
	 */
	private String saveConvert(final String theString, final boolean escapeSpace) {
		final int len = theString.length();
		final StringBuffer outBuffer = new StringBuffer(len * 2);

		for (int x = 0; x < len; x++) {
			final char aChar = theString.charAt(x);
			switch (aChar) {
			case ' ':
				if (x == 0 || escapeSpace) {
					outBuffer.append('\\');
				}

				outBuffer.append(' ');
				break;
			case '\\':
				outBuffer.append('\\');
				outBuffer.append('\\');
				break;
			case '\t':
				outBuffer.append('\\');
				outBuffer.append('t');
				break;
			case '\n':
				outBuffer.append('\\');
				outBuffer.append('n');
				break;
			case '\r':
				outBuffer.append('\\');
				outBuffer.append('r');
				break;
			case '\f':
				outBuffer.append('\\');
				outBuffer.append('f');
				break;
			default:
				if ((aChar < 0x0020) || (aChar > 0x007e)) {
					outBuffer.append('\\');
					outBuffer.append('u');
					outBuffer.append(toHex((aChar >> 12) & 0xF));
					outBuffer.append(toHex((aChar >> 8) & 0xF));
					outBuffer.append(toHex((aChar >> 4) & 0xF));
					outBuffer.append(toHex(aChar & 0xF));
				} else {
					if (specialSaveChars.indexOf(aChar) != -1) {
						outBuffer.append('\\');
					}
					outBuffer.append(aChar);
				}
			}
		}
		return outBuffer.toString();
	}

	/**
	 * Convert a nibble to a hex character
	 * 
	 * @param nibble
	 *        the nibble to convert.
	 */
	private static char toHex(final int nibble) {
		return hexDigit[(nibble & 0xF)];
	}

	/** A table of hex digits */
	private static final char[] hexDigit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A',
			'B', 'C', 'D', 'E', 'F' };

	@Override
	public synchronized Object put(final Object key, final Object value) {
		context.putOrUpdate(key.toString(), value.toString());
		return super.put(key, value);
	}

	public synchronized Object put(final Object key, final Object value, final String line) {
		context.putOrUpdate(key.toString(), value.toString(), line);
		return super.put(key, value);
	}

	@Override
	public synchronized Object remove(final Object key) {
		context.remove(key.toString());
		return super.remove(key);
	}

	class PropertiesContext {
		private final List commentOrEntrys = new ArrayList();

		public List getCommentOrEntrys() {
			return commentOrEntrys;
		}

		public void addCommentLine(final String line) {
			commentOrEntrys.add(line);
		}

		public void putOrUpdate(final PropertyEntry pe) {
			remove(pe.getKey());
			commentOrEntrys.add(pe);
		}

		public void putOrUpdate(final String key, final String value, final String line) {
			final PropertyEntry pe = new PropertyEntry(key, value, line);
			remove(key);
			commentOrEntrys.add(pe);
		}

		public void putOrUpdate(final String key, final String value) {
			final PropertyEntry pe = new PropertyEntry(key, value);
			final int index = remove(key);
			commentOrEntrys.add(index, pe);
		}

		public int remove(final String key) {
			for (int index = 0; index < commentOrEntrys.size(); index++) {
				final Object obj = commentOrEntrys.get(index);
				if (obj instanceof PropertyEntry) {
					if (obj != null) {
						if (key.equals(((PropertyEntry) obj).getKey())) {
							commentOrEntrys.remove(obj);
							return index;
						}
					}
				}
			}
			return commentOrEntrys.size();
		}

		class PropertyEntry {
			private String key;

			private String value;

			private String line;

			public String getLine() {
				return line;
			}

			public void setLine(final String line) {
				this.line = line;
			}

			public PropertyEntry(final String key, final String value) {
				this.key = key;
				this.value = value;
			}

			/**
			 * @param key
			 * @param value
			 * @param line
			 */
			public PropertyEntry(final String key, final String value, final String line) {
				this(key, value);
				this.line = line;
			}

			public String getKey() {
				return key;
			}

			public void setKey(final String key) {
				this.key = key;
			}

			public String getValue() {
				return value;
			}

			public void setValue(final String value) {
				this.value = value;
			}

			@Override
			public String toString() {
				if (line != null) {
					return line;
				}
				if (key != null && value != null) {
					final String k = saveConvert(key, true);
					final String v = saveConvert(value, false);
					return k + "=" + v;
				}
				return null;
			}
		}
	}

	/**
	 * @param string
	 */
	public void addComment(final String comment) {
		if (comment != null) {
			context.addCommentLine("#" + comment);
		}
	}
}
