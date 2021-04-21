package de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

import javax.swing.JComponent;

import org.HelperClass;
import org.apache.log4j.Logger;
import org.vanted.updater.HttpHttpsURL;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.FileDownloadStatusInformationProvider;

/**
 * This Kegg API Service helper will retrieve and cache results looked up from
 * the Kegg public REST webservice. The cache will be implemented using the
 * MapDB database library.
 * The service helper will return KoEntry objects (single or Lists) given on of
 * the following commonly used inputs - KO Ids - EC number - Gene ID
 * 
 * @author matthiak
 */
public class KeggAPIServiceHelper implements HelperClass, FileDownloadStatusInformationProvider {
	
	static Logger logger = Logger.getLogger(KeggAPIServiceHelper.class);
	
	static String URL_REST_GET_DB_ENTRY = "http://rest.kegg.jp/get/";
	// translation URL to get KO entries from other IDs
	static String URL_REST_LINK_ID = "http://rest.kegg.jp/link/ko/";
	
	static int MAX_QUERY_SIZE = 10;
	
	private static final Object lock = new Object();
	static KeggAPIServiceHelper instance;
	
	/**
	 * States for the state machine that reads the KO document
	 * 
	 * @author matthiak
	 */
	enum KoEntryKey {
		ENTRY("ENTRY"), NAME("NAME"), DEFINITION("DEFINITION"), PATHWAY("PATHWAY"), BRITE("BRITE"), DBLINKS("DBLINKS"), REFERENCE("REFERENCE"), GENES("GENES");
		
		String name;
		
		KoEntryKey(String name) {
			this.name = name;
		}
		
		public String toString() {
			return name;
		}
	}
	
	Map<String, KoEntry> mapKOIdToEntry;
	Map<String, HashSet<KoEntry>> mapECIdToEntry;
	
	private KeggAPIServiceHelper() {
		instance = this;
		mapKOIdToEntry = new HashMap<String, KoEntry>();
		mapECIdToEntry = new HashMap<String, HashSet<KoEntry>>();
	}
	
	public static KeggAPIServiceHelper getInstance() {
		synchronized (lock) {
			if (instance == null)
				instance = new KeggAPIServiceHelper();
			return instance;
		}
	}
	
	/**
	 * Retrieves an array of KO entries for a given EC number
	 * 
	 * @param ecId
	 * @return
	 */
	public List<KoEntry> getEntriesByEC(String ecId) {
		List<String> listEC = new ArrayList<String>();
		listEC.add(ecId);
		return getEntriesByEC(listEC);
	}
	
	/**
	 * Algorithm to retrieve KO entries from EC numbers
	 * 2 step process 1. find KO id to EC number in cache OR 1. if not in cache find
	 * KO entry online 2. download KO entry -create KoEntry object and put it in the
	 * cache
	 * returns List of koEntry objects matching the given EC numbers
	 */
	public List<KoEntry> getEntriesByEC(List<String> listEC) {
		List<KoEntry> retListKoEntries = new ArrayList<KoEntry>();
		
		List<String> listIDsToTranslateFromKegg = new ArrayList<>();
		
		// try the cache first
		for (String ec : listEC) {
			Set<KoEntry> koEntry = mapECIdToEntry.get(ec);
			if (koEntry != null)
				retListKoEntries.addAll(koEntry);
			else {
				listIDsToTranslateFromKegg.add("ec:" + ec); // prepend 'ec:' prefix for the lookup
			}
			
		}
		
		if (!listIDsToTranslateFromKegg.isEmpty()) {
			ForkJoinPool pool = new ForkJoinPool(2);
			
			pool.invoke(new RetrieveLinkMappingFromIDtoKO(
					listIDsToTranslateFromKegg.toArray(new String[listIDsToTranslateFromKegg.size()]), 0,
					listIDsToTranslateFromKegg.size()));
			
			/*
			 * after the linking from ecID to KO and retrieving of of KO, do the lookup for
			 * the previously not found ECs
			 */
			for (String ectranslateId : listIDsToTranslateFromKegg) {
				String ec = ectranslateId.substring(3); // exclude the prefix 'ec:' put there before the translation
				Set<KoEntry> koEntry = mapECIdToEntry.get(ec);
				if (koEntry != null)
					retListKoEntries.addAll(koEntry);
			}
		}
		return retListKoEntries;
	}
	
