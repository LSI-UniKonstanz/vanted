package org.vanted.addons.MultilevelFramework.Coarsening;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.AttributeHelper;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Node;
import org.vanted.addons.MultilevelFramework.MultilevelGraph.MultilevelParentNodeAttribute;

/**
 * This implements the matching merger. It looks at all of the graphs edges: if
 * both nodes connected by the edge are not part of a merged node yet, they will
 * be merged
 */
public class MatchingCoarseningAlgorithm extends AbstractCoarseningAlgorithm {

	@Override
	public String getName() {
		return "Matching Merger";
	}

	@Override
	public String getDescription() {
		return "Creates a maximal matching and merges matched nodes.";
	}

	@Override
	public void execute() {
		List<Edge> childEdges = new ArrayList<Edge>(childSelection.getEdges());
		List<Node> childNodes = new ArrayList<Node>(childSelection.getNodes());
		Collections.shuffle(childEdges);
		for (Edge e : childEdges) {
			if (childSelection.contains(e.getTarget()) && childSelection.contains(e.getSource())) {
				Node sourceParent = (Node) AttributeHelper.getAttributeValue(e.getSource(),
						MultilevelParentNodeAttribute.PATH, MultilevelParentNodeAttribute.NAME, null,
						childNodes.get(0));
				Node targetParent = (Node) AttributeHelper.getAttributeValue(e.getTarget(),
						MultilevelParentNodeAttribute.PATH, MultilevelParentNodeAttribute.NAME, null,
						childNodes.get(0));
				if (sourceParent == null && targetParent == null) {
					createParent(Arrays.asList(e.getSource(), e.getTarget()));
				}
			}
		}
		// We still need to create a parent for all the lonely nodes that didn't get a
		// match :(
		for (Node n : childNodes) {
			Node parent = (Node) AttributeHelper.getAttributeValue(n, MultilevelParentNodeAttribute.PATH,
					MultilevelParentNodeAttribute.NAME, null, n);
			if (parent == null) {
				createParent(Arrays.asList(n));
			}
		}
		if (childSelection.getNumberOfNodes() != childGraph.getNumberOfNodes()) {
			createParentNotSelectedNodes();
		}
		createEdges();
	}

}
