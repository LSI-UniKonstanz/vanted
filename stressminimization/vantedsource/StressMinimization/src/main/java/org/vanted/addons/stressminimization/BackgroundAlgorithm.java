package org.vanted.addons.stressminimization;

import org.graffiti.plugin.algorithm.Algorithm;

/**
 * interface of algorithms which can be executed in the background
 * using the class BackgroundExecutionAlgorithm
 *
 */
public interface BackgroundAlgorithm extends Algorithm{
	/**
	 * set the BackgroundExecutionAlgorithm to be able to update 
	 * the status and the layout of the running algorithm  
	 * @param bea
	 */
	public void setBackgroundExecutionAlgorithm(BackgroundExecutionAlgorithm bea);
}
