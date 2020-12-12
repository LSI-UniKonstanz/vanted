package org.vanted.plugins.layout.multilevelframework;

import org.graffiti.plugin.parameter.Parameter;

/**
 * Implementing classes provide and can accept different {@link Parameter}s. This can be seen as a supertype
 * to {@link org.graffiti.plugin.algorithm.Algorithm}.
 *
 * @author Jannik
 * @see org.graffiti.plugin.algorithm.Algorithm#getParameters()
 * @see org.graffiti.plugin.algorithm.Algorithm#setParameters(Parameter[])
 */
public interface Parameterizable {

    /**
     * Gets a list of {@link Parameter}s this class provides.
     * They will be modified and returned to the class.
     *
     * @return gets a list of Parameters this class provides.
     *
     * @author Jannik
     *
     * @see org.graffiti.plugin.algorithm.Algorithm#getParameters()
     */
    Parameter[] getParameters();


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
    void setParameters(final Parameter[] parameters);
}
