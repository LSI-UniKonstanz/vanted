package test.multilevel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.List;

import org.AttributeHelper;
import org.graffiti.attributes.Attribute;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.selection.Selection;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.vanted.addons.MultilevelFramework.Coarsening.AbstractCoarseningAlgorithm;
import org.vanted.addons.MultilevelFramework.Coarsening.CoarseningAlgorithm;
import org.vanted.addons.MultilevelFramework.Coarsening.MatchingCoarseningAlgorithm;
import org.vanted.addons.MultilevelFramework.Coarsening.NullCoarseningAlgorithm;
import org.vanted.addons.MultilevelFramework.Coarsening.RandomCoarseningAlgorithm;
import org.vanted.addons.MultilevelFramework.Coarsening.RandomNeighborCoarseningAlgorithm;
import org.vanted.addons.MultilevelFramework.Coarsening.SolarMergerCoarsening;
import org.vanted.addons.MultilevelFramework.MultilevelGraph.MultilevelParentGraphAttribute;
import org.vanted.addons.MultilevelFramework.MultilevelGraph.MultilevelParentNodeAttribute;
import org.vanted.addons.MultilevelFramework.Placement.PlacementAlgorithm;
import org.vanted.addons.MultilevelFramework.Placement.RandomPlacementAlgorithm;
import org.vanted.addons.MultilevelFramework.Placement.SolarPlacement;
import org.vanted.addons.MultilevelFramework.Placement.ZeroPlacementAlgorithm;

public class TestMultilevel {

	private Graph circleGraph(int nodeCount) {
		Graph g = new AdjListGraph();
		Node oldNode = null;
		Node newNode = null;
		Node firstNode = null;
		for (int i = 0; i < nodeCount; i++) {
			newNode = g.addNode();
			AttributeHelper.setPosition(newNode, new Point2D.Double(0.0, 0.0));
			if (oldNode != null) {
				g.addEdge(oldNode, newNode, false);
			} else {
				firstNode = newNode;
			}
			oldNode = newNode;
		}
		g.addEdge(firstNode, newNode, false);
		return g;
	}

	/* TESTS ALL COARSENING ALGORITHMS NEED TO PASS */

	/**
	 * Tests if a coarsening algorithm assigns a parent to each node.
	 * 
	 * @param algorithm The algorithm to test
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	@ParameterizedTest
	@ValueSource(classes = { NullCoarseningAlgorithm.class, RandomCoarseningAlgorithm.class,
			RandomNeighborCoarseningAlgorithm.class, SolarMergerCoarsening.class, MatchingCoarseningAlgorithm.class })
	public void testCoarseningAllNodesHaveParent(Class algorithm)
			throws InstantiationException, IllegalAccessException {
		CoarseningAlgorithm testAlgorithm = (CoarseningAlgorithm) algorithm.newInstance();
		Graph input = circleGraph(100);
		Graph output = new AdjListGraph();
		Selection selection = new Selection();
		Attribute attr = new MultilevelParentGraphAttribute(MultilevelParentGraphAttribute.NAME, output, selection);
		AttributeHelper.setAttribute(input, MultilevelParentGraphAttribute.PATH, MultilevelParentGraphAttribute.NAME,
				attr);
		Selection sel = new Selection();
		sel.addAll(input.getGraphElements());
		testAlgorithm.attach(input, sel);
		testAlgorithm.execute();
		List<Node> outputNodes = output.getNodes();
		for (Node n : input.getNodes()) {
			Node parent = (Node) n.getAttribute(MultilevelParentNodeAttribute.FULLPATH).getValue();
			assertTrue(outputNodes.contains(parent));
		}
	}

	/* TESTS ALL PLACEMENT ALGORITHMS NEED TO PASS */

	@ParameterizedTest
	@ValueSource(classes = { RandomPlacementAlgorithm.class, ZeroPlacementAlgorithm.class, SolarPlacement.class })
	public void testPlacementAllNodesHavePosition(Class algorithm)
			throws InstantiationException, IllegalAccessException {
		Graph g = circleGraph(100);
		Graph gParent = new AdjListGraph();
		Selection selection = new Selection();
		Attribute attr = new MultilevelParentGraphAttribute(MultilevelParentGraphAttribute.NAME, gParent, selection);
		AttributeHelper.setAttribute(g, MultilevelParentGraphAttribute.PATH, MultilevelParentGraphAttribute.NAME, attr);
		Algorithm merger = new NullCoarseningAlgorithm();
		merger.attach(g, new Selection(g.getGraphElements()));
		merger.execute();
		for (Node n : gParent.getNodes()) {
			AttributeHelper.setPosition(n, new Point2D.Double(0, 0));
		}
		Algorithm placer = (PlacementAlgorithm) algorithm.newInstance();
		placer.attach(g, new Selection(g.getGraphElements()));
		placer.execute();
		for (Node n : g.getNodes()) {
			assertNotNull(AttributeHelper.getPosition(n));
		}
	}

