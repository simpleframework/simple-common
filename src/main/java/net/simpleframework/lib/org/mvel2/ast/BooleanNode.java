package net.simpleframework.lib.org.mvel2.ast;

import net.simpleframework.lib.org.mvel2.ParserContext;

public abstract class BooleanNode extends ASTNode {
	protected ASTNode left;
	protected ASTNode right;

	protected BooleanNode(final ParserContext pCtx) {
		super(pCtx);
	}

	public ASTNode getLeft() {
		return this.left;
	}

	public ASTNode getRight() {
		return this.right;
	}

	public void setLeft(final ASTNode node) {
		this.left = node;
	}

	public void setRight(final ASTNode node) {
		this.right = node;
	}

	public abstract void setRightMost(ASTNode right);

	public abstract ASTNode getRightMost();
}
