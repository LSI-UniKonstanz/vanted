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
public class PaperShapeBottomLeft extends PaperShape {
	
	double calloutPosX = 0.25; // relative position from bottom-left corner
	
	public PaperShapeBottomLeft() {
		
		this.ignorePoints = new HashSet<>();
		this.ignorePoints.add(new Integer(3));
		this.ignorePoints.add(new Integer(4));
		
	}
	
	@Override
	protected Collection<Vector2d> getRelativePointPositions() {
		
		// paper shape with callout bottom-left
		double off = this.roundingRadius;
		Collection<Vector2d> points = new ArrayList<>();
		points.add(new Vector2d(0, 0)); // 0
		points.add(new Vector2d(1 - off, 0)); // 1
		points.add(new Vector2d(1, -off)); // 2
		points.add(new Vector2d(1 - off, -off)); // 3
		points.add(new Vector2d(1 - off, 0)); // 4
		points.add(new Vector2d(1, -off)); // 5
		points.add(new Vector2d(1, 1 - this.offsetBottom)); // 6
		points.add(new Vector2d(this.calloutPosX, 1 - this.offsetBottom)); // callout, 7
		points.add(new Vector2d(this.calloutPosX - 2 * this.calloutWidth, 1)); // callout,8
		points.add(new Vector2d(this.calloutPosX - this.calloutWidth, 1 - this.offsetBottom)); // callout, 9
		points.add(new Vector2d(0, 1 - this.offsetBottom)); // 10
		points.add(new Vector2d(0, 0)); // 11
		return points;
		
	}
	
}
