package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.biomodels;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * API for establishing a connection to the BioModels database with the functionality of retrieving Simple Models
 * and SBML Models.
 *
 * @author niklas-groene
 * @since 2.8.3
 */
public class RestApiBiomodels {

	/** Base URL of the BioModels web service. */
	private static final String BASE_URL = "https://www.biomodels.org/";

	/** Page size used when paginating through search results. */
	private static final int PAGE_SIZE = 100;

	/** Connection timeout in milliseconds. */
	private static final int TIMEOUT_MS = 30000;

	private HttpURLConnection connection;
	private String path;
	private String format;
	private int status;
	private String filename;

	/**
	 * Performs the HTTP GET request configured via {@link #setPath(String)} and
	 * {@link #setFormat(String)} and returns the response body.
	 *
	 * @return the response body, or an empty string if the request could not be completed
	 */
	public String fetchData() {
		StringBuilder responseContent = new StringBuilder();
		try {
			URL url = new URL(BASE_URL + (path == null ? "" : path));
			connection = (HttpURLConnection) url.openConnection();

			connection.setRequestMethod("GET");
			if (format != null) {
				connection.setRequestProperty("accept", "application/" + format);
			}
			connection.setConnectTimeout(TIMEOUT_MS);
			connection.setReadTimeout(TIMEOUT_MS);
			connection.setInstanceFollowRedirects(true);
			if (filename != null) {
				connection.setRequestProperty("filename", filename);
			}

			status = connection.getResponseCode();

			InputStream stream = (status > 299) ? connection.getErrorStream() : connection.getInputStream();
			if (stream != null) {
				try (BufferedReader reader = new BufferedReader(
						new InputStreamReader(stream, StandardCharsets.UTF_8))) {
					String line;
					while ((line = reader.readLine()) != null) {
						responseContent.append(line);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
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

	/**
	 * Checks whether the BioModels web service is reachable by issuing a lightweight search request.
	 *
	 * @return {@code true} if the service responds with a success status code
	 */
	public static boolean isServiceAvailable() {
		RestApiBiomodels call = new RestApiBiomodels();
		call.setFormat("json");
		call.setPath("search?query=*&numResults=1");
		call.fetchData();
		return call.status >= 200 && call.status < 300;
	}

	/**
	 * Retrieves the SBML form of a model (as a string) given its identifier.
	 *
	 * @param id model identifier (e.g. BIOMD0000000408 or MODEL1201250000)
	 * @return SBML model as a string, or {@code null} if the provided identifier is not valid or the model does not exist
	 */
	public static String getModelSBMLById(String id) {
		RestApiBiomodels call = new RestApiBiomodels();
		call.setPath(id);
		call.setFormat("json");
		String response = call.fetchData();
		if (response.isEmpty() || call.status > 299) {
			return null;
		}

		JSONObject data = new JSONObject(response);
		JSONObject files = data.optJSONObject("files");
		if (files == null) {
			return null;
		}
		JSONArray main = files.optJSONArray("main");
		if (main == null || main.isEmpty()) {
			return null;
		}
		String filename = main.getJSONObject(0).optString("name", null);
		if (filename == null) {
			return null;
		}

		call.setPath("model/download/" + id + "?filename=" + filename);
		call.setFormat("octet-stream");
		String sbml = call.fetchData();
		return (call.status > 299 || sbml.isEmpty()) ? null : sbml;
	}

	/**
	 * Queries the database for a given search expression and builds a {@link SimpleModel} for every match,
	 * transparently paginating through all result pages.
	 *
	 * @param searchParameter the (already URL-encoded) value of the {@code query} parameter
	 * @return a list of all simple models matching the request (never {@code null})
	 */
	public static List<SimpleModel> searchForModels(String searchParameter) {
		ArrayList<SimpleModel> simpleModels = new ArrayList<>();

		RestApiBiomodels call = new RestApiBiomodels();
		call.setFormat("json");

		int offset = 0;
		int matches = Integer.MAX_VALUE;
		while (offset < matches) {
			call.setPath("search?query=" + searchParameter + "&numResults=" + PAGE_SIZE + "&offset=" + offset);
			String response = call.fetchData();
			if (response.isEmpty() || call.status > 299) {
				break;
			}

			JSONObject data = new JSONObject(response);
			matches = data.optInt("matches", 0);
			JSONArray models = data.optJSONArray("models");
			if (models == null || models.isEmpty()) {
				break;
			}

			for (int i = 0; i < models.length(); i++) {
				JSONObject info = models.getJSONObject(i);
				String id = info.optString("id", null);
				if (id == null) {
					continue;
				}
				String name = info.optString("name", "(unnamed model)");
				String submitter = info.optString("submitter", "");
				// search results expose submissionDate; lastModified is often null
				String modified = info.optString("lastModified", null);
				if (modified == null || modified.isEmpty()) {
					modified = info.optString("submissionDate", null);
				}
				String format = info.optString("format", "");

				simpleModels.add(new SimpleModel(id, name, submitter, modified, format));
			}

			offset += models.length();
		}
		return simpleModels;
	}
}
