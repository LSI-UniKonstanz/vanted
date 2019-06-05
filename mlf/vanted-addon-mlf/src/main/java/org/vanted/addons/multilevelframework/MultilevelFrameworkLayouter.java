/*************************************************************************************
 * The Exemplary Add-on is (c) 2008-2011 Plant Bioinformatics Group, IPK Gatersleben,
 * http://bioinformatics.ipk-gatersleben.de
 * The source code for this project, which is developed by our group, is
 * available under the GPL license v2.0 available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html. By using this
 * Add-on and VANTED you need to accept the terms and conditions of this
 * license, the below stated disclaimer of warranties and the licenses of
 * the used libraries. For further details see license.txt in the root
 * folder of this project.
 ************************************************************************************/
package org.vanted.addons.multilevelframework;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.connected_components.ConnectedComponentLayout;
import org.AttributeHelper;
import org.Vector2d;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.*;
import org.graffiti.plugin.parameter.JComponentParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.view.View;
import org.graffiti.selection.Selection;
import org.graffiti.session.Session;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.*;

public class MultilevelFrameworkLayouter extends AbstractEditorAlgorithm {

    private Map<String, LayoutAlgorithmWrapper> layoutAlgorithms;
    private final static String DEFAULT_ALGORITHM = "Force Directed (\"parameter\" GUI)";
    private JComboBox<String> algorithmListComboBox;
    private JButton setUpLayoutAlgorithmButton;
    private String lastSelectedAlgorithm = DEFAULT_ALGORITHM;

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
        final View oldView = MainFrame.getInstance().getActiveSession().getActiveView();
        // split the subgraph induced by the selection into connected components
        final Collection<? extends CoarsenedGraph> connectedComponents =
                MlfHelper.calculateConnectedComponentsOfSelection(new HashSet<>(this.getSelectedOrAllNodes()));

        final Merger merger = new RandomMerger();
        final Placer placer = new RandomPlacer();
        final LayoutAlgorithmWrapper algorithm =
                this.layoutAlgorithms.get(Objects.toString(this.algorithmListComboBox.getSelectedItem()));
        final Selection emptySelection = new Selection();

        // layout each connected component
        for (CoarsenedGraph cg : connectedComponents) {
            MultilevelGraph componentMLG = new MultilevelGraph(cg);
            merger.buildCoarseningLevels(componentMLG);
            while (componentMLG.getNumberOfLevels() > 1) {
                System.out.println("Layouting level " + componentMLG.getNumberOfLevels());
                GraphHelper.diplayGraph(componentMLG.getTopLevel());
                algorithm.execute(componentMLG.getTopLevel(), emptySelection);
                placer.reduceCoarseningLevel(componentMLG);
            }
            assert componentMLG.getNumberOfLevels() == 1 : "Not all coarsening levels were removed";
            System.out.println("Layouting level 0");
            algorithm.execute(componentMLG.getTopLevel(), emptySelection);
        }

        // apply position updates

        HashMap<Node, Vector2d> nodes2newPositions = new HashMap<>();

        for (CoarsenedGraph cg : connectedComponents) {
            for (MergedNode mn : cg.getMergedNodes()) {
                final Node representedNode = mn.getInnerNodes().iterator().next();
                assert mn.getInnerNodes().size() == 1 : "More than one node represented in level 0";
                nodes2newPositions.put(representedNode, AttributeHelper.getPositionVec2d(mn));
            }
        }


        MainFrame.getInstance().setActiveSession(oldSession, oldView);
        GraphHelper.applyUndoableNodePositionUpdate(nodes2newPositions, getName());
        ConnectedComponentLayout.layoutConnectedComponents(this.graph);
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

            // TODO add GUI for Mergers and Placers

            this.parameters = new Parameter[]{algorithmList, setUpLayoutAlgorithmButtonParameter};
        }
    }
}
