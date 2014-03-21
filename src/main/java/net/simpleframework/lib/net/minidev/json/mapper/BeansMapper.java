package net.simpleframework.lib.net.minidev.json.mapper;

/*
 *    Copyright 2011 JSON-SMART authors
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

import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;

import net.simpleframework.lib.net.minidev.asm.Accessor;
import net.simpleframework.lib.net.minidev.asm.BeansAccess;
import net.simpleframework.lib.net.minidev.asm.ConvertDate;
import net.simpleframework.lib.net.minidev.json.JSONUtil;

@SuppressWarnings("unchecked")
public abstract class BeansMapper<T> extends AMapper<T> {

	@Override
	public abstract Object getValue(Object current, String key);

	public static class Bean<T> extends AMapper<T> {
		final Class<T> clz;
		final BeansAccess<T> ba;
		final HashMap<String, Accessor> index;

		public Bean(final Class<T> clz) {
			this.clz = clz;
			this.ba = BeansAccess.get(clz, JSONUtil.JSON_SMART_FIELD_FILTER);
			this.index = ba.getMap();
		}

		@Override
		public void setValue(final Object current, final String key, final Object value) {
			ba.set((T) current, key, value);
			// Accessor nfo = index.get(key);
			// if (nfo == null)
			// throw new RuntimeException("Can not set " + key + " field in " +
			// clz);
			// value = JSONUtil.convertTo(value, nfo.getType());
			// ba.set((T) current, nfo.getIndex(), value);
		}

		@Override
		public Object getValue(final Object current, final String key) {
			return ba.get((T) current, key);
			// Accessor nfo = index.get(key);
			// if (nfo == null)
			// throw new RuntimeException("Can not set " + key + " field in " +
			// clz);
			// return ba.get((T) current, nfo.getIndex());
		}

		@Override
		public Type getType(final String key) {
			final Accessor nfo = index.get(key);
			return nfo.getGenericType();
		}

		@Override
		public AMapper<?> startArray(final String key) {
			final Accessor nfo = index.get(key);
			if (nfo == null) {
				throw new RuntimeException("Can not find '" + key + "' field in " + clz);
			}
			return Mapper.getMapper(nfo.getGenericType());
		}

		@Override
		public AMapper<?> startObject(final String key) {
			final Accessor f = index.get(key);
			if (f == null) {
				throw new RuntimeException("Can not find '" + key + "' field in " + clz);
			}
			return Mapper.getMapper(f.getGenericType());
		}

		@Override
		public Object createObject() {
			return ba.newInstance();
		}
	}

	public static class BeanNoConv<T> extends AMapper<T> {
		final Class<T> clz;
		final BeansAccess<T> ba;
		final HashMap<String, Accessor> index;

		public BeanNoConv(final Class<T> clz) {
			this.clz = clz;
			this.ba = BeansAccess.get(clz, JSONUtil.JSON_SMART_FIELD_FILTER);
			this.index = ba.getMap();
		}

		@Override
		public void setValue(final Object current, final String key, final Object value) {
			ba.set((T) current, key, value);
		}

		@Override
		public Object getValue(final Object current, final String key) {
			return ba.get((T) current, key);
		}

		@Override
		public Type getType(final String key) {
			final Accessor nfo = index.get(key);
			return nfo.getGenericType();
		}

		@Override
		public AMapper<?> startArray(final String key) {
			final Accessor nfo = index.get(key);
			if (nfo == null) {
				throw new RuntimeException("Can not set " + key + " field in " + clz);
			}
			return Mapper.getMapper(nfo.getGenericType());
		}

		@Override
		public AMapper<?> startObject(final String key) {
			final Accessor f = index.get(key);
			if (f == null) {
				throw new RuntimeException("Can not set " + key + " field in " + clz);
			}
			return Mapper.getMapper(f.getGenericType());
		}

		@Override
		public Object createObject() {
			return ba.newInstance();
		}
	}

	public static AMapper<Date> MAPPER_DATE = new ArraysMapper<Date>() {
		@Override
		public Date convert(final Object current) {
			return ConvertDate.convertToDate(current);
		}
	};

}
