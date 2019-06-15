package org.vanted.addons.multilevelframework.pse_hack;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.copy_pattern_layout.CopyPatternLayoutAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.graph_to_origin_mover.CenterLayouterAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.no_overlapp_as_tim.NoOverlappLayoutAlgorithmAS;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.MyNonInteractiveSpringEmb;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.MyTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.NodeCacheEntry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.random.RandomLayouterAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.AttributePathNameSearchType;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.SearchAndSelecAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.SearchType;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import info.clearthought.layout.SingleFiledLayout;
import info.clearthought.layout.TableLayout;
import org.*;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.graphics.CoordinateAttribute;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.algorithm.ThreadSafeAlgorithm;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.parameter.DoubleParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugins.views.defaults.DrawMode;
import org.graffiti.plugins.views.defaults.GraffitiView;
import org.graffiti.selection.Selection;
import org.graffiti.session.EditorSession;

import javax.swing.Timer;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Modified to make executeThreadSafe() block
 *
 * @see de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.PatternSpringembedder
 */
public class BlockingForceDirected extends ThreadSafeAlgorithm
// implements EditorAlgorithm
{

    // private static final int maxThreadCount = 1;

    public static final String springName = "Force Directed (custom)";
    HashMap<String, Double> nodeCombination2skewValue = new HashMap<String, Double>();
    AttributePathNameSearchType cacheValidFor = null;
    // AnimGifEncoder agif;
    boolean createGIF = false;
    int pictureCount = 0;
    private Graph non_interact_graph;
    private Selection non_interact_selection;

    /**
     * An image with an animated progress bar
     */
    // ImageIcon progressImg;

    /**
     * An image with an progress bar, which shows the idle state
     */
    // ImageIcon progressImgOK;
    /**
     * Saves the positions of the clusters. The hashMap contains Vector2d values.
     * The keys are Integers for the cluster numbers. This hashMap might be empty,
     * if no cluster locations are calculated or known.
     */
    private HashMap<String, Vector2d> clusterLocations = new HashMap<String, Vector2d>();
    /**
     * used for non interactive run, is used in the <code>reset</code> method to set
     * the maximum move value back to the desired value.
     */
    private double initLength;
    private double cachedClusterForce;
    private ThreadSafeOptions nonInteractiveTSO;

    /**
     * This method returns a <code>Vector</code> with all
     * <code>NodeCacheEntry</code> entries that have the same pattern type and
     * index.
     *
     * @param options  DOCUMENT ME!
     * @param nodeInfo
     * @return <code>Vector</code> with <code>NodeCacheEntry</code> Objects. The
     * node is returned in the result set, if it has no pattern.
     */
    @SuppressWarnings("unchecked")
    private static ArrayList<NodeCacheEntry> getPatternNodes(ThreadSafeOptions options, NodeCacheEntry nodeInfo) {
        return getPatternNodesPublic((ArrayList<NodeCacheEntry>) options.nodeArray,
                nodeInfo);
    }

    public static ArrayList<NodeCacheEntry> getPatternNodesPublic(ArrayList<NodeCacheEntry> nodeArray,
                                                                  NodeCacheEntry nodeInfo) {
        ArrayList<NodeCacheEntry> resultVec = new ArrayList<NodeCacheEntry>();

        if (!nodeInfo.patternTypeEmpty) {
            for (int i = 0; i < nodeArray.size(); i++) {
                if (!nodeArray.get(i).patternTypeEmpty) {
                    if ((nodeArray.get(i).patternType.compareTo(nodeInfo.patternType) == 0)
                            && (nodeArray.get(i).patternIndex == nodeInfo.patternIndex)) {
                        resultVec.add(nodeArray.get(i));
                    }
                }
                // patternType <> null
            }
        } else {
            resultVec.add(nodeInfo);
        }
        return resultVec;
    }

    /**
     * euclidian distance
     */
    private static double getDistance(org.Vector2d a, org.Vector2d b) {
        return Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
    }

    private static double borderForceX(ThreadSafeOptions options, double x) {
        if (x < options.borderWidth) {
            return Math.max(-options.maxBorderForce / options.borderWidth * x + options.maxBorderForce, 0);
        } else { // return 0;
            return -1;
        }
    }

    private static double borderForceY(ThreadSafeOptions options, double y) {
        if (y < options.borderWidth) {
            return Math.max(-options.maxBorderForce / options.borderWidth * y + options.maxBorderForce, 0);
        } else { // return 0;
            return -1;
        }
    }

    private static double gridForceX(ThreadSafeOptions options, double x) {
        if (options.temperature_max_move < 20)
            return -(x % 10 - 5) * 3;
        else
            return 0;
    }

    private static double gridForceY(ThreadSafeOptions options, double y) {
        if (options.temperature_max_move < 20)
            return -(y % 10 - 5) * 3;
        else
            return 0;
    }

    private static NodeCacheEntry getPatternNodeStructFromNode(ThreadSafeOptions options, Node search) {
        return (NodeCacheEntry) options.nodeSearch.get(search);
    }

    private static void rotate(double angle, ArrayList<NodeCacheEntry> patternNodes, Vector2d centerOfPattern) {
        AffineTransform transform = AffineTransform.getRotateInstance(angle, centerOfPattern.x, centerOfPattern.y);
        for (NodeCacheEntry nce : patternNodes) {
            double currentDistance = getDistance(nce.position, centerOfPattern);
            if (Math.abs(currentDistance) > 0.00001) {
                Point2D.Double ptSrc = new Point2D.Double(nce.position.x, nce.position.y);
                Point2D.Double ptDst = new Point2D.Double(nce.position.x, nce.position.y);
                transform.transform(ptSrc, ptDst);
                nce.position.x = ptDst.getX();
                nce.position.y = ptDst.getY();
            }
        }
    }

    private static double linearTransformation(double value, double minS, double maxS, double minT, double maxT) {
        if (value <= minS)
            return minT;
        if (value >= maxS)
            return maxT;
        return (value - minS) / (maxS - minS) * (maxT - minT) + minT;
    }

    /**
     * Determines if two nodes belong to the same pattern.
     *
     * @param n1 Node 1
     * @param n2 Node 2
     * @return True, if the two nodes belong to the same pattern. False, if not.
     */
    private static boolean samePattern(NodeCacheEntry n1, NodeCacheEntry n2) {
        boolean sameNode = n1.nodeIndex == n2.nodeIndex;

        if (sameNode) {
            return true;
        }

        boolean noPattern = ((n1.patternTypeEmpty) && (n2.patternTypeEmpty));

        if (noPattern) {
            return false;
        }

        boolean samePattern = (n1.patternType.compareTo(n2.patternType) == 0);
        boolean sameIndex = n1.patternIndex == n2.patternIndex;

        return samePattern && sameIndex;
    }

    /**
     * returns length of move vector
     *
     * @param options options var for the thread
     * @param moveVec movement vector (will be modified to be smaller than maxmove)
     * @param node    node to be moved
     * @return actual movement
     */
    private static double moveNode(ThreadSafeOptions options, org.Vector2d moveVec, NodeCacheEntry node) {
        double l = Math.sqrt(moveVec.x * moveVec.x + moveVec.y * moveVec.y);

        if (l > options.temperature_max_move) {
            moveVec.x = moveVec.x / l * options.temperature_max_move;
            moveVec.y = moveVec.y / l * options.temperature_max_move;
            l = options.temperature_max_move;
        }
        // if (moveVec.x!=Double.NaN && moveVec.y!=Double.NaN) {
        node.position.x += moveVec.x;
        node.position.y += moveVec.y;
        return l;
        /*
         * } else return 0;
         */
    }

    /**
     * Defines if a force between two nodes should be calculated.
     *
     * @param n1   Node 1
     * @param relN Node 2
     * @return True, if a force between the two nodes should be calculated, false if
     * not
     */
    private static boolean calcForce(NodeCacheEntry n1, NodeCacheEntry relN) {
        if (n1.nodeIndex == relN.nodeIndex) {
            return false;
        }

        if (n1.patternTypeEmpty) {
            return true;
        }

        return !samePattern(n1, relN);
    }

    /**
     * Init method for cache
     *
     * @param options the thread-safe options
     * @param nodeI   the node to be analyzed
     * @return An <code>Vector</code> with the connected nodes.
     * (<code>NodeCacheEntry</code> list)
     */
    private static ArrayList<NodeCacheEntry> getConnectedNodes(ThreadSafeOptions options, NodeCacheEntry nodeI) {
        ArrayList<NodeCacheEntry> connectedNodes = new ArrayList<NodeCacheEntry>();
        for (Node tempNode : nodeI.node.getNeighbors()) {
            NodeCacheEntry n2 = getPatternNodeStructFromNode(options, tempNode);
            if (n2 == null)
                System.err.println("ERROR: Node " + tempNode.getID() + " not found in nodeSearch map!");
            else
                connectedNodes.add(n2);
        }

        return connectedNodes;
    }

    private static void addGUIelements(final ThreadSafeOptions options, JComponent jc, final JButton attributeSelection,
                                       JCheckBox edgeWeight, JLabel labelSliderLength, JSlider sliderLength, JSlider sliderEnergyHor,
                                       JSlider sliderEnergyVert, JSlider sliderMultiplyRepulsiveClusters, JSlider sliderMultiplyRepulsive,
                                       JLabel stiffnessDesc, JSlider sliderStiffnes, final JCheckBox useClusterInfo, JSlider sliderClusterForce,
                                       final JSlider tempSlider, JCheckBox finishToTop, JCheckBox borderForce, JCheckBox randomInit,
                                       JCheckBox removeOverlapping) {

        edgeWeight.setOpaque(false);
        useClusterInfo.setOpaque(false);
        finishToTop.setOpaque(false);
        borderForce.setOpaque(false);
        randomInit.setOpaque(false);
        removeOverlapping.setOpaque(false);

        attributeSelection.setOpaque(false);
        sliderLength.setOpaque(false);
        sliderEnergyHor.setOpaque(false);
        sliderEnergyVert.setOpaque(false);
        sliderMultiplyRepulsiveClusters.setOpaque(false);
        sliderMultiplyRepulsive.setOpaque(false);
        sliderStiffnes.setOpaque(false);
        sliderClusterForce.setOpaque(false);
        tempSlider.setOpaque(false);

        FolderPanel edgePanel = new FolderPanel("Edge Forces (attraction)", false, true, false, null);
        FolderPanel nodePanel = new FolderPanel("Node Forces (repulsion)", false, true, false, null);
        FolderPanel clusterPanel = new FolderPanel("Layout of Clusters", true, true, false, null);
        FolderPanel searchPanel = new FolderPanel("Layout of Search-Subgraphs", true, true, false, null);

        // edge panel
        JComponent edgeLength = TableLayout.getDoubleRow(labelSliderLength,
                TableLayout.getSplitVertical(sliderLength,
                        TableLayout.getSplit(edgeWeight, attributeSelection, TableLayout.PREFERRED, TableLayout.FILL),
                        TableLayout.PREFERRED, TableLayout.PREFERRED),
                Color.WHITE);

        JComponent stiffness = TableLayout.getDoubleRow(stiffnessDesc, sliderStiffnes, Color.WHITE);
        edgePanel.addGuiComponentRow(null, edgeLength, false, 2);
        edgePanel.addGuiComponentRow(null, stiffness, false, 2);
        edgePanel.layoutRows();
        jc.add(edgePanel);

        // node panel
        JComponent horForce = TableLayout.getDoubleRow(new JLabel("Horizontal and vertical forces:"), sliderEnergyHor,
                Color.WHITE);

        nodePanel.addGuiComponentRow(null, horForce, false, 2);
        nodePanel.addGuiComponentRow(null, sliderEnergyVert, false, 2);

        JCheckBox degreeForce = new JCheckBox("Consider node degree", options.doMultiplyByNodeDegree);
        degreeForce.setToolTipText(
                "<html>" + "If enabled, the repulsive forces of a node to the remaining nodes are multiplied by<br>"
                        + "its number of connections to other nodes. Highly connected nodes will get more room.");
        degreeForce.setOpaque(false);
        degreeForce.setSelected(options.doMultiplyByNodeDegree);
        degreeForce.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                options.doMultiplyByNodeDegree = ((JCheckBox) e.getSource()).isSelected();
            }
        });
        nodePanel.addGuiComponentRow(null, degreeForce, false, 2);

        nodePanel.layoutRows();
        jc.add(nodePanel);

        FolderPanel progress = new FolderPanel("Progress", false, true, false, null);
        progress.addGuiComponentRow(null, tempSlider, false, 2);
        progress.layoutRows();
        jc.add(progress);

        // cluster panel
        if (ReleaseInfo.getIsAllowedFeature(FeatureSet.TAB_PATTERNSEARCH)) {
            JComponent multClusterForce = TableLayout.getDoubleRow(
                    new JLabel("Multiply repulsive forces between different clusters:"),
                    sliderMultiplyRepulsiveClusters, Color.WHITE);
            clusterPanel.addGuiComponentRow(null, multClusterForce, false, 2);
            JComponent clusterAttractiveForce = TableLayout.getDoubleRow(useClusterInfo, sliderClusterForce,
                    Color.WHITE);
            clusterPanel.addGuiComponentRow(null, clusterAttractiveForce, false, 2);
            clusterPanel.layoutRows();
            jc.add(clusterPanel);
        }

        // search-subgraph panel
        if (ReleaseInfo.getIsAllowedFeature(FeatureSet.TAB_PATTERNSEARCH)) {
            JComponent multForceSlider = TableLayout.getDoubleRow(
                    new JLabel("Multiply repulsive forces between search-subgraphs and remaining nodes:"),
                    sliderMultiplyRepulsive, Color.WHITE);
            JCheckBox copyLayout = new JCheckBox("Init: Apply Search-Subgraph Layout", options.doCopyPatternLayout);
            copyLayout.setOpaque(false);
            copyLayout.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    options.doCopyPatternLayout = ((JCheckBox) e.getSource()).isSelected();
                }
            });
            JCheckBox rotatePattern = new JCheckBox("Rotation of Search-Subgraphs",
                    options.getBval(myOp.BvalIndexRotatePatternIndex, false));
            rotatePattern.setOpaque(false);
            searchPanel.addGuiComponentRow(null, multForceSlider, false, 2);
            searchPanel.addGuiComponentRow(null, copyLayout, false, 2);
            searchPanel.addGuiComponentRow(null, rotatePattern, false, 2);
            searchPanel.layoutRows();
            jc.add(searchPanel);
        }

        FolderPanel other = new FolderPanel("Options, Post/Pre-Processing", true, true, false, null);
        other.addGuiComponentRow(null, borderForce, false, 2);
        other.addGuiComponentRow(null, randomInit, false, 2);

        if (ReleaseInfo.getIsAllowedFeature(FeatureSet.TAB_PATTERNSEARCH)) {

            JCheckBox gridForce = new JCheckBox("Finish: Grid Force",
                    options.getBval(myOp.BvalIndexGridForceIndex, true));
            gridForce.setOpaque(false);
            gridForce.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    boolean newVal = ((JCheckBox) e.getSource()).isSelected();
                    options.setBval(myOp.BvalIndexGridForceIndex, newVal);
                }
            });
            other.addGuiComponentRow(null, gridForce, false, 2);
        }
        other.addGuiComponentRow(null, removeOverlapping, false, 2);
        other.addGuiComponentRow(null, finishToTop, false, 2);

        other.layoutRows();
        jc.add(other);

    }

    /**
     * Sets Menu Command Title
     *
     * @return Menu command title
     */
    public String getName() {
        return springName;
    }

    public String toString() {
        return getName();
    }

    public ActionEvent getActionEvent() {
        return null;
    }

    public void setActionEvent(ActionEvent a) {
        // empty
    }

    /**
     * Error Checking
     *
     * @throws PreconditionException
     * @throws PreconditionException DOCUMENT ME!
     */
    public void check() throws PreconditionException {
        if (non_interact_graph == null)
            throw new PreconditionException("No graph available!");
    }

    /**
     * @return If position-update for Nodes are done, length of movement vectors
     */
    private double doSpringEmbedder(final ThreadSafeOptions options, final int runValue, final int n,
                                    final int threadCount, ExecutorService run) {
        options.setDouble(0);
        final ThreadSafeOptions tso = new ThreadSafeOptions();
        tso.setInt(threadCount);
        for (int t = 0; t < threadCount; t++) {
            final int tt = t;
            run.submit(new Runnable() {
                public void run() {
                    for (int i = tt; i < n; i += threadCount) {
                        double res = doCalcAndMoveNode(options, runValue, options.getDouble(), i);
                        options.addDouble(res);
                    }
                    tso.addInt(-1);
                }
            });
        }

        try {
            while (tso.getInt() > 0) {
                Thread.sleep(1);
            }
        } catch (InterruptedException e) {
            ErrorMsg.addErrorMessage(e);
        }

        return options.getDouble();
    }

    double doCalcAndMoveNode(ThreadSafeOptions options, int runValue, double returnValue,
                             int indexOfNodeToBeProcessed) {
        NodeCacheEntry nodeI = (NodeCacheEntry) options.nodeArray.get(indexOfNodeToBeProcessed);

        boolean calcNode = true;

        // in case the node has been "touched" before, do not calc again
        if (nodeI.lastTouch >= runValue)
            calcNode = false;

        // in case the current option says, move only selected nodes and the current
        // node
        // is not selected, then do not calc this node
        if (options.getSelection().getNodes().size() > 0 && !nodeI.selected)
            calcNode = false;

        if (calcNode) {
            org.Vector2d force = new org.Vector2d(0, 0);
            org.Vector2d sumForce = new org.Vector2d(0, 0);
            for (int patternI = 0; patternI < nodeI.patternNodes.size(); patternI++) {
                NodeCacheEntry patternNode = nodeI.patternNodes.get(patternI);

                patternNode.lastTouch = runValue;
                calcSpringEmbedderForce(options, patternNode, force, sumForce);
            }
            double s0 = Math.abs(sumForce.x) + Math.abs(sumForce.y);
            // s0 = Double.MAX_VALUE;
            force.x /= nodeI.patternNodes.size();
            force.y /= nodeI.patternNodes.size();
            force.x /= 7;
            force.y /= 7;
            for (int patternI = 0; patternI < nodeI.patternNodes.size(); patternI++) {
                NodeCacheEntry patternNode = nodeI.patternNodes.get(patternI);
                returnValue += moveNode(options, force, patternNode);
            }
            boolean calcRotation = nodeI.patternNodes.size() > 1
                    && options.getBval(myOp.BvalIndexRotatePatternIndex, true);
            if (calcRotation) {
                double plusMinusAngle = linearTransformation(options.temperature_max_move, 0, 300, 0,
                        10 * Math.PI / 180);
                Vector2d centerOfPattern = NodeTools.getCenter(nodeI.patternNodes);
                rotate(+plusMinusAngle, nodeI.patternNodes, centerOfPattern);
                org.Vector2d temp = new org.Vector2d(0, 0);
                org.Vector2d forceS1 = new org.Vector2d(0, 0);
                for (int patternI = 0; patternI < nodeI.patternNodes.size(); patternI++) {
                    NodeCacheEntry patternNode = nodeI.patternNodes.get(patternI);
                    calcSpringEmbedderForce(options, patternNode, temp, forceS1);
                }
                rotate(-plusMinusAngle * 2, nodeI.patternNodes, centerOfPattern);
                org.Vector2d forceS2 = new org.Vector2d(0, 0);
                for (int patternI = 0; patternI < nodeI.patternNodes.size(); patternI++) {
                    NodeCacheEntry patternNode = nodeI.patternNodes.get(patternI);
                    calcSpringEmbedderForce(options, patternNode, temp, forceS2);
                }
                rotate(+plusMinusAngle, nodeI.patternNodes, centerOfPattern);
                double s1 = Math.abs(forceS1.x) + Math.abs(forceS1.y);
                double s2 = Math.abs(forceS2.x) + Math.abs(forceS2.y);
                // System.out.println("Rotation forces S0, S1, S2: "+s0+" / "+s1+" / "+s2);
                if (s1 < s0 && s1 < s2)
                    rotate(+plusMinusAngle, nodeI.patternNodes, centerOfPattern);
                if (s2 < s0 && s2 < s1)
                    rotate(-plusMinusAngle, nodeI.patternNodes, centerOfPattern);
            }
        }
        return returnValue;
    }

    /**
     * Calculates the springembedder force for a given node. For all nodes the
     * distance to the given node is calculated and used to calculate an average
     * repulsive force. Optional a border force is used to move the nodes away from
     * the top/left. This force also moves the nodes to left/top if the position is
     * greater than the borderLengths.
     *
     * @param options  The options
     * @param nodeI    For this node the force will be calculated.
     * @param force    The calculation result (RETURN/call by reference).
     * @param sumForce
     */
    private void calcSpringEmbedderForce(ThreadSafeOptions options, NodeCacheEntry nodeI, org.Vector2d force,
                                         Vector2d sumForce) {
        double distance;
        double distanceX;
        double distanceY;

        double d1_1000 = options.getDval(myOp.DvalIndexSliderHorForce, 1000);
        double d2_1000 = options.getDval(myOp.DvalIndexSliderVertForce, 1000);

        double initFx = force.x;
        double initFy = force.y;

        double multiplyRepulsiveForcesSetting = options.getDval(myOp.DmultiplyRepulsiveForces2Patterns, 1d);
        double multiplyRepulsiveForces2setting = options.getDval(myOp.DmultiplyRepulsiveForces2Subgraphs, 1d);
        double multiplyRepulsiveForces3setting = options.getDval(myOp.DmultiplyRepulsiveForces2Clusters, 1d);
        boolean considerForces3 = Math.abs(multiplyRepulsiveForces3setting - 1) > 0.0001;

        double multiplyRepulsiveForces, multiplyRepulsiveForces2, multiplyRepulsiveForces3;

        // Abstoßungskräfte zu restlichen Knoten
        org.Vector2d workA = nodeI.position;
        int sz = options.nodeArray.size();
        for (int i2 = 0; i2 < sz; i2++) {
            NodeCacheEntry nodeI2 = (NodeCacheEntry) options.nodeArray.get(i2);

            if (calcForce(nodeI, nodeI2)) {
                org.Vector2d workB = nodeI2.position;

                distance = getDistance(workA, workB);
                double d_sq = distance * distance;
                distanceX = workA.x - workB.x;
                distanceY = workA.y - workB.y;
                if (distance > 0) {
                    double degree = 1d;
                    if (options.doMultiplyByNodeDegree) {
                        int szz = nodeI.connectedNodes.size();
                        if (szz > 1)
                            degree = szz;
                    }

                    if (nodeI.patternIndex >= 0 || nodeI2.patternIndex >= 0) {
                        multiplyRepulsiveForces = multiplyRepulsiveForcesSetting;
                    } else
                        multiplyRepulsiveForces = 1d;
                    if (nodeI.subgraphIndex != nodeI2.subgraphIndex) {
                        multiplyRepulsiveForces2 = multiplyRepulsiveForces2setting;
                    } else
                        multiplyRepulsiveForces2 = 1d;

                    try {
                        Field cin = NodeCacheEntry.class.getDeclaredField("clusterIndexNumber");
                        cin.setAccessible(true);
                        if (!considerForces3 || cin.get(nodeI).equals(cin.get(nodeI2))) {
                            multiplyRepulsiveForces3 = 1d;
                        } else
                            multiplyRepulsiveForces3 = multiplyRepulsiveForces3setting;
                    } catch (IllegalAccessException | NoSuchFieldException e) {
                        e.printStackTrace();
                        multiplyRepulsiveForces3 = multiplyRepulsiveForces3setting;
                    }

                    force.x += degree * multiplyRepulsiveForces * multiplyRepulsiveForces2 * multiplyRepulsiveForces3
                            * d1_1000 / d_sq * (distanceX / distance);
                    force.y += degree * multiplyRepulsiveForces * multiplyRepulsiveForces2 * multiplyRepulsiveForces3
                            * d2_1000 / d_sq * (distanceY / distance);
                } else {
                    force.x += Math.random() * 2 - 1;
                    force.y += Math.random() * 2 - 1;
                }
            }
        }

        double dv0_10_stiffness = options.getDval(myOp.DvalIndexSliderStiffness, 10);
        double dv3_200_zero_len = options.getDval(myOp.DvalIndexSliderZeroLength, 200);
        // Anziehungskräfte zwischen verbundenen Knoten
        double sumAddX = 0;
        double sumAddY = 0;
        workA = nodeI.position;
        if (!nodeI.connectedNodes.isEmpty()) {
            sz = nodeI.connectedNodes.size();
            for (int i2 = 0; i2 < sz; i2++) {
                NodeCacheEntry nodeI2 = nodeI.connectedNodes.get(i2);

                if (calcForce(nodeI, nodeI2)) {
                    org.Vector2d workB = nodeI2.position;

                    distance = getDistance(workA, workB);
                    distanceX = workB.x - workA.x;
                    distanceY = workB.y - workA.y;
                    if (distance > 0) {
                        double skew = 1;

                        if (options.getBval(myOp.BvalIndexEnableEdgeWeightProcessing, false)) {
                            AttributePathNameSearchType weightAttribute = (AttributePathNameSearchType) options
                                    .getParam(myOp.OvalIndexEdgeWeightAttributeObject, null);
                            if (weightAttribute != null) {
                                skew = calcSkew(nodeI, nodeI2, weightAttribute);
                            }
                        }
                        double currFx;
                        double currFy;
                        currFx = dv0_10_stiffness / 10 * (distance - (dv3_200_zero_len * skew)) * distanceX / distance;
                        currFy = dv0_10_stiffness / 10 * (distance - (dv3_200_zero_len * skew)) * distanceY / distance;
                        force.x += currFx;
                        force.y += currFy;
                        sumAddX += -currFx + dv0_10_stiffness / 10 * distanceX;
                        sumAddY += -currFy + dv0_10_stiffness / 10 * distanceY;
                    } else {
                        force.x += Math.random() * 2 - 1;
                        force.y += Math.random() * 2 - 1;
                    }
                } // if calc force
            }
        }

        boolean calcGridForce = options.getBval(myOp.BvalIndexGridForceIndex, false);
        if (calcGridForce) {
            force.x += gridForceX(options, nodeI.position.x) / nodeI.patternNodes.size();
            force.y += gridForceY(options, nodeI.position.y) / nodeI.patternNodes.size();
        }

        if (options.borderForce) {
            force.x += borderForceX(options, nodeI.position.x) / nodeI.patternNodes.size();
            force.y += borderForceY(options, nodeI.position.y) / nodeI.patternNodes.size();
        }

        if (options.getBval(myOp.BvalIndexDoClusterLayoutIndex, false)) {
            try {
                Field cin = NodeCacheEntry.class.getDeclaredField("clusterIndexNumber");
                cin.setAccessible(true);
                Vector2d clusterPosition = clusterLocations.get(cin.get(nodeI).toString());
                if (clusterPosition != null)
                    applyMagneticClusterForce(options, force, nodeI.position, clusterPosition);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        sumForce.x += Math.abs(force.x + sumAddX - initFx);
        sumForce.y += Math.abs(force.y + sumAddY - initFy);
    }

    private double calcSkew(NodeCacheEntry nodeI, NodeCacheEntry nodeI2, AttributePathNameSearchType weightAttribute) {
        String key = nodeI.nodeIndex + ";" + nodeI2.nodeIndex;
        if (cacheValidFor != null && cacheValidFor != weightAttribute && nodeCombination2skewValue.size() > 0)
            nodeCombination2skewValue.clear();
        else if (weightAttribute != null)
            cacheValidFor = weightAttribute;
        if (cacheValidFor != null && nodeCombination2skewValue.containsKey(key))
            return nodeCombination2skewValue.get(key);
        double weight = 0;
        double skew = 1;
        boolean found = false;
        for (org.graffiti.graph.Edge e : nodeI.node.getEdges()) {
            if (e.getSource() == nodeI.node && e.getTarget() == nodeI2.node) {
                found = true;
                weight += weightAttribute.getAttributeValue(e, 0);
            } else if (e.getSource() == nodeI2.node && e.getTarget() == nodeI.node) {
                found = true;
                weight += weightAttribute.getAttributeValue(e, 0);
            }
        }
        if (found)
            skew = weight;
        nodeCombination2skewValue.put(key, skew);
        return skew;
    }

    /**
     * Applies magnetic forces between a node (with a cluster index) and a cluster
     * graph node that represents the cluster the node is belonging to. This force
     * is attracting, this way all nodes that belong to one cluster will move to the
     * position of the cluster representation node.
     *
     * @param force           This force is modified
     * @param nodePosition
     * @param clusterPosition
     */
    private void applyMagneticClusterForce(ThreadSafeOptions options, Vector2d force, Vector2d nodePosition,
                                           Vector2d clusterPosition) {
        options.getDval(myOp.DvalIndexSliderClusterForce, 20);
        double xdiff = nodePosition.x - clusterPosition.x;
        double ydiff = nodePosition.y - clusterPosition.y;
        // System.out.println(clusterPosition);
        double len = Math.sqrt(xdiff * xdiff + ydiff * ydiff);
        force.x += -xdiff / len * cachedClusterForce;
        force.y += -ydiff / len * cachedClusterForce;
    }

    /**
     * Layout Algorithm
     */
    public void execute() {

        // Graph gi =
        // GravistoService.getInstance().getMainFrame().getActiveSession().getGraph();
        // Selection s =
        // GravistoService.getInstance().getMainFrame().getActiveEditorSession().getSelectionModel().getActiveSelection();

        MyNonInteractiveSpringEmb mse = new MyNonInteractiveSpringEmb(non_interact_graph, non_interact_selection,
                nonInteractiveTSO);
        BackgroundTaskHelper bth = new BackgroundTaskHelper(mse, mse, "Force Directed Layout", "Force Directed Layout",
                true, false);
        bth.startWork(this);

        // JOptionPane.showMessageDialog(
        // GraffitiSingleton.getInstance().getMainFrame(),
        // "Use the Pattern Graffiti Plugins to start this plugin interactively.",
        // "This plugin currently can not be started from the Plugin-Menu",
        // JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Init cache entry for a node (get pattern nodes-call cache)
     *
     * @param options DOCUMENT ME!
     */
    public void readPatternConnections(ThreadSafeOptions options) {
        for (int i = 0; i < options.nodeArray.size(); i++) {
            NodeCacheEntry pi = (NodeCacheEntry) options.nodeArray.get(i);

            pi.patternNodes = getPatternNodes(options, pi);

            pi.connectedNodes = getConnectedNodes(options, pi);
        }
    }

    public Parameter[] getParameters() {
        if (nonInteractiveTSO == null) {
            nonInteractiveTSO = MyNonInteractiveSpringEmb.getNewThreadSafeOptionsWithDefaultSettings();
            nonInteractiveTSO.temp_alpha = 0.98;
        }
        double dv3_200_zero_len = nonInteractiveTSO.getDval(myOp.DvalIndexSliderZeroLength, 100);
        double d1_1000 = nonInteractiveTSO.getDval(myOp.DvalIndexSliderHorForce, 90000);
        double d2_1000 = nonInteractiveTSO.getDval(myOp.DvalIndexSliderVertForce, 90000);
        return new Parameter[]{
                new DoubleParameter(dv3_200_zero_len, "Target Edge Length", "The target length of the edges"),
                new DoubleParameter(d1_1000, "Horizontal Repulsion", "Strength of horizontal repulsion"),
                new DoubleParameter(d2_1000, "Vertical Repulsion", "Strength of vertical repulsion")};
    }

    public void setParameters(Parameter[] params) {
        initLength = ((DoubleParameter) params[0]).getDouble().doubleValue();
        nonInteractiveTSO.setDval(myOp.DvalIndexSliderZeroLength,
                ((DoubleParameter) params[0]).getDouble().doubleValue());
        nonInteractiveTSO.setDval(myOp.DvalIndexSliderHorForce,
                ((DoubleParameter) params[1]).getDouble().doubleValue());
        nonInteractiveTSO.setDval(myOp.DvalIndexSliderVertForce,
                ((DoubleParameter) params[2]).getDouble().doubleValue());
    }

    public Parameter[] getParameters(double edgeLength, double nodeForce) {
        if (nonInteractiveTSO == null) {
            nonInteractiveTSO = MyNonInteractiveSpringEmb.getNewThreadSafeOptionsWithDefaultSettings();
            nonInteractiveTSO.temp_alpha = 0.98;
        }
        double dv3_200_zero_len = nonInteractiveTSO.getDval(myOp.DvalIndexSliderZeroLength, edgeLength);
        double d1_1000 = nonInteractiveTSO.getDval(myOp.DvalIndexSliderHorForce, nodeForce);
        double d2_1000 = nonInteractiveTSO.getDval(myOp.DvalIndexSliderVertForce, nodeForce);
        return new Parameter[]{
                new DoubleParameter(dv3_200_zero_len, "Target Edge Length", "The target length of the edges"),
                new DoubleParameter(d1_1000, "Horizontal Repulsion", "Strength of horizontal repulsion"),
                new DoubleParameter(d2_1000, "Vertical Repulsion", "Strength of vertical repulsion")};
    }

    // private Timer errorDebugTimer = new Timer(500, new DebugSelectionCheck());

    /*
     * (non-Javadoc)
     *
     * @see
     * org.graffiti.plugin.algorithm.ThreadSafeAlgorithm#setControlInterface(javax.
     * swing.JComponent)
     */
    public boolean setControlInterface(final ThreadSafeOptions options, JComponent jc) {
        int border = 5;
        if (!SwingUtilities.isEventDispatchThread())
            System.err.println("Setting SpringEmbedder interface not in event dispatch thread.");
        jc.setBorder(BorderFactory.createEmptyBorder(border, border, border, border));
        SingleFiledLayout sfl = new SingleFiledLayout(SingleFiledLayout.COLUMN, SingleFiledLayout.FULL, 1);
        jc.setLayout(sfl);
        this.getClass().getClassLoader();
        // System.out.println("Image directory: " + path);
        // progressImg = new ImageIcon(cl.getResource(path + "/" + "waitSlow.gif"));
        // progressImgOK = new ImageIcon(cl.getResource(path + "/" + "waitOK.gif"));

        final JButton attributeSelection = new JButton("Attribute?");
        attributeSelection.setToolTipText("Select the edge weight attribute");
        attributeSelection.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                // options.redraw = true;
                options.setParam(myOp.OvalIndexEdgeWeightAttributeObject, null);
                attributeSelection.setText("Attribute?");
                Graph g = null;
                try {
                    g = GravistoService.getInstance().getMainFrame().getActiveSession().getGraph();
                } catch (NullPointerException npe) {
                    // empty
                }
                if (g == null) {
                    MainFrame.showMessageDialog("No active graph!", "Error");
                    return;
                }
                if (g.getNumberOfEdges() <= 0) {
                    MainFrame.showMessageDialog("Active graph contains no edges!", "Error");
                    return;
                }
                ArrayList<AttributePathNameSearchType> possibleAttributes = new ArrayList<AttributePathNameSearchType>();
                SearchAndSelecAlgorithm.enumerateAttributes(possibleAttributes, g.getEdges(),
                        SearchType.getSetOfNumericSearchTypes());
                if (possibleAttributes.size() <= 0) {
                    MainFrame.showMessageDialog("Edges contain no numeric attributes!", "Information");
                    return;
                }
                Object[] res = MyInputHelper.getInput(
                        "<html>" + "Please select the desired edge weight attribute!<br><br>"
                                + "<small><font color=\"gray\">"
                                + "Hint: The target edge length is calculated by multiplying the<br>"
                                + "attribute value with the target length setting. If more than one<br>"
                                + "edge connects two nodes, the sum of the attribute values are<br>" + "considered.<br>"
                                + "<br>" + "Use the <i>Elements/Add Calculated Attribute</i> command to transform<br>"
                                + "a numeric attribute. A linear transformation is possible with the<br>"
                                + "command <i>Elements/Set Visual Properties dep. on Attribute Value</i>.<br><br>",
                        "Edge Weight", "Weight", possibleAttributes);
                if (res != null) {
                    AttributePathNameSearchType sel = (AttributePathNameSearchType) res[0];
                    attributeSelection.setText(sel.getAttributePath() + ": " + sel.getAttributeName());
                    options.setParam(myOp.OvalIndexEdgeWeightAttributeObject, sel);
                }
            }
        });

        JCheckBox edgeWeight = new JCheckBox("Multiply with edge attribute value: ",
                options.getBval(myOp.BvalIndexEnableEdgeWeightProcessing, false));
        edgeWeight.setToolTipText(
                "<html>If selected, a numeric edge attribute is considered for individually modifying the<br>"
                        + "target edge length");
        edgeWeight.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                options.setBval(myOp.BvalIndexEnableEdgeWeightProcessing, ((JCheckBox) arg0.getSource()).isSelected());
            }
        });

        JComponent helpButton = FolderPanel.getHelpButton(JLabelJavaHelpLink.getHelpActionListener("layout_force"),
                jc.getBackground());

        jc.add(TableLayout
                .getSplit(
                        new JLabel(), TableLayout.get3Split(new JLabel(), new JLabel(), helpButton,
                                TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED),
                        TableLayout.PREFERRED, TableLayout.FILL));

        // ///////////////////////////
        JLabel labelSliderLength = new JLabel("Target Length of Edges:");

        JSlider sliderLength = new JSlider();
        if (SystemInfo.isMac())
            sliderLength.setPaintTrack(false);
        sliderLength.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
        sliderLength.setMinimum(0);
        sliderLength.setMaximum(800);
        sliderLength.setToolTipText("<html>This value determines the &quot;natural&quot; (zero energy)<br>"
                + "length of the graph edges (&quot;springs&quot;)");
        sliderLength.setMinorTickSpacing(50);
        sliderLength.setMajorTickSpacing(100);
        sliderLength.setPaintLabels(true);
        sliderLength.setPaintTicks(true);
        sliderLength.setLabelTable(sliderLength.createStandardLabels(100));
        sliderLength.setValue((int) options.getDval(myOp.DvalIndexSliderZeroLength, 200));

        sliderLength.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                options.setDval(myOp.DvalIndexSliderZeroLength, ((JSlider) e.getSource()).getValue());
            }
        });

        sliderLength.setAlignmentX(10);
        sliderLength.setAlignmentY(70);

        JSlider sliderEnergyHor = new JSlider();
        if (SystemInfo.isMac())
            sliderEnergyHor.setPaintTrack(false);
        sliderEnergyHor.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
        sliderEnergyHor.setMinimum(0);
        sliderEnergyHor.setMaximum(1000000);
        sliderEnergyHor.setMinorTickSpacing(50000);
        sliderEnergyHor.setMajorTickSpacing(200000);
        sliderEnergyHor.setPaintLabels(true);
        sliderEnergyHor.setPaintTicks(true);
        Dictionary<Integer, Component> d = new Hashtable<Integer, Component>();
        d.put(Integer.valueOf(0), new JLabel("low repulsion"));
        d.put(Integer.valueOf(1000000), new JLabel("high repulsion"));
        sliderEnergyHor.setLabelTable(d);
        sliderEnergyHor.setToolTipText(
                "<html>This value determines the horizontal<br>" + "repulsive energy between all nodes");
        sliderEnergyHor.setValue((int) options.getDval(myOp.DvalIndexSliderHorForce, 1000));

        sliderEnergyHor.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                options.setDval(myOp.DvalIndexSliderHorForce, ((JSlider) e.getSource()).getValue());
            }
        });

        JSlider sliderEnergyVert = new JSlider();
        if (SystemInfo.isMac())
            sliderEnergyVert.setPaintTrack(false);
        sliderEnergyVert.setMinimum(0);
        sliderEnergyVert.setMaximum(1000000);
        sliderEnergyVert.setMinorTickSpacing(50000);
        sliderEnergyVert.setMajorTickSpacing(200000);
        sliderEnergyVert.setPaintLabels(true);
        sliderEnergyVert.setPaintTicks(true);
        Dictionary<Integer, Component> d2 = new Hashtable<Integer, Component>();
        d2.put(Integer.valueOf(0), new JLabel("low repulsion"));
        d2.put(Integer.valueOf(1000000), new JLabel("high repulsion"));
        sliderEnergyVert.setLabelTable(d2);
        sliderEnergyVert.setValue((int) options.getDval(myOp.DvalIndexSliderVertForce, 1000));
        sliderEnergyVert
                .setToolTipText("<html>This value determines the vertical<br>repulsive energy between all nodes");
        sliderEnergyVert.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                options.setDval(myOp.DvalIndexSliderVertForce, ((JSlider) e.getSource()).getValue());
            }
        });

        // if (ReleaseInfo.getIsAllowedFeature(FeatureSet.PATTERN_LAYOUT)) {
        JSlider sliderMultiplyRepulsive = new JSlider();
        if (SystemInfo.isMac())
            sliderMultiplyRepulsive.setPaintTrack(false);
        // sliderMultiplyRepulsive.setBackground(Color.YELLOW);
        sliderMultiplyRepulsive.setMinimum(-1);
        sliderMultiplyRepulsive.setMaximum(10);
        sliderMultiplyRepulsive.setMajorTickSpacing(1);
        sliderMultiplyRepulsive.setMinorTickSpacing(1);
        Dictionary<Integer, Component> dMF = new Hashtable<Integer, Component>();
        dMF.put(Integer.valueOf(-1), new JLabel("-1x"));
        dMF.put(Integer.valueOf(0), new JLabel("0x"));
        dMF.put(Integer.valueOf(1), new JLabel("1x"));
        dMF.put(Integer.valueOf(3), new JLabel("3x"));
        dMF.put(Integer.valueOf(5), new JLabel("5x"));
        dMF.put(Integer.valueOf(8), new JLabel("8x"));
        dMF.put(Integer.valueOf(10), new JLabel("10x"));
        sliderMultiplyRepulsive.setLabelTable(dMF);
        sliderMultiplyRepulsive.setPaintLabels(true);
        sliderMultiplyRepulsive.setPaintTicks(true);
        sliderMultiplyRepulsive.setValue((int) options.getDval(myOp.DmultiplyRepulsiveForces2Patterns, 1d));

        sliderMultiplyRepulsive.setToolTipText(
                "<html>This value determines a multipicator for the repulsive energy between pattern nodes and the remaining nodes");
        sliderMultiplyRepulsive.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                options.setDval(myOp.DmultiplyRepulsiveForces2Patterns, ((JSlider) e.getSource()).getValue());
            }
        });

        // }

        JSlider sliderMultiplyRepulsiveSubgraphs = new JSlider();
        if (SystemInfo.isMac())
            sliderMultiplyRepulsiveSubgraphs.setPaintTrack(false);
        sliderMultiplyRepulsiveSubgraphs.setMinimum(-1);
        sliderMultiplyRepulsiveSubgraphs.setMaximum(2);
        sliderMultiplyRepulsiveSubgraphs.setMajorTickSpacing(1);
        sliderMultiplyRepulsiveSubgraphs.setMinorTickSpacing(1);
        Dictionary<Integer, Component> dMF2 = new Hashtable<Integer, Component>();
        dMF2.put(Integer.valueOf(-1), new JLabel("-1x"));
        dMF2.put(Integer.valueOf(0), new JLabel("0x"));
        dMF2.put(Integer.valueOf(1), new JLabel("1x"));
        dMF2.put(Integer.valueOf(2), new JLabel("2x"));
        sliderMultiplyRepulsiveSubgraphs.setLabelTable(dMF2);
        sliderMultiplyRepulsiveSubgraphs.setPaintLabels(true);
        sliderMultiplyRepulsiveSubgraphs.setPaintTicks(true);
        sliderMultiplyRepulsiveSubgraphs.setValue((int) options.getDval(myOp.DmultiplyRepulsiveForces2Subgraphs, 1d));

        // if (ReleaseInfo.getIsAllowedFeature(FeatureSet.TAB_PATTERNSEARCH))
        // jc.add(TableLayout.getDoubleRow(new JLabel("Multiply repulsive between
        // subgraphs:"), sliderMultiplyRepulsiveSubgraphs, Color.WHITE));

        sliderMultiplyRepulsiveSubgraphs.setToolTipText(
                "<html>This value determines a multipicator for the repulsive energy between nodes belonging to different connected subgraphs");
        sliderMultiplyRepulsiveSubgraphs.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                options.setDval(myOp.DmultiplyRepulsiveForces2Subgraphs, ((JSlider) e.getSource()).getValue());
            }
        });

        JSlider sliderMultiplyRepulsiveClusters = new JSlider();
        if (SystemInfo.isMac())
            sliderMultiplyRepulsiveClusters.setPaintTrack(false);
        sliderMultiplyRepulsiveClusters.setMinimum(-1);
        sliderMultiplyRepulsiveClusters.setMaximum(2);
        sliderMultiplyRepulsiveClusters.setMajorTickSpacing(1);
        sliderMultiplyRepulsiveClusters.setMinorTickSpacing(1);
        Dictionary<Integer, Component> dMF3 = new Hashtable<Integer, Component>();
        dMF3.put(Integer.valueOf(-1), new JLabel("-1x"));
        dMF3.put(Integer.valueOf(0), new JLabel("0x"));
        dMF3.put(Integer.valueOf(1), new JLabel("1x"));
        dMF3.put(Integer.valueOf(2), new JLabel("2x"));
        sliderMultiplyRepulsiveClusters.setLabelTable(dMF3);
        sliderMultiplyRepulsiveClusters.setPaintLabels(true);
        sliderMultiplyRepulsiveClusters.setPaintTicks(true);
        sliderMultiplyRepulsiveClusters.setValue((int) options.getDval(myOp.DmultiplyRepulsiveForces2Clusters, 1d));

        sliderMultiplyRepulsiveClusters.setToolTipText(
                "<html>This value determines a multipicator for the repulsive energy between nodes marked with different cluster IDs");
        sliderMultiplyRepulsiveClusters.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                options.setDval(myOp.DmultiplyRepulsiveForces2Clusters, ((JSlider) e.getSource()).getValue());
            }
        });

        JLabel stiffnessDesc = new JLabel("Stiffness:");

        JSlider sliderStiffnes = new JSlider();
        if (SystemInfo.isMac())
            sliderStiffnes.setPaintTrack(false);
        sliderStiffnes.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
        sliderStiffnes
                .setToolTipText("Modifes the forces determined by the connection to other nodes (edge target length).");

        sliderStiffnes.setMinimum(0);
        sliderStiffnes.setMaximum(75);
        sliderStiffnes.setMinorTickSpacing(10);
        sliderStiffnes.setMajorTickSpacing(10);
        sliderStiffnes.setPaintLabels(true);
        sliderStiffnes.setPaintTicks(true);
        sliderStiffnes.setValue((int) options.getDval(myOp.DvalIndexSliderStiffness, 10d));
        Dictionary<Integer, Component> d3 = new Hashtable<Integer, Component>();
        d3.put(Integer.valueOf(0), new JLabel("0"));
        d3.put(Integer.valueOf(10), new JLabel("1 (norm)"));
        d3.put(Integer.valueOf(50), new JLabel("5 (strong)"));
        sliderStiffnes.setLabelTable(d3);
        // sliderStiffnes.setLabelTable(sliderStiffnes.createStandardLabels(10));
        sliderStiffnes.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                options.setDval(myOp.DvalIndexSliderStiffness, ((JSlider) e.getSource()).getValue());
            }
        });

        // final JCheckBox useIndepClusterLayout = new JCheckBox();
        final JCheckBox useClusterInfo = new JCheckBox();
        useClusterInfo
                .setToolTipText("<html>If selected, a clustered graph will be processed in a way so that a additional "
                        + "&quot;Cluster-Force&quot;<br>"
                        + "towards the direction of cluster-reference-nodes is applied");
        useClusterInfo.setSelected(options.getBval(myOp.BvalIndexDoClusterLayoutIndex, false));
        useClusterInfo.setText("Apply attractive cluster-force:");
        useClusterInfo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean newVal = ((JCheckBox) e.getSource()).isSelected();
                options.setBval(myOp.BvalIndexDoClusterLayoutIndex, newVal);
                options.setBval(myOp.BvalIndexDoIndividualClusterLayoutIndex, !newVal);
            }
        });

        JSlider sliderClusterForce = new JSlider();
        if (SystemInfo.isMac())
            sliderClusterForce.setPaintTrack(false);
        sliderClusterForce.setMinimum(0);
        sliderClusterForce.setMaximum(1000);
        sliderClusterForce.setMinorTickSpacing(50);
        sliderClusterForce.setMajorTickSpacing(100);
        sliderClusterForce.setPaintLabels(true);
        sliderClusterForce.setPaintTicks(true);
        Dictionary<Integer, Component> d4 = new Hashtable<Integer, Component>();
        d4.put(Integer.valueOf(0), new JLabel("zero force"));
        d4.put(Integer.valueOf(500), new JLabel("average force"));
        d4.put(Integer.valueOf(1000), new JLabel("strong force"));
        sliderClusterForce.setLabelTable(d4);
        sliderClusterForce.setValue((int) options.getDval(myOp.DvalIndexSliderClusterForce, myOp.InitClusterForce));
        sliderClusterForce.setToolTipText("<html>This value determines the constant additional node-force"
                + "<br>towards " + "the position of the cluster-reference-nodes in the cluster-graph.");
        sliderClusterForce.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                options.setDval(myOp.DvalIndexSliderClusterForce, ((JSlider) e.getSource()).getValue());
                if (!useClusterInfo.isSelected()) {
                    useClusterInfo.doClick();
                }
            }
        });

        final JSlider tempSlider = new JSlider();
        if (SystemInfo.isMac())
            tempSlider.setPaintTrack(false);
        tempSlider.setMinimum(0);
        tempSlider.setMaximum(300);
        tempSlider.setMinorTickSpacing(25);
        tempSlider.setMajorTickSpacing(50);
        tempSlider.setPaintLabels(true);
        tempSlider.setPaintTicks(true);
        tempSlider.setValue((int) options.temperature_max_move);
        tempSlider.setLabelTable(tempSlider.createStandardLabels(50));
        tempSlider.setToolTipText(
                "<html>" + "<b>Move this slider to decrease or increase the run-time of the algorithm</b><br>"
                        + "This value determines the maximum node movement during one layout-loop run." + "");
        tempSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                options.temperature_max_move = ((JSlider) e.getSource()).getValue();
            }
        });

        JCheckBox borderForce = new JCheckBox("Border Force", options.borderForce);
        borderForce.setToolTipText("<html>If selected, a force will be added, which lets the nodes<br>"
                + "move slowly to the top left. The nodes will avoid movement towards negative coordinates.");
        borderForce.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                options.borderForce = ((JCheckBox) e.getSource()).isSelected();
            }
        });

        // JCheckBox useSelection = new JCheckBox();
        // useSelection.setText("Work on Selection");
        // useSelection.setToolTipText("If selected, the not selected nodes will have a
        // fixed position");
        // useSelection.addActionListener(new ActionListener() {
        // public void actionPerformed(ActionEvent e) {
        // boolean newVal = ((JCheckBox) e.getSource()).isSelected();
        // options.setBval(myOp.BvalIndexMoveSelectionIndex, newVal);
        // }
        // });
        // jc.add(useSelection);

        JCheckBox randomInit = new JCheckBox("Init: Random Node Positions", options.doRandomInit);
        randomInit.setToolTipText("<html>If selected, the graph will have a random layout applied<br>"
                + "before executing the spring embedder layouter");
        randomInit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                options.doRandomInit = ((JCheckBox) e.getSource()).isSelected();
            }
        });

        JCheckBox removeOverlapping = new JCheckBox("Finish: Remove Node Overlaps", options.doFinishRemoveOverlapp);
        removeOverlapping.setToolTipText("If selected, the final layout will be modified to remove any node overlaps");
        removeOverlapping.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                options.doFinishRemoveOverlapp = ((JCheckBox) e.getSource()).isSelected();
            }
        });

        JCheckBox finishToTop = new JCheckBox("Finish: Move Network to Top-Left", options.doFinishMoveToTop);
        finishToTop
                .setToolTipText("If selected, all network elements will be moved to the top-left corner of the view");
        finishToTop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                options.doFinishMoveToTop = ((JCheckBox) e.getSource()).isSelected();
            }
        });

        // useIndepClusterLayout.setSelected(options.getBval(
        // myOp.BvalIndexDoIndividualClusterLayoutIndex, false));
        // useIndepClusterLayout.setText("Do individual Cluster Layout");
        // useIndepClusterLayout.addActionListener(new ActionListener() {
        // public void actionPerformed(ActionEvent e) {
        // boolean newVal = ((JCheckBox) e.getSource()).isSelected();
        // options.setBval(myOp.BvalIndexDoIndividualClusterLayoutIndex,
        // newVal);
        // options.setBval(myOp.BvalIndexDoClusterLayoutIndex, !newVal);
        // if (newVal)
        // useClusterInfo.setSelected(!useIndepClusterLayout.isSelected());
        // }
        // });
        // jc.add(useIndepClusterLayout, "1,21");

        // /////////////////////////////
        // double threadBorder = 2;
        // double[][] threadSize = {
        // { threadBorder, TableLayout.PREFERRED, TableLayoutConstants.FILL,
        // threadBorder },
        // // Columns
        // { threadBorder, TableLayout.PREFERRED, threadBorder } }; // Rows
        // JPanel componentForThreadSetting = new JPanel();
        // componentForThreadSetting.setLayout(new TableLayout(threadSize));

        // JLabel threadDesc = new JLabel("Thread-Count:");

        // JSpinner maxThreads = new JSpinner();
        // maxThreads.addChangeListener(new ChangeListener() {
        //
        // public void stateChanged(ChangeEvent e) {
        // try {
        // options.maxThreads = ((Integer) ((JSpinner) e.getSource())
        // .getValue()).intValue();
        // if (options.maxThreads < 0)
        // options.maxThreads = 0;
        // if (options.maxThreads > maxThreadCount)
        // options.maxThreads = maxThreadCount;
        // } finally {
        // ((JSpinner) e.getSource()).setValue(Integer.valueOf(
        // options.maxThreads));
        // }
        // }
        // });
        // componentForThreadSetting.add(threadDesc, "1,1");
        // componentForThreadSetting.add(maxThreads, "2,1");
        // componentForThreadSetting.validate();
        // jc.add(componentForThreadSetting, "1,21");

        sliderLength.setValue(100);
        // sliderStiffnes.setValue(10);
        sliderEnergyHor.setValue(90000);
        sliderEnergyVert.setValue(90000);

        addGUIelements(options, jc, attributeSelection, edgeWeight, labelSliderLength, sliderLength, sliderEnergyHor,
                sliderEnergyVert, sliderMultiplyRepulsiveClusters, sliderMultiplyRepulsiveSubgraphs, stiffnessDesc,
                sliderStiffnes, useClusterInfo, sliderClusterForce, tempSlider, finishToTop, borderForce, randomInit,
                removeOverlapping);

        jc.validate();

        // /////////////////////////////////////////
        // TIMER

        Timer runCheckTimer = new Timer(200, new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                switch (options.runStatus) {
                    case 3:
                        // startStopButton.setIcon(null); // finished
                        if (tempSlider.getValue() == 0) {
                            options.temperature_max_move = 300;
                        }
                        break;
                }
                // set max movement / temperature slider value to current value
                tempSlider.setValue((int) options.temperature_max_move);
            }
        });
        runCheckTimer.start();

        return true;
    }

    /**
     * Initialized the node cache structures.
     *
     * @param options The options to use
     */
    @SuppressWarnings("unchecked")
    public void resetDataCache(ThreadSafeOptions options) {
        options.nodeArray = new ArrayList<NodeCacheEntry>();
        options.nodeSearch = new HashMap<Node, NodeCacheEntry>();
        MyTools.initNodeCache(options.nodeArray, options.nodeSearch, options.getGraphInstance(), options.getSelection(),
                GravistoService.getInstance().getPatternGraphs());
        readPatternConnections(options);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.graffiti.plugin.algorithm.ThreadSafeAlgorithm#executeThreadSafe(org.
     * graffiti.plugin.algorithm.ThreadSafeOptions)
     */
    public void executeThreadSafe(final ThreadSafeOptions options) {

        // if (!errorDebugTimer.isRunning()) {
        // DebugSelectionCheck.setCheckThis(options);
        // errorDebugTimer.setRepeats(true);
        // errorDebugTimer.start();
        // }

        MainFrame.showMessage("Force Directed Layout: Init", MessageType.PERMANENT_INFO);

        if (options.doRandomInit) {
            MainFrame.showMessage("Force Directed Layout: Init Random Positions", MessageType.PERMANENT_INFO);
            RandomLayouterAlgorithm rla = new RandomLayouterAlgorithm();
            rla.attach(options.getGraphInstance(), options.getSelection());
            rla.execute();
        }

        if (options.doCopyPatternLayout) {
            MainFrame.showMessage("Force Directed Layout: Init apply Search-Graph Layout", MessageType.PERMANENT_INFO);
            GravistoService.getInstance().runPlugin(new CopyPatternLayoutAlgorithm().getName(),
                    options.getGraphInstance(), null);
        }

        // Remove bends
        if (options.doRemoveAllBends) {
            MainFrame.showMessage("Force Directed Layout: Init remove edge bends", MessageType.PERMANENT_INFO);
            GraphHelper.removeAllBends(options.getGraphInstance(), true);
        }

        MainFrame.showMessage("Force Directed Layout: Init", MessageType.PERMANENT_INFO);

        resetDataCache(options);

        doClusterInitialization(options);

        int runValue = 0;

        long loopTime = 0;

        double moveRun;

        nodeCombination2skewValue.clear();

        final HashMap<CoordinateAttribute, Vector2d> oldPositions = new HashMap<CoordinateAttribute, Vector2d>();
        final HashMap<CoordinateAttribute, Vector2d> newPositions = new HashMap<CoordinateAttribute, Vector2d>();

        GraphHelper.enumerateNodePositions(options.getGraphInstance(), oldPositions);

        int n = options.getGraphInstance().getNumberOfNodes();

        Graph clusterGraph = (Graph) AttributeHelper.getAttributeValue(options.getGraphInstance(), "cluster",
                "clustergraph", null, new AdjListGraph());
        boolean clusterGraphAvailable = clusterGraph != null;
        boolean idleCheckResultOK = true;
        long starttime = System.currentTimeMillis();
        int threadCount = SystemAnalysis.getNumberOfCPUs() - 3;
        if (threadCount < 1)
            threadCount = 1;
        int threadMinCount = threadCount;
        int threadMaxCount = SystemAnalysis.getNumberOfCPUs();

        HashMap<Integer, ArrayList<Long>> threadCount2speed = new HashMap<Integer, ArrayList<Long>>();
        int maxCalRun = 3; // sample times for each threadCount
        double speed1 = 0;
        boolean calibrate = true;

        if (true) {
            calibrate = false;
            threadCount = SystemAnalysis.getNumberOfCPUs();
        }

        int executorTC = threadCount;
        ExecutorService run = Executors.newFixedThreadPool(threadCount);

        do {
            runValue++;

            loopTime = System.currentTimeMillis();

            if (threadCount != executorTC) {
                if (run.shutdownNow().size() > 0) {
                    System.err.println("Internal Error: SpringEmbedder: stopped threads!");
                }
                try {
                    run.awaitTermination(1, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    ErrorMsg.addErrorMessage(e);
                }
                run = Executors.newFixedThreadPool(threadCount);
                executorTC = threadCount;
            }

            moveRun = doSpringEmbedder(options, runValue, n, threadCount, run);

            long loopTime2 = System.currentTimeMillis() - loopTime;

            if (calibrate) {
                if (threadCount > threadMaxCount)
                    threadCount = threadMinCount;
                if (!threadCount2speed.containsKey(threadCount))
                    threadCount2speed.put(threadCount, new ArrayList<Long>());
                threadCount2speed.get(threadCount).add(loopTime2);
                if (threadCount == threadMaxCount && threadCount2speed.get(threadCount).size() == maxCalRun) {
                    calibrate = false;
                    int bestThreadCount = 1;
                    double bestThreadTiming = Double.MAX_VALUE;
                    for (int test = threadMinCount; test <= threadMaxCount; test++) {
                        long sumTime = 0;
                        for (long l : threadCount2speed.get(test))
                            sumTime += l;
                        long speed = sumTime / threadCount2speed.get(test).size();
                        if (test == 1)
                            speed1 = speed;
                        System.out.println("Average loop time for " + test + " threads is " + speed + " ms");
                        if (speed < bestThreadTiming) {
                            bestThreadTiming = speed;
                            bestThreadCount = test;
                        }
                    }
                    System.out.println("Best thread count: " + bestThreadCount + " (average loop speed "
                            + (long) bestThreadTiming + " ms)");
                    System.out.println("Speed-up: " + AttributeHelper.formatNumber(speed1 / bestThreadTiming, "#.#"));
                    threadCount = bestThreadCount;
                } else {
                    threadCount++;
                }
            }

            options.temperature_max_move *= options.temp_alpha;
            if (options.redraw) {
                propagateCachedGraphPositions(options);
            }

            if (!options.autoRedraw) {
                options.redraw = false;
            }

            cachedClusterForce = options.getDval(myOp.DvalIndexSliderClusterForce, myOp.InitClusterForce);

            loopTime = System.currentTimeMillis() - loopTime;

            if (moveRun <= 0.1) {
                try {
                    options.runStatus = 2; // idle
//                    MainFrame.showMessage("Spring Embedder - IDLE", MessageType.INFO, 10000);
                    Thread.sleep(1); // TODO: decreased this to from 50 to 1ms to make it run faster
                } catch (InterruptedException e) {
//                     ignore (no problem)
                }
            } else {
                options.runStatus = 1; // running
                String clusterMessage = "";
                boolean useClusterLayout = options.getBval(myOp.BvalIndexDoClusterLayoutIndex, false);
                if (clusterGraphAvailable && useClusterLayout)
                    clusterMessage = ", considering cluster-information";
                if (!clusterGraphAvailable && useClusterLayout)
                    clusterMessage = ", no cluster-information available";

                MainFrame.showMessage("Force Directed Layout: RUNNING (max single node movements:"
                        + Math.round(options.temperature_max_move) + ", loop time:" + loopTime + " ms, using "
                        + (calibrate ? threadCount - 1 : threadCount) + " threads" + (calibrate ? " (calibrating)" : "")
                        + ", max. sum of node movement: " + moveRun + clusterMessage + ")", MessageType.PERMANENT_INFO);
            }
            if (options.getBval(myOp.BvalIndexStopWhenIdle, false)) {
                if (options.runStatus == 2)
                    idleCheckResultOK = false;
            }
        } while (!options.isAbortWanted() && options.temperature_max_move > 0.1 && idleCheckResultOK);

        if (run.shutdownNow().size() > 0) {
            System.err.println("Internal Error: SpringEmbedder: stopped threads!");
        }
        try {
            run.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            ErrorMsg.addErrorMessage(e);
        }

        long endtime = System.currentTimeMillis();
        long processingTime = endtime - starttime;
        final String timeDesc;
        if (processingTime > 10000)
            timeDesc = ((int) (processingTime / 1000)) + " seconds";
        else
            timeDesc = processingTime + " ms";

        MainFrame.showMessage("Main layout loop finished after " + timeDesc + ", apply result (please wait)",
                MessageType.PERMANENT_INFO);

        EditorSession es = GravistoService.getInstance().getMainFrame().getActiveEditorSession();
        if (es.getActiveView() instanceof GraffitiView)
            ((GraffitiView) es.getActiveView()).setDrawMode(DrawMode.NORMAL);

        if (!SwingUtilities.isEventDispatchThread()) {
            try {
                SwingUtilities.invokeAndWait(() -> {
                    propagateCachedGraphPositions(options);
                    GraphHelper.enumerateNodePositions(options.getGraphInstance(), newPositions);
                    GraphHelper.postUndoableChanges(options.getGraphInstance(), oldPositions, newPositions, getName());

                    if (options.doFinishRemoveOverlapp) {
                        // int enlDir = getEnlargeDirectionFromNodesSize(options.nodeArray);
                        MainFrame.showMessage("Remove node overlapps", MessageType.PERMANENT_INFO);
                        GravistoService.getInstance().runAlgorithm(new NoOverlappLayoutAlgorithmAS(5, 5),
                                options.getGraphInstance(), options.getSelection(), getActionEvent());
                    }
                    if (options.doFinishMoveToTop)
                        MainFrame.showMessage("Move Network to Top-Left", MessageType.PERMANENT_INFO);
                    GravistoService.getInstance().runAlgorithm(new CenterLayouterAlgorithm(), options.getGraphInstance(),
                            new Selection(""), getActionEvent());
                    MainFrame.showMessage("Spring Embedder - Finished (main layout loop took " + timeDesc + ")",
                            MessageType.INFO, 3000);
                });
            } catch (InterruptedException | InvocationTargetException e) {
                e.printStackTrace();
            }
        } else {
            propagateCachedGraphPositions(options);
            GraphHelper.enumerateNodePositions(options.getGraphInstance(), newPositions);
            GraphHelper.postUndoableChanges(options.getGraphInstance(), oldPositions, newPositions, getName());

            if (options.doFinishRemoveOverlapp) {
                // int enlDir = getEnlargeDirectionFromNodesSize(options.nodeArray);
                MainFrame.showMessage("Remove node overlapps", MessageType.PERMANENT_INFO);
                GravistoService.getInstance().runAlgorithm(new NoOverlappLayoutAlgorithmAS(5, 5),
                        options.getGraphInstance(), options.getSelection(), getActionEvent());
            }
            if (options.doFinishMoveToTop)
                MainFrame.showMessage("Move Network to Top-Left", MessageType.PERMANENT_INFO);
            GravistoService.getInstance().runAlgorithm(new CenterLayouterAlgorithm(), options.getGraphInstance(),
                    new Selection(""), getActionEvent());
            MainFrame.showMessage("Spring Embedder - Finished (main layout loop took " + timeDesc + ")",
                    MessageType.INFO, 3000);
        }

        options.setAbortWanted(false);

        options.runStatus = 3; // finished
    }

    /**
     * @param options
     */
    private void doClusterInitialization(final ThreadSafeOptions options) {
        clusterLocations.clear();
        // Collection clusters =
        // GraphHelper.getClusters(options.getGraphInstance().getNodes());
        Graph clusterGraph = (Graph) AttributeHelper.getAttributeValue(options.getGraphInstance(), "cluster",
                "clustergraph", null, new AdjListGraph());
        boolean clusterGraphAvailable = clusterGraph != null;
        if (clusterGraphAvailable) {
            for (Iterator<?> it = clusterGraph.getNodesIterator(); it.hasNext(); ) {
                Node clusterNode = (Node) it.next();
                String clusterId = NodeTools.getClusterID(clusterNode, "");
                if (clusterId.equals("")) {
                    ErrorMsg.addErrorMessage("Cluster-Graph-Node with no Cluster ID found!");
                } else {
                    Point2D position = AttributeHelper.getPosition(clusterNode);
                    clusterLocations.put(clusterId, new Vector2d(position));
                }
            }
        }
    }

    private void propagateCachedGraphPositions(final ThreadSafeOptions options) {
        try {
            if (!SwingUtilities.isEventDispatchThread()) {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        propagatePositions(options);
                    }
                });
            } else {
                propagatePositions(options);
            }
        } catch (InterruptedException e) {
            ErrorMsg.addErrorMessage(e);
        } catch (InvocationTargetException e) {
            ErrorMsg.addErrorMessage(e);
        }
    }

    private void propagatePositions(final ThreadSafeOptions options) {
        options.getGraphInstance().getListenerManager().transactionStarted(this);
        for (int i = 0; i < options.nodeArray.size(); i++) {
            NodeCacheEntry curNode = (NodeCacheEntry) options.nodeArray.get(i);

            if (options.getBval(myOp.BvalIndexGridForceIndex, false))
                MyTools.setXY(curNode.node, curNode.position.x - (curNode.position.x % 10 - 5),
                        curNode.position.y - (curNode.position.y % 10 - 5));
            else
                MyTools.setXY(curNode.node, curNode.position.x, curNode.position.y);
        }
        options.getGraphInstance().getListenerManager().transactionFinished(this);
        // if (createGIF) {
        // if (agif == null) {
        // try {
        // agif = new AnimGifEncoder(new FileOutputStream("/tmp/lastanim.gif"));
        // } catch (FileNotFoundException e) {
        // ErrorMsg.addErrorMessage(e);
        // }
        // }
        // try {
        // pictureCount++;
        // if (true){
        // BufferedImage bi =
        // PNGAlgorithm.getActiveGraphViewImage(BufferedImage.TYPE_BYTE_INDEXED, "gif",
        // null);
        // if (bi!=null)
        // agif.add(bi, 40);
        // }
        // } catch (IOException e) {
        // ErrorMsg.addErrorMessage(e);
        // }
        // }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.graffiti.plugin.algorithm.Algorithm#attach(org.graffiti.graph.Graph)
     */
    public void attach(Graph g, Selection s) {
        non_interact_graph = g;
        non_interact_selection = s;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.graffiti.plugin.algorithm.Algorithm#reset()
     */
    public void reset() {
        nonInteractiveTSO.setDval(myOp.DvalIndexSliderZeroLength, initLength);
        nonInteractiveTSO.temperature_max_move = initLength;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.graffiti.plugin.algorithm.Algorithm#getCategory()
     */
    public String getCategory() {
        return "Layout";
    }

    @Override
    public Set<Category> getSetCategory() {
        return new HashSet<Category>(Arrays.asList(Category.LAYOUT, Category.GRAPH));
    }

    @Override
    public String getMenuCategory() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.graffiti.plugin.algorithm.Algorithm#isLayoutAlgorithm()
     */
    public boolean isLayoutAlgorithm() {
        return true;
    }

    public String getDescription() {
        //
        return null;
    }
}
