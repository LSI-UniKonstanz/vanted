/**
 * This class provides the Adaptagrams Edge Routing algorithm.
 * It uses the Adaptagrams libavoid library for the edge routing
 * (see also http://www.adaptagrams.org).
 * Copyright (c) 2014-2015 Monash University, Australia
 */
package org.vanted.plugins.layout.adaptagrams.edgerouting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.AttributeHelper;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.ReleaseInfo;
import org.Vector2d;
import org.adaptagrams.ConnRef;
import org.adaptagrams.ConnType;
import org.adaptagrams.Router;
import org.adaptagrams.RouterFlag;
import org.adaptagrams.RoutingOption;
import org.adaptagrams.RoutingParameter;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Node;
import org.graffiti.graphics.DockingAttribute;
import org.graffiti.graphics.EdgeGraphicAttribute;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.DoubleParameter;
import org.graffiti.plugin.parameter.ObjectListParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.view.View;
import org.vanted.plugins.layout.adaptagrams.AdaptagramsLibrary;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

/**
 * @author Tobias Czauderna
 */
public class EdgeRoutingAlgorithm extends AbstractAlgorithm {

	private static boolean isLayoutLibraryLoaded = false;
	// the native Adaptagrams libraries are expected in the Vanted home directory
	private static String libraryPath = ReleaseInfo.getAppSubdirFolderWithFinalSep("plugins", "Adaptagrams");

	private final static String[] phaseDescriptions = new String[] { // descriptions for the different routing phases
			"Building orthogonal visibility graph in x-dimension ...",
			"Building orthogonal visibility graph in y-dimension ...", "Searching initial paths for edges ...",
			"Detecting crossing edges ...", "Rerouting crossing edges ...",
			"Nudging apart edge segments in x-dimension ...", "Nudging apart edge segments in y-dimension ...",
			"Finishing ..." };
	private final static int phaseNumbers = 8; // Adaptagrams (libavoid) has eight edge routing phases
	private final static int polylineRoutingFirstPhase = 3; // first phase for polyline routing is phase three
	private final static int orthogonalRoutingFirstPhase = 1; // first phase for orthogonal routing is phase one

	// settings for the GUI and edge routing parameters
	private static final String polylineStr = "Polyline";
	private static final String orthogonalStr = "Orthogonal";
	private String routingStr = polylineStr;
	private double shapeBufferDistance = 5.0; // distance between nodes and edges
	private double radius = 0.0; // bend radius for orthogonal edge routing
	private double nudgingDistance = 0.0; // distance edge segments are nudged apart
	private boolean segmentPenalty = true; // if set to true the number of segments is minimised during orthogonal
											// routing
	private boolean nudgeOrthogonalSegments = false; // if set to true the first and the last segments of edges are
														// nudged apart
	private boolean ignoreNodesWithoutEdges = false; // if set to true nodes without edges are ignored as obstacle for
														// the edges

	/**
	 * Check preconditions: (1) a graph with edges, (2) native layout library can be
	 * loaded.
	 */
	@Override
	public void check() throws PreconditionException {

		PreconditionException preconditionException = new PreconditionException();
		if (this.graph == null || this.graph.getNumberOfEdges() == 0)
			preconditionException.add("No graph available or graph doesn't contain any edges!");
		if (!isLayoutLibraryLoaded) {
			String errorMessage = AdaptagramsLibrary.loadLibrary("adaptagrams", libraryPath);
			if (errorMessage.isEmpty())
				isLayoutLibraryLoaded = true;
			else
				preconditionException.add("Failed to load layout library!<br>" + errorMessage);
		}
		if (!preconditionException.isEmpty())
			throw preconditionException;

	}

	/**
	 * 
	 */
	@Override
	public Parameter[] getParameters() {

		return new Parameter[] {
				new ObjectListParameter(this.routingStr, "<html>Edge Routing",
						"<html>Polyline or orthogonal edge routing", getRoutings()),
				new DoubleParameter(this.shapeBufferDistance, "<html>Node Distance",
						"<html>Distance between nodes and edges"),
				new DoubleParameter(this.radius, "<html>Bend Radius",
						"<html>Radius for edge smoothing around edge bends"),
				new DoubleParameter(this.nudgingDistance, "<html>Nudging Distance",
						"<html>Distance between parallel edge segments<br>(Orthogonal edge routing only)"),
				new BooleanParameter(this.segmentPenalty, "<html>Minimize Number of Segments",
						"<html>Minimize number of edge segments<br>(Orthogonal edge routing only)"),
				new BooleanParameter(this.nudgeOrthogonalSegments, "<html>Nudge First and Last Segments",
						"<html>Nudge apart edge segments connected to nodes<br>(Orthogonal edge routing only)"),
				new BooleanParameter(this.ignoreNodesWithoutEdges, "<html>Ignore Nodes without Edges",
						"<html>Ignore nodes without edges as obstacles<br>during edge routing") };

	}

