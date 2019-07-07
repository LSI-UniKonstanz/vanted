package org.vanted.addons.MultilevelFramework.Coarsening;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.graffiti.graph.Node;
import org.graffiti.plugin.parameter.IntegerParameter;
import org.graffiti.plugin.parameter.Parameter;

/**
 * Merges a fixed number of randomly selected nodes. They do not have to be
 * neighbours. Not to confuse with random neighbour coarsening.
 */
public class RandomCoarseningAlgorithm extends AbstractCoarseningAlgorithm {
	protected int childrenPerNode;

	@Override
	public String getName() {
		return "True Random Merger";
	}

	@Override
	public String getDescription() {
		return "Merges nodes completely randomly. They don't have to be neighbours.";
	}

	/**
	 *
	 */
	@Override
	public void execute() {
		List<Node> childGraphNodes = new ArrayList<Node>(childSelection.getNodes());
		Collections.shuffle(childGraphNodes);
		int currentChildrenCount = 0;
		List<Node> currentChildren = new ArrayList<Node>();
		for (Node c : childGraphNodes) {
			currentChildrenCount++;
			currentChildren.add(c);
			if (currentChildrenCount == childrenPerNode) {
				currentChildrenCount = 0;
				createParent(currentChildren);
				currentChildren.clear();
			}
		}
		if (currentChildrenCount > 0) {
			createParent(currentChildren);
		}
		if (childSelection.getNumberOfNodes() != childGraph.getNumberOfNodes()) {
			createParentNotSelectedNodes();
		}
		createEdges();
	}

	@Override
	public void setParameters(Parameter[] params) {
		this.parameters = params;
		IntegerParameter p = (IntegerParameter) params[0];
		int childrenPerNode = p.getInteger().intValue();
		if (childrenPerNode == 0) {
			childrenPerNode = 1;
		}
		this.childrenPerNode = childrenPerNode;
	}

	@Override
	public Parameter[] getParameters() {
		IntegerParameter childrenPerNodeParameter = new IntegerParameter(1, 1, 1000, "Child Nodes per Parent",
				"Every parent node will have this many child nodes in the level below after the algorithm is executed.");
		return new Parameter[] { childrenPerNodeParameter };

	}
}
