package org.vanted.addons.multilevelframework;

import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.parameter.IntegerParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.vanted.addons.multilevelframework.sm_util.gui.Describable;

import java.util.*;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.vanted.addons.multilevelframework.MlfHelper.validateNumber;

/**
 * Implements the Solar Merger as described by Jürgen Hachul (but without the target edge length calculation.)
 */
public class SolarMerger implements Merger {
    final static String MIN_NODES_NAME = "Minimum number of nodes";
    final static String MAX_LEVEL_FACTOR_NAME = "Maximum level factor";

    // keys to store the information for the SolarPlacer
    public final static String SUNS_KEY = "SOLAR_MERGER_SUNS_SET";
    public final static String PLANETS_KEY = "SOLAR_MERGER_PLANETS_SET";
    public final static String SUN_TO_PLANETS_KEY = "SOLAR_MERGER_SUN_TO_PLANETS_SET";
    public final static String PLANET_TO_MOONS_KEY = "SOLAR_MERGER_PLANET_TO_MOONS_SET";

    // Variables containing the stopping criteria for the SolarMerger
    int minNodes = 20;
    int maxLevelFactor = 10;

    private Parameter[] parameters = {
            new IntegerParameter(minNodes, 1, Integer.MAX_VALUE, MIN_NODES_NAME,
                    "The minimum number of nodes on a coarsening level. "
                            + "If the number of nodes gets lower than this, the solar merger will stop."),
            new IntegerParameter(maxLevelFactor, 1, Integer.MAX_VALUE, MAX_LEVEL_FACTOR_NAME,
                    "Determines how many levels are maximally created. The maximum number of levels "
                            + "is the number of nodes in the graph divided by this parameter."),};

    /**
     * @author Gordian
     * @see Merger#getParameters()
     */
    @Override
    public Parameter[] getParameters() {
        return this.parameters;
    }

    /**
     * @author Gordian
     * @see Merger#setParameters(Parameter[])
     */
    @Override
    public void setParameters(Parameter[] parameters) {
        this.parameters = parameters;
        for (Parameter parameter : parameters) {
            switch (parameter.getName()) {
                case MAX_LEVEL_FACTOR_NAME -> {
                    final int value = ((IntegerParameter) parameter).getInteger();
                    validateNumber(value, 0, Integer.MAX_VALUE, MAX_LEVEL_FACTOR_NAME);
                    this.maxLevelFactor = value;
                }
                case MIN_NODES_NAME -> {
                    final int value = ((IntegerParameter) parameter).getInteger();
                    validateNumber(value, 0, Integer.MAX_VALUE, MIN_NODES_NAME);
                    this.minNodes = value;
                }
                default -> throw new IllegalStateException("Invalid parameter name passed to solar merger.");
            }
        }
    }

    /**
     * Builds coarsening Levels for a multilevel Graph by using the Solar Merger as described by Jürgen Hachul.
     * Note that we do not calculate target edge lengths.
     *
     * @param multilevelGraph the MultilevelGraph for which coarsening levels are to be created
     * @author Tobias
     */
    @Override
    public void buildCoarseningLevels(MultilevelGraph multilevelGraph) {

        final long startTime = System.nanoTime();

        // Determine MaxLevels from Factor
        int maxLevels = multilevelGraph.getTopLevel().getNodes().size() / this.maxLevelFactor;

        // checks whether the multilevelGraph contains too many Levels and whether the
        // topLevel has enough Nodes
        if (multilevelGraph.getTopLevel().getNodes().size() > minNodes) {
            // calls upon build level to create inner Graphs
            for (int i = 0; i < maxLevels; i++) {
                processGalaxy(multilevelGraph);
                if (multilevelGraph.getTopLevel().getNumberOfNodes() <= this.minNodes) {
                    break;
                }
            }
        }
        // Time for building the innerGraphs
        final long endTime = System.nanoTime();
        System.out.println("Built coarsening levels in: " + NANOSECONDS.toMillis(endTime - startTime) + " ms.");
    }