	/**
	 * Get the different routing types.
	 * 
	 * @return collection of <code>String</code> containing the different routing
	 *         types
	 */
	private static Collection<String> getRoutings() {

		ArrayList<String> routingStrs = new ArrayList<>();
		routingStrs.add(polylineStr);
		routingStrs.add(orthogonalStr);
		return routingStrs;

	}

	/**
	 * 
	 */
	@Override
	public void setParameters(Parameter[] parameters) {

		this.parameters = parameters;
		int k = 0;
		this.routingStr = (String) ((ObjectListParameter) parameters[k++]).getValue();
		this.shapeBufferDistance = ((DoubleParameter) parameters[k++]).getDouble().doubleValue();
		if (this.shapeBufferDistance < 0.0)
			this.shapeBufferDistance = 0.0;
		this.radius = ((DoubleParameter) parameters[k++]).getDouble().doubleValue();
		if (this.radius < 0.0)
			this.radius = 0.0;
		this.nudgingDistance = ((DoubleParameter) parameters[k++]).getDouble().doubleValue();
		if (this.nudgingDistance < 0.0)
			this.nudgingDistance = 0.0;
		this.segmentPenalty = ((BooleanParameter) parameters[k++]).getBoolean().booleanValue();
		this.nudgeOrthogonalSegments = ((BooleanParameter) parameters[k++]).getBoolean().booleanValue();
		if (!this.nudgeOrthogonalSegments)
			this.nudgingDistance = 0.0;
		this.ignoreNodesWithoutEdges = ((BooleanParameter) parameters[k++]).getBoolean().booleanValue();

	}

	/**
	 * 
	 */
	@Override
	public String getCategory() {

		return "Layout";

	}

	/**
	 * 
	 */
	@Override
	public boolean isLayoutAlgorithm() {

		return true;

	}

	/**
	 * 
	 */
	@Override
	public String getDescription() {

		return "<html>Edge routing algorithm based on the Adaptagrams<br>layout library (see http://www.adaptagrams.org)";

	}

	/**
	 * 
	 */
	@Override
	public String getName() {

		return "Adaptagrams Edge Routing";

	}

	/**
	 * 
	 */
	@Override
	public void execute() {

		BackgroundTaskStatusProviderSupportingExternalCall backgroundTaskStatusProvider = new BackgroundTaskStatusProviderSupportingExternalCallImpl(
				"", "");
		View view = MainFrame.getInstance().getActiveEditorSession().getActiveView();

		// set up the router
		Router router;
		int routingType;
		switch (this.routingStr) {

		case polylineStr: // polyline routing
			// router without progress
			// router = new Router(RouterFlag.PolyLineRouting);
			// router with progress
			router = new RouterWithProgress(RouterFlag.PolyLineRouting, backgroundTaskStatusProvider);
			routingType = ConnType.ConnType_PolyLine;
			backgroundTaskStatusProvider.setCurrentStatusText1(getStatusTextPhaseNumber(polylineRoutingFirstPhase));
			backgroundTaskStatusProvider
					.setCurrentStatusText2(getStatusTextPhaseDescription(polylineRoutingFirstPhase));
			break;
		case orthogonalStr: // orthogonal routing
			// router without progress
			// router = new Router(RouterFlag.OrthogonalRouting);
			// router with progress
			router = new RouterWithProgress(RouterFlag.OrthogonalRouting, backgroundTaskStatusProvider);
			routingType = ConnType.ConnType_Orthogonal;
			backgroundTaskStatusProvider.setCurrentStatusText1(getStatusTextPhaseNumber(orthogonalRoutingFirstPhase));
			backgroundTaskStatusProvider
					.setCurrentStatusText2(getStatusTextPhaseDescription(orthogonalRoutingFirstPhase));
			break;
		default:
			return;
		}
		router.setRoutingParameter(RoutingParameter.shapeBufferDistance, this.shapeBufferDistance);
		if (this.segmentPenalty)
			router.setRoutingPenalty(RoutingParameter.segmentPenalty);
		router.setRoutingOption(RoutingOption.nudgeOrthogonalSegmentsConnectedToShapes, this.nudgeOrthogonalSegments);
		router.setRoutingParameter(RoutingParameter.idealNudgingDistance, this.nudgingDistance);

		// define Adaptagrams shapes for all visible nodes and depending on settings
		// ignore nodes without edges
		Collection<Node> visibleNodes = GraphHelper.getVisibleNodes(this.graph.getNodes());
		for (Node node : visibleNodes)
			if (node.getDegree() > 0 || !this.ignoreNodesWithoutEdges)
				AdaptagramsRouting.defineShapeRef(router, view, node);

		// all edges or selected edges
		Collection<Edge> edges = new ArrayList<>();
		if (this.selection.getEdges().isEmpty())
			edges.addAll(this.graph.getEdges());
		else
			edges.addAll(this.selection.getEdges());
		// define Adaptagrams connectors for the edges
		HashMap<Edge, ConnRef> hmConnRefs = new HashMap<>();
		for (Edge edge : edges) {
			ConnRef connRef = AdaptagramsRouting.defineConnRef(router, view, edge, routingType);
			hmConnRefs.put(edge, connRef);
		}

		// create a background task for the routing
		Runnable backgroundTask = createBackgroundTask(router);
		// create a finish swing task to get the bend points from the Adaptagrams router
		// for each edge
		Runnable finishSwingTask = createFinishSwingTask(edges, hmConnRefs, this.radius, view, router);
		BackgroundTaskHelper.issueSimpleTask("Adaptagrams Edge Routing", "", backgroundTask, finishSwingTask,
				backgroundTaskStatusProvider);

	}

