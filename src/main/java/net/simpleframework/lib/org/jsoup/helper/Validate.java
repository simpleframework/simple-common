package net.simpleframework.lib.org.jsoup.helper;

/**
 * Simple validation methods. Designed for jsoup internal use
 */
public final class Validate {

	private Validate() {
	}

	/**
	 * Validates that the object is not null
	 * 
	 * @param obj
	 *           object to test
	 */
	public static void notNull(final Object obj) {
		if (obj == null) {
			throw new IllegalArgumentException("Object must not be null");
		}
	}

	/**
	 * Validates that the object is not null
	 * 
	 * @param obj
	 *           object to test
	 * @param msg
	 *           message to output if validation fails
	 */
	public static void notNull(final Object obj, final String msg) {
		if (obj == null) {
			throw new IllegalArgumentException(msg);
		}
	}

	/**
	 * Validates that the value is true
	 * 
	 * @param val
	 *           object to test
	 */
	public static void isTrue(final boolean val) {
		if (!val) {
			throw new IllegalArgumentException("Must be true");
		}
	}

	/**
	 * Validates that the value is true
	 * 
	 * @param val
	 *           object to test
	 * @param msg
	 *           message to output if validation fails
	 */
	public static void isTrue(final boolean val, final String msg) {
		if (!val) {
			throw new IllegalArgumentException(msg);
		}
	}

	/**
	 * Validates that the value is false
	 * 
	 * @param val
	 *           object to test
	 */
	public static void isFalse(final boolean val) {
		if (val) {
			throw new IllegalArgumentException("Must be false");
		}
	}

	/**
	 * Validates that the value is false
	 * 
	 * @param val
	 *           object to test
	 * @param msg
	 *           message to output if validation fails
	 */
	public static void isFalse(final boolean val, final String msg) {
		if (val) {
			throw new IllegalArgumentException(msg);
		}
	}

	/**
	 * Validates that the array contains no null elements
	 * 
	 * @param objects
	 *           the array to test
	 */
	public static void noNullElements(final Object[] objects) {
		noNullElements(objects, "Array must not contain any null objects");
	}

	/**
	 * Validates that the array contains no null elements
	 * 
	 * @param objects
	 *           the array to test
	 * @param msg
	 *           message to output if validation fails
	 */
	public static void noNullElements(final Object[] objects, final String msg) {
		for (final Object obj : objects) {
			if (obj == null) {
				throw new IllegalArgumentException(msg);
			}
		}
	}

	/**
	 * Validates that the string is not empty
	 * 
	 * @param string
	 *           the string to test
	 */
	public static void notEmpty(final String string) {
		if (string == null || string.length() == 0) {
			throw new IllegalArgumentException("String must not be empty");
		}
	}

	/**
	 * Validates that the string is not empty
	 * 
	 * @param string
	 *           the string to test
	 * @param msg
	 *           message to output if validation fails
	 */
	public static void notEmpty(final String string, final String msg) {
		if (string == null || string.length() == 0) {
			throw new IllegalArgumentException(msg);
		}
	}

	/**
	 * Cause a failure.
	 * 
	 * @param msg
	 *           message to output.
	 */
	public static void fail(final String msg) {
		throw new IllegalArgumentException(msg);
	}
}