	/**
	 * Retrieves an array of KO entries for a given KO id
	 * 
	 * @param KO
	 *           id (K number)
	 * @return
	 */
	public List<KoEntry> getEntriesByKO(String koId) {
		List<String> listKOids = new ArrayList<String>();
		listKOids.add(koId);
		return getEntriesByKO(listKOids);
	}
	
	/**
	 * returns List of koEntry objects matching the given KO ids (K numbers)
	 */
	public List<KoEntry> getEntriesByKO(List<String> listKOids) {
		List<KoEntry> retListKoEntries = new ArrayList<KoEntry>();
		
		List<String> listKOsToLoadFromKegg = new ArrayList<>();
		
		// try the cache first
		for (String koid : listKOids) {
			KoEntry koEntry = mapKOIdToEntry.get(koid);
			if (koEntry != null)
				retListKoEntries.add(koEntry);
			else
				listKOsToLoadFromKegg.add(koid);
			
		}
		
		// load all KOids not found in cache from web
		if (!listKOsToLoadFromKegg.isEmpty()) {
			List<KoEntry> listLoadedKoEntries = new ArrayList<>();
			List<KoEntry> synchronizedList = Collections.synchronizedList(listLoadedKoEntries);
			
			RetrieveKObyKOId task = new RetrieveKObyKOId(listKOids.toArray(new String[listKOids.size()]), 0,
					listKOids.size(), synchronizedList);
			
			ForkJoinPool pool = new ForkJoinPool(2);
			
			pool.invoke(task);
			// add KOentries to KO map and, if there are EC ids found, EC map as well
			for (KoEntry entry : listLoadedKoEntries) {
				
				mapKOIdToEntry.put(entry.getKoID(), entry);
				
				HashSet<String> hashSet = entry.getDbLinkId2Values().get("EC");
				if (hashSet != null)
					for (String curEC : hashSet) {
						if (mapECIdToEntry.get(curEC) == null)
							mapECIdToEntry.put(curEC, new HashSet<KoEntry>());
						mapECIdToEntry.get(curEC).add(entry);
					}
			}
			// add the downloaded entries to the return list
			retListKoEntries.addAll(listLoadedKoEntries);
			
		}
		return retListKoEntries;
	}
	
	/**
	 * Retrieves results from KEGG REST service, using the link functionality see:
	 * http://www.kegg.jp/kegg/rest/keggapi.html [Linked entries]
	 * The result set will be KO entries. The source must be valid identifiers: *
	 * 
	 * @author matthiak
	 */
	class RetrieveLinkMappingFromIDtoKO extends RecursiveAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 4277483859034210243L;
		String[] listIDsToTranslateFromKegg;
		int leftIdx;
		int rightIdx;
		
		public RetrieveLinkMappingFromIDtoKO(String[] listIDsToTranslateFromKegg, int leftIdx, int rightIdx) {
			super();
			this.listIDsToTranslateFromKegg = listIDsToTranslateFromKegg;
			this.leftIdx = leftIdx;
			this.rightIdx = rightIdx;
		}
		
		@Override
		protected void compute() {
			
			if ((rightIdx - leftIdx) > MAX_QUERY_SIZE) {
				invokeAll(
						new RetrieveLinkMappingFromIDtoKO(listIDsToTranslateFromKegg, leftIdx,
								leftIdx + MAX_QUERY_SIZE),
						new RetrieveLinkMappingFromIDtoKO(listIDsToTranslateFromKegg, leftIdx + MAX_QUERY_SIZE,
								rightIdx));
			} else {
				getKOEntries();
			}
		}
		
		private void getKOEntries() {
			List<String> listKoIds = translate();
			if (listKoIds != null && !listKoIds.isEmpty()) {
				getEntriesByKO(listKoIds);
			}
		}
		
