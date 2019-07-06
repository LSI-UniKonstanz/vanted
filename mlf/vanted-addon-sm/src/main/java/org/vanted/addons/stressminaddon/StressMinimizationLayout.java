package org.vanted.addons.stressminaddon;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import org.AttributeHelper;
import org.BackgroundTaskStatusProvider;
import org.Vector2d;
import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
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
import org.vanted.addons.stressminaddon.util.gui.Describable;
import org.vanted.addons.stressminaddon.util.gui.EnableableNumberParameter;
import org.vanted.addons.stressminaddon.util.gui.Parameterizable;
import org.vanted.addons.stressminaddon.util.gui.ParameterizableSelectorParameter;

import javax.swing.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Implements a version of a stress minimization add-on that can be used in VANTED.
 */
public class StressMinimizationLayout extends AbstractEditorAlgorithm implements Parameterizable, Describable {

    /** The current state of this {@link StressMinimizationLayout}, that is not used in a call of {@link #execute()}. */
    State state = new State();

    /** Whether the debug mode is enabled by default for all instances. This can be manually set for each state. */
    public static boolean debugModeDefault = false;

    /** Path of root attribute. */
    public static final String ROOT_ATTRIBUTE = "StressMinimization";
    /** Path of an attribute that is set to the index of an node on the node. */
    public static final String INDEX_ATTRIBUTE = ROOT_ATTRIBUTE + Attribute.SEPARATOR + "index";
    /** Path of an attribute that is set on a graph that is currently processed. */
    public static final String WORKING_ATTRIBUTE = ROOT_ATTRIBUTE + Attribute.SEPARATOR + "working";
    /** The smallest value that will be used instead of 0 for some parameters. */
    static final double SMALLEST_NON_ZERO_VALUE = 0.000000000000000001;

    // MultiLevelFramework support
    /** Name of attribute that signals the current graph is a coarsening level. */
    private static final String MLF_COMPATIBILITY_IS_COARSENING_LEVEL = "GRAPH_IS_MLF_COARSENING_LEVEL";
    /** Name of attribute that signals the current graph is the top coarsening level. */
    private static final String MLF_COMPATIBILITY_IS_TOP_COARSENING_LEVEL = "GRAPH_IS_MLF_COARSENING_TOP_LEVEL";
    /** Name of attribute that signals the current graph is the bottom coarsening level. */
    private static final String MLF_COMPATIBILITY_IS_BOTTOM_COARSENING_LEVEL = "GRAPH_IS_MLF_COARSENING_BOTTOM_LEVEL";

    ///// Parameters and defaults /////
    // stop conditions
    /** Whether to use the stress epsilon by default.*/
    static final boolean USE_STRESS_EPSILON_DEFAULT = true;
    /** The default value for the epsilon to use with stress function. */
    static final double STRESS_EPSILON_DEFAULT = 0.0001;
    /** Whether to use the position change epsilon by default.*/
    static final boolean USE_POSITION_CHANGE_EPSILON_DEFAULT = true;
    /** The default value for the epsilon to use with the difference between old and new distances. */
    static final double POSITION_CHANGE_EPSILON_DEFAULT = 0.0001;
    /** Whether to use the max iterations stop criterion by default.*/
    static final boolean USE_MAX_ITERATIONS_DEFAULT = true;
    /** The default value for the maximal iterations to make. */
    static final int MAX_ITERATIONS_DEFAULT = 1_000;
    // scaling and weight
    /** The default scaling factor for edges between the nodes (as fraction of the biggest node). */
    static final double EDGE_SCALING_FACTOR_DEFAULT = 5.0;
    /** The default minimum required length of the edges between the nodes after they are scaled. */
    static final double EDGE_LENGTH_MINIMUM_DEFAULT = 25.0;
    /** The default constant scale factor for the calculated weight between two nodes. */
    static final double WEIGHT_SCALING_FACTOR_DEFAULT = 1.0;
    /** The default power of the distance for the calculated weight between two nodes. */
    static final double WEIGHT_POWER_DEFAULT = -2.0;
    // looks
    /** Whether to remove edge bends by default.*/
    static final Boolean REMOVE_EDGE_BENDS_DEFAULT = Boolean.TRUE;
    /** Whether the intermediate steps should be undoable by default. */
    static final Boolean INTERMEDIATE_UNDOABLE_DEFAULT = Boolean.FALSE;
    /** Whether the algorithm should be animated by default. */
    static final Boolean DO_ANIMATIONS_DEFAULT = Boolean.TRUE;
    /** Whether the algorithm should always layout the graph components every iteration by default. */
    static final Boolean MOVE_INTO_VIEW_DEFAULT = Boolean.TRUE;
    // multi threading
    /** Whether the algorithm should run in a background task by default. */
    static final Boolean BACKGROUND_TASK_DEFAULT = Boolean.TRUE;
    /** Whether the algorithm should use multiple threads by default. */
    static final Boolean MULTIPLE_THREADS_DEFAULT = Boolean.TRUE;

    /**
     * Contains the registered initial placers.
     * New initial placers can be registered using the {@link #registerInitialPlacer(InitialPlacer)} method.
     */
    static final LinkedList<InitialPlacer> initialPlacers = new LinkedList<>();

    /**
     * Contains the registered iterative algorithms.
     * New initial placers can be registered using the {@link #registerIterativePositionAlgorithm(IterativePositionAlgorithm)}
     * method.
     */
    static final LinkedList<IterativePositionAlgorithm> positionAlgorithms = new LinkedList<>();


    /** Contains the parameters of this {@link StressMinimizationLayout}. */
    private Parameter[] parameters;

    /**
     * Contains the number of {@link InitialPlacer}s and {@link IterativePositionAlgorithm}s in used
     * by this {@link StressMinimizationLayout}. This value is used to determine whether the parameters should
     * be updated.
     */
    private int numberOfSubAlgorithms;


