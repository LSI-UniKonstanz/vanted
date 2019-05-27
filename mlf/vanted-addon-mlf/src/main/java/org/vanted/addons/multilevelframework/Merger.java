package org.vanted.addons.multilevelframework;

import org.graffiti.plugin.parameter.Parameter;

/**
 * Constructs coarsening levels.
 * @see MultilevelGraph
 */
public interface Merger {
    /**
     * Settings (parameters) for the {@link Merger}.
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
     * Create all coarsening levels. The levels are stored within
     * the {@link MultilevelGraph} passed as an argument.
     * @param multilevelGraph
     *     The {@link MultilevelGraph} that contains the original graph to build
     *     the coarsening levels.
     */
    void buildCoarseningLevels(MultilevelGraph multilevelGraph);
}
