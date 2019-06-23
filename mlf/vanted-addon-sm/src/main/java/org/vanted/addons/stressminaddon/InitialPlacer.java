package org.vanted.addons.stressminaddon;

import org.Vector2d;
import org.graffiti.graph.Node;
import org.vanted.addons.stressminaddon.util.NodeValueMatrix;
import org.vanted.addons.stressminaddon.util.gui.Describable;
import org.vanted.addons.stressminaddon.util.gui.Parameterizable;

import java.util.List;

/**
 * Implementing classes can be can calculate an initial layout for a given set of nodes.
 *
 * @author Jannik
 */
public interface InitialPlacer extends Parameterizable, Describable {

    /**
     * Calculates an initial layout for the given nodes.
     * The positions of the nodes shall not be updated, and only the new positions shall be
     * returned.
     *
     * @param nodes
     *      the nodes which shall be layouted. The indices of the nodes in this list correspond to
     *      the indices used by {@code distances}.
     *      The implementing class shall not change their position (attribute) in any way.
     * @param distances
     *      the matrix containing the node theoretical distances between the nodes.
     *      The implementing class shall only read the values in this matrix.
     *
     * @return the new positions the nodes shall be moved to.
     *
     * @author Jannik
     */
    public List<Vector2d> calculateInitialPositions(final List<Node> nodes,
                                                    final NodeValueMatrix distances);
}
