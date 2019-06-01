package org.vanted.addons.stressminaddon;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.random.RandomLayouterAlgorithm;
import org.AttributeHelper;
import org.Vector2d;
import org.graffiti.attributes.Attribute;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractEditorAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.view.View;
import org.vanted.addons.stressminaddon.util.ConnectedComponentsHelper;
import org.vanted.addons.stressminaddon.util.MockShortestDistances;
import org.vanted.addons.stressminaddon.util.NodeValueMatrix;

import java.util.*;
import java.util.stream.Collectors;

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

    public static final IterativePositionAlgorithm positionAlgorithm = new IntuitiveIterativePositionAlgorithm();

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
        // get nodes to work with
        ArrayList<Node> pureNodes;
        if (selection.isEmpty()) {
            pureNodes = new ArrayList<>(GraphHelper.getVisibleNodes(graph.getNodes()));
        } else {
            pureNodes = new ArrayList<>(GraphHelper.getVisibleNodes(selection.getNodes()));
        }


        // get connected components and layout them
        final Set<List<Node>> connectedComponents = ConnectedComponentsHelper.getConnectedComponents(pureNodes);
        ConnectedComponentsHelper.layoutConnectedComponents(connectedComponents);

        // TODO temporary
        RandomLayouterAlgorithm rla = new RandomLayouterAlgorithm();
        rla.attach(graph, selection);
      //  rla.execute();

        List<WorkUnit> workUnits = new ArrayList<>(connectedComponents.size());

        // prepare
        for (List<Node> component : connectedComponents) {
            // Set positions attribute for hopefully better handling
            for (int pos = 0; pos < component.size(); pos++) {
                component.get(pos).setInteger(StressMinimizationLayout.INDEX_ATTRIBUTE, pos);
            }
            WorkUnit unit = new WorkUnit(component);
            //System.out.println(unit);
            //System.out.print("{ distances =\n" + unit.distances);
            //System.out.print("weights =\n" + unit.weights);
            //System.out.println("component = " + component.toString() +
            //        ", maxsize = " +ConnectedComponentsHelper.getMaxNodeSize(component) + "}");

            workUnits.add(new WorkUnit(component));
        }

        boolean someoneIsWorking = true;

        HashMap<Node, Vector2d> move = new HashMap<>();
        final int iterationMax = 1_000_000;
        for (int iteration = 1; someoneIsWorking && iteration <= iterationMax; ++iteration) {
            System.out.println("iteration = " + iteration);
            someoneIsWorking = false;
            move.clear();

            for (WorkUnit unit : workUnits) {
                if (unit.hasStopped)
                    continue;
                move.putAll(unit.nextIteration());

                someoneIsWorking = true;
                System.out.println(unit);
            }

            GraphHelper.applyUndoableNodePositionUpdate(move, "Do iteration.");
        }


        // end
        for (List<Node> connectedComponent : connectedComponents) {
            // Reset attributes
            for (Node node : connectedComponent) {
                node.removeAttribute(StressMinimizationLayout.INDEX_ATTRIBUTE);
            }
        }

        ConnectedComponentsHelper.layoutConnectedComponents(connectedComponents);
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
     * @param positions the positions to use for calculation
     * @param weights the weighs of each node.
     *
     * @return the stress value.
     *
     * @author Jannik
     */
    private static double calculateStress(final List<Node> nodes,
                                   final List<Vector2d> positions,
                                   final NodeValueMatrix distances,
                                   final NodeValueMatrix weights) {
        assert distances.getDimension() == nodes.size() && nodes.size() == weights.getDimension();
        // get needed distances

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
    private static boolean differencePositionsSmallerEpsilon(final List<Vector2d> oldPositions, final List<Vector2d> newPositions,
                                       final double epsilon) {
        assert oldPositions.size() == newPositions.size();
        
        for (int pos = 0; pos < oldPositions.size(); pos++) {
            if (oldPositions.get(pos).distance(newPositions.get(pos)) >= epsilon) {
                return false; // a counter example is found. no further calculation needed.
            }
        }
        return true;
    }

    class WorkUnit {
        final List<Node> nodes;
        final NodeValueMatrix distances;
        final NodeValueMatrix weights;

        double currentStress;
        List<Vector2d> currentPositions;

        boolean hasStopped;

        public WorkUnit(final List<Node> nodes) {
            this.nodes = nodes;
            this.distances = MockShortestDistances.getShortestPaths(nodes);
            final double scalingFactor = ConnectedComponentsHelper.getMaxNodeSize(nodes);
            this.distances.apply(x -> x*scalingFactor*5);
            this.weights = this.distances.clone().apply(x -> 1/(x*x));

            // TODO implement preprocessing
            this.currentPositions = nodes.stream().map(AttributeHelper::getPositionVec2d).collect(Collectors.toList());
            this.currentStress = StressMinimizationLayout.calculateStress(nodes, this.currentPositions, this.distances, this.weights);
            this.hasStopped = false;
        }

        public Map<Node, Vector2d> nextIteration() {
            if (hasStopped) return Collections.emptyMap();

            List<Vector2d> newPositions = StressMinimizationLayout.positionAlgorithm.nextIteration(
                    this.nodes, this.distances, this.weights);

            double newStress = StressMinimizationLayout.calculateStress(
                    this.nodes, newPositions, this.distances, this.weights);

            if ((this.currentStress - newStress)/this.currentStress < StressMinimizationLayout.this.stressEpsilon ||
                StressMinimizationLayout.differencePositionsSmallerEpsilon(newPositions, this.currentPositions,
                        StressMinimizationLayout.this.positionChangeEpsilon)) {
               this.hasStopped = true;
            }
            this.currentPositions = newPositions;
            this.currentStress = newStress;

            HashMap<Node, Vector2d> result = new HashMap<>(this.nodes.size());
            for (int node = 0; node < this.nodes.size(); node++) {
                System.out.println(this.nodes.get(node) + " to " + this.currentPositions.get(node));
                result.put(this.nodes.get(node), this.currentPositions.get(node));
            }
            return result;
        }

        @Override
        public String toString() {
            return "WorkUnit{" +
                    "currentStress=" + currentStress +
                    ", hasStopped=" + hasStopped +
                    '}';
        }
    }
}
