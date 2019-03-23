/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.AttributeHelper;
import org.ErrorMsg;
import org.Release;
import org.ReleaseInfo;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Graph;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.session.EditorSession;

public class ShowClusterGraphAlgorithm extends AbstractAlgorithm {

	public String getName() {
		if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR)
			return "Show Overview Graph";
		else
			return null;
	}

	@Override
	public String getCategory() {
		// return "Analysis";
		return "Cluster";
	}

	@Override
	public String getMenuCategory() {
		return "Network.Cluster.Process Cluster Overview-Graph";
	}

	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(Arrays.asList(Category.CLUSTER, Category.GRAPH));
	}

	@Override
	public void check() throws PreconditionException {
		super.check();
		Graph emptyGraph = new AdjListGraph();
		Graph clusterGraph = (Graph) AttributeHelper.getAttributeValue(graph, "cluster", "clustergraph", emptyGraph,
				new AdjListGraph(), false);
		if (clusterGraph.equals(emptyGraph)) {
			throw new PreconditionException("No overview-graph available!<br>"
					+ "Please load a graph file with cluster-information (e.g. a PAJEK file),<br>"
					+ "or do a Cluster-Analysis to add Cluster-Information to this graph.<br>"
					+ "Then use the command <i>" + new CreateClusterGraphAlgorithm().getName() + "</i>,"
					+ "<br>to create and layout a overview-graph.");
		}
	}

	public void execute() {
		MainFrame mf = GravistoService.getInstance().getMainFrame();
		Graph emptyGraph = new AdjListGraph();
		Graph clusterGraph = (Graph) AttributeHelper.getAttributeValue(graph, "cluster", "clustergraph", emptyGraph,
				new AdjListGraph(), false);
		if (clusterGraph.equals(emptyGraph)) {
			ErrorMsg.addErrorMessage(
					"Internal Error: No Overview-Graph Available, <b>check</b> was not called before <b>execute</b>.");
		} else {
			EditorSession es = new EditorSession(clusterGraph);
			// es.setFileName(file.toURI());
			mf.showViewChooserDialog(es, false, getActionEvent());
		}
	}

}