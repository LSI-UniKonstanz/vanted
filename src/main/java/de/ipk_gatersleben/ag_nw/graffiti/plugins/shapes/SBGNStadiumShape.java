/**
 * Copyright (c) 2016 Monash University, Australia
 */

// This class provides a stadium shape
// It can be used in particular for SBGN PD maps for the simple chemical glyph
package de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes;

import org.graffiti.graphics.NodeGraphicAttribute;
import org.graffiti.plugins.views.defaults.RectangleNodeShape;

/**
 * @author Tobias Czauderna
 */
public class SBGNStadiumShape extends RectangleNodeShape {

	@Override
	public void buildShape(NodeGraphicAttribute graphics) {

		// correct width if necessary, width should be >= height
		if (graphics.getDimension().getHeight() > graphics.getDimension().getWidth())
			graphics.getDimension().setWidth(graphics.getDimension().getHeight());

		super.buildShape(graphics);

	}

	@Override
	protected double getRounding() {

		return this.nodeAttr.getDimension().getHeight();

	}

}
