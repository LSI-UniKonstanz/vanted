/**
 * Copyright (c) 2016 Monash University, Australia
 */

// This class provides a stadium shape
// It can be used in particular for SBGN PD maps for the simple chemical glyph
package de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes;

import org.graffiti.plugins.views.defaults.RectangleNodeShape;

/**
 * @author Tobias Czauderna
 */
public class SBGNStadiumShape extends RectangleNodeShape {
	
	@Override
	protected double getRounding() {
		
		return this.nodeAttr.getDimension().getHeight();
		
	}
	
}
