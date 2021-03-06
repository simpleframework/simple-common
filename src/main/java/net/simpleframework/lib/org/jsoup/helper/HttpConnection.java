package net.simpleframework.lib.org.jsoup.helper;

import static net.simpleframework.lib.org.jsoup.Connection.Method.HEAD;
import static net.simpleframework.lib.org.jsoup.internal.Normalizer.lowerCase;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import net.simpleframework.lib.org.jsoup.Connection;
import net.simpleframework.lib.org.jsoup.HttpStatusException;
import net.simpleframework.lib.org.jsoup.UncheckedIOException;
import net.simpleframework.lib.org.jsoup.UnsupportedMimeTypeException;
import net.simpleframework.lib.org.jsoup.internal.ConstrainableInputStream;
import net.simpleframework.lib.org.jsoup.internal.StringUtil;
import net.simpleframework.lib.org.jsoup.nodes.Document;
import net.simpleframework.lib.org.jsoup.parser.Parser;
import net.simpleframework.lib.org.jsoup.parser.TokenQueue;

/**
 * Implementation of {@link Connection}.
 * 
 * @see net.simpleframework.lib.org.jsoup.Jsoup#connect(String)
 */
public class HttpConnection implements Connection {
	public static final String CONTENT_ENCODING = "Content-Encoding";
	/**
	 * Many users would get caught by not setting a user-agent and therefore
	 * getting different responses on their desktop
	 * vs in jsoup, which would otherwise default to {@code Java}. So by default,
	 * use a desktop UA.
	 */
	public static final String DEFAULT_UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36";
	private static final String USER_AGENT = "User-Agent";
	public static final String CONTENT_TYPE = "Content-Type";
	public static final String MULTIPART_FORM_DATA = "multipart/form-data";
	public static final String FORM_URL_ENCODED = "application/x-www-form-urlencoded";
	private static final int HTTP_TEMP_REDIR = 307; // http/1.1 temporary
																	// redirect, not in Java's
																	// set.
	private static final String DefaultUploadType = "application/octet-stream";

	public static Connection connect(final String url) {
		final Connection con = new HttpConnection();
		con.url(url);
		return con;
	}

	public static Connection connect(final URL url) {
		final Connection con = new HttpConnection();
		con.url(url);
		return con;
	}

	public HttpConnection() {
		req = new Request();
		res = new Response();
	}

	/**
	 * Encodes the input URL into a safe ASCII URL string
	 * 
	 * @param url
	 *        unescaped URL
	 * @return escaped URL
	 */
	private static String encodeUrl(final String url) {
		try {
			final URL u = new URL(url);
			return encodeUrl(u).toExternalForm();
		} catch (final Exception e) {
			return url;
		}
	}

	static URL encodeUrl(final URL u) {
		try {
			// odd way to encode urls, but it works!
			String urlS = u.toExternalForm(); // URL external form may have spaces
															// which is illegal in new URL()
															// (odd asymmetry)
			urlS = urlS.replace(" ", "%20");
			final URI uri = new URI(urlS);
			return new URL(uri.toASCIIString());
		} catch (URISyntaxException | MalformedURLException e) {
			// give up and return the original input
			return u;
		}
	}

	private static String encodeMimeName(final String val) {
		if (val == null) {
			return null;
		}
		return val.replace("\"", "%22");
	}

	private Connection.Request req;
	private Connection.Response res;

	@Override
	public Connection url(final URL url) {
		req.url(url);
		return this;
	}

	@Override
	public Connection url(final String url) {
		Validate.notEmpty(url, "Must supply a valid URL");
		try {
			req.url(new URL(encodeUrl(url)));
		} catch (final MalformedURLException e) {
			throw new IllegalArgumentException("Malformed URL: " + url, e);
		}
		return this;
	}

	@Override
	public Connection proxy(final Proxy proxy) {
		req.proxy(proxy);
		return this;
	}

	@Override
	public Connection proxy(final String host, final int port) {
		req.proxy(host, port);
		return this;
	}

	@Override
	public Connection userAgent(final String userAgent) {
		Validate.notNull(userAgent, "User agent must not be null");
		req.header(USER_AGENT, userAgent);
		return this;
	}

	@Override
	public Connection timeout(final int millis) {
		req.timeout(millis);
		return this;
	}

	@Override
	public Connection maxBodySize(final int bytes) {
		req.maxBodySize(bytes);
		return this;
	}

	@Override
	public Connection followRedirects(final boolean followRedirects) {
		req.followRedirects(followRedirects);
		return this;
	}

	@Override
	public Connection referrer(final String referrer) {
		Validate.notNull(referrer, "Referrer must not be null");
		req.header("Referer", referrer);
		return this;
	}

	@Override
	public Connection method(final Method method) {
		req.method(method);
		return this;
	}

