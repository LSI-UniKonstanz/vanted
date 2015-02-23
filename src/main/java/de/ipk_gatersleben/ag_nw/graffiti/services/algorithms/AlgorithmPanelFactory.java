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
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.JMButton;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.dialog.ParameterEditPanel;
import org.graffiti.graph.Graph;
import org.graffiti.managers.EditComponentManager;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.selection.Selection;

import scenario.ScenarioService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.plugin_settings.PreferencesDialog;

/**
 * @author matthiak
 *
 */
public class AlgorithmPanelFactory extends JPanel
implements ListSelectionListener{



	//	JTree myTree;

	DefaultMutableTreeNode rootNode;
	DefaultMutableTreeNode rootNodeByPlugin;
	DefaultMutableTreeNode rootNodeAlgorithms;
	DefaultMutableTreeNode rootNodeThreadSafeAlgorithms;
	DefaultMutableTreeNode rootNodeSettings;
	DefaultMutableTreeNode rootNodeScripts;

	//	HashMap<String, MyPluginTreeNode> knownNodes;

	JPanel settingsPanel;

	JList<Algorithm> jListAlgorithms;

	/**
	 * 
	 */
	public AlgorithmPanelFactory(boolean vertical, List<Algorithm> algorithms) {
		initializeGUIforGivenContainer(
				vertical, 
				algorithms.toArray(new Algorithm[algorithms.size()]));
	}

	public static JPanel createForAlgorithms(boolean vertical, 
			List<Algorithm> algorithms) {
		AlgorithmPanelFactory fact = new AlgorithmPanelFactory(vertical,algorithms);
		return fact;
	}
	/**
	 * @param cp
	 * @param selection
	 * @param graph
	 * @param setAlgorithmDataObject
	 */
	public void initializeGUIforGivenContainer(
			boolean vertical, 
			Algorithm[] algorithms
			) {

		Container cp = this;
		cp.setLayout(new BoxLayout(cp, BoxLayout.Y_AXIS));


		settingsPanel = new JPanel();
		
		settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));

		jListAlgorithms = new JList<Algorithm>(algorithms);
		jListAlgorithms.setSelectionMode(ListSelectionModel.SINGLE_SELECTION );
		jListAlgorithms.setCellRenderer(new AlgorithmListCellRenderer());
		jListAlgorithms.addListSelectionListener(this);

		JSplitPane mainComp;
		// myTree.setOpaque(false);
		JScrollPane sp = new JScrollPane(jListAlgorithms);
		sp.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
		if (!vertical)
			mainComp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sp, settingsPanel);
		else
			mainComp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, sp, settingsPanel);
		mainComp.setDividerLocation(0.3); // 175
		mainComp.setDividerSize(7);
//		mainComp.setOneTouchExpandable(true);

		mainComp.setBorder(null);

		cp.add(mainComp);

//		cp.validate();
		
	}


	void runAlgorithm(final Algorithm alg, Graph graph, Selection selection) {
		ScenarioService.postWorkflowStep(alg, alg.getParameters());
		alg.attach(graph, selection);
		alg.execute();
		alg.reset();
	}


	private void initAlgorithmPreferencesPanel(final Algorithm alg,
			final Graph graph, Selection selection) {
		// settingsPanel.add(new JLabel("Algorithm selection: "+alg.getName()));

		settingsPanel.removeAll();
		
		JPanel progressAndStatus = new JPanel();
		double border = 5;
		double[][] size =
			{
				{ border, TableLayoutConstants.FILL, border }, // Columns
				{ border, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED, border }
			}; // Rows

		progressAndStatus.setLayout(new TableLayout(size));

		String desc = alg.getDescription();
		JLabel info = new JLabel(desc);
		info.setBorder(BorderFactory.createLoweredBevelBorder());
		info.setOpaque(false);
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
					selection = MainFrame.getInstance().getActiveEditorSession().getSelectionModel().getActiveSelection();
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
				paramPanel = new ParameterEditPanel(alg.getParameters(),
						editComponentManager.getEditComponents(), selection, alg.getName(), true, alg.getName());
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
					selection = MainFrame.getInstance().getActiveEditorSession().getSelectionModel().getActiveSelection();
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
	public void valueChanged(ListSelectionEvent e) {
		if( ! e.getValueIsAdjusting()) {
			Algorithm alg = jListAlgorithms.getSelectedValue();
			initAlgorithmPreferencesPanel(alg, null, null);
		}
	}


}
