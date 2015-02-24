/**
 * 
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.statistics;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.AttributeHelper;
import org.ErrorMsg;
import org.SystemInfo;
import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.DistributionFactory;
import org.apache.commons.math.distribution.NormalDistribution;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math.stat.inference.TTestImpl;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.plugin.inspector.InspectorTab;
import org.graffiti.session.EditorSession;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.TtestInfo;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes.Experiment2GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

/**
 * @author matthiak
 *
 */
public abstract class TabStatisticsSharedFunctionality extends InspectorTab {

	private static final long serialVersionUID = 6604381016296951038L;

	/*
	 * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++ 
	 */
	protected JTextField jTextFieldAlpha;
	
	protected boolean alphaSpecified = true;
	
	protected double alpha = 0.05;
	
	protected double ratioL = 0.8;
	
	protected double ratioU = 1.2;
	
	protected int sampleCalcType_2doublet_3welch_4wilcoxon_5ratio = 2;
	
	protected boolean showStatusResult = true;


	String referenceSelection;
	HashSet<String> validConditions;
	
	
	protected final ArrayList<Edge> correlationEdges = new ArrayList<Edge>();

	
	JButton doTest;
	
	JCheckBox addStatusText1 = new JCheckBox("<html>Add calculation details to node status");
	JCheckBox addStatusText2 = new JCheckBox("<html>Add calculation details to edge status");
	JCheckBox addStatusText3 = new JCheckBox("<html>Add calculation details to node status");

	protected static DistributionFactory factory = DistributionFactory.newInstance();
	protected static NormalDistribution normalDistribution = factory.createNormalDistribution();

	
	/*
	 * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++ 
	 */
	
	JButton doScatterPlotButton;
	
	protected Component lastScatterPlot = null;
	
	protected JComponent placeForScatter;

	
	JCheckBox checkBoxPlotAverage1;
	JCheckBox checkBoxPlotAverage2;
	JCheckBox checkBoxPlotAverage3;

	protected JTextField jTextFieldProb3scatter;

	protected JTextField jTextFieldMinR3;

	protected JSlider gammaSlider2scatter;

	
	protected boolean plotAverage = false;
	
	protected double prob = 0.95;
	protected double minimumR = 0;

	protected boolean rankOrder = false;

	protected boolean mergeDataset = true;

	
	protected boolean showRangeAxis = false;
	protected boolean tickMarksVisible = false;
	protected boolean showLegend = false;
	
	protected boolean colorCodeEdgesWithCorrelationValue = true;

	
	protected float outlineBorderWidth = 10f;

	
	HashMap<Integer, ButtonGroup> correlationTypeButtonGroups = new HashMap<Integer, ButtonGroup>();
		
	HashMap<Integer, ButtonGroup> datasetButtonGroups = new HashMap<Integer, ButtonGroup>();
	
	
	
	protected Color colR_1 = Color.RED;
	protected Color colR0 = Color.WHITE;
	protected Color colR1 = Color.BLUE;

	
	ArrayList<JButton> col1buttons = new ArrayList<JButton>();
	ArrayList<JButton> col2buttons = new ArrayList<JButton>();
	ArrayList<JButton> col3buttons = new ArrayList<JButton>();
	
	protected int currGammaValue = 1;
	
	
	protected boolean considerTimeShifts = false;
	
	protected boolean dontAddNewEdgesUpdateOld = false;
	

