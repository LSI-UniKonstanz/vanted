/**
 * 
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.statistics;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.FolderPanel;
import org.JLabelJavaHelpLink;
import org.JMButton;
import org.apache.log4j.Logger;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.plugin.inspector.InspectorTab;
import org.graffiti.plugin.view.View;
import org.graffiti.plugins.views.defaults.GraffitiView;
import org.graffiti.selection.Selection;
import org.graffiti.session.EditorSession;

/**
 * @author matthiak
 *
 */
public class TabStatisticsScatterMatrix extends TabStatisticsSharedFunctionality
implements ActionListener{

	private static final long serialVersionUID = -5405410247241993449L;

	static Logger logger = Logger.getLogger(TabStatisticsScatterMatrix.class);

	/**
	 * 
	 */
	public TabStatisticsScatterMatrix() {
		
		initGUI();
		
	}
	
	
	
	@Override
	public String getName() {
		return getTitle();
	}

	@Override
	public String getTitle() {
		return "Scatter Plot";
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
		
		JScrollPane jScrollPane = new JScrollPane(getPlotPanel());
//		jScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		add(jScrollPane, BorderLayout.CENTER);
	}


	private JComponent getPlotPanel() {
		ScrollablePanel result = new ScrollablePanel();
		result.setOpaque(false);
		double border = 5;
		double[][] size = {
				{ border, TableLayoutConstants.FILL, border }, // Columns
				{ border, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayoutConstants.FILL,
					border } }; // Rows
		result.setLayout(new TableLayout(size));

		doScatterPlotButton = new JMButton("(Re)Create Scatter-Plot Matrix");
		doScatterPlotButton.setOpaque(false);
		doScatterPlotButton.addActionListener(this);
		result.add(doScatterPlotButton, "1,1");
		lastScatterPlot = new JLabel("");
		result.add(lastScatterPlot, "1,4");

		FolderPanel fp = new FolderPanel("Calculation Settings", false, true, false, JLabelJavaHelpLink
				.getHelpActionListener("stat_scatter"));
		fp.setBackground(null);
		fp.setFrameColor(new JTabbedPane().getBackground(), Color.BLACK, 0, 0);
		fp.setEmptyBorderWidth(0);

		FolderPanel fp2 = new FolderPanel("Visualization Settings", false, true, false, null);
		fp2.setBackground(null);
		fp2.setFrameColor(new JTabbedPane().getBackground(), Color.BLACK, 0, 0);
		fp2.setEmptyBorderWidth(0);

		checkBoxPlotAverage3 = new JCheckBox("<html>Plot average values<br>"
				+ "<small>(recommended for time series data with few replicates per time point)", plotAverage);
		checkBoxPlotAverage3.setOpaque(false);
		checkBoxPlotAverage3.setSelected(plotAverage);
		checkBoxPlotAverage3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JCheckBox src = (JCheckBox) arg0.getSource();
				plotAverage = src.isSelected();
			}
		});

		JComponent mergeEditor = getMergeOptionEditor(3);

		fp.addComp(checkBoxPlotAverage3);
		fp.addComp(mergeEditor);

		jTextFieldProb3scatter = new JTextField(new Double(prob).toString());
		jTextFieldMinR3 = new JTextField(new Double(minimumR).toString());
		JComponent corrType = getCorrelationTypeEditor(3);
		Component panelProb = getProbabilitySettingPanel(jTextFieldProb3scatter, jTextFieldMinR3, corrType);

		fp.addComp((JComponent) panelProb);

		fp2.addComp(getNewColorPanel());

		JLabel descGammaLabel = new JLabel("");
		gammaSlider2scatter = getNewGammaSlider(descGammaLabel);

		fp2.addComp(TableLayout.getSplit(descGammaLabel, gammaSlider2scatter, TableLayout.PREFERRED, TableLayout.FILL));

		final SpinnerModel sm = new SpinnerNumberModel(outlineBorderWidth, 0, 100, 0.5);
		sm.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				outlineBorderWidth = ((Double) sm.getValue()).floatValue();
			}
		});
		JSpinner dataSizeSpinner = new JSpinner(sm);

		JLabel dpsdesc = new JLabel("Datapoint Size");
		dpsdesc.setOpaque(false);

		fp2.addComp(TableLayout.getSplit(dpsdesc, dataSizeSpinner, TableLayout.PREFERRED, TableLayout.FILL));

		final JCheckBox checkLegend = new JCheckBox("Show Legend", showLegend);
		checkLegend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showLegend = checkLegend.isSelected();
			}
		});
		checkLegend.setOpaque(false);

		fp2.addComp(checkLegend);

		final JCheckBox checkShowRangeAxis = new JCheckBox("Show X-Axis", showRangeAxis);
		checkShowRangeAxis.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showRangeAxis = checkShowRangeAxis.isSelected();
			}
		});
		checkShowRangeAxis.setOpaque(false);

		fp2.addComp(checkShowRangeAxis);

		final JCheckBox checkShowTicks = new JCheckBox("Show Y-Axis", tickMarksVisible);
		checkShowTicks.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tickMarksVisible = checkShowTicks.isSelected();
			}
		});
		checkShowTicks.setOpaque(false);

		fp2.addComp(checkShowTicks);

		fp.layoutRows();
		fp2.layoutRows();

		result.add(fp.getBorderedComponent(5, 0, 0, 0), "1,2");
		result.add(fp2.getBorderedComponent(5, 0, 0, 0), "1,3");

		result.validate();
		placeForScatter = result;
		return result;
	}


	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == doScatterPlotButton) {
			checkProbabilityInput(jTextFieldProb3scatter);
			checkRinput(jTextFieldMinR3);
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
		if (e.getSource() == doScatterPlotButton) {
			Collection<GraphElement> graphElements = null;
			if (selection != null)
				graphElements = selection.getElements();
			if (graphElements == null || graphElements.size() < 2) {
				MainFrame.showMessageDialog(
						"Please select at least two nodes or edges which have experimental data assigned.",
						"More than one node needs to be selected");
			} else
				StatisticsHelper.createScatterPlotBlock(plotAverage, tickMarksVisible, showRangeAxis, showLegend, minimumR,
						outlineBorderWidth, mergeDataset, prob, rankOrder, currGammaValue, colR_1, colR0, colR1,
						graphElements, graph, false, lastScatterPlot, placeForScatter);
		}
	}
}
