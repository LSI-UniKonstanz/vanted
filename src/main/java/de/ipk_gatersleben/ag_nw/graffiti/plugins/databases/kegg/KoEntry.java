/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 12.10.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Christian Klukas
 *         (c) 2006 IPK-Gatersleben
 */
public class KoEntry implements Comparable<KoEntry> {
	private String koentryID = "";
	private String koname = "";
	private String kodefinition = "";
	
	private HashMap<String, HashSet<String>> dbLinkId2Values = new HashMap<String, HashSet<String>>();
	private HashMap<String, HashSet<String>> dbOrganism2Genes = new HashMap<String, HashSet<String>>();
	
	/**
	 * mapping between BriteEntry name (KO, Module, Gene) to Brite Entry, which is the leaf node.
	 * To get the full hierarchy just go up by calling getParent() on each briteentry object
	 */
	private HashMap<String, Set<BriteEntry>>	mapBriteHierarchies = new HashMap<>();
	
	
	@Override
	public String toString() {
		return koentryID + " (" + koname + ")";
	}
	
	@Override
	public boolean equals(Object obj) {
		KoEntry objKo = (KoEntry) obj;
		return koentryID.equals(objKo.koentryID);
	}
	
	@Override
	public int hashCode() {
		return koentryID.hashCode();
	}
	

	
	public boolean isValid() {
		return koentryID != null && koname.length() > 0;
	}
	
	public String getKoID() {
		return koentryID;
	}
	
	public String getKoName() {
		return koname;
	}
	
	public String getKoDefinition() {
		return kodefinition;
	}
	

	
	public void setKoentryID(String koentryID) {
		this.koentryID = koentryID;
	}

	public void setKoname(String koname) {
		this.koname = koname;
	}

	public void setKodefinition(String kodefinition) {
		this.kodefinition = kodefinition;
	}

	public HashMap<String, HashSet<String>> getDbLinkId2Values() {
		return dbLinkId2Values;
	}

	public void addDbLinkValue(String dbKey, String linkvalue) {
		if( ! dbLinkId2Values.containsKey(dbKey))
			dbLinkId2Values.put(dbKey, new HashSet<String>());
		dbLinkId2Values.get(dbKey).add(linkvalue);
	}
	
	public void addGeneForSpecies(String speciesKey, String geneId) {
		if( ! dbOrganism2Genes.containsKey(speciesKey))
			dbOrganism2Genes.put(speciesKey, new HashSet<String>());
		dbOrganism2Genes.get(speciesKey).add(geneId);
	}
	
	public void addBriteEntry(String briteName,  Set<BriteEntry> entry) {
		mapBriteHierarchies.put(briteName, entry);
	}
	
	
	
	public Collection<String> getKoDbLinks(String dbLinkId) {
		if (!dbLinkId2Values.containsKey(dbLinkId))
			return new ArrayList<String>();
		else
			return dbLinkId2Values.get(dbLinkId);
	}
	
	public Collection<String> getKoDbLinks() {
		ArrayList<String> result = new ArrayList<String>();
		for (String db : dbLinkId2Values.keySet()) {
			result.addAll(dbLinkId2Values.get(db));
		}
		return result;
	}
	
	public HashSet<String> getGeneIDs(String orgCode) {
		orgCode = orgCode.toUpperCase();
		if (!dbOrganism2Genes.containsKey(orgCode)) {
			return new HashSet<String>();
		} else
			return dbOrganism2Genes.get(orgCode);
	}
	
	public Set<String> getOrganismCodes() {
		return dbOrganism2Genes.keySet();
	}
	
	public boolean hasGeneMapping(String orgCode, String gene) {
		// orgCode = orgCode.toUpperCase();
		HashSet<String> hs = dbOrganism2Genes.get(orgCode);
		if (hs == null || hs.size() <= 0) {
			return false;
		} else
			return hs.contains(gene);
	}
	
	public int compareTo(KoEntry o) {
		return koentryID.compareTo(o.koentryID);
	}
	
	public Set<String> getOrganismCodesForGeneID(String altId) {
		HashSet<String> result = new HashSet<String>();
		for (String o : getOrganismCodes()) {
			if (hasGeneMapping(o, altId))
				result.add(o);
		}
		return result;
	}
}
