package net.simpleframework.lib.org.mvel2.conversion;

import net.simpleframework.lib.org.mvel2.ConversionHandler;

public class CompositeCH implements ConversionHandler {

	private final ConversionHandler[] converters;

	public CompositeCH(final ConversionHandler... converters) {
		this.converters = converters;
	}

	@Override
	public Object convertFrom(final Object in) {
		for (final ConversionHandler converter : converters) {
			if (converter.canConvertFrom(in.getClass())) {
				return converter.convertFrom(in);
			}
		}
		return null;
	}

	@Override
	public boolean canConvertFrom(final Class cls) {
		for (final ConversionHandler converter : converters) {
			if (converter.canConvertFrom(cls)) {
				return true;
			}
		}
		return false;
	}
}
