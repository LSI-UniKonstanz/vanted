/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.davidtest;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.IntegerParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.selection.Selection;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.statistics.TabStatistics;

/**
 * Executes the David et. al statistical quicktest.
 * 
 * @version 2.6.5
 */
public class DavidTestAlgorithm extends AbstractAlgorithm {
	
	/** Significance level (1..3 are valid). */
	private int probab_123 = 1;
	
	public String getName() {
		return "David et al. Quicktest (check for normal distribution)";
	}
	
	@Override
	public String getCategory() {
		return "Mapping.Statistical Analysis";
	}
	
	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(Arrays.asList(Category.DATA, Category.ANALYSIS, Category.STATISTICS));
	}
	
	@Override
	public String getDescription() {
		return "<html><b>David et al. Quicktest for normality distribution.</b><br>"
				+ "All nodes that contain samples and are <b>not</b><br>"
				+ "normally distributed, will be <b>selected</b>.<br>"
				+ "<small>Each sample needs to contain at least 5 values<br>"
				+ "and at most 500 values!</small><br><br>Specify the alpha value (P):";
	}
	
	public void execute() {
		
		Selection sel = new Selection("id");
		
		List<Node> notNormalyDistributedNodes = TabStatistics
				.doDavidSchnellTest(GraphHelper.getSelectedOrAllNodes(selection, graph), graph, probab_123);
		
		sel.addAll(notNormalyDistributedNodes);
		
		graph.getListenerManager().transactionStarted(this);
		
		MainFrame.getInstance().getActiveEditorSession().getSelectionModel().setActiveSelection(sel);
		
		graph.getListenerManager().transactionFinished(this);
	}
	
	@Override
	public Parameter[] getParameters() {
		return new Parameter[] { new IntegerParameter(probab_123, 1, 3, "P (1=5%, 2=1%, 3=0.1%)",
				"Select one of the significance levels (1/2/3)") };
	}
	
	@Override
	public boolean isLayoutAlgorithm() {
		return false;
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		IntegerParameter ip = (IntegerParameter) params[0];
		probab_123 = ip.getInteger().intValue();
	}
	
	@Override
	public void check() throws PreconditionException {
		super.check();
		if (graph == null || graph.getNodes().size() <= 0)
			throw new PreconditionException("No graph available or graph empty!");
	}
	
	@Override
	public void reset() {
		super.reset();
	}
	
}
