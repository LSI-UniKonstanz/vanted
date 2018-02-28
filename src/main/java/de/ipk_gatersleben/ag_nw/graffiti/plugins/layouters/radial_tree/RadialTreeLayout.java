/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.radial_tree;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.AttributeHelper;
import org.ErrorMsg;
import org.graffiti.attributes.Attribute;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.graphics.CoordinateAttribute;
import org.graffiti.graphics.DimensionAttribute;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.DoubleParameter;
import org.graffiti.plugin.parameter.NodeParameter;
import org.graffiti.plugin.parameter.ObjectListParameter;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.rt_tree.TreeContainer;

/**
 * An implementation of a radial tree layout algorithm.
 * 
 * @author Joerg Bartelheimer
 */

/* TODO: Layout of non-horizontal/vertical grids */
public class RadialTreeLayout extends AbstractAlgorithm {

	/*************************************************************/
	/* Member variables */
	/*************************************************************/

	/**
	 * Dynamical defined node coordinate attribute.
	 */
	private final String COORDSTR = GraphicAttributeConstants.GRAPHICS + Attribute.SEPARATOR
			+ GraphicAttributeConstants.COORDINATE;

	/**
	 * Dynamical defined node dimension attribute.
	 */
	private final String DIMENSIONSTR = GraphicAttributeConstants.GRAPHICS + Attribute.SEPARATOR
			+ GraphicAttributeConstants.DIMENSION;

	/**
	 * Distance of each node from the center
	 */
	private double nodeDistance = 100;

	/**
	 * Horizontal distance between trees
	 */
	private double xDistance = 30;

	/**
	 * Vertical distance between trees
	 */
	private double yDistance = 30;

	/**
	 * The maximum y dimension for each node.
	 */
	private HashMap<Integer, Double> maxNodeHeight = new HashMap<>();

	/**
	 * Put all trees in a row
	 */
	private boolean horizontalLayout = true;

	/**
	 * Move all tree in the right direction either 0, 90, 180 or 270 degree
	 */
	private Integer[] treeDirectionParameter = { 0, 90, 180, 270 };
	private int treeDirection;

	/**
	 * Remove all bends
	 */
	private boolean doRemoveBends = true;

	/**
	 * Activate source node red backround
	 */
	// private boolean doMarkSourceNode = true;

	/**
	 * x coordinate of start point
	 */
	private double xStart = 100;

	/**
	 * y coordinate of start point
	 */
	private double yStart = 100;

	/**
	 * All trees are initialized by this variable.
	 */
	private Set<Node> graphNodes;

	/**
	 * The roots in the forest.
	 */
	private HashMap<Node, TreeContainer> forrest = new HashMap<>();

	/**
	 * The root node of the tree.
	 */
	private Node selectedNode = null;

	/**
	 * The depth for each node .
	 */
	private HashMap<Node, Integer> bfsMapNodeToIndex = new HashMap<>();

	/**
	 * If there are a circle edges, save them here
	 */
	private LinkedList<Edge> tempEdges = new LinkedList<>();

	/**
	 * Sum of all children
	 */
	private HashMap<Node, Integer> magnitude = new HashMap<>();

	/**
	 * x coordinate of start point
	 */
	// private double xStartParam = 100;

	/**
	 * y coordinate of start point
	 */
	// private double yStartParam = 100;
	private DoubleParameter distanceParam;
	// private DoubleParameter xStartParam2;
	// private DoubleParameter yStartParam2;
	// private BooleanParameter horizontalParam;
	private BooleanParameter removeBendParam;
	// private BooleanParameter markedSourceNodeParam;
	private ObjectListParameter treeDirectionParam;

	/*************************************************************/
	/* Declarations of methods */
	/*************************************************************/

