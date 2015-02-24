/**
 * 
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.statistics;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.AttributeHelper;
import org.ErrorMsg;
import org.HelperClass;
import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.DistributionFactory;
import org.apache.commons.math.distribution.TDistribution;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.CategoryItemRenderer;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.GraphElementHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.IPKGraffitiView;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.chartDrawComponent.XmlDataChartComponent;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes.Experiment2GraphHelper;

/**
 * @author matthiak
 *
 */
public class StatisticsHelper implements HelperClass{
	
	
	public static double epsilon = 0.0000001;
	
	
	
	public static CorrelationResult calculateCorrelation(MyXML_XYDataset dataset, String dataset1, String dataset2,
			boolean mergeDataset, int dataset2offset, double prob, boolean rankOrder) {
		CorrelationResult corrRes = new CorrelationResult(dataset1, dataset2);
		StringBuilder calculationHistory = new StringBuilder();
		double sum_x = 0;
		double sum_y = 0;
		double sum_x_x = 0; // sum x*x
		double sum_y_y = 0; // sum x*x
		double sum_x_y = 0; // sum x*y
		String mergedSeries = "Series: ";
		int n = 0;
		String initString = "<html>Highest significant (>=" + (prob * 100) + "%) correlation"
				+ (dataset2offset == 0 ? "" : " (timeshift=" + dataset2offset + ")") + ":<br>"
				+ "<!-- optstart --><small><small><table border=\"1\"><tr><td><b>Series</b></td><td><b>Index " + dataset1
				+ "</b></td><td><b>Index " + dataset2 + "</b></td><td><b>Value " + dataset1 + "</b></td><td><b>Value "
				+ dataset2 + "</b></td></tr>\n";
		int maxROW = 1000;
		calculationHistory.append(initString);
		int rowDescription = 0;
		// System.out.println("---");
		for (int series = 0; series < dataset.getSeriesCount(); series++) {
			if (!mergeDataset) {
				calculationHistory = new StringBuilder();
				calculationHistory.append(initString);
				rowDescription = 0;
				sum_x = 0;
				sum_y = 0;
				sum_x_x = 0;
				sum_y_y = 0;
				sum_x_y = 0;
			} else
				mergedSeries += dataset.getSeriesName(series) + (series < dataset.getSeriesCount() - 1 ? ", " : "");
			if (!mergeDataset)
				n = dataset.getItemCount(series);
			else
				n += dataset.getItemCount(series);
			for (int item = 0; item < dataset.getItemCount(series); item++) {
				try {
					double x = dataset.getX(series, item, rankOrder, mergeDataset, dataset2offset);
					double y = dataset.getY(series, item + dataset2offset, rankOrder, mergeDataset, dataset2offset);
					double x_not_ranked;
					if (rankOrder)
						x_not_ranked = dataset.getX(series, item, false, mergeDataset, dataset2offset);
					else
						x_not_ranked = x;
					double y_not_ranked;
					if (rankOrder)
						y_not_ranked = dataset.getY(series, item + dataset2offset, false, mergeDataset, dataset2offset);
					else
						y_not_ranked = y;
					if (Double.isNaN(x) || Double.isNaN(y))
						n--;
					else {
						if (rowDescription < maxROW)
							calculationHistory.append("<tr><td>" + dataset.getSeriesName(series) + "</td><td>" + item + " ("
									+ dataset.getXsrcValue(series, item).timeUnitAndTime + ") </td><td>"
									+ (item + dataset2offset) + " ("
									+ dataset.getYsrcValue(series, item + dataset2offset).timeUnitAndTime + ")" + "</td>"
									+ "<td>" + (rankOrder ? AttributeHelper.formatNumber(x_not_ranked, "#.###") + " -> " : "")
									+ AttributeHelper.formatNumber(x, "#.###") + "</td>" + "<td>"
									+ (rankOrder ? AttributeHelper.formatNumber(y_not_ranked, "#.###") + " -> " : "")
									+ AttributeHelper.formatNumber(y, "#.###") + "</td></tr>\n");
						rowDescription++;
						sum_x += x;
						sum_y += y;
						sum_x_x += x * x;
						sum_y_y += y * y;
						sum_x_y += x * y;
						// System.out.println(x+"\t"+y);
					}
				} catch (IndexOutOfBoundsException ioob) {
					n--;
				}
			}
			if (!mergeDataset) {
				if (n - 2 < 1) {
					ErrorMsg.addErrorMessage("Number of degrees of freedom is too low (DF=" + (n - 2)
							+ "; to few datapoints+" + (mergeDataset ? "!" : "with matching replicate index!")
							+ "+).<br>Correlation between " + dataset1 + " and " + dataset2 + " not calcualted!");
				} else {
					calcAndAddResult(dataset2offset, prob, corrRes, calculationHistory, sum_x, sum_y, sum_x_x, sum_y_y,
							sum_x_y, mergedSeries, n, maxROW, rowDescription, rankOrder);
				}
			}
		}
		if (mergeDataset) {
			calcAndAddResult(dataset2offset, prob, corrRes, calculationHistory, sum_x, sum_y, sum_x_x, sum_y_y, sum_x_y,
					mergedSeries, n, maxROW, rowDescription, rankOrder);
		}
		// System.out.println("N="+n+" (off="+dataset2offset+")");
		return corrRes;
	}


