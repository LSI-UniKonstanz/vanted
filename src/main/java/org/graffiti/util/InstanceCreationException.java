// ==============================================================================
//
// InstanceCreationException.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: InstanceCreationException.java,v 1.5 2010/12/22 13:05:33 klukas Exp $

package org.graffiti.util;

/**
 * An exception, which is thrown, iff the instantiation of a given class fails.
 * 
 * @version $Revision: 1.5 $
 */
public class InstanceCreationException extends Exception {
	// ~ Constructors ===========================================================

	/**
	 * 
	 */
	private static final long serialVersionUID = 5052589819208279218L;

	/**
	 * Creates a new InstanceCreationException object.
	 * 
	 * @param msg
	 *            DOCUMENT ME!
	 */
	public InstanceCreationException(String msg) {
		super(msg);
	}

	/**
	 * Creates a new InstanceCreationException object.
	 * 
	 * @param ex
	 *            DOCUMENT ME!
	 */
	public InstanceCreationException(Exception ex) {
		super(ex);
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
