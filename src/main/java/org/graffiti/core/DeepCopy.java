// ==============================================================================
//
// DeepCopy.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: DeepCopy.java,v 1.4 2010/12/22 13:05:33 klukas Exp $

package org.graffiti.core;

/**
 * Guarantees a deep copy.
 * 
 * @version $Revision: 1.4 $
 */
public interface DeepCopy {
	// ~ Methods ================================================================
	
	/**
	 * Returns a deep copy of this object.
	 * 
	 * @return A deep copy of this object.
	 */
	public Object copy();
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
