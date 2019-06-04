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
import org.graffiti.attributes.Attribute;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.*;
import org.graffiti.plugin.parameter.JComponentParameter;
import org.graffiti.plugin.parameter.ObjectListParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.view.View;
import org.graffiti.selection.Selection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class MultilevelFrameworkLayouter extends AbstractEditorAlgorithm {

    private Map<String, LayoutAlgorithmWrapper> layoutAlgorithms;
    private final static String DEFAULT_ALGORITHM = "Force Directed (\"parameter\" GUI)";
    private JComboBox<String> algorithmListComboBox;
    private JButton setUpLayoutAlgorithmButton;

    public MultilevelFrameworkLayouter() {
        super();
        this.setUpParameters();
    }

    @Override
    public String getDescription() {
        return "<html><b>Multilevel Framework</b></html>";
    }

    /**
     * @see super#reset()
     */
    @Override
    public void reset() {
        super.reset();
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
                AttributeHelper.setPosition(representedNode, AttributeHelper.getPosition(mn));
            }
        }

        GraphHelper.applyUndoableNodePositionUpdate(nodes2newPositions, getName());
        ConnectedComponentLayout.layoutConnectedComponents(this.graph);
    }

    /**
     * @return the parameter array
     */
    @Override
    public Parameter[] getParameters() {
        return this.parameters;
    }

    /**
     * Sets the radius parameter to the given value.
     *
     * @param params An array with exact one DoubleParameter.
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
        final String selected = (String) this.algorithmListComboBox.getSelectedItem();
        if (selected == null) {
            return;
        }
        final LayoutAlgorithmWrapper current = this.layoutAlgorithms.get(selected);
        JOptionPane.showMessageDialog(MainFrame.getInstance(), current.getGUI(), selected, JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Create the parameter objects.
     * @author Gordian
     */
    private void setUpParameters() {
        this.layoutAlgorithms = LayoutAlgorithmWrapper.getLayoutAlgorithms();
        String defaultAlgorithm = this.layoutAlgorithms.keySet().contains(DEFAULT_ALGORITHM) ? DEFAULT_ALGORITHM :
                this.layoutAlgorithms.keySet().iterator().next();
        this.algorithmListComboBox = new JComboBox<>(this.layoutAlgorithms.keySet().stream()
                .sorted().toArray(String[]::new));
        JComponentParameter algorithmList = new JComponentParameter(this.algorithmListComboBox, "Layout Algorithm",
                "Layout Algorithm to be run on each level of the coarsened graph.");
        this.algorithmListComboBox.setSelectedItem(defaultAlgorithm);

        this.setUpLayoutAlgorithmButton = new JButton("Set up layouter");
        this.setUpLayoutAlgorithmButton.addActionListener(this::clickSetUpLayoutAlgorithmButton);
        JComponentParameter setUpLayoutAlgorithmButtonParameter =
                new JComponentParameter(this.setUpLayoutAlgorithmButton, "Set up layouter",
                        "Click the button to change the parameters of the layout algorithm.");

        // TODO add GUI for Mergers and Placers

        this.parameters = new Parameter[] {algorithmList, setUpLayoutAlgorithmButtonParameter};
    }
}
