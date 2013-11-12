package net.simpleframework.lib.org.mvel2.ast;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.simpleframework.lib.org.mvel2.CompileException;
import net.simpleframework.lib.org.mvel2.MVEL;
import net.simpleframework.lib.org.mvel2.Operator;
import net.simpleframework.lib.org.mvel2.ParserContext;
import net.simpleframework.lib.org.mvel2.integration.VariableResolver;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;
import net.simpleframework.lib.org.mvel2.util.ExecutionStack;
import net.simpleframework.lib.org.mvel2.util.ParseTools;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class Stacklang extends BlockNode {
	List<Instruction> instructionList;
	ParserContext pCtx;

	public Stacklang(final char[] expr, final int blockStart, final int blockOffset,
			final int fields, final ParserContext pCtx) {
		super(pCtx);
		this.expr = expr;
		this.blockStart = blockStart;
		this.blockOffset = blockOffset;
		this.fields = fields | ASTNode.STACKLANG;

		final String[] instructions = new String(expr, blockStart, blockOffset).split(";");

		instructionList = new ArrayList<Instruction>(instructions.length);
		for (final String s : instructions) {
			instructionList.add(parseInstruction(s.trim()));
		}

		this.pCtx = pCtx;
	}

	@Override
	public Object getReducedValueAccelerated(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		final ExecutionStack stk = new ExecutionStack();
		stk.push(getReducedValue(stk, thisValue, factory));
		if (stk.isReduceable()) {
			while (true) {
				stk.op();
				if (stk.isReduceable()) {
					stk.xswap();
				} else {
					break;
				}
			}
		}
		return stk.peek();
	}

	@Override
	public Object getReducedValue(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		final ExecutionStack stack = (ExecutionStack) ctx;

		for (int i1 = 0, instructionListSize = instructionList.size(); i1 < instructionListSize; i1++) {
			final Instruction instruction = instructionList.get(i1);
			switch (instruction.opcode) {
			case Operator.STORE:
				if (instruction.cache == null) {
					instruction.cache = factory.createVariable(instruction.expr, stack.peek());
				} else {
					((VariableResolver) instruction.cache).setValue(stack.peek());
				}
				break;
			case Operator.LOAD:
				if (instruction.cache == null) {
					instruction.cache = factory.getVariableResolver(instruction.expr);
				}
				stack.push(((VariableResolver) instruction.cache).getValue());
				break;
			case Operator.GETFIELD:
				try {
					if (stack.isEmpty() || !(stack.peek() instanceof Class)) {
						throw new CompileException("getfield without class", expr, blockStart);
					}

					Field field;
					if (instruction.cache == null) {
						instruction.cache = field = ((Class) stack.pop()).getField(instruction.expr);
					} else {
						stack.discard();
						field = (Field) instruction.cache;
					}

					stack.push(field.get(stack.pop()));
				} catch (final Exception e) {
					throw new CompileException("field access error", expr, blockStart, e);
				}
				break;
			case Operator.STOREFIELD:
				try {
					if (stack.isEmpty() || !(stack.peek() instanceof Class)) {
						throw new CompileException("storefield without class", expr, blockStart);
					}

					final Class cls = (Class) stack.pop();
					final Object val = stack.pop();
					cls.getField(instruction.expr).set(stack.pop(), val);
					stack.push(val);
				} catch (final Exception e) {
					throw new CompileException("field access error", expr, blockStart, e);
				}
				break;

			case Operator.LDTYPE:
				try {
					if (instruction.cache == null) {
						instruction.cache = ParseTools.createClass(instruction.expr, pCtx);
					}
					stack.push(instruction.cache);
				} catch (final ClassNotFoundException e) {
					throw new CompileException("error", expr, blockStart, e);
				}
				break;

			case Operator.INVOKE:
				Object[] parms;
				final ExecutionStack call = new ExecutionStack();
				while (!stack.isEmpty() && !(stack.peek() instanceof Class)) {
					call.push(stack.pop());
				}
				if (stack.isEmpty()) {
					throw new CompileException("invoke without class", expr, blockStart);
				}

				parms = new Object[call.size()];
				for (int i = 0; !call.isEmpty(); i++) {
					parms[i] = call.pop();
				}

				if ("<init>".equals(instruction.expr)) {
					Constructor c;
					if (instruction.cache == null) {
						instruction.cache = c = ParseTools.getBestConstructorCandidate(parms,
								(Class) stack.pop(), false);
					} else {
						c = (Constructor) instruction.cache;
					}

					try {
						stack.push(c.newInstance(parms));
					} catch (final Exception e) {
						throw new CompileException("instantiation error", expr, blockStart, e);
					}
				} else {
					Method m;
					if (instruction.cache == null) {
						final Class cls = (Class) stack.pop();

						instruction.cache = m = ParseTools.getBestCandidate(parms, instruction.expr, cls,
								cls.getDeclaredMethods(), false);
					} else {
						stack.discard();
						m = (Method) instruction.cache;
					}

					try {
						stack.push(m.invoke(stack.isEmpty() ? null : stack.pop(), parms));
					} catch (final Exception e) {
						throw new CompileException("invokation error", expr, blockStart, e);
					}
				}
				break;
			case Operator.PUSH:
				if (instruction.cache == null) {
					instruction.cache = MVEL.eval(instruction.expr, ctx, factory);
				}
				stack.push(instruction.cache);
				break;
			case Operator.POP:
				stack.pop();
				break;
			case Operator.DUP:
				stack.dup();
				break;
			case Operator.LABEL:
				break;
			case Operator.JUMPIF:
				if (!stack.popBoolean()) {
					continue;
				}

			case Operator.JUMP:
				if (instruction.cache != null) {
					i1 = (Integer) instruction.cache;
				} else {
					for (int i2 = 0; i2 < instructionList.size(); i2++) {
						final Instruction ins = instructionList.get(i2);
						if (ins.opcode == Operator.LABEL && instruction.expr.equals(ins.expr)) {
							instruction.cache = i1 = i2;
							break;
						}
					}
				}
				break;
			case Operator.EQUAL:
				stack.push(stack.pop().equals(stack.pop()));
				break;
			case Operator.NEQUAL:
				stack.push(!stack.pop().equals(stack.pop()));
				break;

			case Operator.REDUCE:
				stack.op();
				break;
			}
		}

		return stack.pop();
	}

	private static class Instruction {
		int opcode;
		String expr;
		Object cache;
	}

	private static Instruction parseInstruction(final String s) {
		final int split = s.indexOf(' ');

		final Instruction instruction = new Instruction();

		final String keyword = split == -1 ? s : s.substring(0, split);

		if (opcodes.containsKey(keyword)) {
			instruction.opcode = opcodes.get(keyword);
		}

		// noinspection StringEquality
		if (keyword != s) {
			instruction.expr = s.substring(split + 1);
		}

		return instruction;
	}

	static final Map<String, Integer> opcodes = new HashMap<String, Integer>();

	static {
		opcodes.put("push", Operator.PUSH);
		opcodes.put("pop", Operator.POP);
		opcodes.put("load", Operator.LOAD);
		opcodes.put("ldtype", Operator.LDTYPE);
		opcodes.put("invoke", Operator.INVOKE);
		opcodes.put("store", Operator.STORE);
		opcodes.put("getfield", Operator.GETFIELD);
		opcodes.put("storefield", Operator.STOREFIELD);
		opcodes.put("dup", Operator.DUP);
		opcodes.put("jump", Operator.JUMP);
		opcodes.put("jumpif", Operator.JUMPIF);
		opcodes.put("label", Operator.LABEL);
		opcodes.put("eq", Operator.EQUAL);
		opcodes.put("ne", Operator.NEQUAL);
		opcodes.put("reduce", Operator.REDUCE);
	}
}
