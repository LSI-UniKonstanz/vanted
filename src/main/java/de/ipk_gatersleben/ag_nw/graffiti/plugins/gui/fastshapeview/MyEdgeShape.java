/**
 * 
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.fastshapeview;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

import org.AttributeHelper;
import org.graffiti.graph.Edge;
import org.graffiti.graph.GraphElement;
import org.graffiti.graphics.CoordinateAttribute;
import org.graffiti.graphics.DimensionAttribute;
import org.graffiti.graphics.EdgeGraphicAttribute;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.graphics.NodeGraphicAttribute;
import org.graffiti.plugin.view.CoordinateSystem;
import org.graffiti.plugins.views.defaults.RectangleNodeShape;
import org.graffiti.plugins.views.defaults.RectangularNodeShape;

/**
 * @author matthiak
 *
 */
public class MyEdgeShape implements MyShape{

	GeneralPath path;
	private Line2D line;
	
	Shape	sourceArrowShape;
	Shape	targetArrowShape;
	
	/**
	 * 
	 */
	public MyEdgeShape(GraphElement ge) {
		update(ge);
	}
	
	
	@Override
	public void update(GraphElement ge) {
		EdgeGraphicAttribute ega = (EdgeGraphicAttribute)ge.getAttribute(GraphicAttributeConstants.GRAPHICS);

		Edge edge = (Edge)ge;
		NodeGraphicAttribute sourceNga = (NodeGraphicAttribute)edge.getSource().getAttribute(GraphicAttributeConstants.GRAPHICS);
		NodeGraphicAttribute targetNga = (NodeGraphicAttribute)edge.getTarget().getAttribute(GraphicAttributeConstants.GRAPHICS);
		CoordinateAttribute sourcecoordinate = sourceNga.getCoordinate();
		CoordinateAttribute targetcoordinate = targetNga.getCoordinate();
		DimensionAttribute sourcedimension = sourceNga.getDimension();
		DimensionAttribute targetdimension = targetNga.getDimension();
	
		if(line == null)
			line = new Line2D.Double();
		
		line.setLine(
				sourcecoordinate.getX(), 
				sourcecoordinate.getY(),
				targetcoordinate.getX(),  
				targetcoordinate.getY());
		
		
		Rectangle2D.Double rect = new Rectangle2D.Double();
		rect.setFrame(
				targetcoordinate.getX() - targetdimension.getWidth()/2, 
				targetcoordinate.getY() - targetdimension.getHeight() / 2, 
				targetdimension.getWidth(), 
				targetdimension.getHeight());
		Point2D targetIntersect = RectangleNodeShape.getIntersectionOfRoundRectangleAndLine(
				line, 
				rect, 0.0);
		rect.setFrame(
				sourcecoordinate.getX() - sourcedimension.getWidth()/2, 
				sourcecoordinate.getY() - sourcedimension.getHeight() / 2, 
				sourcedimension.getWidth(), 
				sourcedimension.getHeight());
		if(targetIntersect == null)
			System.out.println();
		Point2D sourceIntersect = RectangleNodeShape.getIntersectionOfRoundRectangleAndLine(
				line, 
				rect, 0.0);
		if(targetIntersect != null && sourceIntersect != null)
		line.setLine(
				sourceIntersect.getX(),
				sourceIntersect.getY(),
				targetIntersect.getX(),
				targetIntersect.getY());
		
		
		path = new GeneralPath(line);
		
	}


	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		g2.draw(path);
		if(sourceArrowShape != null)
		g2.draw(sourceArrowShape);
		if((targetArrowShape != null))
		g2.draw(targetArrowShape);
	}


	public Rectangle getBounds() {
		return path.getBounds();
	}

	
	public Rectangle2D getBounds2D() {
		return path.getBounds2D();
	}

	
	public boolean contains(double x, double y) {
		return path.contains(x, y);
	}

	
	public boolean contains(Point2D p) {
		return path.contains(p);
	}

	
	public boolean intersects(double x, double y, double w, double h) {
		return path.intersects(x, y, w, h);
	}

	
	public boolean intersects(Rectangle2D r) {
		return path.intersects(r);
	}

	
	public boolean contains(double x, double y, double w, double h) {
		return path.contains(x, y, w, h);
	}

	
	public boolean contains(Rectangle2D r) {
		return path.contains(r);
	}

	
	public PathIterator getPathIterator(AffineTransform at) {
		return path.getPathIterator(at);
	}

	
	public PathIterator getPathIterator(AffineTransform at, double flatness) {
		return path.getPathIterator(at, flatness);
	}


	@Override
	public Rectangle2D getRealBounds2D() {
		return getBounds2D();
	}



	@Override
	public void setCoordinateSystem(CoordinateSystem coordinates) {
	}



	@Override
	public double getXexcess() {
		return 0;
	}



	@Override
	public double getYexcess() {
		return 0;
	}



}