	/**
	 * Get status text for the according phase.
	 * 
	 * @param phaseNumber
	 *            phase number
	 * @return status text for the according phase
	 */
	public static String getStatusTextPhaseNumber(int phaseNumber) {

		return "Running phase " + phaseNumber + " of " + phaseNumbers;

	}

	/**
	 * Get status text (description) for the according phase.
	 * 
	 * @param phaseNumber
	 *            phase number
	 * @return status text (description) for the according phase
	 */
	public static String getStatusTextPhaseDescription(int phaseNumber) {

		return phaseDescriptions[phaseNumber - 1];

	}

	/**
	 * Create a background task for the routing.
	 * 
	 * @param router
	 *            the router
	 * @return runnable
	 */
	private static Runnable createBackgroundTask(final Router router) {

		Runnable backgroundTask = new Runnable() {
			@Override
			public void run() {

				try {
					// Date date;
					// SimpleDateFormat simpleDateFormat = new
					// SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SS");
					// date = new Date();
					// outputs router instance to SVG file for debugging purposes
					// router.outputInstanceToSVG("libavoid-debug-" +
					// simpleDateFormat.format(date));
					router.processTransaction();
					// date = new Date();
					// outputs router instance to SVG file for debugging purposes
					// router.outputInstanceToSVG("libavoid-debug-" +
					// simpleDateFormat.format(date));
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
				}

			}

		};
		return backgroundTask;

	}

