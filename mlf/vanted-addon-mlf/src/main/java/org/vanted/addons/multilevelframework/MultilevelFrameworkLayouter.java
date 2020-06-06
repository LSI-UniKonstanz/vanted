package org.vanted.addons.multilevelframework;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.no_overlapp_as_tim.NoOverlappLayoutAlgorithmAS;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.random.RandomLayouterAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider;
import info.clearthought.layout.SingleFiledLayout;
import org.AttributeHelper;
import org.FolderPanel;
import org.JMButton;
import org.Vector2d;
import org.graffiti.editor.LoadSetting;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.*;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.view.View;
import org.graffiti.selection.Selection;
import org.graffiti.session.Session;
import org.vanted.addons.indexednodes.IndexedComponent;
import org.vanted.addons.indexednodes.IndexedGraphOperations;
import org.vanted.addons.indexednodes.IndexedNodeSet;
import org.vanted.addons.multilevelframework.sm_util.ConnectedComponentsHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Main class of the MLF add-on that contains the method that actually performs the layout.
 *
 * @author Gordian
 * @see AbstractEditorAlgorithm
 */
public class MultilevelFrameworkLayouter extends ThreadSafeAlgorithm {

    // store the available mergers, placers and algorithms
    public static final List<Merger> mergers = Collections.synchronizedList(new ArrayList<>());
    public static final List<Placer> placers = Collections.synchronizedList(new ArrayList<>());
    /**
     * Setting this attribute on a graph will indicate that it is currently part of an MLF run. This is to
     * prevent invoking the algorithm twice on the same graph.
     */
    final static String WORKING_ATTRIBUTE_PATH = "MLF_EXECUTING";

    // Default names of the attributes that indicate to the algorithms that the graphs they work on
    // coarsened graph instead of the original graph. The algorithm can use this for optimizations.
    // Currently only used by Stress Minimization.
    // TODO (bm review) these final fields should be put somewhere else
    // TODO (bm review) dont really need these
    private final static String COARSENING_LEVEL_INDICATOR_ATTRIBUTE_PATH = "GRAPH_IS_MLF_COARSENING_LEVEL";
    private final static String COARSENING_TOP_LEVEL_INDICATOR_ATTRIBUTE_PATH = "GRAPH_IS_MLF_COARSENING_TOP_LEVEL";
    private final static String COARSENING_BOTTOM_LEVEL_INDICATOR_ATTRIBUTE_PATH
            = "GRAPH_IS_MLF_COARSENING_BOTTOM_LEVEL";

    // add the default mergers and placers
    // todo (review bm) this static initialiser is a cool thing but it is not
    //  easy to see when exactly this will be modified (quick lookup says when class is "initialised")
    static {
        MultilevelFrameworkLayouter.mergers.add(new SolarMerger());
        MultilevelFrameworkLayouter.mergers.add(new RandomMerger());
        MultilevelFrameworkLayouter.placers.add(new RandomPlacer());
        MultilevelFrameworkLayouter.placers.add(new SolarPlacer());
    }

    /**
     * Set this to true to make execute() block until it is finished and use the specified mergers instead of
     * the ones selected through the GUI.
     */
    public boolean benchmarkMode = false;
    // merger, placer and algorithm to use in non-interactive (benchmark) mode
    public Merger nonInteractiveMerger = null;
    public Placer nonInteractivePlacer = null;
    public String nonInteractiveAlgorithm = null;

    private Graph graph;
    private Selection selection;

    /**
     * Contains the Swing UI components for the configuration (a.k.a "parameters"). At the same time, these
     * are used as a "model" (in the MVC sense).
     */
    private final MLFParamModel parameterModel = new MLFParamModel();

    public MultilevelFrameworkLayouter() {
        super();
    }

    /**
     * @see Algorithm#getDescription()
     */
    @Override
    public String getDescription() {
        return "<html><b>Multilevel Framework</b></html>";
    }

    @Override
    public ActionEvent getActionEvent() {
        return null;
    }

    @Override
    public void setActionEvent(ActionEvent a) {

    }

