/**
 * Copyright (c) 2014-2015 Monash University, Australia
 */
package org.vanted.plugins.layout.adaptagrams.edgerouting;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import org.AttributeHelper;
import org.Vector2d;
import org.adaptagrams.AvoidCheckpoints;
import org.adaptagrams.AvoidRectangle;
import org.adaptagrams.ConnDirFlag;
import org.adaptagrams.ConnEnd;
import org.adaptagrams.ConnRef;
import org.adaptagrams.Point;
import org.adaptagrams.Polygon;
import org.adaptagrams.Router;
import org.adaptagrams.ShapeRef;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Node;
import org.graffiti.graphics.DockingAttribute;
import org.graffiti.graphics.EdgeGraphicAttribute;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.plugin.view.GraphElementComponent;
import org.graffiti.plugin.view.NodeShape;
import org.graffiti.plugin.view.View;
import org.graffiti.plugins.views.defaults.NodeComponent;

/**
 * @author Tobias Czauderna
 */
public class AdaptagramsRouting {
	
	/**
	 * Defines a ShapeRef for a node.
	 * 
	 * @param router
	 *           the router
	 * @param view
	 *           the current view
	 * @param node
	 *           the node
	 * @return Adaptagrams shape (<code>ShapeRef</code>)
	 */
	public static ShapeRef defineShapeRef(Router router, View view, Node node) {
		
		NodeShape nodeShape = getNodeShape(view, node);
		System.out.println("Node " + node + "");
		Rectangle2D rectangle2D = nodeShape.getRealBounds2D();
		AvoidRectangle avoidRectangle = new AvoidRectangle(new Point(rectangle2D.getX(), rectangle2D.getY()),
				new Point(rectangle2D.getX() + rectangle2D.getWidth(), rectangle2D.getY() + rectangle2D.getHeight()));
		ShapeRef shapeRef = new ShapeRef(router, avoidRectangle);
		return shapeRef;
		
	}
	
	/**
	 * Defines a ConnRef for an edge. Start point is either the position of the
	 * source node or the docking position at the source node, end point is either
	 * the position of the target node or the docking position at the target node.
	 * 
	 * @param router
	 *           the router
	 * @param view
	 *           the current view
	 * @param edge
	 *           the edge
	 * @param routingType
	 *           routing type (<code>ConnType.ConnType_PolyLine</code> or
	 *           <code>ConnType.ConnType_Orthogonal</code>)
	 * @return Adaptagrams connector (<code>ConnRef</code>)
	 */
	public static ConnRef defineConnRef(Router router, View view, Edge edge, int routingType) {
		
		return defineConnRef(router, view, edge, ConnDirFlag.ConnDirAll, ConnDirFlag.ConnDirAll, routingType);
		
	}
	
	/**
	 * Defines a ConnRef for an edge. Start point is either the position of the
	 * source node or the docking position at the source node, end point is either
	 * the position of the target node or the docking position at the target node.
	 * 
	 * @param router
	 *           the router
	 * @param view
	 *           the current view
	 * @param edge
	 *           the edge
	 * @param routingType
	 *           routing type (<code>ConnType.ConnType_PolyLine</code> or
	 *           <code>ConnType.ConnType_Orthogonal</code>)
	 * @param avoidCheckpoints
	 *           Adaptagrams checkpoints (<code>AvoidCheckpoints</code>), list of
	 *           checkpoints the connector (the edge) has to pass
	 * @return Adaptagrams connector (<code>ConnRef</code>)
	 */
	public static ConnRef defineConnRef(Router router, View view, Edge edge, int routingType,
			AvoidCheckpoints avoidCheckpoints) {
		
		ConnRef connRef = defineConnRef(router, view, edge, ConnDirFlag.ConnDirAll, ConnDirFlag.ConnDirAll,
				routingType);
		connRef.setRoutingCheckpoints(avoidCheckpoints);
		return connRef;
		
	}
	
