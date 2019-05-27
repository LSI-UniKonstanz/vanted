package org.vanted.addons.multilevelframework;

import org.graffiti.plugin.parameter.Parameter;

public interface Placer {
    Parameter[] getParameters();
    void setParameters(Parameter[] parameters);
    void reduceCoarseningLevel(MultilevelGraph multilevelGraph);
}
