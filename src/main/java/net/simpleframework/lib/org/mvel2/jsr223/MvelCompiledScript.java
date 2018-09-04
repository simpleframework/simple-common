package net.simpleframework.lib.org.mvel2.jsr223;

import java.io.Serializable;

import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

public class MvelCompiledScript extends CompiledScript {

	private final MvelScriptEngine scriptEngine;
	private final Serializable compiledScript;

	public MvelCompiledScript(final MvelScriptEngine engine, final Serializable compiledScript) {
		this.scriptEngine = engine;
		this.compiledScript = compiledScript;
	}

	@Override
	public Object eval(final ScriptContext context) throws ScriptException {
		return scriptEngine.evaluate(compiledScript, context);
	}

	@Override
	public ScriptEngine getEngine() {
		return scriptEngine;
	}
}
