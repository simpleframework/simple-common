package net.simpleframework.lib.net.sf.cglib.core;

import net.simpleframework.lib.org.objectweb.asm.Type;

public interface HashCodeCustomizer extends KeyFactoryCustomizer {
	/**
	 * Customizes calculation of hashcode
	 * 
	 * @param e
	 *        code emitter
	 * @param type
	 *        parameter type
	 */
	boolean customize(CodeEmitter e, Type type);
}