    /**
     * Registers a new initial placer for all {@link StressMinimizationLayout} instances.<br>
     * This initial placer must also have a default Constructor without any arguments.
     * This constructor does not need to be public (it will be accessed via reflection)
     * but the resulting object must behave as desired.<br>
     *
     * If a {@link StressMinimizationLayout} has already initialized it's cached parameter list, this list will
     * be updated on the next call of {@link #getParameters()} and every changes made by the user will be lost!
     *
     * @param initialPlacer
     *      the initial placer to add. It's name may not be <code>null</code> and must not be already contained
     *      in the internal list of initial placers.
     * @author Jannik
     */
    public static void registerInitialPlacer(final InitialPlacer initialPlacer) {
        Objects.requireNonNull(initialPlacer);
        final String name = initialPlacer.getName();
        Objects.requireNonNull(name, "The name of the Initial Placer may not be null!");

        for (InitialPlacer placer : initialPlacers) {
            if (placer.getName().trim().equals(name)) {
                throw new IllegalArgumentException("An Initial Placer with this name is already registered.");
            }
        }

        for (Constructor<?> constructor : initialPlacer.getClass().getDeclaredConstructors()) {
            if (constructor.getParameterCount() == 0) {
                initialPlacers.add(initialPlacer);
                return;
            }
        }
        throw new IllegalArgumentException("The Iterative Position algorithm has no default constructor.");
    }

    /**
     * Registers a new iterative position algorithm for all {@link StressMinimizationLayout} instances.<br>
     * This iterative position algorithm must also have a default Constructor without any arguments.
     * This constructor does not need to be public (it will be accessed via reflection) but the resulting object
     * must behave as desired.<br>
     *
     * If a {@link StressMinimizationLayout} has already initialized it's cached parameter list, this list will
     * be updated on the next call of {@link #getParameters()} and every changes made by the user will be lost!
     *
     * @param iterativePositionAlgorithm
     *      the iterative position algorithm to add. It's name may not be <code>null</code> and must not be already contained
     *      in the internal list of iterative position algorithms.
     * @author Jannik
     */
    public static void registerIterativePositionAlgorithm(final IterativePositionAlgorithm iterativePositionAlgorithm) {
        Objects.requireNonNull(iterativePositionAlgorithm);
        final String name = iterativePositionAlgorithm.getName();
        Objects.requireNonNull(name, "The name of the Iterative Position Algorithm may not be null!");

        for (IterativePositionAlgorithm iterative : positionAlgorithms) {
            if (iterative.getName().trim().equals(name)) {
                throw new IllegalArgumentException("An Iterative Position Algorithm with this name is already registered.");
            }
        }

        for (Constructor<?> constructor : iterativePositionAlgorithm.getClass().getDeclaredConstructors()) {
            if (constructor.getParameterCount() == 0) {
                positionAlgorithms.add(iterativePositionAlgorithm);
                return;
            }
        }
        throw new IllegalArgumentException("The Iterative Position algorithm has no default constructor.");
    }

    /**
     * @return
     *      a list of names of already registered {@link InitialPlacer}s.
     * @author Jannik
     */
    public static List<String> getInitialPlacerNames() {
        return initialPlacers.stream().map(Describable::getName).collect(Collectors.toList());
    }

    /**
     * @return
     *      a list of names of already registered {@link IterativePositionAlgorithm}s.
     * @author Jannik
     */
    public static List<String> getIterativePositionAlgorithmNames() {
        return positionAlgorithms.stream().map(Describable::getName).collect(Collectors.toList());
    }

    /*
     * Registers the default initial placers.
     * @author Jannik
     */
    static {
        // register default initial placers
        registerInitialPlacer(new PivotMDS());
        registerInitialPlacer(new NullPlacer());
        // register default intuitive position algorithms
        registerIterativePositionAlgorithm(new IntuitiveIterativePositionAlgorithm());
    }

