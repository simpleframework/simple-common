package net.simpleframework.lib.org.mvel2.ast;

import static net.simpleframework.lib.org.mvel2.DataConversion.canConvert;
import static net.simpleframework.lib.org.mvel2.DataConversion.convert;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import net.simpleframework.lib.org.mvel2.CompileException;
import net.simpleframework.lib.org.mvel2.ParserContext;
import net.simpleframework.lib.org.mvel2.UnresolveablePropertyException;
import net.simpleframework.lib.org.mvel2.compiler.ExecutableStatement;
import net.simpleframework.lib.org.mvel2.integration.VariableResolver;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;
import net.simpleframework.lib.org.mvel2.integration.impl.MapVariableResolverFactory;
import net.simpleframework.lib.org.mvel2.integration.impl.SimpleValueResolver;
import net.simpleframework.lib.org.mvel2.util.CallableProxy;
import net.simpleframework.lib.org.mvel2.util.SimpleIndexHashMapWrapper;

public class Proto extends ASTNode {
	private final String name;
	private final Map<String, Receiver> receivers;
	private int cursorStart;
	private int cursorEnd;

	public Proto(final String name, final ParserContext pCtx) {
		super(pCtx);
		this.name = name;
		this.receivers = new SimpleIndexHashMapWrapper<String, Receiver>();
	}

	public Receiver declareReceiver(final String name, final Function function) {
		final Receiver r = new Receiver(null, ReceiverType.FUNCTION, function);
		receivers.put(name, r);
		return r;
	}

	public Receiver declareReceiver(final String name, final Class type,
			final ExecutableStatement initCode) {
		final Receiver r = new Receiver(null, ReceiverType.PROPERTY, initCode);
		receivers.put(name, r);
		return r;
	}

	public Receiver declareReceiver(final String name, final ReceiverType type,
			final ExecutableStatement initCode) {
		final Receiver r = new Receiver(null, type, initCode);
		receivers.put(name, r);
		return r;
	}

	public ProtoInstance newInstance(final Object ctx, final Object thisCtx,
			final VariableResolverFactory factory) {
		return new ProtoInstance(this, ctx, thisCtx, factory);
	}

	@Override
	public Object getReducedValue(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		factory.createVariable(name, this);
		return this;
	}

	@Override
	public Object getReducedValueAccelerated(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		factory.createVariable(name, this);
		return this;
	}

	public class Receiver implements CallableProxy {
		private ReceiverType type;
		private Object receiver;
		private ExecutableStatement initValue;
		private final ProtoInstance instance;

		public Receiver(final ProtoInstance protoInstance, final ReceiverType type,
				final Object receiver) {
			this.instance = protoInstance;
			this.type = type;
			this.receiver = receiver;
		}

		public Receiver(final ProtoInstance protoInstance, final ReceiverType type,
				final ExecutableStatement stmt) {
			this.instance = protoInstance;
			this.type = type;
			this.initValue = stmt;
		}

		@Override
		public Object call(final Object ctx, final Object thisCtx,
				final VariableResolverFactory factory, final Object[] parms) {
			switch (type) {
			case FUNCTION:
				return ((Function) receiver).call(ctx, thisCtx,
						new InvokationContextFactory(factory, instance.instanceStates), parms);
			case PROPERTY:
				return receiver;
			case DEFERRED:
				throw new CompileException("unresolved prototype receiver", expr, start);
			}
			return null;
		}

		public Receiver init(final ProtoInstance instance, final Object ctx, final Object thisCtx,
				final VariableResolverFactory factory) {
			return new Receiver(instance, type, type == ReceiverType.PROPERTY && initValue != null
					? initValue.getValue(ctx, thisCtx, factory) : receiver);
		}

		public void setType(final ReceiverType type) {
			this.type = type;
		}

		public void setInitValue(final ExecutableStatement initValue) {
			this.initValue = initValue;
		}
	}

	public enum ReceiverType {
		DEFERRED, FUNCTION, PROPERTY
	}

	public class ProtoInstance implements Map<String, Receiver> {
		private final Proto protoType;
		private final VariableResolverFactory instanceStates;
		private final SimpleIndexHashMapWrapper<String, Receiver> receivers;

		public ProtoInstance(final Proto protoType, final Object ctx, final Object thisCtx,
				final VariableResolverFactory factory) {
			this.protoType = protoType;

			receivers = new SimpleIndexHashMapWrapper<String, Receiver>();
			for (final Map.Entry<String, Receiver> entry : protoType.receivers.entrySet()) {
				receivers.put(entry.getKey(), entry.getValue().init(this, ctx, thisCtx, factory));
			}

			instanceStates = new ProtoContextFactory(receivers);
		}

		public Proto getProtoType() {
			return protoType;
		}

		@Override
		public int size() {
			return receivers.size();
		}

		@Override
		public boolean isEmpty() {
			return receivers.isEmpty();
		}

		@Override
		public boolean containsKey(final Object key) {
			return receivers.containsKey(key);
		}

		@Override
		public boolean containsValue(final Object value) {
			return receivers.containsValue(value);
		}

		@Override
		public Receiver get(final Object key) {
			return receivers.get(key);
		}

		@Override
		public Receiver put(final String key, final Receiver value) {
			return receivers.put(key, value);
		}

		@Override
		public Receiver remove(final Object key) {
			return receivers.remove(key);
		}