	@Override
	public Connection ignoreHttpErrors(final boolean ignoreHttpErrors) {
		req.ignoreHttpErrors(ignoreHttpErrors);
		return this;
	}

	@Override
	public Connection ignoreContentType(final boolean ignoreContentType) {
		req.ignoreContentType(ignoreContentType);
		return this;
	}

	@Override
	public Connection data(final String key, final String value) {
		req.data(KeyVal.create(key, value));
		return this;
	}

	@Override
	public Connection sslSocketFactory(final SSLSocketFactory sslSocketFactory) {
		req.sslSocketFactory(sslSocketFactory);
		return this;
	}

	@Override
	public Connection data(final String key, final String filename, final InputStream inputStream) {
		req.data(KeyVal.create(key, filename, inputStream));
		return this;
	}

	@Override
	public Connection data(final String key, final String filename, final InputStream inputStream,
			final String contentType) {
		req.data(KeyVal.create(key, filename, inputStream).contentType(contentType));
		return this;
	}

	@Override
	public Connection data(final Map<String, String> data) {
		Validate.notNull(data, "Data map must not be null");
		for (final Map.Entry<String, String> entry : data.entrySet()) {
			req.data(KeyVal.create(entry.getKey(), entry.getValue()));
		}
		return this;
	}

	@Override
	public Connection data(final String... keyvals) {
		Validate.notNull(keyvals, "Data key value pairs must not be null");
		Validate.isTrue(keyvals.length % 2 == 0, "Must supply an even number of key value pairs");
		for (int i = 0; i < keyvals.length; i += 2) {
			final String key = keyvals[i];
			final String value = keyvals[i + 1];
			Validate.notEmpty(key, "Data key must not be empty");
			Validate.notNull(value, "Data value must not be null");
			req.data(KeyVal.create(key, value));
		}
		return this;
	}

	@Override
	public Connection data(final Collection<Connection.KeyVal> data) {
		Validate.notNull(data, "Data collection must not be null");
		for (final Connection.KeyVal entry : data) {
			req.data(entry);
		}
		return this;
	}

	@Override
	public Connection.KeyVal data(final String key) {
		Validate.notEmpty(key, "Data key must not be empty");
		for (final Connection.KeyVal keyVal : request().data()) {
			if (keyVal.key().equals(key)) {
				return keyVal;
			}
		}
		return null;
	}

	@Override
	public Connection requestBody(final String body) {
		req.requestBody(body);
		return this;
	}

	@Override
	public Connection header(final String name, final String value) {
		req.header(name, value);
		return this;
	}

	@Override
	public Connection headers(final Map<String, String> headers) {
		Validate.notNull(headers, "Header map must not be null");
		for (final Map.Entry<String, String> entry : headers.entrySet()) {
			req.header(entry.getKey(), entry.getValue());
		}
		return this;
	}

	@Override
	public Connection cookie(final String name, final String value) {
		req.cookie(name, value);
		return this;
	}

	@Override
	public Connection cookies(final Map<String, String> cookies) {
		Validate.notNull(cookies, "Cookie map must not be null");
		for (final Map.Entry<String, String> entry : cookies.entrySet()) {
			req.cookie(entry.getKey(), entry.getValue());
		}
		return this;
	}

	@Override
	public Connection parser(final Parser parser) {
		req.parser(parser);
		return this;
	}

	@Override
	public Document get() throws IOException {
		req.method(Method.GET);
		execute();
		return res.parse();
	}

	@Override
	public Document post() throws IOException {
		req.method(Method.POST);
		execute();
		return res.parse();
	}

	@Override
	public Connection.Response execute() throws IOException {
		res = Response.execute(req);
		return res;
	}

	@Override
	public Connection.Request request() {
		return req;
	}

	@Override
	public Connection request(final Connection.Request request) {
		req = request;
		return this;
	}

	@Override
	public Connection.Response response() {
		return res;
	}

	@Override
	public Connection response(final Connection.Response response) {
		res = response;
		return this;
	}

	@Override
	public Connection postDataCharset(final String charset) {
		req.postDataCharset(charset);
		return this;
	}

	@SuppressWarnings({ "unchecked" })
	private static abstract class Base<T extends Connection.Base> implements Connection.Base<T> {
		URL url;
		Method method;
		Map<String, List<String>> headers;
		Map<String, String> cookies;

		private Base() {
			headers = new LinkedHashMap<>();
			cookies = new LinkedHashMap<>();
		}

		@Override
		public URL url() {
			return url;
		}

		@Override
		public T url(final URL url) {
			Validate.notNull(url, "URL must not be null");
			this.url = url;
			return (T) this;
		}

		@Override
		public Method method() {
			return method;
		}