    /**
     * Builds a Galaxy for the current multilevel consisting of several solar
     * Systems. Each solar System has a sun, planets(dist 1 to the sun) and one or
     * no moon(1+1 dist to sun) for each planet.
     *
     * First a set of Suns is created. Following that all neighbors of the suns are mapped to them as planets.
     * The remaining nodes are mapped as moons to their neighboring planet.
     * The sun, its planets and their moons are added as inner nodes of a MergedNode to a new CoarseningLevel.
     * The edges the inner nodes have to nodes outside the solar system are added as Edges between MergedNodes
     * to the new CoarseningLevel.
     *
     * @param multilevelGraph the MultilevelGraph which buildCoarseningLevels works on
     * @author Tobias, Gordian
     */
    void processGalaxy(MultilevelGraph multilevelGraph) {

        // Graph storing the current topLevel as baseLevel for this coarsening step
        Graph baseLevel = multilevelGraph.getTopLevel();
        // adding a new level to the coarsening graph
        multilevelGraph.newCoarseningLevel();

        // Determining suns for all the solar systems of the galaxy
        Set<Node> suns = findSuns(baseLevel);

        // HashMaps containing suns and their planets as well as planets and their moons
        HashMap<Node, Set<Node>> sunsToPlanets = new HashMap<>();
        HashMap<Node, Set<Node>> planetsToMoons = new HashMap<>();

        // Set containing the already processed nodes of the base level
        Set<Node> alreadyUsed = new HashSet<>(suns);
        // Set containing all the Planet nodes
        Set<Node> allPlanets = new HashSet<>();

        // find planets for all the suns
        for (Node sun : suns) {
            // all neighbors of the sun are introduced as potential planets
            Set<Node> planets = sun.getNeighbors();
            planets.removeAll(alreadyUsed);
            alreadyUsed.addAll(planets);
            // Adding the planets of this sun to allPlanets and already Used;
            allPlanets.addAll(planets);
            // Adding all suns and their planet sunsToPlanets
            sunsToPlanets.put(sun, planets);
        }

        // Add the remaining neighbors of the Planets as moons
        for (Node planet : allPlanets) {
            Set<Node> moons = planet.getNeighbors();
            // prevent the sun and the planets to become their own moons
            moons.removeAll(alreadyUsed);
            alreadyUsed.addAll(moons);

            // checking whether the planet has a potential moon
            if (!moons.isEmpty()) {
                // HashSet to collect and add the planets moons
                // Adding all planets and their moons to planet2Moon
                planetsToMoons.put(planet, moons);
            }
        }

        // HashMap mapping the collapsed Suns to the Sets of potential neighbors for the
        // resulting collapsed Sun
        HashMap<MergedNode, Set<Node>> collapsedSunsToNeighbors = new HashMap<>();
        // HashMap mapping baseLevelNodes to their merged Node
        HashMap<Node, MergedNode> nodeToMergedNode = new HashMap<>();

        // Collapsing the solarSystems of the galaxy into their suns
        for (Map.Entry<Node, Set<Node>> sunWithPlanets : sunsToPlanets.entrySet()) {
            // Preparing the inner Nodes for the collapsing sun
            Set<Node> innerNodes = new HashSet<>();
            // Adding the sun
            innerNodes.add(sunWithPlanets.getKey());
            // Adding Planets
            Set<Node> planets = sunWithPlanets.getValue();
            innerNodes.addAll(planets);
            // Adding moons for each planet
            for (Node planet : planets) {
                Set<Node> moons = planetsToMoons.get(planet);
                if (moons != null) {
                    innerNodes.addAll(moons);
                }
            }

            // Adding the new MergedNode to the current TopLevel
            MergedNode nMergedNode = multilevelGraph.addNode(innerNodes);

            // Calculating interSystemNeighbors of the collapsing sun
            Set<Node> interSystemNeighbors = new HashSet<>();
            for (Node n : innerNodes) {
                interSystemNeighbors.addAll(n.getNeighbors());
                // Adding the new inner Nodes mapped to the MergedNode to nodeToMergedNode
                nodeToMergedNode.put(n, nMergedNode);
            }
            // removing already represented innerNodes and intraSystemNeighbors from the Set
            // of interSolar Neighbors
            interSystemNeighbors.removeAll(innerNodes);

            // Adding the merged Node and its neighbors to the HashMap
            collapsedSunsToNeighbors.put(nMergedNode, interSystemNeighbors);
        }

        // contains suns whose edges have already been added
        Set<Node> alreadyProcessed = new HashSet<>();

        // Determine the resulting MergedNodes for all the Nodes in
        // collapsedSunsToNeighbors
        for (Map.Entry<MergedNode, Set<Node>> interSN : collapsedSunsToNeighbors.entrySet()) {
            Node sun = interSN.getKey();
            Set<Node> neighbors = interSN.getValue();
            // Adding the sun to the already processed targets
            alreadyProcessed.add(sun);
            // Set containing the already established Neighbors of the currently processed
            // sun
            Set<MergedNode> alreadyNeighbor = new HashSet<>();
            // Traversing all noted neighbors of the sun
            for (Node neighbor : neighbors) {
                MergedNode topLevelNeighbor = nodeToMergedNode.get(neighbor);
                if (topLevelNeighbor != null && !alreadyNeighbor.contains(topLevelNeighbor)
                        && !alreadyProcessed.contains(topLevelNeighbor)) {
                    // Adding the new Neighbor to alreadyNeighbor to prevent multiple edges
                    alreadyNeighbor.add(topLevelNeighbor);
                    // Adding the Edge to the TopLevel
                    multilevelGraph.addEdge(sun, topLevelNeighbor);
                }
            }
        }

        // Preparing HashMaps and HashSets for the solarMerger
        LevelGraph top = (LevelGraph) multilevelGraph.getTopLevel();
        top.setObject(SUNS_KEY, suns);
        top.setObject(PLANETS_KEY, allPlanets);
        top.setObject(SUN_TO_PLANETS_KEY, sunsToPlanets);
        top.setObject(PLANET_TO_MOONS_KEY, planetsToMoons);
    }

