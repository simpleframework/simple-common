package net.simpleframework.lib.net.minidev.asm;

public class DefaultConverter {

	public static int convertToint(final Object obj) {
		if (obj == null) {
			return 0;
		}
		if (obj instanceof Number) {
			return ((Number) obj).intValue();
		}
		if (obj instanceof String) {
			return Integer.parseInt((String) obj);
		}
		throw new RuntimeException("Primitive: Can not convert " + obj.getClass().getName()
				+ " to int");
	}

	public static Integer convertToInt(final Object obj) {
		if (obj == null) {
			return null;
		}
		final Class<?> c = obj.getClass();
		if (c == Integer.class) {
			return (Integer) obj;
		}
		if (obj instanceof Number) {
			return Integer.valueOf(((Number) obj).intValue());
		}
		throw new RuntimeException("Primitive: Can not convert " + obj.getClass().getName()
				+ " to Integer");
	}

	public static short convertToshort(final Object obj) {
		if (obj == null) {
			return 0;
		}
		if (obj instanceof Number) {
			return ((Number) obj).shortValue();
		}
		if (obj instanceof String) {
			return Short.parseShort((String) obj);
		}
		throw new RuntimeException("Primitive: Can not convert " + obj.getClass().getName()
				+ " to short");
	}

	public static Short convertToShort(final Object obj) {
		if (obj == null) {
			return null;
		}
		final Class<?> c = obj.getClass();
		if (c == Short.class) {
			return (Short) obj;
		}
		if (obj instanceof Number) {
			return Short.valueOf(((Number) obj).shortValue());
		}
		throw new RuntimeException("Primitive: Can not convert " + obj.getClass().getName()
				+ " to Short");
	}

	public static long convertTolong(final Object obj) {
		if (obj == null) {
			return 0;
		}
		if (obj instanceof Number) {
			return ((Number) obj).longValue();
		}
		if (obj instanceof String) {
			return Long.parseLong((String) obj);
		}
		throw new RuntimeException("Primitive: Can not convert " + obj.getClass().getName()
				+ " to long");
	}

	public static Long convertToLong(final Object obj) {
		if (obj == null) {
			return null;
		}
		final Class<?> c = obj.getClass();
		if (c == Long.class) {
			return (Long) obj;
		}
		if (obj instanceof Number) {
			return Long.valueOf(((Number) obj).longValue());
		}
		throw new RuntimeException("Primitive: Can not convert " + obj.getClass().getName()
				+ " to Long");
	}

	public static byte convertTobyte(final Object obj) {
		if (obj == null) {
			return 0;
		}
		if (obj instanceof Number) {
			return ((Number) obj).byteValue();
		}
		if (obj instanceof String) {
			return Byte.parseByte((String) obj);
		}
		throw new RuntimeException("Primitive: Can not convert " + obj.getClass().getName()
				+ " to byte");
	}

	public static Byte convertToByte(final Object obj) {
		if (obj == null) {
			return null;
		}
		final Class<?> c = obj.getClass();
		if (c == Byte.class) {
			return (Byte) obj;
		}
		if (obj instanceof Number) {
			return Byte.valueOf(((Number) obj).byteValue());
		}
		throw new RuntimeException("Primitive: Can not convert " + obj.getClass().getName()
				+ " to Byte");
	}

	public static float convertTofloat(final Object obj) {
		if (obj == null) {
			return 0f;
		}
		if (obj instanceof Number) {
			return ((Number) obj).floatValue();
		}
		if (obj instanceof String) {
			return Float.parseFloat((String) obj);
		}
		throw new RuntimeException("Primitive: Can not convert " + obj.getClass().getName()
				+ " to float");
	}

	public static Float convertToFloat(final Object obj) {
		if (obj == null) {
			return null;
		}
		final Class<?> c = obj.getClass();
		if (c == Float.class) {
			return (Float) obj;
		}
		if (obj instanceof Number) {
			return Float.valueOf(((Number) obj).floatValue());
		}
		throw new RuntimeException("Primitive: Can not convert " + obj.getClass().getName()
				+ " to Float");
	}

	public static double convertTodouble(final Object obj) {
		if (obj == null) {
			return 0.0;
		}
		if (obj instanceof Number) {
			return ((Number) obj).doubleValue();
		}
		if (obj instanceof String) {
			return Double.parseDouble((String) obj);
		}
		throw new RuntimeException("Primitive: Can not convert " + obj.getClass().getName()
				+ " to float");
	}

	public static Double convertToDouble(final Object obj) {
		if (obj == null) {
			return null;
		}
		final Class<?> c = obj.getClass();
		if (c == Double.class) {
			return (Double) obj;
		}
		if (obj instanceof Number) {
			return Double.valueOf(((Number) obj).doubleValue());
		}
		throw new RuntimeException("Primitive: Can not convert " + obj.getClass().getName()
				+ " to Float");
	}

	public static char convertTochar(final Object obj) {
		if (obj == null) {
			return ' ';
		}
		if (obj instanceof String) {
			if (((String) obj).length() > 0) {
				return ((String) obj).charAt(0);
			} else {
				return ' ';
			}
		}
		throw new RuntimeException("Primitive: Can not convert " + obj.getClass().getName()
				+ " to char");
	}

	public static Character convertToChar(final Object obj) {
		if (obj == null) {
			return null;
		}
		final Class<?> c = obj.getClass();
		if (c == Character.class) {
			return (Character) obj;
		}
		if (obj instanceof String) {
			if (((String) obj).length() > 0) {
				return ((String) obj).charAt(0);
			} else {
				return ' ';
			}
		}
		throw new RuntimeException("Primitive: Can not convert " + obj.getClass().getName()
				+ " to Character");
	}

	public static boolean convertTobool(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj.getClass() == Boolean.class) {
			return ((Boolean) obj).booleanValue();
		}
		if (obj instanceof String) {
			return Boolean.parseBoolean((String) obj);
		}
		throw new RuntimeException("Primitive: Can not convert " + obj.getClass().getName()
				+ " to boolean");
	}

	public static Boolean convertToBool(final Object obj) {
		if (obj == null) {
			return null;
		}
		final Class<?> c = obj.getClass();
		if (c == Boolean.class) {
			return (Boolean) obj;
		}
		if (obj instanceof String) {
			return Boolean.parseBoolean((String) obj);
		}
		throw new RuntimeException("Primitive: Can not convert " + obj.getClass().getName()
				+ " to Boolean");
	}
}
