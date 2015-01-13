// ==============================================================================
//
// GraphElement.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: GraphElement.java,v 1.5 2010/12/22 13:05:33 klukas Exp $

package org.graffiti.graph;

import org.graffiti.attributes.Attributable;

/**
 * Interfaces a graph element. A graph element knows the graph it belongs to
 * and can contain attributes.
 * 
 * @version $Revision: 1.5 $
 * @see Node
 * @see Edge
 */
public interface GraphElement
					extends Attributable, Comparable<GraphElement> {
	// ~ Methods ================================================================
	
	/**
	 * Returns the Graph the GraphElement belongs to.
	 * 
	 * @return the Graph the GraphElement belongs to.
	 */
	public Graph getGraph();
	
	public void setID(long id);
	
	public long getID();
	
	public int getViewID();
	
	public void setViewID(int id);
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
