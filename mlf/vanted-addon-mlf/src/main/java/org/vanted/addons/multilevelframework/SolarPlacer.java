package org.vanted.addons.multilevelframework;

import org.graffiti.graph.Node;
import org.graffiti.plugin.parameter.Parameter;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class SolarPlacer implements Placer {
    /**
     * @see Placer#getParameters()
     * @author Gordian
     */
    @Override
    public Parameter[] getParameters() {
        return new Parameter[0];
    }

    /**
     * @see Placer#setParameters(Parameter[])
     * @author Gordian
     */
    @Override
    public void setParameters(Parameter[] parameters) {

    }

    /**
     * @see Placer#reduceCoarseningLevel(MultilevelGraph)
     * @author Gordian
     */
    @Override
    public void reduceCoarseningLevel(MultilevelGraph multilevelGraph) {
        final InternalGraph top = (InternalGraph) multilevelGraph.getTopLevel();
        // not sure if this even works...
        // could also just cast directly instead of using class objects if not...
        @SuppressWarnings("unchecked")
        final Class<Set<Node>> snClass = (Class<Set<Node>>) (Class<?>) Set.class;
        @SuppressWarnings("unchecked")
        final Class<HashMap<Node, Set<List<Node>>>> mnslnClass = (Class<HashMap<Node, Set<List<Node>>>>) (Class<?>) HashMap.class;
        @SuppressWarnings("unchecked")
        final Class<HashMap<Node, Set<Node>>> mnsnClass = (Class<HashMap<Node, Set<Node>>>) (Class<?>) HashMap.class;
        final Set<Node> suns = getElement(top, SolarMerger.SUNS_KEY, snClass);
        final HashMap<Node, Set<List<Node>>> nodeToPaths = getElement(top, SolarMerger.NODE_TO_PATHS_KEY, mnslnClass);
        final HashMap<Node, Set<Node>> sunToPlanets = getElement(top, SolarMerger.SUN_TO_PLANETS_KEY, mnsnClass);
    }

    /**
     * @see org.vanted.addons.multilevelframework.sm_util.gui.Describable#getName()
     * @author Gordian
     */
    @Override
    public String getName() {
        return "Solar Placer"; // TODO
    }

    /**
     * @see org.vanted.addons.multilevelframework.sm_util.gui.Describable#getDescription()
     * @author Gordian
     */
    @Override
    public String getDescription() {
        return "Solar placer"; // TODO
    }

    /**
     * Get an object attribute cast into the correct type. Throws an exception if it doesn't work.
     * @param internalGraph
     *     The {@link InternalGraph} to get the attribute from. Must not be {@code null}.
     * @param key
     *      The key to get.
     * @param clazz
     *      The class of the return type.
     * @param <T>
     *      The return type.
     * @return
     *      The object returned by {@link InternalGraph#getObject(String)}, cast to {@code T}.
     * @author Gordian
     */
    private static <T> T getElement(InternalGraph internalGraph, String key, Class<T> clazz) {
        final Object tmp = internalGraph.getObject(SolarMerger.SUNS_KEY).orElseThrow(
                () -> new IllegalArgumentException("SolarPlacer can only be run on graphs merged by Solar Merger."));
        if (!clazz.isInstance(tmp)) {
            throw new IllegalStateException("Invalid object found at key \"" + key + "\"");
        }
        return clazz.cast(tmp);
    }
}
