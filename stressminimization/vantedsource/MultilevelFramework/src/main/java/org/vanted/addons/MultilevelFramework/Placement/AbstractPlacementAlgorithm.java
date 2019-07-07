package org.vanted.addons.MultilevelFramework.Placement;

import org.AttributeHelper;
import org.Vector2d;
import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.vanted.addons.MultilevelFramework.MultilevelGraph.MultiLevelChildNodeAttribute;
import org.vanted.addons.MultilevelFramework.MultilevelGraph.MultilevelChildEdgeAttribute;
import org.vanted.addons.MultilevelFramework.MultilevelGraph.MultilevelParentNodeAttribute;

/**
 * Abstract placement algorithm that provides helper functions, that will be
 * useful for the implementation of most placers.
 */
public abstract class AbstractPlacementAlgorithm extends AbstractAlgorithm implements PlacementAlgorithm {
	/**
	 * returns the position of the node representing the parameter node in the next
	 * higher level of the MultilevelGraph
	 * 
	 * @param child: the node who's parent-nodes position is desired
	 * @return position of the parent as Vector2d
	 */
	public Vector2d getParentPosition(Node child) {
		Node parentNode = getParent(child);
		return AttributeHelper.getPositionVec2d(parentNode);

	}

	/**
	 * getter for parent-node of a given node
	 * 
	 * @param child: node parent-node is desired
	 * @return returns the parent in the next higher graph in the hierarchy of the
	 *         multilevel-graph
	 */
	public Node getParent(Node child) {
		return (Node) child.getAttribute(MultilevelParentNodeAttribute.FULLPATH).getValue();
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
