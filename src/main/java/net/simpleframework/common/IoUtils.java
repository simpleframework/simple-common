package net.simpleframework.common;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.simpleframework.common.logger.Log;
import net.simpleframework.common.logger.LogFactory;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class IoUtils {
	static Log log = LogFactory.getLogger(IoUtils.class);

	static final int BUFFER = 8 * 1024;

	public static String getStringFromInputStream(final InputStream inputStream) throws IOException {
		return getStringFromInputStream(inputStream, "UTF-8");
	}

	public static String getStringFromInputStream(final InputStream inputStream,
			final String charsetName) throws IOException {
		return getStringFromReader(new InputStreamReader(inputStream, charsetName));
	}

	public static String getStringFromReader(final Reader reader) throws IOException {
		BufferedReader br = null;
		try {
			br = new BufferedReader(reader);
			final StringWriter sw = new StringWriter();
			final PrintWriter writer = new PrintWriter(sw);
			String s;
			while ((s = br.readLine()) != null) {
				writer.println(s);
			}
			writer.flush();
			return sw.toString();
		} finally {
			if (br != null) {
				br.close();
			}
		}
	}

	public static String[] getStringsFromReader(final Reader reader) throws IOException {
		BufferedReader br = null;
		try {
			br = new BufferedReader(reader);
			final List<String> l = new ArrayList<String>();
			String s;
			while ((s = br.readLine()) != null) {
				l.add(s);
			}
			return l.toArray(new String[l.size()]);
		} finally {
			if (br != null) {
				br.close();
			}
		}
	}

	public static int copyStream(final InputStream inputStream, final OutputStream outputStream)
			throws IOException {
		if (inputStream == null || outputStream == null) {
			return 0;
		}
		int result = 0;
		final byte[] buf = new byte[BUFFER];
		for (;;) {
			final int numRead = inputStream.read(buf);
			if (numRead == -1) {
				break;
			}
			outputStream.write(buf, 0, numRead);
			result += numRead;
		}
		outputStream.flush();
		return result;
	}

	/********************************* Serializable **********************************/

	static Object kryo;
	static boolean hessianEnabled = false;
	static {
		try {
			kryo = Class.forName("com.esotericsoftware.kryo.Kryo").newInstance();
			BeanUtils.setProperty(kryo, "references", false);
			log.info("Kryo serialize enabled!");
		} catch (final Throwable ex) {
		}
		if (kryo == null) {
			try {
				Class.forName("com.caucho.hessian.io.HessianInput");
				hessianEnabled = true;
				log.info("Hessian serialize enabled!");
			} catch (final Throwable ex) {
			}
		}
	}

	public static byte[] serialize(final Object obj) throws IOException {
		if (obj == null) {
			return null;
		}

		if (kryo != null) {
			return IoUtils2.kryo_serialize(kryo, obj);
		} else {
			if (hessianEnabled) {
				return IoUtils2.hessian_serialize(obj);
			} else {
				final ByteArrayOutputStream bos = new ByteArrayOutputStream();
				final ObjectOutputStream oos = new ObjectOutputStream(bos);
				oos.writeObject(obj);
				return bos.toByteArray();
			}
		}
	}

	public static Object deserialize(final byte[] bytes) throws IOException, ClassNotFoundException {
		return deserialize(bytes, null);
	}

	public static Object deserialize(final byte[] bytes, final Class<?> typeClass)
			throws IOException, ClassNotFoundException {
		if (bytes == null || bytes.length == 0) {
			return null;
		}
		if (kryo != null) {
			return IoUtils2.kryo_deserialize(kryo, bytes, typeClass);
		} else {
			if (hessianEnabled) {
				return IoUtils2.hessian_deserialize(bytes);
			} else {
				final ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
				final ObjectInputStream ois = new ObjectInputStream(bis);
				return ois.readObject();
			}
		}
	}

	/********************************* MacAddress **********************************/

	private static Method getHardwareAddressMethod;
	static {
		try {
			getHardwareAddressMethod = NetworkInterface.class.getMethod("getHardwareAddress");
		} catch (final NoSuchMethodException e) {
		}
	}

	public static byte[] getMacAddressBytes() throws IOException {
		final NetworkInterface ni = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
		if (getHardwareAddressMethod != null) {
			return (byte[]) ClassUtils.invoke(getHardwareAddressMethod, ni);
		}
		return MacAddress.getMacAddress().getBytes();
	}

	static private Pattern macPattern = Pattern.compile(".*((:?[0-9a-f]{2}[-:]){5}[0-9a-f]{2}).*",
			Pattern.CASE_INSENSITIVE);

	static final String[] windowsCommand = { "ipconfig", "/all" };

	static final String[] linuxCommand = { "/sbin/ifconfig", "-a" };

	static class MacAddress {

		public final static List<String> getMacAddresses() throws IOException {
			final List<String> macAddressList = new ArrayList<String>();

			final BufferedReader reader = getMacAddressesReader();
			for (String line = null; (line = reader.readLine()) != null;) {
				final Matcher matcher = macPattern.matcher(line);
				if (matcher.matches()) {
					macAddressList.add(matcher.group(1).replaceAll("[-:]", ""));
				}
			}
			reader.close();
			return macAddressList;
		}

		public final static String getMacAddress() throws IOException {
			return getMacAddress(0);
		}

		public final static String getMacAddress(final int nicIndex) throws IOException {
			final BufferedReader reader = getMacAddressesReader();
			int nicCount = 0;
			for (String line = null; (line = reader.readLine()) != null;) {
				final Matcher matcher = macPattern.matcher(line);
				if (matcher.matches()) {
					if (nicCount == nicIndex) {
						reader.close();
						return matcher.group(1).replaceAll("[-:]", "");
					}
					nicCount++;
				}
			}
			reader.close();
			return null;
		}

		private static BufferedReader getMacAddressesReader() throws IOException {
			final String[] command;
			final String os = System.getProperty("os.name");

			if (os.startsWith("Windows")) {
				command = windowsCommand;
			} else if (os.startsWith("Linux")) {
				command = linuxCommand;
			} else {
				throw new IOException("Unknown operating system: " + os);
			}
			final Process process = Runtime.getRuntime().exec(command);
			new Thread() {

				@Override
				public void run() {
					try {
						final InputStream errorStream = process.getErrorStream();
						while (errorStream.read() != -1) {
							;
						}
						errorStream.close();
					} catch (final IOException e) {
						e.printStackTrace();
					}
				}

			}.start();

			return new BufferedReader(new InputStreamReader(process.getInputStream()));
		}
	}
}
