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
public class PaperShapeLeftTop extends PaperShape {

	double calloutPosY = 0.25; // relative position from left-top corner

	public PaperShapeLeftTop() {

		this.ignorePoints = new HashSet<>();
		this.ignorePoints.add(Integer.valueOf(3));
		this.ignorePoints.add(Integer.valueOf(4));

	}

	@Override
	protected Collection<Vector2d> getRelativePointPositions() {

		// paper shape with callout left-top
		double off = this.roundingRadius;
		Collection<Vector2d> points = new ArrayList<>();
		points.add(new Vector2d(this.offsetLeft, 0)); // 0
		points.add(new Vector2d(1, 0)); // 1
		points.add(new Vector2d(1, 1 + off)); // 2
		points.add(new Vector2d(1 - off, 1)); // 3
		points.add(new Vector2d(1 - off, 1 + off)); // 4
		points.add(new Vector2d(1, 1 + off)); // 5
		points.add(new Vector2d(1 - off, 1)); // 6
		points.add(new Vector2d(this.offsetLeft, 1)); // 7
		points.add(new Vector2d(this.offsetLeft, this.calloutPosY)); // callout, 8
		points.add(new Vector2d(0, this.calloutPosY - 2 * this.calloutWidth)); // callout, 9
		points.add(new Vector2d(this.offsetLeft, this.calloutPosY - this.calloutWidth)); // callout, 10
		points.add(new Vector2d(this.offsetLeft, 0)); // 11
		return points;

	}

}
