package de.ipk_gatersleben.ag_nw.graffiti.services.web;

import java.util.ArrayList;

import javax.ws.rs.core.MediaType;

import org.ErrorMsg;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;

/**
 * Klasse zum Auslesen von Rest-Resourcen.
 * 
 * @author Torsten Vogt TODO: Alle anderen Zugriffe als dem Lesezugriff.
 */
public class RestService {

	Client client;

	String baseURL;
	ArrayList<String> path;

	Object response;

	public RestService() {
		this(null, new ArrayList<String>());
	}

	public RestService(String webResource) {
		this(webResource, new ArrayList<String>());
	}

	public RestService(String baseUrl, String path) {
		this.baseURL = baseUrl;
		this.path = new ArrayList<String>();
		this.path.add(path);
		client = Client.create();
		response = null;
	}

	public RestService(String baseUrl, ArrayList<String> path) {
		this.baseURL = baseUrl;
		this.path = path;
		client = Client.create();
		response = null;
	}

	/**
	 * 
	 * @param baseUrl
	 *            Absoluter Pfad zu einer Webresource (Bsp.: "http://rest.kegg.jp/")
	 */
	public void setBaseUrl(String baseUrl) {
		this.baseURL = baseUrl;
	}

	public String getBaseUrl() {
		return baseURL;
	}

	/**
	 * 
	 * @param path
	 *            Pfad ab der webRessource (Bsp.: "list/" ). Kann auch l�nger als
	 *            ein Verzeichnis sein.
	 */
	public void setPath(String path) {
		this.path.clear();
		this.path.add(path);
	}

	/**
	 * 
	 * @param path
	 *            Pfad ab der baseUrl. Array wird der Reihe nach ausgelesen (Bsp.;
	 *            "kegg/" | "rest/" | ...).
	 */
	public void setPath(ArrayList<String> path) {
		this.path = path;
	}

	/**
	 * 
	 * @param path
	 *            Bisheriger Verzeichnispfad wird verl�ngert.
	 */
	public void addPath(String path) {
		this.path.add(path);
	}

	public String getPath() {
		String returnString = "";
		for (String s : path)
			returnString += s;

		return returnString;
	}

	/**
	 * Gibt den gesamten Path zur�ck
	 * 
	 * @return Feld 0 ist die baseUrl nachfolgend die Eintr�ge des paths
	 */
	public ArrayList<String> getPathArray() {
		ArrayList<String> al = new ArrayList<String>();
		al.add(baseURL);
		al.addAll(path);
		return al;
	}

	/**
	 * L�dt die Resource die �ber baseUrl und path spezifiziert wurden. F�r mehrere
	 * Anfragen sinnvoll.
	 * 
	 * @param pathAddition
	 *            Bei multiplen Anfragen an eine Adresse, nur den letzten Teil des
	 *            Pfads angeben. (ansonsten leeren String)
	 * @param mediaType
	 *            MediaType der beim Download akzeptiert wird (Bsp.:
	 *            MediaType.TEXT_PLAIN)
	 * @param cl
	 *            zu erwartende Klasse der Resource (Bsp.: String.class)
	 * @return Gibt Download als Object zur�ck (null, bei Fehlern)
	 */
	public Object makeRequest(String pathAddition, MediaType mediaType, Class<?> cl) {
		WebResource wr = client.resource(baseURL);
		for (String s : path)
			wr = wr.path(s);
		wr = wr.path(pathAddition);
		Builder builder = wr.accept(mediaType);
		ClientResponse clientResponse = null;
		try {
			clientResponse = builder.get(ClientResponse.class);
			if (clientResponse.getStatus() == 200 && clientResponse.hasEntity()) {
				response = clientResponse.getEntity(cl);
			} else {
				resourceNotFoundErrorMessage();
				response = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return response;
	}

	/**
	 * L�dt die Resource die �ber baseUrl spezifiziert wurden. F�r einzelne Anfragen
	 * sinnvoll.
	 * 
	 * @param baseUrl
	 *            Absoluter Pfad zu einer Webresource (Bsp.: "http://rest.kegg.jp/")
	 * @param mediaType
	 *            MediaType der beim Download akzeptiert wird (Bsp.:
	 *            MediaType.TEXT_PLAIN)
	 * @param cl
	 *            zu erwartende Klasse der Resource (Bsp.: String.class)
	 * @return Gibt Download als Object zur�ck (null, bei Fehlern)
	 */
	static public Object makeRequest(String baseUrl, String mediaType, Class<?> cl) {
		ArrayList<String> path = new ArrayList<String>();
		return makeRequest(baseUrl, path, mediaType, cl);
	}

	/**
	 * L�dt die Resource die �ber baseUrl und path spezifiziert wurden. F�r einzelne
	 * Anfragen sinnvoll.
	 * 
	 * @param baseUrl
	 *            Absoluter Pfad zu einer Webresource (Bsp.: "http://rest.kegg.jp/")
	 * @param path
	 *            Pfad ab der baseUrl (Bsp.: "list/" ). Kann auch l�nger als ein
	 *            Verzeichnis sein.
	 * @param mediaType
	 *            MediaType der beim Download akzeptiert wird (Bsp.:
	 *            MediaType.TEXT_PLAIN)
	 * @param cl
	 *            zu erwartende Klasse der Resource (Bsp.: String.class)
	 * @return Gibt Download als Object zur�ck (null, bei Fehlern)
	 */
	static public Object makeRequest(String baseUrl, ArrayList<String> path, String mediaType, Class<?> cl) {
		Client client = Client.create();
		Object response = null;
		WebResource wr = client.resource(baseUrl);
		for (String s : path)
			wr = wr.path(s);
		Builder builder = wr.accept(mediaType);
		ClientResponse clientResponse = null;
		try {
			clientResponse = builder.get(ClientResponse.class);
			if (clientResponse.getStatus() == 200 && clientResponse.hasEntity()) {
				response = clientResponse.getEntity(cl);
			} else {
				resourceNotFoundErrorMessage();
			}
		} catch (Exception e) {
			resourceNotFoundErrorMessage();
		}

		client.destroy();
		return response;

	}

	static private void resourceNotFoundErrorMessage() {
		ErrorMsg.addErrorMessage("Webresouce not found. The URL may be invalid. Please check your connection.");
	}

}
