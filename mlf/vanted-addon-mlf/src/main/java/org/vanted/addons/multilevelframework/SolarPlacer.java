package org.vanted.addons.multilevelframework;

import org.AttributeHelper;
import org.Vector2d;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Node;
import org.graffiti.plugin.parameter.Parameter;

import java.util.*;
import java.util.Map.Entry;

public class SolarPlacer implements Placer {
    /**
     * @author Gordian
     * @see Placer#getParameters()
     */
    @Override
    public Parameter[] getParameters() {
        return new Parameter[0];
    }

    /**
     * @author Gordian
     * @see Placer#setParameters(Parameter[])
     */
    @Override
    public void setParameters(Parameter[] parameters) {

    }

    /**
     * @author Gordian, Katze
     * @see Placer#reduceCoarseningLevel(MultilevelGraph)
     */
    @Override
    public void reduceCoarseningLevel(MultilevelGraph multilevelGraph) {

        final InternalGraph top = (InternalGraph) multilevelGraph.popCoarseningLevel();

        @SuppressWarnings("unchecked") final Set<Node> suns = (Set<Node>) getElement(top, SolarMerger.SUNS_KEY);
        @SuppressWarnings("unchecked") Set<Node> allPlanets = (Set<Node>) getElement(top, SolarMerger.PLANETS_KEY);
        if (allPlanets == null) {
            allPlanets = Collections.emptySet();
        }
        @SuppressWarnings("unchecked") Map<Node, Set<Node>> sunToPlanets = (HashMap<Node, Set<Node>>) getElement(top,
                SolarMerger.SUN_TO_PLANETS_KEY);
        if (sunToPlanets == null) { sunToPlanets = Collections.emptyMap(); }
        @SuppressWarnings("unchecked") Map<Node, Set<Node>> planetToMoons = (HashMap<Node, Set<Node>>) getElement(top,
                SolarMerger.PLANET_TO_MOONS_KEY);
        if (planetToMoons == null) { planetToMoons = Collections.emptyMap(); }

        final Set<Node> allMoons = new HashSet<>();
        for (Set<Node> moons : planetToMoons.values()) {
            allMoons.addAll(moons);
        }

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

        // Place suns first
        for (Node sun : suns) {
            Vector2d center = AttributeHelper.getPositionVec2d(sunToSolarSystem.get(sun));
            AttributeHelper.setPosition(sun, center);
        }

        // calculate makeshift zeroEnergyLength as the minimum distance between two suns
        // TODO: super inefficient
        // TODO maybe just use size of biggest MergedNode * 1.1 or so
        double fakeZeroEnergyLength = 100;
        for (Node sun : suns) {
            for (Node sun2 : suns) {
                if (sun == sun2) { continue; }
                double distance = Math.sqrt(
                        Math.pow(AttributeHelper.getPositionX(sun) - AttributeHelper.getPositionX(sun2), 2) + Math
                                .pow(AttributeHelper.getPositionY(sun) - AttributeHelper.getPositionY(sun2), 2));
                if (distance < fakeZeroEnergyLength) {
                    fakeZeroEnergyLength = distance;
                }
            }
        }

        for (Node planet : allPlanets) {
            // get A collection of all stellar bodies in the same solar system as the
            // current planet
            @SuppressWarnings("unchecked")
            Collection<Node> issb = (Collection<Node>) sunToSolarSystem.get(planetToSun.get(planet)).getInnerNodes();
            // calculate the amount of intra-solar-system connections
            Set<Node> neighbors = planet.getNeighbors(); // all neighbors
            neighbors.removeAll(issb); // remove all intra-solar-system bodies
            int essc = neighbors.size(); // number of inter-solar-system connections

            Vector2d center = AttributeHelper.getPositionVec2d(sunToSolarSystem.get(planetToSun.get(planet)));
            if (essc == 0) { // no inter-solar-system connections. Place Planets randomly around sun
                double angle = Math.random() * Math.PI * 2;
                double x = Math.cos(angle) * (fakeZeroEnergyLength / 3) + center.x;
                double y = Math.sin(angle) * (fakeZeroEnergyLength / 3) + center.y;
                AttributeHelper.setPosition(planet, x, y);
            } else { // connected to other solar system(s)
                double planetPositionX = 0.0;
                double planetPositionY = 0.0;
                for (Node neighbor : neighbors) {
                    double lambda;
                    Vector2d otherSun;
                    if (suns.contains(neighbor)) { // neighbor is a sun
                        lambda = 1.0 / 2.0;
                        otherSun = AttributeHelper.getPositionVec2d(neighbor);
                    } else if (allPlanets.contains(neighbor)) { // neighbor is a planet
                        lambda = 1.0 / 3.0;
                        otherSun = AttributeHelper.getPositionVec2d(planetToSun.get(neighbor));
                    } else if (allMoons.contains(neighbor)) { // neighbor is a moon
                        lambda = 1.0 / 4.0;
                        otherSun = AttributeHelper.getPositionVec2d(planetToSun.get(moonToPlanet.get(neighbor)));
                    } else {
                        throw new IllegalStateException("neighbor is neither sun, planet nor moon (?) wtf");
                    }
                    planetPositionX += center.x + (lambda * (otherSun.x - center.x));
                    planetPositionY += center.y + (lambda * (otherSun.y - center.y));
                }
                AttributeHelper.setPosition(planet, planetPositionX / essc, planetPositionY / essc);
            }
        }

        for (Node moon : allMoons) {
            // get A collection of all stellar bodies in the same solar system as the
            // current moon
            MergedNode sun = sunToSolarSystem.get(planetToSun.get(moonToPlanet.get(moon)));
            Collection<?extends Node> innerNodes = sun.getInnerNodes();
            // calculate the amount of intra-solar-system connections
            Set<Node> interSolarSystemNeighbors = moon.getNeighbors(); // all neighbors
            interSolarSystemNeighbors.removeAll(innerNodes); // remove all intra-solar-system bodies
            int interSolarSystemNeighborCount = interSolarSystemNeighbors.size();

            if (interSolarSystemNeighborCount == 0) { // no inter-solar-system connections. Place Planets randomly around sun
                double angle = Math.random() * Math.PI * 2;
                double placementDist = fakeZeroEnergyLength;
                Node planet = moonToPlanet.get(moon);
                Vector2d planetPos = AttributeHelper.getPositionVec2d(planet);
                // calculate distance from the planet to its nearest neighbor
                for (Node neighbor : planet.getNeighbors()) {
                    if (allMoons.contains(neighbor)) { continue; }
                    double distance = Math.sqrt(
                            Math.pow(AttributeHelper.getPositionX(planet) - AttributeHelper.getPositionX(neighbor), 2) + Math
                                    .pow(AttributeHelper.getPositionY(planet) - AttributeHelper.getPositionY(neighbor), 2));
                    if (distance < placementDist) {
                        placementDist = distance;
                    }
                }
                placementDist /= 2 * Math.sqrt(2.0);
                double x = Math.cos(angle) * placementDist + planetPos.x;
                double y = Math.sin(angle) * placementDist + planetPos.y;
                AttributeHelper.setPosition(planet, x, y);
            } else { // connected to other solar system(s)
                Vector2d sunPos = AttributeHelper.getPositionVec2d(sun);
                double moonPositionX = 0.0;
                double moonPositionY = 0.0;
                for (Node neighbor : interSolarSystemNeighbors) {
                    double lambda;
                    Vector2d otherSun;
                    if (allPlanets.contains(neighbor)) {
                        lambda = 1.0 / 4.0;
                        otherSun = AttributeHelper.getPositionVec2d(planetToSun.get(neighbor));
                    } else if (allMoons.contains(neighbor)) {
                        lambda = 1.0 / 5.0;
                        otherSun = AttributeHelper.getPositionVec2d(planetToSun.get(moonToPlanet.get(neighbor)));
                    } else {
                        throw new IllegalStateException("moon's neighbor is neither planet nor moon");
                    }
                    moonPositionX += sunPos.x + (lambda * (otherSun.x - sunPos.x));
                    moonPositionY += sunPos.y + (lambda * (otherSun.y - sunPos.y));
                }
                AttributeHelper.setPosition(moon, moonPositionX / interSolarSystemNeighborCount,
                        moonPositionY / interSolarSystemNeighborCount);
            }
        }
    }

    /**
     * @author Gordian
     * @see org.vanted.addons.multilevelframework.sm_util.gui.Describable#getName()
     */
    @Override
    public String getName() {
        return "Solar Placer"; // TODO
    }

    /**
     * @author Gordian
     * @see org.vanted.addons.multilevelframework.sm_util.gui.Describable#getDescription()
     */
    @Override
    public String getDescription() {
        return "Solar placer"; // TODO
    }

    /**
     * Get an object attribute. Throws an exception if it
     * doesn't work.
     *
     * @param internalGraph The {@link InternalGraph} to get the attribute from.
     *                      Must not be {@code null}.
     * @param key           The key to get.
     * @return the object returned by {@link InternalGraph#getObject(String)}.
     * @author Gordian
     */
    private static Object getElement(InternalGraph internalGraph, String key) {
        if (!internalGraph.getObject(SolarMerger.SUNS_KEY).isPresent()) {
            MainFrame.getInstance().showMessageDialog("SolarPlacer can only be run on graphs merged by Solar Merger.");
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
