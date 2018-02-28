package edu.monash.vanted.test.kegg;

import java.io.IOException;
import java.util.HashSet;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg.BriteEntry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg.BriteHierarchy;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg.KeggBriteService;

public class BriteHierarchyReadingTest {

	public static void main(String[] args) {
		try {
			BriteHierarchy briteHierarchy = KeggBriteService.getInstance().getBriteHierarchy("ko00001");

			HashSet<BriteEntry> hashSet = briteHierarchy.getEntriesMap().get("K16197");
			HashSet<BriteEntry> hashSet2 = briteHierarchy.getECEntriesMap().get("2.3.3.1");
			System.out.println("chance for breakpoint");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
