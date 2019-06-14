
package org.vanted.addons.stressminimization;

import junit.framework.TestCase;

import java.awt.geom.Point2D;
import java.util.*;

import org.AttributeHelper;
import org.Vector2d;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.selection.Selection;
import org.junit.Test;

/**
 * General test cases for StressMinimizationLayouter
 */
public class TestStressMinimizationLayout extends TestCase {
	
	private StressMinimizationLayout layout = new StressMinimizationLayout();
	
	@Test
	public void testCheckThrowsOnEmpty() throws PreconditionException {
		
		Graph empty = new AdjListGraph();
		Selection sel = new Selection();
		
		layout.attach(empty, sel);
		try {
			// the @Test(expected = PreconditionException.class)
			// seems not to work or not to work as I expect it
			layout.check();
			fail("Expected precodition exception for empty graph");
		} catch (Exception ex) {
			assert(ex instanceof PreconditionException);
		}
		
	}
	
	@Test
	public void testUnconnectedGraphs() {
		
		//create Graph with n unconnected nodes
		Graph unconnected = new AdjListGraph();
		int n = 2;
		for(int i = 0; i<n; i++) {
			Node node = unconnected.addNode();
			AttributeHelper.setPosition(node, i, i);
		}
		Selection sel = new Selection();
		for(Node node : unconnected.getNodes()) {
			sel.add(node);
		}
		
		//Stressmin the graph
		String[] args = new String[0];
		StartVantedWithStressMinAddon.main(args);

		layout.attach(unconnected, sel);
		try {
			layout.check();
		} catch (PreconditionException e) {
			e.printStackTrace();
		}
		layout.setParameters( layout.getParameters() );
	
		layout.execute();
		
		//Get positions of all Nodes
		ArrayList<Point2D> positions = new ArrayList<>();
		for(Node node : unconnected.getNodes()) {
			positions.add(AttributeHelper.getPosition(node));
		}
		
		//Compare all Positions
		boolean positionsUnique = true;
		
		for(int i = 0; i<positions.size(); i++) {
			for(int j = i+1; j<positions.size(); j++) {
				if(positions.get(i).equals(positions.get(j))) {
					positionsUnique = false;
				}
			}
		}
		assert(positionsUnique);
	}
	

	@Test
	public void testWorkingOnSingleNodes() {
		
		//create Graph with n unconnected nodes
		Graph singleNode = new AdjListGraph();
		
		Node node = singleNode.addNode();
		AttributeHelper.setPosition(node, 1, 1);
		
		StartVantedWithStressMinAddon.main(new String[0]);

		layout.attach(singleNode, new Selection(""));
		try {
			layout.check();
		} catch (PreconditionException e) {
			e.printStackTrace();
		}
		layout.setParameters( layout.getParameters() );
		layout.execute();
		
		// fails if error is thrown
		
	}
	
	@Test
	public void testWorkingOnSelection() {
		 
		// create star graph with additional triangle between some nodes on the outer circle
		Graph g = new AdjListGraph();
		Node n0 = g.addNode();
		Node n1 = g.addNode();
		Node n2 = g.addNode();
		Node n3 = g.addNode();
		Node n4 = g.addNode();
		Node n5 = g.addNode();
		g.addEdge(n1, n0, false);
		g.addEdge(n2, n0, false);
		g.addEdge(n3, n0, false);
		g.addEdge(n4, n0, false);
		g.addEdge(n5, n0, false);
		// triangle
		g.addEdge(n3, n4, false);
		g.addEdge(n4, n5, false);
		g.addEdge(n5, n3, false);
		
		Selection s = new Selection();
		// add triangle to selection
		s.add(n3);
		s.add(n4);
		s.add(n5);
		
		// create a certainly suboptimal initial layout
		int counter = 0;
		for (Node n : g.getNodes()) {
			counter += 1;
			Vector2d position = new Vector2d(0, counter);
			AttributeHelper.setPosition(n, position);
		}

		Map<Node, Map<Node, Vector2d>> initialRelativePositions = new HashMap<>();
		for (Node first : g.getNodes()) {
			
			Vector2d firstPos = AttributeHelper.getPositionVec2d(first);
			initialRelativePositions.put(first, new HashMap<>());
			
			for (Node second : g.getNodes()) {
				Vector2d secondPos = AttributeHelper.getPositionVec2d(second);
				Vector2d relativePos = new Vector2d(firstPos.x - secondPos.x, firstPos.y - secondPos.y);
				
				initialRelativePositions.get(first).put(second, relativePos);
			}
		}
		
		StartVantedWithStressMinAddon.main(new String[0]);

		layout.attach(g, s);
		try {
			layout.check();
		} catch (PreconditionException e) {
			e.printStackTrace();
		}
		layout.setParameters( layout.getParameters() );
		layout.execute();
		
		// check that only the positions of the selected nodes changed

		for (Node first : g.getNodes()) {
			
			if (first.equals(n3) || first.equals(n4) || first.equals(n5)) {
				continue;
			}
			
			Vector2d firstPos = AttributeHelper.getPositionVec2d(first);
			
			for (Node second : g.getNodes()) {

				if (second.equals(n3) || second.equals(n4) || second.equals(n5)) {
					continue;
				}
				
				Vector2d secondPos = AttributeHelper.getPositionVec2d(second);
				Vector2d newRelativePos = new Vector2d(firstPos.x - secondPos.x, firstPos.y - secondPos.y);
				Vector2d originalRelativePos = initialRelativePositions.get(first).get(second);
				
				assertEquals(newRelativePos.x, originalRelativePos.x, 1e-10);
				assertEquals(newRelativePos.y, originalRelativePos.y, 1e-10);
				
			}
		}
		
	}
	
	/**
	 * The first implementation did not accept directed inputs, 
	 * but this behavior is undesired.
	 */
	@Test
	public void testCheckDoesNotThrowOnDirected() {
		
		Graph directed = new AdjListGraph();
		Node n1 = directed.addNode();
		Node n2 = directed.addNode();
		directed.addEdge(n1, n2, true);
		Selection sel = new Selection();
		
		layout.attach(directed, sel);
		try {
			layout.check();
		} catch (PreconditionException ex) {
			fail("Implementation did not accept directed input");
		}
		
	}
}
