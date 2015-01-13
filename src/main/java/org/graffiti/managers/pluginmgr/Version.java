// ==============================================================================
//
// Version.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: Version.java,v 1.5 2010/12/22 13:05:33 klukas Exp $

package org.graffiti.managers.pluginmgr;

/**
 * Represents an object, which contains a version number.
 * 
 * @author flierl
 * @version $Revision: 1.5 $
 */
public class Version
					implements Comparable<Object> {
	// ~ Instance fields ========================================================
	
	/** The major version of the plugin. */
	String versionMajor;
	
	/** The minor version of the plugin. */
	String versionMinor;
	
	/** The release version of the plugin. */
	String versionRelease;
	
	// ~ Methods ================================================================
	
	/**
	 * @see java.lang.Comparable#compareTo(Object)
	 */
	public int compareTo(Object o) {
		return 0;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
