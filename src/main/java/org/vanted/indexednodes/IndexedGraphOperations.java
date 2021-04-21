package org.vanted.indexednodes;

import org.apache.commons.math3.linear.RealVector;
import org.graffiti.graph.Node;
import org.vanted.indexednodes.accumulators.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Some graph operations backed by org.vanted.addons.indexednodes.IndexedNodeSet. Faster performance
 * over more naive implementations. Specifically, this data structure is more space efficient.
 * 
 * @since 2.8
 * @author Benjamin Moser
 */
public class IndexedGraphOperations {
	
	private IndexedGraphOperations() {
	}
	
	/**
	 * Get all connected components in the induced `subgraph`
	 *
	 * @param subgraph
	 * @return
	 */
	public static List<IndexedComponent> getComponents(IndexedNodeSet subgraph) {
		List<IndexedComponent> components = new ArrayList<>();
		if (subgraph.isEmpty())
			return components;
		IndexedNodeSet nodesToProcess = subgraph.copy();
		while (!nodesToProcess.isEmpty()) {
			int startNode = nodesToProcess.first();
			IndexedNodeSetAccumulator bfsNodes = new IndexedNodeSetAccumulator(subgraph.emptySubset());
			IndexedEdgeListAccumulator bfsEdges = new IndexedEdgeListAccumulator();
			breadthFirstSearch(startNode, subgraph, bfsNodes, bfsEdges);
			nodesToProcess.setMinus(bfsNodes.get());
			components.add(new IndexedComponent(bfsNodes.get(), bfsEdges.get()));
		}
		return components;
	}
	
	/**
	 * Calculates the distance vector from the specified node to all other nodes in the given
	 * org.vanted.indexednodes.IndexedNodeSet.
	 *
	 * @param from
	 *           the distances from this node to all others will be calculated.
	 * @return the distance vector from the from node to all others
	 */
	public static RealVector calcDistances(final Node from, IndexedNodeSet inSet) {
		int fromIndex = inSet.getIndex(from);
		return calcDistances(fromIndex, inSet);
	}
	
	public static void breadthFirstSearch(
			int startNode,
			IndexedNodeSet subgraph,
			StatefulAccumulator<?, IndexedComponent>... accumulators) {
		IndexedNodeSet firstNeighbours = subgraph.getInducedNeighboursOf(startNode);
		// submit start node and its edges to accumulator
		Arrays.stream(accumulators).forEach((acc) -> {
			acc.apply(new IndexedComponent(
					subgraph.singletonSubset(startNode),
					(new IndexedEdgeList()).addFan(startNode, firstNeighbours)));
		});
		// initialise queue with all neighbours of startNode
		IndexedNodeSet nodesToConsider = firstNeighbours;
		IndexedNodeSet alreadyVisited = subgraph.emptySubset();
		alreadyVisited.add(startNode);
		while (!nodesToConsider.isEmpty()) {
			IndexedNodeSet nodesToConsiderNext = subgraph.emptySubset();
			// only pass nodes into accumulator that are newly discovered
			// i.e. check here with alreadyVisited
			// i.e. construct a new BitSet containing nodes filtered for alreadyVisited
			IndexedNodeSet newNodesToReport = subgraph.emptySubset();
			IndexedEdgeList newEdgesToReport = new IndexedEdgeList();
			nodesToConsider.containedNodes.stream()
					.filter((i) -> !alreadyVisited.contains(i))
					.forEach((newNodeIndex) -> {
						// node is newly encountered, add it to DS of newly encountered nodes
						// that will be passed to consumers
						newNodesToReport.add(newNodeIndex);
						
						IndexedNodeSet neighbours = subgraph.getInducedNeighboursOf(newNodeIndex);
						// add neighbourhood of newly encountered node to queue
						// here we potentially add already-seen nodes. technically, we could
						// also filter here, but we do so after the loop for increased
						// performance
						nodesToConsiderNext.union(neighbours);
						// at this point we can identify all the edges, i.e.
						// those from newNodeIndex to a neighbour
						// if an edge goes to an already-seen node, we have already reported it
						// thus, eliminate all already-seen nodes from neighbours
						neighbours.setMinus(alreadyVisited);
						// now any edge is a new edge. report edges.
						newEdgesToReport.addFan(newNodeIndex, neighbours);
						
						alreadyVisited.add(newNodeIndex);
					});
			Arrays.stream(accumulators).forEach((acc) -> {
				acc.apply(new IndexedComponent(newNodesToReport, newEdgesToReport));
			});
			nodesToConsiderNext.setMinus(alreadyVisited);
			nodesToConsider = nodesToConsiderNext;
		}
	}
	
	public static int countNodes(final int fromIndex, IndexedNodeSet subgraph) {
		NodeCountAccumulator acc = new NodeCountAccumulator(0);
		breadthFirstSearch(fromIndex, subgraph, acc);
		return acc.get();
	}
	
	public static int countEdges(final int fromIndex, IndexedNodeSet subgraph) {
		EdgeCountAccumulator acc = new EdgeCountAccumulator(0);
		breadthFirstSearch(fromIndex, subgraph, acc);
		return acc.get();
	}
	
	public static RealVector calcDistances(final int fromIndex, IndexedNodeSet inSet) {
		DistanceAccumulator acc = new DistanceAccumulator(inSet.size());
		breadthFirstSearch(fromIndex, inSet, acc);
		return acc.get();
	}
	
}