	protected JComponent getCorrelationTypeEditor(Integer i) {
		
		JRadioButton pearsonButton = new JRadioButton("Pearson's product-moment correlation");
		JRadioButton spearmanButton = new JRadioButton("Spearman's rank correlation");
		JRadioButton quadrantButton = new JRadioButton("Quadrant correlation");
		JRadioButton kendallButton = new JRadioButton("Kendall's correlation");
		pearsonButton.setSelected(rankOrder == false);
		spearmanButton.setSelected(rankOrder == true);
		pearsonButton.setOpaque(false);
		spearmanButton.setOpaque(false);
		quadrantButton.setOpaque(false);
		kendallButton.setOpaque(false);
		
		ButtonGroup bg = new ButtonGroup();
		bg.add(pearsonButton);
		bg.add(spearmanButton);
		bg.add(quadrantButton);
		bg.add(kendallButton);
		
		correlationTypeButtonGroups.put(i, bg);
		
		pearsonButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rankOrder = false;
				for (ButtonGroup b : correlationTypeButtonGroups.values()) {
					b.getElements().nextElement().setSelected(true);
				}
			}
		});
		spearmanButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rankOrder = true;
				for (ButtonGroup b : correlationTypeButtonGroups.values()) {
					Enumeration<AbstractButton> en = b.getElements();
					Object o = en.nextElement();
					if (o != null)
						en.nextElement().setSelected(true);
				}
			}
		});
		
		quadrantButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainFrame.showMessageDialog("Not yet implemented!", "Error");
			}
		});
		kendallButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainFrame.showMessageDialog("Not yet implemented!", "Error");
			}
		});
		
		JComponent resultPanel = TableLayout.getSplitVertical(TableLayout.getSplitVertical(pearsonButton, spearmanButton,
							TableLayout.PREFERRED, TableLayout.PREFERRED),
							/*
							 * TableLayout.getSplitVertical( quadrantButton, kendallButton,
							 * TableLayout.PREFERRED, TableLayout.PREFERRED)
							 */null, TableLayout.PREFERRED, TableLayout.PREFERRED);
		
		resultPanel.setOpaque(false);
		resultPanel.setBorder(BorderFactory.createTitledBorder("Calculate"));
		return resultPanel;
	}
	
	@SuppressWarnings("unchecked")
	protected void refreshReferenceInfo(List<Node> nodes) {
		referenceSelection = null;
		ArrayList<String> conditions = new ArrayList<String>();
		for (Iterator<Node> itNodes = nodes.iterator(); itNodes.hasNext();) {
			Node node = itNodes.next();
			ExperimentInterface mappedDataList = Experiment2GraphHelper.getMappedDataListFromGraphElement(node);
			if (mappedDataList != null)
				for (Iterator<SubstanceInterface> itXml = mappedDataList.iterator(); itXml.hasNext();) {
					SubstanceInterface xmldata = itXml.next();
					for (ConditionInterface c : xmldata) {
						if (!conditions.contains(c.getExpAndConditionName()))
							conditions.add(c.getExpAndConditionName());
					}
				}
		}
		
		ArrayList params = new ArrayList();
		
		params.add("Reference Dataset:");
		final JComboBox jc = new JComboBox(conditions.toArray());
		params.add(jc);
		params.add("<html><br>Compare to:");
		params.add(new JLabel());
		
		final ArrayList<JCheckBox> bpl = new ArrayList<JCheckBox>();
		
		for (String c : conditions) {
			params.add("");
			JCheckBox bp = new JCheckBox(c, true);
			bp
								.setToolTipText("If selected, the reference dataset samples will be compared with samples from this condition.");
			params.add(bp);
			
			bpl.add(bp);
		}
		
		ActionListener all = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String sel = (String) jc.getSelectedItem();
				for (JCheckBox bp : bpl) {
					if (bp.getText().equals(sel))
						bp.setEnabled(false);
					else
						bp.setEnabled(true);
				}
			}
		};
		all.actionPerformed(null);
		
		jc.addActionListener(all);
		
		Object[] res = MyInputHelper.getInput("<html>"
							+ "Please select the reference dataset and the conditions.<br><br>", "Select Reference Dataset", params
							.toArray());
		
		if (res == null) {
			referenceSelection = null;
			validConditions = null;
		} else {
			int idx = 0;
			referenceSelection = (String) ((JComboBox) res[idx++]).getSelectedItem();
			idx++;
			validConditions = new HashSet<String>();
			for (String c : conditions) {
				if (((JCheckBox) res[idx++]).isSelected())
					validConditions.add(c);
			}
		}
	}
	
	protected void doTtest(Collection<GraphElement> graphElements, int type_2doublet_3welch_4wilcoxon, Graph g,
						boolean addStatusMessage) {
		String referenceMeasurement = referenceSelection;
		if (referenceMeasurement == null)
			MainFrame.showMessageDialog("Please select a reference measurement.", "No reference dataset selected");
		else
			if (validConditions == null || validConditions.size() < 1) {
				MainFrame.showMessageDialog("<html>" + "At least two conditions (reference and one additional condition)<br>"
									+ "need to be selected.", "No reference dataset selected");
			} else {
				String referenceLineDesc = referenceMeasurement;
				for (GraphElement ge : graphElements) {
					ExperimentInterface mappedDataList = Experiment2GraphHelper.getMappedDataListFromGraphElement(ge);
					List<SampleInterface> samplesInNode = new ArrayList<SampleInterface>();
					HashMap<String, SampleInterface> timeAndConditionNames2sample = new HashMap<String, SampleInterface>();
					if (mappedDataList != null) {
						for (Iterator<SubstanceInterface> itXml = mappedDataList.iterator(); itXml.hasNext();) {
							SubstanceInterface xmldata = itXml.next();
							for (ConditionInterface c : xmldata)
								samplesInNode.addAll(c);
						}
						for (SampleInterface s : samplesInNode) {
							timeAndConditionNames2sample.put(s.getSampleTime() + "/"
												+ s.getParentCondition().getExpAndConditionName(), s);
						}
					}
					String testDesc = "";
					if (type_2doublet_3welch_4wilcoxon == 2)
						testDesc = "homoscedastic Students t-test";
					if (type_2doublet_3welch_4wilcoxon == 4)
						testDesc = "U-test";
					if (type_2doublet_3welch_4wilcoxon == 3)
						testDesc = "Welch's test";
					if (type_2doublet_3welch_4wilcoxon == 5)
						testDesc = "Ratio Check";
					String statusLineText = "<html>[Press <b>F2</b> if text does not completely fit into view]<br>" + "<b>"
										+ testDesc + ", reference: " + referenceLineDesc + "</b>, " + "alpha (two sided): " + alpha
										+ "<br><small>" + "<table border=\"1\">" + "<tr>";
					// now all samples for the current node are gathered
					// if the sample is not a reference measurement, do t-test
					// calculation
					int sampleIdx = 0;
					for (SampleInterface sampleNode : samplesInNode) {
						String line = sampleNode.getParentCondition().getExpAndConditionName();
						sampleIdx++;
						// System.out.println(line);
						if (referenceMeasurement.equals(line)) {
							sampleNode.setSampleTtestInfo(TtestInfo.REFERENCE);
						} else {
							if (validConditions.contains(line)) {
								// search reference sample with the same time value
								String compareTime = sampleNode.getSampleTime();
								SampleInterface refSampleNode = timeAndConditionNames2sample.get(compareTime + "/"
													+ referenceMeasurement);
								if (refSampleNode == null) {
									statusLineText += "<td>No reference sample to compare sample with time point \"" + compareTime
														+ "\" and<br>" + "line name \"" + line + "\" found!</td>";
									sampleNode.setSampleTtestInfo(TtestInfo.H0);
								} else {
									// do t-test and add result to node
									statusLineText += "<td>" + line + (compareTime.length() > 0 ? " [" + compareTime + "]" : "")
														+ "<br><small>";
									StringBuilder statusResult = new StringBuilder();
									
									if (type_2doublet_3welch_4wilcoxon == 2) {
										if (calcuteTtest(refSampleNode.getDataList(), sampleNode.getDataList(), alpha, statusResult,
															sampleIdx))
											sampleNode.setSampleTtestInfo(TtestInfo.H1);
										else
											sampleNode.setSampleTtestInfo(TtestInfo.H0);
									} else
										if (type_2doublet_3welch_4wilcoxon == 3) {
											boolean useApache = true;
											if (calcuteTestVonWelch(refSampleNode.getDataList(), sampleNode.getDataList(), alpha,
																useApache, statusResult, sampleIdx))
												sampleNode.setSampleTtestInfo(TtestInfo.H1);
											else
												sampleNode.setSampleTtestInfo(TtestInfo.H0);
										} else
											if (type_2doublet_3welch_4wilcoxon == 4) {
												if (calcuteWilcoxonTest(refSampleNode.getDataList(), sampleNode.getDataList(), alpha,
																	statusResult, sampleIdx))
													sampleNode.setSampleTtestInfo(TtestInfo.H1);
												else
													sampleNode.setSampleTtestInfo(TtestInfo.H0);
											} else
												if (type_2doublet_3welch_4wilcoxon == 5) {
													if (calcuteRatioTest(refSampleNode.getDataList(), sampleNode.getDataList(), ratioL, ratioU,
																		statusResult, sampleIdx))
														sampleNode.setSampleTtestInfo(TtestInfo.H1);
													else
														sampleNode.setSampleTtestInfo(TtestInfo.H0);
												} else
													ErrorMsg.addErrorMessage("Calculation type not implemented!");
									statusLineText += statusResult.toString() + "</small></td>";
								}
							}
						}
					}
					statusLineText += "</tr></table>";
					if (addStatusMessage)
						AttributeHelper.setToolTipText(ge, statusLineText);
				}
				GraphHelper.issueCompleteRedrawForGraph(g);
			}
	}

	protected boolean calcuteTtest(Double[] X, Double[] Y, double alpha, StringBuilder statusResult, int sampleIdx) { // boolean
		// useApache,
		// DescriptiveStatistics stat = DescriptiveStatistics.newInstance();
		Boolean res1;
		TTestImpl ttest = new TTestImpl();
		double[] xd = new double[X.length];
		int i = 0;
		for (Double v : X)
			xd[i++] = v.doubleValue();
		double[] yd = new double[Y.length];
		i = 0;
		for (Double v : Y)
			yd[i++] = v.doubleValue();
		try {
			res1 = ttest.homoscedasticTTest(xd, yd, alpha);
		} catch (Exception me) {
			ErrorMsg.addErrorMessage("Statistical calculation failed: " + me.getMessage());
			statusResult.append("<b>Sample " + sampleIdx + " : Calculation Error: " + me.getLocalizedMessage()
								+ "</b><br>");
			res1 = null;
		}
		/*
		 * System.out.println("-------------- alpha: " + alpha); print(X, "L1: ");
		 * print(Y, "L2: "); System.out.println("--------------");
		 */
		int n1 = X.length;
		int n2 = Y.length;
		
		double x_ = StatisticsHelper.getAVG(X);
		double y_ = StatisticsHelper.getAVG(Y);
		double s2_1 = StatisticsHelper.getStd(X, x_);
		double s2_2 = StatisticsHelper.getStd(Y, y_);
		
		Math.sqrt(((n1 - 1) * s2_1 + (n2 - 1) * s2_2) /
							// -----------------------------
				(n1 + n2 - 2));
		
		double d_ = Math.abs(x_ - y_);
		statusResult.append("n A= " + n1 + ", " + "n B= " + n2 + ", " + "avg SAMPLE A= "
							+ AttributeHelper.formatNumber(x_, "#.###") + ", " + "avg SAMPLE B="
							+ AttributeHelper.formatNumber(y_, "#.###") + ", " + "variance A="
							+ AttributeHelper.formatNumber(s2_1, "#.###") + ", " + "variance B="
							+ AttributeHelper.formatNumber(s2_2, "#.###") + ", " + "|avg A - avg B|="
							+ AttributeHelper.formatNumber(d_, "#.###") + ", " + "df=" + (n1 + n2 - 2) + "<br>");
		try {
			statusResult.append("<b>P [" + AttributeHelper.formatNumber(ttest.homoscedasticTTest(xd, yd), "#.###")
								+ "] &lt; alpha [" + AttributeHelper.formatNumber(alpha, "#.###") + "]? "
								+ (res1 != null && res1 ? "YES" : "NO") + " : Sample " + sampleIdx + "</b>");
		} catch (IllegalArgumentException e) {
			// empty
		} catch (MathException e) {
			// empty
		}
		// if (useApache)
		return res1 == null ? false : res1;
		/*
		 * else return res2;
		 */
	}
	
	protected boolean calcuteTestVonWelch(Double[] X, Double[] Y, double alpha, boolean useApache,
						StringBuilder statusResult, int sampleIdx) {
		Boolean res1, res2;
		
		DescriptiveStatistics.newInstance();
		TTestImpl ttest = new TTestImpl();
		double[] xd = new double[X.length];
		int i = 0;
		for (Double v : X)
			xd[i++] = v.doubleValue();
		double[] yd = new double[Y.length];
		i = 0;
		for (Double v : Y)
			yd[i++] = v.doubleValue();
		try {
			res1 = ttest.tTest(xd, yd, alpha);
		} catch (Exception me) {
			ErrorMsg.addErrorMessage(me);
			statusResult.append("<b>Sample " + sampleIdx + " : Calculation Error: " + me.getLocalizedMessage()
								+ "</b><br>");
			res1 = null;
		}
		
		int n1 = X.length;
		int n2 = Y.length;
		
		double x_ = StatisticsHelper.getAVG(X);
		double y_ = StatisticsHelper.getAVG(Y);
		double s2_1 = StatisticsHelper.getStd(X, x_);
		double s2_2 = StatisticsHelper.getStd(Y, y_);
		
		double u = (s2_1 / n1) /
							// -------------------------
				(s2_1 / n1 + s2_2 / n2);
		
		double v = 1 / (u * u / (n1 - 1) + (1 - u) * (1 - u) / (n2 - 1));
		double d_ = Math.abs(x_ - y_);
		double t_ = StatisticsHelper.ttab(v, 1 - alpha / 2) * Math.sqrt(s2_1 / n1 + s2_2 / n2);
		
		res2 = d_ > t_;
		if (res1 == null || res2 == null || res2.booleanValue() != res1.booleanValue()) {
			// MainFrame.showMessageDialog("Statistic calculation difference!",
			// "CHECK");
		}
		statusResult.append("n A= " + n1 + ", " + "n B= " + n2 + ", " + "avg SAMPLE A= "
							+ AttributeHelper.formatNumber(x_, "#.###") + ", " + "avg SAMPLE B="
							+ AttributeHelper.formatNumber(y_, "#.###") + ", " + "variance A="
							+ AttributeHelper.formatNumber(s2_1, "#.###") + ", " + "variance B="
							+ AttributeHelper.formatNumber(s2_2, "#.###") + ", " + "|avg A - avg B|="
							+ AttributeHelper.formatNumber(d_, "#.###") + ", " + "df=" + (n1 + n2 - 2) + "<br>");
		try {
			statusResult.append("<b>P [" + AttributeHelper.formatNumber(ttest.tTest(xd, yd), "#.###") + "] &lt; alpha ["
								+ AttributeHelper.formatNumber(alpha, "#.###") + "]? " + (res1 != null && res1 ? "YES" : "NO")
								+ " : Sample " + sampleIdx + "</b>");
		} catch (IllegalArgumentException e) {
			// empty
		} catch (MathException e) {
			// empty
		}
		if (useApache)
			return (res1 == null ? false : res1);
		else
			return res2;
	}

	protected boolean calcuteWilcoxonTest(Double[] X, Double[] Y, double alpha, StringBuilder statusResult, int sampleIdx) {
		double R1, R2;
		R1 = 0;
		R2 = 0;
		
		DoubleAndSourceList[] ranks = StatisticsHelper.getRankValues(X, Y);
		ArrayList<Double> Xranks = new ArrayList<Double>();
		ArrayList<Double> Yranks = new ArrayList<Double>();
		for (DoubleAndSourceList d : ranks) {
			if (d.getSourceListIndex01() == 0)
				Xranks.add(d.getRangValue());
			else
				Yranks.add(d.getRangValue());
		}
		
		for (Double d : Xranks)
			R1 += d;
		for (Double d : Yranks)
			R2 += d;
		statusResult.append("<table><tr><th>RANKS A</th><th>RANKS B</th></tr>");
		statusResult.append("<tr><td>");
		for (Double d : Xranks)
			statusResult.append(d + "<br>");
		statusResult.append("</td><td>");
		for (Double d : Yranks)
			statusResult.append(d + "<br>");
		statusResult.append("</tr></table>");
		int m = Xranks.size();
		int n = Yranks.size();
		double U1, U2;
		U1 = m * n + (m * (m + 1) / 2d) - R1;
		U2 = m * n + (n * (n + 1) / 2d) - R2;
		
		double PG; // Prüfgröße = U1, if U1<U2, else U2
		if (U1 < U2)
			PG = U1;
		else
			PG = U2;
		
		double U = PG;
		statusResult.append("PG=" + U + ", U1=" + U1 + ", U2=" + U2 + ", m=" + m + ", n=" + n + ", R1=" + R1 + ", R2="
							+ R2 + "<br>");
		double epsilon = 0.0000001d;
		if (Math.abs((U1 + U2) - (m * n)) > epsilon) {
			ErrorMsg.addErrorMessage("Sample " + sampleIdx
								+ " : Internal Error, Wilcoxon Test might be calculated incorrectly!");
			statusResult.append("Sample " + sampleIdx
								+ " : Internal Error, Wilcoxon Test might be calculated incorrectly!");
		}
		
		ArrayList<Tie> bindungen = ermittleBindungen(ranks);
		
		double special_sum = 0d;
		double S = m + n;
		
		// für jede Bindung (Anzahl Bindungen = r)
		// t_i = Vielfachheit der Bindung i
		// siehe "Lothar Sachs, Angewandte Statistik, S. 235
		// Walther 1951, nach einem Vorschlag von Kendalls 1945
		int bi = 1;
		for (Tie b : bindungen) {
			double t_i = b.getVielfachheit();
			special_sum += t_i * t_i * t_i - t_i;
			statusResult.append("TIE " + (bi++) + " " + t_i + " times value " + b.getValue() + " with rank "
								+ b.getRankValue() + "<br>");
		}
		// if (Math.abs(special_sum)>epsilon)
		// ErrorMsg.addErrorMessage("Some Ties: "+special_sum);
		special_sum = special_sum / 12d;
		
		double z_ = Math.abs(U - m * n / 2d) / Math.sqrt((m * n / (S * (S - 1))) * ((S * S * S - S) / 12d - special_sum));
		statusResult.append("TIE CORRECTION=" + special_sum + ", " + "z [" + AttributeHelper.formatNumber(z_, "#.###")
							+ "]");
		double compare_z;
		try {
			compare_z = normalDistribution.inverseCumulativeProbability(1 - alpha / 2);
			statusResult.append(" &gt; z_alpha [" + AttributeHelper.formatNumber(compare_z, "#.###") + "]");
			statusResult.append("? <b>" + (z_ > compare_z ? "YES" : "NO") + " : Sample " + sampleIdx + "</b>");
			return z_ > compare_z;
		} catch (MathException e) {
			statusResult.append(", MATH ERROR FOR SAMPLE " + sampleIdx);
			ErrorMsg.addErrorMessage(e);
			return false;
		}
	}
	
	protected boolean calcuteRatioTest(Double[] X, Double[] Y, double belowThisRatio, double overThisRatio,
						StringBuilder statusResult, int sampleIdx) {
		double avgA, avgB;
		double sum = 0;
		for (double xv : X)
			sum += xv;
		avgA = sum / X.length;
		
		sum = 0;
		for (double yv : Y)
			sum += yv;
		avgB = sum / Y.length;
		
		double ratio = avgB / avgA;
		
		boolean result = false;
		
		if (ratio <= belowThisRatio || ratio >= overThisRatio)
			result = true;
		
		statusResult.append("Ratio = " + ratio + " (avg Y/avg X: " + avgB + "/" + avgA + ") : Sample " + sampleIdx
							+ " : " + (ratio < 0 ? "-" : "+") + "<br>");
		
		return result;
	}

	
	protected ArrayList<Tie> ermittleBindungen(DoubleAndSourceList[] rangs) {
		ArrayList<Tie> result = new ArrayList<Tie>();
		double epsilon = 0.0000001;
		for (DoubleAndSourceList rankA : rangs) {
			int cntVielfachheit = 0;
			for (DoubleAndSourceList rankB : rangs) {
				if (rankA != rankB) {
					if (Math.abs(rankB.getRangValue() - rankA.getRangValue()) < epsilon)
						cntVielfachheit++;
				}
			}
			if (cntVielfachheit > 0) {
				boolean rangFound = false;
				for (Tie b : result) {
					if (Math.abs(b.getRankValue() - rankA.getRangValue()) < epsilon) {
						rangFound = true;
						break;
					}
				}
				if (!rangFound)
					result.add(new Tie(rankA.getRangValue(), cntVielfachheit + 1, rankA.getDoubleValue()));
			}
		}
		return result;
	}
	

	
	protected JComponent getMergeOptionEditor(Integer i) {
		
		JRadioButton completeButton = new JRadioButton("All substance values in one step");
		JRadioButton individualButton = new JRadioButton("Each plant/genotype individually");
		completeButton.setSelected(mergeDataset == true);
		individualButton.setSelected(mergeDataset == false);
		completeButton.setOpaque(false);
		individualButton.setOpaque(false);
		ButtonGroup bg = new ButtonGroup();
		bg.add(completeButton);
		bg.add(individualButton);
		
		datasetButtonGroups.put(i, bg);
		
		completeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mergeDataset = true;
				for (ButtonGroup b : datasetButtonGroups.values()) {
					b.getElements().nextElement().setSelected(true);
				}
			}
		});
		individualButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mergeDataset = false;
				for (ButtonGroup b : datasetButtonGroups.values()) {
					Enumeration<AbstractButton> en = b.getElements();
					Object o = en.nextElement();
					if (o != null)
						en.nextElement().setSelected(true);
				}
			}
		});
		
		JComponent resultPanel = TableLayout.getSplitVertical(completeButton, individualButton, TableLayout.PREFERRED,
							TableLayout.PREFERRED);
		
		resultPanel.setOpaque(false);
		resultPanel.setBorder(BorderFactory.createTitledBorder("Correlate"));
		return resultPanel;
	}
	
	protected JComponent getProbabilitySettingPanel(JTextField textFieldProb, JTextField textFieldMinR, JComponent rank) {
		JLabel l2 = new JLabel("Significance >=");
		JLabel l3 = new JLabel("and |r| >=");
		
		l2.setHorizontalAlignment(JLabel.CENTER);
		l3.setHorizontalAlignment(JLabel.CENTER);
		
		JComponent c2 = TableLayout.getSplitVertical(l2, textFieldProb, TableLayout.FILL, TableLayout.FILL);
		c2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
		
		return TableLayout.getSplitVertical(rank, TableLayout.getSplit(c2, TableLayout.getSplitVertical(l3,
							textFieldMinR, TableLayout.FILL, TableLayout.FILL), TableLayout.FILL, TableLayout.FILL),
							TableLayout.PREFERRED, TableLayout.PREFERRED);
	}
	
	protected JSlider getNewGammaSlider(final JLabel jLabelDesc) {
		JSlider gammaSlider = new JSlider(1, 100);
		if (SystemInfo.isMac())
			gammaSlider.setPaintTrack(false);
		gammaSlider.setOpaque(false);
		Dictionary<Integer, JLabel> d = new Hashtable<Integer, JLabel>();
		d.put(new Integer(1), new JLabel("r^1", JLabel.LEFT));
		d.put(new Integer(50), new JLabel("r^50"));
		gammaSlider.setLabelTable(d);
		gammaSlider.setPaintLabels(true);
		gammaSlider.setValue(currGammaValue);
		final String gammaDesc = "<html>Gamma&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
		jLabelDesc.setText(gammaDesc + "<br>correction (" + currGammaValue + ")");
		gammaSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				currGammaValue = ((JSlider) e.getSource()).getValue();
				jLabelDesc.setText(gammaDesc + "<br>correction (" + currGammaValue + ")");
			}
		});
		return gammaSlider;
	}
	
	protected JComponent getNewColorPanel() {
		double border2 = 0;
		double[][] size2 = {
							{ border2, TableLayoutConstants.PREFERRED, TableLayoutConstants.FILL, TableLayoutConstants.FILL,
												TableLayoutConstants.FILL, border2 }, // Columns
				{ border2, TableLayout.PREFERRED, border2 } }; // Rows
		
		JPanel colorPanel = new JPanel();
		colorPanel.setOpaque(false);
		colorPanel.setLayout(new TableLayout(size2));
		
		JLabel descColPanel = new JLabel("Color-Code for r=");
		descColPanel.setOpaque(false);
		colorPanel.add(descColPanel, "1,1");
		JButton jBcol_1 = new JButton("-1");
		JButton jBcol0 = new JButton("0");
		JButton jBcol1 = new JButton("1");
		
		col1buttons.add(jBcol_1);
		col2buttons.add(jBcol0);
		col3buttons.add(jBcol1);
		
		colorPanel.add(jBcol_1, "2,1");
		colorPanel.add(jBcol0, "3,1");
		colorPanel.add(jBcol1, "4,1");
		
		jBcol_1.setBorder(BorderFactory.createLineBorder(colR_1, 3));
		jBcol0.setBorder(BorderFactory.createLineBorder(colR0, 3));
		jBcol1.setBorder(BorderFactory.createLineBorder(colR1, 3));
		jBcol_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color c = getChoosenColor(colR_1);
				if (c != null) {
					colR_1 = c;
					((JButton) e.getSource()).setBorder(BorderFactory.createLineBorder(c, 3));
				}
			}
		});
		jBcol0.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color c = getChoosenColor(colR0);
				if (c != null) {
					colR0 = c;
					((JButton) e.getSource()).setBorder(BorderFactory.createLineBorder(c, 3));
				}
			}
		});
		jBcol1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color c = getChoosenColor(colR1);
				if (c != null) {
					colR1 = c;
					((JButton) e.getSource()).setBorder(BorderFactory.createLineBorder(c, 3));
				}
			}
		});
		return colorPanel;
	}

	
	protected static Color getChoosenColor(Color refCol) {
		MainFrame mf = GravistoService.getInstance().getMainFrame();
		Color c = JColorChooser.showDialog(mf, "Select Color", refCol);
		return c;
	}
	
	protected void checkProbabilityInput(JTextField textFieldProb) {
		try {
			double temp = Double.parseDouble(textFieldProb.getText());
			prob = temp;
		} catch (NumberFormatException nfe) {
			// probability remains unchanged
		}
		textFieldProb.setText(new Double(prob).toString());
	}
	
	protected void checkRinput(JTextField textFieldR) {
		try {
			double temp = Double.parseDouble(textFieldR.getText());
			minimumR = temp;
		} catch (NumberFormatException nfe) {
			// probability remains unchanged
		}
		textFieldR.setText(new Double(minimumR).toString());
	}

	
	protected void findCorrelations(final Collection<Node> nodes, final Graph graph, final EditorSession session) {
		MyCorrlationFinder cf = new MyCorrlationFinder(nodes, graph, session, considerTimeShifts, mergeDataset,
							colorCodeEdgesWithCorrelationValue, minimumR, currGammaValue, colR_1, colR0, colR1, correlationEdges, prob,
							plotAverage, rankOrder, showStatusResult, dontAddNewEdgesUpdateOld);
		BackgroundTaskHelper bth = new BackgroundTaskHelper(cf, cf, "Find Correlations", "Find Correlations", true, false);
		bth.startWork(this);
	}
	
	protected void visualiseCorrelation(GraphElement referenceGraphElement, Graph graph) {
		Collection<GraphElement> allGraphElements = graph.getGraphElements();
		GraphElement ge1 = referenceGraphElement;
		String node1desc = AttributeHelper.getLabel(ge1, "-unnamed-");
		ExperimentInterface mappedDataList1 = Experiment2GraphHelper.getMappedDataListFromGraphElement(ge1);
		graph.getListenerManager().transactionStarted(this);
		
		for (GraphElement ge2 : allGraphElements) {
			String node2desc = AttributeHelper.getLabel(ge2, "-unnamed-");
			ExperimentInterface mappedDataList2 = Experiment2GraphHelper.getMappedDataListFromGraphElement(ge2);
			if (mappedDataList1 != null && mappedDataList2 != null) {
				Iterator<SubstanceInterface> itXml1 = mappedDataList1.iterator();
				Iterator<SubstanceInterface> itXml2 = mappedDataList2.iterator();
				MyXML_XYDataset dataset = new MyXML_XYDataset();
				int series = 0;
				while (itXml1.hasNext() && itXml2.hasNext()) {
					series++;
					SubstanceInterface xmldata1 = itXml1.next();
					SubstanceInterface xmldata2 = itXml2.next();
					dataset.addXmlDataSeries(xmldata2, xmldata1, "M" + series, plotAverage);
				}
				if (ge1 != ge2) {
					CorrelationResult cr = StatisticsHelper.calculateCorrelation(dataset, node2desc, node1desc, mergeDataset, 0, prob,
										rankOrder);
					if (showStatusResult)
						AttributeHelper.setToolTipText(ge2, cr.getRlist());
					float r = cr.getMaxR();
					AttributeHelper.setAttribute(ge2, "statistics", "correlation_r", r);

					if(cr.getMaxTrueCorrProb() > Double.NEGATIVE_INFINITY)
					{
						double prob = 1d - cr.getMaxTrueCorrProb();
						AttributeHelper.setAttribute(ge2, "statistics", "correlation_prob", prob);
					}
					
					AttributeHelper.setFillColor(ge2, StatisticsHelper.getRcolor(r, currGammaValue, colR_1, colR0, colR1));
					if (cr.isAnyOneSignificant(minimumR)) {
						AttributeHelper.setBorderWidth(ge2, 5);
					} else
						AttributeHelper.setBorderWidth(ge2, 1);
				} else {
					AttributeHelper.setFillColor(ge2, Color.YELLOW); // getRcolor(0)
					AttributeHelper.setBorderWidth(ge2, 3);
					if (showStatusResult)
						AttributeHelper.setToolTipText(ge2, "Reference element for correlation analysis");
				}
			}
		}
		graph.getListenerManager().transactionFinished(this);
		// GraphHelper.issueCompleteRedrawForGraph(graph);
	}
	
	
}
