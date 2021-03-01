// ==============================================================================
//
// NodePosition.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: NodePosition.java,v 1.4 2010/12/22 13:05:54 klukas Exp $

package org.graffiti.plugin.algorithm;

import org.graffiti.graph.Node;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision: 1.4 $
 */
public class NodePosition {
	// ~ Instance fields ========================================================
	
	/** DOCUMENT ME! */
	Node n;
	
	/** DOCUMENT ME! */
	double x;
	
	/** DOCUMENT ME! */
	double y;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Creates a new NodePosition object.
	 * 
	 * @param n
	 *           DOCUMENT ME!
	 * @param x
	 *           DOCUMENT ME!
	 * @param y
	 *           DOCUMENT ME!
	 */
	NodePosition(Node n, double x, double y) {
		this.n = n;
		this.x = x;
		this.y = y;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
