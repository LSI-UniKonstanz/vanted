package org.vanted.addons.multilevelframework;

import org.AttributeHelper;
import org.Vector2d;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Node;
import org.graffiti.plugin.parameter.Parameter;

import java.util.*;
import java.util.Map.Entry;

import static org.AttributeHelper.getPositionX;
import static org.AttributeHelper.getPositionY;

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
    public void setParameters(Parameter[] parameters) { }

    /**
     * @author Katze, Tobias, Gordian
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

        // used for placing the planets that don't lie on inter-solar-system-paths
        List<Node> singlePlanets = new ArrayList<>();
        for (Node planet : allPlanets) {
            // get A collection of all stellar bodies in the same solar system as the
            // current planet
            Collection<?extends Node> innerNodes = sunToSolarSystem.get(planetToSun.get(planet)).getInnerNodes();
            // calculate the amount of inter-solar-system connections
            Set<Node> neighbors = planet.getNeighbors(); // all neighbors
            neighbors.removeAll(innerNodes); // remove all intra-solar-system bodies
            int interSolarSystemNeighborCount = neighbors.size(); // number of inter-solar-system connections

            if (interSolarSystemNeighborCount == 0) { // no inter-solar-system connections. Place Planets randomly around sun
                singlePlanets.add(planet);
            } else { // connected to other solar system(s)
                Node sun = planetToSun.get(planet);
                double sunX = getPositionX(sun);
                double sunY = getPositionY(sun);
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
                    planetPositionX += sunX + (lambda * (otherSun.x - sunX));
                    planetPositionY += sunY + (lambda * (otherSun.y - sunY));
                }
                AttributeHelper.setPosition(planet, planetPositionX / interSolarSystemNeighborCount,
                        planetPositionY / interSolarSystemNeighborCount);
            }
        }

        // place planets with no inter-solar-system paths
        for (Node planet : singlePlanets) {
            Node sun = planetToSun.get(planet);
            // default value is the height/width of the sun
            double placementDist = Math.max(AttributeHelper.getWidth(sun), AttributeHelper.getHeight(sun));
            // if the sun has other planets, use the minimal distance of those
            for (Node neighbor : sun.getNeighbors()) {
                if (neighbor != planet) {
                    double distance = distance(sun, neighbor);
                    if (distance < placementDist) {
                        placementDist = distance;
                    }
                }
            }
            double angle = Math.random() * Math.PI * 2;
            double x = Math.cos(angle) * placementDist + getPositionX(sun);
            double y = Math.sin(angle) * placementDist + getPositionY(sun);
            AttributeHelper.setPosition(planet, x, y);
        }

        for (Node moon : allMoons) {
            // get A collection of all stellar bodies in the same solar system as the
            // current moon
            MergedNode sun = sunToSolarSystem.get(planetToSun.get(moonToPlanet.get(moon)));
            Collection<?extends Node> innerNodes = sun.getInnerNodes();
            // calculate the amount of inter-solar-system connections
            Set<Node> interSolarSystemNeighbors = moon.getNeighbors(); // all neighbors
            interSolarSystemNeighbors.removeAll(innerNodes); // remove all intra-solar-system bodies
            int interSolarSystemNeighborCount = interSolarSystemNeighbors.size();

            if (interSolarSystemNeighborCount == 0) { // no inter-solar-system connections. Place Planets randomly around sun
                double angle = Math.random() * Math.PI * 2;
                Node planet = moonToPlanet.get(moon);
                double placementDist = 100; // never used because the moon always has to have at least one neighbor (its planet)
                // calculate distance from the planet to its nearest neighbor
                for (Node neighbor : planet.getNeighbors()) {
                    if (allMoons.contains(neighbor)) { continue; }
                    double distance = distance(neighbor, planet);
                    if (distance < placementDist) {
                        placementDist = distance;
                    }
                }
                placementDist /= 2 * Math.sqrt(2.0);
                double x = Math.cos(angle) * placementDist + getPositionX(planet);
                double y = Math.sin(angle) * placementDist + getPositionY(planet);
                AttributeHelper.setPosition(moon, x, y);
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
        return "Solar Placer";
    }

    /**
     * @author Gordian
     * @see org.vanted.addons.multilevelframework.sm_util.gui.Describable#getDescription()
     */
    @Override
    public String getDescription() {
        return "<html>This placer is recommended when you are using the Solar Merger.<br>" +
                "Note that it cannot work together with mergers other than the Solar Merger.</html>";
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
    static Object getElement(InternalGraph internalGraph, String key) {
        return internalGraph.getObject(key).orElseThrow(() -> {
            MainFrame.getInstance().showMessageDialog("SolarPlacer can only be run on graphs merged by Solar Merger.");
            return new IllegalArgumentException("SolarPlacer can only be run on graphs merged by Solar Merger.");
        });
    }

    /**
     * Compute the Euclidean distance between two nodes.
     * @param n1
     *      The first {@link Node}. Must not be {@code null}.
     * @param n2
     *      The second {@link Node}. Must not be {@code null}.
     * @return
     *      The Euclidean distance between the two nodes.
     * @author Katze, Gordian
     */
    static double distance(Node n1, Node n2) {
        final double dx = getPositionX(n1) - getPositionX(n2);
        final double dy = getPositionY(n1) - getPositionY(n2);
        return Math.sqrt(dx*dx + dy*dy);
    }
}
