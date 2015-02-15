/**
 * 
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.biomodels;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import uk.ac.ebi.biomodels.ws.BioModelsWSClient;
import uk.ac.ebi.biomodels.ws.BioModelsWSException;
import uk.ac.ebi.biomodels.ws.SimpleModel;

/**
 * @author matthiak
 *
 */
public class BiomodelsAccessAdapter {

	private static Logger logger = Logger.getLogger(BiomodelsAccessAdapter.class);

	public enum QueryType{
		CHEBI("ChEBI"),
		NAME("Model Name"),
		TAXONOMY("Taxonomy"),
		PERSON("Person"),
		PUBLICATION("Publication (Name/Id)"),
		GO("GO Term"),
		UNIPROT("Uniprot Ids");
		
		
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
	 * sets, which will be used to further identify the 
	 * models 
	 */
	Set<String> setNonCuratedModelIds;
	Set<String> setCuratedModelIds;

	BioModelsWSClient client;
	
	List<BiomodelsLoaderCallback> listeners;
	/**
	 * 
	 */
	public BiomodelsAccessAdapter() {
		client = new BioModelsWSClient();

//		initSetStructures();
	}

	public List<SimpleModel> queryForSimpleModel(QueryType type, String query) throws BioModelsWSException {
		
		String[] resultIds = null;
		
		List<SimpleModel> resultSimpleModels = null;
		
		switch(type){
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
			resultIds = client.getModelsIdByUniprotIds(query.split(" "));
			break;

		default:
		}
		if(resultIds != null)
			resultSimpleModels = client.getSimpleModelsByIds(resultIds);
		
		notifyResultSimpleModelListeners(type, resultSimpleModels);
		
		return resultSimpleModels;
	}

	public String getSBMLModel(SimpleModel simpleModel) throws BioModelsWSException {
		String result = client.getModelSBMLById(simpleModel.getId());
		
		notifyResultSBMLListeners(simpleModel, result);
		
		return result;
	}
	

	public String getSBMLModel(String modelId) throws BioModelsWSException {
		SimpleModel simpleModel = client.getSimpleModelById(modelId);
		String result = getSBMLModel(simpleModel);
		return result;
	}
	
	/**
	 * @param resultSimpleModels
	 */
	private void notifyResultSimpleModelListeners(QueryType type, List<SimpleModel> resultSimpleModels) {
		if(listeners != null)
			for(BiomodelsLoaderCallback callback : listeners)
				callback.resultForSimpleModelQuery(type, resultSimpleModels);
	}

	/**
	 * @param simpleModel
	 * @param result
	 */
	private void notifyResultSBMLListeners(SimpleModel simpleModel,
			String result) {
		if(listeners != null)
			for(BiomodelsLoaderCallback callback : listeners)
				callback.resultForSBML(simpleModel, result);
	}

	
	private void initSetStructures(){

		setCuratedModelIds = new HashSet<String>();

		setNonCuratedModelIds = new HashSet<String>();

		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					String[] allCuratedModelsId = client.getAllCuratedModelsId();
					if(allCuratedModelsId != null){
						for(String modelId : allCuratedModelsId)
							setCuratedModelIds.add(modelId);
						logger.debug("loaded curated model ids");
					}
				} catch (BioModelsWSException e) {
					e.printStackTrace();
				}
				try {
					String[] allNonCuratedModelsId = client.getAllNonCuratedModelsId();
					if(allNonCuratedModelsId != null){
						for(String modelId : allNonCuratedModelsId)
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
		if(listeners == null)
			listeners = new ArrayList<BiomodelsAccessAdapter.BiomodelsLoaderCallback>();
		listeners.add(listener);
	}




	public interface BiomodelsLoaderCallback{
		public void resultForSimpleModelQuery(QueryType type, List<SimpleModel> simpleModel);
		
		public void resultForSBML(SimpleModel model, String modelstring);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}



}
