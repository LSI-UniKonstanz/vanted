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
	
	@Override
	public void check() throws PreconditionException {
		// TODO Auto-generated method stub
		super.check();
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
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void reset() {
		// TODO Auto-generated method stub
		super.reset();
	}

}