    /**
     * @return the description of the algorithm.
     */
    @Override
    public String getDescription() {
        return "<html>Performs the stress minimization layout on<br>" +
                "the given graph.<br>" +
                "A faster runtime may result in a worse<br>" +
                "layout.</html>";
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

        try { // test if graph is already marked
            graph.getAttribute(WORKING_ATTRIBUTE); // only check for existence
            MainFrame.getInstance().showMessageDialog("Cannot run Stress Minimization on " +
                    "a graph that is currently stress minimized.");
            return;
        } catch (AttributeNotFoundException e) {
            // mark graph
            graph.setBoolean(StressMinimizationLayout.WORKING_ATTRIBUTE, true);
        }
        State state = this.state;
        ArrayList<Node> pureNodes;
        // MultiLevelFramework compatibility mode.
        final boolean compatibilityMLF;

        try {
            // add graph
            state.graph = this.graph;
            assert (!state.intermediateUndoable || state.doAnimations); // intermediateUndoable => doAnimations
            this.state = new State();

            // get nodes to work with
            if (selection.isEmpty()) {
                pureNodes = new ArrayList<>(GraphHelper.getVisibleNodes(state.graph.getNodes()));
            } else {
                pureNodes = new ArrayList<>(GraphHelper.getVisibleNodes(selection.getNodes()));
            }

            // --- MultiLevelFramework compatibility ---


            if (state.graph.getAttributes().getCollection().containsKey(StressMinimizationLayout.MLF_COMPATIBILITY_IS_COARSENING_LEVEL) &&
                    state.graph.getBoolean(StressMinimizationLayout.MLF_COMPATIBILITY_IS_COARSENING_LEVEL)) {
                compatibilityMLF = true;
                state.intermediateUndoable = false;
                state.doAnimations = false;
                state.moveIntoView = false;
                state.backgroundTask = false;
                if (state.graph.getAttributes().getCollection().containsKey(StressMinimizationLayout.MLF_COMPATIBILITY_IS_TOP_COARSENING_LEVEL) &&
                        state.graph.getBoolean(StressMinimizationLayout.MLF_COMPATIBILITY_IS_TOP_COARSENING_LEVEL)) {
                    state.initialPlacer = new NullPlacer(); // we are not at top level
                }
                if (!state.graph.getAttributes().getCollection().containsKey(StressMinimizationLayout.MLF_COMPATIBILITY_IS_BOTTOM_COARSENING_LEVEL) ||
                        !state.graph.getBoolean(StressMinimizationLayout.MLF_COMPATIBILITY_IS_BOTTOM_COARSENING_LEVEL)) {
                    state.edgeScalingFactor = 1.0; // we are not at bottom level (no edge scaling allowed)
                }
            } else {
                compatibilityMLF = false;
            }
        } catch (Throwable t) {
            // release lock
            state.graph.removeAttribute(StressMinimizationLayout.WORKING_ATTRIBUTE);
            throw t;
        }

        Runnable task = () -> {
            try {
                state.startTime = System.currentTimeMillis();

                state.status = "Start (n = " + pureNodes.size() + ")"; if (state.debugOutput) {System.out.println((System.currentTimeMillis() - state.startTime) + " SM: " + state.status);}
                // remove bends
                if (state.removeEdgeBends) {
                    GraphHelper.removeBendsBetweenSelectedNodes(pureNodes, true);
                }
                state.status = "Getting connected components..."; if (state.debugOutput) {System.out.println((System.currentTimeMillis() - state.startTime) + " SM: " + state.status);}
                // save old positions if the user only wants one position update
                ArrayList<Vector2d> oldPositions = new ArrayList<>(0);
                if (!state.intermediateUndoable) {
                    oldPositions.ensureCapacity(pureNodes.size());
                    for (Node node : pureNodes) {
                        oldPositions.add(AttributeHelper.getPositionVec2d(node));
                    }
                }
                // get connected components and layout them
                final Set<List<Node>> connectedComponents = ConnectedComponentsHelper.getConnectedComponents(pureNodes);


                if (state.doAnimations) {
                    ConnectedComponentsHelper.layoutConnectedComponents(connectedComponents, state.intermediateUndoable);
                }
                state.status = "Got connected components. (" + connectedComponents.size() + ")"; if (state.debugOutput) {System.out.println((System.currentTimeMillis() - state.startTime) + " SM: " + state.status);}

                // TODO add random initial layout

                List<WorkUnit> workUnits = new ArrayList<>(connectedComponents.size());
                if (state.debugEnabled) {
                    state.debugWorkUnits = workUnits;
                }
                List<List<Node>> connectComponentList = new ArrayList<>(connectedComponents.size());
                List<List<Vector2d>> connectedComponentListPos = Collections.synchronizedList(new ArrayList<>(connectedComponents.size()));

                // prepare (set helper attribute)
                int posCC = 0;
                ConcurrentMap<Node, Vector2d> move = new ConcurrentHashMap<>();
                for (List<Node> component : connectedComponents) {
                    // Set positions attribute for hopefully better handling
                    state.status = "Preparing connected components for algorithm."; if (state.debugOutput) {System.out.println((System.currentTimeMillis() - state.startTime) + " SM: " + state.status + " (n = " + component.size() + ")");}
                    for (int pos = 0; pos < component.size(); pos++) {
                        component.get(pos).setInteger(StressMinimizationLayout.INDEX_ATTRIBUTE, pos);
                    }
                    WorkUnit unit = new WorkUnit(component, posCC++, state);

                    workUnits.add(unit);

                    if (state.doAnimations) {
                        if (state.moveIntoView) {
                            connectComponentList.add(component);
                            connectedComponentListPos.add(unit.currentPositions);
                        } else {
                            for (int idx = 0; idx < component.size(); idx++) {
                                move.put(component.get(idx), unit.currentPositions.get(idx));
                            }
                        }
                    }
                }
                if (state.doAnimations) {
                    if (state.moveIntoView) {
                        ConnectedComponentsHelper.layoutConnectedComponents(connectComponentList, connectedComponentListPos, state.intermediateUndoable);
                    } else if (state.intermediateUndoable) {
                        GraphHelper.applyUndoableNodePositionUpdate(new HashMap<>(move), "Initial layout");
                    } else {
                        state.graph.getListenerManager().transactionStarted(this);
                        for (Map.Entry<Node, Vector2d> entry : move.entrySet()) {
                            AttributeHelper.setPosition(entry.getKey(), entry.getValue());
                        }
                        state.graph.getListenerManager().transactionFinished(this);
                    }
                }
                state.status = "Finished preparing"; if (state.debugOutput) {System.out.println((System.currentTimeMillis() - state.startTime) + " SM: " + state.status);}

                boolean someoneIsWorking = true;

                ExecutorService executor = Executors.newFixedThreadPool(Math.min(Runtime.getRuntime().availableProcessors(), connectedComponents.size()));
                List<Callable<Object>> todo = new ArrayList<>(workUnits.size());
                // iterate
                int iteration;
                for (iteration = 1; state.keepRunning.get() && someoneIsWorking && iteration <= state.maxIterations; ++iteration) {
                    state.status = "Iteration " + iteration; if (state.debugOutput) {System.out.println((System.currentTimeMillis() - state.startTime) + " SM: " + state.status);}
                    someoneIsWorking = false;
                    move.clear();
                    // execute every work unit
                    for (WorkUnit unit : workUnits) {
                        if (unit.hasStopped) {
                            continue;
                        }
                        todo.add(Executors.callable(() -> {
                            unit.nextIteration();

                            if (state.doAnimations) {
                                if (state.debugOutput) {System.out.println((System.currentTimeMillis() - state.startTime) + " SM@" + unit.pos + ": Prepare result.");}
                                if (state.moveIntoView) {
                                    connectedComponentListPos.set(unit.pos, unit.currentPositions);
                                } else {
                                    HashMap<Node, Vector2d> result = new HashMap<>();
                                    for (int idx = 0; idx < unit.nodes.size(); idx++) {
                                        result.put(unit.nodes.get(idx), unit.currentPositions.get(idx));
                                    }
                                    move.putAll(result);
                                }
                            }
                            if (state.debugOutput) {System.out.println((System.currentTimeMillis() - state.startTime) + " SM@" + unit.pos + ": Iteration finished.");}
                        }));
                        // something was worked on
                        someoneIsWorking = true;
                    }

                    try {
                        if (state.keepRunning.get()) {
                            if (state.multipleThreads) {
                                executor.invokeAll(todo);
                            } else {
                                for (Callable<Object> callable : todo) {
                                    callable.call();
                                }
                            }
                        }
                        todo.clear();
                    } catch (Exception e) {
                        state.keepRunning.set(false);
                        e.printStackTrace();
                    }

                    if (state.doAnimations) {
                        if (state.moveIntoView) {
                            ConnectedComponentsHelper.layoutConnectedComponents(connectComponentList, connectedComponentListPos, state.intermediateUndoable);
                        }
                        if (state.intermediateUndoable) {
                            GraphHelper.applyUndoableNodePositionUpdate(new HashMap<>(move), "Do iteration " + iteration);
                        } else {
                            state.graph.getListenerManager().transactionStarted(this);
                            for (Map.Entry<Node, Vector2d> entry : move.entrySet()) {
                                AttributeHelper.setPosition(entry.getKey(), entry.getValue());
                            }
                            state.graph.getListenerManager().transactionFinished(this);
                        }
                    }
                }
                state.status = "Finish work."; if (state.debugOutput) {System.out.println((System.currentTimeMillis() - state.startTime) + " SM: " + state.status);}


                state.status = "Postprocessing..."; if (state.debugOutput) {System.out.println((System.currentTimeMillis() - state.startTime) + " SM: " + state.status);}
                // finish (remove helper attribute)
                for (Node node : pureNodes) {
                    node.removeAttribute(StressMinimizationLayout.INDEX_ATTRIBUTE);
                }

                if (!state.doAnimations) { // => !intermediateUndoable
                    state.graph.getListenerManager().transactionStarted(this);
                    for (WorkUnit unit : workUnits) { // prepare for layout connected components
                        for (int idx = 0; idx < unit.nodes.size(); idx++) {
                            AttributeHelper.setPosition(unit.nodes.get(idx), unit.currentPositions.get(idx));
                        }
                    }
                    state.graph.getListenerManager().transactionFinished(this);
                }

                if (!state.doAnimations || !state.moveIntoView) { // else already moved
                    ConnectedComponentsHelper.layoutConnectedComponents(connectedComponents, state.intermediateUndoable);
                }
                // create only one undo (or nothing in compatibility mode)
                if (!compatibilityMLF && !state.intermediateUndoable) {
                    state.graph.getListenerManager().transactionStarted(this);
                    for (int idx = 0; idx < pureNodes.size(); idx++) {
                        Node node = pureNodes.get(idx);
                        move.put(node, AttributeHelper.getPositionVec2d(node));
                        // reset to old position so that only undo has correct starting positions
                        AttributeHelper.setPosition(node, oldPositions.get(idx));
                    }
                    state.graph.getListenerManager().transactionFinished(this);
                    GraphHelper.applyUndoableNodePositionUpdate(new HashMap<>(move), "Stress Minimization");
                }
                // debug stuff
                if (state.debugEnabled) {
                    state.debugIterations = iteration-1;

                    state.debugCumulativeStress = 0.0;
                    final int allNodes = pureNodes.size();
                    for (WorkUnit unit : workUnits) {
                        state.debugCumulativeStress += (double)unit.nodes.size()/allNodes*unit.currentStress;

                    }
                }

                state.startTime = System.currentTimeMillis() - state.startTime;
                System.out.println(state.startTime + " SM: " + (state.status = "Finished.") + " Took " + (state.startTime / 1000.0) + "s");

            } finally {
                // release lock
                state.graph.removeAttribute(StressMinimizationLayout.WORKING_ATTRIBUTE);
            }
        };
        // run!
        if (state.backgroundTask) {
            BackgroundTaskHelper.issueSimpleTask("Stress Minimization", "Init", task, null, state, 10);
        } else {
            task.run();
        }
    }

