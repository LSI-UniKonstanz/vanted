package org.vanted.addons.multilevelframework;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.AttributeHelper;
import org.Vector2d;
import org.graffiti.graph.Node;
import org.graffiti.plugin.parameter.Parameter;

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

		final InternalGraph top = (InternalGraph) multilevelGraph.popCoarseningLevel();

		@SuppressWarnings("unchecked")
		final Set<Node> suns = (Set<Node>) getElement(top, SolarMerger.SUNS_KEY);
		@SuppressWarnings("unchecked")
		final Set<Node> allPlanets = (Set<Node>) getElement(top, SolarMerger.PLANETS_KEY);
		@SuppressWarnings("unchecked")
		final Set<Node> allMoons = (Set<Node>) getElement(top, SolarMerger.MOONS_KEY);
		@SuppressWarnings("unchecked")
		final HashMap<Node, Set<Node>> sunToPlanets = (HashMap<Node, Set<Node>>) getElement(top,
				SolarMerger.SUN_TO_PLANETS_KEY);
		@SuppressWarnings("unchecked")
		final HashMap<Node, Set<Node>> planetToMoons = (HashMap<Node, Set<Node>>) getElement(top,
				SolarMerger.PLANET_TO_MOONS_KEY);

		HashMap<Node, Node> moonToPlanet = new HashMap<>();
		for (Entry<Node, Set<Node>> planetWMoons : planetToMoons.entrySet()) {
			Node planet = planetWMoons.getKey();
			for (Node moon : planetWMoons.getValue()) {
				moonToPlanet.put(moon, planet);
			}
		}

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

		// Place suns first
		for (MergedNode solarSystem : top.getMergedNodes()) {
			Vector2d center = AttributeHelper.getPositionVec2d(solarSystem);
			for (Node stellarBody : solarSystem.getInnerNodes()) {
				if (suns.contains(stellarBody)) {
					AttributeHelper.setPosition(stellarBody, center);
					break;
				}
			}
		}

		for (Node sun : suns) {
			Vector2d center = AttributeHelper.getPositionVec2d(sun);
			// TODO check if instead of computing very Planet and Moon separately, using
			// paths is faster
			Set<Node> planets = sunToPlanets.get(sun);
			if (planets != null) {
				for (Node planet : planets) {
					// determine how many intra-system connections lead to a planet
					Set<Node> neighbors = planet.getNeighbors(); // all neighbors
					neighbors.remove(sun); // minus the sun
					neighbors.removeAll(planetToMoons.get(planet)); // minus all moons
					neighbors.removeAll(planets); // minus all inter-system connections

					int deg = neighbors.size(); // amount of remaining connections. These are all intra-system
												// connections

					if (deg == 0) { // no intra-system connections
						double angle = Math.random() * Math.PI * 2;
						double x = Math.cos(angle) * (fakeZeroEngergyLength / 3) + center.x;
						double y = Math.sin(angle) * (fakeZeroEngergyLength / 3) + center.y;
						AttributeHelper.setPosition(planet, x, y);
					} else { // connected to other solar system(s)
						double planetPositionX = 0.0;
						double planetPositionY = 0.0;
						for (Node neighbor : neighbors) {
							double lambda = 0.0;
							if (suns.contains(neighbor)) { // neighbor is a sun
								lambda = 1.0 / 2.0;
							} else {
								if (allPlanets.contains(neighbor)) { // neighbor is a planet
									lambda = 1.0 / 3.0;
								} else {
									if (allMoons.contains(neighbor)) { // neighbor is a moon
										lambda = 1.0 / 4.0;
									}
								}
							}
							Vector2d t = AttributeHelper.getPositionVec2d(neighbor);
							planetPositionX += center.x - (lambda * (t.x - center.x));
							planetPositionY += center.y - (lambda * (t.y - center.y));
						}
						AttributeHelper.setPosition(planet, planetPositionX / deg, planetPositionY / deg);
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
//	private static <T> T getElement(InternalGraph internalGraph, String key, Class<T> clazz) {
//		final Object tmp = internalGraph.getObject(key).orElseThrow(
//				() -> new IllegalArgumentException("SolarPlacer can only be run on graphs merged by Solar Merger."));
//		if (!clazz.isInstance(tmp)) {
//			throw new IllegalStateException("Invalid object found at key \"" + key + "\"");
//		}
//		return clazz.cast(tmp);
//	}
	private static Object getElement(InternalGraph internalGraph, String key) {
		final Object tmp = internalGraph.getObject(key).orElseThrow(
				() -> new IllegalArgumentException("SolarPlacer can only be run on graphs merged by Solar Merger."));
		return tmp;
	}
}
