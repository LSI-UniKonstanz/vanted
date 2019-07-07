package org.vanted.addons.multilevelframework;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.no_overlapp_as_tim.NoOverlappLayoutAlgorithmAS;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.random.RandomLayouterAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider;
import org.AttributeHelper;
import org.Vector2d;
import org.apache.commons.lang.ArrayUtils;
import org.graffiti.editor.LoadSetting;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.AbstractEditorAlgorithm;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.JComponentParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.view.View;
import org.graffiti.selection.Selection;
import org.graffiti.session.Session;
import org.vanted.addons.multilevelframework.pse_hack.BlockingForceDirected;
import org.vanted.addons.multilevelframework.sm_util.ConnectedComponentsHelper;
import org.vanted.addons.multilevelframework.sm_util.gui.ParameterizableSelectorParameter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Main class of the MLF add-on that contains the method that actually performs the layout.
 * @see AbstractEditorAlgorithm
 * @author Gordian
 */
public class MultilevelFrameworkLayouter extends AbstractEditorAlgorithm {

    // store the available mergers, placers and algorithms
    private Map<String, LayoutAlgorithmWrapper> layoutAlgorithms;
    private static List<Merger> mergers = Collections.synchronizedList(new ArrayList<>());
    private static List<Placer> placers = Collections.synchronizedList(new ArrayList<>());

    // default values
    private final static String DEFAULT_ALGORITHM = BlockingForceDirected.springName;
    private final static String DEFAULT_PLACER = new RandomPlacer().getName();
    private final static String DEFAULT_MERGER = new RandomMerger().getName();
    private boolean randomTop = true;
    private boolean removeBends = false;
    private boolean removeOverlaps = false;

    // Default names of the attributes that indicate to the algorithms that the graphs they work on
    // coarsened graph instead of the original graph. The algorithm can use this for optimizations.
    // Currently only used by Stress Minimization.
    private final static String COARSENING_LEVEL_INDICATOR_ATTRIBUTE_PATH = "GRAPH_IS_MLF_COARSENING_LEVEL";
    private final static String COARSENING_TOP_LEVEL_INDICATOR_ATTRIBUTE_PATH = "GRAPH_IS_MLF_COARSENING_TOP_LEVEL";
    private final static String COARSENING_BOTTOM_LEVEL_INDICATOR_ATTRIBUTE_PATH
            = "GRAPH_IS_MLF_COARSENING_BOTTOM_LEVEL";
    final static String WORKING_ATTRIBUTE_PATH = "MLF_EXECUTING";

    // fields for the GUI objects / the parameter system
    private JComboBox<String> algorithmListComboBox;
    private JButton setUpLayoutAlgorithmButton;
    private String lastSelectedAlgorithm = DEFAULT_ALGORITHM;
    private String lastSelectedPlacer = DEFAULT_PLACER;
    private String lastSelectedMerger = DEFAULT_MERGER;
    private int randomTopParameterIndex = 0;
    private int removeBendsParameterIndex = 0;
    private int removeOverlapsParameterIndex = 0;
    private int mergerPSPIndex = 0;
    private int placerPSPIndex = 0;
    private ParameterizableSelectorParameter mergerPSP;
    private ParameterizableSelectorParameter placerPSP;

    /**
     * Set this to true to make execute() block until it is finished and use the specified mergers instead of the ones
     * selected through the GUI.
     */
    public boolean benchmarkMode = false;
    // merger, placer and algorithm to use in non-interactive (benchmark) mode
    public Merger nonInteractiveMerger = null;
    public Placer nonInteractivePlacer = null;
    public String nonInteractiveAlgorithm = null;

    // add the default mergers and placers
    static {
        MultilevelFrameworkLayouter.mergers.add(new SolarMerger());
        MultilevelFrameworkLayouter.mergers.add(new RandomMerger());
        MultilevelFrameworkLayouter.placers.add(new RandomPlacer());
        MultilevelFrameworkLayouter.placers.add(new SolarPlacer());
    }

