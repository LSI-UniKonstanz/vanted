package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractEditorAlgorithm;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.view.View;
import org.graffiti.session.EditorSession;
import org.graffiti.util.Pair;

import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

/**
 * The following algorithm deletes additional parallel edges, leaving only a
 * single edge.
 * 
 * @author Dimitar Garkov
 * @since 2.8.0
 *
 */
public class RemoveParallelEdges extends AbstractEditorAlgorithm {

	private boolean undirected = false;

	@Override
	public boolean activeForView(View v) {
		return v != null;
	}

	@Override
	public void check() throws PreconditionException {
		super.check();
		// Select either only nodes or only edges or nothing
		PreconditionException e = new PreconditionException();
		if (selection.getNumberOfEdges() > 0 && selection.getNumberOfNodes() > 0)
			e.add("<html>Please select either only nodes or only edges.<br>The selection can also be empty to process the whole graph.<br><br>"
					+ "Currently, number of selected nodes: " + selection.getNumberOfNodes()
					+ ", and number of selected edges: " + selection.getNumberOfEdges());

		if (!e.isEmpty())
			throw e;
	}

	@Override
	public Parameter[] getParameters() {
		return new Parameter[] { new BooleanParameter(undirected, "Undirected graph",
				"<html>Should the algorithm ignore edge direction?<br>"
						+ "Using an undirected setting on a directed graph, does not guarantee the order of kept edges.") };
	}

	@Override
	public void setParameters(Parameter[] params) {
		undirected = ((BooleanParameter) params[0]).getBoolean().booleanValue();
	}

	@Override
	public void execute() {
		final BackgroundTaskStatusProviderSupportingExternalCallImpl status = new BackgroundTaskStatusProviderSupportingExternalCallImpl(
				"", "");
		status.setPluginWaitsForUser(false);

		final EditorSession session = MainFrame.getInstance().getActiveEditorSession();
		final LinkedList<Edge> toDelete = new LinkedList<>();
		// We don't update the selection directly, this is handled internally

		Runnable exec = new Runnable() {
			@Override
			public void run() {
				HashSet<Pair<Long, Long>> antiParellelEdges = new HashSet<>();
				Collection<Edge> workingEdges = null;
				if (selection == null || selection.isEmpty()) {
					workingEdges = session.getGraph().getEdges();
				} else if (selection.getEdges().isEmpty()) { // selected: only nodes
					HashSet<Edge> edges = new HashSet<>();
					for (Node n : selection.getNodes()) {
						for (Edge e : n.getEdges()) { // all edges, given selection is subset of all nodes
							edges.add(e);
						}
					}
					workingEdges = edges;
				} else { // selected: only edges
					workingEdges = selection.getEdges();
				}

				if (status.wantsToStop())
					return;

				for (Edge e : workingEdges) {
					Long src = e.getSource().getID();
					Long trg = e.getTarget().getID();
					Pair<Long, Long> srcTrg = new Pair<Long, Long>(src, trg);
					if (isParallelEdge(antiParellelEdges, srcTrg)) {
						toDelete.add(e);
						continue;
					}
					System.out.println("Not parallel -> " + e + "(" + src + ", " + trg + ")");
					antiParellelEdges.add(srcTrg);
				}

				if (status.wantsToStop()) {
					toDelete.clear();
					return;
				}
			}
		};

		Runnable finishSwingTask = new Runnable() {

			@Override
			public void run() {
				session.getGraph().deleteAll(toDelete);
			}
		};
		BackgroundTaskHelper.issueSimpleTask(getName(), "", exec, finishSwingTask, status);
	}

	/**
	 * Depending on direction, an edge, i.e. pair of source and target node IDs, is
	 * tested against a known set of non-parallel edges.
	 * 
	 * @param antiParallelEdges a HashSet with {@link Pair}s representing known
	 *                          edges
	 * @param srcTrg            a pair of source and target node IDs
	 * @return true, given the tested edge is parallel
	 */
	private boolean isParallelEdge(HashSet<Pair<Long, Long>> antiParallelEdges, Pair<Long, Long> srcTrg) {
		if (!undirected)
			return antiParallelEdges.contains(srcTrg);
		else
			return antiParallelEdges.contains(srcTrg)
					|| antiParallelEdges.contains(new Pair<Long, Long>(srcTrg.getSnd(), srcTrg.getFst()));
	}

	@Override
	public String getName() {
		return "Remove Parallel Edges";
	}

	@Override
	public String getDescription() {
		return "<html>The following algorithm removes all parallel edges,<br>"
				+ "keeping one edge for each set of adjacent parallel edges.<br>";
	}

	@Override
	public String getCategory() {
		return "Network.Edges";
	}

	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(Arrays.asList(Category.GRAPH, Category.EDGE));
	}
}
