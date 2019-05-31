package org.vanted.addons.stressminaddon;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import org.AttributeHelper;
import org.Vector2d;
import org.graffiti.attributes.Attribute;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractEditorAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.view.View;
import org.vanted.addons.stressminaddon.util.ConnectedComponentsHelper;
import org.vanted.addons.stressminaddon.util.NodeValueMatrix;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    /** The epsilon to use with the stress function with a reasonable default. */
    private double stressEpsilon = 0.0001;
    /** The epsilon to use with the new distances with a reasonable default. */
    private double positionChangeEpsilon = 0.0001;

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
            pureNodes = new ArrayList<>(GraphHelper.getVisibleNodes(graph.getNodes()));
        } else {
            pureNodes = new ArrayList<>(GraphHelper.getVisibleNodes(selection.getNodes()));
        }


        // TODO layout components

        final Set<List<Node>> connectedComponents = ConnectedComponentsHelper.getConnectedComponents(pureNodes);
        for (List<Node> connectedComponent : connectedComponents) {
            // Set positions attribute for hopefully better handling
            for (int pos = 0; pos < connectedComponent.size(); pos++) {
                connectedComponent.get(pos).setInteger(StressMinimizationLayout.INDEX_ATTRIBUTE, pos);
                System.out.println(connectedComponent.get(pos).getAttribute(INDEX_ATTRIBUTE));
            }

            //////////////////////////////
            // TODO implement algorithm //
            //////////////////////////////


            // Reset attributes
            for (Node node : connectedComponent) {
                node.removeAttribute(StressMinimizationLayout.INDEX_ATTRIBUTE);
            }
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
     * <pre>
     *    \sum_{i < j} w_{ij}(d_{ij} - ||p_i - p_j ||)^2
     * </pre>
     * with {@code w} being the node weights, {@code d} being the gr. theo. distance,
     * p being the positions and {@code ||.||} being the Euclidean distance.
     *
     * All three arguments must have the same dimension/size.
     *
     * @param nodes the nodes to be used.
     * @param distances the graph theoretical distances between the nodes
     * @param weights the weighs of each node.
     *
     * @return the stress value.
     *
     * @author Jannik
     */
    private static double calculateStress(final List<Node> nodes,
                                   final NodeValueMatrix distances,
                                   final NodeValueMatrix weights) {
        assert distances.getDimension() == nodes.size() && nodes.size() == weights.getDimension();
        // get needed distances
        final List<Vector2d> positions = new ArrayList<>(nodes.size());
        for (Node n : nodes) {
            positions.add(AttributeHelper.getPositionVec2d(n));
        }

        double result = 0.0;
        double parenthesis; // holds contents of parentheses

        // i < j is short for a double sum
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i+1; j < nodes.size(); j++) {
                // inner sum term
                parenthesis = distances.get(i, j) - positions.get(i).distance(positions.get(j));
                result += weights.get(i, j)*parenthesis*parenthesis;
            }
        }

        return result;
    }

    /**
     * This method checks whether the difference in distance between two sets of positions (with must have the same size)
     * is below (<) a given threshold {@code epsilon}.
     *
     * @param oldPositions
     *      the first set of positions to be used.
     * @param newPositions
     *      the second set of positions to be used.
     * @param epsilon
     *      the threshold to be used.
     *
     * @return
     *      <code>true</code> if the difference between every pair of positions (with the same index) is strictly
     *      smaller than {@code epsilon}, <code>false</code> otherwise.
     *
     * @author Jannik
     */
    private static boolean differencePositionsSmallerEpsilon(final ArrayList<Vector2d> oldPositions, final ArrayList<Vector2d> newPositions,
                                       final double epsilon) {
        assert oldPositions.size() == newPositions.size();
        
        for (int pos = 0; pos < oldPositions.size(); pos++) {
            if (oldPositions.get(pos).distance(newPositions.get(pos)) >= epsilon) {
                return false; // a counter example is found. no further calculation needed.
            }
        }
        return true;
    }
}
