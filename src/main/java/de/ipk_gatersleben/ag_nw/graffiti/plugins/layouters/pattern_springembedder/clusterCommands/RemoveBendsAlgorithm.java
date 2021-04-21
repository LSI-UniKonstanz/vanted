/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.graffiti.graph.Edge;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;

public class RemoveBendsAlgorithm extends AbstractAlgorithm {
	
	public String getName() {
		return "Remove Bends";
	}
	
	@Override
	public String getCategory() {
		return "Network.Edges.Bends";
	}
	
	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(Arrays.asList(Category.GRAPH, Category.EDGE, Category.VISUAL));
	}
	
	@Override
	public Parameter[] getParameters() {
		/*
		 * return new Parameter[] { Integer.valueOfParameter(minNodeSize, "Min. Node-Size",
		 * "The minimum size of a node"), Integer.valueOfParameter(maxNodeSize,
		 * "Max. Node-Size", "The maximum size of a node") };
		 */
		return null;
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		/*
		 * int i = 0; minNodeSize = ((IntegerParameter)
		 * params[i++]).getInteger().intValue(); maxNodeSize = ((IntegerParameter)
		 * params[i++]).getInteger().intValue();
		 */
	}
	
	@Override
	public void check() throws PreconditionException {
		if (graph == null)
			throw new PreconditionException("No graph available!");
		if (graph.getEdges().size() <= 0)
			throw new PreconditionException("Graph contains no edges!");
	}
	
	public void execute() {
		ArrayList<Edge> workEdges = new ArrayList<Edge>();
		if (selection == null || selection.getEdges().size() == 0)
			workEdges.addAll(graph.getEdges());
		else
			workEdges.addAll(selection.getEdges());
		GraphHelper.removeBends(graph, workEdges, true);
	}
	
	@Override
	public boolean mayWorkOnMultipleGraphs() {
		return true;
	}
	
}