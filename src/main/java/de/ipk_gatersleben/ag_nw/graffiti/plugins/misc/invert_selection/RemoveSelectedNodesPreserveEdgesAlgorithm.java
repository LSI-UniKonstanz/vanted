/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * $Id$
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.AttributeHelper;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.FolderPanel;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.graphics.EdgeGraphicAttribute;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.AlgorithmWithComponentDescription;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.selection.Selection;
import org.graffiti.session.EditorSession;

/**
 * Labels all selected nodes with unique numbers. Does not touch existing
 * labels.
 */
public class RemoveSelectedNodesPreserveEdgesAlgorithm
		extends AbstractAlgorithm
		implements AlgorithmWithComponentDescription {
	
	Selection selection;
	
	private boolean ignoreDirection = true;
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#getParameters()
	 */
	@Override
	public Parameter[] getParameters() {
		return new Parameter[] { new BooleanParameter(ignoreDirection, "Preserve Connectivity",
				"Prevent connectivity loss because of edge-direction - ignore edge directions") };
	}
	
	public JComponent getDescriptionComponent() {
		ClassLoader cl = this.getClass().getClassLoader();
		String path = this.getClass().getPackage().getName().replace('.', '/');
		ImageIcon icon = new ImageIcon(cl.getResource(path + "/images/fold.png"));
		return FolderPanel.getBorderedComponent(new JLabel(icon), 5, 5, 5, 5);
	}
	
	@Override
	public void check() throws PreconditionException {
		super.check();
		if (selection == null) {
			EditorSession session = MainFrame.getInstance()
					.getActiveEditorSession();
			selection = session.getSelectionModel().getActiveSelection();
		}
		if (selection.getNodes().size() <= 0)
			throw new PreconditionException(
					"Please select a number of nodes which will be removed from the network.<br>The result is a <b>folded network</b>, edges are preserved.");
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm# setParameters(org.graffiti.plugin.algorithm.Parameter)
	 */
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		ignoreDirection = ((BooleanParameter) params[i++]).getBoolean();
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 */
	public void execute() {
		EditorSession session = MainFrame.getInstance()
				.getActiveEditorSession();
		if (selection == null)
			selection = session.getSelectionModel().getActiveSelection();
		
		graph.getListenerManager().transactionStarted(this);
		try {
			ArrayList<Node> workNodes = new ArrayList<Node>();
			workNodes.addAll(selection.getNodes());
			removeNodesPreserveEdges(workNodes, graph, ignoreDirection, null);
		} finally {
			graph.getListenerManager().transactionFinished(this);
		}
	}
	
	public static int removeNodesPreserveEdges(ArrayList<Node> workNodes,
			Graph graph, boolean ignoreDirection,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus) {
		Stack<Node> toBeDeleted = new Stack<Node>();
		int workCount = 0;
		toBeDeleted.addAll(workNodes);
		workCount = workNodes.size();
		int selfLoops = 0;
		int edgeCopies = 0;
		int i = 0;
		for (Node n : workNodes) {
			i++;
			// process undirected edges as follows:
			// all nodes that have a undirected connection to the node to be
			// deleted
			// will be connected by a new undirected edge to all neighbours of
			// the
			// node to be deleted
			if (ignoreDirection) {
				if (graph.isUndirected()) {
					for (Edge e : n.getAllOutEdges()) {
						e.setDirected(false);
						AttributeHelper.setArrowhead(e, false);
						AttributeHelper.setArrowtail(e, false);
					}
					for (Edge undirEdge : n.getUndirectedEdges()) {
						Node srcN = undirEdge.getSource();
						for (Node targetN : n.getUndirectedNeighbors()) {
							if (srcN != targetN) {
								if (!srcN.getNeighbors().contains(targetN)) {
									graph.addEdgeCopy(undirEdge, srcN, targetN);
									edgeCopies++;
								}
							}
						}
						srcN = undirEdge.getTarget();
						for (Node targetN : n.getUndirectedNeighbors()) {
							if (srcN != targetN) {
								if (!srcN.getNeighbors().contains(targetN)) {
									graph.addEdgeCopy(undirEdge, srcN, targetN);
									edgeCopies++;
								}
							}
						}
					}
				}
			} else {
				removeNodes(workNodes);
			}
			/*
			{
			
			
				// process undirected edges as follows:
				// all incoming neighbours need to be handled as follows:
				// each incoming neighbour needs to be connected to all outgoing
				// neighbours
				// of the worknode
				// the combination of undirected and directed edges around a node is
				// not specially treated and is ignored
				for (Edge incEdge : n.getDirectedInEdges()) {d
					
					Node srcN = incEdge.getSource();
					for (Node targetN : n.getOutNeighbors()) {
						// if (srcN != targetN) {
						
						if (!ignoreDirection && !srcN.getOutNeighbors().contains(targetN)) {
							graph.addEdgeCopy(incEdge, srcN, targetN);
							edgeCopies++;
						} else
							if (ignoreDirection && !srcN.getOutNeighbors().contains(targetN) && !srcN.getInNeighbors().contains(targetN)) {
								Edge addEdgeCopy = graph.addEdgeCopy(incEdge, srcN, targetN);
								AttributeHelper.setArrowhead(addEdgeCopy, true);
								AttributeHelper.setArrowtail(addEdgeCopy, true);
								edgeCopies++;
							} else {
								
							}
						if (srcN == targetN)
							selfLoops++;
						// }
					}
					if (ignoreDirection) {
						for (Node targetN : n.getInNeighbors()) {
							if (srcN != targetN) {
								if (!srcN.getNeighbors().contains(targetN)) {
									Edge ne = graph.addEdgeCopy(incEdge, srcN, targetN);
									edgeCopies++;
									ne.setDirected(false);
									AttributeHelper.setArrowhead(ne, false);
									AttributeHelper.setArrowtail(ne, false);
								}
							}
						}
					}
				}
			}
			*/
			
			if (optStatus != null) {
				if (selfLoops <= 0)
					optStatus.setCurrentStatusText2("Created " + edgeCopies + " edge copies");
				else
					optStatus.setCurrentStatusText2("Created " + edgeCopies + " edge copies (" + selfLoops + " self-loops)");
				optStatus.setCurrentStatusValueFine(50d * ((double) i / workCount));
			}
			if (optStatus != null && optStatus.wantsToStop())
				break;
		}
		int delCnt = 0;
		if (optStatus == null || !optStatus.wantsToStop())
			while (!toBeDeleted.empty()) {
				Node n = toBeDeleted.pop();
				if (n.getGraph() != null) {
					graph.deleteNode(n);
					delCnt++;
					if (optStatus != null)
						optStatus.setCurrentStatusText2("Removed " + delCnt + "/" + workCount + " nodes");
				}
				if (optStatus != null)
					optStatus.setCurrentStatusValueFine(50d + 50d * ((double) delCnt / workCount));
				if (optStatus != null && optStatus.wantsToStop())
					break;
			}
		if (selfLoops <= 0)
			MainFrame.showMessage("Removed " + workCount + "/" + workCount + " nodes.", MessageType.INFO);
		else
			MainFrame.showMessage("Removed " + workCount + "/" + workCount + " nodes, created " + selfLoops + " self-loop edge(s)!", MessageType.INFO);
		if (optStatus != null)
			optStatus.setCurrentStatusValue(100);
		return workCount;
	}
	
	private enum EdgeType {
		IN_EDGE, //normal incoming edge
		IN_EDGE_OUT, //incoming edge but with tail-arrow and no head-arrow
		IN_EDGE_BI, //incoming edge with head and tail arrow (bidirectional edge)
		OUT_EDGE,
		OUT_EDGE_IN,
		OUT_EDGE_BI
	}
	
	/**
	 * Deletes worknodes and connects the nodes connected to the worknodes.
	 * It will also keep the overall edge connection as well as the
	 * head/tail arrow setting
	 * 
	 * @param workNodes
	 */
	private static void removeNodes(ArrayList<Node> workNodes) {
		
		Map<Node, Map<Node, Set<EdgeType>>> mapInEdgeOutEdgeToFoldedEdge = new HashMap<Node, Map<Node, Set<EdgeType>>>();
		
		EdgeType edge1Type;
		EdgeType edge2Type;
		
		Set<Edge> setRemoveEdges = new HashSet<Edge>();
		for (Node curWorkNode : workNodes) {
			Edge[] edges = curWorkNode.getEdges().toArray(new Edge[curWorkNode.getEdges().size()]);
			
			for (int first = 0; first < edges.length - 1; first++) {
				Edge edge1 = edges[first];
				setRemoveEdges.add(edge1);
				
				for (int second = first + 1; second < edges.length; second++) {
					Edge edge2 = edges[second];
					
					setRemoveEdges.add(edge2);
					// don't check an edge with itself
					if (edge1.equals(edge2))
						continue;
					
					// get the nodes, that will be connected by the folded edge
					Node sourceNode;
					Node targetNode;
					
					edge1Type = testEdgeType(curWorkNode, edge1);
					edge2Type = testEdgeType(curWorkNode, edge2);
					
					assert (edge1Type != null);
					assert (edge2Type != null);
					
					//depending of the edge type, the source/target-node is set from edge-source or edge-target
					switch (edge1Type) {
						case IN_EDGE:
						case IN_EDGE_BI:
						case IN_EDGE_OUT:
							sourceNode = edge1.getSource();
							break;
						default:
							sourceNode = edge1.getTarget();
					}
					switch (edge2Type) {
						case IN_EDGE:
						case IN_EDGE_BI:
						case IN_EDGE_OUT:
							targetNode = edge2.getSource();
							break;
						default:
							targetNode = edge2.getTarget();
					}
					
					EdgeType resultEdge = getResultEdge(edge1Type, edge2Type);
					
					if (resultEdge != null) {
						Map<Node, Set<EdgeType>> mapTargetEdgeType = null;
						if ((mapTargetEdgeType = mapInEdgeOutEdgeToFoldedEdge.get(sourceNode)) == null) {
							mapInEdgeOutEdgeToFoldedEdge.put(sourceNode, new HashMap<Node, Set<EdgeType>>());
							mapTargetEdgeType = mapInEdgeOutEdgeToFoldedEdge.get(sourceNode);
						}
						Set<EdgeType> setEdgeType;
						if ((setEdgeType = mapTargetEdgeType.get(targetNode)) == null) {
							mapTargetEdgeType.put(targetNode, new HashSet<EdgeType>());
							setEdgeType = mapTargetEdgeType.get(targetNode);
						}
						boolean allreadyThere = false;
						for (EdgeType type : setEdgeType) {
							if (type == resultEdge)
								allreadyThere = true;
						}
						if (!allreadyThere)
							setEdgeType.add(resultEdge);
					}
				}
			}
		}
		
		for (Edge edge : setRemoveEdges)
			edge.getGraph().deleteEdge(edge);
		
		for (Node sourceNode : mapInEdgeOutEdgeToFoldedEdge.keySet()) {
			Map<Node, Set<EdgeType>> mapTargetEdgeType = mapInEdgeOutEdgeToFoldedEdge.get(sourceNode);
			for (Node targetNode : mapTargetEdgeType.keySet()) {
				Set<EdgeType> set = mapTargetEdgeType.get(targetNode);
				for (EdgeType newEdgeType : set) {
					Graph g = sourceNode.getGraph();
					
					Edge foldedEdge;
					
					switch (newEdgeType) {
						case IN_EDGE:
							
							foldedEdge = g.addEdge(sourceNode, targetNode, true, AttributeHelper.getDefaultGraphicsAttributeForEdge(Color.BLACK, Color.BLACK, true));
							break;
						case IN_EDGE_BI:
							foldedEdge = g.addEdge(sourceNode, targetNode, true, AttributeHelper.getDefaultGraphicsAttributeForEdge(Color.BLACK, Color.BLACK, true));
							AttributeHelper.setArrowtail(foldedEdge, true);
							break;
						case IN_EDGE_OUT:
							foldedEdge = g.addEdge(sourceNode, targetNode, true, AttributeHelper.getDefaultGraphicsAttributeForEdge(Color.BLACK, Color.BLACK, true));
							AttributeHelper.setArrowtail(foldedEdge, true);
							AttributeHelper.setArrowhead(foldedEdge, false);
							break;
						
						case OUT_EDGE:
							foldedEdge = g.addEdge(targetNode, sourceNode, true, AttributeHelper.getDefaultGraphicsAttributeForEdge(Color.BLACK, Color.BLACK, true));
							break;
						case OUT_EDGE_BI:
							foldedEdge = g.addEdge(targetNode, sourceNode, true, AttributeHelper.getDefaultGraphicsAttributeForEdge(Color.BLACK, Color.BLACK, true));
							AttributeHelper.setArrowtail(foldedEdge, true);
							break;
						case OUT_EDGE_IN:
							foldedEdge = g.addEdge(targetNode, sourceNode, true, AttributeHelper.getDefaultGraphicsAttributeForEdge(Color.BLACK, Color.BLACK, true));
							AttributeHelper.setArrowtail(foldedEdge, true);
							AttributeHelper.setArrowhead(foldedEdge, false);
							break;
					}
				}
				
			}
		}
		
	}
	
	/**
	 * returns the edge type depending on the given node (the node, that
	 * will be deleted (see above)) and the edge to test
	 * 
	 * @param n
	 * @param e
	 * @return
	 */
	private static EdgeType testEdgeType(Node n, Edge e) {
		// test for IN_EDGE (incoming edge)
		EdgeGraphicAttribute ega = (EdgeGraphicAttribute) e.getAttribute(GraphicAttributeConstants.GRAPHICS);
		boolean hasArrowhead = !ega.getArrowhead().isEmpty();
		boolean hasArrowtail = !ega.getArrowtail().isEmpty();
		if (e.getTarget().equals(n)) {
			if (hasArrowhead && !hasArrowtail)
				return EdgeType.IN_EDGE;
			else if (!hasArrowhead && hasArrowtail)
				return EdgeType.IN_EDGE_OUT;
			else if (hasArrowhead && hasArrowtail)
				return EdgeType.IN_EDGE_BI;
		} else {
			// this seems to be an outgoing edge
			if (hasArrowhead && !hasArrowtail)
				return EdgeType.OUT_EDGE;
			else if (!hasArrowhead && hasArrowtail)
				return EdgeType.OUT_EDGE_IN;
			else if (hasArrowhead && hasArrowtail)
				return EdgeType.OUT_EDGE_BI;
		}
		
		return null;
	}
	
	/**
	 * returns the EdgeType for the folded edge
	 * the direction of the edge is with regard to the new target node
	 * that is the node on edge2 opposite to the node that is going to
	 * be removed
	 * 
	 * @param edge1
	 * @param edge2
	 * @return type of new folded edge or null if the edges cannot be folded
	 */
	private static EdgeType getResultEdge(EdgeType edge1, EdgeType edge2) {
		// check combinations for an IN_EDGE
		if (edge1 == EdgeType.IN_EDGE || edge1 == EdgeType.OUT_EDGE_IN) {
			if (edge2 == EdgeType.OUT_EDGE
					|| edge2 == EdgeType.OUT_EDGE_BI
					|| edge2 == EdgeType.IN_EDGE_BI
					|| edge2 == EdgeType.IN_EDGE_OUT) {
				
				if (edge1 == EdgeType.IN_EDGE)
					return EdgeType.IN_EDGE;
				else {
					return EdgeType.OUT_EDGE_IN;
				}
			}
		}
		
		//check combinations for an OUT_EDGE
		else if (edge1 == EdgeType.OUT_EDGE || edge1 == EdgeType.IN_EDGE_OUT) {
			if (edge2 == EdgeType.IN_EDGE
					|| edge2 == EdgeType.OUT_EDGE_IN
					|| edge2 == EdgeType.IN_EDGE_BI
					|| edge2 == EdgeType.OUT_EDGE_BI) {
				
				if (edge1 == EdgeType.OUT_EDGE)
					return EdgeType.OUT_EDGE;
				else
					return EdgeType.IN_EDGE_OUT;
			}
		}
		
		//check combinations for BI edges with primary IN
		else if (edge1 == EdgeType.IN_EDGE_BI && edge2 == EdgeType.OUT_EDGE_BI)
			return EdgeType.IN_EDGE_BI;
		
		//check combinations for BI edges with primary OUT
		else if (edge1 == EdgeType.OUT_EDGE_BI && edge2 == EdgeType.IN_EDGE_BI)
			return EdgeType.OUT_EDGE_BI;
		
		else if (edge1 == EdgeType.IN_EDGE_BI && edge2 == EdgeType.IN_EDGE_BI)
			return EdgeType.IN_EDGE_BI;
		
		else if (edge1 == EdgeType.OUT_EDGE_BI && edge2 == EdgeType.OUT_EDGE_BI)
			return EdgeType.OUT_EDGE_BI;
		
		return null;
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#reset()
	 */
	@Override
	public void reset() {
		graph = null;
		selection = null;
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		return "Remove Connecting Nodes";
	}
	
	@Override
	public String getCategory() {
		return "Network.Nodes";
	}
	
	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(Arrays.asList(
				Category.NODE,
				Category.COMPUTATION
				));
	}
	
	@Override
	public String getDescription() {
		return "<html>" +
				"With this command you can remove nodes, that do connect<br/>"
				+ "other nodes inbetween without loosing the overall connectivity<br/>"
				+ "of the network. The selected nodes (round nodes<br>" +
				"in the example) are removed from a network. <br>" +
				"The connectivity of the resulting network is influenced<br>" +
				"by the corresponding setting as shown in the image.";
	}
	
	/**
	 * Sets the selection on which the algorithm works.
	 * 
	 * @param selection
	 *           the selection
	 */
	public void setSelection(Selection selection) {
		this.selection = selection;
	}
	
	@Override
	public boolean mayWorkOnMultipleGraphs() {
		return true;
	}
}