    /**
     * Calls {@code super.reset()} and updates the parameters.
     *
     * @see super#reset()
     */
    @Override
    public void reset() {
        // super.reset();
    }

    /**
     * @return the algorithm's name
     */
    public String getName() {
        return "Multilevel Framework Layouter";
    }

    /**
     * Make a status message for the GUI.
     * <p>
     * todo (review bm) javadoc
     */
    static String makeStatusMessage(int totalLevels, int currentLevel, int totalComponents,
                                    int currentComponent, String graphName) {
        return "Laying out level "
                + currentLevel
                + " (out of " + totalLevels + ")"
                + " of connected component "
                + currentComponent
                + " (out of " + totalComponents + ") of graph \""
                + graphName + "\"";
    }

    /**
     * Create a percentage for the GUI. Calculates the progress for the current connected component. todo
     * (review bm) javadoc
     */
    static double calculateProgress(MultilevelGraph mlg, int numComponents,
                                    int currentIndex, int numberOfLevelsAtStart) {
        if (numberOfLevelsAtStart == 0 || numComponents == 0) {
            return -1;
        }
        // calculate the progress as the percentage of nodes and connected components already processed
        return 100.0 * (1 - (double) mlg.getNumberOfLevels() / numberOfLevelsAtStart)
                * (currentIndex + 1.0)
                / numComponents;
    }

    /**
     * Does the same thing as {@link AbstractAlgorithm#getSelectedOrAllNodes()} which cannot be used here
     * because that uses instance variables which is not safe it the user runs the algorithm multiples times
     * at the same time on different graphs with different settings.
     *
     * @param graph     The {@link Graph} to use. Must not be {@code null}.
     * @param selection The {@link Selection} to use. May be {@code null}.
     * @return All nodes if the selection is empty or {@code null}, or the selected nodes otherwise.
     * @see AbstractAlgorithm#getSelectedOrAllNodes()
     */
    private static Collection<Node> getSelectedOrAllNodes(Graph graph, Selection selection) {
        if (selection == null || selection.getNodes().size() <= 0) {
            return graph.getNodes();
        } else {
            return selection.getNodes();
        }
    }

    /**
     * Checks, if a graph was given and that the radius is positive.
     *
     * @throws PreconditionException if no graph was given during algorithm invocation or the number of nodes
     *                               is zero or negative
     */
    @Override
    public void check() throws PreconditionException {
        if (graph == null) {
            throw new PreconditionException("Cannot run Multilevel Framework Layouter on null graph.");
        }
        if (graph.getNumberOfNodes() <= 0) {
            throw new PreconditionException("The graph is empty. Cannot run Multilevel Framework Layouter.");
        }
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[0];
    }

    @Override
    public void setParameters(Parameter[] params) {

    }

