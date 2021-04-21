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
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.GraphElement;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.algorithm.PreconditionException;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;

/**
 * @vanted.revision 2.7.0
 */
public class SetClusterInfoFromLabelAlgorithm extends AbstractAlgorithm {
	
	@Override
	public String getName() {
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			return null;
		
		return "Copy Cluster ID from Label";
	}
	
	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(Arrays.asList(Category.GRAPH, Category.ANNOTATION, Category.CLUSTER));
	}
	
	@Override
	public void check() throws PreconditionException {
		if (graph == null)
			throw new PreconditionException("No graph available!");
		if (graph.getNumberOfNodes() < 1)
			throw new PreconditionException("Graph contains no graph elements!");
	}
	
	public void execute() {
		HashMap<GraphElement, String> ge2newClusterID = new HashMap<>();
		getSelectedOrAllGraphElements().forEach(ge -> ge2newClusterID.put(ge, AttributeHelper.getLabel(ge, "")));
		GraphHelper.applyUndoableClusterIdAssignment(graph, ge2newClusterID, getName(), true);
		
		MainFrame.showMessage(
				"<html><b>Hint:</b> Commands dealing with alternative substance IDs from"
						+ " the Mapping menu may be useful to temporarily modify labels, before using this command.",
				MessageType.INFO, 15000);
	}
	
	public boolean mayWorkOnMultipleGraphs() {
		return true;
	}
	
}