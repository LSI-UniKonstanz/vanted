package org.vanted.plugins.layout.stressminimization;

import java.util.List;

import org.apache.commons.math3.linear.RealVector;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.junit.Test;
import org.vanted.indexednodes.IndexedComponent;
import org.vanted.indexednodes.IndexedGraphOperations;
import org.vanted.indexednodes.IndexedNodeSet;

/**
 * General test cases for BasicGraphOperations
 */
public class TestBasicGraphOperations {
	
	@Test
	public void testDistances() {
		//Test distances in line graph
		Graph g = new AdjListGraph();
		Node n = g.addNode();
		for (int i = 0; i < 10; i++) {
			Node m = g.addNode();
			g.addEdge(n, m, false);
			n = m;
		}
		RealVector test = IndexedGraphOperations.calcDistances(g.getNodes().get(0), IndexedNodeSet.setOfAllIn(g.getNodes()));
		for (int i = 0; i <= 10; i++) {
			assert (test.getEntry(i) == i);
		}
		
		//Test distances in graph with all nodes connected to the first
		g = new AdjListGraph();
		n = g.addNode();
		for (int i = 0; i < 10; i++) {
			Node m = g.addNode();
			g.addEdge(n, m, false);
		}
		RealVector test2 = IndexedGraphOperations.calcDistances(g.getNodes().get(0), IndexedNodeSet.setOfAllIn(g.getNodes()));
		for (int i = 1; i <= 10; i++) {
			assert (test2.getEntry(i) == 1);
		}
		
		//Test distances in unconnected graph
		g = new AdjListGraph();
		n = g.addNode();
		for (int i = 0; i < 10; i++) {
			g.addNode();
		}
		RealVector test3 = IndexedGraphOperations.calcDistances(g.getNodes().get(0), IndexedNodeSet.setOfAllIn(g.getNodes()));
		System.out.println(test3.toString());
		for (int i = 1; i <= 10; i++) {
			assert (test3.getEntry(i) == Double.POSITIVE_INFINITY);
		}
	}
	
	@Test
	public void testGetComponents() {
		
		//Get number of components from 10 unconnected nodes
		Graph g = new AdjListGraph();
		for (int i = 0; i < 10; i++) {
			g.addNode();
		}
		List<IndexedComponent> list = IndexedGraphOperations.getComponents(IndexedNodeSet.setOfAllIn(g.getNodes()));
		
		//We should have 10 components
		assert (list.size() == 10);
		for (int i = 0; i < list.size(); i++) {
			//Each component should be a single Node
			assert (list.get(i).size() == 1);
		}
		
		//Connect 2 Nodes. Now there should be 9 components
		g.addEdge(g.getNodes().get(2), g.getNodes().get(3), false);
		list = IndexedGraphOperations.getComponents(IndexedNodeSet.setOfAllIn(g.getNodes()));
		assert (list.size() == 9);
		
		//Test for empty graph
		g = new AdjListGraph();
		list = IndexedGraphOperations.getComponents(IndexedNodeSet.setOfAllIn(g.getNodes()));
		assert (list.size() == 0);
		
	}
	
}
