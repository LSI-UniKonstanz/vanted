package org.vanted.addons.stressminimization;

import org.graffiti.plugin.algorithm.AbstractEditorAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.view.View;

/**
 * Layout algorithm performing a stress minimization layout process.
 * @author David Boetius
 */
public class StressMinimizationLayout extends AbstractEditorAlgorithm {

	/**
	 * Creates a new StressMinimizationLayout instance.
	 */
	public StressMinimizationLayout() {
		super();
	}
	
	// MARK: presentation control methods

	@Override
	public String getName() {
		return "Stress Minimization";
	}

	@Override
	public String getDescription() {
		// TODO write description, may also be left empty
		return "";
	}

	@Override
	public String getCategory() {
		return "Layout";
	}
	
	@Override
	public boolean isLayoutAlgorithm() {
		return true;
	}
	
	@Override
	public boolean activeForView(View v) {
		return v != null;
	}

	// MARK: algorithm execution
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void check() throws PreconditionException {
		super.check();
		
		if (graph.isEmpty()) {
			throw new PreconditionException("Stress Minimization Layout cannot work on empty graphs");
		}
		if (graph.isDirected()) {
			throw new PreconditionException("Stress Minimization Layout cannot work on directed graphs");
		}
		
	}
	
	@Override
	public Parameter[] getParameters() {
		// TODO Auto-generated method stub
		return super.getParameters();
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		// TODO Auto-generated method stub
		super.setParameters(params);
	}
	
	@Override
	public void execute() {
		
		new StressMajorizationImpl(graph).doLayout();
		
	}
	
	@Override
	public void reset() {
		// TODO Auto-generated method stub
		super.reset();
	}

}
