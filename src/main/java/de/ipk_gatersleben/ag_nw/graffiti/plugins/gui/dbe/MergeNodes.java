/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 17.01.2005 by Christian Klukas
 * (c) 2005 IPK Gatersleben, Group Network Analysis
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.KeyStroke;

import org.AttributeHelper;
import org.ErrorMsg;
import org.Vector2d;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelperBio;
import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.algorithms.RemoveMappingDataAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes.Experiment2GraphHelper;

/**
 * Merges nodes within the same network, while considering connecting edges and
 * data mapping information.
 * 
 * @author Christian Klukas (c) 2005 IPK Gatersleben, Group Network Analysis
 * @version 2.6.5
 * @vanted.revision 2.6.5
 */
public class MergeNodes extends AbstractAlgorithm {
	
	private boolean considerSameLabel;
	private boolean considerCluster;
	
	public String getName() {
		return "Merge Nodes";
	}
	
	@Override
	public String getCategory() {
		return "Network.Nodes";
	}
	
	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(Arrays.asList(Category.GRAPH, Category.COMPUTATION));
	}
	
	@Override
	public String getDescription() {
		return "<html><p>Merges nodes together, while edges and data-mapping information<br>"
				+ "are taken into account. To merge internetwork nodes, please, use<br/>"
				+ "Window &#10148; Combine Open Networks beforehand.</p><br/></html>";
	}
	
	@Override
	public KeyStroke getAcceleratorKeyStroke() {
		return KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK);
	}
	
	@Override
	public Parameter[] getParameters() {
		Parameter[] params = new Parameter[] {
				new BooleanParameter(true, "Only with same label", "Consider same label when merging nodes."),
				new BooleanParameter(false, "Consider Cluster", "Consider cluster inforamtion when merging nodes.") };
		
		return params;
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		if (params == null) {
			considerSameLabel = true;
			considerCluster = false;
		} else {
			int i = 0;
			considerSameLabel = ((BooleanParameter) params[i++]).getBoolean();
			considerCluster = ((BooleanParameter) params[i++]).getBoolean();
		}
	}
	
	public void execute() {
		List<Node> workNodes = new ArrayList<Node>(getSelectedOrAllNodes());
		graph.getListenerManager().transactionStarted(this);
		
		if (considerSameLabel) {
			GraphHelperBio.mergeNodesWithSameLabel(workNodes, false, false, considerCluster);
		} else {
			mergeNodesIntoSingleNode(graph, workNodes);
		}
		
		graph.getListenerManager().transactionFinished(this);
	}
	
	public static void mergeNodesIntoSingleNode(Graph graph, Collection<Node> workNodes) {
		Vector2d center = NodeTools.getCenter(workNodes);
		List<SubstanceInterface> targetMappingList = new ArrayList<SubstanceInterface>();
		boolean firstNode = true;
		Node mergedNode = null;
		double targetArea = 0;
		double sumWidth = 0;
		double sumHeight = 0;
		String targetNodeName = "";
		HashMap<SubstanceInterface, String> mapping2chartTitle = new HashMap<SubstanceInterface, String>();
		for (Node workNode : workNodes) {
			if (targetNodeName.length() > 0) {
				targetNodeName += ", " + AttributeHelper.getLabel(workNode, "[unnamed]");
			} else {
				targetNodeName = AttributeHelper.getLabel(workNode, "[unnamed]");
			}
			
			if (firstNode) {
				firstNode = false;
				mergedNode = mergeNode(graph, workNodes, center, true);
			}
			
			Vector2d size = AttributeHelper.getSize(workNode);
			targetArea += size.x * size.x + size.y * size.y;
			sumWidth += size.x;
			sumHeight += size.y;
			
			extractDataMappingInformation(targetMappingList, mapping2chartTitle, workNode);
			
			if (mergedNode != workNode)
				graph.deleteNode(workNode);
		}
		Vector2d tsize = new Vector2d(0d, 0d);
		tsize.x = Math.sqrt(targetArea) * sumWidth / sumHeight;
		tsize.y = Math.sqrt(targetArea) * sumHeight / sumWidth;
		AttributeHelper.setLabel(mergedNode, targetNodeName);
		
		applyDataMappingInformation(targetMappingList, mergedNode, mapping2chartTitle);
	}
	
	private static void applyDataMappingInformation(List<SubstanceInterface> targetMappingList, Node node,
			HashMap<SubstanceInterface, String> mapping2chartTitle) {
		RemoveMappingDataAlgorithm.removeMappingDataFrom(node);
		int idx = 0;
		HashSet<String> titles = new HashSet<String>();
		for (SubstanceInterface mappData : targetMappingList) {
			String s = mapping2chartTitle.get(mappData);
			if (s != null && s.length() > 0)
				titles.add(s);
		}
		for (SubstanceInterface mappData : targetMappingList) {
			Experiment2GraphHelper.addMappingData2Node(mappData, node);
			if (titles.size() > 1)
				AttributeHelper.setAttribute(node, "charting", "chartTitle" + (++idx),
						mapping2chartTitle.get(mappData));
		}
	}
	
	private static void extractDataMappingInformation(List<SubstanceInterface> targetMappingList,
			HashMap<SubstanceInterface, String> mapping2chartTitle, Node node) {
		Iterable<SubstanceInterface> mappingList = Experiment2GraphHelper.getMappedDataListFromGraphElement(node);
		if (mappingList != null) {
			int idx = 0;
			for (SubstanceInterface mapping : mappingList) {
				String chartTitle = (String) AttributeHelper.getAttributeValue(node, "charting", "chartTitle" + (++idx),
						"", "");
				if (chartTitle == null || chartTitle.length() <= 0) {
					chartTitle = AttributeHelper.getLabel(node, null);
				} else {
					chartTitle = AttributeHelper.getLabel(node, null) + ": " + chartTitle;
				}
				mapping2chartTitle.put(mapping, chartTitle);
			}
			for (SubstanceInterface m : mappingList)
				targetMappingList.add(m);
		}
	}
	
	public static Node mergeNode(Graph graph, Collection<Node> toBeMerged, Vector2d center, boolean retainClusterIDs) {
		Node mergedNode;
		mergedNode = graph.addNodeCopy(toBeMerged.iterator().next());
		if (mergedNode == null || mergedNode.getGraph() == null) {
			ErrorMsg.addErrorMessage("Merge Operation Error: Could not add node-copy.");
			return null;
		}
		List<SubstanceInterface> targetMappingList = new ArrayList<SubstanceInterface>();
		HashMap<SubstanceInterface, String> mapping2chartTitle = new HashMap<SubstanceInterface, String>();
		HashSet<String> clusterIDs = new HashSet<String>();
		AttributeHelper.setPosition(mergedNode, center);
		for (Node checkNode : toBeMerged) {
			if (retainClusterIDs) {
				String clusterID = NodeTools.getClusterID(checkNode, "");
				clusterIDs.add(clusterID);
			}
			extractDataMappingInformation(targetMappingList, mapping2chartTitle, checkNode);
			for (Edge undirEdge : checkNode.getUndirectedEdges()) {
				if (undirEdge.getSource() == checkNode
						&& !mergedNode.getUndirectedNeighbors().contains(undirEdge.getTarget())) {
					if (undirEdge.getTarget() != checkNode)
						graph.addEdgeCopy(undirEdge, mergedNode, undirEdge.getTarget());
					else
						graph.addEdgeCopy(undirEdge, mergedNode, mergedNode);
				}
				if (undirEdge.getTarget() == checkNode
						&& !mergedNode.getUndirectedNeighbors().contains(undirEdge.getSource())) {
					if (undirEdge.getSource() != checkNode)
						graph.addEdgeCopy(undirEdge, undirEdge.getSource(), mergedNode);
					else
						graph.addEdgeCopy(undirEdge, mergedNode, mergedNode);
				}
			}
			for (Edge inEdge : checkNode.getAllInEdges()) {
				if (!mergedNode.getInNeighbors().contains(inEdge.getSource())) {
					graph.addEdgeCopy(inEdge, inEdge.getSource(), mergedNode);
				}
			}
			for (Edge outEdge : checkNode.getAllOutEdges()) {
				if (!mergedNode.getOutNeighbors().contains(outEdge.getTarget())) {
					graph.addEdgeCopy(outEdge, mergedNode, outEdge.getTarget());
				}
			}
			
			for (Node undirNode : checkNode.getUndirectedNeighbors())
				if (!mergedNode.getUndirectedNeighbors().contains(undirNode)) {
					graph.addEdge(mergedNode, undirNode, false);
				}
		}
		if (retainClusterIDs && clusterIDs.size() > 1)
			NodeTools.setClusterID(mergedNode, AttributeHelper.getStringList(clusterIDs, ";"));
		if (targetMappingList.size() > 0)
			applyDataMappingInformation(targetMappingList, mergedNode, mapping2chartTitle);
		return mergedNode;
	}
}
