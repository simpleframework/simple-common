package net.simpleframework.lib.org.mvel2.integration.impl;

import static net.simpleframework.lib.org.mvel2.DataConversion.canConvert;
import static net.simpleframework.lib.org.mvel2.DataConversion.convert;

import java.util.Map;

import net.simpleframework.lib.org.mvel2.integration.VariableResolver;

public class PrecachedMapVariableResolver implements VariableResolver {
	private String name;
	private Class<?> knownType;
	private final Map.Entry entry;

	public PrecachedMapVariableResolver(final Map.Entry entry, final String name) {
		this.entry = entry;
		this.name = name;
	}

	public PrecachedMapVariableResolver(final Map.Entry entry, final String name,
			final Class knownType) {
		this.name = name;
		this.knownType = knownType;
		this.entry = entry;
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
			final Class t = value.getClass();
			if (!canConvert(knownType, t)) {
				throw new RuntimeException("cannot assign " + value.getClass().getName() + " to type: "
						+ knownType.getName());
			}
			try {
				value = convert(value, knownType);
			} catch (final Exception e) {
				throw new RuntimeException("cannot convert value of " + value.getClass().getName()
						+ " to: " + knownType.getName());
			}
		}

		// noinspection unchecked
		entry.setValue(value);
	}

	@Override
	public Object getValue() {
		return entry.getValue();
	}

	@Override
	public int getFlags() {
		return 0;
	}
}
