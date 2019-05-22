package org.vanted.addons.stressminaddon;

import org.graffiti.plugin.algorithm.AbstractEditorAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.view.View;

/**
 * Just a small class to show how you can add layout add-ons. Implements the
 * usual {@link AbstractEditorAlgorithm}.
 * 
 * @author Christian Klukas
 */
public class StressMinimizationLayout extends AbstractEditorAlgorithm {
	
	private double defaultRadius = 250;
	
	/**
	 * Creates a new {@link StressMinimizationLayout} object.
	 */
	public StressMinimizationLayout() {
		super();
	}
	
	@Override
	public String getDescription() {
		return ""; // TODO
	}
	
	/**
	 * Returns the name of the algorithm.
	 * 
	 * @return the name of the algorithm
	 */
	public String getName() {
		return "Stress Minimization";
	}
	
	/**
	 * Checks, if a graph was given.
	 * 
	 * @throws PreconditionException
	 *            if no graph was given during algorithm invocation or the
	 *            radius is negative
	 */
	@Override
	public void check() throws PreconditionException {
		if (graph == null) {
			throw new PreconditionException("No graph available!");
		}
		if (graph.getNumberOfNodes() <= 0) {
			throw new PreconditionException(
								"The graph is empty. Cannot run layouter.");
		}
	}
	
	/**
	 * Performs the layout.
	 */
	public void execute() {
		/* TODO */
	}
	
	@Override
	public Parameter[] getParameters() {
		// TODO
	    return null;
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		// TODO
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#getCategory()
	 */
	@Override
	public String getCategory() {
		return "Layout";
	}
	
	/**
	 * This method is important, because it will move the algorithm to the layout-tab of Vanted
	 */
	@Override
	public boolean isLayoutAlgorithm() {
		return true;
	}
	
	public boolean activeForView(View v) {
		return v != null;
	}
}
