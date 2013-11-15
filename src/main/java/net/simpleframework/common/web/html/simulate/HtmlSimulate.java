package net.simpleframework.common.web.html.simulate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;

import net.simpleframework.common.ClassUtils;
import net.simpleframework.common.IoUtils;
import net.simpleframework.common.logger.Log;
import net.simpleframework.common.logger.LogFactory;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class HtmlSimulate {

	static Log log = LogFactory.getLogger(HtmlSimulate.class);

	static Context ctx;
	static Scriptable scope;
	static {
		ctx = ContextFactory.getGlobal().enter();
		ctx.setOptimizationLevel(-1);
		ctx.setLanguageVersion(Context.VERSION_1_5);

		scope = ctx.initStandardObjects();
		eval("function print(message) { java.lang.System.out.println(message); }", "print");
		try {
			eval(ClassUtils.getResourceAsString(HtmlSimulate.class, "env.rhino.1.2.js"),
					"env.rhino.1.2");
			eval(IoUtils.getStringFromInputStream(new URL("http://code.jquery.com/jquery-1.7.2.min.js")
					.openStream()), "jquery");
		} catch (final Throwable e) {
			log.error(e);
		}
	}

	public static void eval(final String source, final String sourceName) {
		ctx.evaluateString(scope, source, sourceName, 1, null);
	}

	public static void eval(final String source) {
		eval(source, null);
	}

	public static String htmlToHtml(final String html) throws IOException {
		scope.put("req", scope, new BufferedReader(new StringReader(html)));
		eval(ClassUtils.getResourceAsString(HtmlSimulate.class, "parser.utils.js"), "parser.utils");
		return String.valueOf(scope.get("req", scope));
	}

	public static String urlToHtml(final String url) throws IOException {
		return String.valueOf(scope.get("req", scope));
	}

	public static void main(final String[] args) {
		try {
			// scope.put("oUrl", scope, "http://www.baidu.com/");
			// eval(IoUtils.getStringFromInputStream(ClassUtils.getResourceAsStream(HtmlSimulate.class,
			// "parser.utils.js")), "parser.utils");
			// System.out.println(scope.get("oUrl", scope));

			System.out.println(htmlToHtml(ClassUtils.getResourceAsString(HtmlSimulate.class,
					"test_case.html")));

			// System.out
			// .println(urlToHtml("http://localhost:9090/dtproject/demo/com-sansoft-project-biaozhun-MyBiaozhunPage"));
		} catch (final Exception e) {
			e.fillInStackTrace();
		}
	}
}
