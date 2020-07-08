/**
 * 
 */
package de.ipk_gatersleben.ag_nw.graffiti.services.algorithms;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.JMButton;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.dialog.ParameterEditPanel;
import org.graffiti.graph.Graph;
import org.graffiti.managers.EditComponentManager;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.selection.Selection;
import org.vanted.scaling.Toolbox;
import org.vanted.scaling.scalers.component.HTMLScaleSupport;
import org.vanted.scaling.scalers.component.JLabelScaler;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.plugin_settings.MyPluginTreeNode;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.plugin_settings.PreferencesDialog;

/**
 * @author matthiak
 * 
 */
public class AlgorithmPanelFactory extends JPanel implements TreeSelectionListener {

	private static final long serialVersionUID = 6550915952424474575L;

	JTree myTree;

	DefaultMutableTreeNode rootNode;
	DefaultMutableTreeNode rootNodeByPlugin;
	DefaultMutableTreeNode rootNodeAlgorithms;
	DefaultMutableTreeNode rootNodeThreadSafeAlgorithms;
	DefaultMutableTreeNode rootNodeSettings;
	DefaultMutableTreeNode rootNodeScripts;

	HashMap<String, MyPluginTreeNode> knownNodes;

	public ThreadSafeOptions optionsForPlugin = null;

	// HashMap<String, MyPluginTreeNode> knownNodes;

	JPanel settingsPanel;

	// JList<Algorithm> jListAlgorithms;

	/**
	 * 
	 */
	public AlgorithmPanelFactory(boolean vertical, List<Algorithm> algorithms) {
		initializeGUIforGivenContainer(vertical, algorithms.toArray(new Algorithm[algorithms.size()]));
	}

	public static JPanel createForAlgorithms(boolean vertical, List<Algorithm> algorithms) {
		AlgorithmPanelFactory fact = new AlgorithmPanelFactory(vertical, algorithms);
		return fact;
	}

