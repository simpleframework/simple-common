/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000-2011 INRIA, France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.simpleframework.lib.org.objectweb.asm.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.simpleframework.lib.org.objectweb.asm.Label;
import net.simpleframework.lib.org.objectweb.asm.MethodVisitor;
import net.simpleframework.lib.org.objectweb.asm.Opcodes;
import net.simpleframework.lib.org.objectweb.asm.TypePath;
import net.simpleframework.lib.org.objectweb.asm.TypeReference;

/**
 * A node that represents a type annotation on a local or resource variable.
 * 
 * @author Eric Bruneton
 */
public class LocalVariableAnnotationNode extends TypeAnnotationNode {

	/**
	 * The fist instructions corresponding to the continuous ranges that make
	 * the scope of this local variable (inclusive). Must not be <tt>null</tt>.
	 */
	public List<LabelNode> start;

	/**
	 * The last instructions corresponding to the continuous ranges that make
	 * the scope of this local variable (exclusive). This list must have the
	 * same size as the 'start' list. Must not be <tt>null</tt>.
	 */
	public List<LabelNode> end;

	/**
	 * The local variable's index in each range. This list must have the same
	 * size as the 'start' list. Must not be <tt>null</tt>.
	 */
	public List<Integer> index;

	/**
	 * Constructs a new {@link LocalVariableAnnotationNode}. <i>Subclasses must
	 * not use this constructor</i>. Instead, they must use the
	 * {@link #LocalVariableAnnotationNode(int, TypePath, LabelNode[], LabelNode[], int[], String)}
	 * version.
	 * 
	 * @param typeRef
	 *        a reference to the annotated type. See {@link TypeReference}.
	 * @param typePath
	 *        the path to the annotated type argument, wildcard bound, array
	 *        element type, or static inner type within 'typeRef'. May be
	 *        <tt>null</tt> if the annotation targets 'typeRef' as a whole.
	 * @param start
	 *        the fist instructions corresponding to the continuous ranges
	 *        that make the scope of this local variable (inclusive).
	 * @param end
	 *        the last instructions corresponding to the continuous ranges
	 *        that make the scope of this local variable (exclusive). This
	 *        array must have the same size as the 'start' array.
	 * @param index
	 *        the local variable's index in each range. This array must have
	 *        the same size as the 'start' array.
	 * @param desc
	 *        the class descriptor of the annotation class.
	 */
	public LocalVariableAnnotationNode(final int typeRef, final TypePath typePath,
			final LabelNode[] start, final LabelNode[] end, final int[] index, final String desc) {
		this(Opcodes.ASM5, typeRef, typePath, start, end, index, desc);
	}

	/**
	 * Constructs a new {@link LocalVariableAnnotationNode}.
	 * 
	 * @param api
	 *        the ASM API version implemented by this visitor. Must be one
	 *        of {@link Opcodes#ASM4} or {@link Opcodes#ASM5}.
	 * @param typeRef
	 *        a reference to the annotated type. See {@link TypeReference}.
	 * @param start
	 *        the fist instructions corresponding to the continuous ranges
	 *        that make the scope of this local variable (inclusive).
	 * @param end
	 *        the last instructions corresponding to the continuous ranges
	 *        that make the scope of this local variable (exclusive). This
	 *        array must have the same size as the 'start' array.
	 * @param index
	 *        the local variable's index in each range. This array must have
	 *        the same size as the 'start' array.
	 * @param typePath
	 *        the path to the annotated type argument, wildcard bound, array
	 *        element type, or static inner type within 'typeRef'. May be
	 *        <tt>null</tt> if the annotation targets 'typeRef' as a whole.
	 * @param desc
	 *        the class descriptor of the annotation class.
	 */
	public LocalVariableAnnotationNode(final int api, final int typeRef, final TypePath typePath,
			final LabelNode[] start, final LabelNode[] end, final int[] index, final String desc) {
		super(api, typeRef, typePath, desc);
		this.start = new ArrayList<LabelNode>(start.length);
		this.start.addAll(Arrays.asList(start));
		this.end = new ArrayList<LabelNode>(end.length);
		this.end.addAll(Arrays.asList(end));
		this.index = new ArrayList<Integer>(index.length);
		for (final int i : index) {
			this.index.add(i);
		}
	}

	/**
	 * Makes the given visitor visit this type annotation.
	 * 
	 * @param mv
	 *        the visitor that must visit this annotation.
	 * @param visible
	 *        <tt>true</tt> if the annotation is visible at runtime.
	 */
	public void accept(final MethodVisitor mv, final boolean visible) {
		final Label[] start = new Label[this.start.size()];
		final Label[] end = new Label[this.end.size()];
		final int[] index = new int[this.index.size()];
		for (int i = 0; i < start.length; ++i) {
			start[i] = this.start.get(i).getLabel();
			end[i] = this.end.get(i).getLabel();
			index[i] = this.index.get(i);
		}
		accept(mv.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, desc, true));
	}
}