    /**
     * determines suns for the solarSystems which will represent the current level
     * of the multiLevelGraph.
     *
     * @param baseLevel the level current of the multilevel framework
     * @return sunList a List of the central Nodes of the solar systems
     * @author Tobias, Gordian
     */
    Set<Node> findSuns(Graph baseLevel) {
        final long startTime = System.nanoTime();
        // HashSet containing the suns for this level
        Set<Node> sunSet = new HashSet<>();
        // HashSet containing all Nodes of the baseLevel
        Set<Node> sunCandidates = new HashSet<>(baseLevel.getNodes());

        // Establishing solar systems on the current level as long as there are
        // unmatched nodes
        while (!sunCandidates.isEmpty()) {
            Node sun = sunCandidates.stream()
                    // find the minimum weight node, use default weight of 0 if the nodes aren't
                    // MergedNodes
                    .min(Comparator.comparing(n -> n instanceof MergedNode ? ((MergedNode) n).getWeight() : 0))
                    .orElseThrow(IllegalStateException::new); // this cannot happen as we checked isEmpty() before
            sunCandidates.remove(sun);
            // Adds the new Sun to the Set
            sunSet.add(sun);
            Set<Node> planets = sun.getNeighbors();
            sunCandidates.removeAll(planets);
            for (Node p : planets) {
                // Preventing Moons from being added as Suns
                sunCandidates.removeAll(p.getNeighbors());
            }
        }
        final long endTime = System.nanoTime();
        System.out.println("Calculated sun list in " + NANOSECONDS.toMillis(endTime - startTime) + " ms.");
        return sunSet;
    }

    /**
     * @author Gordian
     * @see Describable#getName()
     */
    @Override
    public String getName() {
        return "Solar Merger";
    }

    /**
     * @author Gordian
     * @see Describable#getDescription()
     */
    @Override
    public String getDescription() {
        return "Merges nodes by partitioning the graph into \"solar systems\" and"
                + " classifying each node as a sun, planet or moon. The solar system is then"
                + " collapsed into a single merged node.";
    }
}
