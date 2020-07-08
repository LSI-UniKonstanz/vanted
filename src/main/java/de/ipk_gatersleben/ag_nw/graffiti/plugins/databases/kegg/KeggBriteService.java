package de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ReleaseInfo;
import org.apache.log4j.Logger;
import org.vanted.updater.HttpHttpsURL;

/**
 * Service class providing hierarchy information using the BRITE database from
 * KEGG
 * 
 * If a brite hierarchy is not present in the cache, it will be loaded and
 * stored 1. the brite hierarchy file from the kegg service is downloaded and
 * stored in cache if not present 2. the hierarchy is created if not present
 * 
 */
public class KeggBriteService {

	static Logger logger = Logger.getLogger(KeggBriteService.class);

	static String KEGG_CACHE_DIR = "keggcache";

	static String URL_REST_BRITE = "http://rest.kegg.jp/get/br:";

	static KeggBriteService instance;

	/**
	 * map between brite ids and hierarchies ids be like br:ko00001 or br:ko01003
	 * 
	 * the BriteHierarchy object has a map to the entries (like K01624 or K00001)
	 */
	Map<String, BriteHierarchy> mapBriteHierarchies;

	private KeggBriteService() {
		instance = this;
		mapBriteHierarchies = new HashMap<String, BriteHierarchy>();
	}

	public static KeggBriteService getInstance() {
		if (instance == null)
			instance = new KeggBriteService();
		return instance;
	}

	/**
	 * Returns a map between brite ids and hierarchies
	 * 
	 * @param briteId
	 *            e.g. br:ko00001 or br:ko01003
	 * @return The BriteHierarchy object which contains the map to the entries (like
	 *         K01624 or K00001)
	 */
	public BriteHierarchy getBriteHierarchy(String briteId) throws IOException {
		BriteHierarchy hierarchy;
		if ((hierarchy = mapBriteHierarchies.get(briteId)) == null) {
			hierarchy = readAndCacheBriteHierarchy(briteId);
			mapBriteHierarchies.put(briteId, hierarchy);
		}

		return hierarchy;

	}

	/**
	 * Method to get all Brite Entries (Leaf nodes in the hierarchy) that match the
	 * given ID
	 * 
	 * It is a convenience method.
	 * 
	 * @param id
	 * @return All brite entries matching the given id
	 * @throws IOException
	 *             if method was unable to download (from KEGG) or read (cached)
	 *             brite file
	 */
	public HashSet<BriteEntry> getBriteEntryForHierarchyByID(String briteId, String id) throws IOException {
		BriteHierarchy briteHierarchy = getBriteHierarchy(briteId);
		if (briteHierarchy == null)
			return null;
		return briteHierarchy.getBriteEntryById(id);
	}

	/**
	 * Method to get all Brite Entries (Leaf nodes in the hierarchy) that match the
	 * 
	 * It is a convenience method.
	 * 
	 * given EC number
	 * 
	 * @param ec
	 *            Number
	 * @return All brite entries matching the given EC number
	 * @throws IOException
	 *             if method was unable to download (from KEGG) or read (cached)
	 *             brite file
	 * 
	 */
	public HashSet<BriteEntry> getBriteEntryForHierarchyByEC(String briteId, String ec) throws IOException {
		BriteHierarchy briteHierarchy = getBriteHierarchy(briteId);
		if (briteHierarchy == null)
			return null;
		return briteHierarchy.getBriteEntryByEC(ec);
	}

