/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * $Id$
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.shortest_paths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.KeyStroke;

import org.apache.commons.collections.set.ListOrderedSet;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Edge;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;

/**
 * @author klukas
 * @vanted.revision 2.8.0 Remove duplicate parameter, add discription
 */
public class ShortestPathSelectionAlgorithm extends AbstractAlgorithm {
	
	private boolean settingDirected = true;
	private boolean settingIncludeEdges = true;
	
	@Override
	public void check() throws PreconditionException {
		super.check();
		if (selection == null || selection.getNumberOfNodes() < 2)
			throw new PreconditionException("At least one start and one end node have to be selected.");
	}
	
	@Override
	public Parameter[] getParameters() {
		return new Parameter[] {
				new BooleanParameter(settingDirected, "Consider Edge Direction", ""),
				new BooleanParameter(settingIncludeEdges, "Select Edges",
						"If enabled, edges along the shortest path(s) are selected") };
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		settingDirected = ((BooleanParameter) params[i++]).getBoolean();
		settingIncludeEdges = ((BooleanParameter) params[i++]).getBoolean();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void execute() {
		ArrayList<GraphElement> currentSelElements = new ArrayList<GraphElement>();
		graph = MainFrame.getInstance().getActiveEditorSession().getGraph();
		graph.numberGraphElements();
		if (selection != null)
			currentSelElements.addAll(selection.getElements());
		ListOrderedSet targetNodesToBeProcessed = new ListOrderedSet();
		for (GraphElement ge : currentSelElements) {
			if (ge instanceof Node)
				targetNodesToBeProcessed.add((Node) ge);
		}
		for (GraphElement ge : currentSelElements) {
			if (ge instanceof Node) {
				Node n = (Node) ge;
				Collection<GraphElement> shortestPathNodesAndEdges = WeightedShortestPathSelectionAlgorithm
						.getShortestPathElements(graph.getGraphElements(), n, targetNodesToBeProcessed, settingDirected,
								false, false, Double.MAX_VALUE, null, false, false, false);
				for (GraphElement gg : shortestPathNodesAndEdges) {
					if (settingIncludeEdges && (gg instanceof Edge))
						selection.add(gg);
					if (gg instanceof Node)
						selection.add(gg);
				}
				if (!settingDirected)
					targetNodesToBeProcessed.remove(n);
			}
		}
		if (selection != null)
			GraphHelper.selectElements((Collection) selection.getElements());
	}
	
	public String getName() {
		return "Find Shortest Paths (F3)";
	}
	
	@Override
	public KeyStroke getAcceleratorKeyStroke() {
		return KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F3, 0);
	}
	
	@Override
	public String getCategory() {
		return "Network.Analysis";
	}
	
	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(Arrays.asList(Category.GRAPH, Category.SELECTION, Category.ANALYSIS));
	}
	
	@Override
	public String getMenuCategory() {
		return null; // we don't want to appear in the menu
	}
	
	@Override
	public boolean mayWorkOnMultipleGraphs() {
		return false;
	}
	
	@Override
	public String getDescription() {
		return "<html>The algorithm finds shortest path(s) between selected nodes.<br><br>"
				+ "Is edge direction above set accordingly? During execution, edge weights are not considered. "
				+ "Using the F3 shortcut requires the algorithm view to be open.";
	}
}
