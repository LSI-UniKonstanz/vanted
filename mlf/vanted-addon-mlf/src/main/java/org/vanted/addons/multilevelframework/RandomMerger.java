package org.vanted.addons.multilevelframework;

import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.parameter.Parameter;

import java.util.*;

import static org.apache.commons.collections15.CollectionUtils.size;

public class RandomMerger implements Merger {
    @Override
    public Parameter[] getParameters() {
        return new Parameter[0];
    }

    @Override
    public void setParameters(Parameter[] parameters) {


    }

    // Variables containing the future parameters

    // the ratio in size of the baseLevel and the resulting coarsening level
    private double coarseningFactor = 0.5;

    // the minimal number of nodes for a coarsening level
    private int minNumberOfNodesPerLevel = 2;

    // the maximum amount of levels for one multilevelGraph
    private int maxNumberOfIterations = 1000;

    /**
     * builds the coarsening levels for the graph depending on parameters. These Parameters shrinkRatio
     * and mergesPer Step determine how the top coarsening level compares in size to the complete graph
     * how the each coarsening step simplifies the last.
     *
     * @param multilevelGraph The multilevelGraph which the coarsening is performed on
     *                        The {@link MultilevelGraph} that contains the original graph to build
     *
     * @author tobias
     */
    public void buildCoarseningLevels(MultilevelGraph multilevelGraph) {

        // checks whether the coarseningFactor is in Range and the graph component does contain multiple edges.
        // no edges leave inhibit any coarsening while merged on the last edge
        if ((multilevelGraph.getTopLevel().getEdges().size() > 1) && (0 < coarseningFactor) && (coarseningFactor > 1)){
            // calls upon build level to create multiple levels
            for (int i = 0; i < this.maxNumberOfIterations; i++) {
                buildLevel(this.coarseningFactor, multilevelGraph);
                if (multilevelGraph.getTopLevel().getNumberOfNodes() <= this.minNumberOfNodesPerLevel) {
                    break;
                }
            }
        }

    }

    /**
     * builds one level of a {@link MultilevelGraph} by coarsening the current top level.
     * the resulting coarsened Level is added as the new top level
     * @param coarseningPerLevel the ratio between in sizes of the baseLevel and the resulting coarsened Level
     * @param mlg the {@link MultilevelGraph} receiving an additional coarsening level.
     */
    private void buildLevel(double coarseningPerLevel, MultilevelGraph mlg){
        Graph baseLevel = mlg.getTopLevel();

        // initializing the new level for the resulting coarsened Graph
        mlg.newCoarseningLevel();

        // HashMap mapping originNodes to the Sets representing them on the next higher level.
        HashMap <Node, Set<Node>> node2nodeSet = new HashMap<>();

        List<Edge> edges = new ArrayList<>(baseLevel.getEdges());

        // maximum amount of Merges per Level limited by the relative number of Nodes and Edges
        int maxMergedEdges = (int) Math.min((edges.size()* coarseningPerLevel),
                (baseLevel.getNumberOfNodes()*coarseningPerLevel ));

        // shuffling the list to obtain random Edges
        Collections.shuffle(edges);


        // merging nodes and adding their origins and nodes to node2nodeSet
        for (int i = 0; i < maxMergedEdges; i++) {
            Node source = edges.get(i).getSource();
            Node target = edges.get(i).getTarget();

            if (node2nodeSet.containsKey(source)) {
                // the source node being already in the HashMap
                node2nodeSet.get(source).add(target);
                node2nodeSet.put(target, node2nodeSet.get(source));
            } else if (node2nodeSet.containsKey(target)) {
                // the target node being already in the HashMap
                node2nodeSet.get(target).add(source);
                node2nodeSet.put(source, node2nodeSet.get(target));
            } else {
                // both the nodes not being in represented in the HashMap
                Set<Node> represented = new HashSet<>();
                represented.add(target);
                represented.add(source);
                node2nodeSet.put(source, represented);
                node2nodeSet.put(target, represented);
            }
        }

        // Adding the nodes, which are not altered in this coarsening step together with its origin
        // to node2nodeSet.
        for (int i = maxMergedEdges; i < edges.size(); i ++) {

            Node source = edges.get(i).getSource();
            Node target = edges.get(i).getTarget();

            if (!node2nodeSet.containsKey(source)) {
                // the source node being already in the HashMap
                node2nodeSet.put(source, Collections.singleton(source));
            }
            if (!node2nodeSet.containsKey(target)) {
                // the target node being already in the HashMap
                node2nodeSet.put(target, Collections.singleton(target));
            }
        }

        // adding unique mergedNodes to already added and preventing them from being introduces multiple times
        // The origin nodes are mapped to their resulting unique MergedNodes in node2mergedNode
        IdentityHashMap<Set<Node>, Integer> alreadyAdded = new IdentityHashMap<>();
        Map<Node, MergedNode> node2mergedNode = new HashMap<>();
        for (Set<Node> ns : node2nodeSet.values()) {
            //making sure each set appears at most once
            if (!alreadyAdded.containsKey(ns)) {
                alreadyAdded.put(ns, 0);
                // adding all represented Nodes with their resulting MergedNode to node2MergedNode
                MergedNode mergedNode = mlg.addNode(ns);
                for(Node n : ns) {
                    node2mergedNode.put(n, mergedNode);
                }
            }
        }

        // asserting that all nodes have been added to the next coarseningLevel
        assert mlg.isComplete(): "Representation of nodes is incomplete";

        // adding the edges to the new top level
        for(Edge i : edges) {
            MergedNode source = node2mergedNode.get(i.getSource());
            MergedNode target = node2mergedNode.get(i.getTarget());
            // TODO: getUndirectedEdges() creates a new collection each time. Maybe performance could be improved
            // by manually storing which edges have been added.
            if (!source.getNeighbors().contains(target) && !target.getNeighbors().contains(source)) {
                mlg.addEdge(source, target);
            }
        }

    }
}

