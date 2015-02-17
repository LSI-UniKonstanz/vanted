/**
 * 
 */
package edu.monash.vanted.test.path2models;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.junit.Test;

import uk.ac.ebi.biomodels.ws.BioModelsWSClient;
import uk.ac.ebi.biomodels.ws.BioModelsWSException;
import uk.ac.ebi.biomodels.ws.SimpleModel;

/**
 * @author matthiak
 *
 */
public class TestPath2Models extends TestCase{

	private BioModelsWSClient client;

	protected void setUp() throws Exception {
		client = new BioModelsWSClient();
		
		
	}
/*	
//	@Test
	public void testPath2ModelsConnector() {
		
		try {
			System.out.println(client.helloBioModels());
		} catch (BioModelsWSException e) {
			e.printStackTrace();
		}
	}
	
//	@Test
	public void testLoadSimpleModels() {
		
		
		try {
			
			Map<String, List<SimpleModel>> mapModels = client.getSimpleModelsByChEBIIds(new String[]{"CHEBI:15355"});
			
			for(String key : mapModels.keySet()) {
				List<SimpleModel> curList = mapModels.get(key);
				System.out.println("key: "+key);
				for(SimpleModel curModel : curList) {
					
					System.out.println("  model name: "+curModel.getName());
					System.out.println("  model id: "+curModel.getId());
					
					String modelSBMLById = client.getModelSBMLById(curModel.getId());
					FileWriter writer = new FileWriter("/tmp/sbml-testwrite-"+curModel.getId()+".sbml");
					writer.write(modelSBMLById);
					writer.close();
				}
			}
		} catch (BioModelsWSException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
*/	
	@Test
	public void testLoadModelByNameSearch() {
		
		try {
			String modelSBMLById = client.getModelSBMLById("BMID000000107284");
			if(modelSBMLById != null)
				System.out.println("have model");
		} catch (BioModelsWSException e) {
			e.printStackTrace();
		}
	}
}
