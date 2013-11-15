package net.simpleframework.common.coll;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import net.simpleframework.common.Convert;
import net.simpleframework.common.JsonUtils;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
@SuppressWarnings({ "serial", "unchecked" })
public abstract class AbstractKVMap<T, M extends AbstractKVMap<T, M>> implements Map<String, T>,
		Serializable {
	private Map<String, T> kv;

	/* 设置插入的null值 */
	private T nullVal;

	public AbstractKVMap(final int initialCapacity) {
		kv = create(initialCapacity);
	}

	public AbstractKVMap() {
		this(16);
	}

	protected Map<String, T> create(final int initialCapacity) {
		return new LinkedHashMap<String, T>(initialCapacity);
	}

	public M add(final String key, final T value) {
		put(key, value);
		return (M) this;
	}

	public M addAll(final Map<String, T> m) {
		putAll(m);
		return (M) this;
	}

	public Map<String, T> map() {
		return kv;
	}

	public String toJSON() {
		return JsonUtils.toJSON(kv);
	}

	// for caseInsensitive

	private Map<String, String> caseInsensitiveKeys;

	private boolean caseInsensitive;

	public boolean isCaseInsensitive() {
		return caseInsensitive;
	}

	public M setCaseInsensitive(final boolean caseInsensitive) {
		if (caseInsensitive) {
			caseInsensitiveKeys = new HashMap<String, String>();
		}
		this.caseInsensitive = caseInsensitive;
		return (M) this;
	}

	public T getNullVal() {
		return nullVal;
	}

	public M setNullVal(final T nullVal) {
		this.nullVal = nullVal;
		return (M) this;
	}

	protected String caseInsensitiveKey(final String key) {
		return key.toLowerCase();
	}

	protected String key(final Object key) {
		final String key2 = Convert.toString(key);
		return caseInsensitiveKeys != null ? caseInsensitiveKeys.get(caseInsensitiveKey(key2)) : key2;
	}

	// map implement

	@Override
	public T put(final String key, final T value) {
		if (caseInsensitiveKeys != null) {
			caseInsensitiveKeys.put(caseInsensitiveKey(key), key);
		}
		return kv.put(key, value != null ? value : getNullVal());
	}

	@Override
	public T get(final Object key) {
		return kv.get(key(key));
	}

	@Override
	public T remove(final Object key) {
		return kv.remove(key(key));
	}

	@Override
	public void putAll(final Map<? extends String, ? extends T> m) {
		if (m == null) {
			return;
		}
		for (final Map.Entry<? extends String, ? extends T> e : m.entrySet()) {
			put(e.getKey(), e.getValue());
		}
	}

	@Override
	public void clear() {
		if (caseInsensitiveKeys != null) {
			caseInsensitiveKeys.clear();
		}
		kv.clear();
	}

	@Override
	public Set<Map.Entry<String, T>> entrySet() {
		return kv.entrySet();
	}

	@Override
	public Set<String> keySet() {
		return kv.keySet();
	}

	@Override
	public Collection<T> values() {
		return kv.values();
	}

	@Override
	public int size() {
		return kv.size();
	}

	@Override
	public boolean isEmpty() {
		return kv.isEmpty();
	}

	@Override
	public boolean containsKey(final Object key) {
		return kv.containsKey(key(key));
	}

	@Override
	public boolean containsValue(final Object value) {
		return kv.containsValue(value);
	}

	@SuppressWarnings("rawtypes")
	public static Map NULL_MAP = Collections.EMPTY_MAP;
}
