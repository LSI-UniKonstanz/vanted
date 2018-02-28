/**
 * 
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.biomodels;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;
import org.graffiti.options.PreferencesInterface;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.parameter.StringParameter;

import uk.ac.ebi.biomodels.ws.BioModelsWSClient;
import uk.ac.ebi.biomodels.ws.BioModelsWSException;
import uk.ac.ebi.biomodels.ws.SimpleModel;

/**
 * Access adapter for the Biomodels Webservice client.
 * 
 * @author matthiak
 * @vanted.revision 2.6.5
 */
public class BiomodelsAccessAdapter implements PreferencesInterface {

	/**
	 * 
	 */
	private static final String WEBSERVICE_ENDPOINT_PARAMETER = "Alternative Webservice Endpoint";
	private static String WEBSERVICE_ENDPOINT_VALUE = "";

	private static Logger logger = Logger.getLogger(BiomodelsAccessAdapter.class);

	private BioModelsWSClient biomodelsClient;

	/**
	 * Types of models to be queried.
	 * 
	 * @author matthiak
	 */
	public enum QueryType {
		CHEBI("ChEBI"), NAME("Model Name"), TAXONOMY("Taxonomy"), PERSON("Person"), PUBLICATION(
				"Publication (Name/Id)"), GO("GO Term"), UNIPROT("Uniprot Ids"), BIOMODELID("Biomodels Id");

		private String name;

