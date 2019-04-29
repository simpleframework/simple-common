package net.simpleframework.lib.org.mvel2.jsr223;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

import net.simpleframework.lib.org.mvel2.MVEL;

public class MvelScriptEngineFactory implements ScriptEngineFactory {
	private static final String ENGINE_NAME = MVEL.NAME;
	private static final String ENGINE_VERSION = MVEL.VERSION;
	private static final String LANGUAGE_NAME = "mvel";
	private static final String LANGUAGE_VERSION = MVEL.VERSION;

	private static final List<String> NAMES;
	private static final List<String> EXTENSIONS;
	private static final List<String> MIME_TYPES;

	static {
		List<String> n = new ArrayList<>(1);
		n.add(LANGUAGE_NAME);
		NAMES = Collections.unmodifiableList(n);

		EXTENSIONS = NAMES;

		n = new ArrayList<>(1);
		n.add("application/x-" + LANGUAGE_NAME);
		MIME_TYPES = Collections.unmodifiableList(n);
	}

	private static final MvelScriptEngine MVEL_SCRIPT_ENGINE = new MvelScriptEngine();

	public MvelScriptEngineFactory() {
	}

	@Override
	public String getEngineName() {
		return ENGINE_NAME;
	}

	@Override
	public String getEngineVersion() {
		return ENGINE_VERSION;
	}

	@Override
	public List<String> getExtensions() {
		return EXTENSIONS;
	}

	@Override
	public List<String> getMimeTypes() {
		return MIME_TYPES;
	}

	@Override
	public List<String> getNames() {
		return NAMES;
	}

	@Override
	public String getLanguageName() {
		return LANGUAGE_NAME;
	}

	@Override
	public String getLanguageVersion() {
		return LANGUAGE_VERSION;
	}

	@Override
	public Object getParameter(final String key) {
		if (key.equals(ScriptEngine.NAME)) {
			return getLanguageName();
		} else if (key.equals(ScriptEngine.ENGINE)) {
			return getEngineName();
		} else if (key.equals(ScriptEngine.ENGINE_VERSION)) {
			return getEngineVersion();
		} else if (key.equals(ScriptEngine.LANGUAGE)) {
			return getLanguageName();
		} else if (key.equals(ScriptEngine.LANGUAGE_VERSION)) {
			return getLanguageVersion();
		} else if (key.equals("THREADING")) {
			return "THREAD-ISOLATED";
		} else {
			return null;
		}
	}

	@Override
	public String getMethodCallSyntax(final String obj, final String m, final String... args) {
		return null;
	}

	@Override
	public String getOutputStatement(final String toDisplay) {
		return null;
	}

	@Override
	public String getProgram(final String... statements) {
		return null;
	}

	@Override
	public ScriptEngine getScriptEngine() {
		return MVEL_SCRIPT_ENGINE;
	}
}
