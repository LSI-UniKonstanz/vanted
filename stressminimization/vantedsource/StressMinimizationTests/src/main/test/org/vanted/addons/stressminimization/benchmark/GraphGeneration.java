package org.vanted.addons.stressminimization.benchmark;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.AttributeHelper;
import org.PositionGridGenerator;
import org.graffiti.graph.*;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.graph_generation.WattsStrogatzGraphGenerator;


/**
 * Deterministically generates test graphs. 
 * All graphs can be generated with varying node numbers.
 * 
 * Graphs that are called "random" are generated using a fixed seed, 
 * so repeated executions of these generation methods will return the same graphs.
 */
public class GraphGeneration {

	private static final long RANDOM_SEED = 0xFEEFEE;
	
	/**
	 * Generates a complete graph: all nodes are connected to all other node.
	 * The graph will have numberOfNodes nodes and (numberOfNodes(numberOfNodes-1))/2 edges.
	 * @param numberOfNodes
	 */
	public Graph generateComplete(int numberOfNodes) {
		
		if (numberOfNodes < 1) {
			throw new IllegalArgumentException("numberOfNodes must be >= 1.");
		}

		Graph graph = new AdjListGraph();
		
		for (int i = 0; i < numberOfNodes; i += 1) {
			graph.addNode();
		}
		
		List<Node> others = new ArrayList<>(graph.getNodes());
		
		for (Node node : graph.getNodes()) {
			others.remove(node);
			for (Node other : others) {
				graph.addEdge(node, other, false);
			}
		}

		positionNodesRandom(graph);
		return graph;
		
	}

	/**
	 * Generates a star graph: all nodes are connected to one central node.
	 * The graph will have numberOfNodes nodes and numberOfNodes edges.
	 * @param numberOfNodes
	 */
	public Graph generateStar(int numberOfNodes) {
		
		if (numberOfNodes < 1) {
			throw new IllegalArgumentException("numberOfNodes must be >= 1.");
		}
		
		Graph graph = new AdjListGraph();
		
		Node centralNode = graph.addNode();
		for (int i = 1; i < numberOfNodes; i += 1) {
			Node node = graph.addNode();
			graph.addEdge(node, centralNode, false);
		}
		
		positionNodesRandom(graph);
		return graph;
		
	}

	/**
	 * Generates a wheel graph: all nodes are connected to one central node 
	 * and to the nodes next to each node on the outer circle.
	 * The graph will have numberOfNodes nodes and 2 * numberOfNodes edges.
	 * @param numberOfNodes
	 */
	public Graph generateWheel(int numberOfNodes) {
		
		if (numberOfNodes < 1) {
			throw new IllegalArgumentException("numberOfNodes must be >= 1.");
		}
		
		Graph graph = generateStar(numberOfNodes);
		
		Node firstOuter = null;
		Node lastVisitedOuter = null;
		for (Node node : graph.getNodes()) {
			if (node.getDegree() == 1) {
				// node is on the outer circle (not the central node)
				if (firstOuter == null) {
					// this node is the first node that was visited
					// the edge for this node will be added later
					firstOuter = node;
				} else {
					graph.addEdge(node, lastVisitedOuter, false);
				}
				lastVisitedOuter = node;
			}
		}
		
		// add edge for first node
		if (firstOuter != lastVisitedOuter) {
			graph.addEdge(firstOuter, lastVisitedOuter, false);
		}

		positionNodesRandom(graph);
		return graph;
		
	}

	/**
	 * Generates a line graph: all nodes are connected to one follow up node (except the last node).
	 * The graph will have numberOfNodes nodes and numberOfNodes edges.
	 * @param numberOfNodes
	 */
	public Graph generateLine(int numberOfNodes) {
		
		if (numberOfNodes < 1) {
			throw new IllegalArgumentException("numberOfNodes must be >= 1.");
		}
		
		Graph graph = new AdjListGraph();
		
		// the first node is not connected to a previous node
		Node lastNode = graph.addNode();
		
		for (int i = 1; i < numberOfNodes; i += 1) {
			Node node = graph.addNode();
			graph.addEdge(node, lastNode, false);
			lastNode = node;
		}

		positionNodesRandom(graph);
		return graph;
		
	}

	/**
	 * Generates a Barabási–Albert graph: A graph that has similarities to some human made 
	 * and natural networks, for example the internet or social networks (wikipedia).
	 * @param numberOfNodes
	 */
	public Graph generateBarabasiAlbertNetwork(int numberOfNodes) {
		
		if (numberOfNodes < 3) {
			throw new IllegalArgumentException("numberOfNodes must be >= 3 for Barabási Albert network.");
		}

		return generateBarabasiAlbertNetwork(numberOfNodes, 3, 3, 1.0);
		
	}

	/**
	 * This method generates a (pseudo-random, deterministic) Barabási–Albert network.
	 * @param N the number of nodes to generate
	 * @param m0 Size of the initial connected component
	 * @param m Desired degree of each node outside the initial component
	 * @param a Parameter to control the degree distribution
	 */
	public Graph generateBarabasiAlbertNetwork(int N, int m0, int m, double a) {
		
		if (N < 1) {
			throw new IllegalArgumentException("numberOfNodes must be >= 1.");
		}
		if (m0 < 1 || m0 > N) {
			throw new IllegalArgumentException("Illegal m0.");
		}
		if (m > m0) {
			throw new IllegalArgumentException("m must be <= m0.");
		}
		if (a <= 0) {
			throw new IllegalArgumentException("Illegal a.");
		}
		
		Random rand = new Random(RANDOM_SEED);
		
		// generate initial connected component
		// the original algorithm does not specify how the initial component should be connected
		Graph graph = generateLine(m0);
		
		for (int i = m0; i < N; i += 1) {
			
			List<Node> connectedNodes = new ArrayList<>();
			
			while (connectedNodes.size() < m) {
				int j = rand.nextInt(graph.getNumberOfNodes());
				Node nj = graph.getNodes().get(j);
				
				double Pj = Math.pow(((double)nj.getDegree()) / (2.0 * (double) graph.getNumberOfEdges()), a);
				
				if (rand.nextDouble() < Pj) {
					connectedNodes.add(nj);
				}
			}
			
			Node node = graph.addNode();
			for (Node nj : connectedNodes) {
				graph.addEdge(node, nj, false);
			}
			
		}

		positionNodesRandom(graph);
		return graph;
		
	}

