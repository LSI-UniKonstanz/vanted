/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 14.11.2005 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.som;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;

import org.AttributeHelper;
import org.BioStatisticalCategoryDataset;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.labels.StandardCategoryLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.AbstractCategoryItemRenderer;
import org.jfree.data.CategoryDataset;

import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.ColorHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.chart_colors.ChartColorAttribute;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.chartDrawComponent.MyComparableDataPoint;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.chartDrawComponent.MyDefaultShapeDrawingSupplier;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.chartDrawComponent.MyLineAndShapeRenderer;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes.Experiment2GraphHelper;

public class MultiDataView extends AbstractAlgorithm {
	
	public String getName() {
		return "Show Data Chart";
	}
	
	@Override
	public String getCategory() {
		return "Mapping";
	}
	
	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(Arrays.asList(Category.CHART, Category.MAPPING, Category.DATA));
	}
	
	@Override
	public String getDescription() {
		return "<html>" + "This command makes it possible to view multiple measured substances<br>"
				+ "in a single data chart.<br>" + "First select a number of nodes or edges with mapped data.<br>"
				+ "After perfoming this command a diagram which contains the selected<br>"
				+ "measurement data will be shown.<br>"
				+ "Currently this command works best with time-series data as it uses<br>"
				+ "a line chart to display the data.";
	}
	
	@Override
	public void check() throws PreconditionException {
		super.check();
		if (graph == null || graph.getGraphElements().size() <= 0)
			throw new PreconditionException("No graph available or graph empty!");
	}
	
	@Override
	public boolean isLayoutAlgorithm() {
		return false;
	}
	
	public void execute() {
		
		int axisRotation = ((Double) AttributeHelper.getAttributeValue(graph, "", "node_plotAxisRotation",
				Double.valueOf(0d), Double.valueOf(0))).intValue();
		
		boolean plotHor = ((Boolean) AttributeHelper.getAttributeValue(graph, "", "node_plotOrientationHor",
				Boolean.valueOf(true), Boolean.valueOf(true))).booleanValue();
		PlotOrientation orientation;
		if (plotHor)
			orientation = PlotOrientation.VERTICAL;
		else
			orientation = PlotOrientation.HORIZONTAL;
		
		((Boolean) AttributeHelper.getAttributeValue(graph, "", "node_showCategoryAxis", Boolean.valueOf(false),
				Boolean.valueOf(false))).booleanValue();
		((Boolean) AttributeHelper.getAttributeValue(graph, "", "node_showRangeAxis", Boolean.valueOf(false),
				Boolean.valueOf(false))).booleanValue();
		
		boolean showShapes = ((Boolean) AttributeHelper.getAttributeValue(graph, "", "node_lineChartShowShapes",
				Boolean.valueOf(true), Boolean.valueOf(true))).booleanValue();
		boolean showLines = ((Boolean) AttributeHelper.getAttributeValue(graph, "", "node_lineChartShowLines",
				Boolean.valueOf(true), Boolean.valueOf(true))).booleanValue();
		boolean showStdDev = ((Boolean) AttributeHelper.getAttributeValue(graph, "", "node_lineChartShowStdDev",
				Boolean.valueOf(false), Boolean.valueOf(false))).booleanValue();
		
		boolean showStdDevRangeLine = ((Boolean) AttributeHelper.getAttributeValue(graph, "",
				"node_lineChartShowStdDevRangeLine", Boolean.valueOf(true), Boolean.valueOf(true))).booleanValue();
		
		Double temp = (Double) AttributeHelper.getAttributeValue(graph, "", "node_outlineBorderWidth", Double.valueOf(4d),
				Double.valueOf(4d));
		((Boolean) AttributeHelper.getAttributeValue(graph, "", "node_halfErrorBar", Boolean.valueOf(false),
				Boolean.valueOf(false))).booleanValue();
		
		float outlineBorderWidth = temp.floatValue();
		
		temp = (Double) AttributeHelper.getAttributeValue(graph, "", "node_chartShapeSize", Double.valueOf(6d),
				Double.valueOf(6d));
		float shapeSize = temp.floatValue();
		
		temp = (Double) AttributeHelper.getAttributeValue(graph, "", "node_chartStdDevLineWidth", Double.valueOf(4d),
				Double.valueOf(4d));
		float stdDevLineWidth = temp.floatValue();
		
		// ChartColorAttribute chartColorAttribute = (ChartColorAttribute)
		// AttributeHelper
		// .getAttributeValue(graph, ChartColorAttribute.attributeFolder,
		// ChartColorAttribute.attributeName,
		// new ChartColorAttribute(), new ChartColorAttribute());
		// ArrayList<Color> seriesColors = chartColorAttribute.getSeriesColors();
		// ArrayList<Color> seriesOutlineColors = chartColorAttribute
		// .getSeriesOutlineColors();
		ArrayList<Iterable<SubstanceInterface>> mappedDataList = new ArrayList<Iterable<SubstanceInterface>>();
		for (org.graffiti.graph.GraphElement graphElement : getSelectedOrAllGraphElements()) {
			Iterable<SubstanceInterface> mappedDataListEval = Experiment2GraphHelper
					.getMappedDataListFromGraphElement(graphElement);
			if (mappedDataListEval != null) {
				mappedDataList.add(mappedDataListEval);
			}
		}
		BioStatisticalCategoryDataset dataset = getDataset(mappedDataList, graph, showStdDev || showStdDevRangeLine,
				false);
		JFreeChart chart = null;
		chart = createLineChart(dataset, null, orientation, false /* show legend */, null /* domainAxis */,
				null /* rangeAxis */, /* showRangeAxis */true, true/* showCategoryAxis */, outlineBorderWidth,
				axisRotation, showShapes, showLines, shapeSize, showStdDev, stdDevLineWidth, showStdDevRangeLine,
				Double.NaN /* lowerBound */, Double.NaN /* upperBound */);
		JPanel chartPanel = new ChartPanel(chart, true, true, true, true, true);
		MainFrame.showMessageWindow(getName(), chartPanel);
	}
	
	private JFreeChart createLineChart(CategoryDataset dataset, String chartTitle, PlotOrientation orientation,
			boolean showLegend, String domainAxis, String rangeAxis, boolean showRangeAxis, boolean showCategoryAxis,
			float outlineBorderWidth, int axisRotation, boolean showShapes, boolean showLines, float shapeSize,
			boolean showStdDevAsT, float stdDevLineWidth, boolean showStdDevAsFillRange, double minRangeVisible,
			double maxRangeVisible) {
		JFreeChart chart = ChartFactory.createLineChart(chartTitle, domainAxis, // "DOMAIN
				// AXIS",
				rangeAxis, // "RANGE AXIS",
				dataset, orientation, showLegend, false, false);
		
		CategoryPlot plot = chart.getCategoryPlot();
		setCategoryAxisOptions(plot.getDomainAxis(), orientation, showCategoryAxis, axisRotation);
		
		plot.setDrawingSupplier(new MyDefaultShapeDrawingSupplier(shapeSize));
		
		plot.setBackgroundPaint(null);
		// plot.setBackgroundPaint(getBackgroundCol());
		
		// plot.getRangeAxis().setLabelAngle(90);
		
		plot.getRangeAxis().setVisible(showRangeAxis);
		BioStatisticalCategoryDataset bsc = (BioStatisticalCategoryDataset) dataset;
		double currLB = plot.getRangeAxis().getLowerBound();
		double currUB = plot.getRangeAxis().getUpperBound();
		double minVal = bsc.getMinimumRangeValue().doubleValue();
		double maxVal = bsc.getMaximumRangeValue().doubleValue();
		double lowerMargin = plot.getRangeAxis().getLowerMargin();
		double upperMargin = plot.getRangeAxis().getUpperMargin();
		if (minVal < currLB)
			plot.getRangeAxis().setLowerBound(minVal < 0 ? minVal * (1 - lowerMargin) : 0);
		if (maxVal * (1 + upperMargin) > currUB)
			plot.getRangeAxis().setUpperBound(maxVal * (1 + upperMargin));
		
		if (!Double.isNaN(minRangeVisible))
			plot.getRangeAxis().setLowerBound(minRangeVisible);
		if (!Double.isNaN(maxRangeVisible))
			plot.getRangeAxis().setUpperBound(maxRangeVisible);
		
		MyLineAndShapeRenderer myRenderer = new MyLineAndShapeRenderer();
		plot.setRenderer(myRenderer);
		
		MyLineAndShapeRenderer renderer = (MyLineAndShapeRenderer) plot.getRenderer();
		
		renderer.setDrawStdDev(showStdDevAsT, showStdDevAsFillRange);
		renderer.setStdDevLineWidth(stdDevLineWidth);
		setSeriesColorsAndStroke(renderer, outlineBorderWidth, graph);
		
		renderer.setDrawShapes(showShapes);
		renderer.setDrawLines(showLines);
		
		// renderer.getSeriesShape(0);
		
		// renderer.setSeriesStroke(0, new BasicStroke(2.0f,
		// BasicStroke.CAP_ROUND,
		// BasicStroke.JOIN_ROUND, 1.0f, new float[] { 10.0f, 6.0f }, 0.0f));
		// renderer.setSeriesStroke(1, new BasicStroke(2.0f,
		// BasicStroke.CAP_ROUND,
		// BasicStroke.JOIN_ROUND, 1.0f, new float[] { 6.0f, 6.0f }, 0.0f));
		// renderer.setSeriesStroke(2, new BasicStroke(2.0f,
		// BasicStroke.CAP_ROUND,
		// BasicStroke.JOIN_ROUND, 1.0f, new float[] { 2.0f, 6.0f }, 0.0f));
		
		chart.setBackgroundPaint(null);
		
		renderer.setItemLabelsVisible(false);
		renderer.setLabelGenerator(new StandardCategoryLabelGenerator());
		
		return chart;
	}
	
	private static void setCategoryAxisOptions(final CategoryAxis axis, PlotOrientation orientation, boolean showCategoryAxis,
			int axisRotation) {
		if (orientation == PlotOrientation.VERTICAL) {
			axis.setCategoryLabelPositions(
					CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 180 * axisRotation));
		} else {
			axis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(0));
		}
		axis.setVisible(showCategoryAxis);
	}
	
	private static BioStatisticalCategoryDataset getDataset(List<Iterable<SubstanceInterface>> xmldataListOfList, Graph g,
			boolean alsoUsedForPlottingStdDev,
			// ArrayList<Color> seriesColors,
			// ArrayList<Color> seriesOutlineColors,
			boolean showOnlyHalfErrorBar) {
		
		Double markerSize = (Double) AttributeHelper.getAttributeValue(g, "", AttributeHelper.id_ttestCircleSize,
				Double.valueOf(10.0d), Double.valueOf(10.0d));
		boolean useStdErrInsteadOfStdDev = ((Boolean) AttributeHelper.getAttributeValue(g, "", "node_useStdErr",
				Boolean.valueOf(false), Boolean.valueOf(false))).booleanValue();
		BioStatisticalCategoryDataset dataset = new BioStatisticalCategoryDataset(markerSize.floatValue());
		int idx = 0;
		ColorHelper ch = new ColorHelper(g);
		for (Iterable<SubstanceInterface> xmldataList : xmldataListOfList) {
			for (SubstanceInterface xmldata : xmldataList) {
				idx++;
				List<MyComparableDataPoint> ss = NodeTools.getSortedAverageDataSetValues(xmldata);
				
				for (MyComparableDataPoint mcdp : ss) {
					String col = getZeros(idx, 5) + mcdp.serie;
					Color color1 = ch.getColor1ForRowKey(mcdp.serie);
					ch.setColor1For(col, color1);
					dataset.add(mcdp.mean, mcdp.getStddev(useStdErrInsteadOfStdDev), col, mcdp.timeValue,
							mcdp.ttestIsReference, mcdp.ttestIsSignificantDifferent, mcdp.measurementUnit,
							mcdp.timeUnit, alsoUsedForPlottingStdDev, showOnlyHalfErrorBar);
				}
			}
		}
		
		return dataset;
	}
	
	private static String getZeros(int idx, int len) {
		String result = idx + "";
		while (result.length() < len)
			result = "0" + result;
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private static void setSeriesColorsAndStroke(AbstractCategoryItemRenderer renderer, float outlineBorderWidth,
			Graph graph) {
		renderer.setStroke(new BasicStroke(outlineBorderWidth));
		ChartColorAttribute chartColorAttribute = (ChartColorAttribute) AttributeHelper.getAttributeValue(graph,
				ChartColorAttribute.attributeFolder, ChartColorAttribute.attributeName, new ChartColorAttribute(),
				new ChartColorAttribute());
		
		ArrayList<Color> colors1 = chartColorAttribute.getSeriesColors(renderer.getPlot().getDataset().getRowKeys());
		ArrayList<Color> colors2 = chartColorAttribute
				.getSeriesOutlineColors(renderer.getPlot().getDataset().getRowKeys());
		
		if (outlineBorderWidth >= 0)
			renderer.setStroke(new BasicStroke(outlineBorderWidth));
		int i = 0;
		for (Color c1 : colors1) {
			renderer.setSeriesPaint(i, c1, false);
			i++;
		}
		i = 0;
		for (Color c2 : colors2) {
			renderer.setSeriesOutlinePaint(i, c2, false);
			i++;
		}
	}
}