	/**
	 * Construct a new RadialTreeLayout algorithm instance.
	 */
	public RadialTreeLayout() {
	}

	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#check()
	 */
	@Override
	public void check() throws PreconditionException {
		if (graph == null)
			throw new PreconditionException("No graph available!");
		if (graph.getNumberOfNodes() <= 0) {
			throw new PreconditionException("The graph is empty. Cannot run tree layouter.");
		}

	}

	/**
	 * Check whether the tree is rooted.
	 */
	public boolean rootedTree(Node rootNode) {

		int roots = 0;
		for (Node node : bfsMapNodeToIndex.keySet()) {
			/* maybe there is a second */
			if ((node.getInDegree() == 0) && (node.getOutDegree() > 0)) {
				if (roots == 0) {
					roots++;
				} else {
					// return false;
				}
			}
			int ancestors = 0;
			for (Edge neighbourEdge : node.getEdges()) {
				Node neighbour = null;
				if (neighbourEdge.getSource() == node) {
					neighbour = neighbourEdge.getTarget();
				} else {
					neighbour = neighbourEdge.getSource();
				}

				/* any links from upper level nodes more than once ? */
				if (((Integer) bfsMapNodeToIndex.get(node)).intValue() > ((Integer) bfsMapNodeToIndex.get(neighbour))
						.intValue()) {
					ancestors++;
					if (ancestors > 1) {
						tempEdges.add(neighbourEdge);
						graph.deleteEdge(neighbourEdge);
					}
				}
				/* any links from same level nodes ? */
				if (((Integer) bfsMapNodeToIndex.get(node)).intValue() == ((Integer) bfsMapNodeToIndex.get(neighbour))
						.intValue()) {

					tempEdges.add(neighbourEdge);
					graph.deleteEdge(neighbourEdge);
					/*
					 * old rooted check routine ancestors++; if (ancestors > 1) { return false; }
					 */
				}
			}
		}
		return true;
	}

	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#getParameters()
	 */
	@Override
	public Parameter[] getParameters() {
		Node initNode = null;
		if (!selection.getNodes().isEmpty())
			initNode = selection.getNodes().iterator().next();
		else
			initNode = graph.getNodes().iterator().next();

		NodeParameter nodeParam = new NodeParameter(graph, initNode, "<html>Start-Node<br/>(from list or selected)",
				"Tree layouter will start only with a selected node.");
		if (distanceParam == null) {
			distanceParam = new DoubleParameter("Node Radius", "The distance from the center of each node");

			// xStartParam2 = new DoubleParameter(
			// "X base",
			// "The x coordinate of the starting point of the grid horizontal direction.");
			//
			// yStartParam2 = new DoubleParameter(
			// "Y base",
			// "The y coordinate of the starting point of the grid horizontal direction.");
			//
			// horizontalParam = new BooleanParameter(
			// horizontalLayout,
			// "Place Trees in a Row",
			// "Place all trees in a row");

			removeBendParam = new BooleanParameter(doRemoveBends, "Remove Bends", "Remove all bends in the forest");

			// markedSourceNodeParam = new BooleanParameter(
			// doMarkSourceNode,
			// "Mark Start-Node",
			// "Mark each source Node");

			treeDirectionParam = new ObjectListParameter(treeDirectionParameter[0], "Tree Direction (degree)",
					"Move all trees in 0, 90, 180 or 270 degree", treeDirectionParameter);

			distanceParam.setDouble(nodeDistance);
			// xStartParam2.setDouble(this.xStartParam);
			// yStartParam2.setDouble(this.yStartParam);
		}
		return new Parameter[] { nodeParam, distanceParam,
				// xStartParam2,
				// yStartParam2,
				// horizontalParam,
				removeBendParam,
				// markedSourceNodeParam,
				treeDirectionParam };
	}

	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#
	 *      setParameters(org.graffiti.plugin.algorithm.Parameter)
	 */
	@Override
	public void setParameters(Parameter[] params) {
		this.parameters = params;
		int i = 0;
		if (selection.getNodes().isEmpty()) {
			selection.add(((NodeParameter) params[i++]).getNode());
		} else
			i++; // skip parameter
		// System.out.println("Node: " + AttributeHelper.getLabel(n, "- unnamed -"));

		nodeDistance = ((DoubleParameter) params[i++]).getDouble().doubleValue();
		// xStart = ((DoubleParameter) params[i++]).getDouble().doubleValue();
		// yStart = ((DoubleParameter) params[i++]).getDouble().doubleValue();
		// xStartParam = xStart;
		// yStartParam = yStart;
		// horizontalLayout =
		// ((BooleanParameter) params[i++]).getBoolean().booleanValue();
		doRemoveBends = ((BooleanParameter) params[i++]).getBoolean().booleanValue();
		// doMarkSourceNode =
		// ((BooleanParameter) params[i++]).getBoolean().booleanValue();
		treeDirection = (Integer) ((ObjectListParameter) params[i++]).getValue();

	}

