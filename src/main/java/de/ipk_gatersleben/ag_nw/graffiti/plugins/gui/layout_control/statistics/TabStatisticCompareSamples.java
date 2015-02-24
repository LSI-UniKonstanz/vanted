/**
 * 
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.statistics;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.AttributeHelper;
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

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;

/**
 * @author matthiak
 *
 */
public class TabStatisticCompareSamples extends TabStatisticsSharedFunctionality
implements ActionListener{

	private static final long serialVersionUID = 3070514547327413326L;

	static Logger logger = Logger.getLogger(TabStatisticCompareSamples.class);
	
	/**
	 * 
	 */
	public TabStatisticCompareSamples() {
		
		initGUI();
		
	}
	
	
	
	@Override
	public String getName() {
		return getTitle();
	}

	@Override
	public String getTitle() {
		return "Compare Samples";
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
//		removeAll();
		setLayout(new BorderLayout());
//		JButton but = new JButton("relayout"+ (i++));
//		add(but, BorderLayout.NORTH);
//		but.addActionListener(new ActionListener() {
//			
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				initGUI();
//			}
//		});
//		
		
		JScrollPane jScrollPane = new JScrollPane(getStudentPanel());
//		jScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		add(jScrollPane, BorderLayout.CENTER);
	}
	
	private JComponent getStudentPanel() {
		ScrollablePanel result = new ScrollablePanel();
		
		
		double border = 10;
		double[][] size = {
							{ border, TableLayoutConstants.FILL, border }, // Columns
							{ 
								border, 
								TableLayoutConstants.PREFERRED, 
								TableLayoutConstants.PREFERRED, 
								border, 
								TableLayoutConstants.PREFERRED, 
								TableLayoutConstants.PREFERRED,
								TableLayoutConstants.PREFERRED, 
								border 
							} }; // Rows
		result.setLayout(new TableLayout(size));
		result.setOpaque(false);
		
		jTextFieldAlpha = new JTextField();
		
		if (alphaSpecified) {
			jTextFieldAlpha.setText(alpha + "");
			jTextFieldAlpha.setEnabled(true);
		} else {
			jTextFieldAlpha.setText("(using automatic setting, 0.05 / 0.01 / 0.001)");
			jTextFieldAlpha.setEnabled(false);
		}
		
		// add action button
		doTest = new JMButton("<html>Compare Conditions");
		doTest.setOpaque(false);
		doTest.addActionListener(this);
		
		result.add(doTest, "1,1");
		
		final JRadioButton ttestSel = new JRadioButton("<html>Unpaired T-Test<br>"
							+ "<small>StdDev is unknown but expected to be equal"
							+ "<br/>(homoscedastic), assuming a normal distribution "
							+ "<br/>of independent samples");
		final JRadioButton welchSel = new JRadioButton("<html>Welch-Satterthwaite T-Test<br>"
							+ "<small>StdDev is unknown (heteroscedastic), ,<br/>" + "assuming a normal distribution of independent samples");
		final JRadioButton wilcoxonSel = new JRadioButton("<html>Wilcoxon, Mann-Whitney U-Test<br>"
							+ "<small>Rank sum test for two independent samples");
		
		final JRadioButton ratioSel = new JRadioButton("<html>Ratio Difference<br>"
							+ "<small>Check if the ratio of the mean values <br/>is above or below the specified threshold");
		
		ttestSel.setSelected(sampleCalcType_2doublet_3welch_4wilcoxon_5ratio == 2);
		ttestSel.setOpaque(false);
		welchSel.setSelected(sampleCalcType_2doublet_3welch_4wilcoxon_5ratio == 3);
		welchSel.setOpaque(false);
		wilcoxonSel.setSelected(sampleCalcType_2doublet_3welch_4wilcoxon_5ratio == 4);
		wilcoxonSel.setOpaque(false);
		ratioSel.setSelected(sampleCalcType_2doublet_3welch_4wilcoxon_5ratio == 5);
		ratioSel.setOpaque(false);
		ButtonGroup typeOfCalculation = new ButtonGroup();
		typeOfCalculation.add(ttestSel);
		typeOfCalculation.add(welchSel);
		typeOfCalculation.add(wilcoxonSel);
		typeOfCalculation.add(ratioSel);
		ttestSel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (ttestSel.isSelected())
					sampleCalcType_2doublet_3welch_4wilcoxon_5ratio = 2;
			}
		});
		welchSel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (welchSel.isSelected())
					sampleCalcType_2doublet_3welch_4wilcoxon_5ratio = 3;
			}
		});
		wilcoxonSel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (wilcoxonSel.isSelected())
					sampleCalcType_2doublet_3welch_4wilcoxon_5ratio = 4;
			}
		});
		
		ratioSel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (ratioSel.isSelected())
					sampleCalcType_2doublet_3welch_4wilcoxon_5ratio = 5;
			}
		});
		
		SpinnerModel sm1 = new SpinnerNumberModel(ratioL, 0, 1, 0.05d);
		final JSpinner minRatio = new JSpinner(sm1);
		minRatio.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				ratioL = (Double) minRatio.getValue();
			}
		});
		SpinnerModel sm2 = new SpinnerNumberModel(ratioU, 1, Double.MAX_VALUE, 0.05d);
		final JSpinner maxRatio = new JSpinner(sm2);
		minRatio.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				ratioU = (Double) maxRatio.getValue();
			}
		});
		
		JComponent calcTypePanel = TableLayout.get3SplitVertical(ttestSel, TableLayout.getSplitVertical(welchSel,
							wilcoxonSel, TableLayoutConstants.PREFERRED, TableLayout.PREFERRED), ratioSel, TableLayout.PREFERRED,
							TableLayout.PREFERRED, TableLayout.PREFERRED);
		
		calcTypePanel.setOpaque(false);
		calcTypePanel.setBorder(BorderFactory.createTitledBorder("Type of test"));
		
		FolderPanel fp = new FolderPanel("Calculation Settings", false, true, false, JLabelJavaHelpLink
							.getHelpActionListener("stat_ttest"));
		fp.setBackground(null);
		fp.setFrameColor(new JTabbedPane().getBackground(), Color.BLACK, 0, 0);
		fp.setEmptyBorderWidth(0);
		
		fp.addComp(calcTypePanel);
		
		// add significance value selection
		JCheckBox specifyAlpha = new JCheckBox("<html>Specify &#945; value:", alphaSpecified);
		specifyAlpha.setBackground(null);
		specifyAlpha.setOpaque(false);
		specifyAlpha.setSelected(true);
		specifyAlpha.setEnabled(false);
		specifyAlpha.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				alphaSpecified = ((JCheckBox) e.getSource()).isSelected();
				if (alphaSpecified) {
					jTextFieldAlpha.setText(alpha + "");
					jTextFieldAlpha.setEnabled(true);
				} else {
					jTextFieldAlpha.setText("(not yet implemented, using 0.05)"); // /
					// 0.01
					// /
					// 0.001
					jTextFieldAlpha.setEnabled(false);
				}
			}
		});
		JComponent panelProb = TableLayout.getSplit(specifyAlpha, jTextFieldAlpha, TableLayout.PREFERRED,
							TableLayout.PREFERRED);
		fp.addComp(panelProb);
		
		fp.addComp(TableLayout.get3Split(new JLabel("<html>Ratio (Lower / Upper limit): "), minRatio, maxRatio,
							TableLayout.PREFERRED, 50, 50));
		
		fp.layoutRows();
		
		result.add(fp, "1,4");
		
		FolderPanel fp2 = new FolderPanel("Visualization Settings", false, true, false, null);
		fp2.setBackground(null);
		fp2.setFrameColor(new JTabbedPane().getBackground(), Color.BLACK, 0, 0);
		fp2.setEmptyBorderWidth(0);
		
		ClassLoader cl = this.getClass().getClassLoader();
		String path = this.getClass().getPackage().getName().replace('.', '/');
		ImageIcon icon = new ImageIcon(cl.getResource(path + "/ttestCircleSize.png"));
		JLabel ttestCircleSize = new JLabel("T-Test-Marker Size", icon, JLabel.RIGHT);
		ttestCircleSize.setBackground(Color.WHITE);
		ttestCircleSize.setOpaque(true);
		
		double curVal = 10d;
		Graph graph = null;
		try {
			EditorSession session = GravistoService.getInstance().getMainFrame().getActiveEditorSession();
			graph = session.getGraph();
			curVal = ((Double) AttributeHelper.getAttributeValue(graph, "", AttributeHelper.id_ttestCircleSize,
								new Double(10.0d), new Double(10.0d))).doubleValue();
		} catch (Exception e) {
			// empty
		}
		
		final SpinnerNumberModel numberModel = new SpinnerNumberModel(curVal, 0d, Double.MAX_VALUE, 0.5d);
		numberModel.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				try {
					EditorSession session = GravistoService.getInstance().getMainFrame().getActiveEditorSession();
					Graph g = session.getGraph();
					AttributeHelper.setAttribute(g, "", AttributeHelper.id_ttestCircleSize, new Double(numberModel
										.getNumber().doubleValue()));
				} catch (Exception err) {
					// empty
				}
			}
		});
		JSpinner circleSize = new JSpinner(numberModel);
		circleSize.setPreferredSize(new Dimension(50,10));
