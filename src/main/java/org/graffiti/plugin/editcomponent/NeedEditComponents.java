// ==============================================================================
//
// NeedEditComponents.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: NeedEditComponents.java,v 1.5 2010/12/22 13:05:54 klukas Exp $

package org.graffiti.plugin.editcomponent;

import java.util.Map;

import org.graffiti.plugin.Displayable;

/**
 *
 */
public interface NeedEditComponents {
	// ~ Methods ================================================================
	
	/**
	 * Set the map that connects attributes and parameters with editcomponents.
	 * 
	 * @param ecMap
	 */
	public void setEditComponentMap(Map<Class<? extends Displayable>, Class<? extends ValueEditComponent>> ecMap);
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
