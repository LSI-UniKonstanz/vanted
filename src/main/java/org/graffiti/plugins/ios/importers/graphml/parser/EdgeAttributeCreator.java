//==============================================================================
//
//   EdgeAttributeCreator.java
//
//   Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
//==============================================================================
// $Id: EdgeAttributeCreator.java,v 1.7 2011/01/16 16:39:59 klukas Exp $

package org.graffiti.plugins.ios.importers.graphml.parser;

import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.graphics.EdgeGraphicAttribute;

/**
 * Class <code>EdgeAttributeCreator</code> is used for reading
 * <code>Edge</code> attributes.
 *
 * @author ruediger
 */
class EdgeAttributeCreator
    extends AttributeCreator
{
    //~ Constructors ===========================================================

    /**
     * Constructs a new <code>EdgeAttributeCreator</code>.
     */
    EdgeAttributeCreator()
    {
        super();
    }

    //~ Methods ================================================================

    /*
     *
     */
    @Override
	CollectionAttribute createDefaultAttribute()
    {
    	EdgeGraphicAttribute result = new EdgeGraphicAttribute();
    	result.setArrowhead("");
    	result.setArrowtail("");
        return result;
    }
}

//------------------------------------------------------------------------------
//   end of file
//------------------------------------------------------------------------------