		/**
		 * translates given ids to KO ids using KEGG REST API
		 * 
		 * @return
		 */
		private List<String> translate() {
			List<String> retKOIds = new ArrayList<>();
			if (listIDsToTranslateFromKegg.length == 0)
				return retKOIds;
			
			StringBuilder str = new StringBuilder(URL_REST_LINK_ID);
			
			int idx = leftIdx;
			str.append(listIDsToTranslateFromKegg[idx]);
			idx++;
			while (idx < rightIdx) {
				str.append("+");
				str.append(listIDsToTranslateFromKegg[idx]);
				idx++;
			}
			
			try {
				HttpHttpsURL url = new HttpHttpsURL(str.toString());
				HttpURLConnection openConnection = url.openConnection();
				if (openConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
					BufferedReader reader = new BufferedReader(new InputStreamReader(openConnection.getInputStream()));
					String line;
					while ((line = reader.readLine()) != null) {
						if (line.isEmpty())
							continue;
						String[] mappedIds = line.split("\t");
						// if(mappedIds.length != 2)
						// System.err.println(); //good place for Breakpoint
						retKOIds.add(mappedIds[1].trim());
					}
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return retKOIds;
		}
	}
	
	/**
	 * A class that is called initially by the ForkJoin Pool.
	 * Since KEGG only allows 10 Ids per query, we need to be able to split up
	 * queries. The easiest parallel solution is to always take the first 10 pieces
	 * and recursively fork a new Thread with the rest N elements.
	 * 
	 * @author matthiak
	 */
	static class RetrieveKObyKOId extends RecursiveAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 4199328488427630261L;
		
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
			/*
			 * if the query size is larger than a constant (Kegg allows 10), split it up and
			 * recursivley call both with subarrays
			 */
			if (rightIdx - leftIdx > MAX_QUERY_SIZE) {
				invokeAll(new RetrieveKObyKOId(arrayKo, leftIdx, leftIdx + MAX_QUERY_SIZE, synchronizedList),
						new RetrieveKObyKOId(arrayKo, leftIdx + MAX_QUERY_SIZE, rightIdx, synchronizedList));
			} else {
				// small enough.. parse the result
				loadKO(synchronizedList);
			}
		}
		