	public static void calcAndAddResult(int dataset2offset, double prob, CorrelationResult corrRes,
			StringBuilder calculationHistory, double sum_x, double sum_y, double sum_x_x, double sum_y_y, double sum_x_y,
			String mergedSeries, int n, int maxROW, int rowDescription, boolean spearMan) {
		double sum_d_xx = sum_x_x - sum_x * sum_x / n;
		double sum_d_yy = sum_y_y - sum_y * sum_y / n;
		double sum_dx_dy = sum_x_y - sum_x * sum_y / n;
		float r = (float) (sum_dx_dy / Math.sqrt(sum_d_xx * sum_d_yy));
		// double rtab = getRtabVal(n - 2, 1 - prob);
		double t_or_z = Double.NaN;
		double myp = Double.NaN;

		TDistribution tDistribution = DistributionFactory.newInstance().createTDistribution(100);


		try {
			double p2;
			if (spearMan) {
				/*
				 * t_or_z = r*Math.sqrt(n-1); p2 =
				 * nDistribution.cumulativeProbability(Math.abs(t_or_z));
				 */
				tDistribution.setDegreesOfFreedom(n - 2);
				t_or_z = Math.abs(r) / Math.sqrt((1 - r * r) / (n - 2));
				p2 = tDistribution.cumulativeProbability(Math.abs(t_or_z));
			} else {
				tDistribution.setDegreesOfFreedom(n - 2);
				t_or_z = r / Math.sqrt((1 - r * r) / (n - 2));
				p2 = tDistribution.cumulativeProbability(Math.abs(t_or_z));
			}
			/*
			 * System.out.println("Significance of Pearson:"); for (int tn=4;
			 * tn<30; tn++) { double tdf = tn-2;
			 * tDistribution.setDegreesOfFreedom(tdf); System.out.println(tn+" "+
			 * AttributeHelper
			 * .formatNumber(transform(tDistribution.inverseCumulativeProbability
			 * (0.05d), tdf), "#.###")+" "+
			 * AttributeHelper.formatNumber(transform(tDistribution
			 * .inverseCumulativeProbability(0.025d), tdf), "#.###")+" "+
			 * AttributeHelper
			 * .formatNumber(transform(tDistribution.inverseCumulativeProbability
			 * (0.01d), tdf), "#.###")+" "+
			 * AttributeHelper.formatNumber(transform(tDistribution
			 * .inverseCumulativeProbability(0.005d), tdf), "#.###")); }
			 * System.out.println("Significance of Spearman:"); for (int tn=4;
			 * tn<30; tn++) { double tdf = tn-2;
			 * tDistribution.setDegreesOfFreedom(tdf); System.out.println(tn+" "+
			 * AttributeHelper
			 * .formatNumber(transformspear(nDistribution.inverseCumulativeProbability
			 * (0.05d), tn), "#.###")+" "+
			 * AttributeHelper.formatNumber(transformspear
			 * (nDistribution.inverseCumulativeProbability(0.025d), tn),
			 * "#.###")+" "+
			 * AttributeHelper.formatNumber(transformspear(nDistribution
			 * .inverseCumulativeProbability(0.01d), tn), "#.###")+" "+
			 * AttributeHelper
			 * .formatNumber(transformspear(nDistribution.inverseCumulativeProbability
			 * (0.005d), tn), "#.###")); }
			 */
			myp = 2 * (1 - p2);
		} catch (IllegalArgumentException iae) {
			calculationHistory.append("<tr><td colspan=\"5\">CALCULATION (to few datapoints): "
					+ iae.getLocalizedMessage() + "</td></tr>\n");
		} catch (MathException e) {
			calculationHistory.append("<tr><td colspan=\"5\">CALCULATION ERROR: " + e.getLocalizedMessage()
					+ "</td></tr>\n");
			ErrorMsg.addErrorMessage(e);
		}

		if (rowDescription > maxROW) {
			calculationHistory.append("<tr><td colspan=\"5\">(" + (rowDescription - maxROW)
					+ " more rows omitted)</td></tr>\n");
		}
		String warningHeading = "";
		String warningText = "";
		if (spearMan && n < 10) {
			warningHeading = "<td><b>Imprecise P Value!</b></td>";
			warningText = "<small>P value might be imprecise for n&lt;10,<br>it is calculated with an approximation to the t-distribution.";
		}
		calculationHistory
		.append("</table><!-- optend --><br>" + "<table border=\"1\">" + "<tr>" + "<td><b>n</b></td>" + "<td><b>"
				+ (spearMan ? "rs" : "r")
				+ "</b></td>"
				+
				// "<td><b>r-tab</b></td>" +
				(spearMan ?
						// "<td><b>~z</b> = rs<*(n-1)^0.5</td>"
						"<td><b>~t</b> = |rs|/((1-rs^2)/df)^0.5</td>"
						: "<td><b>~t</b> = r/((1-r^2)/df)^0.5</td>")
						+ "<td><b>df</b></td>"
						+ (spearMan ? "<td><b>Probability (non-directional)</b><br><small><small>(approximated to t-distribution)</small><small></td>"
								: "<td><b>Probability (non-directional)</b><br><small><small>(approximated to t-distribution)</small><small></td>")
								+ warningHeading + "</tr>" + "<tr>" + "<td>" + n
								+ "</td>"
								+ "<td>"
								+ AttributeHelper.formatNumber(r, "#.######")
								+ "</td>"
								+
								// "<td>"+AttributeHelper.formatNumber(rtab, "#.###")+"</td>"
								// +
								"<td>" + AttributeHelper.formatNumber(t_or_z, "#.######") + "</td>" + "<td>" + (n - 2) + "</td>"
								+ "<td>" + AttributeHelper.formatNumber(myp, "#.######") + "</td>" + warningText + "</tr>"
								+ "</table>");
		corrRes.addR(r, prob, dataset2offset, calculationHistory.toString(), mergedSeries, 1 - myp);
	}

