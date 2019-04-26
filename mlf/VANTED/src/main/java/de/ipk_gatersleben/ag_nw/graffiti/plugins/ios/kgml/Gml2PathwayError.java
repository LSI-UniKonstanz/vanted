/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 13.04.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml;

public enum Gml2PathwayError {
	PATHWAY_ID_MISSING, PATHWAY_ORG_MISSING, MAP_NUMBER_MISSING, KEGG_ID_MISSING, KEGG_TYPE_MISSING, KEGG_TYPE_INVALID, NEGATIVE_X_VALUE, NEGATIVE_Y_VALUE, INVALID_GRAPHICS_TYPE, INVALID_FOREGROUNDCOLOR, INVALID_BACKGROUNDCOLOR, INCONSISTENT_NUMBER_OF_REACTIONNAMES_AND_TYPES, KEGG_REFERENCED_ID_MISSING, KEGG_REFERENCED_TYPE_MISSING, KEGG_REFERENCED_TYPE_INVALID, REACTION_TYPE_MISSING, REACTION_TYPE_INVALID, REACTION_TYPE_NOTSET, REACTION_TYPE_DIFFERS_AMONG_NODES, ENTRY_FOR_SUBSTRATE_NOT_FOUND, ENTRY_FOR_PRODUCT_NOT_FOUND, INTERNAL_RELATION_SRC_OR_TGT_NOT_FOUND, RELATION_SRC_OR_TGT_MISSING, INVALID_RELATION_TYPE, ENTRY_FOR_RELATION_NOT_FOUND, REACTION_ID_INVALID, REACTION_PRODUCT_INVALID, REACTION_SUBSTRATE_INVALID, REACTION_PRODUCT_INVALID_MORE_THAN_ONE_ENTRY_FITS, REACTION_SUBSTRATE_INVALID_MORE_THAN_ONE_ENTRY_FITS;
}