		/**
		 * Loads the document from the Web using the KEGG REST service Then calls the
		 * actual parsing method
		 */
		protected void loadKO(List<KoEntry> synchronizedList) {
			StringBuffer buf = new StringBuffer();
			
			buf.append(URL_REST_GET_DB_ENTRY);
			
			int i = leftIdx;
			buf.append(arrayKo[i]);
			i++;
			while (i < rightIdx) {
				buf.append("+");
				buf.append(arrayKo[i]);
				i++;
			}
			
			/*
			 * read the result, create a buffer and call a method to handle the result.
			 */
			try {
				HttpHttpsURL url = new HttpHttpsURL(buf.toString());
				HttpURLConnection conn = url.openConnection();
				if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
					InputStream is = conn.getInputStream();
					byte[] buffer = new byte[1000];
					int len;
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					while ((len = is.read(buffer)) != -1)
						bos.write(buffer, 0, len);
					
					splitResult(bos.toByteArray(), synchronizedList);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		/**
		 * Kegg REST KO Document parser.
		 * Works like a state machine.
		 * Adds the KO Entries to the synchronous list
		 * 
		 * @param resultStreamArray
		 * @throws IOException
		 */
		protected void splitResult(byte[] resultStreamArray, List<KoEntry> synchronizedList) throws IOException {
			BufferedReader bufRead = new BufferedReader(
					new InputStreamReader(new ByteArrayInputStream(resultStreamArray)));
			
			KoEntry curKoEntry = null;
			
			String line;
			KoEntryKey curKey = null;
			
			while ((line = bufRead.readLine()) != null) {
				
				/*
				 * change the state, if a Tag at the beginning of he line was found
				 */
				if (line.startsWith(KoEntryKey.ENTRY.name())) {
					curKey = KoEntryKey.ENTRY;
					curKoEntry = new KoEntry();
					synchronizedList.add(curKoEntry);
				} else if (line.startsWith(KoEntryKey.NAME.name())) {
					curKey = KoEntryKey.NAME;
				} else if (line.startsWith(KoEntryKey.DEFINITION.name())) {
					curKey = KoEntryKey.DEFINITION;
				} else if (line.startsWith(KoEntryKey.PATHWAY.name())) {
					curKey = KoEntryKey.PATHWAY;
				} else if (line.startsWith(KoEntryKey.BRITE.name())) {
					curKey = KoEntryKey.BRITE;
				} else if (line.startsWith(KoEntryKey.DBLINKS.name())) {
					curKey = KoEntryKey.DBLINKS;
				} else if (line.startsWith(KoEntryKey.GENES.name())) {
					curKey = KoEntryKey.GENES;
				} else if (line.startsWith(KoEntryKey.REFERENCE.name())) {
					curKey = KoEntryKey.REFERENCE;
				} else if (line.equals("///")) // end of one KO entry
					continue;
				
				int idx = line.indexOf(" ");
				
				// remove the leading TAG (ENTRY, DEINIFITION, etc. )
				if (idx > 0)
					line = line.substring(idx);
				
				line = line.trim();
				
				/*
				 * the handle methods are called for the given state If the state doesn't
				 * change, methods are called for each line for the given Tag
				 */
				switch (curKey) {
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
		
		private static void handleReferences(KoEntry curKoEntry, String line) {
			// TODO Not yet implemented
			
		}
		
		private static void handleGenes(KoEntry curKoEntry, String line) {
			String[] KeyVals = line.split(":");
			if (KeyVals.length < 2) {
				// System.err.println();
				return;
			}
			String organism = KeyVals[0];
			String[] geneids = KeyVals[1].trim().split(" ");
			
			for (String geneid : geneids)
				curKoEntry.addGeneForSpecies(organism, geneid);
		}
		
		private static void handleDbLinks(KoEntry curKoEntry, String line) {
			String[] KeyVals = line.split(":");
			String dbName = KeyVals[0];
			String[] values = KeyVals[1].trim().split(" ");
			
			for (String value : values)
				curKoEntry.addDbLinkValue(dbName, value);
		}
		
		private static void handleBrite(KoEntry curKoEntry, String line) {
			int idx = line.indexOf("[BR:");
			if (idx >= 0) {
				String briteId = line.substring(idx + 4);
				briteId = briteId.substring(1, briteId.indexOf("]")).toLowerCase(); // skip opening and closing brackets
				BriteHierarchy briteHierarchy;
				try {
					briteHierarchy = KeggBriteService.getInstance().getBriteHierarchy(briteId);
					curKoEntry.addBriteEntry(briteHierarchy.getName(),
							briteHierarchy.getEntriesMap().get(curKoEntry.getKoID()));
				} catch (IOException e) {
					
					e.printStackTrace();
				}
			}
			
		}
		
		@SuppressWarnings("unused")
		private static void handlePathway(KoEntry curKoEntry, String line) {
			int idx = line.indexOf(" ");
			String pathid = line.substring(0, idx);
			String pathname = line.substring(idx + 1, line.length());
			
			/*
			 * IMPlement path structure in KOEntry
			 */
		}
		
		private static void handleDefinition(KoEntry curKoEntry, String line) {
			if (line.indexOf("[EC:") >= 0) {
				String ec = line.substring(line.indexOf("[EC:") + "[EC:".length());
				if (ec.indexOf("]") > 0) {
					ec = ec.substring(0, ec.indexOf("]"));
					curKoEntry.addDbLinkValue("EC", ec);
				}
			}
		}
		
		private static void handleName(KoEntry curKoEntry, String line) {
			// String name = line.substring(0, line.indexOf(" "));
			curKoEntry.setKoname(line);
		}
		
		private static void handleEntry(KoEntry curKoEntry, String line) {
			String id = line.substring(0, line.indexOf(" "));
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
		return null;
	}
	
	@Override
	public String getDescription() {
		return null;
	}
	
	@Override
	public void finishedNewDownload() {
	}
	
}
