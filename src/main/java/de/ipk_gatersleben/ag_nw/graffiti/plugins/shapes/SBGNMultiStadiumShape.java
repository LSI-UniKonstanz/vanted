/**
 * Copyright (c) 2016 Monash University, Australia
 */

// This class provides a multi stadium shape
// It can be used in particular for SBGN PD maps for the multimer simple chemical glyph
package de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes;

import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;

import org.graffiti.attributes.IntegerAttribute;
import org.graffiti.graphics.NodeGraphicAttribute;
import org.graffiti.plugin.view.ProvidesAdditonalDrawingShapes;
import org.graffiti.plugins.views.defaults.RectangleNodeShape;

/**
 * @author Tobias Czauderna
 */
@SuppressWarnings("nls")
public class SBGNMultiStadiumShape extends RectangleNodeShape implements ProvidesAdditonalDrawingShapes {
	
	ArrayList<Shape> shapes = null;
	SBGNMultiStadiumShape sbgnMultiStadiumShape = null;
	
	private int iOffX = 10;
	private int iOffY = 10;
	
	public SBGNMultiStadiumShape() {
		
		this.addSx = this.iOffX + 1;
		this.addSy = this.iOffY + 1;
		this.sbgnMultiStadiumShape = new SBGNMultiStadiumShape(true);
		this.shapes = new ArrayList<>();
		this.shapes.add(this.sbgnMultiStadiumShape);
		
	}
	
	@SuppressWarnings("unused")
	public SBGNMultiStadiumShape(boolean multi) {
		
		this.offX = this.iOffX;
		this.offY = this.iOffY;
		
	}
	
	@Override
	public Point2D getIntersection(Line2D line) {
		
		Rectangle2D rectangle = getRealBounds2D();
		double x = rectangle.getCenterX();
		double y = rectangle.getCenterY();
		double width = rectangle.getWidth();
		double heigth = rectangle.getHeight();
		x -= this.iOffX / 2d;
		y -= this.iOffY / 2d;
		width -= this.iOffX;
		heigth -= this.iOffY;
		rectangle.setFrame(x - width / 2, y - heigth / 2, width, heigth);
		
		double rounding = getRounding() / 2;
		
		return RectangleNodeShape.getIntersectionOfRoundRectangleAndLine(line, rectangle, rounding);
		
	}
	
	@Override
	public void buildShape(NodeGraphicAttribute graphics) {
		
		// correct width if necessary, width should be >= height
		if (graphics.getDimension().getHeight() > graphics.getDimension().getWidth())
			graphics.getDimension().setWidth(graphics.getDimension().getHeight());
		
		if (!graphics.getCollection().containsKey("offX"))
			graphics.add(new IntegerAttribute("offX", this.iOffX), false);
		this.iOffX = ((IntegerAttribute) graphics.getAttribute("offX")).getInteger();
		
		if (!graphics.getCollection().containsKey("offY"))
			graphics.add(new IntegerAttribute("offY", this.iOffY), false);
		this.iOffY = ((IntegerAttribute) graphics.getAttribute("offY")).getInteger();
		
		this.addSx = this.iOffX + 1;
		this.addSy = this.iOffY + 1;
		
		if (this.sbgnMultiStadiumShape == null) {
			this.offX = this.iOffX;
			this.offY = this.iOffY;
		}
		
		super.buildShape(graphics);
		
		if (this.sbgnMultiStadiumShape != null) {
			this.sbgnMultiStadiumShape.buildShape(this.nodeAttr);
		}
		
	}
	
	@Override
	public Collection<Shape> getPreBorderShapes() {
		
		if (this.sbgnMultiStadiumShape != null) {
			return this.shapes;
		}
		return null;
		
	}
	
	@Override
	public Collection<Shape> getPostBorderShapes() {
		
		return null;
		
	}
	
	@Override
	public int shapeWidthCorrection() {
		
		return -this.addSx;
		
	}
	
	@Override
	public int shapeHeightCorrection() {
		
		return -this.addSy;
		
	}
	
	@Override
	protected double getRounding() {
		
		return this.nodeAttr.getDimension().getHeight();
		
	}
	
}
