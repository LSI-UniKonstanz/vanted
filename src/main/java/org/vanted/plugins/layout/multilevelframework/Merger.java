package org.vanted.plugins.layout.multilevelframework;

import org.graffiti.plugin.parameter.Parameter;
import org.vanted.plugins.layout.multilevelframework.Describable;
import org.vanted.plugins.layout.multilevelframework.Parameterizable;

/**
 * Constructs coarsening levels.
 * 
 * @see MultilevelGraph
 */
public interface Merger extends Describable, Parameterizable {
	/**
	 * Settings (parameters) for the {@link Merger}.
	 * 
	 * @return an array of {@link Parameter}s
	 * @see Parameterizable#getParameters()
	 * @see org.graffiti.plugin.algorithm.Algorithm#getParameters()
	 */
	@Override
	Parameter[] getParameters();
	
	/**
	 * Called by the Multilevel Framework when the user updates the parameters.
	 * 
	 * @param parameters
	 *           The updated {@link Parameter}.
	 * @see Parameterizable#setParameters(Parameter[])
	 * @see org.graffiti.plugin.algorithm.Algorithm#setParameters(Parameter[])
	 */
	@Override
	void setParameters(Parameter[] parameters);
	
	/**
	 * Create all coarsening levels. The levels are stored within
	 * the {@link MultilevelGraph} passed as an argument.
	 * 
	 * @param multilevelGraph
	 *           The {@link MultilevelGraph} that contains the original graph to build
	 *           the coarsening levels.
	 */
	void buildCoarseningLevels(MultilevelGraph multilevelGraph);
}
