package net.simpleframework.lib.org.jsoup.nodes;

import java.io.IOException;

/**
 * A data node, for contents of style, script tags etc, where contents should
 * not show in text().
 * 
 * @author Jonathan Hedley, jonathan@hedley.net
 */
public class DataNode extends LeafNode {

	/**
	 * Create a new DataNode.
	 * 
	 * @param data
	 *        data contents
	 */
	public DataNode(final String data) {
		value = data;
	}

	@Override
	public String nodeName() {
		return "#data";
	}

	/**
	 * Get the data contents of this node. Will be unescaped and with original
	 * new lines, space etc.
	 * 
	 * @return data
	 */
	public String getWholeData() {
		return coreValue();
	}

	/**
	 * Set the data contents of this node.
	 * 
	 * @param data
	 *        unencoded data
	 * @return this node, for chaining
	 */
	public DataNode setWholeData(final String data) {
		coreValue(data);
		return this;
	}

	@Override
	void outerHtmlHead(final Appendable accum, final int depth, final Document.OutputSettings out)
			throws IOException {
		accum.append(getWholeData()); // data is not escaped in return from data
												// nodes, so " in script, style is plain
	}

	@Override
	void outerHtmlTail(final Appendable accum, final int depth, final Document.OutputSettings out) {
	}

	@Override
	public String toString() {
		return outerHtml();
	}

	@Override
	public DataNode clone() {
		return (DataNode) super.clone();
	}

	/**
	 * Create a new DataNode from HTML encoded data.
	 * 
	 * @param encodedData
	 *        encoded data
	 * @param baseUri
	 *        bass URI
	 * @return new DataNode
	 */
	public static DataNode createFromEncoded(final String encodedData, final String baseUri) {
		final String data = Entities.unescape(encodedData);
		return new DataNode(data);
	}
}