    /**
     * For a list of the parameters and their indices see {@link #setParameters(Parameter[])}.
     * @return
     *      the parameters the algorithm uses. The value of these
     *      parameters will be reset if any new {@link InitialPlacer}s or {@link IterativePositionAlgorithm}s were registered
     *      in the meantime.
     *
     * @author Jannik
     */
    @Override
    public Parameter[] getParameters() {
        if (this.parameters == null || this.numberOfSubAlgorithms != initialPlacers.size() + positionAlgorithms.size()) {
            this.parameters = getNewParameters();
            this.numberOfSubAlgorithms = initialPlacers.size() + positionAlgorithms.size();
        }
        return this.parameters;
    }

    /**
     * @return
     *      a new set of parameters to work with.
     * @author Jannik
     */
    private Parameter[] getNewParameters() {
        return new Parameter[] {
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
                ParameterizableSelectorParameter.getFromList(0, new ArrayList<>(initialPlacers),
                        selection, "Select layout",
                        "<html>The layout to apply before the actual algorithm is run.</html>"),
                new JComponentParameter(new JPanel(), "<html><u>Iterative algorithm</u></html>",
                        "<html>The algorithm that should be used to calculate the new positions of in every iteration step.</html>"), //hacky section header
                ParameterizableSelectorParameter.getFromList(0, new ArrayList<>(positionAlgorithms),
                        selection, "Select algorithm",
                        "<html>The algorithm that should be used to calculate the new positions of in every iteration step.</html>"),
                new JComponentParameter(new JPanel(), "<html><u>General layout</u></html>",
                        "<html>Some general settings for the layouter.</html>"), //hacky section header
                EnableableNumberParameter.alwaysEnabled(EDGE_SCALING_FACTOR_DEFAULT, 0.0, Double.MAX_VALUE, 0.5,
                        "Edge scaling factor", "<html>The amount of space that the algorithm tries to ensure between two nodes<br>" +
                                "(length of the edges) as faction of the size of the largest node encountered.</html>"),
                EnableableNumberParameter.alwaysEnabled(EDGE_LENGTH_MINIMUM_DEFAULT, 0.0, Double.MAX_VALUE, 1.0,
                        "Edge length minimum", "<html>The amount of space that the algorithm at least tries to ensure between two nodes.<br>" +
                                "This value will be used if the scaled length (see above) is smaller than it.</html>"),
                new BooleanParameter(REMOVE_EDGE_BENDS_DEFAULT, "Remove edge bends",
                        "<html>Remove edge bends before starting the algorithm.<br><b>Highly recommended</b> because the algorithm does not " +
                                "move edge bends.<br>Always undoable as extra undo.</html>"),
                new BooleanParameter(INTERMEDIATE_UNDOABLE_DEFAULT, "Iterations undoable",
                        "<html>Should each iteration step be undoable (only recommended for small iteration sizes).<br>" +
                                "This parameter implies “Animate iterations”.</html>"),
                new BooleanParameter(DO_ANIMATIONS_DEFAULT, "Animate iterations",
                        "<html>Whether every iteration should update the node positions.<br>" +
                                "May impact performance. Should be disabled on slow machines.</html>"),
                new BooleanParameter(MOVE_INTO_VIEW_DEFAULT, "Move into view",
                        "<html>Whether every iteration should try layout the graph so that the animation is more readable.<br>" +
                                "Takes only effect if “Animate iterations” is enabled.<br>" +
                                "May impact performance even more. Should be disabled on slow machines.</html>"),
                new JComponentParameter(new JPanel(), "<html><u>Parallelism (advanced)</u></html>",
                        "<html>Sets threading options for running.<br><b>Should not be changed by inexperienced users.</b></html>"), //hacky section header
                new BooleanParameter(BACKGROUND_TASK_DEFAULT, "Run in background",
                        "<html>Create a background task and run the algorithm from there.<br>" +
                                "<b>Highly recommended:</b> If this is disabled <i>VANTED</i> will freeze until the algorithm is complete.</html>"),
                new BooleanParameter(MULTIPLE_THREADS_DEFAULT, "Use parallelism",
                        "<html>Create multiple threads to run the algorithm from.<br>" +
                                "Increases overall performance in most cases.</html>"),

        };
    }

