// ==============================================================================
//
// OutputSerializer.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: OutputSerializer.java,v 1.6 2010/12/22 13:05:34 klukas Exp $

package org.graffiti.plugin.io;

import java.io.IOException;
import java.io.OutputStream;

import org.graffiti.graph.Graph;

/**
 * Interfaces a serializer, which is able to write a given graph in a special
 * format to a given output stream.
 * 
 * @version $Revision: 1.6 $
 */
public interface OutputSerializer
					extends Serializer {
	// ~ Methods ================================================================
	
	/**
	 * Writes the contents of the given graph to a stream.
	 * 
	 * @param stream
	 *           The output stream to save the graph to.
	 * @param g
	 *           The graph to save.
	 */
	public void write(OutputStream stream, Graph g)
						throws IOException;
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
