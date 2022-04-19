package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.biomodels;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;


public class BioModelsRestAPI {



    public BioModelsRestAPI() {
    }
    /**
     * Checks if the web services are working properly.
     * @return "Hello BioModels", if the call is a success
     */
    public static String helloBioModels()
    {
            RestApiBiomodels call = new RestApiBiomodels();
            call.fetchData();
            if (call.status < 299){
                return "Hello BioModels";
            } else {
                return null;
            }
    }

    /**
     * Retrieves the SBML form of a model (in a string) given its identifier.
     * @param id model identifier (e.g. BIOMD0000000408 or MODEL1201250000)
     * @return SBML model in a string, or 'null' if the provided identifier is not valid or the model does not exist
     */
    public static String getModelSBMLById(String id)
    {
            RestApiBiomodels call = new RestApiBiomodels();
            call.setPath(id);
            call.setFormat("json");
            String responds = call.fetchData();
            JSONObject data = new JSONObject(responds);
            JSONObject files = data.getJSONObject("files");
            JSONArray main = files.getJSONArray("main");
            JSONObject fileNameJ = new JSONObject(main.get(0).toString());
            String filename = fileNameJ.getString("name");


            call.setPath("model/download/"+id+"?filename="+filename);
            call.setFormat("octet-stream");
            return call.fetchData();
    }

    /*
     * ###################################### private methods ###########################
     */

    public static List<SimpleModel> searchForModels(String searchParameter){
        RestApiBiomodels call = new RestApiBiomodels();
        call.setFormat("json");
        call.setPath("search?query="+searchParameter);
        String responds = call.fetchData();
        JSONObject data = new JSONObject(responds);
        int range = data.getInt("matches");
        ArrayList<SimpleModel> simpleModels = new ArrayList<>();
        for (int i = 0; i < range; i+=100){
            if (i != 0){
                String offset = "&offset=" + i;
                call.setPath("search?query="+searchParameter+offset);
                responds = call.fetchData();
                data = new JSONObject(responds);
            }
            JSONArray models = data.getJSONArray("models");
            for (int j = i; j < models.length()+i; j++){
                SimpleModel model;
                JSONObject getInformation = new JSONObject(models.get(j-i).toString());
                String iD = getInformation.getString("id");
                String lastModified = getInformation.getString("lastModified");
                String name = getInformation.getString("name");
                String submitter = getInformation.getString("submitter");

                model = new SimpleModel(
                        iD,
                        null,
                        name,
                        null,
                        null,
                        Collections.singletonList(submitter),
                        lastModified
                );
                simpleModels.add(model);
            }
        }
        return simpleModels;
    }
}