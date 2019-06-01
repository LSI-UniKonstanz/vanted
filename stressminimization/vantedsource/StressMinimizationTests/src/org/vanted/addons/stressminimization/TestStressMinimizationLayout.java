
package org.vanted.addons.stressminimization;

import junit.framework.TestCase;

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
	
	@Test()
	public void testCheckThrowsOnDirected() throws PreconditionException {
		
		Graph directed = new AdjListGraph();
		Node n1 = directed.addNode();
		Node n2 = directed.addNode();
		directed.addEdge(n1, n2, true);
		Selection sel = new Selection();
		
		layout.attach(directed, sel);
		try {
			layout.check();
			fail("Expected precodition exception for directed graph");
		} catch (Exception ex) {
			assert(ex instanceof PreconditionException);
		}
		
	}
}
