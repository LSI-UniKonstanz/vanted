package org.vanted.addons.stressminaddon;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.random.RandomLayouterAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import org.AttributeHelper;
import org.BackgroundTaskStatusProvider;
import org.Vector2d;
import org.graffiti.attributes.Attribute;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractEditorAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.view.View;
import org.vanted.addons.stressminaddon.util.ConnectedComponentsHelper;
import org.vanted.addons.stressminaddon.util.NodeValueMatrix;
import org.vanted.addons.stressminaddon.util.ShortestDistanceAlgorithm;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Implements a version of a stress minimization add-on that can be used
 * in VANTED.
 */
public class StressMinimizationLayout extends AbstractEditorAlgorithm  implements BackgroundTaskStatusProvider {

    /** The time in ms the algorithm started. Used to calculate elapsed time. */
    long startTime;

    /** The current displayed status of the algorithm. */
    volatile String status = "";
    /** Whether the algorithm was not stopped by the user. */
    volatile AtomicBoolean keepRunning = new AtomicBoolean(true);

    /**
     * Path of an attribute that is set to the index of an node.
     */
    public static final String INDEX_ATTRIBUTE =
            "StressMinimization" + Attribute.SEPARATOR + "index";

    public static final IterativePositionAlgorithm positionAlgorithm = new IntuitiveIterativePositionAlgorithm();

    /** The epsilon to use with the stress function with a reasonable default. */
    private double stressEpsilon = 0.0001; // TODO make configurable
    /** The epsilon to use with the new distances with a reasonable default. */
    private double positionChangeEpsilon = 0.0001; // TODO make configurable
    /** The maximal iterations to make. */
    private int maxIterations = 1_000_000; // TODO make configurable

    /**
     * Creates a new {@link StressMinimizationLayout} object.
     */
    public StressMinimizationLayout() {
        super();
    }

    /**
     * @return the description of the algorithm.
     */
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

