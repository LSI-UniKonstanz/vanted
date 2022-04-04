package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.biomodels;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import uk.ac.ebi.biomodels.ws.BioModelsWSException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

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

    /**
     * Retrieves the name of the authors of the publication associated with a given model.
     * @param modelId model identifier (e.g. BIOMD0000000408 or MODEL1201250000)
     * @return names of the publication's authors, or 'null' if the provided identifier is not valid or the model does not exist
     */
    public static String[] getAuthorsByModelId(String modelId)
    {
            RestApiBiomodels call = new RestApiBiomodels();
            call.setFormat("json");
            call.setPath(modelId);
            String responds = call.fetchData();
            JSONObject data = new JSONObject(responds);
            JSONObject pub = data.getJSONObject("publication");
            ArrayList<String> authorArrayList = new ArrayList<>();
            JSONArray authorsJ = pub.getJSONArray("authors");
            String[] authors = new String[authorsJ.length()];
            for (int i = 0; i < authorsJ.length(); i++) {
                JSONObject authorS = (JSONObject) authorsJ.get(i);
                authors[i] = authorS.getString("name");
            }
            return authors;
    }

    /**
     * Retrieves the publication's identifier of a given model.
     * @param modelId model identifier (e.g. BIOMD0000000408 or MODEL1201250000)
     * @return publication identifier (can be a PMID, DOI or URL), or 'null' if the provided identifier is not valid or the model does not exist
     */
    public static String getPublicationByModelId(String modelId)
    {
            RestApiBiomodels call = new RestApiBiomodels();
            call.setFormat("json");
            call.setPath(modelId);
            String responds = call.fetchData();
            JSONObject data = new JSONObject(responds);
            return data.getString("publicationId");
    }

    /**
     * Retrieves the date of last modification of a given model.
     * @param modelId model identifier (e.g. BIOMD0000000408 or MODEL1201250000)
     * @return date of last modification (expressed according to ISO 8601), or 'null' if the provided identifier is not valid or the model does not exist
     */
    public static String getLastModifiedDateByModelId(String modelId)
    {
            RestApiBiomodels call = new RestApiBiomodels();
            call.setFormat("json");
            call.setPath(modelId);
            String responds = call.fetchData();
            JSONObject data = new JSONObject(responds);
            JSONObject history = data.getJSONObject("history");
            JSONArray revisions = history.getJSONArray("revisions");

            long lastDate = 0;
            for (int i=0; i<revisions.length(); i++){
                JSONObject revision = new JSONObject(revisions.get(i).toString());
                if (revision.getLong("submitted") > lastDate){
                    lastDate = revision.getLong("submitted");
                }
            }
            return String.valueOf(lastDate);
    }

    /**
     * Retrieves the main information (identifier, name, publication identifier and date of last modification) about a given model.
     * @param id model identifier (e.g. BIOMD0000000408 or MODEL1201250000)
     * @return a <code>SimpleModel</code>, or 'null' if the provided identifier is not valid or the model does not exist
     * @throws BioModelsWSException
     */
    public static SimpleModel getSimpleModelById(String id)
        {
            SimpleModel model = null;
            RestApiBiomodels call = new RestApiBiomodels();
            call.setFormat("json");
            call.setPath(id);
            String respondsbody = call.fetchData();
            JSONObject data = new JSONObject(respondsbody);

            List<String> encoders = new ArrayList<String>();
            List<String> authors = new ArrayList<String>();
            String iD = "";
            String submissionId = "";
            String name = "";
            String publicationId = "";

            try {
                JSONObject pub = data.getJSONObject("publication");
                JSONArray authorsJ = pub.getJSONArray("authors");
                for (int i = 0; i < authorsJ.length(); i++) {
                    JSONObject authorS = (JSONObject) authorsJ.get(i);
                    try {
                        authors.add(authorS.getString("name"));
                    } catch (JSONException e) {
                        System.out.println("Fail");
                    }
                }
            }
            catch (Exception e){
                System.out.println("No Publication");
            }
            try{
                iD = data.getString("publicationId");
            }
            catch (Exception e){
            }
            try {
                submissionId = data.getString("submissionId");
            }
            catch (Exception e){
            }
            try{
                name = data.getString("name");
            }
            catch (Exception e){
            }
            try {
                JSONObject pub = data.getJSONObject("publication");
                publicationId = pub.getString("orcid");
            }
            catch (Exception e){
            }
            String searchForDate = "";
            if (submissionId != null){
                searchForDate = submissionId;
            } else if (iD != null){
                searchForDate = iD;
            } else {
                searchForDate = publicationId;
            }

            model = new SimpleModel(
                    iD,
                    submissionId,
                    name,
                    encoders,
                    publicationId,
                    authors,
                    getLastModifiedDateByModelId(searchForDate)

            );

            return model;
        }

    /**
     * Retrieves the main information (identifier, name, publication identifier and date of last modification, ...) about given models.
     * @param ids list of model identifiers (e.g. BIOMD0000000408 or MODEL1201250000)
     * @return list of <code>SimpleModel</code>
     * @throws BioModelsWSException
     */
    public static List<SimpleModel> getSimpleModelsByIds(String[] ids) {
        List<SimpleModel> models = new ArrayList<>();
            for (String id:ids)
            {
                if (id != null){
                    models.add(getSimpleModelById(id));
                }
            }
        return models;
    }

    /**
     * Retrieves the name of a model name given its identifier.
     * @param id model identifier (e.g. BIOMD0000000408 or MODEL1201250000)
     * @return model name, or 'null' if the provided identifier is not valid or the model does not exist
     * @throws BioModelsWSException
     */
    public static String getModelNameById(String id)
    {
            RestApiBiomodels call = new RestApiBiomodels();
            call.setFormat("json");
            call.setPath(id);
            String responds = call.fetchData();
            JSONObject data = new JSONObject(responds);
            return data.getString("name");

    }


    /**
     * Retrieves the identifiers of all the published curated models.
     * @return list of model identifiers
     * @throws BioModelsWSException
     */
    public static String[] getAllCuratedModelsId()
    {
            return searchForIDs("curationstatus%3A%22Manually%20curated%22&numResults=100");
    }

    /**
     * Retrieves the identifiers of all the published non-curated models.
     * @return list of model identifiers
     * @throws BioModelsWSException
     */
    public static String[] getAllNonCuratedModelsId()
    {
            return searchForIDs("curationstatus%3A%22Non-curated%22&numResults=100");
    }

    /**
     * Retrieves the models' identifiers which name includes the given keyword.
     * @param modelName part of a model name
     * @return list of models identifiers
     * @throws BioModelsWSException
     */
    public static String[] getModelsIdByName(String modelName)
    {
            return searchForIDs(modelName);
    }

    /**
     * Retrieves the identifiers of all models which have a given person as author or encoder.
     * @param personName author's or encoder's name
     * @return list of models identifiers
     * @throws BioModelsWSException
     */
    public static String[] getModelsIdByPerson(String personName)
    {
            return searchForIDs(personName);
    }

    /**
     * Retrieves the identifiers of all models related to one (or more) publication(s).
     * @param publicationIdOrText publication identifier (PMID or DOI) or text which occurs in the publication's title or abstract
     * @return list of model identifiers
     * @throws BioModelsWSException
     */
    public static String[] getModelsIdByPublication(String publicationIdOrText)
    {

            return searchForIDs(publicationIdOrText);
    }

    /**
     * Retrieves the models which are annotated with the given ChEBI terms.
     * @param ChEBIIds identifiers of a ChEBI terms (e.g. CHEBI:4991)
     * @return all models annotated with the provided ChEBI identifiers, as a TreeMap (which uses ChEBI identifiers as keys)
     * @throws BioModelsWSException
     */
    public static Map<String, List<SimpleModel>> getSimpleModelsByChEBIIds(String[] ChEBIIds)
    {
        Map<String, List<SimpleModel>> modelsMap = null;
            modelsMap = new TreeMap<String, List<SimpleModel>>();
            for (String s : ChEBIIds) {
                ArrayList<SimpleModel> models = new ArrayList<>();
                String cleaned = s.replaceAll(":", "%3A");
                String[] ids = searchForIDs("CHEBI%3A" + cleaned + "&numResults=100");
                for (String a : ids) {
                    System.out.println(a);
                }
                for (String id : ids) {
                    models.add(getSimpleModelById(id));
                }
                modelsMap.put(s, models);
            }
            return modelsMap;
    }

    /**
     * Retrieves the identifiers of all models which are associated to some ChEBI terms.
     * This relies on the method 'getLiteEntity' of the ChEBI Web Services (cf. http://www.ebi.ac.uk/chebi/webServices.do).
     * @param text ChEBI identifier (e.g. CHEBI:4991) or name or synonym
     * @return list of models identifiers
     * @throws BioModelsWSException
     */
    public static String[] getModelsIdByChEBI(String text)
    {
            String cleaned = text.replaceAll(":", "%3A");
            return searchForIDs("CHEBI%3A" + cleaned + "&numResults=100");

    }

    /**
     * Retrieves all the models which are annotated with the given UniProt records.
     * @param UniProtIds list of UniProt identifiers (e.g. P12345)
     * @return list of models identifiers
     * @throws BioModelsWSException
     */
    public static String[] getModelsIdByUniprotIds(String[] UniProtIds)
    {
            String[] allIds = new String[0];
            for (String s: UniProtIds)
            {
                String[] ids = searchForIDs("UNIPROT:"+s);
                allIds = Stream.of(allIds, ids).flatMap(Stream::of)
                    .toArray(String[]::new);;
            }
            return allIds;
    }

    /**
     * Retrieves the models which are associated to the provided Gene Ontology text.
     * @param text free (GO based) text
     * @return list of models identifiers
     * @throws BioModelsWSException
     */
    public static String[] getModelsIdByGO(String text)
    {
            return searchForIDs("GO:"+text);
    }

    /**
     * Retrieves the models which are associated to the provided Taxonomy text.
     * @param text free (Taxonomy based) text
     * @return list of models identifiers
     * @throws BioModelsWSException
     */
    public static String[] getModelsIdByTaxonomy(String text)
    {
            return searchForIDs("TAXONOMY:"+text);
    }




    /*
     * ###################################### private methods ###########################
     */


    public static String[] searchForIDs(String searchParameter){
        RestApiBiomodels call = new RestApiBiomodels();
        call.setFormat("json");
        call.setPath("search?query="+searchParameter);
        String responds = call.fetchData();
        JSONObject data = new JSONObject(responds);
        int range = data.getInt("matches");
        String[] modelIds = new String[range];
        for (int i = 0; i < range; i+=100){
            String offset = "&offset=" + i;
            call.setPath("search?query="+searchParameter+offset);
            String responds2 = call.fetchData();
            JSONObject data2 = new JSONObject(responds2);
            JSONArray models = data2.getJSONArray("models");
            for (int j = i; j < models.length()+i; j++){
                JSONObject getID = new JSONObject(models.get(j-i).toString());
                modelIds[j] = getID.getString("id");
            }
        }
        return modelIds;
    }


}

