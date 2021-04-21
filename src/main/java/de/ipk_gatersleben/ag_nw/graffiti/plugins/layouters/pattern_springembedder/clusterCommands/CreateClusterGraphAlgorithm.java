/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 01.09.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.AttributeHelper;
import org.Release;
import org.ReleaseInfo;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.graphics.NodeGraphicAttribute;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.IntegerParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.session.Session;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.cluster_colors.ClusterColorAttribute;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.cluster_colors.ClusterColorParameter;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.circle.NullLayoutAlgorithm;

/**
 * @author Christian Klukas (c) 2004 IPK-Gatersleben
 * @vanted.revision 2.7.0 Default layout of overview graph, based on source graph.
 */
public class CreateClusterGraphAlgorithm extends AbstractAlgorithm {
	
	boolean resizeNodes = true;
	
	int minNodeSize = 50;
	
	int maxNodeSize = 150;
	
	boolean resizeEdges = true;
	
	int minEdgeSize = 1;
	
	int maxEdgeSize = 10;
	
	boolean colorCode = true;
	
	private ClusterColorAttribute clusterColorAttribute;
	
	@Override
	public Parameter[] getParameters() {
		Collection<String> clusters = GraphHelper.getClusters(graph.getNodes());
		
		clusterColorAttribute = (ClusterColorAttribute) AttributeHelper.getAttributeValue(graph,
				ClusterColorAttribute.attributeFolder, ClusterColorAttribute.attributeName,
				ClusterColorAttribute.getDefaultValue(clusters), new ClusterColorAttribute("resulttype"), false);
		
		// cca.ensureMinimumColorSelection(clusters.size());
		clusterColorAttribute.updateClusterList(clusters);
		ClusterColorAttribute cca_new = new ClusterColorAttribute(ClusterColorAttribute.attributeName, clusterColorAttribute.getString());
		ClusterColorParameter op = new ClusterColorParameter(cca_new, "Cluster-Colors", ClusterColorAttribute.desc);
		
		Parameter[] result = new Parameter[] {
				new BooleanParameter(resizeNodes, "Resize Nodes",
						"Resize Nodes depending on number of nodes related to the cluster-node"),
				new IntegerParameter(minNodeSize, "Min. Node Size", "Node minimum size attribute."),
				new IntegerParameter(maxNodeSize, "Max. Node Size", "Node maximum size attribute."),
				new BooleanParameter(resizeEdges, "Set Edge Thickness",
						"Set edge thickness depending on the number of edges that go from one cluster to another"),
				new IntegerParameter(minEdgeSize, "Min. Edge Thickness", "The minimum edge thickness attribute."),
				new IntegerParameter(maxEdgeSize, "Max. Edge Thickness", "The maximum edge thickness attribute."),
				new BooleanParameter(colorCode, "Color-Code Clusters", "<html>"
						+ "Change the color of the nodes in the source- and cluster-graph according to their cluster number.<br>"
						+ "The colors can be changed later with the corresponding attribute-editor in the graph-tab"),
				op };
		return result;
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		resizeNodes = ((BooleanParameter) params[i++]).getBoolean().booleanValue();
		minNodeSize = ((IntegerParameter) params[i++]).getInteger().intValue();
		maxNodeSize = ((IntegerParameter) params[i++]).getInteger().intValue();
		resizeEdges = ((BooleanParameter) params[i++]).getBoolean().booleanValue();
		minEdgeSize = ((IntegerParameter) params[i++]).getInteger().intValue();
		maxEdgeSize = ((IntegerParameter) params[i++]).getInteger().intValue();
		colorCode = ((BooleanParameter) params[i++]).getBoolean().booleanValue();
		
		clusterColorAttribute = (ClusterColorAttribute) ((ClusterColorParameter) params[i++]).getValue();
		if (graph.getAttributes().getCollection().containsKey(clusterColorAttribute.getPath()))
			graph.removeAttribute(clusterColorAttribute.getPath());
		graph.addAttribute(clusterColorAttribute, ClusterColorAttribute.attributeFolder);
		
	}
	