	public static double getAVG(Double[] X) {
		double sum = 0;
		int n = X.length;
		for (int i = 0; i < n; i++)
			sum += X[i].doubleValue();
		return sum / n;
	}

	public static JComponent createScatterPlotBlock(boolean plotAverage, boolean tickMarksVisible,
			boolean showRangeAxis, boolean showLegend, double minimumR, float outlineBorderWidth, boolean mergeDataset,
			double prob, boolean rankOrder, double currGammaValue, Color colR_1, Color colR0, Color colR1,
			Collection<GraphElement> gEe, Graph graph, boolean returnResult, Component lastScatterPlot,
			JComponent placeForScatter) {
		int x = 0;

		ArrayList<GraphElement> graphElements = new ArrayList<GraphElement>();
		for (GraphElement g : gEe) {
			GraphElementHelper geh = new GraphElementHelper(g);
			if (geh.getDataMappings() != null && geh.getDataMappings().size() > 0)
				graphElements.add(g);
		}

		int axisFontSize = ((Integer) AttributeHelper.getAttributeValue(graph, "", "node_plotAxisFontSize", new Integer(
				10), new Integer(10))).intValue();

		MyScatterBlock scatterBlock = new MyScatterBlock(true, axisFontSize);
		for (Iterator<GraphElement> it1 = graphElements.iterator(); it1.hasNext();) {
			x++;
			GraphElement ge1 = it1.next();
			String node1desc;
			if (!(ge1 instanceof Edge))
				node1desc = AttributeHelper.getLabel(ge1, "-?-");
			else {
				Edge nge1 = (Edge) ge1;
				node1desc = AttributeHelper.getLabel(ge1, AttributeHelper.getLabel(nge1.getSource(), "?")
						+ (nge1.isDirected() ? "->" : "--") + AttributeHelper.getLabel(nge1.getTarget(), "?"));
			}
			Iterable<SubstanceInterface> mappedDataList1 = Experiment2GraphHelper.getMappedDataListFromGraphElement(ge1);
			int y = 0;
			for (Iterator<GraphElement> it2 = graphElements.iterator(); it2.hasNext();) {
				y++;
				GraphElement ge2 = it2.next();
				String node2desc;
				if (!(ge2 instanceof Edge))
					node2desc = AttributeHelper.getLabel(ge2, "-?-");
				else {
					node2desc = AttributeHelper.getNiceEdgeOrNodeLabel(ge2, "?");
				}
				List<SubstanceInterface> mappedDataList2 = Experiment2GraphHelper.getMappedDataListFromGraphElement(ge2);
				if (mappedDataList1 != null && mappedDataList2 != null) {
					Iterator<SubstanceInterface> itXml1 = mappedDataList1.iterator();
					Iterator<SubstanceInterface> itXml2 = mappedDataList2.iterator();
					MyXML_XYDataset dataset = new MyXML_XYDataset();
					if (ge1 != ge2) {
						int series = 0;
						while (itXml1.hasNext() && itXml2.hasNext()) {
							series++;
							SubstanceInterface xmldata1 = itXml1.next();
							SubstanceInterface xmldata2 = itXml2.next();
							dataset.addXmlDataSeries(xmldata2, xmldata1, "M" + series, plotAverage);
						}
					}
					JFreeChart chart;
					if (graphElements.size() > 2) {
						// do not include axis labels in case only two
						// substances are compares
						chart = createScatterChart(dataset, null, null, null, graph, tickMarksVisible, showRangeAxis,
								showLegend, outlineBorderWidth);
					} else {
						chart = createScatterChart(dataset, null, node2desc, node1desc, graph, tickMarksVisible,
								showRangeAxis, showLegend, outlineBorderWidth);
					}

					Font af = new Font(Axis.DEFAULT_AXIS_LABEL_FONT.getFontName(), Axis.DEFAULT_AXIS_LABEL_FONT.getStyle(),
							axisFontSize);
					chart.getXYPlot().getRangeAxis().setTickLabelFont(af);
					chart.getXYPlot().getDomainAxis().setTickLabelFont(af);
					chart.getXYPlot().getDomainAxis().setLabelFont(af);
					chart.getXYPlot().getRangeAxis().setLabelFont(af);

					final ChartPanel chartPanel = new ChartPanel(chart, true, true, true, true, true);

					if (ge1 != ge2) {
						final CorrelationResult cr = calculateCorrelation(dataset, node2desc, node1desc, mergeDataset, 0,
								prob, rankOrder);
						// chartPanel.setToolTipText("Mouse-Click for Details");
						chartPanel.addMouseListener(new MouseListener() {
							public void mouseClicked(MouseEvent e) {
								if (e.getButton() == MouseEvent.BUTTON1)
									MainFrame.showMessageDialogWithScrollBars(cr.getRlist(), "Correlation Calculation Result");
								else
									chartPanel.mouseClicked(e);
								if (e.getButton() == MouseEvent.BUTTON3)
									chartPanel.getPopupMenu().show(chartPanel, e.getX(), e.getY());
							}

							public void mousePressed(MouseEvent e) {
								chartPanel.mousePressed(e);
							}

							public void mouseReleased(MouseEvent e) {
								chartPanel.mouseReleased(e);
							}

							public void mouseEntered(MouseEvent e) {
								chartPanel.mouseEntered(e);
							}

							public void mouseExited(MouseEvent e) {
								chartPanel.mouseExited(e);
							}
						});
						if (cr.isAnyOneSignificant(minimumR))
							chartPanel.setBorder(BorderFactory.createLineBorder(getRcolor(cr.getMaxR(), currGammaValue,
									colR_1, colR0, colR1), 3));

						else
							chartPanel.setBorder(BorderFactory.createLineBorder(getRcolor(cr.getMaxR(), currGammaValue,
									colR_1, colR0, colR1), 1));
					}
					scatterBlock.addChartPanel(chartPanel, x, y, node1desc, node2desc);
				}
			}
		}
		if (returnResult)
			return scatterBlock.getChartPanel();

		if (lastScatterPlot != null) {
			if (lastScatterPlot instanceof JPanel) {
				JPanel lsp = (JPanel) lastScatterPlot;
				lsp.removeAll();
			}
			placeForScatter.remove(lastScatterPlot);
		}

		lastScatterPlot = scatterBlock.getChartPanel();
		placeForScatter.add(lastScatterPlot, "1,4");
		placeForScatter.validate();

		return null;
	}

