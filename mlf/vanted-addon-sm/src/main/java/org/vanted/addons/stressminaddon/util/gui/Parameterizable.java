package org.vanted.addons.stressminaddon.util.gui;

import org.graffiti.plugin.parameter.Parameter;

/**
 * Implementing classes provide and can accept different
 * {@link Parameter}s.
 *
 * @see org.graffiti.plugin.algorithm.Algorithm#getParameters()
 * @see org.graffiti.plugin.algorithm.Algorithm#setParameters(Parameter[])
 *
 * @author Jannik
 */
public interface Parameterizable {

    /**
     * Gets a list of {@link Parameter}s this class provides.
     * They will be modified and returned to the class.
     *
     * @return a list of Parameters this class provides.
     *
     * @author Jannik
     *
     * @see org.graffiti.plugin.algorithm.Algorithm#getParameters()
     */
    public Parameter[] getParameters();


    /**
     * Provides a list of {@link Parameter}s that should be accepted by
     * the implementing class.<br>
     * This setter should only be called if the implementing class is not currently
     * executing something else.
     *
     * @param parameters
     *      the parameters to be set.
     *
     * @author Jannik
     *
     * @see org.graffiti.plugin.algorithm.Algorithm#setParameters(Parameter[])
     */
    public void setParameters(final Parameter[] parameters);
}
