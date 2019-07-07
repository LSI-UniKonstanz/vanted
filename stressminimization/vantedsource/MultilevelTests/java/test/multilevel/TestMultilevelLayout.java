package test.multilevel;

import junit.framework.TestCase;

import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.selection.Selection;
import org.junit.Test;
import org.vanted.addons.MultilevelFramework.MultilevelLayoutAlgorithm;

/**
 * General test cases for StressMinimizationLayouter
 */
public class TestMultilevelLayout extends TestCase {
	
	private MultilevelLayoutAlgorithm layout = new MultilevelLayoutAlgorithm();
	
	@Test(expected = PreconditionException.class)
	public void testCheckThrowsOnEmpty() throws PreconditionException {
		
		Graph empty = new AdjListGraph();
		Selection sel = new Selection();
		
		layout.attach(empty, sel);
		layout.check();
		
	}
	
	@Test(expected = PreconditionException.class)
	public void testCheckThrowsOnDirected() throws PreconditionException {
		
		Graph directed = new AdjListGraph();
		Node n1 = directed.addNode();
		Node n2 = directed.addNode();
		directed.addEdge(n1, n2, true);
		Selection sel = new Selection();
		
		layout.attach(directed, sel);
		layout.check();
		
	}
}
