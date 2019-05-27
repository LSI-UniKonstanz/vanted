package org.vanted.addons.stressminaddon;

import antlr.collections.impl.Vector;
import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import org.AttributeHelper;
import org.Vector2d;
import org.Vector3d;
import org.graffiti.graph.Node;
import org.vanted.addons.stressminaddon.util.NodeValueMatrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class IntuitiveIterativePositionAlgorithm implements IterativePositionAlgorithm
{

    /**
     * Performs the next iteration and calculates the new positions from the old ones.
     *
     * @param nodes
     *      the nodes to be worked on. The indices of the nodes in this list correspond to
     *      the indices used by {@code distances} and {@code weights}.
     * @param distances
     *      the matrix containing the node graphical distances between the nodes.
     *      The implementing class shall only read the values in this matrix.
     * @param weights
     *      the matrix containing the node graphical distances between the nodes.
     *      The implementing class shall only read the values in this matrix.
     * @return result
     *      result is a type of List<Vector2d> that contains the new Layout
     */
    @Override
    public HashMap<Node, Vector2d> nextIteration(List<Node> nodes, NodeValueMatrix distances, NodeValueMatrix weights) {


        /**
         * create an ArrayList for saving the node position
         * fill the ArrayList with the positions of nodes from Graph
         */
        ArrayList<Vector2d> positions = new ArrayList<>(nodes.size());
        for (Node n : nodes){
            positions.add(AttributeHelper.getPositionVec2d(n));
        }

        for (int current = 0; current < nodes.size(); current++){
            Vector2d currentNode = positions.get(current); //node position

            double newXPos = 0;     // X position from the moved node
            double newYPos = 0;     // Y position from the moved node
            double totalWeight = 0; // sum of every weight between nodes

            for (int other = 0; other < nodes.size(); other++){
                if (current == other){
                    continue;
                }
                Vector2d otherNode = positions.get(other);

                double euclideanDist = euclidDistance(currentNode,otherNode);   //get the euclidean distance
                double weight = weights.get(current, other);                    //get the weight from weight-matrix
                double desDistance = distances.get(current, other);             //get ideal distance from distances-matrix
                double otherX = otherNode.x;                                    //get the new x-position from other-nodes
                double otherY = otherNode.y;                                    //get the new y-position from other-nodes

                /**
                 * calculate only with distance greater 0
                 */
                if (euclideanDist != 0) {
                    otherX += desDistance * (currentNode.x - otherX) / euclideanDist;
                }
                if (euclideanDist != 0) {
                    otherY += desDistance * (currentNode.y - otherY) / euclideanDist;
                }

                newXPos += weight * otherX;                 //pre-finale new x-position
                newYPos += weight * otherY;                 //pre-finale new y-position
                totalWeight += weight;                      //sum up every weight

                if (totalWeight != 0) {
                    currentNode.x = newXPos / totalWeight;  //finale new x-position
                    currentNode.y = newYPos / totalWeight;  //finale new y-position
                }


            }

        }
        // put the distances back into the graph
        HashMap<Node, Vector2d> result = new HashMap<>(nodes.size());
        for (int i = 0; i < nodes.size(); i++) {
            result.put(nodes.get(i), positions.get(i));
        }
        GraphHelper.applyUndoableNodePositionUpdate(result, "Iterative Position Step");

        return result;
    }

    /**
     * Calculate the euclidean distance
     *
     * @param currentNode - the 2D node that has to change position
     * @param otherNode - a 2D node from the graph
     * @return distance - euclidean distance
     */
    public double euclidDistance(Vector2d currentNode, Vector2d otherNode){

        double xDiff = currentNode.x -otherNode.x;
        double yDiff = currentNode.y - otherNode.y;
        double distance = Math.sqrt(xDiff * xDiff + yDiff * yDiff);

        return distance;
    }

}