	/**
	 * Defines a ConnRef for an edge. Start point is either the position of the
	 * source node or the docking position at the source node, end point is either
	 * the position of the target node or the docking position at the target node.
	 * 
	 * @param router
	 *           the router
	 * @param view
	 *           the current view
	 * @param edge
	 *           the edge
	 * @param srcConnDirFlag
	 *           edge direction at source node
	 *           (<code>ConnDirFlag.ConnDirAll</code>,
	 *           <code>ConnDirFlag.ConnDirUp</code>,
	 *           <code>ConnDirFlag.ConnDirRight</code>,
	 *           <code>ConnDirFlag.ConnDirDown</code>,
	 *           <code>ConnDirFlag.ConnDirLeft</code> or
	 *           <code>ConnDirFlag.ConnDirNone</code>)
	 * @param tgtConnDirFlag
	 *           edge direction at target node
	 *           (<code>ConnDirFlag.ConnDirAll</code>,
	 *           <code>ConnDirFlag.ConnDirUp</code>,
	 *           <code>ConnDirFlag.ConnDirRight</code>,
	 *           <code>ConnDirFlag.ConnDirDown</code>,
	 *           <code>ConnDirFlag.ConnDirLeft</code> or
	 *           <code>ConnDirFlag.ConnDirNone</code>)
	 * @param routingType
	 *           routing type (<code>ConnType.ConnType_PolyLine</code> or
	 *           <code>ConnType.ConnType_Orthogonal</code>)
	 * @param avoidCheckpoints
	 *           Adaptagrams checkpoints (<code>AvoidCheckpoints</code>), list of
	 *           checkpoints the connector (the edge) has to pass
	 * @return Adaptagrams connector (<code>ConnRef</code>)
	 */
	public static ConnRef defineConnRef(Router router, View view, Edge edge, int srcConnDirFlag, int tgtConnDirFlag,
			int routingType, AvoidCheckpoints avoidCheckpoints) {
		
		ConnRef connRef = defineConnRef(router, view, edge, srcConnDirFlag, tgtConnDirFlag, routingType);
		connRef.setRoutingCheckpoints(avoidCheckpoints);
		return connRef;
		
	}
	
	/**
	 * Defines a ConnRef for an edge. Start point is either the position of the
	 * source node or the docking position at the source node, end point is either
	 * the position of the target node or the docking position at the target node.
	 * 
	 * @param router
	 *           the router
	 * @param view
	 *           the current view
	 * @param edge
	 *           the edge
	 * @param srcConnDirFlag
	 *           edge direction at source node
	 *           (<code>ConnDirFlag.ConnDirAll</code>,
	 *           <code>ConnDirFlag.ConnDirUp</code>,
	 *           <code>ConnDirFlag.ConnDirRight</code>,
	 *           <code>ConnDirFlag.ConnDirDown</code>,
	 *           <code>ConnDirFlag.ConnDirLeft</code> or
	 *           <code>ConnDirFlag.ConnDirNone</code>)
	 * @param tgtConnDirFlag
	 *           edge direction at target node
	 *           (<code>ConnDirFlag.ConnDirAll</code>,
	 *           <code>ConnDirFlag.ConnDirUp</code>,
	 *           <code>ConnDirFlag.ConnDirRight</code>,
	 *           <code>ConnDirFlag.ConnDirDown</code>,
	 *           <code>ConnDirFlag.ConnDirLeft</code> or
	 *           <code>ConnDirFlag.ConnDirNone</code>)
	 * @param routingType
	 *           routing type (<code>ConnType.ConnType_PolyLine</code> or
	 *           <code>ConnType.ConnType_Orthogonal</code>
	 * @return Adaptagrams connector (<code>ConnRef</code>)
	 */
	public static ConnRef defineConnRef(Router router, View view, Edge edge, int srcConnDirFlag, int tgtConnDirFlag,
			int routingType) {
		
		Point srcPoint, tgtPoint;
		EdgeGraphicAttribute edgeGraphicAttribute = (EdgeGraphicAttribute) edge
				.getAttribute(GraphicAttributeConstants.GRAPHICS);
		DockingAttribute dockingAttribute = edgeGraphicAttribute.getDocking();
		
		String srcDocking = dockingAttribute.getSource();
		if (srcDocking != null && srcDocking.indexOf(";") > 0) {
			NodeShape nodeShape = getNodeShape(view, edge.getSource());
			Rectangle2D srcRectangle2D = nodeShape.getRealBounds2D();
			srcPoint = calculateDockingPoint(srcDocking, srcRectangle2D, AttributeHelper.getSize(edge.getSource()));
		} else {
			Vector2d srcPosition = AttributeHelper.getPositionVec2d(edge.getSource());
			srcPoint = new Point(srcPosition.x, srcPosition.y);
		}
		ConnEnd srcConnEnd = new ConnEnd(srcPoint, srcConnDirFlag);
		
		String tgtDocking = dockingAttribute.getTarget();
		if (tgtDocking != null && tgtDocking.indexOf(";") > 0) {
			NodeShape nodeShape = getNodeShape(view, edge.getTarget());
			Rectangle2D tgtRectangle2D = nodeShape.getRealBounds2D();
			tgtPoint = calculateDockingPoint(tgtDocking, tgtRectangle2D, AttributeHelper.getSize(edge.getTarget()));
		} else {
			Vector2d tgtPosition = AttributeHelper.getPositionVec2d(edge.getTarget());
			tgtPoint = new Point(tgtPosition.x, tgtPosition.y);
		}
		ConnEnd tgtConnEnd = new ConnEnd(tgtPoint, tgtConnDirFlag);
		
		ConnRef connRef = new ConnRef(router, srcConnEnd, tgtConnEnd);
		connRef.setRoutingType(routingType);
		return connRef;
		
	}
	
