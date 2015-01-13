/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * $Id$
 * Created on 28.10.2003 by Burkhard Sell
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.xgmml;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * Resolver for the XGMML DTD.
 * Normally DTDs will be loaded via HTTP.
 * This Resolver provides functionality so that the XGMML DTD need not be
 * retrieved via HTTP.
 * This resolver will return the DTD from within the <tt>CLASSPATH</tt>.
 * 
 * @author <a href="mailto:sell@nesoft.de">Burkhard Sell</a>
 * @version $Revision$
 */

public class XGMMLDTDResolver implements EntityResolver {
	
	/**
	 * Resolves the entity.
	 * 
	 * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String, java.lang.String)
	 */
	public InputSource resolveEntity(String publicId, String systemId) {
		systemId = systemId.toLowerCase();
		if ((systemId.indexOf("xgmml.dtd") != -1) || publicId.equals(XGMMLConstants.PUBLIC_ID)) {
			return new InputSource(
								new BufferedReader(
													new InputStreamReader(getClass().getResourceAsStream("xgmml.dtd"))));
		}
		
		return null;
	}
}
