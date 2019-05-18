package org.vanted.addons.MultilevelFramework;

import java.awt.event.ActionEvent;
import java.util.Set;

import javax.swing.KeyStroke;

import org.graffiti.graph.Graph;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.selection.Selection;

public class RandomPlacementAlgorithm implements PlacementAlgorithm {

	@Override
	public String getName() {
		return "Random Placement";
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
		return false;
	}

	@Override
	public boolean showMenuIcon() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public KeyStroke getAcceleratorKeyStroke() {
		// TODO Auto-generated method stub
		return null;
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
	public boolean mayWorkOnMultipleGraphs() {
		// TODO Auto-generated method stub
		return false;
	}

}