	/**
	 * reads and caches entries for the given Brite ID
	 * 
	 * @param briteId
	 *            e.g. br:ko00001 or br:ko01003
	 * @throws IOException
	 */
	protected BriteHierarchy readAndCacheBriteHierarchy(String briteId) throws IOException {

		BriteHierarchy hierarchy = new BriteHierarchy();

		File file = new File(ReleaseInfo.getAppFolder() + "/" + KEGG_CACHE_DIR + "/" + briteId);

		if (!file.exists()) {
			loadBrite(briteId);
			file = new File(ReleaseInfo.getAppFolder() + "/" + KEGG_CACHE_DIR + "/" + briteId);
		}

		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;

		char maxDepth = 0;

		// read header
		while ((line = reader.readLine()) != null) {
			if (line.startsWith("+")) {
				maxDepth = line.charAt(1);
			}
			if (line.contains("ENTRY")) {
				hierarchy.setId(line.substring(line.indexOf(" ")).trim());
			}
			if (line.contains("NAME")) {
				hierarchy.setName(line.substring(line.indexOf(" ")).trim());
			}
			if (line.contains("DEFINITION")) {
				hierarchy.setDefinition(line.substring(line.indexOf(" ")).trim());
			}

			if (line.equals("!"))
				break;
		}

		/*
		 * just a logical split of loops to keep code more readable and if clauses to a
		 * miminum
		 */

		BriteEntry current;
		BriteEntry parent = null;
		char curDepth = 0;
		char prevDepth = 0;

		// create artificial root node
		hierarchy.root = new BriteEntry(null, null, hierarchy.getDefinition(), null, null);

		current = hierarchy.root;

		while ((line = reader.readLine()) != null) {

			if (line.equals("!")) // we reached the end
				break;

			curDepth = line.charAt(0);
			if (curDepth > prevDepth) { // next level
				parent = current;
			} else if (curDepth < prevDepth) { // previous level
				int i = prevDepth - curDepth;
				for (; i > 0; i--)
					parent = parent.getParent();
			}

			current = readBriteEntry(parent, line);

			if (curDepth == maxDepth) {
				hierarchy.addBriteEntryToEntryMap(current.getId(), current);
			}

			if (current.getSetEC() != null)
				for (String curEC : current.getSetEC())
					hierarchy.addBriteEntryToECEntryMap(curEC, current);

			parent.addChild(current);

			prevDepth = curDepth;

		}

		reader.close();

		return hierarchy;
	}

	protected void loadBrite(String briteId) {
		StringBuffer buf = new StringBuffer();

		buf.append(URL_REST_BRITE);

		buf.append(briteId);
		/*
		 * read the result, create a buffer and call a method to handle the result.
		 */
		File f = new File(ReleaseInfo.getAppFolder() + "/" + KEGG_CACHE_DIR + "/" + briteId);
		f.getParentFile().mkdirs();

		try (FileOutputStream fos = new FileOutputStream(f)) {
			HttpHttpsURL url = new HttpHttpsURL(buf.toString());
			HttpURLConnection conn = url.openConnection();
			if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				InputStream is = conn.getInputStream();
				byte[] buffer = new byte[10000];
				int len;
				while ((len = is.read(buffer)) != -1)
					fos.write(buffer, 0, len);
			}
		} catch (IOException e) {
			// e.printStackTrace();
		}

	}

	/**
	 * reads a brite entry, which are the leaf nodes in this hierarchy. currently
	 * only the ID and the name are extracted and set.
	 * 
	 * @param parent
	 *            the parent briteentry
	 * @param line
	 *            the text line from the file that will be parsed
	 * @return new briteentry object
	 */
	protected BriteEntry readBriteEntry(BriteEntry parent, String line) {
		/*
		 * Leaf Brite entries be like: D K00150 gap2; glyceraldehyde-3-phosphate
		 * dehydrogenase (NAD(P)) [EC:1.2.1.59] | \ \-2 ' ' \ \- optional ID to other DB
		 * | \ \-name | \-ID \-level in hierarchy (max depth)
		 */
		line = line.substring(1).trim(); // cut level
		int idx = line.indexOf("  "); // heuristical scans say, that an ID is separated by the name by 2 space
										// characters
		String id = null;
		if (idx > 0) {
			id = line.substring(0, idx).trim();
			line = line.substring(idx + 2).trim(); // cut ID including the 2 space characters
		}

		/*
		 * we currently assume, that if there is another DB identifier for this entry
		 * it'll surrounded by '[' ']'
		 */
		String name = null;
		Set<String> ecIds = null;
		String pathid = null;

		idx = line.lastIndexOf("[");
		if (idx > 0) {
			name = line.substring(0, idx);
			String ids = line.substring(idx);
			idx = ids.indexOf("EC:");
			if (idx > 0) { // handle list of EC numbers
				ecIds = new HashSet<>();
				StringTokenizer ecs = new StringTokenizer(ids.substring(idx + 3, ids.indexOf("]")), " ");
				while (ecs.hasMoreTokens())
					ecIds.add(ecs.nextToken());
			}
			idx = ids.indexOf("PATH:");
			if (idx > 0) { // handle mentioning of path id
				pathid = ids.substring(idx + 5, ids.indexOf("]"));
			}
		} else
			name = line;

		return new BriteEntry(parent, id, name, ecIds, pathid);
	}

	static Pattern KNumberPattern = Pattern.compile("K\\d{5}");

	public static String extractKeggKOId(String koString) {
		Matcher matcher = KNumberPattern.matcher(koString);
		if (matcher.find())
			return matcher.group();
		else
			return null;
	}

}
