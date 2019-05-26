package org.vanted.addons.stressminaddon;

import org.AttributeHelper;
import org.Vector2d;
import org.graffiti.attributes.Attribute;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractEditorAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.view.View;
import org.vanted.addons.stressminaddon.util.NodeValueMatrix;

import java.util.*;

/**
 * Implements a version of a stress minimization add-on that can be used
 * in VANTED.
 */
public class StressMinimizationLayout extends AbstractEditorAlgorithm {

    /**
     * Path of an attribute that is set to the index of an node.
     */
    public static final String INDEX_ATTRIBUTE =
            "StressMinimization" + Attribute.SEPARATOR + "index";

    /**
     * Creates a new {@link StressMinimizationLayout} object.
     */
    public StressMinimizationLayout() {
        super();
    }

    @Override
    public String getDescription() {
        return ""; // TODO
    }

    /**
     * Returns the name of the algorithm.
     *
     * @return the name of the algorithm
     */
    public String getName() {
        return "Stress Minimization";
    }

    /**
     * Checks, if a graph was given.
     *
     * @throws PreconditionException
     *            if no graph was given during algorithm invocation or the
     *            radius is negative
     */
    @Override
    public void check() throws PreconditionException {
        if (graph == null) {
            throw new PreconditionException("No graph available!");
        }
        if (graph.getNumberOfNodes() <= 0) {
            throw new PreconditionException(
                    "The graph is empty. Cannot run layouter.");
        }
    }

    /**
     * Performs the layout.
     */
    public void execute() {
        ArrayList<Node> pureNodes;
        if (selection.isEmpty()) {
            pureNodes = new ArrayList<>(graph.getNodes());
        } else {
            pureNodes = new ArrayList<>(selection.getNodes());
        }

        List<Node> nodes = Collections.unmodifiableList(pureNodes);
        // Set positions attribute for hopefully better handling
        for (int pos = 0; pos < nodes.size(); pos++) {
            nodes.get(pos).setInteger(StressMinimizationLayout.INDEX_ATTRIBUTE, pos);
            System.out.println(nodes.get(pos).getAttribute(INDEX_ATTRIBUTE));
        }

        //////////////////////////////
        // TODO implement algorithm //
        //////////////////////////////


        // Reset attributes
        for (Node node : nodes) {
            node.removeAttribute(StressMinimizationLayout.INDEX_ATTRIBUTE);
        }
    }

    @Override
    public Parameter[] getParameters() {
        // TODO
        return null;
    }

    @Override
    public void setParameters(Parameter[] params) {
        // TODO
    }

    /*
     * (non-Javadoc)
     * @see org.graffiti.plugin.algorithm.Algorithm#getCategory()
     */
    @Override
    public String getCategory() {
        return "Layout";
    }

    /**
     * This method is important, because it will move the algorithm to the layout-tab of Vanted
     */
    @Override
    public boolean isLayoutAlgorithm() {
        return true;
    }

    public boolean activeForView(View v) {
        return v != null;
    }

    /**
     * Calculates the stress function for a given set of nodes as given by the following formular
     * <p>
     *    \sum_{i < j} w_{ij}(d_{ij} - ||p_i - p_j ||)^2
     * </p>
     * with {@code w} being the node weights, {@code d} being the gr. theo. distance,
     * p being the positions and ||.|| being the Euclidian distance.
     *
     * @param nodes the nodes to be used.
     * @param distances the graph theoretical distances between the nodes
     * @param weights the weighs of each node.
     *
     * @return the stress value.
     */
    private double calculateStress(final List<Node> nodes,
                                   final NodeValueMatrix distances,
                                   final NodeValueMatrix weights) {
        // get needed distances
        final List<Vector2d> positions = new ArrayList<>(nodes.size());
        for (Node n : nodes) {
            positions.add(AttributeHelper.getPosition(n));
        }

        double result = 0.0;
        Vector2d posI, posJ; // posittion sof nodes in the loop
        double diffX, diffY; // taxi distance between them
        double euclidian; // holds euclidian distance
        double parenthesis; // holds contents of parentheses

        // i < j is short for a double sum
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i+1; j < nodes.size(); j++) {
                // inner parenthesis
                //  euclidian distance
                Vector2d posI = positions.get(i), posJ = positions.get(j);
                diffX = posI.x - posJ.x;
                diffY = posI.y - posJ.y;
                euclidian = Math.sqrt(diffX*diffX + diffY*diffY);
                // inner sum term
                parenthesis = distances.get(i, j) - euclidian;

                result += weights.get(i, j)*parenthesis*parenthesis;
            }
        }

        return result;

    }
}
