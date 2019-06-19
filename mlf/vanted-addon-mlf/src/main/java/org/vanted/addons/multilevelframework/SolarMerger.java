package org.vanted.addons.multilevelframework;

import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.parameter.Parameter;

import java.util.*;
import java.util.concurrent.TimeUnit;

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
    int minNodes = 20;
    int maxLevelFactor = 10;


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
        // adding a new level to the coarsening graph
        multilevelGraph.newCoarseningLevel();

        // Determining suns for all the solar systems of the galaxy
        Set<Node> suns = findSuns(baseLevel);

        // HashMaps containing suns and their planets as well as planets and their moons
        HashMap<Node, Set<Node>> sun2Planet = new HashMap<>();
        HashMap<Node, Set<Node>> planet2Moons = new HashMap<>();

        // Set containing all moons of the
        Set<Node> allMoons = new HashSet<>();

        // find planets and moons for all the suns
        for (Node sun : suns) {
            Set<Node> planets = sun.getNeighbors();
            //Adding all suns and their planet sun2Planet
            sun2Planet.put(sun, planets);

            for (Node planet : planets) {
                Set<Node> candidateMoons = planet.getNeighbors();
                // TODO the closest planet to each moon should be used
                // prevent the sun and the planets to become their own moons
                candidateMoons.remove(sun);
                candidateMoons.removeAll(planets);

                // checking whether the planet has a potential moon
                if (!candidateMoons.isEmpty()) {
                    // HashSet to collect and add the planets moons
                    Set<Node> moons = new HashSet<>();

                    for (Node moon : candidateMoons) {
                        // checking whether the current node is already a moon
                        if (!(allMoons.contains(moon))) {
                            allMoons.add(moon);
                            moons.add(moon);

                        }
                    }
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
            if (!planets.isEmpty()){
                innerNodes.addAll(planets);
                // Adding moons for each planet
                for (Node planet : planets) {
                    Set<Node> moons = planet2Moons.get(planet);
                    //Todo Edges have to be calculated, weights have to be calculated
                    if (!moons.isEmpty()) {
                        innerNodes.addAll(moons);
                    }
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

        // Determine the resulting MergedNodes for all the Nodes in collapsedSunsToNeighbors
        for (Map.Entry<MergedNode, Set<Node>> interSN : collapsedSunsToNeighbors.entrySet()) {
            // Set containing the already established Neighbors
            Set<MergedNode> alreadyNeighbor = new HashSet<>();
            // Traversing all noted neighbors of the sun
            for (Node neighbor : interSN.getValue()) {
                for (Map.Entry<Node, MergedNode> resultingNeighbor : nodeToMergedNode.entrySet()){
                    if (resultingNeighbor.getKey() == neighbor){
                        MergedNode topLevelNeighbor = resultingNeighbor.getValue();
                        if (!alreadyNeighbor.contains(topLevelNeighbor)){
                            // Adding the new Neighbor to alreadyNeighbor to prevent multiple edges
                            alreadyNeighbor.add(topLevelNeighbor);
                            // Adding the Edge to the TopLevel
                            multilevelGraph.addEdge(interSN.getKey(), topLevelNeighbor);
                        }
                    }
                }
            }
        }


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
        // HashSet containing "blacklisted" candidates
        Set<Node> notSuns = new HashSet<>();

        // Establishing solar systems on the current level as long as there are unmatched nodes
        for (Node n : sunCandidates) {
            // Makes sure no Planet or Moon is used as a Sun
            if (!notSuns.contains(n)) {
                // Adds the new Sun to the Set
                sunSet.add(n);
                // Preventing the sun from being added again
                notSuns.add(n);
                Set<Node> planets = new HashSet<>(n.getNeighbors());
                for (Node p : planets) {
                    Set<Node> moons = new HashSet<>(p.getNeighbors());
                    // Preventing the Planets from being added as Suns
                    notSuns.add(p);
                    // Preventing Moons from being added as Suns
                    notSuns.addAll(moons);
                }
            }
        }
        return sunSet;
    }
}