    /**
     * Sets the parameters. The following parameters are expected at the different indices:
     * <table>
     *      <tr><th>Index</th><th>Name</th></tr>
     *      <tr><td>0</td><td></td></tr>
     *      <tr><td>1</td><td>Stress change</td></tr>
     *      <tr><td>2</td><td>Position change</td></tr>
     *      <tr><td>3</td><td>Max iterations</td></tr>
     *      <tr><td>4</td><td></td></tr>
     *      <tr><td>5</td><td>Scaling factor</td></tr>
     *      <tr><td>6</td><td>Distance power</td></tr>
     *      <tr><td>7</td><td></td></tr>
     *      <tr><td>8</td><td>Initial layout</td></tr>
     *      <tr><td>9</td><td></td></tr>
     *      <tr><td>10</td><td>Iterative algorithm</td></tr>
     *      <tr><td>11</td><td></td></tr>
     *      <tr><td>12</td><td>Edge scaling factor</td></tr>
     *      <tr><td>13</td><td>Edge length minimum</td></tr>
     *      <tr><td>14</td><td>Remove edge bends</td></tr>
     *      <tr><td>15</td><td>Iterations undoable</td></tr>
     *      <tr><td>16</td><td>Animate iterations</td></tr>
     *      <tr><td>17</td><td>Move into view</td></tr>
     *      <tr><td>18</td><td></td></tr>
     *      <tr><td>19</td><td>Run in background</td></tr>
     *      <tr><td>20</td><td>Use parallelism</td></tr>
     * </table>
     *
     * @param params the parameters to be set.
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
                this.state.stressEpsilon = SMALLEST_NON_ZERO_VALUE;
            } else {
                this.state.stressEpsilon = doubleParameter.getValue();
            }
        } else {
            this.state.stressEpsilon = Double.NEGATIVE_INFINITY;
        }
        // Position epsilon
        doubleParameter = (EnableableNumberParameter<Double>) params[2].getValue();
        if (doubleParameter.isEnabled()) {
            if (doubleParameter.getValue() == 0.0) {
                this.state.positionChangeEpsilon = SMALLEST_NON_ZERO_VALUE;
            } else {
                this.state.positionChangeEpsilon = doubleParameter.getValue();
            }
        } else {
            this.state.positionChangeEpsilon = Double.NEGATIVE_INFINITY;
        }
        // Max iterations
        integerParameter = ((EnableableNumberParameter<Integer>) params[3].getValue());
        if (integerParameter.isEnabled()) {
            this.state.maxIterations = integerParameter.getValue();
        } else {
            this.state.maxIterations = Long.MAX_VALUE;
        }
        // weight variables
        doubleParameter = (EnableableNumberParameter<Double>) params[5].getValue();
        this.state.weightScalingFactor = doubleParameter.getValue();
        doubleParameter = (EnableableNumberParameter<Double>) params[6].getValue();
        this.state.weightPower = doubleParameter.getValue();
        // initial layout
        selectorParameter = ((ParameterizableSelectorParameter) params[8].getValue());
        InitialPlacer templateInitialPlacer = ((InitialPlacer) selectorParameter.getSelectedParameterizable());
        Parameter[] parametersInitialPlacer = selectorParameter.getUpdatedParameters();
        // position algorithm
        selectorParameter = ((ParameterizableSelectorParameter) params[10].getValue());
        IterativePositionAlgorithm templateIterativePositionAlgorithm = ((IterativePositionAlgorithm) selectorParameter.getSelectedParameterizable());
        Parameter[] parametersIterativePositionAlgorithm = selectorParameter.getUpdatedParameters();
        // General settings
        doubleParameter = (EnableableNumberParameter<Double>) params[12].getValue();
        this.state.edgeScalingFactor = doubleParameter.getValue();
        doubleParameter = (EnableableNumberParameter<Double>) params[13].getValue();
        if (doubleParameter.getValue() == 0.0) {
            this.state.edgeLengthMinimum = SMALLEST_NON_ZERO_VALUE;
        } else {
            this.state.edgeLengthMinimum = doubleParameter.getValue();
        }
        this.state.removeEdgeBends = ((BooleanParameter) params[14]).getBoolean();
        // Intermediates undoable
        this.state.intermediateUndoable = ((BooleanParameter) params[15]).getBoolean();
        // Do animation
        this.state.doAnimations = ((BooleanParameter) params[16]).getBoolean();
        if (state.intermediateUndoable) { // intermediateUndoable => doAnimations
            state.doAnimations = true;
        }
        this.state.moveIntoView = ((BooleanParameter) params[17]).getBoolean();
        // Threading
        this.state.backgroundTask = ((BooleanParameter) params[19]).getBoolean();
        this.state.multipleThreads = ((BooleanParameter) params[20]).getBoolean();

        // Create copy of InitialPlacer and IterativePositionAlgorithm
        try {
            final Constructor<? extends InitialPlacer> constructor =
                    templateInitialPlacer.getClass().getDeclaredConstructor();
            constructor.setAccessible(true);
            this.state.initialPlacer = constructor.newInstance();
            this.state.initialPlacer.setParameters(parametersInitialPlacer);
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
        try {
            final Constructor<? extends IterativePositionAlgorithm> constructor =
                    templateIterativePositionAlgorithm.getClass().getDeclaredConstructor();
            constructor.setAccessible(true);
            this.state.positionAlgorithm = constructor.newInstance();
            this.state.positionAlgorithm.setParameters(parametersIterativePositionAlgorithm);
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
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

    /**
     * @param v the View to check.
     * @return whether this algorithm is active for the given View.
     */
    public boolean activeForView(View v) {
        return v != null;
    }