	@SuppressWarnings("unchecked")
	public static JComponent getScatterPlot(Graph graph) {
		Collection<GraphElement> graphElements = (Collection) graph.getNodes();

		boolean plotAverage = false;
		boolean mergeDataset = true;
		boolean rankOrder = false;

		double prob = 0.95;

		Color colR_1 = Color.RED;
		Color colR0 = Color.WHITE;
		Color colR1 = Color.BLUE;

		boolean showRangeAxis = false;
		boolean tickMarksVisible = false;
		boolean showLegend = false;
		float outlineBorderWidth = 10f;

		int currGammaValue = 1;

		double minimumR = 0;

		return createScatterPlotBlock(plotAverage, tickMarksVisible, showRangeAxis, showLegend, minimumR,
				outlineBorderWidth, mergeDataset, prob, rankOrder, currGammaValue, colR_1, colR0, colR1, graphElements,
				graph, true, null, null);
	}

	public static Color getRcolor(float maxOrMinR) {
		return getRcolor(maxOrMinR, 1, Color.red, Color.WHITE, Color.blue);
	}

	/**
	 * Returns col1 if maxOrMinR is -1, returns col2 if maxOrMinR is 1, returns a
	 * color between these colors if marOrMin is between -1 and 1.
	 * 
	 * @param maxOrMinR
	 *           a value between -1 and 1
	 * @param gamma
	 *           Instead of r, r^gamma is used for determining the color. This
	 *           makes it possible to stay longer near col_0.
	 * @param col1
	 *           The returned color in case maxOrMinR is -1
	 * @param col2
	 *           The returned color in case maxOrMinR is 1
	 * @return A average color depending on maxOrMinR
	 */
	public static Color getRcolor(float maxOrMinR, double gamma, Color col__1, Color col_0, Color col_1) {
		Color col1;
		Color col2;
		if (maxOrMinR >= 0) {
			col1 = col_0;
			col2 = col_1;
		} else {
			col1 = col_0;
			col2 = col__1;
		}
		maxOrMinR = Math.abs(maxOrMinR);
		maxOrMinR = (float) Math.pow(maxOrMinR, gamma);
		float red = (col2.getRed() - col1.getRed()) * maxOrMinR + col1.getRed();
		float green = (col2.getGreen() - col1.getGreen()) * maxOrMinR + col1.getGreen();
		float blue = (col2.getBlue() - col1.getBlue()) * maxOrMinR + col1.getBlue();
		float alpha = (col2.getAlpha() - col1.getAlpha()) * maxOrMinR + col1.getAlpha();
		return new Color(red / 255f, green / 255f, blue / 255f, alpha / 255f);
	}
	public static JFreeChart createScatterChart(MyXML_XYDataset dataset, String title, String labelX, String labelY,
			Graph graph, boolean tickMarksVisible, boolean showRangeAxis, boolean showLegend, float outlineBorderWidth) {

		// ChartColorAttribute cca = (ChartColorAttribute) AttributeHelper
		// .getAttributeValue(graph, ChartColorAttribute.attributeFolder,
		// ChartColorAttribute.attributeName, new ChartColorAttribute(),
		// new ChartColorAttribute());
		// ArrayList<Color> seriesColors = cca.getSeriesColors();
		// ArrayList<Color> seriesOutlineColors = cca.getSeriesOutlineColors();
		final JFreeChart chart = ChartFactory.createScatterPlot(title, // chart
				// title
				labelX, // domain axis label
				labelY, // range axis label
				dataset, // data
				PlotOrientation.HORIZONTAL, // orientation
				showLegend, // include legend
				true, // tooltips
				false // urls
				);
		XYPlot p = chart.getXYPlot();
		p.getDomainAxis().setTickLabelsVisible(tickMarksVisible);
		p.getRangeAxis().setTickLabelsVisible(showRangeAxis);
		p.getDomainAxis().setTickMarksVisible(tickMarksVisible);
		p.getRangeAxis().setTickMarksVisible(tickMarksVisible);
		p.setDomainGridlinesVisible(tickMarksVisible);
		p.setRangeGridlinesVisible(showRangeAxis);
		chart.setBorderVisible(false);
		chart.setBackgroundPaint(MyScatterBlock.getBackCol());
		chart.setAntiAlias(IPKGraffitiView.getUseAntialiasingSetting());
		if (p.getRenderer() instanceof CategoryItemRenderer)
			XmlDataChartComponent.setSeriesColorsAndStroke((CategoryItemRenderer) p.getRenderer(), outlineBorderWidth,
					graph); // seriesColors, seriesOutlineColors
		return chart;
	}

