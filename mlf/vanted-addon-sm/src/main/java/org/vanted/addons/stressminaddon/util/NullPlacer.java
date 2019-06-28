package org.vanted.addons.stressminaddon.util;

import org.AttributeHelper;
import org.Vector2d;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.parameter.Parameter;
import org.vanted.addons.stressminaddon.InitialPlacer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
 * A initial placer that does nothing but returns the current positions in the graph.
 * @author Jannik
 */
public class NullPlacer implements InitialPlacer {
    /**
     * Just returns the positions of the nodes in <code>nodes</code> in the order that is given by
     * the <code>nodes</code> array.
     *
     * @param nodes     the nodes which will be “layouted”.
     * @param distances the matrix containing the node theoretical distances between the nodes.
     *                  It is not be needed by this class and will be ignored.
     * @return the current positions of the nodes.
     * @author Jannik
     */
    @Override
    public List<Vector2d> calculateInitialPositions(List<Node> nodes, NodeValueMatrix distances) {
        List<Vector2d> result = new ArrayList<>();
        for (Node node : nodes) {
            result.add(AttributeHelper.getPositionVec2d(node));
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * @return the name of this class.
     * @author Jannik
     */
    @Override
    public String getName() {
        return "Do nothing";
    }

    /**
     * @return the description of this class.
     * @author Jannik
     */
    @Override
    public String getDescription() {
        return "Just work with the current positions of the nodes.";
    }

    /**
     * Gets a list of {@link Parameter}s this class provides.
     * It will be empty for this class.
     *
     * @return an empty list of Parameters.
     * @author Jannik
     * @see Algorithm#getParameters()
     */
    @Override
    public Parameter[] getParameters() {
        return new Parameter[0];
    }

    /**
     * This class does not have any parameters and
     * does not accept any.
     *
     * @param parameters these will always be ignored.
     * @author Jannik
     * @see Algorithm#setParameters(Parameter[])
     */
    @Override
    public void setParameters(Parameter[] parameters) {
        // no-op
    }
}
