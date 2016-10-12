package net.simpleframework.lib.net.minidev.json.writer;

import java.io.IOException;
import java.lang.reflect.Type;

import net.simpleframework.lib.net.minidev.json.parser.ParseException;

public class UpdaterMapper<T> extends JsonReaderI<T> {
	final T obj;
	final JsonReaderI<?> mapper;

	public UpdaterMapper(final JsonReader base, final T obj) {
		super(base);
		if (obj == null) {
			throw new NullPointerException("can not update null Object");
		}
		this.obj = obj;
		this.mapper = base.getMapper(obj.getClass());
	}

	public UpdaterMapper(final JsonReader base, final T obj, final Type type) {
		super(base);
		if (obj == null) {
			throw new NullPointerException("can not update null Object");
		}
		this.obj = obj;
		this.mapper = base.getMapper(type);
	}

	/**
	 * called when json-smart parser meet an object key
	 */
	@Override
	public JsonReaderI<?> startObject(final String key) throws ParseException, IOException {
		final Object bean = mapper.getValue(obj, key);
		if (bean == null) {
			return mapper.startObject(key);
		}
		return new UpdaterMapper<Object>(base, bean, mapper.getType(key));
	}

	/**
	 * called when json-smart parser start an array.
	 * 
	 * @param key
	 *        the destination key name, or null.
	 */
	@Override
	public JsonReaderI<?> startArray(final String key) throws ParseException, IOException {
		// if (obj != null)
		return mapper.startArray(key);
	}

	/**
	 * called when json-smart done parsing a value
	 */
	@Override
	public void setValue(final Object current, final String key, final Object value)
			throws ParseException, IOException {
		// if (obj != null)
		mapper.setValue(current, key, value);
	}

	/**
	 * add a value in an array json object.
	 */
	@Override
	public void addValue(final Object current, final Object value)
			throws ParseException, IOException {
		// if (obj != null)
		mapper.addValue(current, value);
	}

	/**
	 * use to instantiate a new object that will be used as an object
	 */
	@Override
	public Object createObject() {
		if (obj != null) {
			return obj;
		}
		return mapper.createObject();
	}

	/**
	 * use to instantiate a new object that will be used as an array
	 */
	@Override
	public Object createArray() {
		if (obj != null) {
			return obj;
		}
		return mapper.createArray();
	}

	/**
	 * Allow a mapper to converte a temprary structure to the final data format.
	 * 
	 * example: convert an List<Integer> to an int[]
	 */
	@Override
	@SuppressWarnings("unchecked")
	public T convert(final Object current) {
		if (obj != null) {
			return obj;
		}
		return (T) mapper.convert(current);
	}
}
