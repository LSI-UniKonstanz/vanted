package org.vanted.addons.MultilevelFramework.Coarsening;

import java.util.Arrays;

import org.graffiti.graph.Node;

/**
 * Every node gets its own parent node.
 */
public class NullCoarseningAlgorithm extends AbstractCoarseningAlgorithm {
	@Override
	public String getName() {
		return "Null Merger";
	}

	@Override
	public String getDescription() {
		return "Creates an exact copy of the level below.";
	}

	@Override
	public void execute() {
		for (Node c : childSelection.getNodes()) {
			createParent(Arrays.asList(c));
		}
		if (childSelection.getNumberOfNodes() != childGraph.getNumberOfNodes()) {
			createParentNotSelectedNodes();
		}
		createEdges();
	}
}