	@ParameterizedTest
	@ValueSource(classes = { RandomPlacementAlgorithm.class, ZeroPlacementAlgorithm.class, SolarPlacement.class })
	public void testPlacementGraphStructureUnchanged(Class algorithm)
			throws InstantiationException, IllegalAccessException {
		Graph g = circleGraph(100);
		int numberOfNodes = g.getNodes().size();
		int numberOfEdges = g.getEdges().size();
		Graph gParent = new AdjListGraph();
		Selection selection = new Selection();
		Attribute attr = new MultilevelParentGraphAttribute(MultilevelParentGraphAttribute.NAME, gParent, selection);
		AttributeHelper.setAttribute(g, MultilevelParentGraphAttribute.PATH, MultilevelParentGraphAttribute.NAME, attr);
		Algorithm merger = new NullCoarseningAlgorithm();
		merger.attach(g, new Selection(g.getGraphElements()));
		merger.execute();
		for (Node n : gParent.getNodes()) {
			AttributeHelper.setPosition(n, new Point2D.Double(0, 0));
		}
		Algorithm placer = (PlacementAlgorithm) algorithm.newInstance();
		placer.attach(g, new Selection(g.getGraphElements()));
		placer.execute();
		assertEquals(numberOfNodes, g.getNodes().size());
		assertEquals(numberOfEdges, g.getEdges().size());
		for (Node n : g.getNodes()) {
			assertEquals(n.getDegree(), 2);
		}
	}

	/* TESTS FOR COARSENING ALGORITHM HELPER FUNCTIONS */

	@Test
	public void testAveragePosition() {
		Graph g = new AdjListGraph();
		AttributeHelper.setPosition(g.addNode(), new Point2D.Double(0, 0));
		AttributeHelper.setPosition(g.addNode(), new Point2D.Double(0, 2));
		AttributeHelper.setPosition(g.addNode(), new Point2D.Double(10, 0));
		AttributeHelper.setPosition(g.addNode(), new Point2D.Double(10, 2));
		AbstractCoarseningAlgorithm a = new NullCoarseningAlgorithm();
		Point2D.Double result = a.averagePosition(g.getNodes());
		assertEquals(result.x, 5.0, 0.0001);
		assertEquals(result.y, 1.0, 0.0001);
	}

	@Test
	public void testAllChildrenHaveSameParent() {
		Graph g = circleGraph(100);
		Graph gParent = new AdjListGraph();
		Selection selection = new Selection();
		Attribute attr = new MultilevelParentGraphAttribute(MultilevelParentGraphAttribute.NAME, gParent, selection);
		AttributeHelper.setAttribute(g, MultilevelParentGraphAttribute.PATH, MultilevelParentGraphAttribute.NAME, attr);
		AbstractCoarseningAlgorithm a = new NullCoarseningAlgorithm();
		a.attach(g, new Selection(g.getGraphElements()));
		a.createParent(g.getNodes(), new Point2D.Double(0, 0));
		Node firstNodeParent = a.getParent(g.getNodes().get(0));
		// test if parent of first node is parent of every node
		for (Node n : g.getNodes()) {
			assertSame(a.getParent(n), firstNodeParent);
		}
	}

	@Test
	public void testCreateEdges() {
		Graph g = circleGraph(100);
		Graph gParent = new AdjListGraph();
		Selection selection = new Selection();
		Attribute attr = new MultilevelParentGraphAttribute(MultilevelParentGraphAttribute.NAME, gParent, selection);
		AttributeHelper.setAttribute(g, MultilevelParentGraphAttribute.PATH, MultilevelParentGraphAttribute.NAME, attr);
		AbstractCoarseningAlgorithm a = new NullCoarseningAlgorithm();
		a.attach(g, new Selection(g.getGraphElements()));
		for (Node n : g.getNodes()) {
			a.createParent(Arrays.asList(n), new Point2D.Double(0, 0));
		}
		a.createEdges();
		for (Node n : g.getNodes()) {
			assertEquals(n.getDegree(), 2);
		}
	}

}
