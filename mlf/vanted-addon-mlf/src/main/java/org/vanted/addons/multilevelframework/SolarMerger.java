package org.vanted.addons.multilevelframework;

import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.parameter.Parameter;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
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
                buildGalaxy(multilevelGraph);
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
     *
     */
    private void buildGalaxy(MultilevelGraph multilevelGraph) {

        // Graph storing the current topLevel as baseLevel for this coarsening step
        Graph baseLevel = multilevelGraph.getTopLevel();

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
                Set<Node> moons = m.getNeighbors();
                moons.removeAll(planets);
                // Adding all planets and their moons to planet2Moon
                planet2Moon.put(m, moons);
            }
        }



    }

    /**
     * determines suns for the solarsystems which will represent the current level of the multilevelgraph
     * @param baseLevel  the level current of the multilevel framework
     */
    private List<Node> findSuns(Graph baseLevel) {
        // Graph containing a copy of the the baseLevel to determine suns    !!!!UNUSED!!!!
        Graph sunCanditates = baseLevel;
        // List containing the suns for this level
        List<Node> sunList = new List;
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
