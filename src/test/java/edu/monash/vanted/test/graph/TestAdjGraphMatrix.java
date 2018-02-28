/**
 * 
 */
package edu.monash.vanted.test.graph;

import java.awt.Color;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.AttributeHelper;
import org.PositionGridGenerator;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.junit.Test;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;

/**
 * @author matthiak
 */
public class TestAdjGraphMatrix extends TestCase {

	Graph g;
	Node[] nodes;

	@Override
	protected void setUp() throws Exception {
		g = createRandomGraph(5, 0.5f, true, true, false);
		nodes = g.getNodes().toArray(new Node[g.getNodes().size()]);
	}

	@Test
	public void testOne() {
		byte[][] createAdjacencyMatrix = GraphHelper.createAdjacencyMatrix(g, true);
		printAdjacencyMatrix(nodes, createAdjacencyMatrix);

	}

	public static void printAdjacencyMatrix(Node[] nodes, byte[][] createAdjacencyMatrix) {
		System.out.print("patNode\\targetNode\t");
		for (Node n : nodes)
			System.out.print(AttributeHelper.getLabel(n, n.toString()) + "\t");
		System.out.println();
		for (int y = 0; y < nodes.length; y++) {
			System.out.print("\t" + AttributeHelper.getLabel(nodes[y], nodes[y].toString()) + "\t\t");
			for (int x = 0; x < nodes.length; x++) {
				System.out.print(createAdjacencyMatrix[y][x] + "\t");
			}
			System.out.println();
		}

	}

	private Graph createRandomGraph(int numberOfNodes, float p, boolean label, boolean directed, boolean selfLoops) {
		Graph rdg = new AdjListGraph();

		ArrayList<Node> nodes = new ArrayList<Node>();
		PositionGridGenerator pgg = new PositionGridGenerator(50, 50, 800);
		for (int i = 0; i < numberOfNodes; i++) {
			Node n = rdg.addNode(AttributeHelper.getDefaultGraphicsAttributeForNode(pgg.getNextPositionVec2d()));
			AttributeHelper.setShapeEllipse(n);
			nodes.add(n);

			if (label)
				AttributeHelper.setLabel(n, "" + (i + 1));
		}
		for (int i = 0; i < numberOfNodes; i++) {
			Node a = nodes.get(i);
			for (int j = 0; j < numberOfNodes; j++) {
				if (!selfLoops && (i == j))
					continue;
				if (!directed && i > j)
					continue;
				Node b = nodes.get(j);
				double r = Math.random();
				if (r <= p) {
					rdg.addEdge(a, b, directed,
							AttributeHelper.getDefaultGraphicsAttributeForEdge(Color.BLACK, Color.BLACK, directed));
				}
			}
		}
		return rdg;
	}
}
