package de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg_ko;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class BriteHierarchy {
	
	/**
	 * Brite Hierarchy Name
	 */
	String name;
	
	/**
	 * the definition of this hierarchy
	 */
	String definition;
	/**
	 * The Brite ID
	 */
	String id;
	
	/**
	 * Root node of the hierarchy
	 */
	BriteEntry root;
	
	/**
	 * All leaf nodes (actuall entries we want to look up, when
	 * we want to find the hierarchy this entry is in
	 * Is is then possible to go up through the hierarchy to the root
	 * node, by recursively getting the parent BriteEntry
	 * 
	 * A set is returned, since an ID can have multiple occasions as leaf node
	 */
	Map<String, HashSet<BriteEntry>> mapEntries;

	
	
	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	
	public String getDefinition() {
		return definition;
	}

	public BriteEntry getRoot() {
		return root;
	}

	public Map<String, HashSet<BriteEntry>> getEntriesMap() {
		if(mapEntries == null)
			mapEntries = new HashMap<>();
		return mapEntries;
	}

	
	protected void setMapEntries(Map<String, HashSet<BriteEntry>> mapEntries) {
		this.mapEntries = mapEntries;
	}

	protected void setName(String name) {
		this.name = name;
	}

	protected void setDefinition(String definition) {
		this.definition = definition;
	}

	protected void setId(String id) {
		this.id = id;
	}

	protected void setRoot(BriteEntry root) {
		this.root = root;
	}
	
	
	
	
	
}
