
package org.vanted.addons.stressminimization;

import junit.framework.TestCase;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.AttributeHelper;
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
	
	@Test()
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

		layout.attach(unconnected, new Selection(""));
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
