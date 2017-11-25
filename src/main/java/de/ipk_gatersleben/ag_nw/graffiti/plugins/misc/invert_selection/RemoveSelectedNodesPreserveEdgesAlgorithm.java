/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * $Id$
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.KeyStroke;

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

import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands.IntroduceParallelEdgeBends;

/**
 * Labels all selected nodes with unique numbers. Does not touch existing
 * labels.
 */
public class RemoveSelectedNodesPreserveEdgesAlgorithm
		extends AbstractAlgorithm
		implements AlgorithmWithComponentDescription {
	
	
	private static boolean ignoreDirection = false;
	
	private boolean layoutParallelEdges = true;
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#getParameters()
	 */
	@Override
	public Parameter[] getParameters() {
		return new Parameter[] { new BooleanParameter(ignoreDirection, "Ignore Edge Direction",
				"<html>Prevent any connectivity loss, execute <em>Preserving</em> scenario."),
				new BooleanParameter(layoutParallelEdges, "Layout Parallel Edges",
						"In case multiple types of edges are created between two nodes, edge bends are introduced.") };
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
		layoutParallelEdges = ((BooleanParameter) params[i++]).getBoolean();
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
			//chains are handled specially
			boolean chaining = isChain(workNodes);
			boolean reset = !ignoreDirection;
			if (chaining)
				ignoreDirection = !ignoreDirection;
			removeNodesPreserveEdges(workNodes, graph, ignoreDirection, layoutParallelEdges, null);
			if (chaining && reset)
				ignoreDirection = !ignoreDirection;
				
		} finally {
			graph.getListenerManager().transactionFinished(this);
		}
	}
	
	private boolean isChain(ArrayList<Node> selection) {
		HashMap<Node, Node> pairs = new HashMap<>();
		int actualPairs = 0; //because put() replaces some
		for (Node node : selection) {
			for (Node nn : selection) {
				if (node.equals(nn))
					continue;
				
				if (node.getNeighbors().contains(nn)) {
					if (pairs.containsKey(node)) {
						actualPairs++;						
						pairs.put(nn, node);
					} else {
						actualPairs++;
						pairs.put(node, nn);
					}
				}
			}
		}
		if (actualPairs == (selection.size() - 1) * 2)
			return true;
		
		return false;

	}
	
	public static int removeNodesPreserveEdges(ArrayList<Node> workNodes,
			Graph graph, boolean ignoreDirection, boolean layoutParallelEdges,
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
			/**
			 * Process undirected edges as follows:
			 * all nodes that have an undirected connection to the node, which
			 * are to be deleted, will be connected by a new undirected edge
			 * to all neighbours of the node, which is to be deleted.
			 */		
			if (graph.isUndirected()) {
				for (Edge e : n.getAllOutEdges())
					undirectEdge(e);
				
				for (Edge undirEdge : n.getUndirectedEdges()) {
					Node srcN = undirEdge.getSource();
					for (Node targetN : n.getUndirectedNeighbors())
						if (srcN != targetN)
							if (!srcN.getNeighbors().contains(targetN)) {
								graph.addEdgeCopy(undirEdge, srcN, targetN);
								edgeCopies++;
							}


					srcN = undirEdge.getTarget();
					for (Node targetN : n.getUndirectedNeighbors())
						if (srcN != targetN)
							if (!srcN.getNeighbors().contains(targetN)) {
								graph.addEdgeCopy(undirEdge, srcN, targetN);
								edgeCopies++;
							}
				}
			} else {
				removeNodes(workNodes, layoutParallelEdges);
			}
			
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
	
	/**
	 * Deletes selected nodes (workNodes) and connects their adjacent nodes.
	 * It will also keep the overall edge connection as well as the
	 * head/tail arrow setting.
	 * 
	 * @param workNodes
	 */
	private static void removeNodes(ArrayList<Node> workNodes, boolean layoutParallelEdges) {

		Set<Edge> setRemoveEdges = new HashSet<Edge>();
		Set<Edge> newEdges = new HashSet<Edge>();
		
		for (Node curWorkNode : workNodes) {
			Edge[] edges = curWorkNode.getEdges().toArray(new Edge[curWorkNode.getEdges().size()]);
			
			for (int first = 0; first < edges.length - 1; first++) {
				Edge edge1 = edges[first];

				setRemoveEdges.add(edge1);
				
				for (int second = first + 1; second < edges.length; second++) {
					Edge edge2 = edges[second];
					
					// don't check an edge with itself
					if (edge1.equals(edge2))
						continue;

					setRemoveEdges.add(edge2);
					
					Node sourceNode = null;
					Node targetNode = null;
										
					// get the nodes, that will be connected by the folded edge
					Node[] neighbours = getSourceAndTarget(curWorkNode, edge1, edge2);
					if (neighbours != null) {
						sourceNode = neighbours[0];
						targetNode = neighbours[1];		
					}

					//undirected pair of edges in directed graph
					if (sourceNode == null || targetNode == null) {
						Graph g = curWorkNode.getGraph();
						Node source = (!edge1.getSource().equals(curWorkNode)) ? edge1.getSource() : edge1.getTarget();
						Node target = (!edge2.getSource().equals(curWorkNode)) ? edge2.getSource() : edge2.getTarget();
						
						if (!source.getNeighbors().contains(target)) {
							Edge foldedEdge = g.addEdgeCopy(edge1, source, target); //both undirected (or direction ignored)
							undirectEdge(foldedEdge);
							newEdges.add(foldedEdge);
						}
					} else
						//mixed types of edges
						if (!sourceNode.getNeighbors().contains(targetNode)) {
							Graph g = sourceNode.getGraph();
							Edge foldedEdge = g.addEdge(sourceNode, targetNode, true, AttributeHelper.getDefaultGraphicsAttributeForEdge(Color.BLACK, Color.BLACK, true));
							newEdges.add(foldedEdge);
						}
				}
			}
		}
		
		for (Edge edge : setRemoveEdges)
			edge.getGraph().deleteEdge(edge);

		if (layoutParallelEdges && !newEdges.isEmpty()) {
			IntroduceParallelEdgeBends edgeBendsAlgo = new IntroduceParallelEdgeBends();
			edgeBendsAlgo.attach(workNodes.get(0).getGraph(), new Selection(newEdges));
			edgeBendsAlgo.execute();
		}
	}
	
	/**
	 * Interface method for 
	 * {@link RemoveSelectedNodesPreserveEdgesAlgorithm#getNeightbouringNodesOf(Node, Edge, Edge)}.
	 * 
	 * @param curWorkNode the node, whose neighbours are gotten
	 * @param edge1 first incident edge
	 * @param edge2 second incident edge
	 * 
	 * @return a 2-element array containing the new edge's {source, target}
	 */
	private static Node[] getSourceAndTarget(Node curWorkNode, Edge edge1, Edge edge2) {		
		boolean isEdge_1_Directed = 
				//semantically
				edge1.isDirected() && 
				//and visually
				!(AttributeHelper.getArrowhead(edge1).equals("") && AttributeHelper.getArrowtail(edge1).equals(""));
		boolean isEdge_2_Directed = 
				//semantically
				edge2.isDirected() &&
				//and visually
				!(AttributeHelper.getArrowhead(edge2).equals("") && AttributeHelper.getArrowtail(edge2).equals(""));
		
		if (isEdge_1_Directed && isEdge_2_Directed) {
			/*
			 * If in Preserving scenario (ignoreDirection is true) treat both as undirected (s. method's last else),
			 * otherwise set for deletion. */
			if (edge1.getSource().equals(curWorkNode) && edge2.getSource().equals(curWorkNode)) // <-- * --> 
				return ignoreDirection ? null : new Node[] {curWorkNode, curWorkNode};
			else if (edge1.getTarget().equals(curWorkNode) && edge2.getTarget().equals(curWorkNode)) // --> * <--
				return ignoreDirection ? null : new Node[] {curWorkNode, curWorkNode};
		}
		
		/* When both are directed and there is no change in direction,
		 * otherwise take only the directed one as direction basis. */
		if (isEdge_1_Directed)
				return getNeightbouringNodesOf(curWorkNode, edge1, edge2);		
		else if (isEdge_2_Directed)
				return getNeightbouringNodesOf(curWorkNode, edge2, edge1);				
		else //both undirected in generally directed graph, handled accordingly later
			return null;
	}
	
	/**
	 * Below we consider only the non-preserving scenario (ignoreDirection is false).
	 * 
	 * It handles directed as well as directed-undirected edge pairs. A mixed pair
	 * of edges is turned into one directed edge, whose new direction is set to be
	 * the same, though setting of source and target nodes, as the directed edge of
	 * the mixed pair.  
	 * 
	 * @param curWorkNode the node, whose neighbours are gotten
	 * @param edge1 first incident edge
	 * @param edge2 second incident edge
	 * 
	 * @return a 2-element array containing the new edge's {source, target}
	 */
	private static Node[] getNeightbouringNodesOf(Node curWorkNode, Edge edge1, Edge edge2) {
		Node sourceNode = null, targetNode = null;
		
		//determine edge1's other incident node role
		if (edge1.getSource().equals(curWorkNode))
			targetNode = edge1.getTarget();
		else if (edge1.getTarget().equals(curWorkNode))
			sourceNode = edge1.getSource();

		//based on edge1's other incident node, set edge2's other incident node 
		if (targetNode == null)
			targetNode = edge2.getTarget();
		else
			sourceNode = edge2.getSource();
		
		//assure we didn't grab the curWorkNode (directed & undirected edges mixed case)
		if (sourceNode.equals(curWorkNode))
			sourceNode = edge2.getTarget();
		else if (targetNode.equals(curWorkNode))
			targetNode = edge2.getSource();
		
		return new Node[] {sourceNode, targetNode};
	}
	
	private static void undirectEdge(Edge e) {
		e.setDirected(false);
		AttributeHelper.setArrowhead(e, false);
		AttributeHelper.setArrowtail(e, false);
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
		return "Fold-Delete";
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
		return "<html><br />"
				+ "This command helps you delete intermediate nodes,<br />"
				+ "while preserving overall neighbours' connectedness,<br />"
				+ "i.e. folds in nodes. The image depicts how selected<br />"
				+ "nodes are removed from the network. The resulting<br />"
				+ "network connectivity is influenced by the direction<br />"
				+ "of information flow. To avoid any connectivity loss,<br />"
				+ "use \"Ignore Edge Direction\".<br /><br />";
	}
	
	/* (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.AbstractAlgorithm#getAcceleratorKeyStroke()
	 */
	@Override
	public KeyStroke getAcceleratorKeyStroke() {
		return KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, java.awt.event.InputEvent.CTRL_DOWN_MASK);
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

	@Override
	public boolean isLayoutAlgorithm() {
			return false;
	}

	@Override
	public boolean isAlwaysExecutable() {
		return false;
	}
	
	
}