	/**
	 * Return a postordered tree
	 * 
	 * @param root
	 * @return a postordered tree
	 */
	private LinkedList<Node> postorder(Node root) {
		LinkedList<Node> result = new LinkedList<>();
		postorderTraverse(null, root, result);
		return result;
	}

	/**
	 * Return a postordered tree
	 * 
	 * @param root
	 * @return a preordered tree
	 */
	/*
	 * private LinkedList<Node> preorder(Node root) { LinkedList<Node> result = new
	 * LinkedList<>(); preorderTraverse(null, root, result); return result; }
	 */
	/**
	 * Traverse the tree in postorder
	 * 
	 * @param ancestor
	 *            - from node
	 * @param node
	 *            - start node
	 * @param lq
	 *            - result is a LinkedList
	 */
	private void postorderTraverse(Node ancestor, Node node, LinkedList<Node> lq) {
		for (Node neighbor : node.getNeighbors()) {
			if (neighbor != ancestor) {
				postorderTraverse(node, neighbor, lq);
			}
		}
		lq.addLast(node);
	}

	/**
	 * Traverse the tree in preorder
	 * 
	 * @param ancestor
	 *            - from node
	 * @param node
	 *            - start node
	 * @param lq
	 *            - result is a LinkedList
	 */
	/*
	 * private void preorderTraverse(Node ancestor, Node node, LinkedList<Node> lq)
	 * { lq.addLast(node); for(Node neighbor : node.getNeighbors()){ if (neighbor !=
	 * ancestor) { preorderTraverse(node, neighbor, lq); } }
	 * 
	 * }
	 */
	/**
	 * Get the successors of the given node
	 * 
	 * @param node
	 * @return
	 */
	private LinkedList<Node> getSuccessors(Node node) {
		LinkedList<Node> result = new LinkedList<>();

		for (Iterator<Node> neighbors = node.getNeighborsIterator(); neighbors.hasNext();) {
			Node neighbor = (Node) neighbors.next();
			if (((Integer) bfsMapNodeToIndex.get(node)).intValue() < ((Integer) bfsMapNodeToIndex.get(neighbor))
					.intValue()) {
				result.add(neighbor);
			}
		}
		return result;
	}

	/**
	 * Get the predecessors of the given node
	 * 
	 * @param node
	 * @return
	 */
	private LinkedList<Node> getPredecessors(Node node) {
		LinkedList<Node> result = new LinkedList<>();

		for (Iterator<Node> neighbors = node.getNeighborsIterator(); neighbors.hasNext();) {
			Node neighbor = (Node) neighbors.next();
			if (((Integer) bfsMapNodeToIndex.get(node)).intValue() > ((Integer) bfsMapNodeToIndex.get(neighbor))
					.intValue()) {
				result.add(neighbor);
			}
		}
		return result;
	}

