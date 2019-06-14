package org.vanted.addons.multilevelframework;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.connected_components.ConnectedComponentLayout;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.random.RandomLayouterAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider;
import org.AttributeHelper;
import org.Vector2d;
import org.apache.commons.lang.ArrayUtils;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
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

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class MultilevelFrameworkLayouter extends AbstractEditorAlgorithm {

    private Map<String, LayoutAlgorithmWrapper> layoutAlgorithms;
    private final static String DEFAULT_ALGORITHM = BlockingForceDirected.springName;
    private JComboBox<String> algorithmListComboBox;
    private JButton setUpLayoutAlgorithmButton;
    private String lastSelectedAlgorithm = DEFAULT_ALGORITHM;
    private int randomTopParameterIndex = 0;
    private int removeBendsParameterIndex = 0;
    private boolean randomTop = false;
    private boolean removeBends = false;

    public MultilevelFrameworkLayouter() {
        super();
        this.setUpParameters();
    }

    @Override
    public String getDescription() {
        return "<html><b>Multilevel Framework</b></html>";
    }

    /**
     * Does nothing. This layouter doesn't currently support resetting; it causes problems with the GUI of
     * sub-algorithms (such as the level layouter).
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
     */
    public void execute() {
        // need to save old session and view, because they need to be restored for applyUndoableNodePositionUpdate
        // to work
        final Session oldSession = MainFrame.getInstance().getActiveSession();
        final View oldView = oldSession.getActiveView();
        final Merger merger = new RandomMerger();
        final Placer placer = new RandomPlacer();
        final LayoutAlgorithmWrapper algorithm =
                this.layoutAlgorithms.get(Objects.toString(this.algorithmListComboBox.getSelectedItem()));
        final List<?extends CoarsenedGraph>[] connectedComponents = new List[1];

        final MLFBackgroundTaskStatus bts = new MLFBackgroundTaskStatus();

        // displaying levels doesn't work in a background task

        BackgroundTaskHelper.issueSimpleTask(this.getName(), "Multilevel Framework is running", () -> {
            if (this.removeBends) {
                GraphHelper.removeAllBends(this.graph, true);
            }
            // split the subgraph induced by the selection into connected components
            connectedComponents[0] =
                    MlfHelper.calculateConnectedComponentsOfSelection(new HashSet<>(this.getSelectedOrAllNodes()));

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
                componentMLG.getTopLevel().setBoolean("GRAPH_IS_MLF_COARSENING_LEVEL", true);
                // indicate that this is the top level
                componentMLG.getTopLevel().setBoolean("GRAPH_IS_MLF_COARSENING_TOP_LEVEL", true);

                if (bts.isStopped) { bts.status = -1; return; }

                if (this.randomTop) {
                    final RandomLayouterAlgorithm rla = new RandomLayouterAlgorithm();
                    rla.attach(componentMLG.getTopLevel(), emptySelection);
                    rla.execute();
                }

                while (componentMLG.getNumberOfLevels() > 1) {
                    bts.statusMessage = this.makeStatusMessage(numLevelsAtStart, componentMLG.getNumberOfLevels(),
                            connectedComponents[0], i);
                    bts.status = this.calculateProgress(componentMLG, connectedComponents[0], i, numLevelsAtStart);
//                    this.display(componentMLG.getTopLevel());
                    algorithm.execute(componentMLG.getTopLevel(), emptySelection);
                    placer.reduceCoarseningLevel(componentMLG);
                    // indicate that this is a coarsened graph to allow for optimizations in the level layouter
                    componentMLG.getTopLevel().setBoolean("GRAPH_IS_MLF_COARSENING_LEVEL", true);
                    if (bts.isStopped) { bts.status = -1; return; }
                }

                assert componentMLG.getNumberOfLevels() == 1 : "Not all coarsening levels were removed";
                bts.status = this.calculateProgress(componentMLG, connectedComponents[0], i, numLevelsAtStart);
                bts.statusMessage = this.makeStatusMessage(numLevelsAtStart, componentMLG.getNumberOfLevels(),
                        connectedComponents[0], i);
                algorithm.execute(componentMLG.getTopLevel(), emptySelection);
                bts.statusMessage = "Finished laying out the levels";
                bts.status = -1;
            }
        }, () -> {
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
            ConnectedComponentLayout.layoutConnectedComponents(this.graph);
        }, bts);
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
     * Sets the parameters to the given array.
     * @param params The new parameters.
     * @see Algorithm#setParameters(Parameter[])
     */
    @Override
    public void setParameters(Parameter[] params) {
        this.parameters = params;
        this.randomTop = (Boolean) this.parameters[this.randomTopParameterIndex].getValue();
        this.removeBends = (Boolean) this.parameters[this.removeBendsParameterIndex].getValue();
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

    public boolean activeForView(View v) {
        return v != null;
    }

    /**
     * Calls {@link GraphHelper#diplayGraph(Graph)} in the event dispatcher thread (it throws exceptions if called from
     * other threads).
     * @param g
     *      The {@link Graph} to pass along.
     * @author Gordian
     */
    private void display(Graph g) {
        try {
            SwingUtilities.invokeAndWait(() -> GraphHelper.diplayGraph(g));
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
     * @return
     *      the status message.
     * @author Gordian
     */
    private String makeStatusMessage(int totalLevels, int level, List<?extends CoarsenedGraph> connectedComponents,
                                     int current) {
        return "Laying out level "
                + level
                + " (out of " + totalLevels + ")"
                + " of connected component "
                + (current + 1)
                + " (out of " + connectedComponents.size() + ") of graph \""
                + this.graph.getName() + "\"";
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
    private double calculateProgress(MultilevelGraph mlg, List<?extends CoarsenedGraph> connectedComponents,
                                     int currentIndex, int numberOfLevelsAtStart) {
        if (numberOfLevelsAtStart == 0 || connectedComponents.size() == 0) { return -1; }
        // calculate the progress as the percentage of nodes and connected components already processed
        return 100.0 * (1 - (double)mlg.getNumberOfLevels()/numberOfLevelsAtStart)
                * (currentIndex + 1.0)
                / connectedComponents.size();
    }

    /**
     * The handler that gets called if the "Set up Layouter" button was clicked.
     * @param e
     *      Ignored.
     * @author Gordian
     */
    private void clickSetUpLayoutAlgorithmButton(ActionEvent e) {
        final LayoutAlgorithmWrapper current = this.layoutAlgorithms.get(this.lastSelectedAlgorithm);
        final JComponent gui = current.getGUI();
        if (gui != null) {
            JOptionPane.showMessageDialog(MainFrame.getInstance(), gui,
                    this.lastSelectedAlgorithm, JOptionPane.PLAIN_MESSAGE);
        }
    }

    /**
     * The handler that gets called if the user changes to a different layout algorithm for the levels.
     * @param e
     *      Ignored.
     * @author Gordian
     */
    private void changeSelectedAlgorithm(ActionEvent e) {
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
     * @author Gordian
     */
    private void updateParameters() {
        Map<String, LayoutAlgorithmWrapper> tmp = LayoutAlgorithmWrapper.getLayoutAlgorithms();
        boolean changed = false;
        for (Map.Entry<String, LayoutAlgorithmWrapper> e : tmp.entrySet()) {
            if (!this.layoutAlgorithms.containsKey(e.getKey())) {
                this.layoutAlgorithms.put(e.getKey(), e.getValue());
                changed = true;
            }
        }
        if (changed || this.parameters == null) {
            if (!this.layoutAlgorithms.containsKey(this.lastSelectedAlgorithm)) {
                MainFrame.showMessageDialog("Algorithm \"" + this.layoutAlgorithms + "\" doesn't seem to exist.",
                        "Error");
                this.lastSelectedAlgorithm = this.layoutAlgorithms.keySet().contains(this.lastSelectedAlgorithm) ?
                        this.lastSelectedAlgorithm : this.layoutAlgorithms.keySet().iterator().next();
            }

            this.algorithmListComboBox = new JComboBox<>(this.layoutAlgorithms.keySet().stream()
                    .sorted().toArray(String[]::new));
            this.algorithmListComboBox.addActionListener(this::changeSelectedAlgorithm);

            JComponentParameter algorithmList = new JComponentParameter(this.algorithmListComboBox, "Layout Algorithm",
                    "Layout Algorithm to be run on each level of the coarsened graph.");
            this.algorithmListComboBox.setSelectedItem(this.lastSelectedAlgorithm);

            this.setUpLayoutAlgorithmButton = new JButton("Set up layouter");
            this.setUpLayoutAlgorithmButton.addActionListener(this::clickSetUpLayoutAlgorithmButton);

            JComponentParameter setUpLayoutAlgorithmButtonParameter =
                    new JComponentParameter(this.setUpLayoutAlgorithmButton, "Set up layouter",
                            "Click the button to change the parameters of the layout algorithm.");

            BooleanParameter randomLayoutParameter = new BooleanParameter(this.randomTop,
                    "Random initial layout on top level",
                    "Do an random layout on the top (i.e. coarsest) coarsening level");

            BooleanParameter removeBendsParameter = new BooleanParameter(this.removeBends,
                    "Remove bends",
                    "Remove all edge bends from the graph");

            // TODO add GUI for Mergers and Placers

            this.parameters = new Parameter[]{algorithmList, setUpLayoutAlgorithmButtonParameter,
                    randomLayoutParameter, removeBendsParameter};

            this.randomTopParameterIndex = ArrayUtils.indexOf(this.parameters, randomLayoutParameter);
            this.removeBendsParameterIndex = ArrayUtils.indexOf(this.parameters, removeBendsParameter);
        }
    }
}


/**
 * @author Gordian
 * @see BackgroundTaskStatusProvider
 */
class MLFBackgroundTaskStatus implements BackgroundTaskStatusProvider {
    boolean isStopped = false;
    double status = -1;
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