	public static DoubleAndSourceList[] getRankValues(Collection<Double> x) {
		return getRankValues(x.toArray(new Double[] {}), new Double[] {});
	}
	
	public static DoubleAndSourceList[] getRankValues(Double[] x, Double[] y) {
		ArrayList<DoubleAndSourceList> values = new ArrayList<DoubleAndSourceList>();
		// System.out.println("n1="+x.length+", n2="+y.length+"\nSample A");
		for (Double d : x) {
			// System.out.println(d);
			values.add(new DoubleAndSourceList(d, 0));
		}
		// System.out.println("----\nSample B");
		for (Double d : y) {
			// System.out.println(d);
			values.add(new DoubleAndSourceList(d, 1));
		}
		DoubleAndSourceList[] valueArray = values.toArray(new DoubleAndSourceList[] {});
		Arrays.sort(valueArray, new Comparator<DoubleAndSourceList>() {
			public int compare(DoubleAndSourceList o1, DoubleAndSourceList o2) {
				return o1.getDoubleValue().compareTo(o2.getDoubleValue());
			}
		});
		
		Stack<DoubleAndSourceList> todo = new Stack<DoubleAndSourceList>();
		int nextRank = 0; // rangs will be still set to 1...n
		for (DoubleAndSourceList testDasl : valueArray) {
			if (todo.size() == 0 || Math.abs(testDasl.getDoubleValue() - todo.peek().getDoubleValue()) < epsilon) {
				todo.push(testDasl);
			} else {
				double rang = (nextRank - todo.size() + 1 + nextRank) / 2d;
				while (!todo.empty()) {
					DoubleAndSourceList dasl = todo.pop();
					dasl.setRank(rang);
				}
				todo.push(testDasl);
			}
			nextRank++;
		}
		double rang = (nextRank + (nextRank + todo.size() - 1)) / 2d;
		while (!todo.empty()) {
			DoubleAndSourceList dasl = todo.pop();
			dasl.setRank(rang);
		}
		// int errorCnt = 0;
		// for (DoubleAndSourceList dasl : values) {
		// if (Double.isNaN(dasl.getRangValue())) {
		// errorCnt++;
		// }
		// }
		// System.out.println("ERRORS: "+errorCnt+" / "+values.size());
		return valueArray;
	}
		
	public static double getStd(Double[] X, double x_) {
		double sumQuadDiff = 0;
		int n = X.length;
		for (int i = 0; i < n; i++)
			sumQuadDiff += (X[i].doubleValue() - x_) * (X[i].doubleValue() - x_);
		return 1 / ((double) n - 1) * sumQuadDiff;
	}
	
	
	public static double ttab(double v, double p) {
		return StatisticTable.backwardT(p, (int) v);
	}

	
}
