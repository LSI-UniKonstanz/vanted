package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.biomodels;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class RestApiBiomodels {

    private static HttpURLConnection connection;
    public String path;
    public String format;
    public int status;
    public String filename;



    public String fetchData() {

        StringBuilder responseContent = null;
        try {
            BufferedReader reader;
            String line;
            responseContent = new StringBuilder();
            URL url = new URL("https://www.ebi.ac.uk/biomodels/" + path);
            connection = (HttpURLConnection) url.openConnection();

            //Request setup
            connection.setRequestMethod("GET");
            connection.setRequestProperty("accept", "application/"+format);
            connection.setConnectTimeout(5000); //in ms
            connection.setReadTimeout(5000); //in ms
            if (filename != null){
                connection.setRequestProperty("filename",filename);
            }

            status = connection.getResponseCode();

            if (status > 299) {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                while ((line = reader.readLine()) != null) {
                    responseContent.append(line);
                }
                reader.close();
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    responseContent.append(line);
                }
            }
        } catch (
                IOException e) {
            e.printStackTrace();
        } finally {
            connection.disconnect();
        }
        return responseContent.toString();
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}