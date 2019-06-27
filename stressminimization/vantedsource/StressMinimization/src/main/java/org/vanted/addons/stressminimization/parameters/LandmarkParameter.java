package org.vanted.addons.stressminimization.parameters;

import org.graffiti.plugin.parameter.IntegerParameter;

public class LandmarkParameter extends IntegerParameter{

	private final int minUsefull;
	private final int maxUsefull;

	public LandmarkParameter(int defaultValue, int minUsefull, int maxUsefull, String name, String description) {
		super(defaultValue, name, description);
		this.minUsefull = minUsefull;
		this.maxUsefull = maxUsefull;
	}

	/**
	 * Returns the maximum value that is a usefull value for the algorithm
	 */
	public int getMaxUsefullValue() {
		return maxUsefull;
	}

	/**
	 * Returns the minimum usefull value for the algorithm.
	 * @return
	 */
	public int getMinUsefullValue() {
		return minUsefull;
	}

}