		private QueryType(String name) {
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
	 *            of models
	 * @param query
	 * @return list of {@linkplain SimpleModel}s of the given type
	 * @throws BioModelsWSException
	 *             unable to establish connection
	 */
	public List<SimpleModel> queryForSimpleModel(QueryType type, String query) throws BioModelsWSException {
		abort = false;
		BioModelsWSClient client = createClient();
		String[] resultIds = null;

		List<SimpleModel> resultSimpleModels = null;

		try {
			switch (type) {
			case NAME:
				resultIds = client.getModelsIdByName(query);
				break;
			case TAXONOMY:
				resultIds = client.getModelsIdByTaxonomy(query);
				break;
			case CHEBI:
				resultIds = client.getModelsIdByChEBI(query);
				break;
			case PERSON:
				resultIds = client.getModelsIdByPerson(query);
				break;
			case GO:
				resultIds = client.getModelsIdByGO(query);
				break;
			case PUBLICATION:
				resultIds = client.getModelsIdByPublication(query);
				break;
			case UNIPROT:
				resultIds = client.getModelsIdByUniprotIds(query.split("[ ;:\t]"));
				break;
			case BIOMODELID:
				resultIds = query.toUpperCase().split("[ ;:\t]");
				break;
			default:
			}
			if (resultIds != null)
				resultSimpleModels = client.getSimpleModelsByIds(resultIds);
			if (!isAbort())
				notifyResultSimpleModelListeners(type, resultSimpleModels);
		} catch (BioModelsWSException e) {
			e.printStackTrace();
			notifyErrorListener(e);
			throw e;
		}

		return resultSimpleModels;
	}

	public String getSBMLModel(SimpleModel simpleModel) throws BioModelsWSException {
		abort = false;
		String result = null;

		try {
			result = createClient().getModelSBMLById(simpleModel.getId());

			if (!isAbort())
				notifyResultSBMLListeners(simpleModel, result);
		} catch (BioModelsWSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			notifyErrorListener(e);
			throw e;
		}

		return result;
	}

	public String getSBMLModel(String modelId) throws BioModelsWSException {
		SimpleModel simpleModel = null;

		String result = null;
		try {
			simpleModel = createClient().getSimpleModelById(modelId);
			result = getSBMLModel(simpleModel);
		} catch (BioModelsWSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			notifyErrorListener(e);
			throw e;
		}
		return result;
	}

	public boolean isAvailable() throws BioModelsWSException {
		return createClient().helloBioModels().equals("Hello BioModels");

	}

	private BioModelsWSClient createClient() throws BioModelsWSException {
		if (biomodelsClient == null) {
			biomodelsClient = new BioModelsWSClient();

			/*
			 * check preferences for custom webservice endpoint This has priority above the
			 * backup endpoint (see below)
			 */
			if (!WEBSERVICE_ENDPOINT_VALUE.equals(""))
				biomodelsClient.setEndPoint(WEBSERVICE_ENDPOINT_VALUE);
			else {
				/*
				 * if standard webservice endpoint doesn't work automatically go to the known
				 * backup endpoint
				 */
				if (!isAvailable())
					biomodelsClient.setEndPoint("http://biomodels.caltech.edu/services/BioModelsWebServices");

			}
		}

		return biomodelsClient;
	}

	/**
	 * @param resultSimpleModels
	 */
	private void notifyResultSimpleModelListeners(QueryType type, List<SimpleModel> resultSimpleModels) {
		if (listeners != null)
			for (BiomodelsLoaderCallback callback : listeners)
				callback.resultForSimpleModelQuery(type, resultSimpleModels);
	}

	/**
	 * @param simpleModel
	 * @param result
	 */
	private void notifyResultSBMLListeners(SimpleModel simpleModel, String result) {
		if (listeners != null)
			for (BiomodelsLoaderCallback callback : listeners)
				callback.resultForSBML(simpleModel, result);
	}

	private void notifyErrorListener(Exception e) {
		if (listeners != null)
			for (BiomodelsLoaderCallback callback : listeners)
				callback.resultError(e);
	}

	public boolean isAbort() {
		return abort;
	}

	public void setAbort(boolean abort) {
		this.abort = abort;
	}

	/**
	 * This methods sets up 2 sets of biomodels identifiers These identifiers are
	 * then used to indicate items in the result set, if they're (non)curated FUTURE
	 * IMPLEMENTATION
	 * 
	 * @throws BioModelsWSException
	 */
	@SuppressWarnings("unused")
	private void initSetStructures() throws BioModelsWSException {

		final BioModelsWSClient client = createClient();

		setCuratedModelIds = new HashSet<String>();

		setNonCuratedModelIds = new HashSet<String>();

		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					String[] allCuratedModelsId = client.getAllCuratedModelsId();
					if (allCuratedModelsId != null) {
						for (String modelId : allCuratedModelsId)
							setCuratedModelIds.add(modelId);
						logger.debug("loaded curated model ids");
					}
				} catch (BioModelsWSException e) {
					e.printStackTrace();
				}
				try {
					String[] allNonCuratedModelsId = client.getAllNonCuratedModelsId();
					if (allNonCuratedModelsId != null) {
						for (String modelId : allNonCuratedModelsId)
							setNonCuratedModelIds.add(modelId);
						logger.debug("loaded non curated model ids");
					}
				} catch (BioModelsWSException e) {
					e.printStackTrace();
				}

			}
		}).start();
	}

	public boolean removeListener(BiomodelsLoaderCallback listener) {
		return listeners.remove(listener);
	}

	public void addListener(BiomodelsLoaderCallback listener) {
		if (listeners == null)
			listeners = new ArrayList<BiomodelsAccessAdapter.BiomodelsLoaderCallback>();
		listeners.add(listener);
	}

	public interface BiomodelsLoaderCallback {
		public void resultForSimpleModelQuery(QueryType type, List<SimpleModel> simpleModel);

		public void resultForSBML(SimpleModel model, String modelstring);

		public void resultError(Exception e);
	}

	@Override
	public List<Parameter> getDefaultParameters() {
		List<Parameter> list = new ArrayList<Parameter>();
		list.add(new StringParameter("", WEBSERVICE_ENDPOINT_PARAMETER,
				"<html>Specify an alternative endpoint in case the standard endpoint is not available"
						+ "<br/>The endpoint needs to be in URL form like:"
						+ "<br/>&nbsp &nbsp &nbsp http://biomodels.caltech.edu/services/BioModelsWebServices" + "<br/>"
						+ "<br/>Leave empty for the standard webservice endpoint"));

		return list;
	}

	@Override
	public void updatePreferences(Preferences preferences) {
		WEBSERVICE_ENDPOINT_VALUE = preferences.get(WEBSERVICE_ENDPOINT_PARAMETER, "");
		biomodelsClient = null;
	}

	@Override
	public String getPreferencesAlternativeName() {
		return "BioModels Webservice Endpoint";
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

}
