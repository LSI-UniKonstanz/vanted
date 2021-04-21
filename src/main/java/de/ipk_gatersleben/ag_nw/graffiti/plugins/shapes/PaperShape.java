/**
 * Copyright (c) 2009-2015 IPK Gatersleben, Germany
 * Copyright (c) 2016 IPK Gatersleben, Germany; Monash University, Australia
 */

// This class provides a paper like shape
// It can be used in particular for SBGN maps for the annotation glyph
package de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes;

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.Vector2d;
import org.graffiti.graphics.DimensionAttribute;
import org.graffiti.graphics.NodeGraphicAttribute;

/**
 * @author Christian Klukas, Tobias Czauderna
 */
public class PaperShape extends RelativePolyShape {
	
	// definition of offsets and width off callout for paper shapes with callout
	double offsetTop = 0.4; // offset of paper shape from top
	double offsetRight = 0.4; // offset of paper shape from right
	double offsetBottom = 0.4; // offset of paper shape from bottom
	double offsetLeft = 0.4; // offset of paper shape from left
	double calloutWidth = 0.125; // relative width off callout
	
	public PaperShape() {
		
		this.ignorePoints = new HashSet<>();
		this.ignorePoints.add(Integer.valueOf(3));
		this.ignorePoints.add(Integer.valueOf(4));
		
	}
	
	@Override
	public void buildShape(NodeGraphicAttribute nodeGraphicAttribute) {
		
		this.nodeAttr = nodeGraphicAttribute;
		
		DimensionAttribute dimensionAttribute = nodeGraphicAttribute.getDimension();
		double width = dimensionAttribute.getWidth();
		double height = dimensionAttribute.getHeight();
		double rounding = nodeGraphicAttribute.getRoundedEdges();
		if (rounding * 2 > width)
			rounding = width / 2d;
		if (rounding * 2 > height)
			rounding = height / 2d;
		this.roundingRadius = rounding / width;
		double frameThickness = nodeGraphicAttribute.getFrameThickness();
		double offset = frameThickness / 2d;
		
		Collection<Vector2d> points = getRelativePointPositions();
		int[] xPoints = new int[points.size()];
		int[] yPoints = new int[points.size()];
		int k = 0;
		for (Vector2d point : points) {
			xPoints[k] = (int) Math.round(point.x * width);
			if (point.y >= 0 && point.y <= 1)
				yPoints[k] = (int) Math.round(point.y * height);
			else if (point.y > 1)
				yPoints[k] = (int) Math.round(height - (point.y - 1) * width);
			else
				yPoints[k] = (int) Math.round(-point.y * width);
			k++;
		}
		
		this.polygon = new Polygon(xPoints, yPoints, points.size());
		int nOffset = (int) Math.round(offset);
		this.polygon.translate(nOffset, nOffset);
		
		if (Double.compare(Math.floor(offset), offset) == 0) {
			width = width + frameThickness + 1;
			height = height + frameThickness + 1;
		} else {
			width += frameThickness;
			height += frameThickness;
		}
		if (frameThickness < 2.0) {
			width += 1;
			height += 1;
		}
		setThickShape(width, height);
		
	}
	
	@Override
	protected Collection<Vector2d> getRelativePointPositions() {
		
		// paper shape without callout
		double off = this.roundingRadius;
		Collection<Vector2d> points = new ArrayList<>();
		points.add(new Vector2d(0, 0)); // 0
		points.add(new Vector2d(1 - off, 0)); // 1
		points.add(new Vector2d(1, -off)); // 2
		points.add(new Vector2d(1 - off, -off)); // 3
		points.add(new Vector2d(1 - off, 0)); // 4
		points.add(new Vector2d(1, -off)); // 5
		points.add(new Vector2d(1, 1)); // 6
		points.add(new Vector2d(0, 1)); // 7
		points.add(new Vector2d(0, 0)); // 8
		return points;
		
	}
	
}
