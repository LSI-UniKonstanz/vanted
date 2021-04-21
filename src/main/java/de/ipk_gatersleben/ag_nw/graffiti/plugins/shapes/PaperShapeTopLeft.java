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
public class PaperShapeTopLeft extends PaperShape {
	
	double calloutPos = 0.125; // relative position from top-left corner
	
	public PaperShapeTopLeft() {
		
		this.ignorePoints = new HashSet<>();
		this.ignorePoints.add(Integer.valueOf(6));
		this.ignorePoints.add(Integer.valueOf(7));
		
	}
	
	@Override
	protected Collection<Vector2d> getRelativePointPositions() {
		
		// paper shape with callout top-left
		double off = this.roundingRadius;
		Collection<Vector2d> points = new ArrayList<>();
		points.add(new Vector2d(0, this.offsetTop)); // 0
		points.add(new Vector2d(this.calloutPos, this.offsetTop)); // callout, 1
		points.add(new Vector2d(this.calloutPos - this.calloutWidth, 0)); // callout, 2
		points.add(new Vector2d(this.calloutPos + this.calloutWidth, this.offsetTop)); // callout, 3
		points.add(new Vector2d(1, this.offsetTop)); // 4
		points.add(new Vector2d(1, 1 + off)); // 5
		points.add(new Vector2d(1 - off, 1)); // 6
		points.add(new Vector2d(1 - off, 1 + off)); // 7
		points.add(new Vector2d(1, 1 + off)); // 8
		points.add(new Vector2d(1 - off, 1)); // 9
		points.add(new Vector2d(0, 1)); // 10
		points.add(new Vector2d(0, this.offsetTop)); // 11
		return points;
		
	}
	
}
