/**
 * Copyright (c) 2016 Monash University, Australia
 */

// This class provides a circular shape with little linear sticks left - right or top - bottom of the shape
// It can be used in particular for SBGN PD maps for the dissociation glyph
package de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes;

import java.awt.Shape;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.Collection;

import org.graffiti.graphics.DimensionAttribute;
import org.graffiti.graphics.NodeGraphicAttribute;

/**
 * @author Tobias Czauderna
 */
public class SBGNDissociationShape extends SBGNCircleShape {
	
	ArrayList<Shape> shapes = null;
	SBGNDissociationShape sbgnDissociationShape = null;
	
	public SBGNDissociationShape() {
		
		this.addSx = 0;
		this.addSy = 0;
		this.sbgnDissociationShape = new SBGNDissociationShape(0, 0);
		this.shapes = new ArrayList<>();
		this.shapes.add(this.sbgnDissociationShape);
		
	}
	
	public SBGNDissociationShape(int offX, int offY) {
		
		this.offX = this.offX + offX;
		this.offY = this.offY + offY;
		
	}
	
	@Override
	public void buildShape(NodeGraphicAttribute nodeGraphicAttribute) {
		
		this.nodeAttr = nodeGraphicAttribute;
		
		DimensionAttribute dimensionAttribute = nodeGraphicAttribute.getDimension();
		// width and height include the sticks left - right / top - bottom
		double width = dimensionAttribute.getWidth();
		double height = dimensionAttribute.getHeight();
		
		double frameThickness = Math.floor(nodeGraphicAttribute.getFrameThickness());
		double offset = frameThickness / 2d;
		
		if (this.sbgnDissociationShape != null) {
			int delta = (int) frameThickness;
			if (width >= height)
				delta = delta + (int) Math.floor(height / 6d);
			else
				delta = delta + (int) Math.floor(width / 6d);
			this.addSx = delta * 2;
			this.addSy = delta * 2;
			width = width - delta * 2;
			height = height - delta * 2;
			this.offX = delta;
			this.offY = delta;
		}
		
		double offsetX = offset;
		double offsetY = offset;
		// set size of the circle according to min(width, height)
		if (width > height) // horizontal dissociation
			this.ell2D.setFrame((width - height) / 2d + offsetX + this.offX, offsetY + this.offY, height, height);
		else
			if (height > width) // vertical dissociation
				this.ell2D.setFrame(offsetX + this.offX, (height - width) / 2d + offsetY + this.offY, width, width);
			else
				this.ell2D.setFrame(offsetX + this.offX, offsetY + this.offY, width, height); // sticks not visible
				
		double correctedWidth = width + frameThickness;
		double correctedHeight = height + frameThickness;
		if (Double.compare(Math.floor(offset), offset) == 0) {
			correctedWidth = width + frameThickness + 1;
			correctedHeight = height + frameThickness + 1;
		}
		// set shape size including sticks
		((RectangularShape) this.thickShape).setFrame(0, 0, correctedWidth + this.addSx, correctedHeight + this.addSy);
		
		if (this.sbgnDissociationShape != null) {
			this.sbgnDissociationShape.buildShape(nodeGraphicAttribute);
		}
		
	}
	
	@Override
	public Collection<Shape> getPreBorderShapes() {
		
		if (this.sbgnDissociationShape != null)
			return this.shapes;
		return null;
		
	}
	
}
