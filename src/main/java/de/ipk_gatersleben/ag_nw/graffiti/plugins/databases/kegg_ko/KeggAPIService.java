package de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg_ko;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

import javax.swing.JComponent;

import org.HelperClass;
import org.apache.log4j.Logger;
import org.w3c.www.http.HTTP;

import sun.net.www.http.HttpClient;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.FileDownloadStatusInformationProvider;

/**
 * This Kegg API Service wrapper will retrieve and cache results looked up from the Kegg public REST webservice.
 * The cache will be implemented using the MapDB database library.
 * 
 * The wrapper will return KoEntry objects (single or Lists) given on of the following inputs
 * - KO Ids
 * - EC number
 * - Gene ID
 * 
 * 
 * @author matthiak
 *
 */
public class KeggAPIService implements HelperClass, FileDownloadStatusInformationProvider{

	static Logger logger = Logger.getLogger(KeggAPIService.class);
	
	static String URL_REST_KO = "http://rest.kegg.jp/get/";
	static String URL_REST_EC = "http://rest.kegg.jp/link/ko/";
	
	static int MAX_QUERY_SIZE = 10;
	
	static KeggAPIService instance;

	enum KoEntryKey {
		ENTRY("ENTRY"),
		NAME("NAME"),
		DEFINITION("DEFINITION"),
		PATHWAY("PATHWAY"),
		BRITE("BRITE"),
		DBLINKS("DBLINKS"),
		REFERENCE("REFERENCE"),
		GENES("GENES");

		String name;
		KoEntryKey(String name) {
			this.name = name;
		}
		
		public String toString() {
			return name;
		}
	}
	
	HttpClient client;
	
	
	Map<String, KoEntry> mapKOIdToEntry;
	Map<String, KoEntry> mapECIdToEntry;
	
	
	private KeggAPIService() {
		instance = this;
	}
	
	public static KeggAPIService getInstance() {
		if(instance == null)
			instance = new KeggAPIService();
		return instance;
	}
	
	/*
	 * Algorithm to retrieve KO entries from EC numbers
	 * 
	 * 2 step process
	 * 1. find KO id to EC number in cache
	 * OR
	 * 1. if not in cache find KO entry online
	 * 2. download KO entry
	 * 	-create KoEntry object and put it in the cache
	 */
	
	/**
	 * returns List of koEntry objects matching the given EC numbers
	 */
	public List<KoEntry> getEntriesByEC(List<String> listEC) {
		List<KoEntry> listKoEntries = null;
		
		return listKoEntries;
	}
	
	
	/**
	 * returns List of koEntry objects matching the given KO ids
	 */
	public List<KoEntry> getEntriesByKO(List<String> listKOids) {
		List<KoEntry> listKoEntries = new ArrayList<KoEntry>();
	
		List<KoEntry> synchronizedList = Collections.synchronizedList(listKoEntries);
		
		RetrieveKObyKOId task = new RetrieveKObyKOId(
				listKOids.toArray(new String[listKOids.size()]),
				0,
				listKOids.size(),
				synchronizedList
				);
		
		ForkJoinPool pool = new ForkJoinPool();
		
		
		pool.invoke(task);
		
		return listKoEntries;
	}
	
	
	class RetrieveKObyKOId extends RecursiveAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		URL url;
		
		String[] arrayKo; 
		int leftIdx; 
		int rightIdx; 
		List<KoEntry> synchronizedList;
		
		public RetrieveKObyKOId(String[] arrayKo, int leftIdx, int rightIdx, List<KoEntry> synchronizedList) {
			super();
			this.arrayKo = arrayKo;
			this.leftIdx = leftIdx;
			this.rightIdx = rightIdx;
			this.synchronizedList = synchronizedList;
		}

		@Override
		protected void compute() {
			if(rightIdx - leftIdx > MAX_QUERY_SIZE) {
				invokeAll(	new RetrieveKObyKOId(arrayKo, leftIdx, leftIdx + MAX_QUERY_SIZE, synchronizedList),
								new RetrieveKObyKOId(arrayKo, leftIdx + MAX_QUERY_SIZE, rightIdx, synchronizedList));
			}
			else {
				loadKO();
			}
		}

