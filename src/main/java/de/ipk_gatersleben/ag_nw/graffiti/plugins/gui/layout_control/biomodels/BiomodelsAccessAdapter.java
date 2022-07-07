/**
 * 
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.biomodels;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Access adapter for the Biomodels Webservice client.
 * 
 * @author matthiak
 * @vanted.revision 2.8.3
 */
public class BiomodelsAccessAdapter {

	/**
	 * Types of models to be queried.
	 * 
	 * @author matthiak
	 */
	public enum QueryType {
		CHEBI("CHEBI"), NAME("Model Name"), TAXONOMY("TAXONOMY"), PERSON("Person"),
		PUBLICATION("Publication (Name/ID)"), GO("GO Term"), UNIPROT("UNIPROT IDs"),
		BIOMODELID("BioModels ID"), PUBMED("PUBMED"), DESCRIPTION("Description"),
		DISEASE("Disease"), ENSEMBL("ENSEMBL");
		
		private final String name;
		
		QueryType(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}


		public String toString() {
			return name;
		}
	}

	public enum QueryAdvanced {
		AND, OR, NOT

	}
	
	List<BiomodelsLoaderCallback> listeners;
	
	/**
	 * this variable is used by calling threads to indicate, that they want to abort
	 * the current query This results in NOT calling the callback methods
	 */
	boolean abort;
	
	/**
	 * Default constructor.
	 */
	public BiomodelsAccessAdapter() {
		abort = false;
	}
	
	/**
	 * Queries simple models, {@linkplain SimpleModel}, by type
	 * {@linkplain QueryType} and loads the corresponding Simple Models
	 *
	 * @param type of models
	 * @
	 */
	public void queryForSimpleModel(BiomodelsAccessAdapter.QueryType[] type, String[] query) {
		abort = false;
		List<SimpleModel> resultSimpleModels;
		StringBuilder queryRequestString = new StringBuilder();
		String and = "%20AND%20";
		String param = "&numResults=100";
		for (int i = 0; i < type.length; i++){
			if (query[i] != null && !Objects.equals(query[i], "")){
				if (i > 0){
					queryRequestString.append(and);
				}

				String queryNoLetter = query[i].toUpperCase().replaceAll("([A-Z]*:*)*","");
				switch (type[i]) {
					case NAME:
						queryRequestString.append("name%3A%22").append("*").append(query[i]).append("*").append("%22");
						break;
					case TAXONOMY:
						String cleanedTAXONOMY = query[i].toUpperCase().replaceAll("TAXONOMY:","");
						queryRequestString.append("TAXONOMY%3A").append("*").append(cleanedTAXONOMY).append("*");
						break;
					case CHEBI:
						queryRequestString.append("CHEBI%3ACHEBI%3A").append("*").append(queryNoLetter).append("*");
						break;
					case PERSON:
						String cleaned = query[i].replaceAll(" ", "%20");
						queryRequestString.append("publication_authors%3A%22").append("*").append(cleaned).append("*").append("%22");
						break;
					case GO:
						//Changed GO Term
						queryRequestString.append("GO%3AGO%3A").append(queryNoLetter).append("*");
						break;
					case PUBLICATION:
						queryRequestString.append("publication%3A(").append("*").append(query[i]).append("*").append(")");
						break;
					case UNIPROT:
						String queryCleanedU = query[i].toUpperCase().replaceAll("UNIPROT:*P*","");
						queryRequestString.append("UNIPROT%3AP*").append(queryCleanedU).append("*");
						break;
					case BIOMODELID:
						queryRequestString.append("BIOMD*").append(queryNoLetter).append("*").append(param);
						break;
					case PUBMED:
						queryRequestString.append("PUBMED%3A%22").append(queryNoLetter).append("%22");
						break;
					case DESCRIPTION:
						queryRequestString.append("description%3A%22").append(query[i]).append("*").append("%22");
						break;
					case DISEASE:
						String cleanedDisease = query[i].replaceAll(" ","%20");
						queryRequestString.append("disease%3A%22").append(cleanedDisease).append("%22");
						break;
					case ENSEMBL:
						String cleanedEn = query[i].toUpperCase().replaceAll("ENSEMBL","");
						queryRequestString.append("ENSEMBL%3A*").append(cleanedEn).append("*");
						break;
					default:
				}
			}
		}
		//Testing of the HTML Request String
		System.out.println(queryRequestString);

		queryRequestString.append(param);
		resultSimpleModels = RestApiBiomodels.searchForModels(String.valueOf(queryRequestString));
		notifyResultSimpleModelListeners(resultSimpleModels);

	}
	
	public void notifySBML() {
			notifyResultSBMLListeners();
	}
	
	public String getSBMLModel(String modelId) {
		return RestApiBiomodels.getModelSBMLById(modelId);
	}
	
	public boolean isAvailable() {
		return Objects.equals(RestApiBiomodels.helloBioModels(), "Hello BioModels");
	}

	/**
	 */
	private void notifyResultSimpleModelListeners(List<SimpleModel> resultSimpleModels) {
		if (listeners != null)
			for (BiomodelsLoaderCallback callback : listeners)
				callback.resultForSimpleModelQuery(resultSimpleModels);
	}

	private void notifyResultSBMLListeners() {
		if (listeners != null)
			for (BiomodelsLoaderCallback callback : listeners)
				callback.resultForSBML();
	}
	
	public boolean isAbort() {
		return abort;
	}
	
	public void setAbort(boolean abort) {
		this.abort = abort;
	}
	
	public boolean removeListener(BiomodelsLoaderCallback listener) {
		return listeners.remove(listener);
	}
	
	public void addListener(BiomodelsLoaderCallback listener) {
		if (listeners == null)
			listeners = new ArrayList<>();
		listeners.add(listener);
	}
	
	public interface BiomodelsLoaderCallback {
		void resultForSimpleModelQuery(List<SimpleModel> simpleModel);
		void resultForSBML();
	}
	
}
