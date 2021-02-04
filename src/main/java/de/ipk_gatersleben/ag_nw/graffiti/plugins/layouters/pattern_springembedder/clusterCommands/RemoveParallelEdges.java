package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
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

		Runnable exec = new Runnable() {
			@Override
			public void run() {
				HashMap<Long, Long> srcTrg = new HashMap<>();
				Collection<Edge> workingEdges = null;
				if (selection == null || selection.isEmpty()) {
					workingEdges = session.getGraph().getEdges();
				} else if (selection.getEdges().isEmpty()) { // selected: only nodes
					HashSet<Edge> edges = new HashSet<>();
					for (Node n : selection.getNodes()) {
						for (Edge e : n.getEdges()) {
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
					if (isParallel(srcTrg, src, trg)) {
						toDelete.add(e);
						continue;
					}
					srcTrg.put(src, trg);
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

	private boolean isParallel(HashMap<Long, Long> srcTrg, Long src, Long trg) {
		if (!undirected) {
			if (srcTrg.containsKey(src) && srcTrg.get(src).equals(trg))
				return true;
			else
				return false;
		} else {
			if ((srcTrg.containsKey(src) && srcTrg.get(src).equals(trg))
					|| (srcTrg.containsKey(trg) && srcTrg.get(trg).equals(src)))
				return true;
			else
				return false;
		}
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
