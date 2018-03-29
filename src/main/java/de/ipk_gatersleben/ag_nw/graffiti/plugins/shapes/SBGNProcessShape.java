/**
 * Copyright (c) 2016 Monash University, Australia
 */

// This class provides a quadratic shape with little linear sticks left - right or top - bottom of the shape
// It can be used in particular for SBGN PD maps for the process glyph, the omitted process glyph, and the uncertain process glyph
package de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes;

import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.Collection;

import org.graffiti.graphics.DimensionAttribute;
import org.graffiti.graphics.NodeGraphicAttribute;
import org.graffiti.plugin.view.ProvidesAdditonalDrawingShapes;
import org.graffiti.plugins.views.defaults.RectangleNodeShape;

/**
 * @author Tobias Czauderna
 */
public class SBGNProcessShape extends RectangleNodeShape implements ProvidesAdditonalDrawingShapes {

	@Override
	public void buildShape(NodeGraphicAttribute nodeGraphicAttribute) {

		this.nodeAttr = nodeGraphicAttribute;

		DimensionAttribute dimensionAttribute = nodeGraphicAttribute.getDimension();
		// width and height include the sticks left - right / top - bottom
		double width = dimensionAttribute.getWidth();
		double height = dimensionAttribute.getHeight();

		double frameThickness = Math.floor(nodeGraphicAttribute.getFrameThickness());
		double offset = frameThickness / 2d;

		double offsetX = offset;
		double offsetY = offset;
		// set size of the square according to min(width, height)
		if (width > height) // horizontal process
			this.rect2D.setFrame((width - height) / 2d + offsetX + this.offX, offsetY + this.offY, height, height);
		else if (height > width) // vertical process
			this.rect2D.setFrame(offsetX + this.offX, (height - width) / 2d + offsetY + this.offY, width, width);
		else
			this.rect2D.setFrame(offsetX + this.offX, offsetY + this.offY, width, height); // sticks not visible

		double correctedWidth = width + frameThickness;
		double correctedHeight = height + frameThickness;
		if (Double.compare(Math.floor(offset), offset) == 0) {
			correctedWidth = width + frameThickness + 1;
			correctedHeight = height + frameThickness + 1;
		}
		// set shape size including sticks
		((RectangularShape) this.thickShape).setFrame(0, 0, correctedWidth + this.addSx, correctedHeight + this.addSy);

	}

	@Override
	public Point2D getIntersection(Line2D line) {

		Rectangle2D rectangle = getRealBounds2D();
		double x = rectangle.getX();
		double y = rectangle.getY();
		double width = rectangle.getWidth();
		double height = rectangle.getHeight();
		double rounding = getRounding() / 2;
		// correct size to width == height, only the quadratic shape is considered for
		// calculation of intersection
		if (width > height)
			rectangle = new Rectangle2D.Double((width - height) / 2d + x, y, height, height);
		else
			rectangle = new Rectangle2D.Double(x, (height - width) / 2d + y, width, width);
		return getIntersectionOfRoundRectangleAndLine(line, rectangle, rounding);

	}

	@Override
	public Collection<Shape> getPreBorderShapes() {

		return null;

	}

	@Override
	public Collection<Shape> getPostBorderShapes() {

		Rectangle2D bounds2D = getBounds2D();
		double width = bounds2D.getWidth();
		double height = bounds2D.getHeight();

		ArrayList<Shape> stickShapes = new ArrayList<>();
		if (width > height) { // horizontal sticks
			double length = (width - height) / 2d;
			stickShapes.add(new Line2D.Double(bounds2D.getMinX(), bounds2D.getCenterY(), bounds2D.getMinX() + length,
					bounds2D.getCenterY()));
			stickShapes.add(new Line2D.Double(bounds2D.getMaxX() - length, bounds2D.getCenterY(),
					bounds2D.getMaxX() - 1, bounds2D.getCenterY()));
		} else if (height > width) { // vertical sticks
			double length = (height - width) / 2d;
			stickShapes.add(new Line2D.Double(bounds2D.getCenterX(), bounds2D.getMinY(), bounds2D.getCenterX(),
					bounds2D.getMinY() + length));
			stickShapes.add(new Line2D.Double(bounds2D.getCenterX(), bounds2D.getMaxY() - length, bounds2D.getCenterX(),
					bounds2D.getMaxY() - 1));
		} else
			return null; // sticks not visible
		return stickShapes;

	}

	@Override
	protected double getRounding() {

		return 0;

	}

}
