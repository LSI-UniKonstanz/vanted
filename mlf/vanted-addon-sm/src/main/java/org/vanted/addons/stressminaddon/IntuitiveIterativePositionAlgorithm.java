package org.vanted.addons.stressminaddon;

import org.Vector2d;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.parameter.Parameter;
import org.vanted.addons.stressminaddon.util.NodeValueMatrix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An implementation of the {@link IterativePositionAlgorithm} that calculates the new
 * distances for every node separately by using the vote of all other nodes.
 * This is more intuitive.
 *
 * @author René
 */
public class IntuitiveIterativePositionAlgorithm implements IterativePositionAlgorithm
{

    /**
     * Performs the next iteration and calculates the new positions from the old ones.
     *
     * @param nodes
     *      the nodes to be worked on. The indices of the nodes in this list correspond to
     *      the indices used by {@code distances} and {@code weights}.
     * @param positions
     *      the positions that are used as basis for the next iteration.
     * @param distances
     *      the matrix containing the node theoretical distances between the nodes.
     * @param weights
     *      the matrix containing the weights associated with each node pair.
     *
     * @return
     *      the newly iterated positions of the nodes in an list with the same
     *
     * @author René, Jannik
     */
    @Override
    public List<Vector2d> nextIteration(final List<Node> nodes, final List<Vector2d> positions,
                                        final NodeValueMatrix distances, final NodeValueMatrix weights) {
        ArrayList<Vector2d> newPositions = new ArrayList<>(positions.size());
        for (Vector2d position : positions) { // create a copy of the positions
            newPositions.add(new Vector2d(position));
        }

        final int numberOfNodes = nodes.size();

        for (int current = 0; current < numberOfNodes; current++){

            Vector2d currentNode = newPositions.get(current);  //node position
            double newXPos = 0;                             // X position from the moved node
            double newYPos = 0;                             // Y position from the moved node
            double totalWeight = 0;                         // sum of every weight between nodes

            for (int other = 0; other < nodes.size(); other++){
                if (current == other){
                    continue;
                }
                Vector2d otherNode = newPositions.get(other);

                double euclideanDist = currentNode.distance(otherNode);   //get the euclidean distance
                double weight       = weights.get(current, other);        //get the weight from weight-matrix
                double desDistance  = distances.get(current, other);      //get ideal distance from distances-matrix
                double otherX       = otherNode.x;                        //get the new x-position from other-nodes
                double otherY       = otherNode.y;                        //get the new y-position from other-nodes

                /*
                 * calculate only with distance greater 0
                 */
                if (euclideanDist != 0) {
                    otherX  += desDistance * (currentNode.x - otherX) / euclideanDist;
                }
                if (euclideanDist != 0) {
                    otherY  += desDistance * (currentNode.y - otherY) / euclideanDist;
                }

                newXPos     += weight * otherX;             //pre-finale new x-position
                newYPos     += weight * otherY;             //pre-finale new y-position
                totalWeight += weight;                      //sum up every weight

            }
            
            if (totalWeight != 0) {
                currentNode.x = newXPos / totalWeight;  //finale new x-position
                currentNode.y = newYPos / totalWeight;  //finale new y-position
            }

        }
        // make the positions unmodifiable
        return Collections.unmodifiableList(newPositions);
    }

    /**
     * Gets a list of {@link Parameter}s this {@link IntuitiveIterativePositionAlgorithm}
     * provides.
     *
     * @return gets a list of Parameters this class provides.
     * @author Jannik
     * @see Algorithm#getParameters()
     */
    @Override
    public Parameter[] getParameters() {
        // TODO implement settings
        return new Parameter[0];
    }

    /**
     * Provides a list of {@link Parameter}s that should be accepted by
     * the {@link IntuitiveIterativePositionAlgorithm}.<br>
     * This setter should only be called if the {@link IntuitiveIterativePositionAlgorithm}
     * not currently used by the top level algorithm.
     *
     * @param parameters the parameters to be set.
     * @author Jannik
     * @see Algorithm#setParameters(Parameter[])
     */
    @Override
    public void setParameters(Parameter[] parameters) {
        // TODO implement settings
    }

    /**
     * @return the name of the this algorithm.
     *         This may be be used to represent this class to the user.
     * @author Jannik
     */
    @Override
    public String getName() {
        return "Intuitive Algorithm";
    }

    /**
     * @return the description of the this algorithm.
     *         This may be be used to explain the behaviour of this class to the user.
     * @author Jannik
     */
    @Override
    public String getDescription() {
        return "<html>This algorithm uses the position of each node to “vote” for the<br>" +
                "for the node of the current node i.e. nodes will be moved<br>" +
                "to the weighted average of all other node positions.<br>" +
                "Running time <code>amountNodes<sup>2</sup></code>.</html>";
    }
}