		@Override
		public T method(final Method method) {
			Validate.notNull(method, "Method must not be null");
			this.method = method;
			return (T) this;
		}

		@Override
		public String header(final String name) {
			Validate.notNull(name, "Header name must not be null");
			final List<String> vals = getHeadersCaseInsensitive(name);
			if (vals.size() > 0) {
				// https://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.2
				return StringUtil.join(vals, ", ");
			}

			return null;
		}

		@Override
		public T addHeader(final String name, String value) {
			Validate.notEmpty(name);
			value = value == null ? "" : value;

			List<String> values = headers(name);
			if (values.isEmpty()) {
				values = new ArrayList<>();
				headers.put(name, values);
			}
			values.add(fixHeaderEncoding(value));

			return (T) this;
		}

		@Override
		public List<String> headers(final String name) {
			Validate.notEmpty(name);
			return getHeadersCaseInsensitive(name);
		}

		private static String fixHeaderEncoding(final String val) {
			try {
				final byte[] bytes = val.getBytes("ISO-8859-1");
				if (!looksLikeUtf8(bytes)) {
					return val;
				}
				return new String(bytes, "UTF-8");
			} catch (final UnsupportedEncodingException e) {
				// shouldn't happen as these both always exist
				return val;
			}
		}

		private static boolean looksLikeUtf8(final byte[] input) {
			int i = 0;
			// BOM:
			if (input.length >= 3 && (input[0] & 0xFF) == 0xEF
					&& (input[1] & 0xFF) == 0xBB & (input[2] & 0xFF) == 0xBF) {
				i = 3;
			}

			int end;
			for (final int j = input.length; i < j; ++i) {
				int o = input[i];
				if ((o & 0x80) == 0) {
					continue; // ASCII
				}

				// UTF-8 leading:
				if ((o & 0xE0) == 0xC0) {
					end = i + 1;
				} else if ((o & 0xF0) == 0xE0) {
					end = i + 2;
				} else if ((o & 0xF8) == 0xF0) {
					end = i + 3;
				} else {
					return false;
				}

				if (end >= input.length) {
					return false;
				}

				while (i < end) {
					i++;
					o = input[i];
					if ((o & 0xC0) != 0x80) {
						return false;
					}
				}
			}
			return true;
		}

		@Override
		public T header(final String name, final String value) {
			Validate.notEmpty(name, "Header name must not be empty");
			removeHeader(name); // ensures we don't get an "accept-encoding" and a
										// "Accept-Encoding"
			addHeader(name, value);
			return (T) this;
		}

		@Override
		public boolean hasHeader(final String name) {
			Validate.notEmpty(name, "Header name must not be empty");
			return !getHeadersCaseInsensitive(name).isEmpty();
		}

