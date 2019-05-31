package org.vanted.addons.stressminaddon.util;

import org.AttributeHelper;
import org.Vector2d;
import org.graffiti.graph.Node;

import java.awt.geom.Rectangle2D;
import java.util.*;

/**
 * Contains utility and helper methods for working with connected components.
 * @author Jannik
 */
public enum ConnectedComponentsHelper {
    ; // No need for instances.

    /**
     * Returns a {@link Set} containing all connected components in the given <code>workingSet</code>
     * as unmodifiable {@link RandomAccess} lists.
     *
     * This code is currently almost an exact copy of
     * {@link de.ipk_gatersleben.ag_nw.graffiti.GraphHelper#getConnectedComponents(Collection)}
     * with the addition that only nodes from the working set are regarded, not all nodes that share the same
     * connected component.
     *
     * @param workingSet
     *      nodes to work with. Only nodes in this set will be added to the returned connected components.
     *
     * @return
     *      a {@link Set} of the connected components that are contained in the <code>workingSet</code> and
     *      are saved in unmodifiable, {@link RandomAccess} lists.
     *
     * @author Christian Klukas, Jannik
     */
    public static Set<List<Node>> getConnectedComponents(final Collection<Node> workingSet) {
        Set<Set<Node>> nodeSetSet = new HashSet<>();

        // get all RefSets
        Set<Node> allNodes = new HashSet<>(workingSet);

        Set<Node> alreadyContainedNodes = new HashSet<>();

        for (Node startNode : allNodes) {
            if (alreadyContainedNodes.contains(startNode)) {
                continue;
            }

            // set of unlabeled nodes
            Set<Node> connectedComponent = new HashSet<>();
            nodeSetSet.add(connectedComponent);

            // Set of unlabeled refSets to visit
            Stack<Node> nodesToProcess = new Stack<>();
            nodesToProcess.push(startNode);

            // find and add all connected refSets
            while (!nodesToProcess.isEmpty()) {
                Node current = nodesToProcess.pop();

                // add new startNode
                connectedComponent.add(current);

                // get all adjacent nodes
                Set<Node> neighbors = current.getNeighbors();

                // add all new refSets
                for (Node neighbor : neighbors) {
                    if (!connectedComponent.contains(neighbor) && !nodesToProcess.contains(neighbor) &&
                            allNodes.contains(neighbor)) { // new neighbour found that is in working set
                        nodesToProcess.push(neighbor);
                    }
                }
            }
            // mark all nodes as seen
            alreadyContainedNodes.addAll(connectedComponent);
        }
        // prepare the final result
        HashSet<List<Node>> result = new HashSet<>(nodeSetSet.size());
        for (Set<Node> connectedComponent : nodeSetSet) {
            result.add(Collections.unmodifiableList(new ArrayList<>(connectedComponent)));
        }
        return result;
    }

    /**
     * Layout connected components so that they do not overlap.
     * This action can be undoable.
     *
     * @param connectedComponents
     *      the connected components to be layouted.
     *
     * @author Jannik
     */
    public static void layoutConnectedComponents(final Set<List<Node>> connectedComponents,
                                                 final double scaleWidth, final double scaleHeight,
                                                 final double minWidth, final double minHeight) {
        // TODO
    }

    /**
     * Calculates the bounding rectangle of a connected component.
     *
     * @param connectedComponent the connected component which's bounds shall be calculated.
     * @param scaleWidth the amount to scale the width by. Must be >= 1.
     * @param scaleHeight the amount to scale the height by. Must be >= 1.
     * @param minWidth the minimum width to be used.
     * @param minHeight the minimum height to be used.
     *
     * @return the bounds of the connected component.
     *
     * @see de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.connected_components.ConnectedComponentLayout#getBoundsOfNodes(Set)
     *
     * @author Jannik
     */
    public static Rectangle2D.Double getConnectedComponetBounds(final List<Node> connectedComponent,
                                                                final double scaleWidth, final double scaleHeight,
                                                                final double minWidth, final double minHeight) {
        if (scaleWidth < 1)  throw new IllegalArgumentException("Scaling width must not be smaller than 1: " + scaleWidth);
        if (scaleHeight < 1) throw new IllegalArgumentException("Scaling height must not be smaller than 1: " + scaleHeight);

        // positions of the left upper edge of the rectangle
        double xPosMin = Double.POSITIVE_INFINITY;
        double yPosMin = Double.POSITIVE_INFINITY;
        // positions of the right lower edge of the rectangle
        double xPosMax = Double.NEGATIVE_INFINITY;
        double yPosMax = Double.NEGATIVE_INFINITY;

        final Vector2d zeroSize = new Vector2d(0, 0); // use in case of error

        Vector2d position, size;
        for (Node node : connectedComponent) {
            position = AttributeHelper.getPositionVec2d(node); // center of node
            size = AttributeHelper.getSize(node); // x = width, y = height
            if (size == null) size = zeroSize;

            // adjust for upper left position
            position.x -= size.x / 2;
            position.y -= size.y / 2;

            // the smallest (leftest, most upper) value must be chosen
            xPosMin = (position.x < xPosMin) ? position.x : xPosMin;
            yPosMin = (position.y < yPosMin) ? position.y : yPosMin;
            // the biggest (rightest, most lower) value must be chosen
            xPosMax = (position.x + size.x > xPosMax) ? position.x + size.x : xPosMax;
            yPosMax = (position.y + size.y > yPosMax) ? position.y + size.y : yPosMax;
        }

        // additional scaling
        double width = xPosMax - xPosMin;
        double height = yPosMax - yPosMin;
        if (width < minWidth)   width = minWidth;
        if (height < minHeight) height = minHeight;

        double scaledXMargin = (scaleWidth*width - width) / 2;
        double scaledYMargin = (scaleHeight*height - height) / 2;

        return new Rectangle2D.Double(xPosMin - scaledXMargin, yPosMin - scaledYMargin,
                width*scaleWidth, height*scaleHeight);
    }

    /**
     * Gets the maximum width or height of any node in the connected component.
     *
     * @param connectedComponent the connected component which's nodes shall be examined.
     *
     * @return
     *      the maximum dimension found. This can be the width or height of a node.
     *
     * @author Jannik
     */
    public static double getMaxNodeSize(final List<Node> connectedComponent) {
        double max = Double.NEGATIVE_INFINITY;
        Vector2d size;
        for (Node node : connectedComponent) {
            size = AttributeHelper.getSize(node); // x = width, y = height
            if (size == null) { continue; }
            // test new max
            max = (max < size.x) ? size.x : max;
            max = (max < size.y) ? size.y : max;
        }

        return max;
    }
}
