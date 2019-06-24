package org.vanted.addons.multilevelframework.sm_util;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import org.AttributeHelper;
import org.Vector2d;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.graphics.CoordinateAttribute;

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
     * {@link GraphHelper#getConnectedComponents(Collection)}
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
     * This action can be undone.<br>
     * This code is almost an exact copy of
     * {@link de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.connected_components.ConnectedComponentLayout#layoutConnectedComponents(Graph)}
     * except for a few adjustments
     * <ul>
     *     <li>regard only connected components in the selection</li>
     *     <li>use individual bounds method
     *     {@link ConnectedComponentsHelper#getConnectedComponentBounds(List, double, double, double, double)}</li>
     *     <li>make changes undoable (in one step)</li>
     *     <li>maybe make some perceived improvements to code regarding duplicates and strange code</li>
     * </ul>
     * <br>
     * <br>
     * The default values for the componet bound margins are set to
     * {@code marginFractionWidth = marginFractionHeight = 0.1} and
     * {@code minMarginWidth = minMarginHeight = 10.0} respectively.
     *
     * @param connectedComponents
     *      the connected components to be layouted.
     * @param isUndoable whether this action should be undoable.
     *
     * @throws IllegalArgumentException if any fraction is negative.
     *
     * @see de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.connected_components.ConnectedComponentLayout#layoutConnectedComponents(Graph)
     * @see ConnectedComponentsHelper#layoutConnectedComponents(Set, double, double, double, double, boolean)
     *
     * @author Jannik
     */
    public static void layoutConnectedComponents(final Set<List<Node>> connectedComponents, final boolean isUndoable) {
        ConnectedComponentsHelper.layoutConnectedComponents(connectedComponents,
                0.1, 0.1, 10, 10, isUndoable);
    }

    /**
     * Layout connected components so that they do not overlap.
     * This action can be undone.<br>
     *
     * This code is almost an exact copy of
     * {@link de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.connected_components.ConnectedComponentLayout#layoutConnectedComponents(Graph)}
     * except for a few adjustments:
     * <ul>
     *     <li>regard only connected components in the selection</li>
     *     <li>use individual bounds method
     *     {@link ConnectedComponentsHelper#getConnectedComponentBounds(List, double, double, double, double)}</li>
     *     <li>make changes undoable (in one step)</li>
     *     <li>maybe make some perceived improvements to code regarding duplicates and strange code</li>
     * </ul>
     *
     * @param connectedComponents
     *      the connected components to be layouted.
     *
     * @param marginFractionWidth
     *      the size of the additional width margin as fraction of the calculated width of every component.
     *      Must be {@code >= 0}.
     * @param marginFractionHeight
     *      the size of the additional height margin as fraction of the calculated height of every component.
     *      Must be {@code >= 0}.
     * @param minMarginWidth the minimum width margin to be used for every component.
     * @param minMarginHeight the minimum height margin to be used for every component.
     * @param isUndoable whether this action should be undoable.
     *
     * @throws IllegalArgumentException if any fraction is negative.
     *
     * @see de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.connected_components.ConnectedComponentLayout#layoutConnectedComponents(Graph)
     * @see ConnectedComponentsHelper#getConnectedComponentBounds(List, double, double, double, double)
     *
     * @author Jannik
     */
    public static void layoutConnectedComponents(final Set<List<Node>> connectedComponents,
                                                 final double marginFractionWidth, final double marginFractionHeight,
                                                 final double minMarginWidth, final double minMarginHeight,
                                                 final boolean isUndoable) {
        // #######################################################################################
        // preprocessing

        Objects.requireNonNull(connectedComponents);
        if (connectedComponents.size() <= 1)
            return;

        ArrayList<List<Node>> componentList = new ArrayList<>(connectedComponents);

        // sort by node count
        componentList.sort((l1, l2) -> l2.size() - l1.size());

        // get components bounds
        Rectangle2D.Double[] componentsBoundsWithAnchor = new Rectangle2D.Double[componentList.size()];
        for (int i = 0; i < componentList.size(); i++) {
            componentsBoundsWithAnchor[i] = ConnectedComponentsHelper.getConnectedComponentBounds(componentList.get(i), marginFractionWidth,
                    marginFractionHeight, minMarginWidth, minMarginHeight);
        }

        // #######################################################################################
        // arrange components
        Vector2d[] componentOffsets = new Vector2d[componentList.size()];
        componentOffsets[0] = new Vector2d(0, 0);

        Vector2d[] bestComponentOffsets = new Vector2d[componentOffsets.length];
        double bestScore = 0;

        for (int numberOfCCsInFirstLine = 1; numberOfCCsInFirstLine < componentList.size(); numberOfCCsInFirstLine++) {
            // get maximum row Width
            double maxRowWidth = 0;
            double maxRowHeight = componentOffsets[0].y;

            for (int i = 0; i < numberOfCCsInFirstLine; i++)
                maxRowWidth += componentsBoundsWithAnchor[i].width;

            double actRowOffsetY = 0;
            double actRowOffsetX = 0;

            for (int actCcCount = 0; actCcCount < componentList.size(); actCcCount++) {
                double actEndX = actRowOffsetX + componentsBoundsWithAnchor[actCcCount].width;
                double actEndY = actRowOffsetY + componentsBoundsWithAnchor[actCcCount].height;

                if (actEndX <= maxRowWidth) {
                    // extends current row
                    componentOffsets[actCcCount] = new Vector2d(actRowOffsetX, actRowOffsetY);

                    actRowOffsetX = actEndX;

                    if (actEndY > maxRowHeight)
                        maxRowHeight = actEndY;
                } else {
                    // start new row - complete line reset

                    actRowOffsetX = 0;
                    actRowOffsetY = maxRowHeight;

                    componentOffsets[actCcCount] = new Vector2d(actRowOffsetX, actRowOffsetY);

                    actRowOffsetX = componentsBoundsWithAnchor[actCcCount].width;
                    maxRowHeight = actEndY;
                }
            }

            // assess result and remember the best
            double score = (maxRowWidth > maxRowHeight) ? maxRowHeight / maxRowWidth : maxRowWidth / maxRowHeight;

            if (score > bestScore) {
                bestComponentOffsets = componentOffsets;
                bestScore = score;
            } else
                // every further result will be worse
                break;
        }

        HashMap<Node, Vector2d> newNodePositions = new HashMap<>();
        HashMap<CoordinateAttribute, Vector2d> newBendPositions = new HashMap<>();

        // #######################################################################################
        // assign node position
        for (int i = 0; i < componentList.size(); i++) {
            Rectangle2D.Double rect = componentsBoundsWithAnchor[i]; // faster access

            double shiftX = bestComponentOffsets[i].x - rect.x;
            double shiftY = bestComponentOffsets[i].y - rect.y;

            Set<Edge> conComponentEdges = new HashSet<>();
            // move nodes
            for (Node node : componentList.get(i)) {
                Vector2d pos = AttributeHelper.getPositionVec2d(node);
                pos.x += shiftX;
                pos.y += shiftY;
                newNodePositions.put(node, pos);
                // prepare edges
                conComponentEdges.addAll(node.getEdges());
            }

            // move edge bends
            // TODO maybe interpolate bend position if edge leads outside of connected component
            for (Edge edge : conComponentEdges) {
                for (CoordinateAttribute bendCA : AttributeHelper.getEdgeBendCoordinateAttributes(edge)) {
                    Vector2d pos = new Vector2d(bendCA.getX() + shiftX, bendCA.getY() + shiftY);
                    newBendPositions.put(bendCA, pos);
                }
            }
        }
        // do move
        if (isUndoable) {
            GraphHelper.applyUndoableNodeAndBendPositionUpdate(
                    newNodePositions, newBendPositions, "Layout connected components");
        } else {
            for (Map.Entry<Node, Vector2d> entry : newNodePositions.entrySet()) {
                AttributeHelper.setPosition(entry.getKey(), entry.getValue());
            }
            for (Map.Entry<CoordinateAttribute, Vector2d> entry : newBendPositions.entrySet()) {
                entry.getKey().setCoordinate(entry.getValue().x, entry.getValue().y);
            }
        }
    }

    /**
     * Calculates the bounding rectangle of a connected component.
     *
     * @param connectedComponent the connected component which's bounds shall be calculated.
     *
     * @param marginFractionWidth the size of the additional width margin as fraction of the calculated width. Must be {@code >= 0}.
     * @param marginFractionHeight the size of the additional height margin as fraction of the calculated height. Must be {@code >= 0}.
     * @param minMarginWidth the minimum width margin to be used.
     * @param minMarginHeight the minimum height margin to be used.
     *
     * @return the bounds of the connected component.
     *
     * @throws IllegalArgumentException if any fraction is negative.
     *
     * @author Jannik
     */
    public static Rectangle2D.Double getConnectedComponentBounds(final List<Node> connectedComponent,
                                                                 final double marginFractionWidth, final double marginFractionHeight,
                                                                 final double minMarginWidth, final double minMarginHeight) {
        if (marginFractionWidth < 0)
            throw new IllegalArgumentException("Margin fraction width must be non negative: " + marginFractionWidth);
        if (marginFractionHeight < 0)
            throw new IllegalArgumentException("Margin fraction height must be non negative: " + marginFractionHeight);

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

        double marginWidth = width*marginFractionWidth;
        double marginHeight = height*marginFractionHeight;
        if (marginWidth < minMarginWidth)   marginWidth = minMarginWidth;
        if (marginHeight < minMarginHeight) marginHeight = minMarginHeight;

        return new Rectangle2D.Double(xPosMin - marginWidth, yPosMin - marginHeight,
                width + 2*marginWidth, height + 2*marginHeight);
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
