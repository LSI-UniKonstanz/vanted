package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.biomodels;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * API for establishing a connection to the Biomodels database with the functionality of retrieving Simple Models
 * and SBML Models.
 * @author niklas-groene
 * @since 2.8.3
 *
 */

public class RestApiBiomodels
{

    private static HttpURLConnection connection;
    public String path;
    public String format;
    public int status;
    public String filename;

    public String fetchData()
    {

        StringBuilder responseContent = null;
        try
        {
            BufferedReader reader;
            String line;
            responseContent = new StringBuilder();
            URL url = new URL("https://www.ebi.ac.uk/biomodels/" + path);
            connection = (HttpURLConnection) url.openConnection();

            //Request setup
            connection.setRequestMethod("GET");
            connection.setRequestProperty("accept", "application/"+format);
            connection.setConnectTimeout(100000); //in ms
            connection.setReadTimeout(100000); //in ms
            if (filename != null)
            {
                connection.setRequestProperty("filename",filename);
            }

            status = connection.getResponseCode();

            if (status > 299)
            {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                while ((line = reader.readLine()) != null)
                {
                    responseContent.append(line);
                }
                reader.close();
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line = reader.readLine()) != null)
                {
                    responseContent.append(line);
                }
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {
            connection.disconnect();
        }
        return responseContent.toString();
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public void setFormat(String format)
    {
        this.format = format;
    }

    public void setFilename(String filename)
    {
        this.filename = filename;
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
     * Calls to the database, for a given HTTP request and creates a Simple Model of all models found in the Database.
     * @param searchParameter part of the HTTP request for searching the right Models
     * @return Returns a List of all Simple Models associated with the request.
     */
    public static List<SimpleModel> searchForModels(String searchParameter){
        RestApiBiomodels call = new RestApiBiomodels();
        call.setFormat("json");
        call.setPath("search?query="+searchParameter);
        String responds = call.fetchData();
        JSONObject data = new JSONObject(responds);
        int range = data.getInt("matches");
        System.out.println(range);
        ArrayList<SimpleModel> simpleModels = new ArrayList<>();
        for (int i = 0; i < range; i+=100){
            if (i != 0)
            {
                String offset = "&offset=" + i;
                call.setPath("search?query="+searchParameter+offset);
                responds = call.fetchData();
                data = new JSONObject(responds);
            }
            JSONArray models = data.getJSONArray("models");
            for (int j = i; j < models.length()+i; j++)
            {
                SimpleModel model;
                JSONObject getInformation = new JSONObject(models.get(j-i).toString());
                String iD = getInformation.getString("id");
                String lastModified = getInformation.getString("lastModified");
                String name = getInformation.getString("name");
                String submitter = getInformation.getString("submitter");

                model = new SimpleModel(
                        iD,
                        name,
                        submitter,
                        lastModified
                );
                simpleModels.add(model);
            }
        }
        return simpleModels;
    }
}