package net.simpleframework.lib.org.mvel2.optimizers.impl.refl.nodes;

import static net.simpleframework.lib.org.mvel2.DataConversion.convert;

import java.lang.reflect.Array;

import net.simpleframework.lib.org.mvel2.compiler.ExecutableStatement;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;

public abstract class InvokableAccessor extends BaseAccessor {
	protected int length;
	protected ExecutableStatement[] parms;
	protected Class[] parameterTypes;
	protected boolean coercionNeeded = false;

	protected Object[] executeAndCoerce(final Class[] target, final Object elCtx,
			final VariableResolverFactory vars, final boolean isVarargs) {
		final Object[] values = new Object[length];
		for (int i = 0; i < length && !(isVarargs && i >= length - 1); i++) {
			// noinspection unchecked
			values[i] = convert(parms[i].getValue(elCtx, vars), target[i]);
		}
		if (isVarargs) {
			final Class<?> componentType = target[length - 1].getComponentType();
			Object vararg;
			if (parms == null) {
				vararg = Array.newInstance(componentType, 0);
			} else {
				vararg = Array.newInstance(componentType, parms.length - length + 1);
				for (int i = length - 1; i < parms.length; i++) {
					Array.set(vararg, i - length + 1,
							convert(parms[i].getValue(elCtx, vars), componentType));
				}
			}
			values[length - 1] = vararg;
		}
		return values;
	}

	public Class[] getParameterTypes() {
		return parameterTypes;
	}
}
