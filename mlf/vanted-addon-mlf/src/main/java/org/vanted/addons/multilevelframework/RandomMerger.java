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
        // Variables containing the future parameters
        double shrinkRatio = 0.5;
        int minNumberOfNodesPerLevel = 1;
        int maxNumberOfIterations = 1000;

        for (int i = 0; i < maxNumberOfIterations; i++) {
            buildLevel(shrinkRatio, multilevelGraph);
            if (multilevelGraph.getTopLevel().getNumberOfNodes() <= minNumberOfNodesPerLevel) {
                break;
            }
        }

    }


    private void buildLevel(double shrinkRatio, MultilevelGraph mlg){
        Graph baseLevel = mlg.getTopLevel();
        mlg.newCoarseningLevel();

        List<Edge> edges = new ArrayList<>(baseLevel.getEdges());
        Collections.shuffle(edges);
        int stop = Math.max(1, (int) (edges.size()* shrinkRatio));

        HashMap <Node, Set<Node>> node2nodeSet = new HashMap<>();

        for (int i = 0; i < stop; i++) {
            Node source = edges.get(i).getSource();
            Node target = edges.get(i).getTarget();

            if (node2nodeSet.containsKey(source)) {
                node2nodeSet.get(source).add(target);
            } else if (node2nodeSet.containsKey(target)) {
                node2nodeSet.get(target).add(source);
            } else {
                Set<Node> represented = new HashSet<>();
                represented.add(target);
                represented.add(source);
                node2nodeSet.put(source, represented);
                node2nodeSet.put(target, represented);
            }
        }

        // Adding the nodes, which are not altered in this coarsening step together with its origin
        // to substitutions.
        for (int i = stop; i < edges.size(); i ++) {

            Node source = edges.get(i).getSource();
            Node target = edges.get(i).getTarget();

            if (!node2nodeSet.containsKey(source)) {
                node2nodeSet.put(source, Collections.singleton(source));
            }
            if (!node2nodeSet.containsKey(target)) {
                node2nodeSet.put(target, Collections.singleton(target));
            }
        }

        // add MergedNodes
        IdentityHashMap<Set<Node>, Integer> alreadyAdded = new IdentityHashMap<>();
        Map<Node, MergedNode> node2mergedNode = new HashMap<>();
        for (Set<Node> ns : node2nodeSet.values()) {
            if (!alreadyAdded.containsKey(ns)) {
                alreadyAdded.put(ns, 0);
                MergedNode mergedNode = mlg.addNode(ns);
                for(Node n : ns) {
                    node2mergedNode.put(n, mergedNode);
                }
            }
        }

        assert mlg.isComplete(): "neva gonna happen";

        for (int i = 0; i < edges.size(); i++) {
            MergedNode source = node2mergedNode.get(edges.get(i).getSource());
            MergedNode target = node2mergedNode.get(edges.get(i).getTarget());
            // TODO: getUndirectedEdges() creates a new collection each time. Maybe performance could be improved
            // by manually storing which edges have been added.
            if (!source.getUndirectedEdges().contains(target) && !target.getEdges().contains(source)) {
                mlg.addEdge(source, target);
            }
        }

    }


    /**
     * @// TODO: 03.06.2019  substitutions in hashset aendern
     * iteration ueber edges nicht knoten und damit bestimmung der zu mergenden knoten
     * begrenzung der schritte dynamischen auf graphgroese
     */
}

