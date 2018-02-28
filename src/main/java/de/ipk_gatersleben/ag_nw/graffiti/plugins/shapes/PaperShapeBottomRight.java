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
public class PaperShapeBottomRight extends PaperShape {

	double calloutPosX = 0.875; // relative position from bottom-left corner

	public PaperShapeBottomRight() {

		this.ignorePoints = new HashSet<>();
		this.ignorePoints.add(Integer.valueOf(1));
		this.ignorePoints.add(Integer.valueOf(2));

	}

	@Override
	protected Collection<Vector2d> getRelativePointPositions() {

		// paper shape with callout bottom-right
		double off = this.roundingRadius;
		Collection<Vector2d> points = new ArrayList<>();
		points.add(new Vector2d(0, -off)); // 0
		points.add(new Vector2d(off, 0)); // 1
		points.add(new Vector2d(off, -off)); // 2
		points.add(new Vector2d(0, -off)); // 3
		points.add(new Vector2d(off, 0)); // 4
		points.add(new Vector2d(1, 0)); // 5
		points.add(new Vector2d(1, 1 - this.offsetBottom)); // 6
		points.add(new Vector2d(this.calloutPosX, 1 - this.offsetBottom)); // callout, 7
		points.add(new Vector2d(this.calloutPosX + this.calloutWidth, 1)); // callout, 8
		points.add(new Vector2d(this.calloutPosX - this.calloutWidth, 1 - this.offsetBottom)); // callout, 9
		points.add(new Vector2d(0, 1 - this.offsetBottom)); // 10
		points.add(new Vector2d(0, -off)); // 11
		return points;

	}

}
