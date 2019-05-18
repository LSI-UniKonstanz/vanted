package org.vanted.addons.MultilevelFramework;

import java.awt.event.ActionEvent;
import java.util.Set;

import javax.swing.JComponent;

import org.graffiti.graph.Graph;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.algorithm.ThreadSafeAlgorithm;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.selection.Selection;

public class MultilevelLayoutAlgorithm extends ThreadSafeAlgorithm {

	@Override
	public String getName() {
		return "Multilevel Layout";
	}

	@Override
	public void setParameters(Parameter[] params) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Parameter[] getParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void attach(Graph g, Selection selection) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void check() throws PreconditionException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void execute() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getCategory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Category> getSetCategory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMenuCategory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isLayoutAlgorithm() {
		return true;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setActionEvent(ActionEvent a) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ActionEvent getActionEvent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean setControlInterface(ThreadSafeOptions options, JComponent jc) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void executeThreadSafe(ThreadSafeOptions options) {
		// TODO Auto-generated method stub
	}

	@Override
	public void resetDataCache(ThreadSafeOptions options) {
		// TODO Auto-generated method stub
	}

}