	/**
	 * @param cp
	 * @param selection
	 * @param graph
	 * @param setAlgorithmDataObject
	 */
	public void initializeGUIforGivenContainer(boolean vertical, Algorithm[] algorithms) {

		Container cp = this;
		cp.setLayout(new BoxLayout(cp, BoxLayout.Y_AXIS));

		settingsPanel = new JPanel();

		settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
		settingsPanel.setPreferredSize(new Dimension(200, 200));

		// jListAlgorithms = new JList<Algorithm>(algorithms);
		// jListAlgorithms.setSelectionMode(ListSelectionModel.SINGLE_SELECTION );
		// jListAlgorithms.setCellRenderer(new AlgorithmListCellRenderer());
		// jListAlgorithms.addListSelectionListener(this);

		rootNode = new DefaultMutableTreeNode("Algorithms");

		for (Algorithm algo : algorithms)
			rootNode.add(new MyPluginTreeNode(algo.getName(), algo, Algorithm.class));

		myTree = new JTree(rootNode);
		// DefaultTreeCellRenderer tcr = (DefaultTreeCellRenderer)
		// myTree.getCellRenderer();
		// tcr.setOpaque(true);
		// tcr.setBackgroundNonSelectionColor(Color.YELLOW);
		myTree.addTreeSelectionListener(this);

		JSplitPane mainComp;
		// myTree.setOpaque(false);
		JScrollPane sp = new JScrollPane(myTree);
		sp.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
		sp.setMinimumSize(new Dimension(200, 100));
		if (!vertical)
			mainComp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sp, settingsPanel);
		else
			mainComp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, sp, settingsPanel);
		mainComp.setDividerLocation(0.5); // 175
		mainComp.setDividerSize(7);
		// mainComp.setOneTouchExpandable(true);

		mainComp.setBorder(null);

		cp.add(mainComp);

		// cp.validate();

	}

	/**
	 * 
	 * @param alg
	 * @param graph
	 * @param selection
	 * 
	 * @vanted.revision 2.7.0 Undoable Algorithm support
	 */
	void runAlgorithm(final Algorithm alg, Graph graph, Selection selection) {
		// ScenarioService.postWorkflowStep(alg, alg.getParameters());
		alg.reset();
		alg.attach(graph, selection);
		alg.execute();
		GravistoService.processUndoableAlgorithm(alg);
	}

	private void initAlgorithmPreferencesPanel(final Algorithm alg, final Graph graph, Selection selection) {
		// settingsPanel.add(new JLabel("Algorithm selection: "+alg.getName()));

		settingsPanel.removeAll();

		JPanel progressAndStatus = new JPanel();
		double border = 5;
		double[][] size = { { border, TableLayoutConstants.FILL, border }, // Columns
				{ border, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED,
						TableLayoutConstants.PREFERRED, border } }; // Rows

		progressAndStatus.setLayout(new TableLayout(size));

		String desc = HTMLScaleSupport.scaleText(alg.getDescription());
		JLabel info = new JLabel(desc);
		info.setBorder(BorderFactory.createLoweredBevelBorder());
		info.setOpaque(false);

		// scaling
		float factor = Toolbox.getDPIScalingRatio();
		if (factor != 1f)
			new JLabelScaler(factor).coscaleHTML(info);

		if (desc != null && desc.length() > 0)
			progressAndStatus.add(info, "1,3");
		EditComponentManager editComponentManager = MainFrame.getInstance().getEditComponentManager();

		ParameterEditPanel paramPanel = null;
		alg.attach(graph, selection);
		boolean canNotStart = false;
		try {
			Graph workgraph = graph;
			try {
				if (workgraph == null) {
					workgraph = MainFrame.getInstance().getActiveEditorSession().getGraph();
					selection = MainFrame.getInstance().getActiveEditorSession().getSelectionModel()
							.getActiveSelection();
					if (selection == null)
						selection = new Selection("");
				}
			} catch (NullPointerException npe) {
				// empty
			}
			alg.attach(workgraph, selection);
			alg.check();
		} catch (PreconditionException e1) {
			canNotStart = true;
			JLabel hint = new JLabel("<html>Algorithm can not be used at the moment:<br>" + e1.getLocalizedMessage());
			progressAndStatus.add(hint, "1,2");
			paramPanel = null;
		}
		if (!canNotStart)
			if (alg.getParameters() != null) {
				paramPanel = new ParameterEditPanel(alg.getParameters(), editComponentManager.getEditComponents(),
						selection, alg.getName(), true, alg.getName());
				if (paramPanel != null) {
					JScrollPane sp = new JScrollPane(paramPanel);
					sp.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
					sp.setOpaque(false);
					sp.setBackground(null);
					progressAndStatus.add(sp, "1,2");
				}
			}
		final ParameterEditPanel finalParamPanel = paramPanel;
		JButton runButton = new JMButton("Execute");
		PreferencesDialog.activeStartLayoutButton = runButton;

		if (canNotStart)
			runButton.setEnabled(false);

		final Selection selectionF = selection;
		runButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Graph workgraph = graph;
				if (workgraph == null) {
					try {
						workgraph = GravistoService.getInstance().getMainFrame().getActiveSession().getGraph();
					} catch (Exception err) {
						MainFrame.showMessageDialog("No active graph!", "Error");
						return;
					}
				}
				if (finalParamPanel != null)
					alg.setParameters(finalParamPanel.getUpdatedParameters());
				Selection selection = selectionF;
				try {
					workgraph = MainFrame.getInstance().getActiveEditorSession().getGraph();
					selection = MainFrame.getInstance().getActiveEditorSession().getSelectionModel()
							.getActiveSelection();
					if (selection == null)
						selection = new Selection("");
				} catch (NullPointerException npe) {
					// empty
				}
				runAlgorithm(alg, workgraph, selection);
			}
		});
		runButton.setMinimumSize(new Dimension(10, 10));
		progressAndStatus.add(runButton, "1,1");

		progressAndStatus.validate();
		settingsPanel.add(progressAndStatus);
		settingsPanel.validate();
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		processTreeSelectionEvent(e);
	}

	private void processTreeSelectionEvent(TreeSelectionEvent e) {

		if (optionsForPlugin != null) {
			optionsForPlugin.setAbortWanted(true);
		}
		Object lastPathComponent = e.getPath().getLastPathComponent();
		if (lastPathComponent instanceof MyPluginTreeNode)
			initAlgorithmPreferencesPanel((Algorithm) ((MyPluginTreeNode) lastPathComponent).getUserObject(), null,
					null);
	}
}
