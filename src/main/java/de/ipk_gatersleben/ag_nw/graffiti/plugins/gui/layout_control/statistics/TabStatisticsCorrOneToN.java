/**
 * 
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.statistics;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
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
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
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
public class TabStatisticsCorrOneToN extends TabStatisticsSharedFunctionality
implements ActionListener {

	private static final long serialVersionUID = 1757113924286048397L;

	static Logger logger = Logger.getLogger(TabStatisticsCorrOneToN.class);


	private JTextField jTextFieldProb2visCorr;

	private JTextField jTextFieldMinR2;

	JButton visCorrButton;

	JButton findCorrButton;
	
	
	JButton resetColorAndBorder;

	private JSlider gammaSlider1vis;

	
	/**
	 * 
	 */
	public TabStatisticsCorrOneToN() {
		
		initGUI();
		
	}
	
	
	
	@Override
	public String getName() {
		return getTitle();
	}

	@Override
	public String getTitle() {
		return "Correlate 1:n";
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
		
		JScrollPane jScrollPane = new JScrollPane(getAnalysisPanelOneToN());
		add(jScrollPane, BorderLayout.CENTER);
	}


	private JComponent getAnalysisPanelOneToN() {
		ScrollablePanel result = new ScrollablePanel();
		result.setOpaque(false);
		double border = 5;
		double[][] size = { { border, TableLayoutConstants.FILL, border }, // Columns
				{ border, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, border } }; // Rows
		result.setLayout(new TableLayout(size));

		visCorrButton = new JMButton("<html>Calculate and visualize correlations");
		visCorrButton.addActionListener(this);
		visCorrButton.setOpaque(false);

		resetColorAndBorder = new JMButton("<html>Reset Node/Edge-<br>Color/Border");
		resetColorAndBorder.addActionListener(this);
		resetColorAndBorder.setOpaque(false);

		result.add(TableLayout.getSplit(visCorrButton, resetColorAndBorder, TableLayout.FILL, TableLayout.PREFERRED),
				"1,1");

		FolderPanel fp = new FolderPanel("Calculation Settings", false, true, false, JLabelJavaHelpLink
				.getHelpActionListener("stat_vis"));
		fp.setBackground(null);
		fp.setFrameColor(new JTabbedPane().getBackground(), Color.BLACK, 0, 0);
		fp.setEmptyBorderWidth(0);

		checkBoxPlotAverage2 = new JCheckBox("<html>Use average values<br>"
				+ "<small>(recommended for time series data with few replicates per time point)", plotAverage);
		checkBoxPlotAverage2.setOpaque(false);
		checkBoxPlotAverage2.setSelected(plotAverage);
		checkBoxPlotAverage2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JCheckBox src = (JCheckBox) arg0.getSource();
				plotAverage = src.isSelected();
			}
		});

		JComponent mergeEditor = getMergeOptionEditor(2);

		fp.addComp(TableLayout.getSplitVertical(checkBoxPlotAverage2, mergeEditor, TableLayout.PREFERRED,
				TableLayout.PREFERRED));
		jTextFieldProb2visCorr = new JTextField(new Double(prob).toString());
		jTextFieldMinR2 = new JTextField(new Double(minimumR).toString());
		JComponent corrType = getCorrelationTypeEditor(2);
		JComponent panelProb = getProbabilitySettingPanel(jTextFieldProb2visCorr, jTextFieldMinR2, corrType);
		fp.addComp(panelProb);

		FolderPanel fp2 = new FolderPanel("Visualization Settings", false, true, false, null);
		fp2.setBackground(null);
		fp2.setFrameColor(new JTabbedPane().getBackground(), Color.BLACK, 0, 0);
		fp2.setEmptyBorderWidth(0);

		addStatusText3.setOpaque(false);
		addStatusText3.setSelected(showStatusResult);
		addStatusText3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showStatusResult = addStatusText3.isSelected();
				addStatusText1.setSelected(showStatusResult);
				addStatusText2.setSelected(showStatusResult);
			}
		});

		JButton clearStatus = new JMButton("<html><small>Clear<br>Status Text");
		clearStatus.setOpaque(false);
		clearStatus.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Collection<GraphElement> graphElements = GraphHelper.getSelectedOrAllGraphElements();
					for (GraphElement ge : graphElements)
						AttributeHelper.setToolTipText(ge, "");
				} catch (NullPointerException npe) {
					MainFrame.showMessageDialog("No active graph editor window found!", "Error");
				}
			}
		});
		fp2.addComp(TableLayout.get3Split(addStatusText3, new JLabel(""), clearStatus, TableLayout.FILL, 5,
				TableLayout.PREFERRED));

		fp2.addComp(getNewColorPanel());
		JLabel descGammaLabel = new JLabel();
		gammaSlider1vis = getNewGammaSlider(descGammaLabel);

		fp2.addComp(TableLayout.getSplit(descGammaLabel, gammaSlider1vis, TableLayout.PREFERRED, TableLayout.FILL));

		fp.layoutRows();
		fp2.layoutRows();

		result.add(fp.getBorderedComponent(5, 0, 0, 0), "1,2");
		result.add(fp2.getBorderedComponent(5, 0, 0, 0), "1,3");

		result.validate();
		return result;
	}


	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == visCorrButton) {
			checkProbabilityInput(jTextFieldProb2visCorr);
			checkRinput(jTextFieldMinR2);
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
		if (e.getSource() == visCorrButton) {
			Collection<GraphElement> graphElements = null;
			if (selection != null)
				graphElements = selection.getElements();
			if (graphElements == null || graphElements.size() != 1) {
				MainFrame.showMessageDialog("Please select a single node or edge which has experimental data assigned.",
						"One element needs to be selected");
			} else
				visualiseCorrelation(graphElements.iterator().next(), graph);
		}
	}
}
