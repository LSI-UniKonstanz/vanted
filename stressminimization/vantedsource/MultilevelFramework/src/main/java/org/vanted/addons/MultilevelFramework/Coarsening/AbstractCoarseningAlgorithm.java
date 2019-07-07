package org.vanted.addons.MultilevelFramework.Coarsening;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.AttributeHelper;
import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.selection.Selection;
import org.vanted.addons.MultilevelFramework.MultilevelGraph.MultiLevelChildNodeAttribute;
import org.vanted.addons.MultilevelFramework.MultilevelGraph.MultilevelChildEdgeAttribute;
import org.vanted.addons.MultilevelFramework.MultilevelGraph.MultilevelParentGraphAttribute;
import org.vanted.addons.MultilevelFramework.MultilevelGraph.MultilevelParentNodeAttribute;

/**
 * Abstract coarsening algorithm that provides helper functions, that will be
 * useful for the implementation of most mergers.
 */
public abstract class AbstractCoarseningAlgorithm extends AbstractAlgorithm implements CoarseningAlgorithm {
	protected Graph childGraph;
	protected Selection childSelection;

	@Override
	public void attach(Graph graph, Selection selection) {
		// attributes the coarsening algorithm will create
		Object[] childObjects = (Object[]) (graph.getAttribute(MultilevelParentGraphAttribute.FULLPATH).getValue());
		this.graph = (Graph) childObjects[0]; // this is the graph the coarsening algorithm will create
		this.selection = (Selection) childObjects[1];

		// attributes from the level below
		this.childGraph = graph; // this is the input graph from the level below
		this.childSelection = selection;
	}

	/**
	 * Creates a parent-node for a list of given nodes, adds the parent attribute to
	 * every node in the list, gives the parent node the mean position of it's
	 * children and labels the parent node if the children have labels also adds the
	 * MultilevelChildNodeAttribute, containing the number of child-nodes, to the
	 * new parent-node.
	 * 
	 * @param childNodes: the nodes to be merged to a single parent node
	 */
	public void createParent(List<Node> childNodes) {
		createParent(childNodes, averagePosition(childNodes));
	}

	public Point2D.Double averagePosition(List<Node> nodes) {
		int nodeCount = nodes.size();
		if (nodeCount == 0) {
			return null; // TODO: error message
		}
		double X = 0.0;
		double Y = 0.0;
		for (Node n : nodes) {
			X += AttributeHelper.getPositionX(n);
			Y += AttributeHelper.getPositionY(n);
		}
		X = X / nodeCount;
		Y = Y / nodeCount;
		return new Point2D.Double(X, Y);
	}

	/**
	 * Will create a Label for the parent-node consisting of the labels of the
	 * child-nodes separated by commas, if no child-node has a label this function
	 * returns an empty string.
	 * 
	 * @param childNodes child-nodes of the parent-node
	 * @return label for potential parent of the childNodes,
	 */
	public String parentLabel(List<Node> childNodes) {
		String parentLabel = "";
		boolean anyChildHasLabel = false;
		for (Node n : childNodes) {
			String childlabel = AttributeHelper.getLabel(n, "");
			parentLabel = parentLabel.concat(childlabel + ",");
			if (!childlabel.isEmpty()) {
				anyChildHasLabel = true;
			}
		}
		if (anyChildHasLabel) {
			parentLabel = parentLabel.substring(0, parentLabel.length() - 1);
			return parentLabel;
		} else {
			return "";
		}
	}

	/**
	 * Creates a parent-node for a list of given nodes at a user-defined position,
	 * adds the parent attribute to every node in the list, as well as the
	 * MultilevelChildnodeAttribute containing the number of child-nodes to the
	 * parent, and labels the parent node if the children have labels
	 * 
	 * @param childNodes : the nodes to be merged to a single parent node
	 * @param position :   the position of the new parent-node
	 */
	public void createParent(List<Node> childNodes, Point2D position) {
		if (childNodes.size() == 0) {
			return; // TODO: error message maybe?
		}
		Node parent = graph.addNode();
		selection.add(parent);
		for (Node n : childNodes) {
			Attribute attr = new MultilevelParentNodeAttribute(MultilevelParentNodeAttribute.NAME, parent);
			AttributeHelper.setAttribute(n, MultilevelParentNodeAttribute.PATH, MultilevelParentNodeAttribute.NAME,
					attr);
		}
		AttributeHelper.setPosition(parent, position);
		AttributeHelper.setLabel(parent, parentLabel(childNodes));
		setNumberOfChildNodes(childNodes, parent);
	}

	/**
	 * creates a parent-node identical to a chosen representative Node in the next
	 * higher graph in the multilevel hierarchyAbstractCoarseningAlgorithm. adds the
	 * parent attribute to every node in the list, as well as the
	 * MultilevelChildnodeAttribute containing the number of child-nodes to the
	 * parent, and labels the parent node if the children have labels
	 * 
	 * @param childnodes:     the nodes to be merged to a single parent node
	 * @param representative: the Node which is to represent the child-nodes in the
	 *                        higherGraph
	 */
	public void createParent(List<Node> childnodes, Node representative) {
		Node parent = graph.addNodeCopy(representative);
		selection.add(parent);
		for (Node n : childnodes) {
			Attribute parentAttribute = new MultilevelParentNodeAttribute(MultilevelParentNodeAttribute.NAME, parent);
			AttributeHelper.setAttribute(n, MultilevelParentNodeAttribute.PATH, MultilevelParentNodeAttribute.NAME,
					parentAttribute);
		}
		setNumberOfChildNodes(childnodes, parent);
	}

