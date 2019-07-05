package org.vanted.addons.stressminimization.primitives;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.graffiti.graph.Node;

/**
 * Utility class for basic graph operations
 */
public class BasicGraphOperations {

	private BasicGraphOperations() {}

	public static List<IndexedNodeSet> getComponents(IndexedNodeSet nodes) {

		List<IndexedNodeSet> components = new ArrayList<>();

		if (nodes.isEmpty()) {
			return components;
		}

		IndexedNodeSet nodesNotFound = nodes.copy();

		while (!nodesNotFound.isEmpty()) {

			int currentIndex = nodesNotFound.first();

			RealVector distances = calcDistances(currentIndex, nodes);

			IndexedNodeSet component = nodes.emptySubset();

			for (int i = 0; i < distances.getDimension(); i += 1) {
				if (distances.getEntry(i) < Double.POSITIVE_INFINITY) {
					component.add(i);
				}
			}

			nodesNotFound.setMinus(component);

			components.add(component);

		}

		return components;

	}

	/**
	 * Calculates the distance vector from the specified node to all other nodes in the given IndexedNodeSet.
	 * @param from the distances from this node to all others will be calculated.
	 * @return the distance vector from the from node to all others
	 */
	public static RealVector calcDistances(final Node from, IndexedNodeSet inSet) {

		int fromIndex = inSet.getIndex(from);
		return calcDistances(fromIndex, inSet);

	}

	/**
	 * Calculates the distance vector from the node at the specified index in the given IndexedNodeSet to all other nodes in the given IndexedNodeSet.
	 * @param fromIndex the distances from this node to all others will be calculated.
	 * @return the distance vector from the from node to all others
	 */
	public static RealVector calcDistances(final int fromIndex, IndexedNodeSet inSet) {

		final int n = inSet.size();

		RealVector distances = new ArrayRealVector(n);

		//Breadth first Search
		for (int i = 0; i < n; i += 1) {
			distances.setEntry(i, Double.POSITIVE_INFINITY);
		}

		distances.setEntry(fromIndex, 0);

		IndexedNodeSet nodesToVisit = inSet.getNeighbors(fromIndex);

		int dist = 1;

		IndexedNodeSet alreadyVisited = inSet.emptySubset();

		while(nodesToVisit.size() != 0) {
			//next layer is empty at first
			IndexedNodeSet nodesToVisitNext = inSet.emptySubset();

			for(Integer j : nodesToVisit) {

				if(!alreadyVisited.contains(j)) {

					if(distances.getEntry(j) > dist) {
						distances.setEntry(j, dist);
					}

					alreadyVisited.add(j);

					//Add neighbors of node to next layer
					nodesToVisitNext.union( inSet.getNeighbors(j) );
				}
			}
			//current layer is done
			nodesToVisitNext.setMinus(alreadyVisited);
			nodesToVisit = nodesToVisitNext;
			dist++;
		}

		return distances;

	}

}
