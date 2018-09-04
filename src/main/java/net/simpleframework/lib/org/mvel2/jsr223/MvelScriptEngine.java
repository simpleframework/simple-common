package net.simpleframework.lib.org.mvel2.jsr223;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;

import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

import net.simpleframework.lib.org.mvel2.MVEL;

public class MvelScriptEngine extends AbstractScriptEngine implements ScriptEngine, Compilable {

	private volatile MvelScriptEngineFactory factory;

	@Override
	public Object eval(final String script, final ScriptContext context) throws ScriptException {
		final Serializable expression = compiledScript(script);
		return evaluate(expression, context);
	}

	@Override
	public Object eval(final Reader reader, final ScriptContext context) throws ScriptException {
		return this.eval(readFully(reader), context);
	}

	@Override
	public Bindings createBindings() {
		return new MvelBindings();
	}

	@Override
	public ScriptEngineFactory getFactory() {
		if (this.factory == null) {
			synchronized (this) {
				if (this.factory == null) {
					this.factory = new MvelScriptEngineFactory();
				}
			}
		}

		return this.factory;
	}

	private static String readFully(final Reader reader) throws ScriptException {
		final char[] arr = new char[8192];
		final StringBuilder buf = new StringBuilder();

		int numChars;
		try {
			while ((numChars = reader.read(arr, 0, arr.length)) > 0) {
				buf.append(arr, 0, numChars);
			}
		} catch (final IOException var5) {
			throw new ScriptException(var5);
		}

		return buf.toString();
	}

	@Override
	public CompiledScript compile(final String script) throws ScriptException {
		return new MvelCompiledScript(this, compiledScript(script));
	}

	@Override
	public CompiledScript compile(final Reader reader) throws ScriptException {
		return this.compile(readFully(reader));
	}

	public Serializable compiledScript(final String script) throws ScriptException {
		try {
			final Serializable expression = MVEL.compileExpression(script);
			return expression;
		} catch (final Exception e) {
			throw new ScriptException(e);
		}
	}

	public Object evaluate(final Serializable expression, final ScriptContext context)
			throws ScriptException {
		try {
			return MVEL.executeExpression(expression, context.getBindings(ScriptContext.ENGINE_SCOPE));
		} catch (final Exception e) {
			throw new ScriptException(e);
		}
	}
}