	/**
	 * Saves the number of child-nodes for a given parent-node as
	 * MultiLevelChildNodeAttribute
	 * 
	 * @param childnodes list of child-nodes
	 * @param parentNode parent node who will receive the attribute
	 */
	public void setNumberOfChildNodes(List<Node> childnodes, Node parentNode) {
		Attribute attr = new MultiLevelChildNodeAttribute(MultiLevelChildNodeAttribute.NAME, childnodes.size());
		AttributeHelper.setAttribute(parentNode, MultiLevelChildNodeAttribute.PATH, MultiLevelChildNodeAttribute.NAME,
				attr);
	}

	/**
	 * getter for the parent-node of the given child-node
	 * 
	 * @param child: the node who�s parent-node is to be returned
	 * @return the node representing child in the next higher graph in the
	 *         multilevel hierarchy (parent-node).
	 */
	public Node getParent(Node child) {
		return (Node) child.getAttribute(MultilevelParentNodeAttribute.FULLPATH).getValue();
	}

	/**
	 * For every pair of nodes in the graph an undirected edge is created, if there
	 * is an edge between the child nodes. adds the MultilevelChildEdgeAttribute
	 * containing the number of child-edges to the created parent edge.
	 */
	public void createEdges() {
		ArrayList<ArrayList<Edge>> parents = new ArrayList<ArrayList<Edge>>();
		for (Edge e : childGraph.getEdges()) {
			Node sparent = getParent(e.getSource());
			Node tparent = getParent(e.getTarget());
			if (graph.getEdges(sparent, tparent).isEmpty() && sparent != tparent && graph.containsNode(sparent)
					&& graph.containsNode(tparent)) {
				Edge parent = graph.addEdge(sparent, tparent, false);
				ArrayList<Edge> a = new ArrayList<Edge>();
				a.add(parent);
				parents.add(a);
				if (selection.contains(sparent) && selection.contains(tparent)) {
					selection.add(parent);
				}
			}
		}
		for (Edge e : childGraph.getEdges()) {
			Node sparent = getParent(e.getSource());
			Node tparent = getParent(e.getTarget());
			if (!graph.getEdges(sparent, tparent).isEmpty()) {
				for (ArrayList<Edge> a : parents) {
					if ((a.get(0).getSource() == sparent && a.get(0).getTarget() == tparent)
							|| a.get(0).getSource() == tparent && a.get(0).getTarget() == sparent) {
						a.add(e);
						break;
					}
				}
			}
		}
		for (ArrayList<Edge> a : parents) {
			Edge e = a.get(0);
			Attribute attr = new MultilevelChildEdgeAttribute(MultilevelChildEdgeAttribute.NAME, a.size() - 1);
			AttributeHelper.setAttribute(e, MultilevelChildEdgeAttribute.PATH, MultilevelChildEdgeAttribute.NAME, attr);
		}

	}

	/**
	 * creates a new parent node for each child node at the same position as in the
	 * child graph, if the node is not in the child selection.
	 */
	public void createParentNotSelectedNodes() {
		for (Node n : childGraph.getNodes()) {
			if (!childSelection.contains(n)) {
				Node parent = graph.addNode();
				Attribute attr = new MultilevelParentNodeAttribute(MultilevelParentNodeAttribute.NAME, parent);
				AttributeHelper.setAttribute(n, MultilevelParentNodeAttribute.PATH, MultilevelParentNodeAttribute.NAME,
						attr);
				AttributeHelper.setPosition(parent, averagePosition(Arrays.asList(n)));
				AttributeHelper.setLabel(parent, parentLabel(Arrays.asList(n)));
				Attribute at = new MultiLevelChildNodeAttribute(MultiLevelChildNodeAttribute.NAME, 1);
				AttributeHelper.setAttribute(parent, MultiLevelChildNodeAttribute.PATH,
						MultiLevelChildNodeAttribute.NAME, at);
			}
		}
	}

	/**
	 * getter for the number of child-nodes of a node
	 * 
	 * @param n the Node, whose number of children is needed
	 * @return number of children of node n
	 */
	public int getNumberOfChildNodes(Node n) {
		try {
			return (int) n.getAttribute(MultiLevelChildNodeAttribute.FULLPATH).getValue();
		} catch (AttributeNotFoundException e) {
			return 0;
		}
	}

	/**
	 * getter for the number of child-edges of an edge
	 * 
	 * @param e the Node, whose number of children is needed
	 * @return number of children of edge e
	 */
	public int getNumberOfChildEdges(Edge e) {
		try {
			return (int) e.getAttribute(MultilevelChildEdgeAttribute.FULLPATH).getValue();
		} catch (AttributeNotFoundException ex) {
			return 0;
		}
	}

}