//		fp2.addComp(TableLayout.get3Split(ttestCircleSize, new JLabel(""), circleSize, TableLayout.PREFERRED, 3,
//							TableLayout.PREFERRED));
		fp2.addComp(TableLayout.get3Split(ttestCircleSize, new JLabel(""), circleSize, TableLayout.PREFERRED, 3,
				TableLayout.PREFERRED));


		addStatusText1.setOpaque(false);
		addStatusText1.setSelected(showStatusResult);
		addStatusText1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showStatusResult = addStatusText1.isSelected();
				addStatusText2.setSelected(showStatusResult);
				addStatusText3.setSelected(showStatusResult);
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

		fp2.addComp(TableLayout.get3Split(addStatusText1, new JLabel(""), clearStatus, TableLayout.PREFERRED, 5,
							TableLayout.PREFERRED));
		
		fp2.layoutRows();
		result.add(fp2/*.getBorderedComponent(5, 0, 0, 0)*/, "1,5");
		
//		result.validate();
		return result;
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == doTest && alphaSpecified)
			jTextFieldAlpha.setText(new Double(alpha).toString());
		
		EditorSession session = GravistoService.getInstance().getMainFrame().getActiveEditorSession();
		
		Selection selection = null;
		if (session != null)
			selection = session.getSelectionModel().getActiveSelection();
		
		Graph graph = null;
		if (session != null)
			graph = session.getGraph();
		
		if (e.getSource() == doTest) {
			refreshReferenceInfo(GraphHelper.getSelectedOrAllNodes(selection, graph));
			doTtest(GraphHelper.getSelectedOrAllGraphElements(selection, graph),
								sampleCalcType_2doublet_3welch_4wilcoxon_5ratio, graph, showStatusResult);
		}
		
		
	}
	
	

}