    public MultilevelFrameworkLayouter() {
        super();
        this.setUpParameters();
    }

    /**
     * @see Algorithm#getDescription()
     */
    @Override
    public String getDescription() {
        return "<html><b>Multilevel Framework</b></html>";
    }

    /**
     * Calls {@code super.reset()} and updates the parameters.
     * @see super#reset()
     */
    @Override
    public void reset() {
        super.reset();
        this.updateParameters();
    }

    /**
     * @return the algorithm's name
     */
    public String getName() {
        return "Multilevel Framework Layouter";
    }

    /**
     * Checks, if a graph was given and that the radius is positive.
     *
     * @throws PreconditionException if no graph was given during algorithm invocation or the
     *                               number of nodes is zero or negative
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
        final Graph graph = this.graph;
        final Selection selection = this.selection;
        final boolean removeBends = this.removeBends;
        final boolean removeOverlaps = this.removeOverlaps;
        final boolean randomTop = this.randomTop;
        final Merger merger;
        final Placer placer;
        final LayoutAlgorithmWrapper algorithm;
        if (!this.benchmarkMode) { // otherwise the caller needs to set those values
            merger = this.getSelectedMergerCopyAndSetParameters();
            placer = this.getSelectedPlacerCopyAndSetParameters();
            algorithm = this.layoutAlgorithms.get(Objects.toString(this.algorithmListComboBox.getSelectedItem()));
        } else {
            merger = MlfHelper.tryMakingNewInstance(this.nonInteractiveMerger);
            placer = MlfHelper.tryMakingNewInstance(this.nonInteractivePlacer);
            algorithm = this.layoutAlgorithms.get(this.nonInteractiveAlgorithm);
        }
        final List<?extends CoarsenedGraph>[] connectedComponents = new List[1];

        final MLFBackgroundTaskStatus bts = new MLFBackgroundTaskStatus();

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

        System.out.println("Running MLF using " + merger.getName() + ", " + placer.getName() + ", "
                + algorithm.getAlgorithm().getName());

        final Runnable backgroundTask = () -> {
            if (removeBends) {
                GraphHelper.removeAllBends(graph, true);
            }
            // split the subgraph induced by the selection into connected components
            connectedComponents[0] = MlfHelper.calculateConnectedComponentsOfSelection(
                            new HashSet<>(getSelectedOrAllNodes(graph, selection)));

            final Selection emptySelection = new Selection();

            // layout each connected component
            for (int i = 0; i < connectedComponents[0].size(); i++) {
                final CoarsenedGraph cg = connectedComponents[0].get(i);
                if (bts.isStopped) { bts.status = -1; return; }
                MultilevelGraph componentMLG = new MultilevelGraph(cg);
                merger.buildCoarseningLevels(componentMLG);

                // keep track of how many nodes coarsening levels there were at the start (for the progress bar)
                final int numLevelsAtStart = componentMLG.getNumberOfLevels();
                // indicate that this is a coarsened graph to allow for optimizations in the level layouter
                componentMLG.getTopLevel().setBoolean(COARSENING_LEVEL_INDICATOR_ATTRIBUTE_PATH, true);
                // indicate that this is the top level
                componentMLG.getTopLevel().setBoolean(COARSENING_TOP_LEVEL_INDICATOR_ATTRIBUTE_PATH, true);

                if (bts.isStopped) { bts.status = -1; return; }

                if (randomTop) { randomLayout(componentMLG.getTopLevel()); }

                while (componentMLG.getNumberOfLevels() > 1) {
                    bts.statusMessage = makeStatusMessage(numLevelsAtStart, componentMLG.getNumberOfLevels(),
                            connectedComponents[0], i, graph.getName());
                    bts.status = calculateProgress(componentMLG, connectedComponents[0], i, numLevelsAtStart);
//                    display(componentMLG.getTopLevel());
                    // force directed sometimes takes tens of seconds to "layout" a single node
                    if (componentMLG.getTopLevel().getNumberOfNodes() >= 2) {
                        algorithm.execute(componentMLG.getTopLevel(), emptySelection);
                    }
                    if (removeOverlaps) { removeOverlaps(componentMLG.getTopLevel()); }
                    placer.reduceCoarseningLevel(componentMLG);
                    // indicate that this is a coarsened graph to allow for optimizations in the level layouter
                    componentMLG.getTopLevel().setBoolean(COARSENING_LEVEL_INDICATOR_ATTRIBUTE_PATH, true);
                    if (bts.isStopped) { bts.status = -1; return; }
                }

                // indicate that this is the bottom level
                componentMLG.getTopLevel().setBoolean(COARSENING_BOTTOM_LEVEL_INDICATOR_ATTRIBUTE_PATH, true);
                bts.status = calculateProgress(componentMLG, connectedComponents[0], i, numLevelsAtStart);
                bts.statusMessage = makeStatusMessage(numLevelsAtStart, componentMLG.getNumberOfLevels(),
                        connectedComponents[0], i, graph.getName());
//                display(componentMLG.getTopLevel());
                algorithm.execute(componentMLG.getTopLevel(), emptySelection);
                if (removeOverlaps) { removeOverlaps(componentMLG.getTopLevel()); }
                bts.statusMessage = "Finished laying out the levels";
                bts.status = -1;
            }
        };

        final Runnable finishSwingTask = () -> {
            // apply position updates
            HashMap<Node, Vector2d> nodes2newPositions = new HashMap<>();

            for (CoarsenedGraph cg : connectedComponents[0]) {
                for (MergedNode mn : cg.getMergedNodes()) {
                    final Node representedNode = mn.getInnerNodes().iterator().next();
                    assert mn.getInnerNodes().size() == 1 : "More than one node represented in level 0";
                    nodes2newPositions.put(representedNode, AttributeHelper.getPositionVec2d(mn));
                }
            }

            MainFrame.getInstance().setActiveSession(oldSession, oldView);
            GraphHelper.applyUndoableNodePositionUpdate(nodes2newPositions, getName());
//            ConnectedComponentLayout.layoutConnectedComponents(graph);
            ConnectedComponentsHelper.layoutConnectedComponents(ConnectedComponentsHelper.getConnectedComponents(
                    getSelectedOrAllNodes(graph, selection)), true);
            graph.setBoolean(WORKING_ATTRIBUTE_PATH, false);
        };

        if (this.benchmarkMode) { // this is basically only useful for benchmarking
            backgroundTask.run();
            try {
                SwingUtilities.invokeAndWait(finishSwingTask);
            } catch (InterruptedException | InvocationTargetException e) {
                e.printStackTrace();
            }
        } else { // the normal case, when the algorithm is executed interactively
            // make sure the running attribute is removed if an exception is thrown
            final Runnable tryBackgroundTask = () -> {
                try {
                    backgroundTask.run();
                } catch (Exception e) { // need to remove the "MLF running" attribute
                    graph.setBoolean(WORKING_ATTRIBUTE_PATH, false);
                    throw e; // rethrow
                }
            };
            BackgroundTaskHelper.issueSimpleTask(this.getName(), "Multilevel Framework is running",
                    tryBackgroundTask, finishSwingTask, bts);
        }
    }

    /**
     * @return the parameter array
     */
    @Override
    public Parameter[] getParameters() {
        this.updateParameters();
        return this.parameters;
    }

