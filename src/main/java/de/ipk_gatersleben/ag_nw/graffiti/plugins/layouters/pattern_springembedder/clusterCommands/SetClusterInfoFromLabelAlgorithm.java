/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.AttributeHelper;
import org.Release;
import org.ReleaseInfo;
import org.graffiti.graph.GraphElement;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.algorithm.PreconditionException;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;

public class SetClusterInfoFromLabelAlgorithm extends AbstractAlgorithm {

	@Override
	public String getName() {
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			return null;
		else
			return "Copy Cluster IDs from Labels";
	}

	@Override
	public String getDescription() {
		return "<html>" + "Undoable operation, continue?<br><br><br>"
				+ "Hint: Commands dealing with alternative substance IDs, available from<br>"
				+ "the Mapping menu may be useful to temporarily modify labels, before<br>"
				+ "application of this command.";
	}

	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(Arrays.asList(Category.GRAPH, Category.ANNOTATION, Category.CLUSTER));
	}

	@Override
	public void check() throws PreconditionException {
		if (graph == null)
			throw new PreconditionException("No graph available!");
		if (graph.getNumberOfNodes() <= 0)
			throw new PreconditionException("Graph contains no graph elements!");
	}

	public void execute() {
		HashMap<GraphElement, String> ge2newClusterID = new HashMap<GraphElement, String>();
		for (GraphElement ge : getSelectedOrAllGraphElements()) {
			String lbl = AttributeHelper.getLabel(ge, "");
			ge2newClusterID.put(ge, lbl);
		}
		GraphHelper.applyUndoableClusterIdAssignment(graph, ge2newClusterID, getName(), true);
	}

	public boolean mayWorkOnMultipleGraphs() {
		return true;
	}

}