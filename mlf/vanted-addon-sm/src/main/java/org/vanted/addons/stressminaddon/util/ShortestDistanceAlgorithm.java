package org.vanted.addons.stressminaddon.util;

import org.graffiti.graph.Edge;
import org.graffiti.graph.Node;
import org.vanted.addons.stressminaddon.StressMinimizationLayout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ShortestDistanceAlgorithm {

    /**
     * implements runnable
     */
    private class BFSRunnable implements Runnable {

        private Node n;
        private int numberNodes;
        private NodeValueMatrix results;

        /**
         * 
         * @param n       the node BFS starts from
         * @param results matrux the final result is written
         */
        public BFSRunnable(Node n, final ArrayList<Node>  nodes, NodeValueMatrix results) {
            this.n = n;
            this.numberNodes = nodes.size();
            this.results = results;
        }

        @Override
        public void run() {

            // save distances to all nodes from startNode
            double[] distances = new double[numberNodes];
            for (int i = 0; i < distances[numberNodes]; i++) {
                distances[i] = 0;
            }

            int posStartNode = n.getInteger(StressMinimizationLayout.INDEX_ATTRIBUTE);
            distances[posStartNode] = 1;

            // Create a queue for BFS
            LinkedList<Node> queue = new LinkedList<Node>();
            queue.add(n);

            Collection<Edge> edgesOfCurrentNode;
            Node neighbour;
            int currentDistance = 0;

            // BFS
            while (queue.size() != 0) {
                n = queue.poll();
                edgesOfCurrentNode = n.getEdges();
                currentDistance++;

                for (Edge e : edgesOfCurrentNode) {
                    if (e.getSource() == e.getTarget())
                        continue;
                    neighbour = e.getTarget();
                    int posInGraph = n.getInteger(StressMinimizationLayout.INDEX_ATTRIBUTE);
                    if (distances[posInGraph] == 0) {
                        distances[posInGraph] = currentDistance;
                        queue.add(neighbour);
                    }
                }
            }
            // copy distances to resultMatrix
            for (int i = 0; i < posStartNode; i++) {
                results.set(posStartNode, i, distances[i]);
            }
        }
    }

    /**
     * starts executing the BFS on all n nodes
     * 
     * @return a NodeValueMatrix containig the results of all n BFS
     */
    public NodeValueMatrix getShortestPaths(final ArrayList<Node> nodes) {

        int numberOfNodes = nodes.size();
        NodeValueMatrix resultMatrix = new NodeValueMatrix(numberOfNodes);
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        try {
            for (Node n : nodes) {
                executor.execute(new BFSRunnable(n, nodes, resultMatrix));
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
        executor.shutdown();

        return resultMatrix;

    }

}
