/**
 * 
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.biomodels;

import java.util.*;
import java.util.prefs.Preferences;
import org.apache.log4j.Logger;
import org.graffiti.options.PreferencesInterface;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.parameter.StringParameter;


/**
 * Access adapter for the Biomodels Webservice client.
 * 
 * @author matthiak
 */
public class BiomodelsAccessAdapter {
	
	/**
	 * 
	 */
	private static final String WEBSERVICE_ENDPOINT_PARAMETER = "Alternative Webservice Endpoint";

	private static final Logger logger = Logger.getLogger(BiomodelsAccessAdapter.class);

	/**
	 * Types of models to be queried.
	 * 
	 * @author matthiak
	 */

	//ENSEMBL
		//changelog .3
	public enum QueryType {
		CHEBI("ChEBI"), NAME("Model Name"), TAXONOMY("Taxonomy"), PERSON("Person"),
		PUBLICATION("Publication (Name/Id)"), GO("GO Term"), UNIPROT("Uniprot Ids"),
		BIOMODELID("Biomodels Id"), PUBMED("PUBMED"), DESCRIPTION("Description"),
		DISEASE("Disease"), ENSEMBL("Ensembl");
		
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
	
	/**
	 * sets, which will be used to further identify the models
	 */
	Set<String> setNonCuratedModelIds;
	Set<String> setCuratedModelIds;
	
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
		// initSetStructures();
	}
	
	/**
	 * Queries simple models, {@linkplain SimpleModel}, by type
	 * {@linkplain QueryType} and loads the corresponding Simple Models
	 * 
	 * @param type
	 *           of models
	 */
	public void queryForSimpleModel(QueryType type, String query) {
		abort = false;
		List<SimpleModel> resultSimpleModels = null;
			switch (type) {
				case NAME:
					resultSimpleModels = BioModelsRestAPI.searchForModels("name%3A%22"+query+
							"%22"+"&numResults=100");
					break;
				case TAXONOMY:
					String cleanedTAXONOMY = query.toUpperCase().replaceAll("TAXONOMY:","");
					resultSimpleModels = BioModelsRestAPI.searchForModels("TAXONOMY%3A"+cleanedTAXONOMY
							+"&numResults=100");
					break;
				case CHEBI:
					String cleanedCHEBI = query.toUpperCase().replaceAll("CHEBI:","");
					resultSimpleModels = BioModelsRestAPI.searchForModels("CHEBI%3ACHEBI%3A" + cleanedCHEBI
							+"&numResults=100");
					break;
				case PERSON:
					String cleaned = query.replaceAll(" ", "%20");
					List<SimpleModel> submitter = BioModelsRestAPI.searchForModels("submitter%3A%22"
							+ cleaned+"%22" +"&numResults=100");
					List<SimpleModel> pubAuthor = BioModelsRestAPI.searchForModels("publication_authors%3A%22"
							+cleaned+"%22" +"&numResults=100");
					List<SimpleModel> results = new ArrayList<>(submitter);
					results.addAll(pubAuthor);
					resultSimpleModels = results;
					break;
				case GO:
					String queryCleaned = query.toUpperCase().replaceAll("GO:","");
					resultSimpleModels = BioModelsRestAPI.searchForModels("GO%3AGO%3A"+queryCleaned
							+"&numResults=100");
					break;
				case PUBLICATION:
					resultSimpleModels = BioModelsRestAPI.searchForModels("publication%3A("
							+ query + ")&numResults=100");
					break;
				case UNIPROT:
					String queryCleanedU = query.toUpperCase().replaceAll("UNIPROT:","");
					resultSimpleModels = BioModelsRestAPI.searchForModels("UNIPROT%3A"+queryCleanedU
							+ "&numResults=100");
					break;
				case BIOMODELID:
					resultSimpleModels = BioModelsRestAPI.searchForModels(query +"&numResults=100");
					break;
				case PUBMED:
					resultSimpleModels = BioModelsRestAPI.searchForModels("PUBMED%3A%22" + query
							+ "%22&numResults=100");
					break;
				case DESCRIPTION:
					resultSimpleModels = BioModelsRestAPI.searchForModels("description%3A%22" + query
							+ "%22&numResults=100");
					break;
				case DISEASE:
					String cleanedDisease = query.replaceAll(" ","%20");
					resultSimpleModels = BioModelsRestAPI.searchForModels("disease%3A%22"
							+ cleanedDisease + "%22&numResults=100");
					break;
				case ENSEMBL:
					String cleanedEn = query.toUpperCase().replaceAll("ENSEMBL","");
					resultSimpleModels = BioModelsRestAPI.searchForModels("ENSEMBL%3A"+cleanedEn
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
		return BioModelsRestAPI.getModelSBMLById(modelId);
	}
	
	public boolean isAvailable() {
		return Objects.equals(BioModelsRestAPI.helloBioModels(), "Hello BioModels");
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
	
	/**
	 * This method sets up 2 sets of biomodels identifiers These identifiers are
	 * then used to indicate items in the result set, if they're (non)curated FUTURE
	 * IMPLEMENTATION
	 * 
	 */
	@SuppressWarnings("unused")
	private void initSetStructures(){

		setCuratedModelIds = new HashSet<>();
		
		setNonCuratedModelIds = new HashSet<>();
		
		new Thread(() -> {
		}).start();
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
