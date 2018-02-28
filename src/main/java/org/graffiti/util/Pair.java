// ==============================================================================
//
// Pair.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: Pair.java,v 1.4 2010/12/22 13:05:33 klukas Exp $

package org.graffiti.util;

/**
 * Encapsulates two values.
 * 
 * @author Paul
 * @version $Revision: 1.4 $
 */
public class Pair<T, V> {
	// ~ Instance fields ========================================================

	/** DOCUMENT ME! */
	private T val1;

	/** DOCUMENT ME! */
	private V val2;

	// ~ Constructors ===========================================================

	/**
	 * Creates a new Pair object.
	 * 
	 * @param val1
	 *            DOCUMENT ME!
	 * @param val2
	 *            DOCUMENT ME!
	 */
	public Pair(T val1, V val2) {
		this.val1 = val1;
		this.val2 = val2;
	}

	// ~ Methods ================================================================

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public T getFst() {
		return val1;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public V getSnd() {
		return val2;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
