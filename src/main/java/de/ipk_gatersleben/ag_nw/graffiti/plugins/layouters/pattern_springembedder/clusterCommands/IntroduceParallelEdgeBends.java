/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.AttributeHelper;
import org.Vector2d;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.Parameter;

public class IntroduceParallelEdgeBends extends AbstractAlgorithm {

	public String getName() {
		return "Layout Parallel Edges";
	}

	@Override
	public String getCategory() {
		return "Network.Edges.Bends";
	}

	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(Arrays.asList(Category.EDGE, Category.LAYOUT, Category.VISUAL));
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public Parameter[] getParameters() {
		return null;
	}

	@Override
	public void setParameters(Parameter[] params) {
	}

	@Override
	public void check() throws PreconditionException {
		if (this.graph == null)
			throw new PreconditionException("No graph available!");
		if (this.graph.getEdges().size() <= 0)
			throw new PreconditionException("Graph contains no edges!");
	}

	public void execute() {
		this.graph.getListenerManager().transactionStarted(this);
		try {

			if (selection.getNodes().size() == 0) {

				if (selection.getEdges().size() == 0)
					parallelEdgesFromEdgeSelection(graph.getEdges());
				else
					parallelEdgesFromEdgeSelection(selection.getEdges());

			} else {

				parallelEdgesFromNodeSelection(new ArrayList<Node>(selection.getNodes()));
			}
			// HashSet<Edge> changed = new HashSet<Edge>();

			// MainFrame.getInstance().getActiveEditorSession().getSelectionModel().setActiveSelection(new
			// Selection("changed edges", changed));
			// MainFrame.getInstance().getActiveEditorSession().getSelectionModel().selectionChanged();

			// MainFrame.showMessage(changed.size() + " edges have been processed and
			// selected.", MessageType.INFO);
		} finally {
			this.graph.getListenerManager().transactionFinished(this);
		}
	}

	private void parallelEdgesFromNodeSelection(List<Node> nodes) {
		for (int s = 0; s < nodes.size(); s++) {
			for (int t = s + 1; t < nodes.size(); t++) {
				Node source = nodes.get(s);
				Node target = nodes.get(t);
				Collection<Edge> edgeSet = this.graph.getEdges(source, target);

				if (edgeSet == null || edgeSet.size() <= 1)
					continue;

				Edge[] edges = edgeSet.toArray(new Edge[edgeSet.size()]);

				introduceOrthogonalBends(edges);
			}
		}
	}

	private void parallelEdgesFromEdgeSelection(Collection<Edge> edges) {

		Map<String, List<Edge>> mapCombinedNodeIdToEdgeList = new HashMap<String, List<Edge>>();

		for (Edge curEdge : edges) {
			Node sourceNode = curEdge.getSource();
			Node targetNode = curEdge.getTarget();

			sourceNode.toString();

			String combinedIdForward = Integer.toString(sourceNode.hashCode())
					+ Integer.toString(targetNode.hashCode());
			String combinedIdBackward = Integer.toString(targetNode.hashCode())
					+ Integer.toString(sourceNode.hashCode());

			List<Edge> listEdges;

			if ((listEdges = mapCombinedNodeIdToEdgeList.get(combinedIdForward)) == null) {
				if ((listEdges = mapCombinedNodeIdToEdgeList.get(combinedIdBackward)) == null) {
					listEdges = new ArrayList<Edge>();
					mapCombinedNodeIdToEdgeList.put(combinedIdForward, listEdges);
				}
			}
			listEdges.add(curEdge);

		}

		for (String key : mapCombinedNodeIdToEdgeList.keySet()) {
			List<Edge> curListEdges = mapCombinedNodeIdToEdgeList.get(key);
			introduceOrthogonalBends(curListEdges.toArray(new Edge[curListEdges.size()]));
		}
	}

	private void introduceOrthogonalBends(Edge[] edges) {
		if (edges == null || edges.length == 0)
			return;
		// node positions
		// and we assume that they are the same source and target for all edges
		Node source = edges[0].getSource();
		Node target = edges[0].getTarget();

		Vector2d sourcePos = AttributeHelper.getPositionVec2d(source);
		Vector2d targetPos = AttributeHelper.getPositionVec2d(target);

		// node position difference vector
		Vector2d diffVector = new Vector2d(targetPos.x - sourcePos.x, targetPos.y - sourcePos.y);

		// orthogonal (scaled) vector
		Vector2d orthVector = diffVector.getOrthogonal().scale(1 / (5. * edges.length / 2.));

		// middle point between nodes
		Vector2d middlePoint = new Vector2d(sourcePos.x + diffVector.x / 2, sourcePos.y + diffVector.y / 2);

		boolean odd = edges.length % 2 == 1;
		int overhead = 0;

		// layout first the middle edge (if exists) and then pairwise all other edges
		if (odd) {
			overhead++;

			adjustEdge(edges[0], middlePoint);
			// changed.add(edges[0]);
		}

		for (int i = 0; i < edges.length - overhead; i += 2) {
			Edge edgeOne = edges[overhead + i];
			Edge edgeTwo = edges[overhead + i + 1];

			int mult = (i + 1) + ((odd) ? 1 : 0);

			Vector2d bend1 = new Vector2d(middlePoint.x + orthVector.x * mult, middlePoint.y + orthVector.y * mult);
			Vector2d bend2 = new Vector2d(middlePoint.x - orthVector.x * mult, middlePoint.y - orthVector.y * mult);

			this.adjustEdge(edgeOne, bend1);
			this.adjustEdge(edgeTwo, bend2);
			// changed.add(edgeOne);
			// changed.add(edgeTwo);
		}
	}

	private void adjustEdge(Edge edge, Vector2d pos) {
		AttributeHelper.removeEdgeBends(edge);

		AttributeHelper.addEdgeBend(edge, pos);

		AttributeHelper.setEdgeBendStyle(edge, "org.graffiti.plugins.views.defaults.SmoothLineEdgeShape");
	}

	@Override
	public boolean mayWorkOnMultipleGraphs() {
		return true;
	}
}