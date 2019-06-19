package org.vanted.addons.multilevelframework;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import org.AttributeHelper;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.parameter.Parameter;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

class SolarMerger implements Merger {
    @Override
    public Parameter[] getParameters() {
        return new Parameter[0];
    }

    @Override
    public void setParameters(Parameter[] parameters) {
        // TODO: set parameters

    }


    // Variables containing the stopping criteria for the SolarMerger
    int minNodes = 1;
    int maxLevelFactor = 1;


    @Override
    public void buildCoarseningLevels(MultilevelGraph multilevelGraph) {

        final long startTime = System.nanoTime();

        // Determine MaxLevels from Factor
        int maxLevels = multilevelGraph.getTopLevel().getNodes().size() / this.maxLevelFactor;

        // checks whether the multilevelGraph contains too many Levels and whether the topLevel has enough Nodes
        if (multilevelGraph.getTopLevel().getNodes().size() > minNodes) {
            // calls upon build level to create multiple levels
            for (int i = 0; i < maxLevels; i++) {
                processGalaxy(multilevelGraph);
                if (multilevelGraph.getTopLevel().getNumberOfNodes() <= this.minNodes) {
                    break;
                }
            }
        }

        final long endTime = System.nanoTime();
        System.out.println("Built coarsening levels in: " +
                TimeUnit.NANOSECONDS.toMillis(endTime - startTime) + " ms.");
    }


    /**
     * Builds a Galaxy for the current multilevel consisting of several solar Systems.
     * Each solar System has a sun, planets(dist 1 to the sun) and one or no moon for each planet.
     */
    private void processGalaxy(MultilevelGraph multilevelGraph) {

        // Graph storing the current topLevel as baseLevel for this coarsening step
        Graph baseLevel = multilevelGraph.getTopLevel();
        assert GraphHelper.getConnectedComponents(baseLevel.getNodes()).size() == 1;
        // adding a new level to the coarsening graph
        multilevelGraph.newCoarseningLevel();

        // Determining suns for all the solar systems of the galaxy
        Set<Node> suns = findSuns(baseLevel);

        // HashMaps containing suns and their planets as well as planets and their moons
        HashMap<Node, Set<Node>> sun2Planet = new HashMap<>();
        HashMap<Node, Set<Node>> planet2Moons = new HashMap<>();

        Set<Node> alreadyUsed = new HashSet<>(suns);

        // find planets and moons for all the suns
        for (Node sun : suns) {
            Set<Node> planets = sun.getNeighbors();
            planets.removeAll(alreadyUsed);
            alreadyUsed.addAll(planets);
            //Adding all suns and their planet sun2Planet
            sun2Planet.put(sun, planets);

            for (Node planet : planets) {
                Set<Node> moons = planet.getNeighbors();
                // TODO the closest planet to each moon should be used
                // prevent the sun and the planets to become their own moons
                moons.removeAll(alreadyUsed);
                alreadyUsed.addAll(moons);

                // checking whether the planet has a potential moon
                if (!moons.isEmpty()) {
                    // HashSet to collect and add the planets moons
                    // Adding all planets and their moons to planet2Moon
                    planet2Moons.put(planet, moons);
                }
            }
        }

        // HashMap mapping the collapsed Suns to the Sets of potential neighbors for the resulting collapsed Sun
        HashMap<MergedNode, Set<Node>> collapsedSunsToNeighbors = new HashMap<>();
        // HashMap mapping baseLevelNodes to their merged Node
        HashMap<Node, MergedNode> nodeToMergedNode = new HashMap<>();

        //Collapsing the solarSystems of the galaxy into their suns
        for (Map.Entry<Node, Set<Node>> preparerMergedNode : sun2Planet.entrySet()) {
            // Preparing the inner Nodes for the collapsing sun
            Set<Node> innerNodes = new HashSet<>();
            // Adding the sun
            innerNodes.add(preparerMergedNode.getKey());
            // Adding Planets
            Set<Node> planets = preparerMergedNode.getValue();
            innerNodes.addAll(planets);
            // Adding moons for each planet
            for (Node planet : planets) {
                Set<Node> moons = planet2Moons.get(planet);
                //Todo Edges have to be calculated, weights have to be calculated
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
                nodeToMergedNode.put(n,nMergedNode);
            }

            // removing already represented innerNodes and intraSystemNeighbors from the Set of interSolar Neighbors
            interSystemNeighbors.removeAll(innerNodes);

            // Adding the merged Node and its neighbors to the HashMap
            collapsedSunsToNeighbors.put(nMergedNode, interSystemNeighbors);
        }

        // contains suns whose edges have already been added
        Set<Node> alreadyProcessed = new HashSet<>();

        // Determine the resulting MergedNodes for all the Nodes in collapsedSunsToNeighbors
        for (Map.Entry<MergedNode, Set<Node>> interSN : collapsedSunsToNeighbors.entrySet()) {
            Node sun = interSN.getKey();
            Set<Node> neighbors = interSN.getValue();
            alreadyProcessed.add(sun);
            // Set containing the already established Neighbors
            Set<MergedNode> alreadyNeighbor = new HashSet<>();
            // Traversing all noted neighbors of the sun
            for (Node neighbor : neighbors) {
                MergedNode topLevelNeighbor = nodeToMergedNode.get(neighbor);
                if (topLevelNeighbor != null
                        && !alreadyNeighbor.contains(topLevelNeighbor)
                        && !alreadyProcessed.contains(topLevelNeighbor)) {
                    // Adding the new Neighbor to alreadyNeighbor to prevent multiple edges
                    alreadyNeighbor.add(topLevelNeighbor);
                    // Adding the Edge to the TopLevel
                    multilevelGraph.addEdge(sun, topLevelNeighbor);
                }
            }
        }

        assert GraphHelper.getConnectedComponents(multilevelGraph.getTopLevel().getNodes()).size() == 1;

        Set<Node> nodes = new HashSet<>(baseLevel.getNodes());
        nodes.removeAll(multilevelGraph.getTopLevel().getNodes().stream().flatMap(n -> {
            MergedNode mn = (MergedNode) n;
            return mn.getInnerNodes().stream();
        }).collect(Collectors.toSet()));
        System.out.println("Unrepresented Nodes:");
        for (Node n : nodes) {
            MergedNode mn = (MergedNode)n;
            for (Node node : mn.getInnerNodes()) {
                System.out.print(", ");
                System.out.print(AttributeHelper.getLabel(node, "Unknown"));
            }
        }
        System.out.println();

    }

    /**
     * determines suns for the solarSystems which will represent the current level of the multiLevelGraph
     *
     * @param baseLevel the level current of the multilevel framework
     * @return sunList a List of the central Nodes of the solar systems
     */
    private Set<Node> findSuns(Graph baseLevel) {
        // List containing the suns for this level
        Set<Node> sunSet = new HashSet<>();
        // HashSet containing all Nodes of the Top list with their Neighbors
        Set<Node> sunCandidates = new HashSet<>(baseLevel.getNodes());

        // Establishing solar systems on the current level as long as there are unmatched nodes
        while (!sunCandidates.isEmpty()) {
            Node sun = sunCandidates.iterator().next();
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
        return sunSet;
    }
}
