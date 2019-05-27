package org.vanted.addons.stressminaddon;

import org.Vector2d;
import org.graffiti.graph.Node;
import org.vanted.addons.stressminaddon.util.NodeValueMatrix;

import java.util.HashMap;
import java.util.List;

public interface IterativePositionAlgorithm {

    /**
     * Performs the next iteration and calculates the new positions from the old ones.
     *
     * @param nodes
     *      the nodes to be worked on. The indices of the nodes in this list correspond to
     *      the indices used by {@code distances} and {@code weights}.
     * @param distances
     *      the matrix containing the node graphical distances between the nodes.
     *      The implementing class shall only read the values in this matrix.
     * @param weights
     *      the matrix containing the node graphical distances between the nodes.
     *      The implementing class shall only read the values in this matrix.
     * @return
     */
    public HashMap<Node, Vector2d> nextIteration(final List<Node> nodes,
                                                 final NodeValueMatrix distances,
                                                 final NodeValueMatrix weights);
}
