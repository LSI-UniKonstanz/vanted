package de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveAction;

import org.apache.log4j.Logger;
import org.vanted.updater.HttpHttpsURL;

/**
 * A Kegg API REST accessor class
 * 
 * It will retrieve (VATEND-)supported database objects from the KEGG database.
 * The supported databases are listed in the enum object: KeggDatabases.
 * 
 * The common URL for that is e.g.: http://rest.kegg.jp/get/ko:K00001 This
 * retrieves the KEGG Orthology (ko) entry (K number) K00001 -----
 * 
 * This accessor class also supports the 'link' operation of the REST service
 * which translates an ID to a target ID givent the target database.
 * 
 * http://rest.kegg.jp/link/compound/enzyme:1.1.1.1
 * 
 * This will find all connected KEGG compounds, given the EC ID from the KEGG
 * enyzme database
 * 
 * 
 * @author matthiak
 *
 */
public class KeggAPIService {

	static Logger logger = Logger.getLogger(KeggAPIServiceHelper.class);

	static String URL_REST_LINK_ID = "http://rest.kegg.jp/link/";

	static int MAX_QUERY_SIZE = 10;

	/**
	 * Retrieves results from KEGG REST service, using the link functionality see:
	 * http://www.kegg.jp/kegg/rest/keggapi.html [Linked entries]
	 * 
	 * The result set will be KO entries. The source must be valid identifiers: *
	 * 
	 * @author matthiak
	 *
	 */
	class RetrieveLinkMappingFromIDtoKO extends RecursiveAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 7688882888446634509L;
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
				// do the work
			}
		}

		/**
		 * translates given ids to KO ids using KEGG REST API
		 * 
		 * @return
		 */
		@SuppressWarnings("unused")
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
						String[] mappedIds = line.split("\t");
						if (mappedIds.length != 2)
							System.err.println();
						retKOIds.add(mappedIds[1].trim());
					}
				}
			} catch (IOException e) {

				e.printStackTrace();
			}

			return retKOIds;
		}
	}

}