	/**
	 * Generates a Watts Strogatz graph: A graph with "small world" properties.
	 * @param numberOfNodes
	 */
	public Graph generateWattsStrogatzNetwork(int numberOfNodes) {

		return generateWattsStrogatzNetwork(numberOfNodes, 3, 0.5);
		
	}
	
	/**
	 * This method generates a (pseudo-random, deterministic) Watts-Strogatz network.
	 * @param numberOfNodes
	 * @param k Initial degree of each node.
	 * @param p Probability that an edge will be "rewired"
	 * @return
	 */
	public Graph generateWattsStrogatzNetwork(int numberOfNodes, int k, double p) {

		// NOTE: We don't use the other generator, because we want to control the random numbers used for generation
		
		if (numberOfNodes < 1) {
			throw new IllegalArgumentException("numberOfNodes must be >= 1.");
		}
		if (k < 2) {
			throw new IllegalArgumentException("k must be >= 2.");
		}

		Random rand = new Random(RANDOM_SEED);

		Graph graph = new AdjListGraph();
		
		// generate the initial graph
		for (int i = 0; i < numberOfNodes; i++) {
			graph.addNode();
		}
		for (int i = 0; i < numberOfNodes; i++) {
			Node node = graph.getNodes().get(i);
			
			int currentDegree = 0;
			int j = i - k / 2;
			if (j < 0) {
				j += numberOfNodes;
			}
			while (currentDegree < k) {
				j = (j + 1) % numberOfNodes;
				
				Node other = graph.getNodes().get(j);
				if (!node.getNeighbors().contains(other) && node != other) {
					graph.addEdge(node, other, false);
				}
				currentDegree += 1;
			}
			
		}
		
		// "rewire" nodes
		for (Edge edge : graph.getEdges()) {
			double r = rand.nextDouble();
			
			if (r <= p) {
				
				Node node = edge.getSource();
				ArrayList<Node> unconnected = new ArrayList<Node>(graph.getNodes());
				unconnected.remove(node);
				unconnected.removeAll(node.getNeighbors());
				
				if (unconnected.size() > 0) {
					int j = rand.nextInt(unconnected.size());
					Node newTarget = unconnected.get(j);
					edge.setTarget(newTarget);
				}
				
			}
		}
		
		positionNodesRandom(graph);
		return graph;
		
	}
	
	/**
	 * Generates a Sierpisky Triangle graph with {@code recusionDepth} levels. 
	 * @param recursionDepth How many levels of nested triangles shawl be generated. A value of 1 generates one simple triangle.
	 */
	public Graph generateSierpinskyTriangle(int recursionDepth) {
		
		if (recursionDepth < 1) {
			throw new IllegalArgumentException("recursionDepth must be >= 1.");
		}
		
		Graph graph = new AdjListGraph();
		
		// the triangle structures of each "recursion step" 
		// are stored for the next iteration
		List<Triangle> triangles = new ArrayList<Triangle>();
		
		// create the first triangle (recursion step 0)
		// edges are added after all nodes were generated
		Node n1 = graph.addNode();
		Node n2 = graph.addNode();
		Node n3 = graph.addNode();
		triangles.add(new Triangle(n1, n2, n3));
		
		for (int level = 0; level < recursionDepth; level += 1) {
			
			List<Triangle> newTriangles = new ArrayList<Triangle>();
			
			for (Triangle oldTriangle : triangles) {
				Node old1 = oldTriangle.corner1;
				Node old2 = oldTriangle.corner2;
				Node old3 = oldTriangle.corner3;
				
				Node new1 = graph.addNode();
				Node new2 = graph.addNode();
				Node new3 = graph.addNode();
				
				Triangle t1 = new Triangle(old1, new1, new3);
				Triangle t2 = new Triangle(old2, new1, new2);
				Triangle t3 = new Triangle(old3, new2, new3);
				
				newTriangles.add(t1);
				newTriangles.add(t2);
				newTriangles.add(t3);
			}
			
			triangles = newTriangles;
		}
		
		// add the edges in the triangles
		for (Triangle t : triangles) {
			graph.addEdge(t.corner1, t.corner2, false);
			graph.addEdge(t.corner1, t.corner3, false);
			graph.addEdge(t.corner2, t.corner3, false);
		}
		
		positionNodesRandom(graph);
		return graph;
		
	}
	
	private class Triangle {
		public final Node corner1;
		public final Node corner2;
		public final Node corner3;
		
		public Triangle(Node corner1, Node corner2, Node corner3) {
			super();
			this.corner1 = corner1;
			this.corner2 = corner2;
			this.corner3 = corner3;
		}
		
	}
	
	// MARK: Utils
	 
	/**
	 * Assigns a random position to each node in the graph
	 */
	private void positionNodesRandom(Graph graph) {

		Random rand = new Random(RANDOM_SEED);
		
		for (Node node : graph.getNodes()) {
			
			double posX = rand.nextDouble() * 100;
			double posY = rand.nextDouble() * 100;
			AttributeHelper.setPosition(node, posX, posY);
			
		}
		
	}
	
}