        BackgroundTaskHelper.issueSimpleTask("Stress Minimization", "Init", () -> {
            startTime = System.currentTimeMillis();
            System.out.println((System.currentTimeMillis() - startTime) + " SM: " + (status = "Start (n = " + pureNodes.size() + ")"));
            System.out.println((System.currentTimeMillis() - startTime) + " SM: " + (status = "Getting connected components..."));
            // get connected components and layout them
            final Set<List<Node>> connectedComponents = ConnectedComponentsHelper.getConnectedComponents(pureNodes);
            ConnectedComponentsHelper.layoutConnectedComponents(connectedComponents);
            System.out.println((System.currentTimeMillis() - startTime) + " SM: " + (status = "Got connected components. (" + connectedComponents.size() + ")"));

            // TODO temporary
            System.out.println((System.currentTimeMillis() - startTime) + " SM: " + (status = "Running random..."));
            RandomLayouterAlgorithm rla = new RandomLayouterAlgorithm();
            rla.attach(graph, selection);
            rla.execute();
            System.out.println((System.currentTimeMillis() - startTime) + " SM: " + (status = "Finished random."));

            List<WorkUnit> workUnits = new ArrayList<>(connectedComponents.size());

            // prepare (set helper attribute)
            int id = 0;
            for (List<Node> component : connectedComponents) {
                // Set positions attribute for hopefully better handling
                System.out.println((System.currentTimeMillis() - startTime) + " SM: " + (status = "Preparing connected components for algorithm.") + " (n = " + component.size() + ")");
                for (int pos = 0; pos < component.size(); pos++) {
                    component.get(pos).setInteger(StressMinimizationLayout.INDEX_ATTRIBUTE, pos);
                }
                WorkUnit unit = new WorkUnit(component, id++);
                System.out.println(unit.toStringShort());

                workUnits.add(unit);
            }
            System.out.println((System.currentTimeMillis() - startTime) + " SM: " + (status = "Finished preparing"));

            boolean someoneIsWorking = true;

            ExecutorService executor = Executors.newWorkStealingPool(Runtime.getRuntime().availableProcessors());
            List<Callable<Object>> todo = new ArrayList<>(workUnits.size());
            // iterate
            ConcurrentMap<Node, Vector2d> move = new ConcurrentHashMap<>();
            for (int iteration = 1; keepRunning.get() && someoneIsWorking && iteration <= maxIterations; ++iteration) {
                System.out.println((System.currentTimeMillis() - startTime) + " SM: " + (status = "Iteration " + iteration));
                someoneIsWorking = false;
                move.clear();
                // execute every work unit
                for (WorkUnit unit : workUnits) {
                    if (unit.hasStopped) {
                        continue;
                    }
                    todo.add(Executors.callable(() -> move.putAll(unit.nextIteration())));
                    // something was worked on
                    someoneIsWorking = true;
                    //System.out.println(unit.toStringShort());
                }

                try {
                    if (keepRunning.get()) {
                        executor.invokeAll(todo);
                    }
                    todo.clear();
                } catch (InterruptedException e) {
                    keepRunning.set(false);
                    e.printStackTrace();
                }

                GraphHelper.applyUndoableNodePositionUpdate(new HashMap<>(move), "Do iteration.");
            }
            System.out.println((System.currentTimeMillis() - startTime) + " SM: " + (status = "Finish work."));


            System.out.println((System.currentTimeMillis() - startTime) + " SM: " + (status = "Postprocessing..."));
            // finish (remove helper attribute)
            for (List<Node> connectedComponent : connectedComponents) {
                // Reset attributes
                for (Node node : connectedComponent) {
                    node.removeAttribute(StressMinimizationLayout.INDEX_ATTRIBUTE);
                }
            }

            ConnectedComponentsHelper.layoutConnectedComponents(connectedComponents);
            startTime = System.currentTimeMillis() - startTime;
            System.out.println(startTime + " SM: " + (status = "Finished.") + " Took " + (startTime/1000.0) + "s");
            keepRunning.set(true);

        }, null, this, 10);


    }

    /**
     * @return the parameters the algorithm uses.
     */
    @Override
    public Parameter[] getParameters() {
        // TODO
        return null;
    }

    /**
     * @param params set the parameters.
     */
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
     * @param weights the weights of each node.
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

    /**
     * Returns a status message on what is going on. WARNING: This method must be
     * Thread-Safe!
     *
     * @return A status message, or null if not needed.
     */
    @Override
    public String getCurrentStatusMessage1() {
        return status;
    }

    /**
     * If this method is called on the status provider, the linked work task should
     * stop its execution as soon as possible.
     */
    @Override
    public void pleaseStop() {
        keepRunning.set(false);
    }


    /**
     * Represents a unit (in most cases a connected component) the stress minimization
     * algorithm works on.
     * All data relevant for the session will be saved and the algorithm will be run
     * for every WorkUnit separately.
     *
     * @author Jannik
     */
    class WorkUnit {

        /** The identifier of this unit. Should be unique. */
        final int id;
        /** The nodes to work on. */
        final List<Node> nodes;
        /** The graph theoretical distances to work with. These will not change once calculated. */
        final NodeValueMatrix distances;
        /** The weights used for every node pair. These will not change once calculated. */
        final NodeValueMatrix weights;

        /**
         * The current calculated stress from the {@link #calculateStress(List, List, NodeValueMatrix, NodeValueMatrix)}
         * method.
         */
        double currentStress;
        /** The current positions of the nodes. */
        List<Vector2d> currentPositions;

        /** Whether the stress minimization has terminated on this {@link WorkUnit}. */
        boolean hasStopped;

        /**
         * Creates a new {@link WorkUnit} and initializes all relevant fields like <code>distances</code>,
         * <code>weights</code>, <code>currentStress</code> and <code>currentPositions</code>.
         *
         * @param nodes the nodes this {@link WorkUnit} shall work on.
         * @param id the identifier of this {@link WorkUnit}. Shall be unique.
         *
         * @author Jannik
         */
        public WorkUnit(final List<Node> nodes, final int id) {
            this.id = id;
            this.nodes = nodes;
            System.out.println((System.currentTimeMillis() - startTime) + " SM@"+(status = id+": Calculate distances..."));
            this.distances = new ShortestDistanceAlgorithm().getShortestPaths(nodes, Integer.MAX_VALUE);
            System.out.println((System.currentTimeMillis() - startTime) + " SM@"+(status = id+": Calculate scaling factor..."));
            final double scalingFactor = ConnectedComponentsHelper.getMaxNodeSize(nodes);
            // scale for better display TODO make configurable
            this.distances.apply(x -> x*scalingFactor*5);
            System.out.println((System.currentTimeMillis() - startTime) + " SM@"+(status = id+": Calculate weights..."));
            this.weights = this.distances.clone().apply(x -> 1/(x*x)); // derive weights TODO make configurable

            // TODO implement preprocessing
            // calculate first values
            System.out.println((System.currentTimeMillis() - startTime) + " SM@"+(status = id+": Get positions..."));
            this.currentPositions = nodes.stream().map(AttributeHelper::getPositionVec2d).collect(Collectors.toList());
            System.out.println((System.currentTimeMillis() - startTime) + " SM@"+(status = id+": Calculate initial stress..."));
            this.currentStress = StressMinimizationLayout.calculateStress(nodes, this.currentPositions, this.distances, this.weights);
            this.hasStopped = false;
            System.out.println((System.currentTimeMillis() - startTime) + " SM@"+(status = id+": Preprocessing finished."));
        }

        /**
         * Perform the next iteration and return the positions to be changed
         * in a format accepted by {@link GraphHelper#applyUndoableNodePositionUpdate(HashMap, String)}.
         *
         * @return
         *      the new positions or an empty map if the {@link WorkUnit} has already stopped.
         *
         * @author Jannik
         */
        public Map<Node, Vector2d> nextIteration() {
            if (hasStopped) return Collections.emptyMap();

            // calculate new positions
            System.out.println((System.currentTimeMillis() - startTime) + " SM@"+this.id+": Iterate new positions...");
            List<Vector2d> newPositions = StressMinimizationLayout.positionAlgorithm.nextIteration(
                    this.nodes, this.distances, this.weights);

            // calculate new stress
            System.out.println((System.currentTimeMillis() - startTime) + " SM@"+this.id+": Calculate new stress...");
            double newStress = StressMinimizationLayout.calculateStress(
                    this.nodes, newPositions, this.distances, this.weights);

            // shall we stop? is the change in stress or position is smaller than the given epsilon
            System.out.println((System.currentTimeMillis() - startTime) + " SM@"+this.id+": Check stop condition...");
            if ((this.currentStress - newStress)/this.currentStress < stressEpsilon ||
                differencePositionsSmallerEpsilon(newPositions, this.currentPositions, positionChangeEpsilon)) {
               this.hasStopped = true;
            }

            // update with new values
            this.currentPositions = newPositions;
            this.currentStress = newStress;

            // create the output
            System.out.println((System.currentTimeMillis() - startTime) + " SM@"+this.id+": Prepare result...");
            HashMap<Node, Vector2d> result = new HashMap<>(this.nodes.size());
            for (int node = 0; node < this.nodes.size(); node++) {
                //System.out.println(this.nodes.get(node) + " to " + this.currentPositions.get(node));
                result.put(this.nodes.get(node), this.currentPositions.get(node));
            }
            System.out.println((System.currentTimeMillis() - startTime) + " SM@"+this.id+": Iteration finished.");
            return result;
        }

        /**
         * @return a string representation of the {@link WorkUnit}.
         * @author IntelliJ
         */
        @Override
        public String toString() {
            return "WorkUnit"+id+"{" +
                    "nodes=" + nodes +
                    ", distances=\n" + distances +
                    "weights=\n" + weights +
                    "currentStress=" + currentStress +
                    ", currentPositions=" + currentPositions +
                    ", hasStopped=" + hasStopped +
                    '}';
        }

        /**
         * @return a short string representation of the {@link WorkUnit}.
         * @author IntelliJ
         */
        public String toStringShort() {
            return "WorkUnit"+id+"{" +
                    "currentStress=" + currentStress +
                    ", hasStopped=" + hasStopped +
                    '}';
        }
    }

    // Unused methods from BackgroundTaskStatusProvider interface //
    /**
     * Returns the completion status. WARNING: This method must be Thread-Safe!
     *
     * @return A number from 0..100 which represents the completion status. If -1 is
     * returned, the progress bar is set to "indeterminate", which means,
     * that the progress bar will float from left to right and reverse.
     * (Useful if status can not be determined) Other values let the
     * progressbar disappear.
     */
    @Override
    public int getCurrentStatusValue() {
        return -1;
    }

    @Override
    public void setCurrentStatusValue(int value) {}
    @Override
    public double getCurrentStatusValueFine() { return -1; }
    @Override
    public String getCurrentStatusMessage2() { return null; }
    @Override
    public boolean pluginWaitsForUser() { return false; }
    @Override
    public void pleaseContinueRun() {}
    // End Methods from BackgroundTaskStatusProvider interface //
}