	/**
	 * Init for each node the sum of all children and sub children
	 */
	protected void initMagnitude() {
		magnitude = new HashMap<>();

		for (Node node : postorder(selectedNode)) {

			int nodeValue = 1;
			if (magnitude.get(node) != null) {
				nodeValue = ((Integer) magnitude.get(node)).intValue();
			} else {
				magnitude.put(node, Integer.valueOf(1));
			}

			for (Node neighbour : getPredecessors(node)) {
				int sum = nodeValue;
				if (magnitude.get(neighbour) != null) {
					sum += ((Integer) magnitude.get(neighbour)).intValue();
				}

				magnitude.put(neighbour, Integer.valueOf(sum));
			}

		}
	}

	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute(Graph) The given graph
	 *      must have at least one node.
	 */
	public void execute() {
		// GravistoService.getInstance().algorithmAttachData(this);

		tempEdges = new LinkedList<Edge>();

		forrest = new HashMap<>();

		graphNodes = new HashSet<>();
		graphNodes.addAll(graph.getNodes());
		// for (Iterator iterator = graph.getNodesIterator(); iterator.hasNext();) {
		// forest.put(iterator.next(), null);
		// }

		/* check all trees with selected nodes, whether they have one root */

		for (Iterator<Node> iterator = selection.getNodes().iterator(); iterator.hasNext();) {

			selectedNode = iterator.next();

			/* ignore multiple selection */
			if (graphNodes.contains(selectedNode)) {
				/* check circle connection by using the depth of each node */
				computeDepth(selectedNode);
				forrest.put(selectedNode, new TreeContainer(bfsMapNodeToIndex, maxNodeHeight));

				if (!rootedTree(selectedNode)) {
					ErrorMsg.addErrorMessage("The given graph is not a tree.");
				}
			}
		}

		/* check the trees whether they have one root */
		/*
		 * while (graphNodes.iterator().hasNext()) {
		 * 
		 * selectedNode = (Node) graphNodes.iterator().next();
		 * 
		 * /* check circle connection by using the depth of each node
		 */
		/*
		 * computeDepth(selectedNode); forrest.put( selectedNode, new TreeContainer(
		 * bfsMapNodeToIndex, maxNodeHeight));
		 * 
		 * if (!rootedTree(selectedNode)) { for (Edge edge : tempEdges) {
		 * graph.addEdgeCopy(edge, edge.getSource(), edge.getTarget()); }
		 * ErrorMsg.addErrorMessage("The given graph has trees with multiple roots."); }
		 * 
		 * /* in case of arrows, try to find the root
		 */
		/*
		 * Node node = null; for (Iterator iterator = preorder(selectedNode).iterator();
		 * iterator.hasNext();) { node = (Node) iterator.next(); if ((node.getInDegree()
		 * == 0) && (node.getOutDegree() > 0)) {
		 * 
		 * forrest.remove(selectedNode);
		 * 
		 * selectedNode = node;
		 * 
		 * computeDepth(selectedNode);
		 * 
		 * forrest.put( selectedNode, new TreeContainer( bfsMapNodeToIndex,
		 * maxNodeHeight));
		 * 
		 * break; } }
		 * 
		 * }
		 */
		graph.getListenerManager().transactionStarted(this);

		for (Iterator<Node> iterator = forrest.keySet().iterator(); iterator.hasNext();) {

			selectedNode = iterator.next();

			Point2D position = AttributeHelper.getPosition(selectedNode);
			xStart = position.getX();
			yStart = position.getY();

			// if (doMarkSourceNode)
			// AttributeHelper.setFillColor(sourceNode, Color.RED);
			//
			bfsMapNodeToIndex = ((TreeContainer) forrest.get(selectedNode)).getBfsNum();
			maxNodeHeight = ((TreeContainer) forrest.get(selectedNode)).getMaxNodeHeight();

			/* compute segments of the tree */
			initMagnitude();

			/* compute positions */
			computePositions();

			/* place the tree on its position */
			if (horizontalLayout) {
				if ((treeDirection == 180) || (treeDirection == 0)) {
					xStart += maxNodeHeight.size() * nodeDistance * 2 + xDistance;
				} else {
					yStart += maxNodeHeight.size() * nodeDistance * 2 + yDistance;
				}
			} else {
				if ((treeDirection == 180) || (treeDirection == 0)) {
					yStart += maxNodeHeight.size() * nodeDistance * 2 + yDistance;
				} else {
					xStart += maxNodeHeight.size() * nodeDistance * 2 + xDistance;
				}
			}

			if (doRemoveBends) {
				GraphHelper.removeBendsBetweenSelectedNodes(bfsMapNodeToIndex.keySet(), false);
			}
			Point2D newPosition = AttributeHelper.getPosition(selectedNode);
			GraphHelper.moveNodes(bfsMapNodeToIndex.keySet(), position.getX() - newPosition.getX(),
					position.getY() - newPosition.getY());
		}
		for (Edge edge : tempEdges) {
			graph.addEdgeCopy(edge, edge.getSource(), edge.getTarget());
		}

		graph.getListenerManager().transactionFinished(this);
	}

