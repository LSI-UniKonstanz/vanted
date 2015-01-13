// ==============================================================================
//
// Dependency.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: Dependency.java,v 1.4 2010/12/22 13:05:33 klukas Exp $

package org.graffiti.managers.pluginmgr;

/**
 * Interface for dependency of a plugin.
 * 
 * @version $Revision: 1.4 $
 */
public interface Dependency {
	// ~ Methods ================================================================
	
	/**
	 * Returns the main.
	 * 
	 * @return String
	 */
	public String getMain();
	
	// /**
	// * Returns <code>true</code>, if the dependency is satisfied.
	// */
	// public abstract boolean isSatisfied();
	
	/**
	 * Returns the name.
	 * 
	 * @return String
	 */
	public String getName();
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
