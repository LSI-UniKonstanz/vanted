// ==============================================================================
//
// GraphMLException.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: GraphMLException.java,v 1.5 2011/01/16 16:39:47 klukas Exp $

package org.graffiti.plugins.ios.importers.graphml;

import java.io.IOException;

/**
 * This exception is thrown when errors occur during parsing.
 *
 * @author ruediger
 */
public class GraphMLException extends IOException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3402057563881951712L;
	
	// ~ Constructors ===========================================================
	/**
	 * Constructs a new <code>GraphMLException</code> from a given
	 * <code>Throwable</code>.
	 *
	 * @param cause
	 *           the <code>Throwable</code> that caused the exception to be thrown.
	 */
	public GraphMLException(Throwable cause) {
		super(cause.getMessage());
		this.initCause(cause);
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
