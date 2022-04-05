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
public class BiomodelsAccessAdapter implements PreferencesInterface {
	
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
	public enum QueryType {
		CHEBI("ChEBI"), NAME("Model Name"), TAXONOMY("Taxonomy"), PERSON("Person"), PUBLICATION("Publication (Name/Id)"), GO("GO Term"), UNIPROT(
				"Uniprot Ids"), BIOMODELID("Biomodels Id");
		
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
	 * {@linkplain QueryType} and
	 * 
	 * @param type
	 *           of models
	 */
	public void queryForSimpleModel(QueryType type, String query) {
		abort = false;
		String[] resultIds = null;
		List<SimpleModel> resultSimpleModels = null;
			switch (type) {
				case NAME:
					resultIds = BioModelsRestAPI.getModelsIdByName(query);
					break;
				case TAXONOMY:
					resultIds = BioModelsRestAPI.getModelsIdByTaxonomy(query);
					break;
				case CHEBI:
					resultIds = BioModelsRestAPI.getModelsIdByChEBI(query);
					break;
				case PERSON:
					resultIds = BioModelsRestAPI.getModelsIdByPerson(query);
					break;
				case GO:
					resultIds = BioModelsRestAPI.getModelsIdByGO(query);
					break;
				case PUBLICATION:
					resultIds = BioModelsRestAPI.getModelsIdByPublication(query);
					break;
				case UNIPROT:
					resultIds = BioModelsRestAPI.getModelsIdByUniprotIds(query.split("[ ;:\t]"));
					break;
				case BIOMODELID:
					resultIds = query.toUpperCase().split("[ ;:\t]");
					break;
				default:
			}
			if (resultIds != null) {
				resultSimpleModels = BioModelsRestAPI.getSimpleModelsByIds(resultIds);
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
				String[] allCuratedModelsId = BioModelsRestAPI.getAllCuratedModelsId();
				Collections.addAll(setCuratedModelIds, allCuratedModelsId);
				logger.debug("loaded curated model ids");
				String[] allNonCuratedModelsId = BioModelsRestAPI.getAllNonCuratedModelsId();
				Collections.addAll(setNonCuratedModelIds, allNonCuratedModelsId);
				logger.debug("loaded non curated model ids");

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
	
	@Override
	public List<Parameter> getDefaultParameters() {
		List<Parameter> list = new ArrayList<>();
		list.add(new StringParameter("", WEBSERVICE_ENDPOINT_PARAMETER,
				"<html>Specify an alternative endpoint in case the standard endpoint is not available"
						+ "<br/>The endpoint needs to be in URL form like:"
						+ "<br/>&nbsp &nbsp &nbsp http://biomodels.caltech.edu/services/BioModelsWebServices" + "<br/>"
						+ "<br/>Leave empty for the standard webservice endpoint"));
		
		return list;
	}

	@Override
	public void updatePreferences(Preferences preferences) {
		String WEBSERVICE_ENDPOINT_VALUE = preferences.get(WEBSERVICE_ENDPOINT_PARAMETER, "");
	}
	
	@Override
	public String getPreferencesAlternativeName() {
		return "BioModels Webservice Endpoint";
	}
	
}