    /**
     * Does the same thing as {@link AbstractAlgorithm#getSelectedOrAllNodes()} which cannot be used here
     * because that uses instance variables which is not safe it the user runs the algorithm multiples times at the
     * same time on different graphs with different settings.
     * @param graph
     *      The {@link Graph} to use. Must not be {@code null}.
     * @param selection
     *      The {@link Selection} to use. May be {@code null}.
     * @return
     *      All nodes if the selection is empty or {@code null} or the selected nodes otherwise.
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
     * Sets the parameters to the given array.
     * @param params The new parameters.
     * @see Algorithm#setParameters(Parameter[])
     */
    @Override
    public void setParameters(Parameter[] params) {
        this.parameters = params;
        this.randomTop = (Boolean) this.parameters[this.randomTopParameterIndex].getValue();
        this.removeBends = (Boolean) this.parameters[this.removeBendsParameterIndex].getValue();
        this.removeOverlaps = (Boolean) this.parameters[this.removeOverlapsParameterIndex].getValue();
        this.mergerPSP = (ParameterizableSelectorParameter) this.parameters[this.mergerPSPIndex].getValue();
        this.placerPSP = (ParameterizableSelectorParameter) this.parameters[this.placerPSPIndex].getValue();
    }

    /*
     * @see org.graffiti.plugin.algorithm.Algorithm#getCategory()
     */
    @Override
    public String getCategory() {
        return "Layout";
    }

