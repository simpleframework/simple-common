package net.simpleframework.lib.org.mvel2.util;

import java.util.Set;

import net.simpleframework.lib.org.mvel2.MVEL;
import net.simpleframework.lib.org.mvel2.ParserContext;

/**
 * @author Mike Brock .
 */
public class VariableSpaceCompiler {
	private static final Object[] EMPTY_OBJ = new Object[0];

	public static SharedVariableSpaceModel compileShared(final String expr,
			final ParserContext pCtx) {
		return compileShared(expr, pCtx, EMPTY_OBJ);
	}

	public static SharedVariableSpaceModel compileShared(final String expr, final ParserContext pCtx,
			final Object[] vars) {
		final String[] varNames = pCtx.getIndexedVarNames();

		final ParserContext analysisContext = ParserContext.create();
		analysisContext.setIndexAllocation(true);

		MVEL.analysisCompile(expr, analysisContext);

		final Set<String> localNames = analysisContext.getVariables().keySet();

		pCtx.addIndexedLocals(localNames);

		final String[] locals = localNames.toArray(new String[localNames.size()]);
		final String[] allVars = new String[varNames.length + locals.length];

		System.arraycopy(varNames, 0, allVars, 0, varNames.length);
		System.arraycopy(locals, 0, allVars, varNames.length, locals.length);

		return new SharedVariableSpaceModel(allVars, vars);
	}

	public static SimpleVariableSpaceModel compile(final String expr, final ParserContext pCtx) {
		final String[] varNames = pCtx.getIndexedVarNames();

		final ParserContext analysisContext = ParserContext.create();
		analysisContext.setIndexAllocation(true);

		MVEL.analysisCompile(expr, analysisContext);

		final Set<String> localNames = analysisContext.getVariables().keySet();

		pCtx.addIndexedLocals(localNames);

		final String[] locals = localNames.toArray(new String[localNames.size()]);
		final String[] allVars = new String[varNames.length + locals.length];

		System.arraycopy(varNames, 0, allVars, 0, varNames.length);
		System.arraycopy(locals, 0, allVars, varNames.length, locals.length);

		return new SimpleVariableSpaceModel(allVars);
	}
}