		@Override
		public void putAll(final Map m) {
		}

		@Override
		public void clear() {
		}

		@Override
		public Set<String> keySet() {
			return receivers.keySet();
		}

		@Override
		public Collection<Receiver> values() {
			return receivers.values();
		}

		@Override
		public Set<Entry<String, Receiver>> entrySet() {
			return receivers.entrySet();
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "proto " + name;
	}

	public class ProtoContextFactory extends MapVariableResolverFactory {
		private final SimpleIndexHashMapWrapper<String, VariableResolver> variableResolvers;

		public ProtoContextFactory(final SimpleIndexHashMapWrapper variables) {
			super(variables);
			variableResolvers = new SimpleIndexHashMapWrapper<String, VariableResolver>(variables,
					true);
		}

		@Override
		public VariableResolver createVariable(final String name, final Object value) {
			VariableResolver vr;

			try {
				(vr = getVariableResolver(name)).setValue(value);
				return vr;
			} catch (final UnresolveablePropertyException e) {
				addResolver(name, vr = new ProtoResolver(variables, name)).setValue(value);
				return vr;
			}
		}

		@Override
		public VariableResolver createVariable(final String name, final Object value,
				final Class<?> type) {
			VariableResolver vr;
			try {
				vr = getVariableResolver(name);
			} catch (final UnresolveablePropertyException e) {
				vr = null;
			}

			if (vr != null && vr.getType() != null) {
				throw new CompileException(
						"variable already defined within scope: " + vr.getType() + " " + name, expr,
						start);
			} else {
				addResolver(name, vr = new ProtoResolver(variables, name, type)).setValue(value);
				return vr;
			}
		}

		@Override
		public void setIndexedVariableNames(final String[] indexedVariableNames) {
			//
		}

		@Override
		public String[] getIndexedVariableNames() {
			//
			return null;
		}

		@Override
		public VariableResolver createIndexedVariable(final int index, final String name,
				final Object value, final Class<?> type) {
			final VariableResolver vr = this.variableResolvers != null
					? this.variableResolvers.getByIndex(index) : null;
			if (vr != null && vr.getType() != null) {
				throw new CompileException(
						"variable already defined within scope: " + vr.getType() + " " + name, expr,
						start);
			} else {
				return createIndexedVariable(variableIndexOf(name), name, value);
			}
		}

		@Override
		public VariableResolver createIndexedVariable(final int index, final String name,
				final Object value) {
			VariableResolver vr = variableResolvers.getByIndex(index);

			if (vr == null) {
				vr = new SimpleValueResolver(value);
				variableResolvers.putAtIndex(index, vr);
			} else {
				vr.setValue(value);
			}

			return indexedVariableResolvers[index];
		}

		@Override
		public VariableResolver getIndexedVariableResolver(final int index) {
			return variableResolvers.getByIndex(index);
		}

		@Override
		public VariableResolver setIndexedVariableResolver(final int index,
				final VariableResolver resolver) {
			variableResolvers.putAtIndex(index, resolver);
			return resolver;
		}

		@Override
		public int variableIndexOf(final String name) {
			return variableResolvers.indexOf(name);
		}

		@Override
		public VariableResolver getVariableResolver(final String name) {
			VariableResolver vr = variableResolvers.get(name);
			if (vr != null) {
				return vr;
			} else if (variables.containsKey(name)) {
				variableResolvers.put(name, vr = new ProtoResolver(variables, name));
				return vr;
			} else if (nextFactory != null) {
				return nextFactory.getVariableResolver(name);
			}

			throw new UnresolveablePropertyException("unable to resolve variable '" + name + "'");
		}
	}

	public class ProtoResolver implements VariableResolver {
		private String name;
		private Class<?> knownType;
		private final Map<String, Object> variableMap;

		public ProtoResolver(final Map<String, Object> variableMap, final String name) {
			this.variableMap = variableMap;
			this.name = name;
		}

		public ProtoResolver(final Map<String, Object> variableMap, final String name,
				final Class knownType) {
			this.name = name;
			this.knownType = knownType;
			this.variableMap = variableMap;
		}

		public void setName(final String name) {
			this.name = name;
		}

		@Override
		public void setStaticType(final Class knownType) {
			this.knownType = knownType;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Class getType() {
			return knownType;
		}

		@Override
		public void setValue(Object value) {
			if (knownType != null && value != null && value.getClass() != knownType) {
				if (!canConvert(knownType, value.getClass())) {
					throw new CompileException("cannot assign " + value.getClass().getName()
							+ " to type: " + knownType.getName(), expr, start);
				}
				try {
					value = convert(value, knownType);
				} catch (final Exception e) {
					throw new CompileException("cannot convert value of " + value.getClass().getName()
							+ " to: " + knownType.getName(), expr, start);
				}
			}

			((Receiver) variableMap.get(name)).receiver = value;
		}

		@Override
		public Object getValue() {
			return ((Receiver) variableMap.get(name)).receiver;
		}

		@Override
		public int getFlags() {
			return 0;
		}
	}

	public void setCursorPosition(final int start, final int end) {
		this.cursorStart = start;
		this.cursorEnd = end;
	}

	public int getCursorStart() {
		return cursorStart;
	}

	public int getCursorEnd() {
		return cursorEnd;
	}
}