    /**
     * Performs the layout.
     * Can be executed on multiple graphs at the same time (by calling attach, execute, attach, execute...),
     * but only if the {@link Algorithm} used for the levels supports thread safe execution
     * (i.e. multiple execute methods running at the same time using the same instance).
     * This is only possible if the algorithm's execute method copies the parameter values or the
     * {@link org.graffiti.plugin.algorithm.ThreadSafeOptions} object in case of a
     * {@link org.graffiti.plugin.algorithm.ThreadSafeAlgorithm}.
     * Unless you can guarantee that the algorithm you're using does this, it is not recommended to run this on
     * multiple graphs at the same time.
     * @author Gordian
     */
    public void execute() {
        // need to save old session and view, because they need to be restored for applyUndoableNodePositionUpdate
        // to work
        final Session oldSession = MainFrame.getInstance().getActiveSession();
        final View oldView = oldSession.getActiveView();
        // all settings need to be stored as local variables, because the user might want to execute on multiple
        // graphs at the same time with different settings, which would change the instance variables
        // todo (review bm) cannot really guarantee that this is the case right now, check this
        final Selection selection = this.selection;
        final boolean removeBends = true; // todo: remove
        final boolean removeOverlaps = true; // todo: remove
        final boolean randomTop = this.parameterModel.layoutAlgGroup.isRandomTop();
        final Merger merger;
        final Placer placer;
        final LayoutAlgorithmWrapper layouter;
        if (!this.benchmarkMode) { // otherwise the caller needs to set those values todo (bm review) not done?
            merger = this.getSelectedMergerCopyAndSetParameters();
            placer = this.getSelectedPlacerCopyAndSetParameters();
            layouter = this.parameterModel.layoutAlgGroup.getSelected();
        } else {
            merger = MlfHelper.tryMakingNewInstance(this.nonInteractiveMerger);
            placer = MlfHelper.tryMakingNewInstance(this.nonInteractivePlacer);
            layouter = this.parameterModel.layoutAlgGroup.getSelected();
        }

        // check if the graph is currently being worked on by another MLF instance/thread
        // this is necessary to prevent the user from double clicking the layout button to start the layout twice
        // (VANTED does nothing to prevent this)
        if (graph.getAttributes().getCollection().containsKey(WORKING_ATTRIBUTE_PATH)
                && graph.getBoolean(WORKING_ATTRIBUTE_PATH)) {
            MainFrame.getInstance().showMessageDialog("Cannot run MLF twice on the same graph at the same time!");
            return;
        }
        // if not, indicate that the MLF is running on the graph
        graph.setBoolean(WORKING_ATTRIBUTE_PATH, true);

        // todo: use logger
        System.out.println("Running MLF using " + merger.getName() + ", " + placer.getName() + ", "
                + layouter.getAlgorithm().getName());

        // remember the base level (of each induced component) to later access its nodes and, in
        // particular, their new positions
        List<LevelGraph> componentBaseLevels = new LinkedList<>();

        final MLFBackgroundTaskStatus bts = new MLFBackgroundTaskStatus();
        // this is where all the work happens
        final Runnable mainTask = () -> {
            if (removeBends) {
                GraphHelper.removeAllBends(graph, true);
            }

            // the connected components of the subgraph induced by the selection, of the
            // entire graph if there is no selection.
            List<IndexedComponent> components = IndexedGraphOperations.getComponents(
                    IndexedNodeSet.setOfAllIn(
                            getSelectedOrAllNodes(graph, selection)
                    )
            );


            int currentComponentIndex = 0;
            // layout each connected component
            for (IndexedComponent component : components) {
                currentComponentIndex++;
                if (bts.stopRequested) {
                    bts.status = -1;
                    return;
                }

                LevelGraph componentBaseLevel = LevelGraph.fromIndexedComponent(component);
                componentBaseLevels.add(componentBaseLevel);
                // we run the MLF on each CC separately -- hence one MultilevelGraph per CC
                MultilevelGraph componentMLG = new MultilevelGraph(
                        // todo: to avoid constructing entirely new graphs,
                        //   but still support operating only on induced subgraphs,
                        //   we would need a proper data structure for that
                        componentBaseLevel
                );

                merger.buildCoarseningLevels(componentMLG);
                // TODO (bm review) would like componentMLG.coarsen(merger) better.

                // keep track of how many nodes coarsening levels there were at the start (for the progress bar)
                final int numLevelsAtStart = componentMLG.getNumberOfLevels();
                // indicate that this is a coarsened graph to allow for optimizations in the level layouter
                componentMLG.getTopLevel().setBoolean(COARSENING_LEVEL_INDICATOR_ATTRIBUTE_PATH, true);
                // indicate that this is the top level
                componentMLG.getTopLevel().setBoolean(COARSENING_TOP_LEVEL_INDICATOR_ATTRIBUTE_PATH, true);

                if (bts.stopRequested) {
                    bts.status = -1;
                    return;
                }
                reportStatus(bts, numLevelsAtStart, componentMLG, components, currentComponentIndex);

                if (randomTop) {
                    randomLayout(componentMLG.getTopLevel());
                }

                while (componentMLG.getNumberOfLevels() > 1) {
                    if (componentMLG.getTopLevel().getNumberOfNodes() >= 2) {
                        layouter.execute(componentMLG.getTopLevel());
                    }
                    if (removeOverlaps) {
                        removeOverlaps(componentMLG.getTopLevel());
                    }
                    placer.reduceCoarseningLevel(componentMLG);
                    // indicate that this is a coarsened graph to allow for optimizations in the level layouter
                    // TODO (bm review) needed? i think any other level than the 1st one would be coarsened by construction
                    componentMLG.getTopLevel().setBoolean(COARSENING_LEVEL_INDICATOR_ATTRIBUTE_PATH, true);
                    if (bts.stopRequested) {
                        bts.status = -1;
                        return;
                    }
                    reportStatus(bts, numLevelsAtStart, componentMLG, components, currentComponentIndex);
                }

                // indicate that this is the bottom level
                // TODO (bm review) naming -- what is top/bottom?
                componentMLG.getTopLevel().setBoolean(COARSENING_BOTTOM_LEVEL_INDICATOR_ATTRIBUTE_PATH, true);

                reportStatus(bts, numLevelsAtStart, componentMLG, components, currentComponentIndex);

                layouter.execute(componentMLG.getTopLevel());

                if (removeOverlaps) {
                    removeOverlaps(componentMLG.getTopLevel());
                }

                reportDone(bts);
            }
        };

        // a wrapper around the main task to handle exceptions
        final Runnable attributeSafeTask = () -> {
            try {
                mainTask.run();
            } catch (Exception e) { // need to remove the "MLF running" attribute
                // make sure the running attribute is removed if an exception is thrown
                graph.setBoolean(WORKING_ATTRIBUTE_PATH, false);
                throw e; // rethrow
            }
        };

        // this will be invoked when MLF is done
        // todo (review bm) position this piece of code somewhere else
        final Runnable finishSwingTask = () -> {
            // apply position updates, i.e. use positions from MergedNodes
            HashMap<Node, Vector2d> nodes2newPositions = new HashMap<>();

            for (LevelGraph comp : componentBaseLevels) {
                for (MergedNode mn : comp.getMergedNodes()) {
                    // if this is the base level, a MergedNode represents a single node
                    // that is also part of the displayed graph. So, applying the position
                    // update to that node layouts the actual displayed graph.
                    final Node representedNode = mn.getInnerNodes().iterator().next();
                    nodes2newPositions.put(representedNode, AttributeHelper.getPositionVec2d(mn));
                }
            }

            MainFrame.getInstance().setActiveSession(oldSession, oldView);
            GraphHelper.applyUndoableNodePositionUpdate(nodes2newPositions, getName());

            // position connected components in the view.
            // todo: get rid of duplicate code (ConnectedComponentsHelper)
            Set<List<Node>> componentsByNodes = new HashSet<>(); // todo better DS?
            componentBaseLevels.forEach((cpt) -> componentsByNodes.add(cpt.getNodes()));
            ConnectedComponentsHelper.layoutConnectedComponents(
                    componentsByNodes,
                    true
            );
            graph.setBoolean(WORKING_ATTRIBUTE_PATH, false);
        };

        // actually issue the job to be executed
        BackgroundTaskHelper.issueSimpleTask(this.getName(), "Multilevel Framework is running",
                attributeSafeTask, finishSwingTask, bts);
    }

