/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.Release;
import org.ReleaseInfo;
import org.graffiti.graph.GraphElement;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.parameter.StringParameter;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;

/**
 * @vanted.revision 2.7.0 Added Undo/Redo
 */
public class SetClusterInfoAlgorithm extends AbstractAlgorithm {
	
	private String currentValue = "";
	
	@Override
	public String getName() {
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			return null;
		
		return "Enter Cluster ID";
	}
	
	@Override
	public String getDescription() {
		return "<html>" + "Assign/Modify cluster ID for<br>" + "selected or all nodes, given none are selected.";
	}
	
	@Override
	public Parameter[] getParameters() {
		try {
			HashSet<String> ids = new HashSet<String>();
			for (GraphElement ge : getSelectedOrAllGraphElements()) {
				ids.add(NodeTools.getClusterID(ge, ""));
			}
			currentValue = "";
			for (String s : ids) {
				if (currentValue.length() > 0)
					currentValue = currentValue + " / " + s;
				else
					currentValue = s;
			}
		} catch (Exception e) {
			// ignore
		}
		return new Parameter[] { new StringParameter(currentValue, "Cluster ID", null) };
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		currentValue = ((StringParameter) params[i++]).getString();
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
		graph.getListenerManager().transactionStarted(this);
		try {
			HashMap<GraphElement, String> ge2newClusterID = new HashMap<>();
			getSelectedOrAllGraphElements().forEach(ge -> ge2newClusterID.put(ge, currentValue));
			GraphHelper.applyUndoableClusterIdAssignment(graph, ge2newClusterID, getName(), true);
		} finally {
			graph.getListenerManager().transactionFinished(this);
		}
	}
	
	@Override
	public boolean mayWorkOnMultipleGraphs() {
		return true;
	}
}