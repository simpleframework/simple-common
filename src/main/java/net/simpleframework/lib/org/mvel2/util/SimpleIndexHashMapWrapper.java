package net.simpleframework.lib.org.mvel2.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * As most use-cases of the VariableResolverFactory's rely on Maps, this is
 * meant to implement a simple wrapper
 * which records index positions for use by the optimizing facilities.
 * <p/>
 * This wrapper also ensures that the Map is only additive. You cannot remove an
 * element once it's been added. While this may seem like an odd limitation, it
 * is consistent with the language semantics. (ie. it's not possible to delete a
 * variable at runtime once it's been declared).
 *
 * @author Mike Brock
 */
public class SimpleIndexHashMapWrapper<K, V> implements Map<K, V> {
	private int indexCounter;
	private final Map<K, ValueContainer<K, V>> wrappedMap;
	private final ArrayList<ValueContainer<K, V>> indexBasedLookup;

	public SimpleIndexHashMapWrapper() {
		this.wrappedMap = new HashMap<>();
		this.indexBasedLookup = new ArrayList<>();
	}

	public SimpleIndexHashMapWrapper(final SimpleIndexHashMapWrapper<K, V> wrapper,
			final boolean allocateOnly) {
		this.indexBasedLookup = new ArrayList<>(wrapper.indexBasedLookup.size());
		this.wrappedMap = new HashMap<>();

		ValueContainer<K, V> vc;
		int index = 0;
		if (allocateOnly) {
			for (final ValueContainer<K, V> key : wrapper.indexBasedLookup) {
				vc = new ValueContainer<>(index++, key.getKey(), null);
				indexBasedLookup.add(vc);
				wrappedMap.put(key.getKey(), vc);
			}
		} else {
			for (final ValueContainer<K, V> key : wrapper.indexBasedLookup) {
				vc = new ValueContainer<>(index++, key.getKey(), key.getValue());
				indexBasedLookup.add(vc);
				wrappedMap.put(key.getKey(), vc);
			}
		}
	}

	public SimpleIndexHashMapWrapper(final K[] keys) {
		this.wrappedMap = new HashMap<>(keys.length * 2);
		this.indexBasedLookup = new ArrayList<>(keys.length);

		initWithKeys(keys);
	}

	public SimpleIndexHashMapWrapper(final K[] keys, final int initialCapacity, final float load) {
		this.wrappedMap = new HashMap<>(initialCapacity * 2, load);
		this.indexBasedLookup = new ArrayList<>(initialCapacity);

		initWithKeys(keys);
	}

	public void initWithKeys(final K[] keys) {
		int index = 0;
		ValueContainer<K, V> vc;
		for (final K key : keys) {
			vc = new ValueContainer<>(index++, key, null);
			wrappedMap.put(key, vc);
			indexBasedLookup.add(vc);
		}
	}

	public void addKey(final K key) {
		final ValueContainer<K, V> vc = new ValueContainer<>(indexCounter++, key, null);
		this.indexBasedLookup.add(vc);
		this.wrappedMap.put(key, vc);
	}

	public void addKey(final K key, final V value) {
		final ValueContainer<K, V> vc = new ValueContainer<>(indexCounter++, key, value);
		this.indexBasedLookup.add(vc);
		this.wrappedMap.put(key, vc);
	}

	@Override
	public int size() {
		return wrappedMap.size();
	}

	@Override
	public boolean isEmpty() {
		return wrappedMap.isEmpty();
	}

	@Override
	public boolean containsKey(final Object key) {
		return wrappedMap.containsKey(key);
	}

	@Override
	public boolean containsValue(final Object value) {
		return wrappedMap.containsValue(value);
	}

	@Override
	public V get(final Object key) {
		return wrappedMap.get(key).getValue();
	}

	public V getByIndex(final int index) {
		return indexBasedLookup.get(index).getValue();
	}

	public K getKeyAtIndex(final int index) {
		return indexBasedLookup.get(index).getKey();
	}

	public int indexOf(final K key) {
		return wrappedMap.get(key).getIndex();
	}

	@Override
	public V put(final K key, final V value) {
		final ValueContainer<K, V> vc = wrappedMap.get(key);
		if (vc == null) {
			throw new RuntimeException(
					"cannot add a new entry.  you must allocate a new key with addKey() first.");
		}

		indexBasedLookup.add(vc);
		return wrappedMap.put(key, vc).getValue();
	}

	public void putAtIndex(final int index, final V value) {
		final ValueContainer<K, V> vc = indexBasedLookup.get(index);
		vc.setValue(value);
	}

	@Override
	public V remove(final Object key) {
		throw new UnsupportedOperationException("cannot remove keys");
	}

	@Override
	public void putAll(final Map<? extends K, ? extends V> m) {
		// wrappedMap.put
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("cannot clear map");
	}

	@Override
	public Set<K> keySet() {
		return wrappedMap.keySet();
	}

	@Override
	public Collection<V> values() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		throw new UnsupportedOperationException();
	}

	private class ValueContainer<K, V> {
		private final int index;
		private K key;
		private V value;

		public ValueContainer(final int index, final K key, final V value) {
			this.index = index;
			this.key = key;
			this.value = value;
		}

		public int getIndex() {
			return index;
		}

		public K getKey() {
			return key;
		}

		public V getValue() {
			return value;
		}

		void setKey(final K key) {
			this.key = key;
		}

		void setValue(final V value) {
			this.value = value;
		}
	}
}
