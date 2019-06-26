package org.vanted.addons.multilevelframework;

import org.AttributeHelper;
import org.graffiti.graph.Node;
import org.graffiti.plugin.parameter.Parameter;

import java.util.Collection;
import java.util.HashMap;
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
	 * @author Gordian, Katze
	 */
	@Override
	public void reduceCoarseningLevel(MultilevelGraph multilevelGraph) {
//        final InternalGraph top = (InternalGraph) multilevelGraph.getTopLevel();
		// not sure if this even works...
		// could also just cast directly instead of using class objects if not...
//        @SuppressWarnings("unchecked")
//        final Class<Set<Node>> snClass = (Class<Set<Node>>) (Class<?>) Set.class;
//        @SuppressWarnings("unchecked")
//        final Class<HashMap<Node, Set<List<Node>>>> mnslnClass = (Class<HashMap<Node, Set<List<Node>>>>) (Class<?>) HashMap.class;
//        @SuppressWarnings("unchecked")
//        final Class<HashMap<Node, Set<Node>>> mnsnClass = (Class<HashMap<Node, Set<Node>>>) (Class<?>) HashMap.class;
//        final Set<Node> suns = getElement(top, SolarMerger.SUNS_KEY, snClass);
//        final HashMap<Node, Set<List<Node>>> nodeToPaths = getElement(top, SolarMerger.NODE_TO_PATHS_KEY, mnslnClass);
//        final HashMap<Node, Set<Node>> sunToPlanets = getElement(top, SolarMerger.SUN_TO_PLANETS_KEY, mnsnClass);

		final InternalGraph top = (InternalGraph) multilevelGraph.popCoarseningLevel();

		@SuppressWarnings("unchecked")
		final Class<Set<Node>> snClass = (Class<Set<Node>>) (Class<?>) Set.class;
		@SuppressWarnings("unchecked")
		final Class<HashMap<Node, Set<Node>>> snpnClass = (Class<HashMap<Node, Set<Node>>>) (Class<?>) Set.class;
		@SuppressWarnings("unchecked")
		final Class<HashMap<Node, Set<Node>>> pnmnClass = (Class<HashMap<Node, Set<Node>>>) (Class<?>) Set.class;
		final Set<Node> suns = getElement(top, SolarMerger.SUNS_KEY, snClass);
		final HashMap<Node, Set<Node>> sunToPlanets = getElement(top, SolarMerger.SUN_TO_PLANETS_KEY, snpnClass);
		final HashMap<Node, Set<Node>> planetToMoons = getElement(top, SolarMerger.PLANET_TO_MOONS_KEY, pnmnClass);

		// calculate makeshift zeroEnergyLength
		double fakeZeroEngergyLength = Double.POSITIVE_INFINITY;
		for (Node sun : suns) {
			for (Node neighbor : sun.getNeighbors()) {
				double distance = Math.sqrt(
						Math.pow(AttributeHelper.getPositionX(sun) - AttributeHelper.getPositionX(neighbor), 2) + Math
								.pow(AttributeHelper.getPositionY(sun) - AttributeHelper.getPositionY(neighbor), 2));
				if (distance < fakeZeroEngergyLength) {
					fakeZeroEngergyLength = distance;
				}
			}
		}

		for (MergedNode node : top.getMergedNodes()) {
			double centerX = AttributeHelper.getPositionX(node);
			double centerY = AttributeHelper.getPositionY(node);

			Collection<? extends Node> innerNodes = node.getInnerNodes();
			Node sun = innerNodes.iterator().next(); // every first Node is the sun Node
			AttributeHelper.setPosition(sun, centerX, centerY); // place the sun node at the position of the collapsed
																// solar system

			Set<Node> planets = sunToPlanets.get(sun);
			for (Node planet : planets) {
				// determine how many intra-system connections lead to a planet
				Set<Node> neighbors = planet.getNeighbors(); // all neighbors
				neighbors.remove(sun); // minus the sun
				neighbors.removeAll(planetToMoons.get(planet)); // minus all moons

				for (Node neighbor : neighbors) {
					if (planets.contains(neighbor)) {
						neighbors.remove(neighbor);
					} // minus all inter-system connections
				}

				int deg = neighbors.size(); // amount of remaining connections. These are all intra-system connections

				if (deg == 0) { // no intra-system connections
					double angle = Math.random() * Math.PI * 2;
					double x = Math.cos(angle) * (fakeZeroEngergyLength / 3) + centerX;
					double y = Math.sin(angle) * (fakeZeroEngergyLength / 3) + centerY;
					AttributeHelper.setPosition(planet, x, y);
				} else { // connected to other solar system(s)
					for (Node neighbor : neighbors) {
						if (suns.contains(neighbor)) { // neighbor is a sun
							// lambda = 1/2
						}
						else {
							for (Node otherSun : suns) {
								if(sunToPlanets.get(otherSun).contains(neighbor)) {
									// lambda = 1/3
								}
								else {
									for (Node otherPlanet : sunToPlanets.get(sun)) {
										if (planetToMoons.get(otherPlanet).contains(neighbor)) {
											// lambda = 1/4
										}
									}
								}
							}
						}
					}
				}
			}
		}
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
	 * Get an object attribute cast into the correct type. Throws an exception if it
	 * doesn't work.
	 * 
	 * @param internalGraph The {@link InternalGraph} to get the attribute from.
	 *                      Must not be {@code null}.
	 * @param key           The key to get.
	 * @param clazz         The class of the return type.
	 * @param               <T> The return type.
	 * @return The object returned by {@link InternalGraph#getObject(String)}, cast
	 *         to {@code T}.
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
