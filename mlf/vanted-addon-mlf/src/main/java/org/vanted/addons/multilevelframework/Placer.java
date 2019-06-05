package org.vanted.addons.multilevelframework;

import org.graffiti.plugin.parameter.Parameter;

/**
 * Deconstructs coarsening levels.
 * @see MultilevelGraph
 */
public interface Placer {
    /**
     * Settings (parameters) for the {@link Placer}.
     * @return an array of {@link Parameter}s
     */
    Parameter[] getParameters();

    /**
     * Called by the Multilevel Framework when the user updates the parameters.
     * @param parameters
     *     The updated {@link Parameter}.
     */
    void setParameters(Parameter[] parameters);

    /**
     * Remove a coarsening level from the {@link MultilevelGraph} by calling
     * {@link MultilevelGraph#popCoarseningLevel()} and but the nodes represented by the {@link MergedNode} at
     * positions close to the {@link MergedNode} they were represented by.
     * @param multilevelGraph
     *      The {@link MultilevelGraph} whose top coarsening level should be deconstructed.
     */
    void reduceCoarseningLevel(MultilevelGraph multilevelGraph);
}
