/****************
 * c***************************************************************
 * Copyright ) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 27.08.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.AttributeHelper;
import org.Release;
import org.ReleaseInfo;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.ObjectListParameter;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.cluster_colors.ClusterColorAttribute;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.cluster_colors.ClusterColorParameter;

/**
 * @author Christian Klukas (c) 2004 IPK-Gatersleben
 */
public class PajekClusterColor extends AbstractAlgorithm {

	ClusterColorAttribute cca;

	private static String modeNode = "Colorize Nodes and Edges";
	private static String modeSurr = "Colorize Surrounding of Nodes";

	private String modeOfOperation = modeNode;

	@Override
	public String getName() {
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			return "Pathway-Subgraph Coloring";
		else
			return "Color Clusters";
	}

	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(Arrays.asList(Category.GRAPH, Category.VISUAL, Category.CLUSTER));
	}

	@Override
	public void check() throws PreconditionException {
		if (graph == null)
			throw new PreconditionException("No graph available!");

		Collection<String> clusters = GraphHelper.getClusters(graph.getGraphElements());
		if (clusters.size() <= 0)
			throw new PreconditionException("No cluster information available for this graph!");
	}

	@Override
	public Parameter[] getParameters() {
		Graph g = graph;
		Collection<String> clusters = GraphHelper.getClusters(g.getGraphElements());
		ClusterColorAttribute cca = (ClusterColorAttribute) AttributeHelper.getAttributeValue(g,
				ClusterColorAttribute.attributeFolder, ClusterColorAttribute.attributeName,
				ClusterColorAttribute.getDefaultValue(clusters), new ClusterColorAttribute("resulttype"), false);

		// if (cca.getClusterColors() != null && cca.getClusterColors().size() >
		// clusters.size()) {
		// cca.trimColorSelection(clusters.size());
		// }

		// cca.ensureMinimumColorSelection(clusters.size());
		cca.updateClusterList(clusters);
		ClusterColorAttribute cca_new = new ClusterColorAttribute(ClusterColorAttribute.attributeName, cca.getString());
		ClusterColorParameter op = new ClusterColorParameter(cca_new, "Cluster-Colors", ClusterColorAttribute.desc);

		ArrayList<String> modeList = new ArrayList<String>();
		modeList.add(modeNode);
		modeList.add(modeSurr);
		ObjectListParameter modeParam = new ObjectListParameter(modeOfOperation, "Visualization Mode",
				"Use either the node fill color or a coloring of the node surrounding to visualize different clusters.",
				modeList);

		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			return new Parameter[] { op };
		else
			return new Parameter[] { op, modeParam };
	}

	@Override
	public void setParameters(Parameter[] params) {
		ClusterColorAttribute cca = (ClusterColorAttribute) ((ClusterColorParameter) params[0]).getValue();
		cca = (ClusterColorAttribute) cca.copy();
		if (graph.getAttributes().getCollection().containsKey(cca.getPath()))
			graph.removeAttribute(cca.getPath());
		graph.addAttribute(cca, ClusterColorAttribute.attributeFolder);
		if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR) {
			modeOfOperation = (String) ((ObjectListParameter) params[1]).getValue();
		}
	}

	@Override
	public String getCategory() {
		return "Network.Cluster";
	}

	@Override
	public String getDescription() {
		return "<html>" + "Please select the desired colors for the different subsets of graph elements,<br>"
				+ "depending on their cluster Id.<br>"
				+ "The first button row determines the fill color for nodes of the particular subset<br>"
				+ "and the line color for edges.<br>" + "The second row determines the border coloring for nodes.<br>"
				+ "If selected, edges with assigned cluster IDs are also colored.";
	}

	@Override
	public void execute() {
		Graph g = graph;
		try {
			Set<String> clusters = new TreeSet<String>();
			for (GraphElement ge : g.getGraphElements()) {
				String clusterId = NodeTools.getClusterID(ge, "");
				if (!clusterId.equals(""))
					clusters.add(clusterId);
			}

			g.getListenerManager().transactionStarted(this);
			ClusterColorAttribute cca = (ClusterColorAttribute) AttributeHelper.getAttributeValue(g,
					ClusterColorAttribute.attributeFolder, ClusterColorAttribute.attributeName,
					ClusterColorAttribute.getDefaultValue(clusters), new ClusterColorAttribute("resulttype"));

			if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR || modeOfOperation.equals(modeNode))
				executeClusterColoringOnGraph(g, clusters, cca);
			if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR && modeOfOperation.equals(modeSurr)) {
				AttributeHelper.setAttribute(graph, "", "background_coloring", Boolean.valueOf(true));
			}
			if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR && modeOfOperation.equals(modeNode)) {
				AttributeHelper.setAttribute(graph, "", "background_coloring", Boolean.valueOf(false));
			}

			Graph emptyGraph = new AdjListGraph();
			Graph clusterGraph = (Graph) AttributeHelper.getAttributeValue(g, "cluster", "clustergraph", emptyGraph,
					new AdjListGraph(), false);
			if (clusterGraph != emptyGraph) {
				executeClusterColoringOnGraph(clusterGraph, clusters, cca);
			}
		} finally {
			g.getListenerManager().transactionFinished(this);
		}
	}

	/**
	 * Colour clusters based on the stored {@linkplain ClusterColorAttribute} for
	 * the graph.
	 * 
	 * @param g graph {@linkplain Graph}
	 * @since 2.7.0
	 */
	public static void executeClusterColoringOnGraph(Graph g) {
		Collection<String> clusters = GraphHelper.getClusters(g.getGraphElements());
		ClusterColorAttribute cca = (ClusterColorAttribute) AttributeHelper.getAttributeValue(g,
				ClusterColorAttribute.attributeFolder, ClusterColorAttribute.attributeName,
				ClusterColorAttribute.getDefaultValue(clusters), new ClusterColorAttribute("resulttype"), false);

		executeClusterColoringOnGraph(g, cca);
	}

	public static void executeClusterColoringOnGraph(Graph g, ClusterColorAttribute cca) {
		Collection<String> clusters = GraphHelper.getClusters(g.getGraphElements());
		executeClusterColoringOnGraph(g, clusters, cca);
	}

	public static void executeClusterColoringOnGraph(Graph g, Collection<String> clusters, ClusterColorAttribute cca) {

		// cca.ensureMinimumColorSelection(clusters.size());
		cca.updateClusterList(clusters);
		String[] clusterValues = clusters.toArray(new String[0]);
		g.getListenerManager().transactionStarted(g);
		try {
			for (GraphElement ge : g.getGraphElements()) {
				String clusterId = NodeTools.getClusterID(ge, "");
				if (!clusterId.equals("")) {
					for (int i = 0; i < clusterValues.length; i++) {
						if (clusterValues[i].equals(clusterId)) {
							AttributeHelper.setFillColor(ge, cca.getClusterColor(i));
							if (ge instanceof Edge)
								AttributeHelper.setOutlineColor(ge, cca.getClusterColor(i));
							else
								AttributeHelper.setOutlineColor(ge, cca.getClusterOutlineColor(i));
						}
					}
				}
			}
		} finally {
			g.getListenerManager().transactionFinished(g);
		}
	}

	/**
	 * Re-colours graph elements to their default fill and outline colours.
	 * Colouring attribute values are preserved.
	 * 
	 * @param g
	 * @since 2.7.0
	 */
	public static void removeClusterColoringOnGraph(Graph g) {
		g.getListenerManager().transactionStarted(g);
		for (GraphElement ge : g.getGraphElements()) {
			AttributeHelper.setFillColor(ge, Color.WHITE);
			if (ge instanceof Edge)
				AttributeHelper.setOutlineColor(ge, Color.BLACK);
			else
				AttributeHelper.setOutlineColor(ge, Color.BLACK);
		}
		g.getListenerManager().transactionFinished(g);
	}

	@Override
	public boolean mayWorkOnMultipleGraphs() {
		return false; // would only work if all open graphs contain the same set of clusters
	}
}