	/**
	 * Initialize the level of each node
	 */
	private void computeDepth(Node startNode) {

		LinkedList<Node> queue = new LinkedList<>();

		maxNodeHeight = new HashMap<>();

		bfsMapNodeToIndex = new HashMap<>();

		queue.addLast(startNode);
		bfsMapNodeToIndex.put(startNode, Integer.valueOf(0));
		graphNodes.remove(startNode);

		/* BreadthFirstSearch algorithm which calculates the depth of the tree */
		while (!queue.isEmpty()) {

			Node v = (Node) queue.removeFirst();
			/* Walk through all neighbours of the last node */
			for (Node neighbour : v.getNeighbors()) {

				/* Not all neighbours, just the neighbours not visited yet */
				if (!bfsMapNodeToIndex.containsKey(neighbour)) {
					Integer depth = Integer.valueOf(((Integer) bfsMapNodeToIndex.get(v)).intValue() + 1);

					double nodeHeight = getNodeHeight(neighbour);

					/* Compute the maximum height of nodes in each level of the tree */
					Double maxNodeHeightValue = (Double) maxNodeHeight.get(depth);
					if (maxNodeHeightValue != null) {
						maxNodeHeight.put(depth, new Double(Math.max(maxNodeHeightValue.doubleValue(), nodeHeight)));
					} else {
						maxNodeHeight.put(depth, new Double(nodeHeight));
					}

					graphNodes.remove(neighbour);
					bfsMapNodeToIndex.put(neighbour, depth);
					queue.addFirst(neighbour);
				}
			}
		}

	}

	/**
	 * Start computing the root
	 */
	protected void computePositions() {

		double rho = 0.0;
		double alpha1 = 0.0;
		double alpha2 = 2 * Math.PI;

		/* compute the coordinates (alpha1 + alpha2) / 2 */
		double nodeCoordX = polarToCartesianX(rho, (alpha1 + alpha2) / 2);
		double nodeCoordY = polarToCartesianY(rho, (alpha1 + alpha2) / 2);

		setX(selectedNode, nodeCoordX);
		setY(selectedNode, nodeCoordY);

		/*
		 * give every kid a sector proportional to its width / width of the whole
		 * subtree
		 */
		double rootWidth = magnitude(selectedNode);
		rho++;
		/* launch the RadialSubtree method on its leaves */
		for (Node successor : getSuccessors(selectedNode)) {
			double succWidth = magnitude(successor);

			alpha2 = alpha1 + (2 * Math.PI * succWidth / rootWidth);
			/* start computing the children */
			radialSubTree(successor, succWidth, rho, alpha1, alpha2);
			alpha1 = alpha2;
		}
	}

	/**
	 * Compute the x coordinate
	 * 
	 * @param rho
	 * @param alpha
	 * @return
	 */
	protected double polarToCartesianX(double rho, double alpha) {
		double result = xStart + rho * Math.cos(alpha) * nodeDistance + maxNodeHeight.size() * nodeDistance;

		if (treeDirection == 270) {
			result = xStart - rho * Math.cos(alpha) * nodeDistance + maxNodeHeight.size() * nodeDistance;
		}

		return result;
	}