		protected void loadKO() {
			StringBuffer buf = new StringBuffer();
			
			buf.append(URL_REST_KO);
			
			int i = leftIdx; 
			buf.append(arrayKo[i]);
			i++;
			while(i < rightIdx) { 
				buf.append("+");
				buf.append(arrayKo[i]);
				i++;
			}
			
			try {
				url = new URL(buf.toString());
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			/*
			 * read the result, create a buffer and call a method to handle the result.
			 */
			try {
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				if(conn.getResponseCode() == HTTP.OK) {
					InputStream is = conn.getInputStream();
					byte[] buffer = new byte[1000];
					int len;
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					while((len = is.read(buffer)) != -1)
						bos.write(buffer, 0, len);
					
					splitResult(bos.toByteArray());
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		protected void splitResult(byte[] resultStreamArray) throws IOException{
			BufferedReader bufRead = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(resultStreamArray)));
			
			KoEntry curKoEntry = null;
			
			String line;
			KoEntryKey curKey = null;
			
			
			while((line = bufRead.readLine()) != null) {

				if(line.startsWith(KoEntryKey.ENTRY.name())) {
					curKey = KoEntryKey.ENTRY;
					curKoEntry = new KoEntry();
					synchronizedList.add(curKoEntry);
				} else if(line.startsWith(KoEntryKey.NAME.name())) {
					curKey = KoEntryKey.NAME;
				} else if(line.startsWith(KoEntryKey.DEFINITION.name())) {
					curKey = KoEntryKey.DEFINITION;
				} else if(line.startsWith(KoEntryKey.PATHWAY.name())) {
					curKey = KoEntryKey.PATHWAY;
				} else if(line.startsWith(KoEntryKey.BRITE.name())) {
					curKey = KoEntryKey.BRITE;
				} else if(line.startsWith(KoEntryKey.DBLINKS.name())) {
					curKey = KoEntryKey.DBLINKS;
				} else if(line.startsWith(KoEntryKey.GENES.name())) {
					curKey = KoEntryKey.GENES;
				} else if(line.startsWith(KoEntryKey.REFERENCE.name())) {
					curKey = KoEntryKey.REFERENCE;
				} else if (line.equals("///")) // end of one KO entry
					continue;

				int idx = line.indexOf(" ");
				
				//remove the leading TAG (ENTRY, DEINIFITION, etc. )
				if(idx > 0)
					line = line.substring(idx);
				
				line = line.trim();
				
				switch(curKey) {
					case ENTRY:
						handleEntry(curKoEntry, line);
						break;
					case NAME:
						handleName(curKoEntry, line);
						break;
					case DEFINITION:
						handleDefinition(curKoEntry, line);
						break;
					case PATHWAY:
						handlePathway(curKoEntry, line);
						break;
					case BRITE:
						handleBrite(curKoEntry, line);
						break;
					case DBLINKS:
						handleDbLinks(curKoEntry, line);
						break;
					case GENES:
						handleGenes(curKoEntry, line);
						break;
					case REFERENCE:
						handleReferences(curKoEntry, line);
						break;
				}
				
				
			}
			
		}

		private void handleReferences(KoEntry curKoEntry, String line) {
			// TODO Auto-generated method stub
			
		}

		private void handleGenes(KoEntry curKoEntry, String line) {
			String[] KeyVals= line.split(":");
			if(KeyVals.length < 2) {
				System.err.println();
				return;
			}
			String organism = KeyVals[0];
			String[] geneids = KeyVals[1].split(" ");
			
			for(String geneid : geneids)
				curKoEntry.addGeneForSpecies(organism, geneid);
		}

		private void handleDbLinks(KoEntry curKoEntry, String line) {
			String[] KeyVals= line.split(":");
			String dbName = KeyVals[0];
			String[] values = KeyVals[1].split(" ");
			
			for(String value : values)
				curKoEntry.addDbLinkValue(dbName, value);
		}

		private void handleBrite(KoEntry curKoEntry, String line) {
			int idx = line.indexOf("[BR:");
			if (idx >= 0) {
				String briteId = line.substring(idx);
				briteId = briteId.substring(1, briteId.indexOf("]")).toLowerCase(); //skip opening and closing brackets
				BriteHierarchy briteHierarchy;
				try {
					briteHierarchy = KeggBriteService.getInstance().getBriteHierarchy(briteId);
					curKoEntry.addBriteEntry(briteHierarchy.getName(), briteHierarchy.getEntriesMap().get(curKoEntry.getKoID()));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}

		private void handlePathway(KoEntry curKoEntry, String line) {
			int idx = line.indexOf(" ");
			String pathid = line.substring(0, idx);
			String pathname = line.substring(idx + 1, line.length());
			
			/*
			 * IMPlement path structure in KOEntry
			 */
		}

		private void handleDefinition(KoEntry curKoEntry, String line) {
			if (line.indexOf("[EC:") >= 0) {
				String ec = line.substring(line.indexOf("[EC:") + "[EC:".length());
				if (ec.indexOf("]") > 0) {
					ec = ec.substring(0, ec.indexOf("]"));
					curKoEntry.addDbLinkValue("EC", ec);
				}
			}		}

		private void handleName(KoEntry curKoEntry, String line) {
//			String name = line.substring(0,  line.indexOf(" "));
			curKoEntry.setKoname(line);
		}

		private void handleEntry(KoEntry curKoEntry, String line) {
			String id = line.substring(0,  line.indexOf(" "));
			curKoEntry.setKoentryID(id);
		}

		
	}
	
	
	/*
	 * 
	 * 
	 * Implementation for FileDownloadStatusInformationProvider
	 * 
	 * 
	 */
	
	
	@Override
	public JComponent getStatusPane(boolean showEmpty) {
		// TODO Auto-generated method stub
		return null;
	}
 

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void finishedNewDownload() {
		// TODO Auto-generated method stub
		
	}

	
}
