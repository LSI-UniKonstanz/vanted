package org.vanted.indexednodes.accumulators;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.vanted.indexednodes.IndexedComponent;

/**
 * 
 * @since 2.8
 * @author Benjamin Moser
 *
 */
public class DistanceAccumulator extends StatefulAccumulator<RealVector, IndexedComponent> {
    public DistanceAccumulator(int n) {
        super( new ArrayRealVector(n).mapToSelf((e) -> Double.POSITIVE_INFINITY) );
    }

    // will be incremented with each new level
    int distance = 0;

    @Override
    public void accept(IndexedComponent component) {
        // we know that each call of this method corresponds to an increase of 1 in distance
        // from start node. The passed-in nodes are newly encountered nodes in the BFS.
        // the first call will contain only the start node.
        for (int node : component.nodes) {
            if (distance < this.state.getEntry(node)) {
                this.state.setEntry(node, distance);
            }
        }
        distance++;
    }
}
