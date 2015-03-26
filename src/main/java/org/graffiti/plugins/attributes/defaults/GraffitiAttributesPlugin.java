// ==============================================================================
//
// GraffitiAttributesPlugin.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: GraffitiAttributesPlugin.java,v 1.5 2010/12/22 13:05:58 klukas Exp $

package org.graffiti.plugins.attributes.defaults;

import org.graffiti.attributes.ArrowShapeAttribute;
import org.graffiti.attributes.BooleanAttribute;
import org.graffiti.attributes.ByteAttribute;
import org.graffiti.attributes.DoubleAttribute;
import org.graffiti.attributes.FloatAttribute;
import org.graffiti.attributes.HashMapAttribute;
import org.graffiti.attributes.IntegerAttribute;
import org.graffiti.attributes.LinkedHashMapAttribute;
import org.graffiti.attributes.LongAttribute;
import org.graffiti.attributes.ShortAttribute;
import org.graffiti.attributes.StringAttribute;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.plugin.GenericPluginAdapter;

/**
 * This class provides the default attribute types.
 */
public class GraffitiAttributesPlugin
					extends GenericPluginAdapter {
	// ~ Static fields/initializers =============================================
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new <code>GraffitiAttributesPlugin</code>.
	 */
	@SuppressWarnings("unchecked")
	public GraffitiAttributesPlugin() {
		super();
		this.attributes = new Class[] {
		BooleanAttribute.class,
		ByteAttribute.class,
		DoubleAttribute.class,
		FloatAttribute.class,
		HashMapAttribute.class,
		IntegerAttribute.class,
		LongAttribute.class,
		ShortAttribute.class,
		StringAttribute.class,
		LinkedHashMapAttribute.class
		};
		
		StringAttribute.putAttributeType("reversible", BooleanAttribute.class);
		StringAttribute.putAttributeType(GraphicAttributeConstants.ARROWHEAD, ArrowShapeAttribute.class);
		StringAttribute.putAttributeType(GraphicAttributeConstants.ARROWTAIL, ArrowShapeAttribute.class);
		
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