    private void reportDone(MLFBackgroundTaskStatus bts) {
        bts.statusMessage = "Finished laying out the levels";
        bts.status = -1;
    }

    @Override
    public void attach(Graph g, Selection selection) {
        this.graph = g;
        this.selection = selection;
    }

    /*
     * @see org.graffiti.plugin.algorithm.Algorithm#getCategory()
     */
    @Override
    public String getCategory() {
        return "Layout";
    }

    @Override
    public Set<Category> getSetCategory() {
        return null;
    }

    @Override
    public String getMenuCategory() {
        return null;
    }

    /**
     * This makes sure the algorithm is moved to the layout-tab of VANTED
     *
     * @return {@code true}
     */
    @Override
    public boolean isLayoutAlgorithm() {
        return true;
    }


    /**
     * Add a new {@link Merger} that will be available for the user to choose.
     * This method is meant to be called by other VANTED add-ons that seek to extend the MLF with new {@link Merger}s.
     * @param merger
     *      The {@link Merger} to add. Must not be {@code null}.
     * @author Gordian
     */
    public static void addMerger(Merger merger) {
        Objects.requireNonNull(merger, "Cannot add null Merger to the MLF");
        if (!MultilevelFrameworkLayouter.mergers.contains(merger)) {
            MultilevelFrameworkLayouter.mergers.add(merger);
        }
    }