    /**
     * Calculates the stress function for a given graph as given by the following formula
     * <pre>
     *    \sum_{i < j} w_{ij}(d_{ij} - ||p_i - p_j ||)^2
     * </pre>
     * with {@code w} being the node weights, which are defined , {@code d} being the graph theoretical distance,
     * p being the positions and {@code ||.||} being the Euclidean distance.
     *
     * All data structures needed for the calculation will also be calculated.
     *
     * @param graph the graph to be used.
     * @param cumulateStress
     *      whether to comulate the stress of all connected components as weighted sum relative
     *      to the number of nodes in it.<br>
     *      If this is true, the returned list will only contain the cumulated stress as an element.
     * @param edgeScalingFactor
     *      the scaling factor for edges between the nodes (as fraction of the biggest node).
     *      See {@link StressMinimizationLayout#EDGE_SCALING_FACTOR_DEFAULT}.
     * @param edgeLengthMinimum
     *      the minimum length to try to ensure for the edges.
     *      See {@link StressMinimizationLayout#EDGE_LENGTH_MINIMUM_DEFAULT}.
     * @param weightConstant
     *      the constant scale factor for the calculated weight between two nodes.
     *      See {@link StressMinimizationLayout#WEIGHT_SCALING_FACTOR_DEFAULT}.
     * @param weightPower
     *      the constant scale factor for the calculated weight between two nodes.
     *      See {@link StressMinimizationLayout#WEIGHT_POWER_DEFAULT}.
     *
     * @return
     *      the stress values of the different connected components sorted in descending order by the
     *      number of the node size in the related connected component<br>
     *      or the cumulated value as single element in a list.<br>
     *      An empty list will be returned or a list containing <code>NaN</code> in case of cumulation,
     *      if the graph is empty.
     *
     * @author Jannik
     */
    public static List<Double> calculateStress(final Graph graph, final boolean cumulateStress,
                                        final double edgeScalingFactor, final double edgeLengthMinimum,
                                        final double weightConstant, final double weightPower) {
        ArrayList<List<Node>> connectedComponents = new ArrayList<>(
                ConnectedComponentsHelper.getConnectedComponents(graph.getNodes()));
        if (!cumulateStress) { // else no need to sort
            connectedComponents.sort((l1, l2) -> l2.size() - l1.size());
        }
        List<Double> result = new ArrayList<>(connectedComponents.size());

        // go through every connected component
        NodeValueMatrix distances, weights;
        List<Vector2d> positions;
        for (List<Node> component : connectedComponents) {
            // set position attribute correctly
            for (int pos = 0; pos < component.size(); pos++) {
                component.get(pos).setInteger(StressMinimizationLayout.INDEX_ATTRIBUTE, pos);
            }

            distances = ShortestDistanceAlgorithm.calculateShortestPaths(component, Integer.MAX_VALUE, true);
            final double scalingFactor =
                    Math.max(edgeLengthMinimum, ConnectedComponentsHelper.getMaxNodeSize(component)*edgeScalingFactor);
            distances.apply(x -> x*scalingFactor);
            weights = distances.clone().apply(x -> weightConstant*Math.pow(x, weightPower));
            positions = component.stream().map(AttributeHelper::getPositionVec2d).collect(Collectors.toList());

            result.add(StressMinimizationLayout.calculateStress(positions, distances, weights));

            // remove position attribute
            for (Node node : component) {
                node.removeAttribute(StressMinimizationLayout.INDEX_ATTRIBUTE);
            }
        }

        if (!cumulateStress) {
            return result;
        } else if (connectedComponents.isEmpty()) {
            return Collections.singletonList(Double.NaN);
        }

        // calculate the weighted sum
        double weighted = 0.0;
        final int allNodes = graph.getNumberOfNodes();

        for (int idx = 0; idx < result.size(); idx++) {
            weighted += (double)connectedComponents.get(idx).size()/allNodes*result.get(idx);
        }
        return Collections.singletonList(weighted);
    }

    /**
     * Calculates the stress function for a given graph as given by the following formula
     * <pre>
     *    \sum_{i < j} w_{ij}(d_{ij} - ||p_i - p_j ||)^2
     * </pre>
     * with {@code w} being the node weights, which are defined , {@code d} being the graph theoretical distance,
     * p being the positions and {@code ||.||} being the Euclidean distance.
     *
     * All data structures needed for the calculation will also be calculated.
     * This method will use the default values for every needed configurable value.
     *
     * @param graph the graph to be used.
     * @param cumulateStress
     *      whether to comulate the stress of all connected components as weighted sum relative
     *      to the number of nodes in it.<br>
     *      If this is true, the returned list will only contain the cumulated stress as an element.
     *
     * @return
     *      the stress values of the different connected components sorted in descending order by the
     *      number of the node size in the related connected component<br>
     *      or the cumulated value as single element in a list.<br>
     *      An empty list will be returned, if the graph is empty.
     *
     * @see StressMinimizationLayout#calculateStress(Graph, boolean, double, double, double, double)
     * @author Jannik
     */
    public static List<Double> calculateStress(final Graph graph, final boolean cumulateStress) {
        return calculateStress(graph, cumulateStress, EDGE_SCALING_FACTOR_DEFAULT, EDGE_LENGTH_MINIMUM_DEFAULT,
                WEIGHT_SCALING_FACTOR_DEFAULT, WEIGHT_POWER_DEFAULT);
    }

