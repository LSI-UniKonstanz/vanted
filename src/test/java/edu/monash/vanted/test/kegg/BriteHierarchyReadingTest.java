package edu.monash.vanted.test.kegg;

import java.io.IOException;
import java.util.HashSet;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg_ko.BriteEntry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg_ko.BriteHierarchy;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg_ko.KeggBriteService;

public class BriteHierarchyReadingTest {

	public static void main(String[] args) {
		try {
			BriteHierarchy briteHierarchy = KeggBriteService.getInstance().getBriteHierarchy("br:br08620");
			
			HashSet<BriteEntry> hashSet = briteHierarchy.getEntriesMap().get("NC_025244");
			System.out.println();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
