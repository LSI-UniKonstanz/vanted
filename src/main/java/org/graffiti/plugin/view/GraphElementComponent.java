// ==============================================================================
//
// GraphElementComponent.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: GraphElementComponent.java,v 1.7 2010/12/22 13:05:53 klukas Exp $

package org.graffiti.plugin.view;

import java.awt.Graphics;
import java.awt.geom.AffineTransform;

import javax.swing.JComponent;
import javax.swing.RepaintManager;

import org.graffiti.graphics.GraphicAttributeConstants;

/**
 * Class that shares common members for all GraphElementComponents.
 * 
 * @version $Revision: 1.7 $
 */
public abstract class GraphElementComponent
					extends JComponent
					implements GraffitiViewComponent, GraphicAttributeConstants,
					GraphElementComponentInterface {
	// ~ Methods ================================================================
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Returns whether the given coordinates lie within this component and
	 * within its encapsulated shape. The coordinates are assumed to be
	 * relative to the coordinate system of this component.
	 * 
	 * @see java.awt.Component#contains(int, int)
	 */
	@Override
	public boolean contains(int x, int y) {
		return super.contains(x, y);
	}
	
	/**
	 * Paints the graph element contained in this component.
	 * 
	 * @param g
	 *           the graphics context in which to paint.
	 * @see javax.swing.JComponent#paintComponent(Graphics)
	 */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
	}
	
    public void repaint(long tm, int x, int y, int width, int height) {
    	if(getParent() != null) {
    	AffineTransform zoom = ((AbstractView) getParent()).getZoom();
        RepaintManager.currentManager(this).addDirtyRegion(
        		(JComponent)getParent(), 
        		(int)(getX()*zoom.getScaleX()), 
        		(int)(getY()*zoom.getScaleY()),
        		(int)(width*zoom.getScaleX()), 
        		(int)(height*zoom.getScaleY())
        		);
    	} else
    		super.repaint(tm, x, y, width, height);
    }

}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
