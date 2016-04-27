package net.simpleframework.lib.net.sf.cglib.core.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.simpleframework.lib.net.sf.cglib.core.Customizer;
import net.simpleframework.lib.net.sf.cglib.core.KeyFactoryCustomizer;

public class CustomizerRegistry {
	private final Class[] customizerTypes;
	private final Map<Class, List<KeyFactoryCustomizer>> customizers = new HashMap<Class, List<KeyFactoryCustomizer>>();

	public CustomizerRegistry(final Class[] customizerTypes) {
		this.customizerTypes = customizerTypes;
	}

	public void add(final KeyFactoryCustomizer customizer) {
		final Class<? extends KeyFactoryCustomizer> klass = customizer.getClass();
		for (final Class type : customizerTypes) {
			if (type.isAssignableFrom(klass)) {
				List<KeyFactoryCustomizer> list = customizers.get(type);
				if (list == null) {
					customizers.put(type, list = new ArrayList<KeyFactoryCustomizer>());
				}
				list.add(customizer);
			}
		}
	}

	public <T> List<T> get(final Class<T> klass) {
		final List<KeyFactoryCustomizer> list = customizers.get(klass);
		if (list == null) {
			return Collections.emptyList();
		}
		return (List<T>) list;
	}

	/**
	 * @deprecated Only to keep backward compatibility.
	 */
	@Deprecated
	public static CustomizerRegistry singleton(final Customizer customizer) {
		final CustomizerRegistry registry = new CustomizerRegistry(new Class[] { Customizer.class });
		registry.add(customizer);
		return registry;
	}
}
