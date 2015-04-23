package de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg_ko;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BriteEntry {
	
	
	protected BriteEntry parent;
	protected List<BriteEntry> children;
	
	String KOId;
	String name;
	Set<String> setEC;
	String pathId;
	

	public BriteEntry(BriteEntry parent, String kOId, String name, Set<String> setEC, String pathId) {
		super();
		this.parent = parent;
		KOId = kOId;
		this.name = name;
		this.setEC = setEC;
		this.pathId = pathId;
		
	}

	public BriteEntry getParent() {
		return parent;
	}

	public List<BriteEntry> getChildren() {
		return children;
	}


	public void addChild(BriteEntry entry){
		if(this.children == null)
			children = new ArrayList<>();

		this.children.add(entry);
	}

	public String getId() {
		return KOId;
	}

	public String getName() {
		return name;
	}

	public Set<String> getSetEC() {
		return setEC;
	}

	public String getPathId() {
		return pathId;
	}


	
	
	
}
