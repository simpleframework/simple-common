/**
 * MVEL 2.0
 * Copyright (C) 2007 The Codehaus
 * Mike Brock, Dhanji Prasanna, John Graham, Mark Proctor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.simpleframework.lib.org.mvel2.debug;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.simpleframework.lib.org.mvel2.ast.LineLabel;
import net.simpleframework.lib.org.mvel2.compiler.CompiledExpression;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;

public class DebuggerContext {
	private Map<String, Set<Integer>> breakpoints;
	private Debugger debugger;
	private int debuggerState = 0;

	public DebuggerContext() {
		breakpoints = new HashMap<>();
	}

	public Map<String, Set<Integer>> getBreakpoints() {
		return breakpoints;
	}

	public void setBreakpoints(final Map<String, Set<Integer>> breakpoints) {
		this.breakpoints = breakpoints;
	}

	public Debugger getDebugger() {
		return debugger;
	}

	public void setDebugger(final Debugger debugger) {
		this.debugger = debugger;
	}

	public int getDebuggerState() {
		return debuggerState;
	}

	public void setDebuggerState(final int debuggerState) {
		this.debuggerState = debuggerState;
	}

	// utility methods

	public void registerBreakpoint(final String sourceFile, final int lineNumber) {
		if (!breakpoints.containsKey(sourceFile)) {
			breakpoints.put(sourceFile, new HashSet<Integer>());
		}
		breakpoints.get(sourceFile).add(lineNumber);
	}

	public void removeBreakpoint(final String sourceFile, final int lineNumber) {
		if (!breakpoints.containsKey(sourceFile)) {
			return;
		}
		breakpoints.get(sourceFile).remove(lineNumber);
	}

	public void clearAllBreakpoints() {
		breakpoints.clear();
	}

	public boolean hasBreakpoints() {
		return breakpoints.size() != 0;
	}

	public boolean hasBreakpoint(final LineLabel label) {
		return breakpoints.containsKey(label.getSourceFile())
				&& breakpoints.get(label.getSourceFile()).contains(label.getLineNumber());
	}

	public boolean hasBreakpoint(final String sourceFile, final int lineNumber) {
		return breakpoints.containsKey(sourceFile)
				&& breakpoints.get(sourceFile).contains(lineNumber);
	}

	public boolean hasDebugger() {
		return debugger != null;
	}

	public int checkBreak(final LineLabel label, final VariableResolverFactory factory,
			final CompiledExpression expression) {
		if (debuggerState == Debugger.STEP || hasBreakpoint(label)) {
			if (debugger == null) {
				throw new RuntimeException("no debugger registered to handle breakpoint");
			}
			return debuggerState = debugger.onBreak(new Frame(label, factory));

		}
		return 0;
	}

}
