package edu.monash.vanted.test.kegg;

import java.util.ArrayList;
import java.util.List;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg_ko.KeggAPIService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg_ko.KoEntry;

public class KoDBReaderTest {

	
	public static void main(String[] args) {
		
		List<String> listKOs = new ArrayList<>();
		listKOs.add("K00001");
		listKOs.add("K00002");
		listKOs.add("K00003");
		listKOs.add("K00004");
		listKOs.add("K00005");
		listKOs.add("K00006");
		listKOs.add("K00007");
		listKOs.add("K00008");
		listKOs.add("K00009");
		listKOs.add("K00010");
		listKOs.add("K00011");
		listKOs.add("K00121");
//		List<KoEntry> entriesByKO = KeggAPIService.getInstance().getEntriesByKO(listKOs);
		
		List<String> listECs = new ArrayList<>();
		listECs.add("2.7.1.2");
		listECs.add("2.7.1.3");
		List<KoEntry> entriesKO = KeggAPIService.getInstance().getEntriesByEC(listECs);
		System.out.println();
	}
}
