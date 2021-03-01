package org.vanted.indexednodes;

import java.util.ArrayList;

/**
 * A list of edges
 * 
 * @since 2.8
 * @author Benjamin Moser
 */
public class IndexedEdgeList extends ArrayList<int[]> {
	/**
	* 
	*/
	private static final long serialVersionUID = 5039113382830679266L;
	
	/**
	 * Add an edge indicated by indices. No sanity checks are done.
	 *
	 * @param from
	 *           index of source node
	 * @param to
	 *           index of target node
	 */
	public void add(int from, int to) {
		this.add(new int[] { from, to });
	}
	
	/**
	 * Add edges from `source` to all nodes in `targets`.
	 */
	public IndexedEdgeList addFan(int source, IndexedNodeSet targets) {
		for (int target : targets) {
			this.add(source, target);
		}
		return this;
	}
	
}
