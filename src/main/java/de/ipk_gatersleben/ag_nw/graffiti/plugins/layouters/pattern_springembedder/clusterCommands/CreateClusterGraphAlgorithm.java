/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 01.09.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

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
import org.graffiti.selection.Selection;
import org.graffiti.session.Session;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.cluster_colors.ClusterColorAttribute;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.cluster_colors.ClusterColorParameter;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.circle.NullLayoutAlgorithm;

/**
 * @author Christian Klukas (c) 2004 IPK-Gatersleben
 * 
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

	@Override
	public Parameter[] getParameters() {

		Graph g = graph;
		Set<String> clusters = new TreeSet<String>();
		for (Iterator<?> it = g.getNodesIterator(); it.hasNext();) {
			Node n = (Node) it.next();
			String clusterId = NodeTools.getClusterID(n, "");
			if (!clusterId.equals(""))
				clusters.add(clusterId);
		}

		ClusterColorAttribute cca = (ClusterColorAttribute) AttributeHelper.getAttributeValue(g,
				ClusterColorAttribute.attributeFolder, ClusterColorAttribute.attributeName,
				ClusterColorAttribute.getDefaultValue(clusters), new ClusterColorAttribute("resulttype"), false);

		// cca.ensureMinimumColorSelection(clusters.size());
		cca.updateClusterList(clusters);
		ClusterColorAttribute cca_new = new ClusterColorAttribute(ClusterColorAttribute.attributeName, cca.getString());
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

		ClusterColorAttribute cca = (ClusterColorAttribute) ((ClusterColorParameter) params[i++]).getValue();
		if (graph.getAttributes().getCollection().containsKey(cca.getPath()))
			graph.removeAttribute(cca.getPath());
		graph.addAttribute(cca, ClusterColorAttribute.attributeFolder);

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

	public void execute() {
		HashMap<String, Integer> clusterNodeIDandNumberOfContainingNodes = new HashMap<String, Integer>();
		Graph clusterReferenceGraph = GraphHelper.createClusterReferenceGraph(graph,
				clusterNodeIDandNumberOfContainingNodes);
		AttributeHelper.setAttribute(graph, "cluster", "clustergraph", clusterReferenceGraph);

		if (colorCode) {
			PajekClusterColor pcc = new PajekClusterColor();
			pcc.attach(graph, new Selection(""));
			pcc.execute();
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

	private void processNodeSize(Graph clusterReferenceGraph,
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

	private void processEdgeWidth(Graph clusterReferenceGraph, double minWidth, double maxWidth) {
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
