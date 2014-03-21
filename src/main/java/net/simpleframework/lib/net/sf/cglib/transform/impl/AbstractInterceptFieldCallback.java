/*
 * Copyright 2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.simpleframework.lib.net.sf.cglib.transform.impl;

/**
 * @author Chris Nokleberg
 */
public class AbstractInterceptFieldCallback implements InterceptFieldCallback {

	@Override
	public int writeInt(final Object obj, final String name, final int oldValue, final int newValue) {
		return newValue;
	}

	@Override
	public char writeChar(final Object obj, final String name, final char oldValue,
			final char newValue) {
		return newValue;
	}

	@Override
	public byte writeByte(final Object obj, final String name, final byte oldValue,
			final byte newValue) {
		return newValue;
	}

	@Override
	public boolean writeBoolean(final Object obj, final String name, final boolean oldValue,
			final boolean newValue) {
		return newValue;
	}

	@Override
	public short writeShort(final Object obj, final String name, final short oldValue,
			final short newValue) {
		return newValue;
	}

	@Override
	public float writeFloat(final Object obj, final String name, final float oldValue,
			final float newValue) {
		return newValue;
	}

	@Override
	public double writeDouble(final Object obj, final String name, final double oldValue,
			final double newValue) {
		return newValue;
	}

	@Override
	public long writeLong(final Object obj, final String name, final long oldValue,
			final long newValue) {
		return newValue;
	}

	@Override
	public Object writeObject(final Object obj, final String name, final Object oldValue,
			final Object newValue) {
		return newValue;
	}

	@Override
	public int readInt(final Object obj, final String name, final int oldValue) {
		return oldValue;
	}

	@Override
	public char readChar(final Object obj, final String name, final char oldValue) {
		return oldValue;
	}

	@Override
	public byte readByte(final Object obj, final String name, final byte oldValue) {
		return oldValue;
	}

	@Override
	public boolean readBoolean(final Object obj, final String name, final boolean oldValue) {
		return oldValue;
	}

	@Override
	public short readShort(final Object obj, final String name, final short oldValue) {
		return oldValue;
	}

	@Override
	public float readFloat(final Object obj, final String name, final float oldValue) {
		return oldValue;
	}

	@Override
	public double readDouble(final Object obj, final String name, final double oldValue) {
		return oldValue;
	}

	@Override
	public long readLong(final Object obj, final String name, final long oldValue) {
		return oldValue;
	}

	@Override
	public Object readObject(final Object obj, final String name, final Object oldValue) {
		return oldValue;
	}
}
