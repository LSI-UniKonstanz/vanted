package org.vanted.addons.multilevelframework;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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

		// generate Map of moons to their respective planets
		HashMap<Node, Node> moonToPlanet = new HashMap<>();
		for (Entry<Node, Set<Node>> planetWMoons : planetToMoons.entrySet()) {
			Node planet = planetWMoons.getKey();
			for (Node moon : planetWMoons.getValue()) {
				moonToPlanet.put(moon, planet);
			}
		}

		// generate Map of planets to their respective suns
		HashMap<Node, Node> planetToSun = new HashMap<>();
		for (Entry<Node, Set<Node>> sunWPlanets : sunToPlanets.entrySet()) {
			Node sun = sunWPlanets.getKey();
			for (Node planet : sunWPlanets.getValue()) {
				planetToSun.put(planet, sun);
			}
		}

		// generate Map of suns to their respective solar systems
		HashMap<Node, MergedNode> sunToSolarSystem = new HashMap<>();
		for (MergedNode solarSystem : top.getMergedNodes()) {
			for (Node stellar : solarSystem.getInnerNodes()) {
				if (suns.contains(stellar)) {
					sunToSolarSystem.put(stellar, solarSystem);
				}
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
		for (Node sun : suns) {
			Vector2d center = AttributeHelper.getPositionVec2d(sunToSolarSystem.get(sun));
			AttributeHelper.setPosition(sun, center);
		}

		HashSet<Node> allBodies = new HashSet<>();
		if (allPlanets != null) {
			allBodies.addAll(allPlanets);
		}
		if (allMoons != null) {
			allBodies.addAll(allMoons);
		}

		for (Node planet : allBodies) {
			// get A collection of all stellar bodies in the same solar system as the
			// current planet
			@SuppressWarnings("unchecked")
			Collection<Node> issb = (Collection<Node>) sunToSolarSystem.get(planetToSun.get(planet)).getInnerNodes();
			// calculate the amount of intra-solar-system connections
			Set<Node> neighbors = planet.getNeighbors(); // all neighbors
			neighbors.removeAll(issb); // remove all inter-solar-system bodies
			int essc = neighbors.size(); // number of intra-solar-system connections

			Vector2d center = AttributeHelper.getPositionVec2d(sunToSolarSystem.get(planetToSun.get(planet)));
			if (essc == 0) { // no intra-solar-system connections. Place Planets randomly around sun
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
						if (allPlanets != null) {
							if (allPlanets.contains(neighbor)) { // neighbor is a planet
								lambda = 1.0 / 3.0;
							} else {
								if (allMoons != null) {
									if (allMoons.contains(neighbor)) { // neighbor is a moon
										lambda = 1.0 / 4.0;
									}
								}
							}
						}
					}
					Vector2d t = AttributeHelper.getPositionVec2d(neighbor);
					planetPositionX += center.x - (lambda * (t.x - center.x));
					planetPositionY += center.y - (lambda * (t.y - center.y));
				}
				AttributeHelper.setPosition(planet, planetPositionX / essc, planetPositionY / essc);
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
		if (!internalGraph.getObject(SolarMerger.SUNS_KEY).isPresent()) {
			throw new IllegalArgumentException("SolarPlacer can only be run on graphs merged by Solar Merger.");
		}
		try {
			final Object tmp = internalGraph.getObject(key).get();
			return tmp;
		} catch (Exception e) {
			return null;
		}
	}
}
