package org.vanted.addons.MultilevelFramework;

import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.parameter.Parameter;

/**
 * Saves an algorithm together with it's parameters. This is the value
 * determined by an AlgorithmListParameter.
 */
public class AlgorithmWithParameters {
	public Algorithm algorithm;
	public Parameter[] parameters;
	public String name;

	/**
	 * Constructs an AlgorithmWithParameters with a name.
	 * 
	 * @param algorithm the algorithm to be parameterized
	 * @param parameters the parameters for the algorithm
	 * @param name the name the algorithm shall have
	 */
	public AlgorithmWithParameters(Algorithm algorithm, Parameter[] parameters, String name) {
		this.algorithm = algorithm;
		this.parameters = parameters;
		this.name = name;
	}

	/**
	 * Constructs an AlgorithmWithParameters. It's name will be the name of the
	 * algorithm.
	 * 
	 * @param algorithm the algorithm to be parameterized
	 * @param parameters the parameters for the algorithm
	 */
	public AlgorithmWithParameters(Algorithm algorithm, Parameter[] parameters) {
		this(algorithm, parameters, algorithm.getName());
	}
}
