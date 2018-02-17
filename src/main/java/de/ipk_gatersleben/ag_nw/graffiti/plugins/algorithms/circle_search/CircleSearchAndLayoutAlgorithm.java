/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.circle_search;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.AttributeHelper;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.DoubleParameter;
import org.graffiti.plugin.parameter.IntegerParameter;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.naive_pattern_finder.NaivePatternFinderAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.naive_pattern_finder.PatternAttributeUtils;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.circle.CircleLayouterAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

/**
 * Searches for circular sub-graphs and optionally applies a CircleLayouter.
 * 
 * Modified in version 2.6.5 to improve parameter handling (Dimitar Garkov).
 * 
 * @author Christian Klukas
 * @version 2.6.5
 */
public class CircleSearchAndLayoutAlgorithm extends AbstractAlgorithm {
	
	/** The minimum number of nodes that can form circle.*/
	public static final Integer MIN_CIRCLE_SIZE = 3;
	
	private int startNodeCount;
	private int endNodeCount;
	private int currentGraphID;
	private double patternNodeDistance = 50;
	private boolean doCircleLayout = true;
	private boolean startWithLargestCircle = true;
	
	@Override
	public String getCategory() {
		return "Layout";
	}
	
	@Override
	public boolean isLayoutAlgorithm() {
		return true;
	}
	/**
	 * @return Find and Layout Circles
	 */
	public String getName() {
		return "Find and Layout Circles";
	}
	
	@Override
	public String getDescription() {
		return "<html>" +
						"This algorithm searches for circular sub-networks<br>" +
						"and applies a circle layout to these sub-networks.<br>" +
						"Please, specify the aproximate node distance and<br>" +
						"the minimum and maximum size of circles (node count).<br>" +
						"<small>Remark: There may be additional, overlapping circles -<br>" +
						"only one of the possibilities is processed.</small>";
	}
	
	@Override
	public Parameter[] getParameters() {
		int nNodes = graph.getNumberOfNodes();
		
		ensureMinMaxHolds();
		
		return new Parameter[] {
							new IntegerParameter((startNodeCount > nNodes) ? nNodes : startNodeCount,
									MIN_CIRCLE_SIZE, nNodes, "Maximum Node Count", ""),
							new IntegerParameter(endNodeCount, MIN_CIRCLE_SIZE, nNodes, "Minimum Node Count", ""),
							new BooleanParameter(startWithLargestCircle, "Start search with large circles",
												"If enabled, the circle search starts with the maximum node count."),
							new BooleanParameter(doCircleLayout, "Apply Circle Layout/Select Nodes",
												"<html>If selected, the a circle layout is applied,<br>" +
																	"if not selected, the circles are selected."),
							new DoubleParameter(patternNodeDistance, "Approximate Node Distance", "") };
	}

	/**
	 * When the user tries to run with min > max.
	 */
	private void ensureMinMaxHolds() {
		if (endNodeCount > startNodeCount)
			endNodeCount = MIN_CIRCLE_SIZE;
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		this.startNodeCount = ((IntegerParameter) params[i++]).getInteger();
		this.endNodeCount = ((IntegerParameter) params[i++]).getInteger();
		this.startWithLargestCircle = ((BooleanParameter) params[i++]).getBoolean();
		this.doCircleLayout = ((BooleanParameter) params[i++]).getBoolean();
		this.patternNodeDistance = ((DoubleParameter) params[i++]).getDouble();
	}
	
	@Override
	public void check() throws PreconditionException {
		if (graph == null)
			throw new PreconditionException("No active network!");
		if (graph.getNumberOfNodes() < MIN_CIRCLE_SIZE)
			throw new PreconditionException("Network too small (less than 3 nodes)!");
		
		onGraphSelectionChange();			
	}

	/**
	 * Update parameters, when new graph is selected.
	 */
	private void onGraphSelectionChange() {
		//first or new graph -> set counts
		if (currentGraphID != graph.hashCode()) {
			startNodeCount = graph.getNumberOfNodes();
			endNodeCount = MIN_CIRCLE_SIZE;
			currentGraphID = graph.hashCode();
		}
	}
	
	public void execute() {
		for (GraphElement ge : graph.getGraphElements()) {
			AttributeHelper.deleteAttribute(ge, PatternAttributeUtils.PATTERN_PATH, PatternAttributeUtils.PATTERN_RECORD_PREFIX + "*");
		}
		final BackgroundTaskStatusProviderSupportingExternalCallImpl status = new BackgroundTaskStatusProviderSupportingExternalCallImpl("", "");
		final Graph graph = this.graph;
		final boolean startLarge = startWithLargestCircle;
		BackgroundTaskHelper.issueSimpleTask(this.getName(), "Please wait...",
							new Runnable() {
								public void run() {
									List<Graph> circleGraphs = new LinkedList<Graph>();
									for (int n = startNodeCount; n >= endNodeCount; n--) {
										if (status.wantsToStop())
											break;
										
										status.setCurrentStatusText2("Create circle of size " + n);
										Graph circleGraph = new AdjListGraph();
										Node previousNode = null;
										Node firstNode = null;
										
										for (int i = 0; i < n; i++) {
											if (status.wantsToStop())
												break;
											
											Node node = circleGraph.addNode(AttributeHelper.getDefaultGraphicsAttributeForNode(20, 20));
											if (firstNode == null)
												firstNode = node;
											if (previousNode != null)
												circleGraph.addEdge(previousNode, node, true);

											previousNode = node;
										}
										if (firstNode != null && previousNode != null)
											circleGraph.addEdge(firstNode, previousNode, true);
										circleGraph.setName("circle_" + circleGraph.getNumberOfNodes());
										circleGraphs.add(circleGraph);
									}
									if (!status.wantsToStop()) {
										status.setCurrentStatusText2("Search circles in network...");
										if (circleGraphs.size() > 0) {
											CircleLayouterAlgorithm layout = null;
											if (doCircleLayout) {
												layout = new CircleLayouterAlgorithm();
												layout.setPatternNodeDistance(patternNodeDistance);
											}
											try {
												graph.getListenerManager().transactionStarted(this);
												NaivePatternFinderAlgorithm.searchPatterns(
																	graph,
																	circleGraphs,
																	layout,
																	true,
																	true,
																	startLarge,
																	status
																	);
											} finally {
												graph.getListenerManager().transactionFinished(this, true);
												GraphHelper.issueCompleteRedrawForGraph(graph);
											}
										}
										status.setCurrentStatusText2("Processing finished");
									}
								}
							},
							null, status);
	}
	
	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(Arrays.asList(
				Category.GRAPH,
				Category.LAYOUT,
				Category.SEARCH
				));
	}

	@Override
	public boolean mayWorkOnMultipleGraphs() {
		return true;
	}
}
