package org.vanted.addons.stressminimization.primitives;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.junit.Test;

/**
 * General test cases for IndexedNodeSet
 */
public class TestIndexedNodeSet {

	// ==============
	// MARK: creation
	// ==============
	
	@Test
	public void testAllNodesCreation() {

		Graph g = new AdjListGraph();
		List<Node> nodes = new ArrayList<>();
		for (int i = 0; i < 10; i += 1) {
			nodes.add(g.addNode());
		}
		
		IndexedNodeSet nodeSet = IndexedNodeSet.setOfAllIn(nodes);
		
		for (int i = 0; i < nodes.size(); i += 1) {
			assert(nodeSet.contains(i));
			assert(nodeSet.contains(nodes.get(i)));
		}
		
	}
	
	@Test
	public void testEmptyCreation() {

		Graph g = new AdjListGraph();
		List<Node> nodes = new ArrayList<>();
		for (int i = 0; i < 10; i += 1) {
			nodes.add(g.addNode());
		}
		
		IndexedNodeSet nodeSet = IndexedNodeSet.emptySetOf(nodes);
		
		for (int i = 0; i < nodes.size(); i += 1) {
			assert(!nodeSet.contains(i));
			assert(!nodeSet.contains(nodes.get(i)));
		}
		
		assert(nodeSet.isEmpty());
		assert(nodeSet.size() == 0);
		
	}
	
	// ==============
	// MARK: indexing
	// ==============
	
	@Test
	public void testAllNodesIndexedAsExpected() {

		Graph g = new AdjListGraph();
		List<Node> nodes = new ArrayList<>();
		for (int i = 0; i < 10; i += 1) {
			nodes.add(g.addNode());
		}
		
		IndexedNodeSet nodeSet = IndexedNodeSet.emptySetOf(nodes);
		nodeSet.add(1);
		nodeSet.add(3);
		nodeSet.add(nodes.get(6));
		nodeSet.add(7);
		nodeSet.add(nodes.get(0));
		
		assert(nodeSet.getIndex(nodes.get(0)) == 0);
		assert(nodeSet.getIndex(nodes.get(1)) == 1);
		assert(nodeSet.getIndex(nodes.get(3)) == 3);
		assert(nodeSet.getIndex(nodes.get(6)) == 6);
		assert(nodeSet.getIndex(nodes.get(7)) == 7);
		
	}

	@Test
	public void testNotContainedNodeNotIndexed() {

		Graph g = new AdjListGraph();
		List<Node> nodes = new ArrayList<>();
		for (int i = 0; i < 10; i += 1) {
			nodes.add(g.addNode());
		}
		
		Node specialNode = g.addNode();
		
		IndexedNodeSet nodeSet = IndexedNodeSet.setOfAllIn(nodes);
		try {
			nodeSet.getIndex(specialNode);
			fail();
		} catch (IndexedNodeSet.NotIndexedException ex) {
			
		}
		
	}
	
	@Test
	public void testIndexAccess() {

		Graph g = new AdjListGraph();
		List<Node> nodes = new ArrayList<>();
		for (int i = 0; i < 10; i += 1) {
			nodes.add(g.addNode());
		}
		
		IndexedNodeSet nodeSet = IndexedNodeSet.setOfAllIn(nodes);

		for (int i = 0; i < nodes.size(); i += 1) {
			assert(nodeSet.get(i).equals(nodes.get(i)));
		}
		
	}
	
}
