package org.vanted.addons.stressminaddon;

import org.Vector2d;
import org.graffiti.graph.Node;
import org.vanted.addons.stressminaddon.util.NodeValueMatrix;

import java.util.HashMap;
import java.util.List;

/**
 * Implementing classes can calculate the next iteration of positions of a given set of
 * nodes and their positions.
 *
 * @author Jannik
 */
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
     *      the newly iterated positions of the nodes in an list with the same
     *      index.
     *
     * @author Jannik
     */
    public List<Vector2d> nextIteration(final List<Node> nodes,
                                        final NodeValueMatrix distances,
                                        final NodeValueMatrix weights);
}
