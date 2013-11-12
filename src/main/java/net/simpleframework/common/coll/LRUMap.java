package net.simpleframework.common.coll;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class LRUMap<K, V> extends LinkedHashMap<K, V> implements Serializable {

	private final int maxEntries;

	public LRUMap(final int maxEntries) {
		super(maxEntries, .75f, true);
		this.maxEntries = maxEntries;
	}

	@Override
	protected boolean removeEldestEntry(final Map.Entry<K, V> eldest) {
		return (size() > maxEntries);
	}

	private static final long serialVersionUID = -4468453950954557286L;
}
