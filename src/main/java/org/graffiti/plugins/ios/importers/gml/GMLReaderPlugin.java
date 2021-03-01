// ==============================================================================
//
// GMLReaderPlugin.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: GMLReaderPlugin.java,v 1.4 2011/01/03 12:35:17 klukas Exp $

package org.graffiti.plugins.ios.importers.gml;

import org.graffiti.plugin.GenericPluginAdapter;
import org.graffiti.plugin.io.InputSerializer;

/**
 * DOCUMENT ME!
 * 
 * @author $Author: klukas $
 * @version $Revision: 1.4 $ $Date: 2011/01/03 12:35:17 $
 */
public class GMLReaderPlugin extends GenericPluginAdapter {
	// ~ Constructors ===========================================================
	
	/**
	 * Creates a new GMLReaderPlugin object.
	 */
	public GMLReaderPlugin() {
		this.inputSerializers = new InputSerializer[1];
		this.inputSerializers[0] = new GMLReader();
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
