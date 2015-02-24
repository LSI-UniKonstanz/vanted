/**
 * 
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.statistics;

import info.clearthought.layout.SingleFiledLayout;
import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.AttributeHelper;
import org.FolderPanel;
import org.JLabelJavaHelpLink;
import org.JMButton;
import org.apache.log4j.Logger;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.inspector.InspectorTab;
import org.graffiti.plugin.view.View;
import org.graffiti.plugins.views.defaults.GraffitiView;
import org.graffiti.selection.Selection;
import org.graffiti.session.EditorSession;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;

/**
 * @author matthiak
 *
 */
public class TabStatisticsCorrNToN extends TabStatisticsSharedFunctionality
implements ActionListener
{
	
	private static final long serialVersionUID = -2459123386778503054L;

	static Logger logger = Logger.getLogger(TabStatisticsCorrNToN.class);

	JButton findCorrButton;

	JButton removeCorrelationEdges;

	JButton selectCorrelationEdges;

	private JTextField jTextFieldProb1findCorr;
	
	private JTextField jTextFieldMinR1;
	
	JCheckBox onlyUpdateExistingEdges = new JCheckBox("<html>Update existing edges, disable edge-creation");

	private JSlider gammaSlider3edgeCorr;
	
	
	/**
	 * 
	 */
	public TabStatisticsCorrNToN() {
		
		initGUI();
		
	}
	
	
	
	@Override
	public String getName() {
		return getTitle();
	}

	@Override
	public String getTitle() {
		return "Correlate n:n";
	}
	

	@Override
	public boolean visibleForView(View v) {
		return v != null && v instanceof GraffitiView;
	}



	@Override
	public String getTabParentPath() {
		return "Analysis.Data";
	}



	@Override
	public int getPreferredTabPosition() {
		return InspectorTab.TAB_TRAILING;
	}


	int i = 0;

	private void initGUI() {
		setLayout(new BorderLayout());
		
		JScrollPane jScrollPane = new JScrollPane(getAnalysisPanel());
		add(jScrollPane, BorderLayout.CENTER);
	}

	
	
	
	private JComponent getAnalysisPanel() {
		ScrollablePanel result = new ScrollablePanel();
		result.setOpaque(false);
		result.setLayout(new SingleFiledLayout(SingleFiledLayout.COLUMN, SingleFiledLayout.FULL, 0));
		int border = 5;
		result.setBorder(BorderFactory.createEmptyBorder(border, border, border, border));
		findCorrButton = new JMButton("<html>Find Significant Correlations");
		findCorrButton.setOpaque(false);
		findCorrButton.addActionListener(this);
		
		removeCorrelationEdges = new JMButton("<html><small>Remove Edges");
		removeCorrelationEdges.addActionListener(this);
		removeCorrelationEdges.setOpaque(false);
		
		selectCorrelationEdges = new JMButton("<html><small>Select Edges");
		selectCorrelationEdges.addActionListener(this);
		selectCorrelationEdges.setOpaque(false);
		
		result.add(TableLayout.getSplit(findCorrButton, TableLayout.getSplitVertical(removeCorrelationEdges,
							selectCorrelationEdges, TableLayout.PREFERRED, TableLayout.PREFERRED), TableLayout.FILL,
							TableLayout.PREFERRED), "1,1");
		
		FolderPanel fp = new FolderPanel("Calculation Settings", false, true, false, JLabelJavaHelpLink
							.getHelpActionListener("stat_corr"));
		fp.setBackground(null);
		fp.setFrameColor(new JTabbedPane().getBackground(), Color.BLACK, 0, 0);
		fp.setEmptyBorderWidth(0);
		
		checkBoxPlotAverage1 = new JCheckBox(
							"<html>Use average values<br><small>(recommended for time series data with few replicates per time point)",
							plotAverage);
		checkBoxPlotAverage1.setOpaque(false);
		checkBoxPlotAverage1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JCheckBox src = (JCheckBox) arg0.getSource();
				plotAverage = src.isSelected();
			}
		});
		checkBoxPlotAverage1.setSelected(plotAverage);
		JComponent mergeOptionEditor = getMergeOptionEditor(1);
		
		final JCheckBox checkBoxFindTimeShifts = new JCheckBox("Find time-shifted (index -3..3) correlations",
							considerTimeShifts);
		checkBoxFindTimeShifts.setOpaque(false);
		checkBoxFindTimeShifts.setSelected(considerTimeShifts);
		checkBoxFindTimeShifts.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JCheckBox src = (JCheckBox) arg0.getSource();
				considerTimeShifts = src.isSelected();
			}
		});
		
		jTextFieldProb1findCorr = new JTextField(new Double(prob).toString());
		jTextFieldMinR1 = new JTextField(new Double(minimumR).toString());
		
		JComponent corrType = getCorrelationTypeEditor(1);
		
		JComponent panelProb = getProbabilitySettingPanel(jTextFieldProb1findCorr, jTextFieldMinR1, corrType);
		
		final JComponent colPanel = getNewColorPanel();
		JCheckBox colorCodeEdgesCorrelation = new JCheckBox("Change edge color dependent on correlation:");
		
		colorCodeEdgesCorrelation.setOpaque(false);
		
		colorCodeEdgesCorrelation.setSelected(colorCodeEdgesWithCorrelationValue);
		checkColPanel(colPanel, colorCodeEdgesWithCorrelationValue);
		colorCodeEdgesCorrelation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				colorCodeEdgesWithCorrelationValue = ((JCheckBox) e.getSource()).isSelected();
				checkColPanel(colPanel, colorCodeEdgesWithCorrelationValue);
			}
		});
		JLabel descGammaLabel = new JLabel();
		gammaSlider3edgeCorr = getNewGammaSlider(descGammaLabel);
		
		fp.addComp(checkBoxPlotAverage1);
		fp.addComp(checkBoxFindTimeShifts);
		fp.addComp(mergeOptionEditor);
		fp.addComp(panelProb);
		
		FolderPanel fp2 = new FolderPanel("Visualization Settings", false, true, false, null);
		fp2.setBackground(null);
		fp2.setFrameColor(new JTabbedPane().getBackground(), Color.BLACK, 0, 0);
		fp2.setEmptyBorderWidth(0);
		
		addStatusText2.setOpaque(false);
		addStatusText2.setSelected(showStatusResult);
		addStatusText2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showStatusResult = addStatusText2.isSelected();
				addStatusText1.setSelected(showStatusResult);
				addStatusText3.setSelected(showStatusResult);
			}
		});
		
		JButton clearStatus = new JMButton("<html><small>Clear Edge-<br>Status Text");
		clearStatus.setOpaque(false);
		clearStatus.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					List<Node> nodes = GraphHelper.getSelectedOrAllNodes(MainFrame.getInstance().getActiveEditorSession());
					for (Node n : nodes) {
						for (Edge edge : n.getEdges())
							AttributeHelper.setToolTipText(edge, "");
					}
					if (correlationEdges != null && correlationEdges.size() > 0)
						for (Edge edge : correlationEdges)
							AttributeHelper.setToolTipText(edge, "");
				} catch (NullPointerException npe) {
					MainFrame.showMessageDialog("No active graph editor window found!", "Error");
				}
			}
		});
		fp2.addComp(TableLayout.get3Split(addStatusText2, new JLabel(""), clearStatus, TableLayout.FILL, 5,
							TableLayout.PREFERRED));
		
		onlyUpdateExistingEdges.setOpaque(false);
		onlyUpdateExistingEdges.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (onlyUpdateExistingEdges.isSelected()) {
					checkBoxFindTimeShifts.setSelected(false);
					checkBoxFindTimeShifts.setEnabled(false);
					dontAddNewEdgesUpdateOld = true;
				} else {
					checkBoxFindTimeShifts.setEnabled(true);
					checkBoxFindTimeShifts.setSelected(considerTimeShifts);
					dontAddNewEdgesUpdateOld = false;
				}
			}
		});
		
		fp2.addComp(onlyUpdateExistingEdges);
		
		fp2.addComp(colorCodeEdgesCorrelation);
		fp2.addComp(colPanel);
		fp2.addComp(TableLayout.getSplit(descGammaLabel, gammaSlider3edgeCorr, TableLayout.PREFERRED, TableLayout.FILL));
		
		fp.layoutRows();
		fp2.layoutRows();
		
		result.add(fp.getBorderedComponent(5, 0, 0, 0));
		result.add(fp2.getBorderedComponent(5, 0, 0, 0));
		
		result.validate();
		return result;
	}

	private void checkColPanel(Component colPanel, boolean enabled) {
		JPanel jp = (JPanel) colPanel;
		for (int i = 0; i < jp.getComponentCount(); i++) {
			Object o = jp.getComponent(i);
			if (o instanceof JButton) {
				JButton jb = (JButton) o;
				jb.setEnabled(enabled);
			}
		}
	}
	
	
	private void removeCorrelationEdges(Graph graph) {
		ArrayList<Edge> toBeDeleted = new ArrayList<Edge>();
		for (Edge e : graph.getEdges()) {
			if (correlationEdges.contains(e)) {
				toBeDeleted.add(e);
				correlationEdges.remove(e);
			}
		}
		graph.getListenerManager().transactionStarted(this);
		for (Edge e : toBeDeleted) {
			graph.deleteEdge(e);
		}
		graph.getListenerManager().transactionFinished(this);
	}
	
	private void selectCorrelationEdges(Graph graph, EditorSession session) {
		Selection s = session.getSelectionModel().getActiveSelection();
		if (s == null)
			s = new Selection("new edges");
		for (Edge e : graph.getEdges()) {
			if (correlationEdges.contains(e)) {
				s.add(e);
			}
		}
		graph.getListenerManager().transactionStarted(this);
		session.getSelectionModel().selectionChanged();
		graph.getListenerManager().transactionFinished(this);
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == findCorrButton) {
			checkProbabilityInput(jTextFieldProb1findCorr);
			checkRinput(jTextFieldMinR1);
		}
	
		try {
			double temp = Double.parseDouble(jTextFieldAlpha.getText());
			alpha = temp;
		} catch (NumberFormatException nfe) {
			// probability remains unchanged
		}
		if (e.getSource() == doTest && alphaSpecified)
			jTextFieldAlpha.setText(new Double(alpha).toString());
		
		EditorSession session = GravistoService.getInstance().getMainFrame().getActiveEditorSession();
		
		Selection selection = null;
		if (session != null)
			selection = session.getSelectionModel().getActiveSelection();
		
		Graph graph = null;
		if (session != null)
			graph = session.getGraph();
		
		if (e.getSource() == findCorrButton) {
			Collection<Node> nodes = null;
			if (selection != null)
				nodes = selection.getNodes();
			if (nodes == null || nodes.size() == 0) {
				if (graph != null)
					nodes = graph.getNodes();
			}
			if (nodes == null || nodes.size() < 2) {
				MainFrame.showMessageDialog("Please select at least two nodes which have experimental data assigned.",
									"More than one node needs to be selected");
			} else {
				findCorrelations(nodes, graph, session);
			}
		}	
		
		if (e.getSource() == removeCorrelationEdges) {
			if (graph == null) {
				MainFrame.showMessageDialog("No graph available", "No graph available");
			} else
				removeCorrelationEdges(graph);
		}
		if (e.getSource() == selectCorrelationEdges) {
			if (graph == null) {
				MainFrame.showMessageDialog("No graph available", "No graph available");
			} else
				selectCorrelationEdges(graph, session);
		}
	}
	
}