	/**
	 * Compute the y coordinate
	 * 
	 * @param rho
	 * @param alpha
	 * @return y coordinate
	 */
	protected double polarToCartesianY(double rho, double alpha) {
		double result = yStart + rho * Math.sin(alpha) * nodeDistance + maxNodeHeight.size() * nodeDistance;

		if (treeDirection == 180) {
			result = yStart - rho * Math.sin(alpha) * nodeDistance + maxNodeHeight.size() * nodeDistance;
		}

		return result;
	}

	/**
	 * Compute all subtrees recursively
	 * 
	 * @param node
	 * @param width
	 * @param rho
	 * @param alpha1
	 * @param alpha2
	 */
	protected void radialSubTree(Node node, double width, double rho, double alpha1, double alpha2) {

		/* compute the coordinates (alpha1 + alpha2) / 2 */
		double nodeCoordX = polarToCartesianX(rho, (alpha1 + alpha2) / 2);
		double nodeCoordY = polarToCartesianY(rho, (alpha1 + alpha2) / 2);

		setX(node, nodeCoordX);
		setY(node, nodeCoordY);

		double tau = 2 * Math.acos(rho / (rho + 1));
		double alpha = 0.0;
		double s = 0.0;
		if (tau < (alpha2 - alpha1)) {
			alpha = (alpha1 + alpha2 - tau) / 2.0;
			s = tau / width;
		} else {
			alpha = alpha1;
			s = (alpha2 - alpha1) / width;
		}
		/* launch the RadialSubtree method on its leaves */
		for (Node successor : getSuccessors(node)) {

			double succWidth = magnitude(successor);

			radialSubTree(successor, succWidth, rho + 1, alpha, alpha += s * succWidth);
		}
	}

	/**
	 * Get the magnitude for the given node
	 * 
	 * @param node
	 * @return the sum of all children and sub children
	 */
	protected double magnitude(Node node) {

		return ((Integer) magnitude.get(node)).intValue();
	}

	/**
	 * Return the height dimension of the given node n
	 * 
	 * @param n
	 *            node
	 * @return
	 */
	private double getNodeHeight(Node n) {
		DimensionAttribute dimAttr = (DimensionAttribute) n.getAttribute(DIMENSIONSTR);

		double result = 0.0;

		if ((treeDirection == 90) || (treeDirection == 270)) {
			result = dimAttr.getDimension().getWidth();
		} else {
			result = dimAttr.getDimension().getHeight();
		}

		return result;
	}

	/**
	 * Sets the x position of the given node n
	 * 
	 * @param n
	 *            node
	 * @param x
	 *            position
	 */
	private void setX(Node n, double x) {
		CoordinateAttribute coordAttr = (CoordinateAttribute) n.getAttribute(COORDSTR);

		if (coordAttr != null) {
			if ((treeDirection == 90) || (treeDirection == 270)) {
				coordAttr.setY(x);
			} else {
				coordAttr.setX(x);
			}
		}
	}

	/**
	 * Set the y position of the given node n
	 * 
	 * @param n
	 *            node
	 * @param y
	 *            position
	 */
	private void setY(Node n, double y) {
		CoordinateAttribute coordAttr = (CoordinateAttribute) n.getAttribute(COORDSTR);

		if (coordAttr != null) {
			if ((treeDirection == 90) || (treeDirection == 270)) {
				coordAttr.setX(y);
			} else {
				coordAttr.setY(y);
			}
		}
	}

	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		// return null;
		return "Radial Tree";
	}

	@Override
	public String getCategory() {
		return "Layout";
	}

	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(Arrays.asList(Category.GRAPH, Category.LAYOUT));
	}

	@Override
	public boolean isLayoutAlgorithm() {
		return true;
	}

}