	public String getName() {
		if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR)
			return "Create Overview Graph";
		else
			return null;
	}
	
	@Override
	public String getCategory() {
		return "Cluster";
	}
	
	@Override
	public String getMenuCategory() {
		return "Network.Cluster.Process Cluster Overview-Graph";
	}
	
	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(Arrays.asList(Category.CLUSTER, Category.GRAPH));
	}
	
	@Override
	public void execute() {
		HashMap<String, Integer> clusterNodeIDandNumberOfContainingNodes = new HashMap<String, Integer>();
		Graph clusterReferenceGraph = GraphHelper.createClusterReferenceGraph(graph,
				clusterNodeIDandNumberOfContainingNodes);
		AttributeHelper.setAttribute(graph, "cluster", "clustergraph", clusterReferenceGraph);
		
		if (colorCode) {
			PajekClusterColor.executeClusterColoringOnGraph(graph, clusterColorAttribute);
			PajekClusterColor.executeClusterColoringOnGraph(clusterReferenceGraph, clusterColorAttribute);
		} else {
			
			PajekClusterColor.removeClusterColoringOnGraph(graph);
			//clusterReferenceGraph is not coloured by default, when created
		}
		if (resizeEdges)
			processEdgeWidth(clusterReferenceGraph, minEdgeSize, maxEdgeSize);
		if (resizeNodes)
			processNodeSize(clusterReferenceGraph, clusterNodeIDandNumberOfContainingNodes, minNodeSize, maxNodeSize);
		
		boolean found = false;
		for (Session s : MainFrame.getSessions())
			if (s.getGraph() == clusterReferenceGraph) {
				found = true;
				break;
			}
		if (!found) {
			clusterReferenceGraph.setName("overview-graph for " + graph.getName());
			MainFrame.getInstance().showGraph(clusterReferenceGraph, null);
		}
		
		MyClusterGraphBasedReLayoutService clusterLayoutService = new MyClusterGraphBasedReLayoutService(false, graph);
		clusterLayoutService.setAlgorithm(new NullLayoutAlgorithm(), null);
		clusterLayoutService.run();
		
		clusterReferenceGraph.setModified(false);
	}
	
	private static void processNodeSize(Graph clusterReferenceGraph,
			HashMap<String, Integer> clusterNodeIDandNumberOfContainingNodes, double minNodeSize, double maxNodeSize) {
		// search minimum and maximum of cluster/node size
		int minNodes = Integer.MAX_VALUE;
		int maxNodes = Integer.MIN_VALUE;
		for (Node n : clusterReferenceGraph.getNodes()) {
			String cluster = NodeTools.getClusterID(n, "");
			if (cluster.equals(""))
				continue;
			String cid = cluster;
			Integer numberOfNodesInThisCluster = clusterNodeIDandNumberOfContainingNodes.get(cid);
			if (numberOfNodesInThisCluster.intValue() > maxNodes)
				maxNodes = numberOfNodesInThisCluster.intValue();
			if (numberOfNodesInThisCluster.intValue() < minNodes)
				minNodes = numberOfNodesInThisCluster.intValue();
			AttributeHelper.setLabel(n,
					"<html>Cluster " + cid + "<br>(" + numberOfNodesInThisCluster.intValue() + " Nodes)");
			AttributeHelper.setAttribute(n, "cluster", "nodecount", numberOfNodesInThisCluster);
			NodeGraphicAttribute na = (NodeGraphicAttribute) n.getAttribute(GraphicAttributeConstants.GRAPHICS);
			na.setShape(GraphicAttributeConstants.ELLIPSE_CLASSNAME);
		}
		for (Node n : clusterReferenceGraph.getNodes()) {
			Integer val = (Integer) AttributeHelper.getAttributeValue(n, "cluster", "nodecount",
					Integer.valueOf(Integer.MAX_VALUE), Integer.valueOf(Integer.MAX_VALUE), false);
			int v = val.intValue();
			if (v == Integer.MAX_VALUE)
				continue;
			double nodeSize;
			if (maxNodes > minNodes) {
				nodeSize = (double) (v - minNodes) / (double) (maxNodes - minNodes) * (maxNodeSize - minNodeSize)
						+ minNodeSize;
			} else
				nodeSize = (minNodeSize + maxNodeSize) / 2;
			
			if (Double.valueOf(nodeSize).isNaN() || Double.valueOf(nodeSize).isInfinite()) {
				AttributeHelper.setSize(n, minNodeSize, minNodeSize);
			} else
				AttributeHelper.setSize(n, nodeSize, nodeSize);
		}
	}
	
	private static void processEdgeWidth(Graph clusterReferenceGraph, double minWidth, double maxWidth) {
		// search min and max edgecount
		int minCnt = Integer.MAX_VALUE;
		int maxCnt = Integer.MIN_VALUE;
		boolean okToProceed = false;
		for (Edge e : clusterReferenceGraph.getEdges()) {
			Integer val = (Integer) AttributeHelper.getAttributeValue(e, "cluster", "edgecount", Integer.valueOf(0),
					Integer.valueOf(0), false);
			if (e.getSource() != e.getTarget()) {
				if (val.intValue() < minCnt)
					minCnt = val.intValue();
				if (val.intValue() > maxCnt)
					maxCnt = val.intValue();
				okToProceed = true;
			}
		}
		// set width of edges according to the edgecount
		if (okToProceed)
			for (Edge e : clusterReferenceGraph.getEdges()) {
				Integer val = (Integer) AttributeHelper.getAttributeValue(e, "cluster", "edgecount", Integer.valueOf(0),
						Integer.valueOf(0), false);
				int v = val.intValue();
				double edgeWidth;
				if (maxCnt > minCnt)
					edgeWidth = (double) (v - minCnt) / (double) (maxCnt - minCnt) * (maxWidth - minWidth) + minWidth;
				else
					edgeWidth = (minWidth + maxWidth) / 2;
				AttributeHelper.setBorderWidth(e, edgeWidth);
			}
	}
}
