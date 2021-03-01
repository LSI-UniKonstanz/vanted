// ==============================================================================
//
// GraphElementsEdit.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: GraphElementsEdit.java,v 1.4 2010/12/22 13:05:35 klukas Exp $

package org.graffiti.undo;

import java.util.Map;

import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;

/**
 * <code>GraphElementsEdit</code> is abstract class for building edits belong to
 * the operations on graph elements like adding or removing.
 * 
 * @author $Author: klukas $
 * @version $Revision: 1.4 $ $Date: 2010/12/22 13:05:35 $
 */
public abstract class GraphElementsEdit extends GraffitiAbstractUndoableEdit {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7103160764993932741L;
	// ~ Instance fields ========================================================
	/** Necessary graph reference */
	protected Graph graph;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Create a nes <code>GraphElementsEdit</code>.
	 * 
	 * @param graph
	 *           a graph reference
	 * @param geMap
	 *           reference to the map supports the undo operations.
	 */
	public GraphElementsEdit(Graph graph, Map<GraphElement, GraphElement> geMap) {
		super(geMap);
		assert graph != null;
		this.graph = graph;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
