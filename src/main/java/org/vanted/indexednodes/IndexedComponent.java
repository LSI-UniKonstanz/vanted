package org.vanted.indexednodes;

/**
 * @since 2.8
 * @author Benjamin Moser
 */
public class IndexedComponent {
	public IndexedNodeSet nodes;
	public IndexedEdgeList edges;
	
	public IndexedComponent(IndexedNodeSet nodes, IndexedEdgeList edges) {
		this.nodes = nodes;
		this.edges = edges;
	}
	
	public int size() {
		return this.nodes.size();
	}
}