    /**
     * This makes sure the algorithm is moved to the layout-tab of VANTED
     * @return {@code true}
     */
    @Override
    public boolean isLayoutAlgorithm() {
        return true;
    }

    /**
     * @see org.graffiti.plugin.algorithm.EditorAlgorithm#activeForView(View)
     */
    @Override
    public boolean activeForView(View v) {
        return v != null;
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
        } catch (InterruptedException | InvocationTargetException ignored) { }
    }

    /**
     * Make a status message for the GUI.
     * @param totalLevels
     *      The total number of coarsening levels.
     * @param level
     *      The current level.
     * @param connectedComponents
     *      The connected components of the current graph. Must not be {@code null}.
     * @param current
     *      The index of the connected component that is currently being laid out.
     * @param graphName
     *      The graph's name. Must not be {@code null}.
     * @return
     *      the status message.
     * @author Gordian
     */
    static String makeStatusMessage(int totalLevels, int level, List<?extends CoarsenedGraph> connectedComponents,
                                     int current, String graphName) {
        return "Laying out level "
                + level
                + " (out of " + totalLevels + ")"
                + " of connected component "
                + (current + 1)
                + " (out of " + connectedComponents.size() + ") of graph \""
                + graphName + "\"";
    }

    /**
     * @return
     *     the {@link Merger} selected by the user and set it up using the parameters specified by the user through
     *     the GUI.
     * @author Gordian
     */
    private Merger getSelectedMergerCopyAndSetParameters() {
        final Merger merger = MlfHelper.tryMakingNewInstance((Merger) this.mergerPSP.getSelectedParameterizable());
        this.lastSelectedMerger = merger.getName();
        merger.setParameters(this.mergerPSP.getUpdatedParameters());
        return merger;
    }

    /**
     * @return
     *     the {@link Placer} selected by the user and set it up using the parameters specified by the user through
     *     the GUI.
     * @author Gordian
     */
    private Placer getSelectedPlacerCopyAndSetParameters() {
        final Placer placer = MlfHelper.tryMakingNewInstance((Placer) this.placerPSP.getSelectedParameterizable());
        this.lastSelectedPlacer = placer.getName();
        placer.setParameters(this.placerPSP.getUpdatedParameters());
        return placer;
    }

    /**
     * Create a percentage for the GUI. Calculates the progress for the current connected component.
     * @param mlg
     *      The {@link MultilevelGraph}. Must not be {@code null}.
     * @param connectedComponents
     *      The list of connected components for the graph that is currently being processed.
     * @param currentIndex
     *      The index of the connected component that is currently being processed.
     * @param numberOfLevelsAtStart
     *      The total number of coarsening levels.
     * @return
     *      the calculated progress
     * @author Gordian
     */
    static double calculateProgress(MultilevelGraph mlg, List<?extends CoarsenedGraph> connectedComponents,
                                     int currentIndex, int numberOfLevelsAtStart) {
        if (numberOfLevelsAtStart == 0 || connectedComponents.size() == 0) { return -1; }
        // calculate the progress as the percentage of nodes and connected components already processed
        return 100.0 * (1 - (double) mlg.getNumberOfLevels() / numberOfLevelsAtStart)
                * (currentIndex + 1.0)
                / connectedComponents.size();
    }

    /**
     * The handler that gets called if the "Set up Layouter" button was clicked.
     * @param ignored
     *      Ignored.
     * @author Gordian
     */
    void clickSetUpLayoutAlgorithmButton(ActionEvent ignored) {
        final LayoutAlgorithmWrapper current = this.layoutAlgorithms.get(this.lastSelectedAlgorithm);
        final JComponent gui = current.getGUI();
        if (gui != null) {
            JOptionPane.showMessageDialog(MainFrame.getInstance(), gui,
                    "Set up " + this.lastSelectedAlgorithm, JOptionPane.PLAIN_MESSAGE);
        }
    }

    /**
     * The handler that gets called if the user changes to a different layout algorithm for the levels.
     * @param ignored
     *      Ignored.
     * @author Gordian
     */
    private void changeSelectedAlgorithm(ActionEvent ignored) {
        final String selected = (String) this.algorithmListComboBox.getSelectedItem();
        if (selected == null) {
            return;
        }
        this.lastSelectedAlgorithm = selected;
    }

    /**
     * Create the parameter objects.
     * @author Gordian
     */
    private void setUpParameters() {
        this.layoutAlgorithms = new HashMap<>();
        this.updateParameters();
    }

    /**
     * Apply a random layout to the given graph.
     * @param graph
     *      The graph to layout. Must not be {@code null}.
     * @author Gordian
     */
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

    /**
     * Updates the parameter object. Among other things this puts new {@link Merger}s, {@link Algorithm}s
     * or {@link Placer}s into the GUI.
     * @author Gordian
     */
    private void updateParameters() {
        Map<String, LayoutAlgorithmWrapper> tmp = LayoutAlgorithmWrapper.getLayoutAlgorithms();
        for (Map.Entry<String, LayoutAlgorithmWrapper> e : tmp.entrySet()) {
            if (!this.layoutAlgorithms.containsKey(e.getKey())) {
                this.layoutAlgorithms.put(e.getKey(), e.getValue());
            }
        }
        // the algorithm that used to be selected no longer exists
        if (!this.layoutAlgorithms.containsKey(this.lastSelectedAlgorithm)) {
            MainFrame.showMessageDialog("Algorithm \"" + this.layoutAlgorithms + "\" doesn't seem to exist.",
                    "Error");
            this.lastSelectedAlgorithm = this.layoutAlgorithms.keySet().contains(this.lastSelectedAlgorithm) ?
                    this.lastSelectedAlgorithm : this.layoutAlgorithms.keySet().iterator().next();
        }

        // combobox that lets the user choose an algorithm
        if (this.algorithmListComboBox != null) {
            for (ActionListener actionListener : this.algorithmListComboBox.getActionListeners()) {
                this.algorithmListComboBox.removeActionListener(actionListener);
            }
        }
        this.algorithmListComboBox = new JComboBox<>(this.layoutAlgorithms.keySet().stream()
                .sorted().toArray(String[]::new));
        this.algorithmListComboBox.addActionListener(this::changeSelectedAlgorithm);

        JComponentParameter algorithmList = new JComponentParameter(this.algorithmListComboBox,
                "Level Layout Algorithm",
                "Layout Algorithm to be run on each level of the coarsened graph.");
        this.algorithmListComboBox.setSelectedItem(this.lastSelectedAlgorithm);

        if (this.setUpLayoutAlgorithmButton != null) {
            for (ActionListener actionListener : this.setUpLayoutAlgorithmButton.getActionListeners()) {
                this.setUpLayoutAlgorithmButton.removeActionListener(actionListener);
            }
        }
        this.setUpLayoutAlgorithmButton = new JButton("Set up layout algorithm");
        this.setUpLayoutAlgorithmButton.addActionListener(this::clickSetUpLayoutAlgorithmButton);

        JComponentParameter setUpLayoutAlgorithmButtonParameter =
                new JComponentParameter(this.setUpLayoutAlgorithmButton, "",
                        "Click the button to change the parameters of the layout algorithm.");

        BooleanParameter randomLayoutParameter = new BooleanParameter(this.randomTop,
                "Random init on top",
                "Do an random layout on the top (i.e. coarsest) coarsening level");

        BooleanParameter removeBendsParameter = new BooleanParameter(this.removeBends,
                "Remove edge bends",
                "Remove all edge bends from the graph");

        BooleanParameter removeOverlapsParameter = new BooleanParameter(this.removeOverlaps,
                "Remove overlaps",
                "Remove overlaps using VANTED's builtin no-overlap-algorithm after each level.");


        // needs to be a copy as the synchronized list "mergers" cannot be iterated over without a synchronized
        // block
        ArrayList<Merger> tmpMergers = new ArrayList<>(MultilevelFrameworkLayouter.mergers);
        tmpMergers.sort(Comparator.comparing(Merger::getName));
        // select the default or last selected merger
        int mergerIndex = tmpMergers.stream()
                .filter(m -> m.getName().equals(this.lastSelectedMerger))
                .map(tmpMergers::indexOf).findAny().orElse(0);
        JComponentParameter mergerPSP = ParameterizableSelectorParameter.getFromList(mergerIndex,
                tmpMergers, this.selection, "Choose the merger",
                "The merger is used to merge multiple nodes into one in order " +
                        "to create the coarsening levels.");

        // see above
        ArrayList<Placer> tmpPlacers = new ArrayList<>(MultilevelFrameworkLayouter.placers);
        tmpPlacers.sort(Comparator.comparing(Placer::getName));
        // select the default or last selected placer
        int placerIndex = tmpPlacers.stream()
                .filter(p -> p.getName().equals(this.lastSelectedPlacer))
                .map(tmpPlacers::indexOf).findAny().orElse(0);
        JComponentParameter placerPSP = ParameterizableSelectorParameter.getFromList(placerIndex,
                tmpPlacers, this.selection, "Choose the placer",
                "The placer determines how the nodes contained within merged nodes are placed back into"
                        + " the graph during the uncoarsening process.");

        this.parameters = new Parameter[]{algorithmList, setUpLayoutAlgorithmButtonParameter,
                randomLayoutParameter, removeBendsParameter, removeOverlapsParameter, mergerPSP, placerPSP};

        this.randomTopParameterIndex = ArrayUtils.indexOf(this.parameters, randomLayoutParameter);
        this.removeBendsParameterIndex = ArrayUtils.indexOf(this.parameters, removeBendsParameter);
        this.removeOverlapsParameterIndex = ArrayUtils.indexOf(this.parameters, removeOverlapsParameter);
        this.mergerPSPIndex = ArrayUtils.indexOf(this.parameters, mergerPSP);
        this.placerPSPIndex = ArrayUtils.indexOf(this.parameters, placerPSP);
    }
}


/**
 * @author Gordian
 * @see BackgroundTaskStatusProvider
 */
class MLFBackgroundTaskStatus implements BackgroundTaskStatusProvider {
    /**
     * This is set to {@code true} if the user clicks the stop button.
     */
    boolean isStopped = false;
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
    @Override public String getCurrentStatusMessage2() { return null; }

    /**
     * @see org.BackgroundTaskStatusProvider#pleaseStop()
     */
    @Override public void pleaseStop() { this.isStopped = true; }

    /**
     * @see org.BackgroundTaskStatusProvider#pluginWaitsForUser()
     */
    @Override public boolean pluginWaitsForUser() { return false; }

    /**
     * @see org.BackgroundTaskStatusProvider#pleaseContinueRun()
     */
    @Override public void pleaseContinueRun() { }
}