	/**
	 * Calculates the docking point of an edge at a node if a docking is defined for
	 * the edge.
	 * 
	 * @param docking
	 *           docking position at the node
	 * @param rectangle2D
	 *           rectangle with real bounds for the node
	 * @param nodeSize
	 *           node size
	 * @return docking point
	 */
	private static Point calculateDockingPoint(String docking, Rectangle2D rectangle2D, Vector2d nodeSize) {
		
		// use the rectangle size when docking coordinate > 1 or docking coordinate < -1
		// use the node size otherwise in the according dimension
		double width = rectangle2D.getWidth();
		double height = rectangle2D.getHeight();
		String[] dockingArr = docking.split(";");
		
		double dockingFactorX = Double.valueOf(dockingArr[0]).doubleValue();
		double dockingOffsetX = 0;
		if (dockingFactorX < -1 || dockingFactorX > 1) {
			dockingOffsetX = dockingFactorX;
			dockingFactorX = dockingFactorX > 0 ? 1 : -1;
		} else
			width = nodeSize.x;
		
		double dockingFactorY = Double.valueOf(dockingArr[1]).doubleValue();
		double dockingOffsetY = 0;
		if (dockingFactorY < -1 || dockingFactorY > 1) {
			dockingOffsetY = dockingFactorY;
			dockingFactorY = dockingFactorY > 0 ? 1 : -1;
		} else
			height = nodeSize.y;
		
		return new Point(rectangle2D.getCenterX() + (width * 0.5 * dockingFactorX) + dockingOffsetX,
				rectangle2D.getCenterY() + (height * 0.5 * dockingFactorY) + dockingOffsetY);
		
	}
	
	/**
	 * Returns the edge bend points for a connector.
	 * 
	 * @param connRef
	 *           Adaptagrams connector (<code>ConnRef</code>)
	 * @return <code>ArrayList</code> of bend points
	 */
	public static ArrayList<Vector2d> getEdgeBends(ConnRef connRef) {
		
		Polygon polygon = connRef.displayRoute();
		return getEdgeBends(polygon);
		
	}
	
	/**
	 * Returns the edge bend points for a connector.
	 * 
	 * @param connRef
	 *           Adaptagrams connector (<code>ConnRef</code>)
	 * @param radius
	 *           bend radius
	 * @return <code>ArrayList</code> of bend points
	 */
	public static ArrayList<Vector2d> getEdgeBends(ConnRef connRef, double radius) {
		
		Polygon polygon = connRef.displayRoute().curvedPolyline(2 * radius);
		return getEdgeBends(polygon);
		
	}
	
	/**
	 * Returns the edge bend points for a polygon (from a connector).
	 * 
	 * @param polygon
	 *           Adaptagrams polygon (<code>Polygon</code>)
	 * @return <code>ArrayList</code> of bend points
	 */
	private static ArrayList<Vector2d> getEdgeBends(Polygon polygon) {
		
		ArrayList<Vector2d> edgeBends = new ArrayList<>();
		for (int k = 0; k < polygon.size(); k++) {
			Vector2d edgeBend = new Vector2d(polygon.at(k).getX(), polygon.at(k).getY());
			edgeBends.add(edgeBend);
		}
		return edgeBends;
		
	}
	
	/**
	 * Returns the node shape.
	 * 
	 * @param view
	 *           the current view
	 * @param node
	 *           the node
	 * @return node shape
	 */
	private static NodeShape getNodeShape(View view, Node node) {
		
		NodeShape nodeShape = null;
		if (view != null) {
			GraphElementComponent graphElementComponent = view.getComponentForElement(node);
			if (graphElementComponent instanceof NodeComponent) {
				NodeComponent nodeComponent = (NodeComponent) graphElementComponent;
				nodeShape = (NodeShape) nodeComponent.getShape();
			}
		}
		return nodeShape;
		
	}
	
