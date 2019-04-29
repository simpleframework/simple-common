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

package net.simpleframework.lib.org.mvel2.util;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import net.simpleframework.lib.org.mvel2.ImmutableElementException;

public class FastList<E> extends AbstractList<E> implements Externalizable {
	private E[] elements;
	private int size = 0;
	private boolean updated = false;

	public FastList(final int size) {
		elements = (E[]) new Object[size == 0 ? 1 : size];
	}

	public FastList(final E[] elements) {
		this.size = (this.elements = elements).length;
	}

	public FastList() {
		this(10);
	}

	@Override
	public void writeExternal(final ObjectOutput out) throws IOException {
		out.writeInt(size);
		for (int i = 0; i < size; i++) {
			out.writeObject(elements[i]);
		}
	}

	@Override
	public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
		elements = (E[]) new Object[size = in.readInt()];
		for (int i = 0; i < size; i++) {
			elements[i] = (E) in.readObject();
		}
	}

	@Override
	public E get(final int index) {
		return elements[index];
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean add(final E o) {
		if (size == elements.length) {
			increaseSize(elements.length * 2);
		}

		elements[size++] = o;
		return true;
	}

	@Override
	public E set(final int i, final E o) {
		if (!updated) {
			copyArray();
		}
		final E old = elements[i];
		elements[i] = o;
		return old;
	}

	@Override
	public void add(final int i, final E o) {
		if (size == elements.length) {
			increaseSize(elements.length * 2);
		}

		for (int c = size; c != i; c--) {
			elements[c] = elements[c - 1];
		}
		elements[i] = o;
		size++;
	}

	@Override
	public E remove(final int i) {
		final E old = elements[i];
		for (int c = i + 1; c < size; c++) {
			elements[c - 1] = elements[c];
			elements[c] = null;
		}
		size--;
		return old;
	}

	@Override
	public int indexOf(final Object o) {
		if (o == null) {
			return -1;
		}
		for (int i = 0; i < elements.length; i++) {
			if (o.equals(elements[i])) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public int lastIndexOf(final Object o) {
		if (o == null) {
			return -1;
		}
		for (int i = elements.length - 1; i != -1; i--) {
			if (o.equals(elements[i])) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public void clear() {
		elements = (E[]) new Object[1];
		size = 0;
	}

	@Override
	public boolean addAll(final int i, final Collection<? extends E> collection) {
		final int offset = collection.size();
		ensureCapacity(offset + size);

		if (i != 0) {
			// copy forward all elements that the insertion is occuring before
			for (int c = i; c != (i + offset); c++) {
				elements[c + offset + 1] = elements[c];
			}
		}

		int c = size == 0 ? -1 : 0;
		for (final E o : collection) {
			elements[offset + c++] = o;
		}

		size += offset;

		return true;
	}

	@Override
	public Iterator iterator() {
		final int size = this.size;
		return new Iterator() {
			private int cursor = 0;

			@Override
			public boolean hasNext() {
				return cursor < size;
			}

			@Override
			public Object next() {
				return elements[cursor++];
			}

			@Override
			public void remove() {
				throw new ImmutableElementException("cannot change elements in immutable list");
			}
		};

	}

	@Override
	public ListIterator<E> listIterator() {
		return new ListIterator<E>() {
			private int i = -1;

			@Override
			public boolean hasNext() {
				return i < size - 1;
			}

			@Override
			public E next() {
				return elements[++i];
			}

			@Override
			public boolean hasPrevious() {
				return i > 0;
			}

			@Override
			public E previous() {
				return elements[--i];
			}

			@Override
			public int nextIndex() {
				return i++;
			}

			@Override
			public int previousIndex() {
				return i--;
			}

			@Override
			public void remove() {
				throw new java.lang.UnsupportedOperationException();
			}

			@Override
			public void set(final E o) {
				elements[i] = o;
			}

			@Override
			public void add(final Object o) {
				throw new java.lang.UnsupportedOperationException();
			}
		};
	}

	@Override
	public ListIterator listIterator(final int i) {
		return super.listIterator(i);
	}

	@Override
	public List subList(final int i, final int i1) {
		return super.subList(i, i1);
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof List)) {
			return false;
		}

		final ListIterator e1 = listIterator();
		final ListIterator e2 = ((List) o).listIterator();
		while (e1.hasNext() && e2.hasNext()) {
			final Object o1 = e1.next();
			final Object o2 = e2.next();
			if (!(o1 == null ? o2 == null : o1.equals(o2))) {
				return false;
			}
		}
		return !(e1.hasNext() || e2.hasNext());
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	protected void removeRange(final int i, final int i1) {
		throw new RuntimeException("not implemented");
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	@Override
	public boolean contains(final Object o) {
		return indexOf(o) != -1;
	}

	@Override
	public Object[] toArray() {
		return toArray(new Object[size]);
	}

	@Override
	public Object[] toArray(Object[] objects) {
		if (objects.length < size) {
			objects = new Object[size];
		}
		for (int i = 0; i < size; i++) {
			objects[i] = elements[i];
		}
		return objects;
	}

	@Override
	public boolean remove(final Object o) {
		throw new RuntimeException("not implemented");
	}

	@Override
	public boolean containsAll(final Collection collection) {
		throw new RuntimeException("not implemented");
	}

	@Override
	public boolean addAll(final Collection collection) {
		return addAll(size, collection);
	}

	@Override
	public boolean removeAll(final Collection collection) {
		throw new RuntimeException("not implemented");
	}

	@Override
	public boolean retainAll(final Collection collection) {
		throw new RuntimeException("not implemented");
	}

	private void ensureCapacity(final int additional) {
		if ((size + additional) > elements.length) {
			increaseSize((size + additional) * 2);
		}
	}

	private void copyArray() {
		increaseSize(elements.length);
	}

	private void increaseSize(final int newSize) {
		final E[] newElements = (E[]) new Object[newSize];
		for (int i = 0; i < elements.length; i++) {
			newElements[i] = elements[i];
		}

		elements = newElements;

		updated = true;
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
