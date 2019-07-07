package org.vanted.addons.MultilevelFramework.GUI;

import java.util.List;

import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.parameter.AbstractSingleParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.vanted.addons.MultilevelFramework.AlgorithmWithParameters;

/**
 * Parameter allowing the specification of an algorithm out of a list of
 * algorithms along with the selected algorithms parameters. The component
 * displaying this class is AlgorithmListComponent.
 */
public class AlgorithmListParameter extends AbstractSingleParameter {
	protected Algorithm selectedAlgorithm;
	protected Parameter[] selectedAlgorithmParameters;
	protected List<Algorithm> possibleAlgorithms;

	/**
	 * Creates a new AlgorithmListParameter
	 * 
	 * @param name        parameter name
	 * @param description parameter description
	 * @param algorithms  List of available algorithms
	 */
	public AlgorithmListParameter(String name, String description, List<Algorithm> algorithms) {
		super(name, description);
		possibleAlgorithms = algorithms;
	}

	/**
	 * Creates a new AlgorithmListParameter
	 * 
	 * @param selectedAlgorithm Preselected algorithm (has to be an element in
	 *                          algorithms)
	 * @param name              Parameter name
	 * @param description       Parameter description
	 * @param algorithms        List of available algorithms
	 */
	public AlgorithmListParameter(Algorithm selectedAlgorithm, String name, String description,
			List<Algorithm> algorithms) {
		this(name, description, algorithms);
		setValue(new AlgorithmWithParameters(selectedAlgorithm, selectedAlgorithm.getParameters()));
	}

	@Override
	public void setValue(Object value) {
		AlgorithmWithParameters input = (AlgorithmWithParameters) value;
		selectedAlgorithm = input.algorithm;
		selectedAlgorithmParameters = input.parameters;
	}

	@Override
	public Object getValue() {
		return new AlgorithmWithParameters(selectedAlgorithm, selectedAlgorithmParameters, null);
	}

}
