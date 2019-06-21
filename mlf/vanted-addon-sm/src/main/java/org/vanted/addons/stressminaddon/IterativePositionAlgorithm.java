package org.vanted.addons.stressminaddon;

import org.Vector2d;
import org.graffiti.graph.Node;
import org.vanted.addons.stressminaddon.util.NodeValueMatrix;
import org.vanted.addons.stressminaddon.util.gui.Describable;
import org.vanted.addons.stressminaddon.util.gui.Parameterizable;

import java.util.List;

/**
 * Implementing classes can calculate the next iteration of positions of a given set of
 * nodes and their positions.
 *
 * @author Jannik
 */
public interface IterativePositionAlgorithm extends Parameterizable, Describable {

    /**
     * Performs the next iteration and calculates the new positions from the old ones.
     *
     * @param nodes
     *      the nodes to be worked on. The indices of the nodes in this list correspond to
     *      the indices used by {@code positions}, {@code distances} and {@code weights}.<br>
     *      The implementing class shall not change their position (attribute) in any way.<br>
     *      The position of these nodes may not represent the actual positions of these
     *      nodes in the calling class (because they <i>may</i> only be updated after
     *      all iterations are done).
     *      The implementing class should refer to {@code positions} to get these positions reliably.
     * @param positions
     *      the positions the algorithm shall work with.<br>
     *      The implementing class shall not change their values in any way.
     * @param distances
     *      the matrix containing the node theoretical distances between the nodes.
     *      The implementing class shall only read the values in this matrix.
     * @param weights
     *      the matrix containing the weights associated with each node pair.
     *      The implementing class shall only read the values in this matrix.
     * @return
     *      the newly iterated positions of the nodes in an list with the same
     *      index.
     *
     * @author Jannik
     */
    public List<Vector2d> nextIteration(final List<Node> nodes,
                                        final List<Vector2d> positions,
                                        final NodeValueMatrix distances,
                                        final NodeValueMatrix weights);
}
