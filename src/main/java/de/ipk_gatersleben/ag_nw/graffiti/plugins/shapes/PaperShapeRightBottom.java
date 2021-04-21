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
public class PaperShapeRightBottom extends PaperShape {
	
	double calloutPosY = 0.75; // relative position from right-top corner
	
	public PaperShapeRightBottom() {
		
		this.ignorePoints = new HashSet<>();
		this.ignorePoints.add(Integer.valueOf(1));
		this.ignorePoints.add(Integer.valueOf(2));
		
	}
	
	@Override
	protected Collection<Vector2d> getRelativePointPositions() {
		
		// paper shape with callout right-bottom
		double off = this.roundingRadius;
		Collection<Vector2d> points = new ArrayList<>();
		points.add(new Vector2d(0, -off)); // 0
		points.add(new Vector2d(off, 0)); // 1
		points.add(new Vector2d(off, -off)); // 2
		points.add(new Vector2d(0, -off)); // 3
		points.add(new Vector2d(off, 0)); // 4
		points.add(new Vector2d(1 - this.offsetRight, 0)); // 5
		points.add(new Vector2d(1 - this.offsetRight, this.calloutPosY)); // callout, 6
		points.add(new Vector2d(1, this.calloutPosY + 2 * this.calloutWidth)); // callout, 7
		points.add(new Vector2d(1 - this.offsetRight, this.calloutPosY + this.calloutWidth)); // callout, 8
		points.add(new Vector2d(1 - this.offsetRight, 1)); // 9
		points.add(new Vector2d(0, 1)); // 10
		points.add(new Vector2d(0, -off)); // 11
		return points;
		
	}
	
}
