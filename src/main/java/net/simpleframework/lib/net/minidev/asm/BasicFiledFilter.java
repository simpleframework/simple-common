package net.simpleframework.lib.net.minidev.asm;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class BasicFiledFilter implements FieldFilter {
	public final static BasicFiledFilter SINGLETON = new BasicFiledFilter();

	@Override
	public boolean canUse(final Field field) {
		return true;
	}

	@Override
	public boolean canUse(final Field field, final Method method) {
		return true;
	}

	@Override
	public boolean canRead(final Field field) {
		return true;
	}

	@Override
	public boolean canWrite(final Field field) {
		return true;
	}

}
