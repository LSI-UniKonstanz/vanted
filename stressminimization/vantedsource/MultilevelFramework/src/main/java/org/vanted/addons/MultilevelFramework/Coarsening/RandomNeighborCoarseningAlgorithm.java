package org.vanted.addons.MultilevelFramework.Coarsening;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.AttributeHelper;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Node;
import org.graffiti.plugin.parameter.DoubleParameter;
import org.graffiti.plugin.parameter.Parameter;

/**
 * Randomly selects edges and merges the nodes on both ends of the edges until
 * the number of nodes is reduced by a target fraction.
 */
public class RandomNeighborCoarseningAlgorithm extends AbstractCoarseningAlgorithm {
	private static final String RNC_INDEX = "rnc_index";

	double targetNodeFraction;

	@Override
	public String getName() {
		return "Random Merger";
	}

	@Override
	public String getDescription() {
		return "Randomly merges neighbouring nodes until a certain fraction of nodes are left.";
	}

	@Override
	public void execute() {
		List<Node> childNodes = new ArrayList<Node>(childSelection.getNodes());
		int currentNumberOfParentNodes = childNodes.size();
		// we want targetNumberOfParentNodes parent nodes, when we are done with this
		int targetNumberOfParentNodes = (int) (currentNumberOfParentNodes * targetNodeFraction);
		// each future node is represented by a list of it's children
		// each child additionally (for performance reasons) saves the index of this
		// list in the array
		List<Node> parentNodes[] = new List[currentNumberOfParentNodes];
		int i = 0;
		for (Node c : childNodes) {
			// at first there is one parent node for every child
			parentNodes[i] = new ArrayList<Node>(Arrays.asList(c));
			c.addInteger("", RNC_INDEX, i);
			i++;
		}
		List<Edge> edges = new ArrayList<Edge>(childSelection.getEdges());
		Collections.shuffle(edges);
		for (Edge e : edges)// randomly test edges and try to merge the nodes on the edge
		{
			if (currentNumberOfParentNodes == targetNumberOfParentNodes) // are we done yet?
			{
				break;
			}
			if (childSelection.contains(e.getSource()) && childSelection.contains(e.getTarget())) {
				// merge parentNodes for the edge
				int sourceIndex = (int) AttributeHelper.getAttribute(e.getSource(), RNC_INDEX).getValue();
				int targetIndex = (int) AttributeHelper.getAttribute(e.getTarget(), RNC_INDEX).getValue();
				if (sourceIndex != targetIndex) {
					parentNodes[targetIndex].addAll(parentNodes[sourceIndex]);
					parentNodes[sourceIndex].forEach(n -> n.setInteger(RNC_INDEX, targetIndex));
					parentNodes[sourceIndex].clear();
					currentNumberOfParentNodes--;
				}
			}
		}
		for (List<Node> pn : parentNodes) {
			if (!pn.isEmpty())// if list is not empty
			{
				createParent(pn);// create the parent of these nodes
			}
		}
		if (childSelection.getNumberOfNodes() != childGraph.getNumberOfNodes()) {
			createParentNotSelectedNodes();
		}
		createEdges();

		// don't mind me, I'm just cleaning up
		for (Node c : childNodes) {
			c.removeAttribute(RNC_INDEX);
		}
	}

	@Override
	public void setParameters(Parameter[] params) {
		this.parameters = params;
		double fraction = ((DoubleParameter) params[0]).getDouble().doubleValue();
		this.targetNodeFraction = fraction;
	}

	@Override
	public Parameter[] getParameters() {
		DoubleParameter targetFraction = new DoubleParameter("Remaining Fraction of Nodes",
				"Merging stops when only this fraction of nodes is left.");
		targetFraction.setDouble(0.3);
		targetFraction.setMax(1.0);
		targetFraction.setMin(0.01);

		return new Parameter[] { targetFraction };

	}

}