	/**
	 * Create finish swing task to get the bend points from the Adaptagrams router
	 * for each edge.
	 * 
	 * @param edges
	 *            the edges
	 * @param hmConnRefs
	 *            <code>HashMap</code> containing the edges and the Adaptagrams
	 *            connectors
	 * @param radius
	 *            bend radius for orthogonal edge routing
	 * @param view
	 *            the current view
	 * @param router
	 *            the router
	 * @return runnable
	 */
	private static Runnable createFinishSwingTask(final Collection<Edge> edges, final HashMap<Edge, ConnRef> hmConnRefs,
			final double radius, final View view, final Router router) {

		Runnable finishSwingTask = new Runnable() {

			@Override
			public void run() {

				// for each edge get the according Adaptagrams connector and return the bend
				// points from the routing
				// clean up the bend points
				// set the bend points
				for (Edge edge : edges) {
					ConnRef connRef = hmConnRefs.get(edge);
					if (connRef != null) {
						ArrayList<Vector2d> edgeBends;
						if (radius > 0)
							edgeBends = AdaptagramsRouting.getEdgeBends(connRef, radius);
						else
							edgeBends = AdaptagramsRouting.getEdgeBends(connRef);

						// check for triples of bend points where x1 == x2 == x3 or y1 == y2 == y3 and
						// remove bend point (x2, y2)
						// this happens especially for orthogonal routing when radius > 0
						Collection<Vector2d> edgeBendsToRemove = new ArrayList<>();
						for (int k = 1; k < edgeBends.size() - 1; k++) {
							Vector2d bend_pre_k = edgeBends.get(k - 1);
							Vector2d bend_k = edgeBends.get(k);
							Vector2d bend_post_k = edgeBends.get(k + 1);
							if ((Math.abs(bend_pre_k.x - bend_k.x) < 0.0001
									&& Math.abs(bend_post_k.x - bend_k.x) < 0.0001)
									|| (Math.abs(bend_pre_k.y - bend_k.y) < 0.0001
											&& Math.abs(bend_post_k.y - bend_k.y) < 0.0001))
								edgeBendsToRemove.add(bend_k);
						}
						edgeBends.removeAll(edgeBendsToRemove);

						// for each edge 'edgeBends' should be at least of size two:
						// the start point and end point of the edge are returned as bend points
						if (edgeBends.size() > 0) {
							AttributeHelper.removeEdgeBends(edge);
							AttributeHelper.setEdgeBendStyle(edge, GraphicAttributeConstants.STRAIGHTLINE_CLASSNAME);
							EdgeGraphicAttribute edgeGraphicAttribute = (EdgeGraphicAttribute) edge
									.getAttribute(GraphicAttributeConstants.GRAPHICS);
							DockingAttribute dockingAttribute = edgeGraphicAttribute.getDocking();

							Node srcNode = edge.getSource();
							String srcDocking = dockingAttribute.getSource();
							if (srcDocking != null && srcDocking.indexOf(";") > 0) {
								// edges (or edge segments) which run horizontally or vertically might not be
								// perfectly horizontal or vertical
								// due to the docking position (it's often difficult to set the exact docking
								// position for a perfectly horizontal or
								// vertical edge (segment))
								// orthogonal edge routing might therefore introduce bend points in close
								// proximity to the docking position
								// delete bends points with a distance to the docking position <= 1
								for (int k = 1; k < edgeBends.size(); k++) {
									double deltaX = Math.abs(edgeBends.get(0).x - edgeBends.get(k).x);
									double deltaY = Math.abs(edgeBends.get(0).y - edgeBends.get(k).y);
									if ((deltaX < 0.0001 && deltaY < 1.0001) || (deltaX < 1.0001 && deltaY < 0.0001))
										edgeBends.remove(k);
									else
										break;
								}
								// edge has a docking position at the source node
								// ignore starting point == first bend point
								edgeBends.remove(0);
							} else {
								AdaptagramsRouting.addBendPointForNodePosition(edgeBends, 0, 0,
										AttributeHelper.getPositionVec2d(srcNode));
								// do we have more bend points than the start point and the end point of the
								// edge?
								// if yes fix the source bend points
								// if no remove first bend point
								if (edgeBends.size() > 2)
									AdaptagramsRouting.fixSourceBendPoints(edgeBends, view, srcNode);
								else
									edgeBends.remove(0);
							}

							Node tgtNode = edge.getTarget();
							String tgtDocking = dockingAttribute.getTarget();
							if (tgtDocking != null && tgtDocking.indexOf(";") > 0) {
								// edges (or edge segments) which run horizontally or vertically might not be
								// perfectly horizontal or vertical
								// due to the docking position (it's often difficult to set the exact docking
								// position for a perfectly horizontal or
								// vertical edge (segment))
								// orthogonal edge routing might therefore introduce bend points in close
								// proximity to the docking position
								// delete bends points with a distance to the docking position <= 1
								for (int k = edgeBends.size() - 2; k >= 0; k--) {
									double deltaX = Math
											.abs(edgeBends.get(edgeBends.size() - 1).x - edgeBends.get(k).x);
									double deltaY = Math
											.abs(edgeBends.get(edgeBends.size() - 1).y - edgeBends.get(k).y);
									if ((deltaX < 0.0001 && deltaY < 1.0001) || (deltaX < 1.0001 && deltaY < 0.0001))
										edgeBends.remove(k);
									else
										break;
								}
								// edge has a docking position at the target node
								// ignore end point == last bend point
								edgeBends.remove(edgeBends.size() - 1);
							} else {
								AdaptagramsRouting.addBendPointForNodePosition(edgeBends, edgeBends.size() - 1,
										edgeBends.size(), AttributeHelper.getPositionVec2d(tgtNode));
								// after fixing the source bend points do we have more bend points than the end
								// point of the edge?
								// if yes fix the target bend points
								// if no remove last bend point
								if (edgeBends.size() > 1)
									AdaptagramsRouting.fixTargetBendPoints(edgeBends, view, tgtNode);
								else
									edgeBends.remove(edgeBends.size() - 1);
							}

							if (edgeBends.size() > 0) {
								AttributeHelper.addEdgeBends(edge, edgeBends);
								AttributeHelper.setEdgeBendStyle(edge, GraphicAttributeConstants.POLYLINE_CLASSNAME);
							}
						}
					}
				}

				router.delete();

			}

		};
		return finishSwingTask;

	}

}
