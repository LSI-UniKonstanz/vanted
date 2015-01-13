/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * $Id$
 * Created on 25.10.2003 by Burkhard Sell
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.xgmml;

import org.graffiti.plugin.GenericPluginAdapter;
import org.graffiti.plugin.io.InputSerializer;

/**
 * Plugin to provide a reader for praphs stored in XGMML format
 * 
 * @see org.graffiti.plugin.GenericPluginAdapter
 * @author <a href="mailto:sell@nesoft.de">Burkhard Sell</a>
 * @version $Revision$
 */
public class XGMMLReaderPlugin extends GenericPluginAdapter {
	
	/**
	 * Creates a new XGMMLReaderPlugin instance.
	 */
	public XGMMLReaderPlugin() {
		inputSerializers = new InputSerializer[1];
		inputSerializers[0] = new XGMMLReader();
	}
}
