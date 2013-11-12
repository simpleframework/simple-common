package net.simpleframework.lib.org.mvel2.integration.impl;

import net.simpleframework.lib.org.mvel2.integration.VariableResolver;

public class IndexVariableResolver implements VariableResolver {
	private final int indexPos;
	private final Object[] vars;

	public IndexVariableResolver(final int indexPos, final Object[] vars) {
		this.indexPos = indexPos;
		this.vars = vars;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public Class getType() {
		return null;
	}

	@Override
	public void setStaticType(final Class type) {
	}

	@Override
	public int getFlags() {
		return 0;
	}

	@Override
	public Object getValue() {
		return vars[indexPos];
	}

	@Override
	public void setValue(final Object value) {
		vars[indexPos] = value;
	}
}
