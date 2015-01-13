// ==============================================================================
//
// EdgeComponentInterface.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: EdgeComponentInterface.java,v 1.4 2010/12/22 13:05:53 klukas Exp $

package org.graffiti.plugin.view;

/**
 * This component represents a <code>org.graffiti.graph.Edge</code>.
 * 
 * @version $Revision: 1.4 $
 */
public interface EdgeComponentInterface
					extends GraphElementComponentInterface {
	// ~ Methods ================================================================
	
	/**
	 * Sets the source component.
	 * 
	 * @param snc
	 *           the source component to be set.
	 */
	public void setSourceComponent(NodeComponentInterface snc);
	
	/**
	 * Sets the source component.
	 * 
	 * @param tnc
	 *           the source component to be set.
	 */
	public void setTargetComponent(NodeComponentInterface tnc);
	
	/**
	 * Calls buildShape if no NodeShapes have changed.
	 */
	public void updateShape();
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
