/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 17.01.2005 by Christian Klukas
 * (c) 2005 IPK Gatersleben, Group Network Analysis
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.database_processing.go_cluster_histogram;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Category;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.chart_settings.GraffitiCharts;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;

/**
 * @author Christian Klukas (c) 2006 IPK Gatersleben, Group Network Analysis
 */
public class CreateDirectChildrenClustersHistogramAlgorithm extends AbstractAlgorithm {
	
	public String getName() {
		return "Add Neighbour-Node Cluster-Histogram";
	}
	
	@Override
	public String getCategory() {
		return "Network.Hierarchy";
	}
	
	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(
				Arrays.asList(Category.GRAPH, Category.ENRICHMENT, Category.COMPUTATION, Category.HIDDEN));
	}
	
	@Override
	public String getDescription() {
		return "<html>" + "This command enumerates the neighbours of all or the selected nodes with mapping data.<br>"
				+ "A histogram of the number of occurrences of a specific cluster ID is created. A data mapping<br>"
				+ "representing this data is created and shown as a bar-chart.<br><br>"
				+ "A similar command which processes all leaf nodes in a hierarchy is available from the<br>"
				+ "&quot;Hierarchy&quot; menu.";
	}
	
	public void execute() {
		try {
			graph.getListenerManager().transactionStarted(this);
			Collection<Node> workingSet = getSelectedOrAllNodes();
			TreeSet<String> knownClusterIDs = new TreeSet<String>();
			HashSet<Node> processedNodes = new HashSet<Node>();
			for (Node n : workingSet) {
				knownClusterIDs.addAll(getChildNodeClusterIDs(processedNodes, new NodeHelper(n), knownClusterIDs));
			}
			for (Node n : workingSet) {
				NodeHelper nh = new NodeHelper(n);
				processNode(nh, knownClusterIDs);
			}
		} finally {
			graph.getListenerManager().transactionFinished(this);
		}
	}
	
	private static void processNode(NodeHelper nh, TreeSet<String> knownClusterIDs) {
		Collection<Node> workNodes = nh.getNeighbors();
		TreeMap<String, Integer> cluster2frequency = new TreeMap<String, Integer>();
		for (String ci : knownClusterIDs)
			cluster2frequency.put(ci, Integer.valueOf(0));
		
		for (Node n : workNodes) {
			NodeHelper nnh = new NodeHelper(n);
			if (nnh.hasDataMapping()) {
				String clusterID = nnh.getClusterID(null);
				if (clusterID != null) {
					Integer value = cluster2frequency.get(clusterID);
					if (value == null)
						value = Integer.valueOf(0);
					cluster2frequency.put(clusterID, Integer.valueOf(value + 1));
				}
			}
		}
		// create dataset
		String substanceName = nh.getLabel();
		if (substanceName == null || substanceName.length() <= 0)
			substanceName = "Node " + nh.getID();
		nh.removeDataMapping();
		for (String clusterID : cluster2frequency.keySet()) {
			int plantID = nh.memGetPlantID(clusterID, "", "", "", "");
			Integer value = cluster2frequency.get(clusterID);
			if (value == null)
				nh.memSample(Double.valueOf(0), -1, plantID, "frequency", "-1", Integer.valueOf(-1));
			else
				nh.memSample(Double.valueOf(value), -1, plantID, "frequency", "-1", Integer.valueOf(-1));
		}
		nh.memAddDataMapping(substanceName, "cluster frequency", null, "calculated analysis", "system",
				"Frequency of clusters in neighbour nodes", "");
		nh.setChartType(GraffitiCharts.BAR);
	}
	
	private static Collection<String> getChildNodeClusterIDs(HashSet<Node> processedNodes, NodeHelper nh,
			Collection<String> knownClusterIDs) {
		Collection<String> result = new ArrayList<String>();
		if (!processedNodes.contains(nh.getGraphNode())) {
			processedNodes.add(nh.getGraphNode());
			Collection<Node> workNodes = nh.getNeighbors();
			for (Node n : workNodes) {
				NodeHelper nnh = new NodeHelper(n);
				if (nnh.hasDataMapping()) {
					String clusterID = nnh.getClusterID(null);
					if (clusterID != null)
						result.add(clusterID);
				}
			}
		}
		return result;
	}
}
