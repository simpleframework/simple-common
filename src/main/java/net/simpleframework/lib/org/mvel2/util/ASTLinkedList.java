/**
 * MVEL 2.0
 * Copyright (C) 2007 The Codehaus
 * Mike Brock, Dhanji Prasanna, John Graham, Mark Proctor
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

package net.simpleframework.lib.org.mvel2.util;

import net.simpleframework.lib.org.mvel2.ast.ASTNode;

public class ASTLinkedList implements ASTIterator {
	private ASTNode firstASTNode;
	private ASTNode current;
	private ASTNode last;
	private int size;

	public ASTLinkedList() {
	}

	public ASTLinkedList(final ASTIterator iter) {
		this.current = this.firstASTNode = iter.firstNode();
	}

	public ASTLinkedList(final ASTNode firstASTNode) {
		this.current = this.firstASTNode = firstASTNode;
	}

	public ASTLinkedList(final ASTNode firstASTNode, final int size) {
		this.current = this.firstASTNode = firstASTNode;
		this.size = size;
	}

	@Override
	public void addTokenNode(final ASTNode astNode) {
		size++;

		if (this.firstASTNode == null) {
			this.firstASTNode = this.current = astNode;
		} else {
			this.last = this.current = (this.current.nextASTNode = astNode);
		}
	}

	@Override
	public void addTokenNode(final ASTNode astNode, final ASTNode token2) {
		size += 2;

		if (this.firstASTNode == null) {
			this.last = this.current = ((this.firstASTNode = astNode).nextASTNode = token2);
		} else {
			this.last = this.current = (this.current.nextASTNode = astNode).nextASTNode = token2;
		}
	}

	@Override
	public ASTNode firstNode() {
		return firstASTNode;
	}

	public boolean isSingleNode() {
		return size == 1 || (size == 2 && firstASTNode.fields == -1);
	}

	public ASTNode firstNonSymbol() {
		if (firstASTNode.fields == -1) {
			return firstASTNode.nextASTNode;
		} else {
			return firstASTNode;
		}
	}

	@Override
	public void reset() {
		this.current = firstASTNode;
	}

	@Override
	public boolean hasMoreNodes() {
		return this.current != null;
	}

	@Override
	public ASTNode nextNode() {
		if (current == null) {
			return null;
		}
		try {
			return current;
		} finally {
			last = current;
			current = current.nextASTNode;
		}
	}

	@Override
	public void skipNode() {
		if (current != null) {
			current = current.nextASTNode;
		}
	}

	@Override
	public ASTNode peekNext() {
		if (current != null && current.nextASTNode != null) {
			return current.nextASTNode;
		} else {
			return null;
		}
	}

	@Override
	public ASTNode peekNode() {
		if (current == null) {
			return null;
		}
		return current;
	}

	public void removeToken() {
		if (current != null) {
			current = current.nextASTNode;
		}
	}

	@Override
	public ASTNode peekLast() {
		return last;
	}

	@Override
	public ASTNode nodesBack(final int offset) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public ASTNode nodesAhead(final int offset) {
		if (current == null) {
			return null;
		}
		ASTNode cursor = null;
		for (int i = 0; i < offset; i++) {
			if ((cursor = current.nextASTNode) == null) {
				return null;
			}
		}
		return cursor;
	}

	@Override
	public void back() {
		current = last;
	}

	@Override
	public String showNodeChain() {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public int index() {
		return -1;
	}

	public void setCurrentNode(final ASTNode node) {
		this.current = node;
	}

	@Override
	public void finish() {
		reset();

		ASTNode last = null;
		ASTNode curr;

		while (hasMoreNodes()) {
			if ((curr = nextNode()).isDiscard()) {
				if (last == null) {
					last = firstASTNode = nextNode();
				} else {
					last.nextASTNode = nextNode();
				}
				continue;
			}

			if (!hasMoreNodes()) {
				break;
			}

			last = curr;
		}

		this.last = last;

		reset();
	}

}
