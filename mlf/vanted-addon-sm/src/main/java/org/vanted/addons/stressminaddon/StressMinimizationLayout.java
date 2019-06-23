package org.vanted.addons.stressminaddon;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import org.AttributeHelper;
import org.BackgroundTaskStatusProvider;
import org.Vector2d;
import org.graffiti.attributes.Attribute;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractEditorAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.JComponentParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.view.View;
import org.vanted.addons.stressminaddon.util.ConnectedComponentsHelper;
import org.vanted.addons.stressminaddon.util.NodeValueMatrix;
import org.vanted.addons.stressminaddon.util.NullPlacer;
import org.vanted.addons.stressminaddon.util.ShortestDistanceAlgorithm;
import org.vanted.addons.stressminaddon.util.gui.EnableableNumberParameter;
import org.vanted.addons.stressminaddon.util.gui.ParameterizableSelectorParameter;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

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

    /** The {@link IntuitiveIterativePositionAlgorithm} to use. */
    private IterativePositionAlgorithm positionAlgorithm = new IntuitiveIterativePositionAlgorithm();
    /** The {@link InitialPlacer} to use. */
    private InitialPlacer initialPlacer = new PivotMDS();

    ///// Parameters and defaults /////
    // stop conditions
    /** Whether to use the stress epsilon by default.*/
    private static final boolean USE_STRESS_EPSILON_DEFAULT = true;
    /** The default value for the epsilon to use with stress function. */
    private static final double STRESS_EPSILON_DEFAULT = 0.0001;
    /** The epsilon to use with the stress function. */
    private double stressEpsilon = STRESS_EPSILON_DEFAULT;
    /** Whether to use the position change epsilon by default.*/
    private static final boolean USE_POSITION_CHANGE_EPSILON_DEFAULT = true;
    /** The default value for the epsilon to use with the difference between old and new distances. */
    private static final double POSITION_CHANGE_EPSILON_DEFAULT = 0.0001;
    /** The epsilon to use with the difference between old  new distances. */
    private double positionChangeEpsilon = POSITION_CHANGE_EPSILON_DEFAULT;
    /** Whether to use the max iterations stop criterion by default.*/
    private static final boolean USE_MAX_ITERATIONS_DEFAULT = true;
    /** The default value for the maximal iterations to make. */
    private static final int MAX_ITERATIONS_DEFAULT = 1_000;
    /** The maximal iterations to make.*/
    private long maxIterations = MAX_ITERATIONS_DEFAULT;
    // scaling and weight
    /** The default scaling factor for edges between the nodes (as fraction of the biggest node). */
    private static final double EDGE_SCALING_FACTOR_DEFAULT = 5.0;
    /** The scaling factor for the edges between the nodes (as fraction of the biggest node). */
    private double edgeScalingFactor = EDGE_SCALING_FACTOR_DEFAULT;
    /** The default constant scale factor for the calculated weight between two nodes. */
    private static final double WEIGHT_SCALING_FACTOR_DEFAULT = 1.0;
    /** The constant scale factor for the calculated weight between two nodes. */
    private double weightScalingFactor = WEIGHT_SCALING_FACTOR_DEFAULT;
    /** The default power of the distance for the calculated weight between two nodes. */
    private static final double WEIGHT_POWER_DEFAULT = -2.0;
    /** The constant power of the distance for the calculated weight between two nodes. */
    private double weightPower = WEIGHT_POWER_DEFAULT;
    // looks
    /** Whether to remove edge bends by default.*/
    private static final Boolean REMOVE_EDGE_BENDS_DEFAULT = Boolean.TRUE;
    /** Whether to remove edge bends.*/
    private boolean removeEdgeBends = REMOVE_EDGE_BENDS_DEFAULT;
    /** Whether the intermediate steps should be undoable by default. */
    private static final Boolean INTERMEDIATE_UNDOABLE_DEFAULT = Boolean.FALSE;
    /** Whether the intermediate steps should be undoable. */
    private boolean intermediateUndoable = INTERMEDIATE_UNDOABLE_DEFAULT;
    /** Whether the algorithm should be animated by default. */
    private static final Boolean DO_ANIMATIONS_DEFAULT = Boolean.TRUE;
    /** Whether the algorithm should be animated. */
    private boolean doAnimations = DO_ANIMATIONS_DEFAULT;
    // multi threading
    /** Whether the algorithm should run in a background task by default. */
    private static final Boolean BACKGROUND_TASK_DEFAULT = Boolean.TRUE;
    /** Whether the algorithm should run in a background task. */
    private boolean backgroundTask = BACKGROUND_TASK_DEFAULT;
    /** Whether the algorithm should use multiple threads by default. */
    private static final Boolean MULTIPLE_THREADS_DEFAULT = Boolean.TRUE;
    /** Whether the algorithm should use multiple threads. */
    private boolean multipleThreads = MULTIPLE_THREADS_DEFAULT;
    /** MultiLevelFramework compatibility mode. */
    private boolean compatibilityMLF = false;

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
        return "<html>Performs the stress minimization layout on<br>" +
                "the given graph.<br>" +
                "A faster runtime (configurable below) may result<br>" +
                "in a worse layout.</html>";
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
        assert (!this.intermediateUndoable || this.doAnimations); // intermediateUndoable => doAnimations
        // get nodes to work with
        ArrayList<Node> pureNodes;
        if (selection.isEmpty()) {
            pureNodes = new ArrayList<>(GraphHelper.getVisibleNodes(graph.getNodes()));
        } else {
            pureNodes = new ArrayList<>(GraphHelper.getVisibleNodes(selection.getNodes()));
        }

        Runnable task = () -> {
            startTime = System.currentTimeMillis();
            System.out.println((System.currentTimeMillis() - startTime) + " SM: " + (status = "Start (n = " + pureNodes.size() + ")"));
            // remove bends
            if (this.removeEdgeBends) {
                GraphHelper.removeBendsBetweenSelectedNodes(pureNodes, true);
            }
            System.out.println((System.currentTimeMillis() - startTime) + " SM: " + (status = "Getting connected components..."));
            // save old positions if the user only wants one position update
            ArrayList<Vector2d> oldPositions = new ArrayList<>(0);
            if (!this.intermediateUndoable) {
                oldPositions.ensureCapacity(pureNodes.size());
                for (Node node : pureNodes) {
                    oldPositions.add(AttributeHelper.getPositionVec2d(node));
                }
            }
            // get connected components and layout them
            final Set<List<Node>> connectedComponents = ConnectedComponentsHelper.getConnectedComponents(pureNodes);


            if (this.doAnimations) {
                ConnectedComponentsHelper.layoutConnectedComponents(connectedComponents, this.intermediateUndoable);
            }
            System.out.println((System.currentTimeMillis() - startTime) + " SM: " + (status = "Got connected components. (" + connectedComponents.size() + ")"));

            // TODO add random initial layout

            List<WorkUnit> workUnits = new ArrayList<>(connectedComponents.size());

            // prepare (set helper attribute)
            int id = 0;
            ConcurrentMap<Node, Vector2d> move = new ConcurrentHashMap<>();
            for (List<Node> component : connectedComponents) {
                // Set positions attribute for hopefully better handling
                System.out.println((System.currentTimeMillis() - startTime) + " SM: " + (status = "Preparing connected components for algorithm.") + " (n = " + component.size() + ")");
                for (int pos = 0; pos < component.size(); pos++) {
                    component.get(pos).setInteger(StressMinimizationLayout.INDEX_ATTRIBUTE, pos);
                }
                WorkUnit unit = new WorkUnit(component, id++);
                System.out.println(unit.toStringShort());

                workUnits.add(unit);

                if (this.doAnimations) {
                    for (int idx = 0; idx < component.size(); idx++) {
                        move.put(component.get(idx), unit.currentPositions.get(idx));
                    }
                }
            }
            if (this.doAnimations) {
                if (this.intermediateUndoable) {
                    GraphHelper.applyUndoableNodePositionUpdate(new HashMap<>(move), "Initial layout");
                } else {
                    graph.getListenerManager().transactionStarted(this);
                    for (Map.Entry<Node, Vector2d> entry : move.entrySet()) {
                        AttributeHelper.setPosition(entry.getKey(), entry.getValue());
                    }
                    graph.getListenerManager().transactionFinished(this);
                }
            }
            System.out.println((System.currentTimeMillis() - startTime) + " SM: " + (status = "Finished preparing"));

            boolean someoneIsWorking = true;

            ExecutorService executor = Executors.newFixedThreadPool(Math.min(Runtime.getRuntime().availableProcessors(), connectedComponents.size()));
            List<Callable<Object>> todo = new ArrayList<>(workUnits.size());
            // iterate
            for (int iteration = 1; keepRunning.get() && someoneIsWorking && iteration <= maxIterations; ++iteration) {
                System.out.println((System.currentTimeMillis() - startTime) + " SM: " + (status = "Iteration " + iteration));
                someoneIsWorking = false;
                move.clear();
                // execute every work unit
                for (WorkUnit unit : workUnits) {
                    if (unit.hasStopped) {
                        continue;
                    }
                    todo.add(Executors.callable(() -> {
                        unit.nextIteration();

                        if (this.doAnimations) {
                            System.out.println((System.currentTimeMillis() - startTime) + " SM@"+unit.id+": Prepare result.");
                            HashMap<Node, Vector2d> result = new HashMap<>();
                            for (int idx = 0; idx < unit.nodes.size(); idx++) {
                                result.put(unit.nodes.get(idx), unit.currentPositions.get(idx));
                            }
                            move.putAll(result);
                        }
                        System.out.println((System.currentTimeMillis() - startTime) + " SM@"+unit.id+": Iteration finished.");
                    }));
                    // something was worked on
                    someoneIsWorking = true;
                    //System.out.println(unit.toStringShort());
                }

                try {
                    if (keepRunning.get()) {
                        if (this.multipleThreads) {
                            executor.invokeAll(todo);
                        } else {
                            for (Callable<Object> callable : todo) {
                                callable.call();
                            }
                        }
                    }
                    todo.clear();
                } catch (Exception e) {
                    keepRunning.set(false);
                    e.printStackTrace();
                }

                if (this.doAnimations) {
                    if (this.intermediateUndoable) {
                        GraphHelper.applyUndoableNodePositionUpdate(new HashMap<>(move), "Do iteration " + iteration);
                    } else {
                        graph.getListenerManager().transactionStarted(this);
                        for (Map.Entry<Node, Vector2d> entry : move.entrySet()) {
                            AttributeHelper.setPosition(entry.getKey(), entry.getValue());
                        }
                        graph.getListenerManager().transactionFinished(this);
                    }
                }
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

            if (!doAnimations) { // => !intermediateUndoable
                graph.getListenerManager().transactionStarted(this);
                for (WorkUnit unit : workUnits) { // prepare for layout connected components
                    for (int idx = 0; idx < unit.nodes.size(); idx++) {
                        AttributeHelper.setPosition(unit.nodes.get(idx), unit.currentPositions.get(idx));
                    }
                }
                graph.getListenerManager().transactionFinished(this);
            }

            ConnectedComponentsHelper.layoutConnectedComponents(connectedComponents, this.intermediateUndoable);
            // create only one undo (or nothing in compatibility mode)
            if (!compatibilityMLF && !this.intermediateUndoable) {
                graph.getListenerManager().transactionStarted(this);
                for (int idx = 0; idx < pureNodes.size(); idx++) {
                    Node node = pureNodes.get(idx);
                    move.put(node, AttributeHelper.getPositionVec2d(node));
                    // reset to old position so that only undo has correct starting positions
                    AttributeHelper.setPosition(node, oldPositions.get(idx));
                }
                graph.getListenerManager().transactionFinished(this);
                GraphHelper.applyUndoableNodePositionUpdate(new HashMap<>(move), "Stress Minimization");
            }

            startTime = System.currentTimeMillis() - startTime;
            System.out.println(startTime + " SM: " + (status = "Finished.") + " Took " + (startTime/1000.0) + "s");
            keepRunning.set(true);
        };
        // run!
        if (this.backgroundTask) {
            BackgroundTaskHelper.issueSimpleTask("Stress Minimization", "Init", task, null, this, 10);
        } else {
            task.run();
        }


    }

    /**
     * @return the parameters the algorithm uses.
     *
     * @author Jannik
     */
    @Override
    public Parameter[] getParameters() {

        // initial placers
        InitialPlacer[] initialPlacers = new InitialPlacer[] {new PivotMDS(), new NullPlacer()};
        // iterative algorithms
        IterativePositionAlgorithm[] iterativeAlgorithms = new IterativePositionAlgorithm[] {new IntuitiveIterativePositionAlgorithm()};

        Parameter[] result = new Parameter[] {
                new JComponentParameter(new JPanel(), "<html><u>Stop criterion</u></html>",
                        "<html>Select one or multiple stop criterion for the algorithm.<br>This can be used to influence running time and quality.</html>"), //hacky section header
                EnableableNumberParameter.canBeEnabledDisabled(STRESS_EPSILON_DEFAULT,
                        0.0, Double.MAX_VALUE, 0.0001,
                        USE_STRESS_EPSILON_DEFAULT, "Used", "Not used",
                        "Stress change",
                        "<html>Stop the iterations if the change in stress is smaller than smaller than this value.<br>" +
                                "The algorithm will only stop on this criterion, if this is enabled.</html>"),
                EnableableNumberParameter.canBeEnabledDisabled(POSITION_CHANGE_EPSILON_DEFAULT,
                        0.0, Double.MAX_VALUE, 0.0001,
                        USE_POSITION_CHANGE_EPSILON_DEFAULT, "Used", "Not used",
                        "Position change",
                        "<html>Stop the iterations if the maximum change in positions of the nodes is smaller " +
                                "than smaller than this value.<br>" +
                                "The algorithm will only stop on this criterion, if this is enabled.</html>"),
                EnableableNumberParameter.canBeEnabledDisabled(MAX_ITERATIONS_DEFAULT,
                        0, Integer.MAX_VALUE, 1,
                        USE_MAX_ITERATIONS_DEFAULT, "Used", "Not Used",
                        "Max iterations",
                        "<html>Stop after provided number of iterations.<br>" +
                                "The algorithm will only stop on this criterion, if this is enabled.</html>"),
                new JComponentParameter(new JPanel(), "<html><u>Weights</u></html>",
                        "<html>The weight function is used to determine how much distance nodes can influence each other.</html>"), //hacky section header
                EnableableNumberParameter.alwaysEnabled(WEIGHT_SCALING_FACTOR_DEFAULT, 0.0, Double.MAX_VALUE, 0.5,
                        "Scaling factor", "<html>The constant factor &alpha; used in the weight function that derives a weight<br>" +
                                "between two given nodes <i>i</i> and <i>j</i> from their graph theoretical distance &delta;.<br>" +
                                "The weight function is &alpha;&delta;<sup>&beta;</sup><sub>ij</sub>.</html>"),
                EnableableNumberParameter.alwaysEnabled(WEIGHT_POWER_DEFAULT, -Double.MAX_VALUE, Double.MAX_VALUE, 1.0,
                        "Distance power", "<html>The power for the distance &beta; used in the weight function that derives a weight<br>" +
                                "between two given nodes <i>i</i> and <i>j</i> from their graph theoretical distance &delta;.<br>" +
                                "The weight function is &alpha;&delta;<sup>&beta;</sup><sub>ij</sub>.</html>"),
                new JComponentParameter(new JPanel(), "<html><u>Initial layout</u></html>",
                        "<html>The layout to apply before the actual algorithm is run.</html>"), //hacky section header
                ParameterizableSelectorParameter.getFromList(0, new ArrayList<>(Arrays.asList(initialPlacers)),
                        selection, "Select layout",
                        "<html>The layout to apply before the actual algorithm is run.</html>"),
                new JComponentParameter(new JPanel(), "<html><u>Iterative algorithm</u></html>",
                        "<html>The algorithm that should be used to calculate the new positions of in every iteration step.</html>"), //hacky section header
                ParameterizableSelectorParameter.getFromList(0, new ArrayList<>(Arrays.asList(iterativeAlgorithms)),
                        selection, "Select algorithm",
                        "<html>The algorithm that should be used to calculate the new positions of in every iteration step.</html>"),
                new JComponentParameter(new JPanel(), "<html><u>General layout</u></html>",
                        "<html>Some general settings for the layouter.</html>"), //hacky section header
                EnableableNumberParameter.alwaysEnabled(EDGE_SCALING_FACTOR_DEFAULT, 0.0, Double.MAX_VALUE, 0.5,
                        "Edge scaling factor", "<html>The amount of space that the algorithm tries to ensure between two nodes<br>" +
                                "(length of the edges) as faction of the size of the largest node encountered.</html>"),
                new BooleanParameter(REMOVE_EDGE_BENDS_DEFAULT, "Remove edge bends",
                        "<html>Remove edge bends before starting the algorithm.<br><b>Highly recommended</b> because the algorithm does not " +
                                "move edge bends.<br>Always undoable as extra undo.</html>"),
                new BooleanParameter(INTERMEDIATE_UNDOABLE_DEFAULT, "Iterations undoable",
                        "<html>Should each iteration step be undoable (only recommended for small iteration sizes).<br>" +
                                "This parameter implies “Animate iterations”.</html>"),
                new BooleanParameter(DO_ANIMATIONS_DEFAULT, "Animate iterations",
                        "<html>Whether every iteration should update the node positions.</html>"),
                new JComponentParameter(new JPanel(), "<html><u>Parallelism (advanced)</u></html>",
                        "<html>Sets threading options for running.<br><b>Should not be changed by inexperienced users.</b></html>"), //hacky section header
                new BooleanParameter(BACKGROUND_TASK_DEFAULT, "Run in background",
                        "<html>Create a background task and run the algorithm from there.<br>" +
                                "<b>Highly recommended:</b> If this is disabled <i>VANTED</i> will freeze until the algorithm is complete.</html>"),
                new BooleanParameter(MULTIPLE_THREADS_DEFAULT, "Use parallelism",
                        "<html>Create multiple threads to run the algorithm from.<br>" +
                                "Increases overall performance in most cases.</html>"),

        };
        return result;
    }

    /**
     * @param params set the parameters.
     */
    @Override
    @SuppressWarnings("unchecked") // will always work (see above) if calling class does not temper with args too much
    public void setParameters(Parameter[] params) {
        EnableableNumberParameter<Double> doubleParameter;
        EnableableNumberParameter<Integer> integerParameter;
        ParameterizableSelectorParameter selectorParameter;
        // Stress epsilon
        doubleParameter = (EnableableNumberParameter<Double>) params[1].getValue();
        if (doubleParameter.isEnabled()) {
            if (doubleParameter.getValue() == 0.0) {
                this.stressEpsilon = Double.MIN_VALUE;
            } else {
                this.stressEpsilon = doubleParameter.getValue();
            }
        } else {
            this.stressEpsilon = Double.NEGATIVE_INFINITY;
        }
        // Position epsilon
        doubleParameter = (EnableableNumberParameter<Double>) params[2].getValue();
        if (doubleParameter.isEnabled()) {
            if (doubleParameter.getValue() == 0.0) {
                this.positionChangeEpsilon = Double.MIN_VALUE;
            } else {
                this.positionChangeEpsilon = doubleParameter.getValue();
            }
        } else {
            this.positionChangeEpsilon = Double.NEGATIVE_INFINITY;
        }
        // Max iterations
        integerParameter = ((EnableableNumberParameter<Integer>) params[3].getValue());
        if (integerParameter.isEnabled()) {
            this.maxIterations = integerParameter.getValue();
        } else {
            this.maxIterations = Long.MAX_VALUE;
        }
        // weight variables
        doubleParameter = (EnableableNumberParameter<Double>) params[5].getValue();
        this.weightScalingFactor = doubleParameter.getValue();
        doubleParameter = (EnableableNumberParameter<Double>) params[6].getValue();
        this.weightPower = doubleParameter.getValue();
        // initial layout
        selectorParameter = ((ParameterizableSelectorParameter) params[8].getValue());
        this.initialPlacer = ((InitialPlacer) selectorParameter.getSelectedParameterizable());
        this.initialPlacer.setParameters(selectorParameter.getUpdatedParameters());
        // position algorithm
        selectorParameter = ((ParameterizableSelectorParameter) params[10].getValue());
        this.positionAlgorithm = ((IterativePositionAlgorithm) selectorParameter.getSelectedParameterizable());
        this.positionAlgorithm.setParameters(selectorParameter.getUpdatedParameters());
        // General settings
        doubleParameter = (EnableableNumberParameter<Double>) params[12].getValue();
        if (doubleParameter.getValue() == 0.0) {
            this.edgeScalingFactor = Double.MIN_VALUE;
        } else {
            this.edgeScalingFactor = doubleParameter.getValue();
        }
        this.removeEdgeBends = ((BooleanParameter) params[13]).getBoolean();
        // Intermediates undoable
        this.intermediateUndoable = ((BooleanParameter) params[14]).getBoolean();
        // Do animation
        this.doAnimations = ((BooleanParameter) params[15]).getBoolean();
        if (intermediateUndoable) { // intermediateUndoable => doAnimations
            doAnimations = true;
        }
        // Threading
        this.backgroundTask = ((BooleanParameter) params[17]).getBoolean();
        this.multipleThreads = ((BooleanParameter) params[18]).getBoolean();

        // MultiLevelFramework compatibility
        if (graph.getBoolean("GRAPH_IS_MLF_COARSENING_LEVEL")) {
            this.compatibilityMLF = true;
            if (!graph.getBoolean("GRAPH_IS_MLF_COARSENING_TOP_LEVEL")) {
                this.initialPlacer = new NullPlacer(); // we are not at top level
            }
            this.intermediateUndoable = false;
            this.doAnimations = false;
            this.backgroundTask = false;
        }
    }

    /*
     * The category of this layouter.
     */
    @Override
    public String getCategory() {
        return "Layout";
    }

    /**)
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
    protected class WorkUnit {

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
            this.distances = ShortestDistanceAlgorithm.calculateShortestPaths(nodes, Integer.MAX_VALUE, multipleThreads); // TODO make configurable
            System.out.println((System.currentTimeMillis() - startTime) + " SM@"+(status = id+": Calculate scaling factor..."));
            final double scalingFactor = ConnectedComponentsHelper.getMaxNodeSize(nodes);
            // scale for better display
            this.distances.apply(x -> x*scalingFactor*StressMinimizationLayout.this.edgeScalingFactor);

            System.out.println((System.currentTimeMillis() - startTime) + " SM@"+(status = id+": Calculate initial layout..."));
            //this.currentPositions = nodes.stream().map(AttributeHelper::getPositionVec2d).collect(Collectors.toList());
            this.currentPositions = initialPlacer.calculateInitialPositions(nodes, this.distances);
            // calculate weight only before it's needed (to save some memory for the initial layout)
            System.out.println((System.currentTimeMillis() - startTime) + " SM@"+(status = id+": Calculate weights..."));
            this.weights = this.distances.clone().apply(x -> weightScalingFactor*Math.pow(x, weightPower));
            // calculate first values
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
        public List<Vector2d> nextIteration() {
            if (hasStopped) return this.currentPositions;

            // calculate new positions
            System.out.println((System.currentTimeMillis() - startTime) + " SM@"+this.id+": Iterate new positions...");
            List<Vector2d> newPositions = positionAlgorithm.nextIteration(
                    this.nodes, this.currentPositions, this.distances, this.weights);

            // check position change
            System.out.println((System.currentTimeMillis() - startTime) + " SM@"+this.id+": Check position stop condition...");
            if (differencePositionsSmallerEpsilon(newPositions, this.currentPositions, positionChangeEpsilon)) {
                this.hasStopped = true;
                System.out.println((System.currentTimeMillis() - startTime) + " SM@"+this.id+": Iteration finished.");
                return this.currentPositions = newPositions;
            }

            // calculate new stress
            System.out.println((System.currentTimeMillis() - startTime) + " SM@"+this.id+": Calculate new stress...");
            double newStress = StressMinimizationLayout.calculateStress(
                    this.nodes, newPositions, this.distances, this.weights);

            // shall we stop? is the change in stress or position is smaller than the given epsilon
            System.out.println((System.currentTimeMillis() - startTime) + " SM@"+this.id+": Check stress stop condition...");
            if ((this.currentStress - newStress)/this.currentStress < stressEpsilon) {
               this.hasStopped = true;
            }

            // update with new values
            this.currentPositions = newPositions;
            this.currentStress = newStress;
            return this.currentPositions;
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
                    ", weights=\n" + weights +
                    ", currentStress=" + currentStress +
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
