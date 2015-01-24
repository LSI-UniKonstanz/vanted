/**
 * 
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.fastshapeview;


import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

import org.graffiti.graph.GraphElement;
import org.graffiti.graphics.CoordinateAttribute;
import org.graffiti.graphics.DimensionAttribute;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.graphics.NodeGraphicAttribute;
import org.graffiti.plugin.view.CoordinateSystem;

/**
 * @author matthiak
 *
 */
public class MyNodeShape implements MyShape{

	RectangularShape shape;
	
	/**
	 * 
	 */
	public MyNodeShape(GraphElement ge) {
		NodeGraphicAttribute nga = (NodeGraphicAttribute)ge.getAttribute(GraphicAttributeConstants.GRAPHICS);

		CoordinateAttribute coordinate = nga.getCoordinate();
		DimensionAttribute dimension = nga.getDimension();
		shape = new Rectangle2D.Double(
				coordinate.getX() - dimension.getWidth()/2,
				coordinate.getY() - dimension.getHeight() /2,
				dimension.getWidth(),
				dimension.getHeight()
				);

	}


	
	@Override
	public void paint(java.awt.Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		g2d.draw(shape);
	}



	public void update(GraphElement ge) {
		NodeGraphicAttribute nga = (NodeGraphicAttribute)ge.getAttribute(GraphicAttributeConstants.GRAPHICS);

		CoordinateAttribute coordinate = nga.getCoordinate();
		DimensionAttribute dimension = nga.getDimension();
		
		shape.setFrame(coordinate.getX() - dimension.getWidth()/2,
				coordinate.getY() - dimension.getHeight() /2,
				dimension.getWidth(),
				dimension.getHeight());

//		setPosition(coordinate.getX(), coordinate.getY());
//		setDimension(dimension.getWidth(), dimension.getHeight());
	}

	
	public void setPosition(double x, double y) {
		shape.setFrame(x - shape.getWidth()/2, y - shape.getHeight()/2, shape.getWidth(), shape.getHeight());
	}

	
	public void setDimension(double w, double h) {
		shape.setFrame(shape.getX() - w/2, shape.getY() - h/2, w, h);

	}
	
	
	public Rectangle getBounds() {
		return shape.getBounds();
	}

	
	public Rectangle2D getBounds2D() {
		return shape.getBounds2D();
	}

	
	public boolean contains(double x, double y) {
		return shape.contains(x, y);
	}

	
	public boolean contains(Point2D p) {
		return shape.contains(p);
	}

	
	public boolean intersects(double x, double y, double w, double h) {
		return shape.intersects(x, y, w, h);
	}

	
	public boolean intersects(Rectangle2D r) {
		return shape.intersects(r);
	}

	
	public boolean contains(double x, double y, double w, double h) {
		return shape.contains(x, y, w, h);
	}

	
	public boolean contains(Rectangle2D r) {
		return shape.contains(r);
	}

	
	public PathIterator getPathIterator(AffineTransform at) {
		return shape.getPathIterator(at);
	}

	
	public PathIterator getPathIterator(AffineTransform at, double flatness) {
		return shape.getPathIterator(at, flatness);
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
