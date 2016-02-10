/**
 * Copyright (c) 2016 Monash University, Australia
 */

// This class provides a paper like shape with a callout
// It can be used in particular for SBGN maps for the annotation glyph
package de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.Vector2d;

/**
 * @author Tobias Czauderna
 */
public class PaperShapeRightTop extends PaperShape {
	
	double calloutPos = 0.125; // relative position from right-top corner
	
	public PaperShapeRightTop() {
		
		this.ignorePoints = new HashSet<>();
		this.ignorePoints.add(new Integer(8));
		this.ignorePoints.add(new Integer(9));
		
	}
	
	@Override
	protected Collection<Vector2d> getRelativePointPositions() {
		
		// paper shape with callout right-top
		double off = this.roundingRadius;
		Collection<Vector2d> points = new ArrayList<>();
		points.add(new Vector2d(0, 0)); // 0
		points.add(new Vector2d(1 - this.offsetRight, 0)); // 1
		points.add(new Vector2d(1 - this.offsetRight, this.calloutPos)); // callout, 2
		points.add(new Vector2d(1, this.calloutPos - this.calloutWidth)); // callout, 3
		points.add(new Vector2d(1 - this.offsetRight, this.calloutPos + this.calloutWidth)); // callout, 4
		points.add(new Vector2d(1 - this.offsetRight, 1)); // 5
		points.add(new Vector2d(off, 1)); // 6
		points.add(new Vector2d(0, 1 + off)); // 7
		points.add(new Vector2d(off, 1 + off)); // 8
		points.add(new Vector2d(off, 1)); // 9
		points.add(new Vector2d(0, 1 + off)); // 10
		points.add(new Vector2d(0, 0)); // 11
		return points;
		
	}
	
}
