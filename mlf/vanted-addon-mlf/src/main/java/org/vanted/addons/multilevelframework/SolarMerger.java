package org.vanted.addons.multilevelframework;

import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.parameter.Parameter;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class SolarMerger implements  Merger{
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
    int maxLevels;



    @Override
    public void buildCoarseningLevels(MultilevelGraph multilevelGraph) {

        final long startTime = System.nanoTime();

        // checks whether the multilevelGraph contains too many Levels and whether the topLevel has enough Nodes
        if (multilevelGraph.getTopLevel().getNodes().size() > minNodes) {
            // calls upon build level to create multiple levels
            for (int i = 0; i < this.maxLevels; i++) {
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
        List<Node> suns = findSuns(baseLevel);

        // HashMaps containing suns and their planets as well as planets and their moons
        HashMap <Node, Set<Node>> sun2Planet = new HashMap<>();
        HashMap <Node, Set<Node>> planet2Moon = new HashMap<>();

        // find planets and moons for all the suns
        for (Node n : suns){
            Set<Node>planets = n.getNeighbors();
            //Adding all suns and their planet sun2Planet
            sun2Planet.put(n, planets);

            for (Node m : planets){
                Set<Node> candidateMoons = m.getNeighbors();
                // TODO the closest planet to each moon should be used
                // prevent the sun and the planets to become their own moons
                candidateMoons.remove(n);
                candidateMoons.removeAll(planets);

                // checking whether the planet has a potential moon
                if(!candidateMoons.isEmpty()) {
                    // HashSet to collect and add the planets moons
                    Set<Node> moons = new HashSet<>();

                    for (Node o : candidateMoons) {
                        // checking whether the current node is already a moon
                        if (!planet2Moon.containsValue(o)) {
                            moons.add(o);
                        }
                    }
                    // Adding all planets and their moons to planet2Moon
                    planet2Moon.put(n,moons);
                }
            }
        }

        // HashMap mapping the collapsed Suns to the Sets of potential neighbors for the resulting collapsed Sun
        HashMap<MergedNode, Set<Node>> collapsedSuns2N = new HashMap<>();
        // Set containing the already represented inner Nodes, to prohibit the occurence of edge-duplicates
        Set <Node> representedNodes = new HashSet<>();

        //Collapsing the solarSystems of the galaxy into their suns
        for (Map.Entry<Node, Set<Node>> entry  :  sun2Planet.entrySet()){
            // Preparing the inner Nodes for the collapsing sun
            Set<Node> innerNodes = new HashSet<>();
            // Adding the sun
            innerNodes.add(entry.getKey());
            // Adding Planets
            Set<Node> planets = entry.getValue();
            innerNodes.addAll(planets);
            // Adding moons for each planet
            for (Node p : planets){
                Set<Node> moons = planet2Moon.get(p);
                //Todo Edges have to be calculated, weights have to be calculated
                innerNodes.addAll(moons);
            }

            // Calculating interSystemNeighbors of the collapsing sun
            Set<Node> interSystemNeighbors = new HashSet<>();
            for (Node n : innerNodes){
                interSystemNeighbors.addAll(n.getNeighbors());
            }
            // adding all innerNodes to the Set of already represented Nodes
            representedNodes.addAll(innerNodes);

            /* removing already represented innerNodes and intraSystemNeighbors from the Set of
               interSolar Neighbors
             */
            interSystemNeighbors.removeAll(representedNodes);

            // Adding the new MergedNode to the current TopLevel
            MergedNode nMergedNode = multilevelGraph.addNode(innerNodes);

            // Adding the merged Node and its neighbors to the HashMap
            collapsedSuns2N.put(nMergedNode,interSystemNeighbors);

            /* Introducing the created mergedNode as interSystemNeighbor.
            Since neighborhood includes source and target finding one of the is enough.
            Not checking the currently added nodes does not ignore existing edges.
            */
            // traversing all the inner Nodes
            for (Node in : innerNodes ){
                //Searching existing mergedNodes having these inner Nodes as neighbors
                for (Map.Entry<MergedNode,Set<Node>> ns: collapsedSuns2N.entrySet()){
                    if(ns.getValue().contains(in)){
                        // Creating new NeighborSet containing the new MergedNode
                        Set<Node> neighborSet = ns.getValue();
                        // Removing the inner Node
                        neighborSet.remove(in);
                        // Adding MergedNode
                        neighborSet.add(nMergedNode);
                        // Introducing new NeighborSet to the HashMap
                        ns.setValue(neighborSet);
                    }
                }
            }
        }
        
        // Adding the Edges resulting from interSystemNeighbors to the TopLevel
        for (Map.Entry<MergedNode, Set<Node>> interSN : collapsedSuns2N.entrySet()){
            // Traversing all noted neighbors of the sun
            for (Node n : interSN.getValue()){
                // Adding the Edge to the TopLevel
                multilevelGraph.addEdge(interSN.getKey(),n);
            }
        }



    }

    /**
     * determines suns for the solarSystems which will represent the current level of the multiLevelGraph
     * @param baseLevel  the level current of the multilevel framework
     * @return sunList a List of the central Nodes of the solar systems
     */
    private ArrayList<Node> findSuns(Graph baseLevel) {
        // List containing the suns for this level
        ArrayList<Node> sunList = new ArrayList<>();
        // HashMap containing all Nodes of the Top list with their Neighbors
        HashMap<Integer, Node> degree2Node = new HashMap<>();

        // Adding all Nodes to the hashMap
        for (Node n : baseLevel.getNodes()){
            degree2Node.put(n.getDegree(), n);
        }

        // Adding establishing solar systems on the current level as long as there are unmatched nodes
        while (!(degree2Node.isEmpty())) {
            //Sorting the hashMap to find sunCandidate with highest degree
            int maxKey = Collections.max(degree2Node.keySet());
            Node newSun = degree2Node.get(maxKey);

            // Adding newSun to the sunList
            sunList.add(newSun);

            // removing this sun, the suns planets and moons from the hashMap
            // this way of removing might be to costly
            Set<Node> planets = newSun.getNeighbors();
            degree2Node.values().remove(newSun);
            for (Node p : planets) {
                Set<Node> moons = p.getNeighbors();
                degree2Node.values().remove(p);
                for (Node m : moons) {
                    degree2Node.values().remove(m);
                }
            }
        }
        return sunList;
    }
}
