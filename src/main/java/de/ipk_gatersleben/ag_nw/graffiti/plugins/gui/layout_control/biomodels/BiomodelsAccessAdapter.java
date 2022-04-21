/**
 * 
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.biomodels;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.apache.log4j.Logger;


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
	public void queryForSimpleModel(QueryType type, String query) {
		abort = false;
		List<SimpleModel> resultSimpleModels = null;
			switch (type) {
				case NAME:
					resultSimpleModels = RestApiBiomodels.searchForModels("name%3A%22"+query+
							"%22"+"&numResults=100");
					break;
				case TAXONOMY:
					String cleanedTAXONOMY = query.toUpperCase().replaceAll("TAXONOMY:","");
					resultSimpleModels = RestApiBiomodels.searchForModels("TAXONOMY%3A"+cleanedTAXONOMY
							+"&numResults=100");
					break;
				case CHEBI:
					String cleanedCHEBI = query.toUpperCase().replaceAll("CHEBI:","");
					resultSimpleModels = RestApiBiomodels.searchForModels("CHEBI%3ACHEBI%3A" + cleanedCHEBI
							+"&numResults=100");
					break;
				case PERSON:
					String cleaned = query.replaceAll(" ", "%20");
					List<SimpleModel> submitter = RestApiBiomodels.searchForModels("submitter%3A%22"
							+ cleaned+"%22" +"&numResults=100");
					List<SimpleModel> pubAuthor = RestApiBiomodels.searchForModels("publication_authors%3A%22"
							+cleaned+"%22" +"&numResults=100");
					List<SimpleModel> results = new ArrayList<>(submitter);
					results.addAll(pubAuthor);
					resultSimpleModels = results;
					break;
				case GO:
					String queryCleaned = query.toUpperCase().replaceAll("GO:","");
					resultSimpleModels = RestApiBiomodels.searchForModels("GO%3AGO%3A"+queryCleaned
							+"&numResults=100");
					break;
				case PUBLICATION:
					resultSimpleModels = RestApiBiomodels.searchForModels("publication%3A("
							+ query + ")&numResults=100");
					break;
				case UNIPROT:
					String queryCleanedU = query.toUpperCase().replaceAll("UNIPROT:","");
					resultSimpleModels = RestApiBiomodels.searchForModels("UNIPROT%3A"+queryCleanedU
							+ "&numResults=100");
					break;
				case BIOMODELID:
					resultSimpleModels = RestApiBiomodels.searchForModels(query +"&numResults=100");
					break;
				case PUBMED:
					resultSimpleModels = RestApiBiomodels.searchForModels("PUBMED%3A%22" + query
							+ "%22&numResults=100");
					break;
				case DESCRIPTION:
					resultSimpleModels = RestApiBiomodels.searchForModels("description%3A%22" + query
							+ "%22&numResults=100");
					break;
				case DISEASE:
					String cleanedDisease = query.replaceAll(" ","%20");
					resultSimpleModels = RestApiBiomodels.searchForModels("disease%3A%22"
							+ cleanedDisease + "%22&numResults=100");
					break;
				case ENSEMBL:
					String cleanedEn = query.toUpperCase().replaceAll("ENSEMBL","");
					resultSimpleModels = RestApiBiomodels.searchForModels("ENSEMBL%3A"+cleanedEn
							+ "&numResults=100");
				default:
			}
			if (!isAbort())
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
