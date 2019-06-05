package org.vanted.addons.stressminimization.benchmark;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.AttributeHelper;
import org.graffiti.graph.*;


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
	 * Generates a "natural" graph: A graph that is somewhat similar to typical human made graphs.
	 * This method generates a (pseudo-random, deterministic) Barabási–Albert network.
	 * @param numberOfNodes
	 */
	public Graph generateNatural(int numberOfNodes) {
		
		if (numberOfNodes < 3) {
			throw new IllegalArgumentException("numberOfNodes must be >= 3 for natural network.");
		}

		return generateBarabásiAlbertNetwork(numberOfNodes, 3, 3, 1.0);
		
	}

	/**
	 * This method generates a (pseudo-random, deterministic) Barabási–Albert network.
	 * This method is not guaranteed to terminate.
	 * @param N the number of nodes to generate
	 * @param m0 Size of the initial connected component
	 * @param m Desired degree of each node outside the initial component
	 * @param a Parameter to control the degree distribution
	 */
	public Graph generateBarabásiAlbertNetwork(int N, int m0, int m, double a) {
		
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