    /**
     * @return the currently registered {@link Merger}s as an unmodifiable {@link Collection}.
     * @author Gordian
     */
    public static Collection<Merger> getMergers() {
        return Collections.unmodifiableList(MultilevelFrameworkLayouter.mergers);
    }

    /**
     * Add a new {@link Placer} that will be available for the user to choose.
     * This method is meant to be called by other VANTED add-ons that seek to extend the MLF with new {@link Placer}s.
     * @param placer
     *      The {@link Placer} to add. Must not be {@code null}.
     * @author Gordian
     */
    public static void addPlacer(Placer placer) {
        Objects.requireNonNull(placer, "Cannot add null placer to the MLF");
        if (!MultilevelFrameworkLayouter.placers.contains(placer)) {
            MultilevelFrameworkLayouter.placers.add(placer);
        }
    }

    /**
     * @return the currently registered {@link Placer}s as an unmodifiable {@link Collection}.
     * @author Gordian
     */
    public static Collection<Placer> getPlacers() {
        return Collections.unmodifiableList(MultilevelFrameworkLayouter.placers);
    }

    /**
     * Calls {@link GraphHelper#diplayGraph(Graph)} in the event dispatcher thread (it throws exceptions if called from
     * other threads).
     * @param graph
     *      The {@link Graph} to pass along.
     * @author Gordian
     */
    static void display(Graph graph) {
        try {
            SwingUtilities.invokeAndWait(() -> {
                MainFrame.getInstance().showGraph(graph, null, LoadSetting.VIEW_CHOOSER_NEVER);
            });
        } catch (InterruptedException | InvocationTargetException ignored) {
        }
    }

    private void reportStatus(MLFBackgroundTaskStatus bts, int numLevelsAtStart, MultilevelGraph componentMLG, List<IndexedComponent> components, int currentComponentIndex) {
        bts.statusMessage = makeStatusMessage(numLevelsAtStart,
                componentMLG.getNumberOfLevels(), components.size(),
                currentComponentIndex, graph.getName());
        bts.status = calculateProgress(componentMLG, components.size(),
                currentComponentIndex, numLevelsAtStart);
    }

    /**
     * @return
     *     the {@link Merger} selected by the user and set it up using the parameters specified by the user through
     *     the GUI.
     * @author Gordian
     * TODO (bm review) naming
     */
    private Merger getSelectedMergerCopyAndSetParameters() {
        final Merger merger = MlfHelper.tryMakingNewInstance(this.parameterModel.mergerGroup.getSelected());
        merger.setParameters(this.parameterModel.mergerGroup.getUpdatedParameters());
        return merger;
    }

    /**
     * @return
     *     the {@link Placer} selected by the user and set it up using the parameters specified by the user through
     *     the GUI.
     * TODO (bm review) naming
     * @author Gordian
     */
    private Placer getSelectedPlacerCopyAndSetParameters() {
        final Placer placer = MlfHelper.tryMakingNewInstance(this.parameterModel.placerGroup.getSelected());
        placer.setParameters(this.parameterModel.placerGroup.getUpdatedParameters());
        return placer;
    }

    private void stopTaskIfRequested(MLFBackgroundTaskStatus bts) {
        // something todo here?
    }

    static void randomLayout(Graph graph) {
        final RandomLayouterAlgorithm rla = new RandomLayouterAlgorithm();
        rla.attach(graph, new Selection());
        rla.execute();
    }