    /**
     * Calculates the stress function for a given set of nodes as given by the following formula
     * <pre>
     *    \sum_{i < j} w_{ij}(d_{ij} - ||p_i - p_j ||)^2
     * </pre>
     * with {@code w} being the node weights, {@code d} being the graph theoretical distance,
     * p being the positions and {@code ||.||} being the Euclidean distance.
     *
     * All three arguments must have the same dimension/size.
     *
     * @param positions the positions to use for calculation
     * @param distances the graph theoretical distances between the nodes.
     * @param weights the weights of each node.
     *
     * @return the stress value.
     *
     * @see StressMinimizationLayout#calculateStress(List, NodeValueMatrix, NodeValueMatrix)
     *
     * @author Jannik
     */
    private static double calculateStress(final List<Vector2d> positions,
                                          final NodeValueMatrix distances,
                                          final NodeValueMatrix weights) {
        assert distances.getDimension() == positions.size() && positions.size() == weights.getDimension();
        // get needed distances

        double result = 0.0;
        double parenthesis; // holds contents of parentheses
        Vector2d iPos;

        // i < j is short for a double sum
        for (int i = 0; i < positions.size(); i++) {
            iPos = positions.get(i);
            for (int j = i+1; j < positions.size(); j++) {
                // inner sum term
                parenthesis = distances.get(i, j) - iPos.distance(positions.get(j));
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
     * A class that contains the current state of a {@link StressMinimizationLayout}.
     * A instance of this class should be unique for every call of {@link #execute()}
     * to prevent interference of different executions.<br>
     * The fields of this class should not be changed after it is passed to an to a call of
     * {@link #execute()}.
     *
     * @author Jannik
     */
    static class State implements BackgroundTaskStatusProvider {
        /** The graph to work with. */
        public Graph graph;
        /** The time in ms the algorithm started. Used to calculate elapsed time. */
        long startTime;
        /** The current displayed status of the algorithm. */
        volatile String status;
        /** Whether the algorithm was not stopped by the user. */
        volatile AtomicBoolean keepRunning;
        /** The {@link IntuitiveIterativePositionAlgorithm} to use. */
        IterativePositionAlgorithm positionAlgorithm;
        /** The {@link InitialPlacer} to use. */
        InitialPlacer initialPlacer;
        /** The epsilon to use with the stress function. */
        double stressEpsilon;
        /** The epsilon to use with the difference between old  new distances. */
        double positionChangeEpsilon;
        /** The maximal iterations to make. */
        long maxIterations;
        /** The scaling factor for the edges between the nodes (as fraction of the biggest node). */
        double edgeScalingFactor;
        /** The minimum required length of the edges between the nodes after they are scaled. */
        double edgeLengthMinimum;
        /** The constant scale factor for the calculated weight between two nodes. */
        double weightScalingFactor;
        /** The constant power of the distance for the calculated weight between two nodes. */
        double weightPower;
        /** Whether to remove edge bends. */
        boolean removeEdgeBends;
        /** Whether the intermediate steps should be undoable. */
        boolean intermediateUndoable;
        /** Whether the algorithm should be animated. */
        boolean doAnimations;
        /** Whether the algorithm should always layout the graph components every iteration. */
        boolean moveIntoView;
        /** Whether the algorithm should run in a background task. */
        boolean backgroundTask;
        /** Whether the algorithm should use multiple threads. */
        boolean multipleThreads;

        // Debugging and debugging information
        /** Whether to enable debug mode. */
        boolean debugEnabled;
        /**
         * Whether to print more information to the console. Only has effect if debug mode is active.
         * Defaults to <code>true</code> and must be disabled for every state separately.
         */
        boolean debugOutput;
        /** The number of iterations after the algorithm is finished. */
        int debugIterations;
        /** A list containing all work units. Only set if debug mode is active */
        List<WorkUnit> debugWorkUnits;
        /** The cumulated stress value after the algorithm is finished. Only set if debug mode is active. */
        double debugCumulativeStress;


        /**
         * Constructs a new state with the default values.
         * @author Jannik
         */
        State() {
            this.stressEpsilon = StressMinimizationLayout.STRESS_EPSILON_DEFAULT;
            this.positionChangeEpsilon = StressMinimizationLayout.POSITION_CHANGE_EPSILON_DEFAULT;
            this.maxIterations = StressMinimizationLayout.MAX_ITERATIONS_DEFAULT;
            this.edgeScalingFactor = StressMinimizationLayout.EDGE_SCALING_FACTOR_DEFAULT;
            this.edgeLengthMinimum = StressMinimizationLayout.EDGE_LENGTH_MINIMUM_DEFAULT;
            this.weightScalingFactor = StressMinimizationLayout.WEIGHT_SCALING_FACTOR_DEFAULT;
            this.weightPower = StressMinimizationLayout.WEIGHT_POWER_DEFAULT;
            this.removeEdgeBends = StressMinimizationLayout.REMOVE_EDGE_BENDS_DEFAULT;
            this.intermediateUndoable = StressMinimizationLayout.INTERMEDIATE_UNDOABLE_DEFAULT;
            this.doAnimations = StressMinimizationLayout.DO_ANIMATIONS_DEFAULT;
            this.moveIntoView = StressMinimizationLayout.MOVE_INTO_VIEW_DEFAULT;
            this.backgroundTask = StressMinimizationLayout.BACKGROUND_TASK_DEFAULT;
            this.multipleThreads = StressMinimizationLayout.MULTIPLE_THREADS_DEFAULT;

            this.keepRunning = new AtomicBoolean(true);
            this.status = "";
            this.positionAlgorithm = new IntuitiveIterativePositionAlgorithm();
            this.initialPlacer = new PivotMDS();

            debugEnabled = debugModeDefault;
            if (debugEnabled) {debugOutput = true;}
            debugIterations = -1;
            debugCumulativeStress = Double.NaN;
        }

        /**
         * @return
         *      the current status of the {@link #execute()} method.
         */
        @Override
        public String getCurrentStatusMessage1() {
            return status;
        }

        /**
         * If this method is called on the status provider, the stress minimization task will
         * stop any preprocessing or stop after the current iteration.
         */
        @Override
        public void pleaseStop() {
            keepRunning.set(false);
        }

        /**
         * Returns the completion status. This cannot be determined
         * so {@code -1} is returned.
         * @return {@code -1}
         */
        @Override
        public int getCurrentStatusValue() {/* No op*/ return -1;}

        /**
         * @param value will be ignored.
         */
        @Override
        public void setCurrentStatusValue(int value) {/* No op*/}

        /**
         * @return {@code -1}, because a the value cannot be determined.
         *         This implies that it cannot be refined.
         */
        @Override
        public double getCurrentStatusValueFine() { return -1; }

        /**
         * @return {@code null} because there is no second
         */
        @Override
        public String getCurrentStatusMessage2() {/* No op*/ return null;}

        /**
         * @return
         *      <code>false</code>, because plugin does not wait for user.
         */
        @Override
        public boolean pluginWaitsForUser() {/* No op*/return false;}

        /**
         * Does nothing because plugin does not wait for user.
         */
        @Override
        public void pleaseContinueRun() {/* No-op */}
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

        /** The position of {@code nodes} in the connected component list. Should be unique. */
        final int pos;
        /** The nodes to work on. */
        final List<Node> nodes;
        /** The graph theoretical distances to work with. These will not change once calculated. */
        final NodeValueMatrix distances;
        /** The weights used for every node pair. These will not change once calculated. */
        final NodeValueMatrix weights;

        /** The state this unit is working with. */
        final State state;

        /**
         * The current calculated stress from the {@link #calculateStress(List, NodeValueMatrix, NodeValueMatrix)}
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
         * @param pos the position of {@code nodes} in the connected component list.
         * @param state the state to work with.
         *
         * @author Jannik
         */
        public WorkUnit(final List<Node> nodes, final int pos, final State state) {
            this.pos = pos;
            this.nodes = nodes;
            this.state = state;
            if (state.debugOutput) {System.out.println((System.currentTimeMillis() - state.startTime) + " SM@"+(state.status = pos + ": Calculate distances..."));}
            this.distances = ShortestDistanceAlgorithm.calculateShortestPaths(nodes, Integer.MAX_VALUE, state.multipleThreads); // TODO make configurable
            if (state.debugOutput) {System.out.println((System.currentTimeMillis() - state.startTime) + " SM@"+(state.status = pos + ": Calculate scaling factor..."));}
            final double scalingFactor = Math.max(
                    ConnectedComponentsHelper.getMaxNodeSize(nodes)*state.edgeScalingFactor, state.edgeLengthMinimum);
            // scale for better display
            // TODO maybe place this after initial layout to prevent over scaling
            this.distances.apply(x -> x*scalingFactor);

            if (state.debugOutput) {System.out.println((System.currentTimeMillis() - state.startTime) + " SM@"+(state.status = pos + ": Calculate initial layout..."));}
            //this.currentPositions = nodes.stream().map(AttributeHelper::getPositionVec2d).collect(Collectors.toList());
            this.currentPositions = state.initialPlacer.calculateInitialPositions(nodes, this.distances);
            // calculate weight only before it's needed (to save some memory for the initial layout)
            if (state.debugOutput) {System.out.println((System.currentTimeMillis() - state.startTime) + " SM@"+(state.status = pos + ": Calculate weights..."));}
            this.weights = this.distances.clone().apply(x -> state.weightScalingFactor *Math.pow(x, state.weightPower));
            // calculate first values
            if (state.stressEpsilon != Double.NEGATIVE_INFINITY) { // only calculate if necessary
                if (state.debugOutput) {System.out.println((System.currentTimeMillis() - state.startTime) + " SM@"+(state.status = pos + ": Calculate initial stress..."));}
                this.currentStress = StressMinimizationLayout.calculateStress(this.currentPositions, this.distances, this.weights);
            }
            this.hasStopped = false;
            if (state.debugOutput) {System.out.println((System.currentTimeMillis() - state.startTime) + " SM@"+(state.status = pos + ": Preprocessing finished."));}
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
            if (state.debugOutput) {System.out.println((System.currentTimeMillis() - state.startTime) + " SM@"+this.pos +": Iterate new positions...");}
            List<Vector2d> newPositions = state.positionAlgorithm.nextIteration(
                    this.nodes, this.currentPositions, this.distances, this.weights);

            // check position change if necessary
            if (state.positionChangeEpsilon != Double.NEGATIVE_INFINITY) {
                if (state.debugOutput) {System.out.println((System.currentTimeMillis() - state.startTime) + " SM@"+this.pos +": Check position stop condition...");}
                if (differencePositionsSmallerEpsilon(newPositions, this.currentPositions, state.positionChangeEpsilon)) {
                    this.hasStopped = true;
                    if (state.debugOutput) {System.out.println((System.currentTimeMillis() - state.startTime) + " SM@"+this.pos +": Iteration finished.");}
                    return this.currentPositions = newPositions;
                }
            }


            double newStress = Double.NaN;
            // calculate new stress if necessary
            if (state.stressEpsilon != Double.NEGATIVE_INFINITY) {
                if (state.debugOutput) {System.out.println((System.currentTimeMillis() - state.startTime) + " SM@"+this.pos +": Calculate new stress...");}
                newStress = StressMinimizationLayout.calculateStress(newPositions, this.distances, this.weights);

                // shall we stop? is the change in stress or position is smaller than the given epsilon
                double stressChange = (this.currentStress - newStress)/this.currentStress;
                if (state.debugOutput) {System.out.println((System.currentTimeMillis() - state.startTime) + " SM@"+this.pos +": Check stress stop condition..." +
                        " (" + currentStress + " => " + newStress + ", " + stressChange + ")");}
                if (stressChange < state.stressEpsilon) {
                    this.hasStopped = true;
                }
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
            return "WorkUnit"+ pos +"{" +
                    "nodes=" + nodes +
                    ", distances=\n" + distances +
                    ", weights=\n" + weights +
                    ", currentStress=" + currentStress +
                    ", currentPositions=" + currentPositions +
                    ", hasStopped=" + hasStopped +
                    '}';
        }
    }
}