		/**
		 * Test if the request has a header with this value (case insensitive).
		 */
		@Override
		public boolean hasHeaderWithValue(final String name, final String value) {
			Validate.notEmpty(name);
			Validate.notEmpty(value);
			final List<String> values = headers(name);
			for (final String candidate : values) {
				if (value.equalsIgnoreCase(candidate)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public T removeHeader(final String name) {
			Validate.notEmpty(name, "Header name must not be empty");
			final Map.Entry<String, List<String>> entry = scanHeaders(name); // remove
																									// is
			// case
			// insensitive
			// too
			if (entry != null) {
				headers.remove(entry.getKey()); // ensures correct case
			}
			return (T) this;
		}

		@Override
		public Map<String, String> headers() {
			final LinkedHashMap<String, String> map = new LinkedHashMap<>(headers.size());
			for (final Map.Entry<String, List<String>> entry : headers.entrySet()) {
				final String header = entry.getKey();
				final List<String> values = entry.getValue();
				if (values.size() > 0) {
					map.put(header, values.get(0));
				}
			}
			return map;
		}

		@Override
		public Map<String, List<String>> multiHeaders() {
			return headers;
		}

		private List<String> getHeadersCaseInsensitive(final String name) {
			Validate.notNull(name);

			for (final Map.Entry<String, List<String>> entry : headers.entrySet()) {
				if (name.equalsIgnoreCase(entry.getKey())) {
					return entry.getValue();
				}
			}

			return Collections.emptyList();
		}

		private Map.Entry<String, List<String>> scanHeaders(final String name) {
			final String lc = lowerCase(name);
			for (final Map.Entry<String, List<String>> entry : headers.entrySet()) {
				if (lowerCase(entry.getKey()).equals(lc)) {
					return entry;
				}
			}
			return null;
		}

		@Override
		public String cookie(final String name) {
			Validate.notEmpty(name, "Cookie name must not be empty");
			return cookies.get(name);
		}

		@Override
		public T cookie(final String name, final String value) {
			Validate.notEmpty(name, "Cookie name must not be empty");
			Validate.notNull(value, "Cookie value must not be null");
			cookies.put(name, value);
			return (T) this;
		}

		@Override
		public boolean hasCookie(final String name) {
			Validate.notEmpty(name, "Cookie name must not be empty");
			return cookies.containsKey(name);
		}

		@Override
		public T removeCookie(final String name) {
			Validate.notEmpty(name, "Cookie name must not be empty");
			cookies.remove(name);
			return (T) this;
		}

		@Override
		public Map<String, String> cookies() {
			return cookies;
		}
	}

	public static class Request extends HttpConnection.Base<Connection.Request>
			implements Connection.Request {
		private Proxy proxy; // nullable
		private int timeoutMilliseconds;
		private int maxBodySizeBytes;
		private boolean followRedirects;
		private final Collection<Connection.KeyVal> data;
		private String body = null;
		private boolean ignoreHttpErrors = false;
		private boolean ignoreContentType = false;
		private Parser parser;
		private boolean parserDefined = false; // called parser(...) vs
															// initialized in ctor
		private String postDataCharset = DataUtil.defaultCharset;
		private SSLSocketFactory sslSocketFactory;

		Request() {
			timeoutMilliseconds = 30000; // 30 seconds
			maxBodySizeBytes = 1024 * 1024 * 2; // 2MB
			followRedirects = true;
			data = new ArrayList<>();
			method = Method.GET;
			addHeader("Accept-Encoding", "gzip");
			addHeader(USER_AGENT, DEFAULT_UA);
			parser = Parser.htmlParser();
		}

		@Override
		public Proxy proxy() {
			return proxy;
		}

		@Override
		public Request proxy(final Proxy proxy) {
			this.proxy = proxy;
			return this;
		}

		@Override
		public Request proxy(final String host, final int port) {
			this.proxy = new Proxy(Proxy.Type.HTTP, InetSocketAddress.createUnresolved(host, port));
			return this;
		}

		@Override
		public int timeout() {
			return timeoutMilliseconds;
		}

		@Override
		public Request timeout(final int millis) {
			Validate.isTrue(millis >= 0, "Timeout milliseconds must be 0 (infinite) or greater");
			timeoutMilliseconds = millis;
			return this;
		}

		@Override
		public int maxBodySize() {
			return maxBodySizeBytes;
		}

		@Override
		public Connection.Request maxBodySize(final int bytes) {
			Validate.isTrue(bytes >= 0, "maxSize must be 0 (unlimited) or larger");
			maxBodySizeBytes = bytes;
			return this;
		}

		@Override
		public boolean followRedirects() {
			return followRedirects;
		}

		@Override
		public Connection.Request followRedirects(final boolean followRedirects) {
			this.followRedirects = followRedirects;
			return this;
		}

		@Override
		public boolean ignoreHttpErrors() {
			return ignoreHttpErrors;
		}

		@Override
		public SSLSocketFactory sslSocketFactory() {
			return sslSocketFactory;
		}

		@Override
		public void sslSocketFactory(final SSLSocketFactory sslSocketFactory) {
			this.sslSocketFactory = sslSocketFactory;
		}

		@Override
		public Connection.Request ignoreHttpErrors(final boolean ignoreHttpErrors) {
			this.ignoreHttpErrors = ignoreHttpErrors;
			return this;
		}

		@Override
		public boolean ignoreContentType() {
			return ignoreContentType;
		}

		@Override
		public Connection.Request ignoreContentType(final boolean ignoreContentType) {
			this.ignoreContentType = ignoreContentType;
			return this;
		}

		@Override
		public Request data(final Connection.KeyVal keyval) {
			Validate.notNull(keyval, "Key val must not be null");
			data.add(keyval);
			return this;
		}

		@Override
		public Collection<Connection.KeyVal> data() {
			return data;
		}

		@Override
		public Connection.Request requestBody(final String body) {
			this.body = body;
			return this;
		}

		@Override
		public String requestBody() {
			return body;
		}

		@Override
		public Request parser(final Parser parser) {
			this.parser = parser;
			parserDefined = true;
			return this;
		}

		@Override
		public Parser parser() {
			return parser;
		}

		@Override
		public Connection.Request postDataCharset(final String charset) {
			Validate.notNull(charset, "Charset must not be null");
			if (!Charset.isSupported(charset)) {
				throw new IllegalCharsetNameException(charset);
			}
			this.postDataCharset = charset;
			return this;
		}

		@Override
		public String postDataCharset() {
			return postDataCharset;
		}
	}

	public static class Response extends HttpConnection.Base<Connection.Response>
			implements Connection.Response {
		private static final int MAX_REDIRECTS = 20;
		private static final String LOCATION = "Location";
		private int statusCode;
		private String statusMessage;
		private ByteBuffer byteData;
		private InputStream bodyStream;
		private HttpURLConnection conn;
		private String charset;
		private String contentType;
		private boolean executed = false;
		private boolean inputStreamRead = false;
		private int numRedirects = 0;
		private Connection.Request req;

		/*
		 * Matches XML content types (like text/xml,
		 * application/xhtml+xml;charset=UTF8, etc)
		 */
		private static final Pattern xmlContentTypeRxp = Pattern
				.compile("(application|text)/\\w*\\+?xml.*");

		Response() {
			super();
		}

		private Response(final Response previousResponse) throws IOException {
			super();
			if (previousResponse != null) {
				numRedirects = previousResponse.numRedirects + 1;
				if (numRedirects >= MAX_REDIRECTS) {
					throw new IOException(String.format(
							"Too many redirects occurred trying to load URL %s", previousResponse.url()));
				}
			}
		}

		static Response execute(final Connection.Request req) throws IOException {
			return execute(req, null);
		}

		static Response execute(final Connection.Request req, final Response previousResponse)
				throws IOException {
			Validate.notNull(req, "Request must not be null");
			Validate.notNull(req.url(), "URL must be specified to connect");
			final String protocol = req.url().getProtocol();
			if (!protocol.equals("http") && !protocol.equals("https")) {
				throw new MalformedURLException("Only http & https protocols supported");
			}
			final boolean methodHasBody = req.method().hasBody();
			final boolean hasRequestBody = req.requestBody() != null;
			if (!methodHasBody) {
				Validate.isFalse(hasRequestBody,
						"Cannot set a request body for HTTP method " + req.method());
			}

			// set up the request for execution
			String mimeBoundary = null;
			if (req.data().size() > 0 && (!methodHasBody || hasRequestBody)) {
				serialiseRequestUrl(req);
			} else if (methodHasBody) {
				mimeBoundary = setOutputContentType(req);
			}

			final long startTime = System.nanoTime();
			final HttpURLConnection conn = createConnection(req);
			Response res = null;
			try {
				conn.connect();
				if (conn.getDoOutput()) {
					writePost(req, conn.getOutputStream(), mimeBoundary);
				}

				final int status = conn.getResponseCode();
				res = new Response(previousResponse);
				res.setupFromConnection(conn, previousResponse);
				res.req = req;

				// redirect if there's a location header (from 3xx, or 201 etc)
				if (res.hasHeader(LOCATION) && req.followRedirects()) {
					if (status != HTTP_TEMP_REDIR) {
						req.method(Method.GET); // always redirect with a get. any
														// data param from original req are
														// dropped.
						req.data().clear();
						req.requestBody(null);
						req.removeHeader(CONTENT_TYPE);
					}

					String location = res.header(LOCATION);
					if (location.startsWith("http:/") && location.charAt(6) != '/') {
						// broken
						// Location:
						// http:/temp/AAG_New/en/index.php
						location = location.substring(6);
					}
					final URL redir = StringUtil.resolve(req.url(), location);
					req.url(encodeUrl(redir));

					for (final Map.Entry<String, String> cookie : res.cookies.entrySet()) { // add
						// response
						// cookies
						// to
						// request
						// (for
						// e.g.
						// login
						// posts)
						req.cookie(cookie.getKey(), cookie.getValue());
					}
					return execute(req, res);
				}
				if ((status < 200 || status >= 400) && !req.ignoreHttpErrors()) {
					throw new HttpStatusException("HTTP error fetching URL", status,
							req.url().toString());
				}

				// check that we can handle the returned content type; if not, abort
				// before fetching it
				final String contentType = res.contentType();
				if (contentType != null && !req.ignoreContentType() && !contentType.startsWith("text/")
						&& !xmlContentTypeRxp.matcher(contentType).matches()) {
					throw new UnsupportedMimeTypeException(
							"Unhandled content type. Must be text/*, application/xml, or application/*+xml",
							contentType, req.url().toString());
				}

				// switch to the XML parser if content type is xml and not parser
				// not explicitly set
				if (contentType != null && xmlContentTypeRxp.matcher(contentType).matches()) {
					// only flip it if a HttpConnection.Request (i.e. don't presume
					// other impls want it):
					if (req instanceof HttpConnection.Request && !((Request) req).parserDefined) {
						req.parser(Parser.xmlParser());
					}
				}

				res.charset = DataUtil.getCharsetFromContentType(res.contentType); // may
																											// be
																											// null,
																											// readInputStream
																											// deals
																											// with
																											// it
				if (conn.getContentLength() != 0 && req.method() != HEAD) { // -1
																								// means
																								// unknown,
																								// chunked.
																								// sun
																								// throws
																								// an IO
																								// exception
																								// on
																								// 500
																								// response
																								// with
																								// no
																								// content
																								// when
																								// trying
																								// to
																								// read
																								// body
					res.bodyStream = null;
					res.bodyStream = conn.getErrorStream() != null ? conn.getErrorStream()
							: conn.getInputStream();
					if (res.hasHeaderWithValue(CONTENT_ENCODING, "gzip")) {
						res.bodyStream = new GZIPInputStream(res.bodyStream);
					} else if (res.hasHeaderWithValue(CONTENT_ENCODING, "deflate")) {
						res.bodyStream = new InflaterInputStream(res.bodyStream, new Inflater(true));
					}
					res.bodyStream = ConstrainableInputStream
							.wrap(res.bodyStream, DataUtil.bufferSize, req.maxBodySize())
							.timeout(startTime, req.timeout());
				} else {
					res.byteData = DataUtil.emptyByteBuffer();
				}
			} catch (final IOException e) {
				if (res != null) {
					res.safeClose(); // will be non-null if got to conn
				}
				throw e;
			}

			res.executed = true;
			return res;
		}

		@Override
		public int statusCode() {
			return statusCode;
		}

		@Override
		public String statusMessage() {
			return statusMessage;
		}

		@Override
		public String charset() {
			return charset;
		}

		@Override
		public Response charset(final String charset) {
			this.charset = charset;
			return this;
		}

		@Override
		public String contentType() {
			return contentType;
		}

		@Override
		public Document parse() throws IOException {
			Validate.isTrue(executed,
					"Request must be executed (with .execute(), .get(), or .post() before parsing response");
			if (byteData != null) { // bytes have been read in to the buffer, parse
											// that
				bodyStream = new ByteArrayInputStream(byteData.array());
				inputStreamRead = false; // ok to reparse if in bytes
			}
			Validate.isFalse(inputStreamRead, "Input stream already read and parsed, cannot re-read.");
			final Document doc = DataUtil.parseInputStream(bodyStream, charset, url.toExternalForm(),
					req.parser());
			charset = doc.outputSettings().charset().name(); // update charset from
																				// meta-equiv,
																				// possibly
			inputStreamRead = true;
			safeClose();
			return doc;
		}

		private void prepareByteData() {
			Validate.isTrue(executed,
					"Request must be executed (with .execute(), .get(), or .post() before getting response body");
			if (byteData == null) {
				Validate.isFalse(inputStreamRead, "Request has already been read (with .parse())");
				try {
					byteData = DataUtil.readToByteBuffer(bodyStream, req.maxBodySize());
				} catch (final IOException e) {
					throw new UncheckedIOException(e);
				} finally {
					inputStreamRead = true;
					safeClose();
				}
			}
		}

		@Override
		public String body() {
			prepareByteData();
			// charset gets set from header on execute, and from meta-equiv on
			// parse. parse may not have happened yet
			String body;
			if (charset == null) {
				body = Charset.forName(DataUtil.defaultCharset).decode(byteData).toString();
			} else {
				body = Charset.forName(charset).decode(byteData).toString();
			}
			((Buffer) byteData).rewind(); // cast to avoid covariant return type
													// change in jdk9
			return body;
		}

		@Override
		public byte[] bodyAsBytes() {
			prepareByteData();
			return byteData.array();
		}

		@Override
		public Connection.Response bufferUp() {
			prepareByteData();
			return this;
		}

		@Override
		public BufferedInputStream bodyStream() {
			Validate.isTrue(executed,
					"Request must be executed (with .execute(), .get(), or .post() before getting response body");
			Validate.isFalse(inputStreamRead, "Request has already been read");
			inputStreamRead = true;
			return ConstrainableInputStream.wrap(bodyStream, DataUtil.bufferSize, req.maxBodySize());
		}

		// set up connection defaults, and details from request
		private static HttpURLConnection createConnection(final Connection.Request req)
				throws IOException {
			final HttpURLConnection conn = (HttpURLConnection) (req.proxy() == null
					? req.url().openConnection()
					: req.url().openConnection(req.proxy()));

			conn.setRequestMethod(req.method().name());
			conn.setInstanceFollowRedirects(false); // don't rely on native
																	// redirection support
			conn.setConnectTimeout(req.timeout());
			conn.setReadTimeout(req.timeout() / 2); // gets reduced after
																	// connection is made and
																	// status is read

			if (req.sslSocketFactory() != null && conn instanceof HttpsURLConnection) {
				((HttpsURLConnection) conn).setSSLSocketFactory(req.sslSocketFactory());
			}
			if (req.method().hasBody()) {
				conn.setDoOutput(true);
			}
			if (req.cookies().size() > 0) {
				conn.addRequestProperty("Cookie", getRequestCookieString(req));
			}
			for (final Map.Entry<String, List<String>> header : req.multiHeaders().entrySet()) {
				for (final String value : header.getValue()) {
					conn.addRequestProperty(header.getKey(), value);
				}
			}
			return conn;
		}

		/**
		 * Call on completion of stream read, to close the body (or error) stream.
		 * The connection.disconnect allows
		 * keep-alives to work (as the underlying connection is actually held
		 * open, despite the name).
		 */
		private void safeClose() {
			if (bodyStream != null) {
				try {
					bodyStream.close();
				} catch (final IOException e) {
					// no-op
				} finally {
					bodyStream = null;
				}
			}
			if (conn != null) {
				conn.disconnect();
				conn = null;
			}
		}

		// set up url, method, header, cookies
		private void setupFromConnection(final HttpURLConnection conn,
				final HttpConnection.Response previousResponse) throws IOException {
			this.conn = conn;
			method = Method.valueOf(conn.getRequestMethod());
			url = conn.getURL();
			statusCode = conn.getResponseCode();
			statusMessage = conn.getResponseMessage();
			contentType = conn.getContentType();

			final Map<String, List<String>> resHeaders = createHeaderMap(conn);
			processResponseHeaders(resHeaders);

			// if from a redirect, map previous response cookies into this response
			if (previousResponse != null) {
				for (final Map.Entry<String, String> prevCookie : previousResponse.cookies()
						.entrySet()) {
					if (!hasCookie(prevCookie.getKey())) {
						cookie(prevCookie.getKey(), prevCookie.getValue());
					}
				}
				previousResponse.safeClose();
			}
		}

		private static LinkedHashMap<String, List<String>> createHeaderMap(
				final HttpURLConnection conn) {
			// the default sun impl of conn.getHeaderFields() returns header values
			// out of order
			final LinkedHashMap<String, List<String>> headers = new LinkedHashMap<>();
			int i = 0;
			while (true) {
				final String key = conn.getHeaderFieldKey(i);
				final String val = conn.getHeaderField(i);
				if (key == null && val == null) {
					break;
				}
				i++;
				if (key == null || val == null) {
					continue; // skip http1.1 line
				}

				if (headers.containsKey(key)) {
					headers.get(key).add(val);
				} else {
					final ArrayList<String> vals = new ArrayList<>();
					vals.add(val);
					headers.put(key, vals);
				}
			}
			return headers;
		}

		void processResponseHeaders(final Map<String, List<String>> resHeaders) {
			for (final Map.Entry<String, List<String>> entry : resHeaders.entrySet()) {
				final String name = entry.getKey();
				if (name == null) {
					continue; // http/1.1 line
				}

				final List<String> values = entry.getValue();
				if (name.equalsIgnoreCase("Set-Cookie")) {
					for (final String value : values) {
						if (value == null) {
							continue;
						}
						final TokenQueue cd = new TokenQueue(value);
						final String cookieName = cd.chompTo("=").trim();
						final String cookieVal = cd.consumeTo(";").trim();
						// ignores path, date, domain, validateTLSCertificates et al.
						// req'd?
						// name not blank, value not null
						if (cookieName.length() > 0) {
							cookie(cookieName, cookieVal);
						}
					}
				}
				for (final String value : values) {
					addHeader(name, value);
				}
			}
		}

		private static String setOutputContentType(final Connection.Request req) {
			String bound = null;
			if (req.hasHeader(CONTENT_TYPE)) {
				// no-op; don't add content type as already set (e.g. for
				// requestBody())
				// todo - if content type already set, we could add charset

				// if user has set content type to multipart/form-data, auto add
				// boundary.
				if (req.header(CONTENT_TYPE).contains(MULTIPART_FORM_DATA)
						&& !req.header(CONTENT_TYPE).contains("boundary")) {
					bound = DataUtil.mimeBoundary();
					req.header(CONTENT_TYPE, MULTIPART_FORM_DATA + "; boundary=" + bound);
				}

			} else if (needsMultipart(req)) {
				bound = DataUtil.mimeBoundary();
				req.header(CONTENT_TYPE, MULTIPART_FORM_DATA + "; boundary=" + bound);
			} else {
				req.header(CONTENT_TYPE, FORM_URL_ENCODED + "; charset=" + req.postDataCharset());
			}
			return bound;
		}

		private static void writePost(final Connection.Request req, final OutputStream outputStream,
				final String bound) throws IOException {
			final Collection<Connection.KeyVal> data = req.data();
			final BufferedWriter w = new BufferedWriter(
					new OutputStreamWriter(outputStream, req.postDataCharset()));

			if (bound != null) {
				// boundary will be set if we're in multipart mode
				for (final Connection.KeyVal keyVal : data) {
					w.write("--");
					w.write(bound);
					w.write("\r\n");
					w.write("Content-Disposition: form-data; name=\"");
					w.write(encodeMimeName(keyVal.key())); // encodes " to %22
					w.write("\"");
					if (keyVal.hasInputStream()) {
						w.write("; filename=\"");
						w.write(encodeMimeName(keyVal.value()));
						w.write("\"\r\nContent-Type: ");
						w.write(keyVal.contentType() != null ? keyVal.contentType() : DefaultUploadType);
						w.write("\r\n\r\n");
						w.flush(); // flush
						DataUtil.crossStreams(keyVal.inputStream(), outputStream);
						outputStream.flush();
					} else {
						w.write("\r\n\r\n");
						w.write(keyVal.value());
					}
					w.write("\r\n");
				}
				w.write("--");
				w.write(bound);
				w.write("--");
			} else if (req.requestBody() != null) {
				// data will be in query string, we're sending a plaintext body
				w.write(req.requestBody());
			} else {
				// regular form data (application/x-www-form-urlencoded)
				boolean first = true;
				for (final Connection.KeyVal keyVal : data) {
					if (!first) {
						w.append('&');
					} else {
						first = false;
					}

					w.write(URLEncoder.encode(keyVal.key(), req.postDataCharset()));
					w.write('=');
					w.write(URLEncoder.encode(keyVal.value(), req.postDataCharset()));
				}
			}
			w.close();
		}

		private static String getRequestCookieString(final Connection.Request req) {
			final StringBuilder sb = StringUtil.borrowBuilder();
			boolean first = true;
			for (final Map.Entry<String, String> cookie : req.cookies().entrySet()) {
				if (!first) {
					sb.append("; ");
				} else {
					first = false;
				}
				sb.append(cookie.getKey()).append('=').append(cookie.getValue());
				// todo: spec says only ascii, no escaping / encoding defined.
				// validate on set? or escape somehow here?
			}
			return StringUtil.releaseBuilder(sb);
		}

		// for get url reqs, serialise the data map into the url
		private static void serialiseRequestUrl(final Connection.Request req) throws IOException {
			final URL in = req.url();
			final StringBuilder url = StringUtil.borrowBuilder();
			boolean first = true;
			// reconstitute the query, ready for appends
			url.append(in.getProtocol()).append("://").append(in.getAuthority()) // includes
																										// host,
																										// port
					.append(in.getPath()).append("?");
			if (in.getQuery() != null) {
				url.append(in.getQuery());
				first = false;
			}
			for (final Connection.KeyVal keyVal : req.data()) {
				Validate.isFalse(keyVal.hasInputStream(),
						"InputStream data not supported in URL query string.");
				if (!first) {
					url.append('&');
				} else {
					first = false;
				}
				url.append(URLEncoder.encode(keyVal.key(), DataUtil.defaultCharset)).append('=')
						.append(URLEncoder.encode(keyVal.value(), DataUtil.defaultCharset));
			}
			req.url(new URL(StringUtil.releaseBuilder(url)));
			req.data().clear(); // moved into url as get params
		}
	}

	private static boolean needsMultipart(final Connection.Request req) {
		// multipart mode, for files. add the header if we see something with an
		// inputstream, and return a non-null boundary
		for (final Connection.KeyVal keyVal : req.data()) {
			if (keyVal.hasInputStream()) {
				return true;
			}
		}
		return false;
	}

	public static class KeyVal implements Connection.KeyVal {
		private String key;
		private String value;
		private InputStream stream;
		private String contentType;

		public static KeyVal create(final String key, final String value) {
			return new KeyVal().key(key).value(value);
		}

		public static KeyVal create(final String key, final String filename,
				final InputStream stream) {
			return new KeyVal().key(key).value(filename).inputStream(stream);
		}

		private KeyVal() {
		}

		@Override
		public KeyVal key(final String key) {
			Validate.notEmpty(key, "Data key must not be empty");
			this.key = key;
			return this;
		}

		@Override
		public String key() {
			return key;
		}

		@Override
		public KeyVal value(final String value) {
			Validate.notNull(value, "Data value must not be null");
			this.value = value;
			return this;
		}

		@Override
		public String value() {
			return value;
		}

		@Override
		public KeyVal inputStream(final InputStream inputStream) {
			Validate.notNull(value, "Data input stream must not be null");
			this.stream = inputStream;
			return this;
		}

		@Override
		public InputStream inputStream() {
			return stream;
		}

		@Override
		public boolean hasInputStream() {
			return stream != null;
		}

		@Override
		public Connection.KeyVal contentType(final String contentType) {
			Validate.notEmpty(contentType);
			this.contentType = contentType;
			return this;
		}

		@Override
		public String contentType() {
			return contentType;
		}

		@Override
		public String toString() {
			return key + "=" + value;
		}
	}
}