    /**
     * Apply the "no overlap" algorithm to the given graph.
     * @param graph
     *      The graph to layout. Must not be {@code null}.
     * @author Gordian
     */
    static void removeOverlaps(Graph graph) {
        Algorithm noOverlapAlgorithm = new NoOverlappLayoutAlgorithmAS(1, 1);
        noOverlapAlgorithm.attach(graph, new Selection());
        noOverlapAlgorithm.execute();
    }

    @Override
    public boolean setControlInterface(ThreadSafeOptions __, JComponent jc) {

        this.graph = MainFrame.getInstance().getActiveEditorSession().getGraph();
        this.selection = MainFrame.getInstance().getActiveEditorSession().getSelectionModel().getActiveSelection();

        MLFParamModel paramUIElems = this.parameterModel;
        // MLFParamModel paramUIElems = new MLFParamModel(); // debug only

        jc.setBorder(new EmptyBorder(5, 5, 5, 5));


        JMButton startButton = new JMButton("Layout Network");
        startButton.addActionListener(e -> this.execute());
        jc.add(startButton);

        SingleFiledLayout sfl = new SingleFiledLayout(SingleFiledLayout.COLUMN, SingleFiledLayout.FULL, 1);
        jc.setLayout(sfl);

        final FolderPanel layoutFolder = new FolderPanel("Level layout", false, true, false, null);
        layoutFolder.addComp(paramUIElems.layoutAlgGroup);
        layoutFolder.layoutRows();
        jc.add(layoutFolder);

        final FolderPanel mergerFolder = new FolderPanel("Merging", false, true, false, null);
        mergerFolder.addComp(paramUIElems.mergerGroup);
        mergerFolder.layoutRows();
        jc.add(mergerFolder);

        final FolderPanel placerFolder = new FolderPanel("Placing", false, true, false, null);
        placerFolder.addComp(paramUIElems.placerGroup);
        placerFolder.layoutRows();
        jc.add(placerFolder);


        jc.validate();
        return true;
    }

    @Override
    public void executeThreadSafe(ThreadSafeOptions options) {
        this.execute();
    }

    @Override
    public void resetDataCache(ThreadSafeOptions options) {

    }
}


/**
 * @author Gordian
 * @see BackgroundTaskStatusProvider
 * TODO (review bm) should go somewhere else?
 */
class MLFBackgroundTaskStatus implements BackgroundTaskStatusProvider {
    /**
     * This is set to {@code true} if the user clicks the stop button.
     */
    boolean stopRequested = false;
    /**
     * Current state of the progress bar.
     */
    double status = -1;
    /**
     * Status message displayed above the progress bar in VANTED.
     */
    String statusMessage = "";

    /**
     * @see org.BackgroundTaskStatusProvider#getCurrentStatusValue()
     */
    @Override public int getCurrentStatusValue() { return (int) Math.ceil(this.getCurrentStatusValueFine()); }

    /**
     * @see org.BackgroundTaskStatusProvider#setCurrentStatusValue(int)
     */
    @Override public void setCurrentStatusValue(int value) { this.status = value; }

    /**
     * @see org.BackgroundTaskStatusProvider#getCurrentStatusValueFine()
     */
    @Override public double getCurrentStatusValueFine() { return this.status; }

    /**
     * @see org.BackgroundTaskStatusProvider#getCurrentStatusMessage1()
     */
    @Override public String getCurrentStatusMessage1() { return this.statusMessage; }

    /**
     * @see org.BackgroundTaskStatusProvider#getCurrentStatusMessage2()
     */
    @Override
    public String getCurrentStatusMessage2() {
        return null;
    }

    /**
     * @see org.BackgroundTaskStatusProvider#pleaseStop()
     */
    @Override public void pleaseStop() { this.stopRequested = true; }

    /**
     * @see org.BackgroundTaskStatusProvider#pluginWaitsForUser()
     */
    @Override public boolean pluginWaitsForUser() { return false; }

    /**
     * @see org.BackgroundTaskStatusProvider#pleaseContinueRun()
     */
    @Override public void pleaseContinueRun() { }
}