	/**
	 * Adds an edge bend point for the node position.
	 * 
	 * @param edgeBends
	 *           <code>ArrayList</code> of bend points
	 * @param compareIndex
	 *           compare the node position with bend point at this index
	 * @param addIndex
	 *           add a bend point for the node position at this index
	 * @param nodePos
	 *           node position
	 */
	public static void addBendPointForNodePosition(ArrayList<Vector2d> edgeBends, int compareIndex, int addIndex,
			Vector2d nodePos) {
		
		// check whether 'edgeBends' already contains a bend point with same coordinates
		// as node position
		// otherwise add bend point for the node position
		if (Double.compare(edgeBends.get(compareIndex).x, nodePos.x) != 0
				|| Double.compare(edgeBends.get(compareIndex).y, nodePos.y) != 0)
			edgeBends.add(addIndex, nodePos);
		
	}
	
	/**
	 * Fixes the edge bend points at the source node. Only edge bend points outside
	 * the node are kept.
	 * 
	 * @param edgeBends
	 *           <code>ArrayList</code> of bend points
	 * @param view
	 *           the current view
	 * @param node
	 *           the node
	 */
	public static void fixSourceBendPoints(ArrayList<Vector2d> edgeBends, View view, Node node) {
		
		NodeShape nodeShape = getNodeShape(view, node);
		if (nodeShape != null)
			for (int k = 0; k <= edgeBends.size() - 2; k++) {
				Point2D intersectionPoint = nodeShape.getIntersection(new Line2D.Double(edgeBends.get(k).x,
						edgeBends.get(k).y, edgeBends.get(k + 1).x, edgeBends.get(k + 1).y));
				if (intersectionPoint != null) {
					Vector2d innerBendPoint = edgeBends.get(k);
					// k+1 is the first bend point outside the current source node shape
					edgeBends.subList(0, k + 1).clear();
					if (k > 0) {
						Vector2d bendPoint = calculateBendPoint(innerBendPoint, intersectionPoint);
						edgeBends.add(0, bendPoint);
					}
					break;
				}
			}
		else
			edgeBends.clear();
		
	}
	
	/**
	 * Fixes the edge bend points at the target node. Only edge bend points outside
	 * the node are kept.
	 * 
	 * @param edgeBends
	 *           <code>ArrayList</code> of bend points
	 * @param view
	 *           the current view
	 * @param node
	 *           the node
	 */
	public static void fixTargetBendPoints(ArrayList<Vector2d> edgeBends, View view, Node node) {
		
		NodeShape nodeShape = getNodeShape(view, node);
		if (nodeShape != null)
			for (int k = 0; k <= edgeBends.size() - 2; k++) {
				int l = edgeBends.size() - 2 - k;
				Point2D intersectionPoint = nodeShape.getIntersection(new Line2D.Double(edgeBends.get(l).x,
						edgeBends.get(l).y, edgeBends.get(l + 1).x, edgeBends.get(l + 1).y));
				if (intersectionPoint != null) {
					Vector2d innerBendPoint = edgeBends.get(l + 1);
					// l is the first bend point outside the current target node shape
					edgeBends.subList(l + 1, edgeBends.size()).clear();
					if (k > 0) {
						Vector2d outerBendPoint = calculateBendPoint(innerBendPoint, intersectionPoint);
						edgeBends.add(edgeBends.size(), outerBendPoint);
					}
					break;
				}
			}
		else
			edgeBends.clear();
		
	}
	
	/**
	 * Calculates (corrects) the first/last edge bend point outside the node based
	 * on the intersection point between edge and node. This bend point cannot be
	 * identical with the intersection point. During drawing Vanted considers edge
	 * bend points on the node border (the intersection points) as being within the
	 * node. Edges are therefore drawn within the node. To avoid this the first/last
	 * bend point needs to be corrected.
	 * 
	 * @param innerBendPoint
	 *           bend point within node
	 * @param intersectionPoint
	 *           intersection point between edge and node
	 * @return bend point
	 */
	private static Vector2d calculateBendPoint(Vector2d innerBendPoint, Point2D intersectionPoint) {
		
		double deltaX = intersectionPoint.getX() - innerBendPoint.x;
		double deltaY = intersectionPoint.getY() - innerBendPoint.y;
		if (Math.abs(deltaX) > Math.abs(deltaY))
			deltaY = 0;
		else
			deltaX = 0;
		return new Vector2d(intersectionPoint.getX() + Math.signum(deltaX) * 0.5,
				intersectionPoint.getY() + Math.signum(deltaY) * 0.5);
		
	}
	
}
