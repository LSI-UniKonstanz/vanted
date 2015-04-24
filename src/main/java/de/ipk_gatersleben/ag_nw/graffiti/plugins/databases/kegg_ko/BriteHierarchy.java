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

	/**
	 * This map stores entries, where a link to a EC number was given
	 * The key will be the EC number that retrieves a set of BriteEntries 
	 */
	Map<String, HashSet<BriteEntry>> mapEntriesByEC;
	
	
	
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

	/**
	 * Method to get all Brite Entries (Leaf nodes in the hierarchy) that match the
	 * given ID
	 * @param id 
	 * @return All brite entries matching the given id
	 */
	public HashSet<BriteEntry> getBriteEntryById(String id) {
		return getEntriesMap().get(id);
	}
	
	public void addBriteEntryToEntryMap(String id, BriteEntry entry) {
		HashSet<BriteEntry> set;
		if( (set =  getEntriesMap().get(id)) == null){
			set = new HashSet<BriteEntry>();
			getEntriesMap().put(id, set);
		}
		set.add(entry);
	}

	public Map<String, HashSet<BriteEntry>> getECEntriesMap() {
		if(mapEntriesByEC == null)
			mapEntriesByEC = new HashMap<>();
		return mapEntriesByEC;
	}

	/**
	 * Method to get all Brite Entries (Leaf nodes in the hierarchy) that match the
	 * given EC number 
	 * @param ec Number 
	 * @return All brite entries matching the given EC number
	 */
	public HashSet<BriteEntry> getBriteEntryByEC(String ec) {
		return getECEntriesMap().get(ec);
	}

	public void addBriteEntryToECEntryMap(String ecid, BriteEntry entry) {
		HashSet<BriteEntry> set;
		if( (set =  getECEntriesMap().get(ecid)) == null){
			set = new HashSet<BriteEntry>();
			getECEntriesMap().put(ecid, set);
		}
		set.add(entry);
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
