package org.vanted.updater;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.graffiti.editor.MainFrame;

/**
 * Handles automatic redirects from http to https.
 * 
 * @author Dimitar
 * @since 2.7.0
 */
public class HttpHttpsURL {

	private String spec;

	public HttpHttpsURL(String spec) {
		this.setURL(spec);
	}

	/**
	 * This returns the URL's spec. Use it to initialize a {@linkplain URL} object.
	 * @return URL's spec
	 */
	public String getURL() {
		return spec;
	}

	/**
	 * Set URL's spec.
	 * 
	 * @param spec
	 */
	public void setURL(String spec) {
		this.spec = spec;
	}

	/**
	 * This method is a shorthand for:
	 * <p>
	 * <code>openConnection().getInputStream()</code>
	 * </p>
	 * with support for Http-Https redirect.
	 * 
	 * @return
	 * @throws IOException
	 */
	public InputStream openStream() throws IOException {
		URL url = new URL(spec);
		String protocol = url.getProtocol().toLowerCase();
		if (protocol.equals("http") || protocol.equals("https"))
			return openConnection().getInputStream();
		else
			return url.openStream();
	}

	/**
	 * Use this method to open connections with both Http and Https protocols. Also
	 * any errors are therewith reported to the user.
	 * 
	 * @return a successfully opened connection
	 * @throws IOException
	 */
	public HttpURLConnection openConnection() throws IOException {
		URL url = new URL(spec);
		HttpURLConnection connection = null;
		BufferedReader errorReader = null;
		try {
			connection = (HttpURLConnection) url.openConnection();
			int status = connection.getResponseCode();
			if (status == HttpURLConnection.HTTP_OK)
				return connection;

			if (status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_MOVED_TEMP
					|| status == HttpURLConnection.HTTP_SEE_OTHER) {
				String redirectedUrl = connection.getHeaderField("Location");
				// update spec
				setURL(redirectedUrl);
				url = new URL(redirectedUrl);
				connection = (HttpURLConnection) url.openConnection();
			}

			// There has been connection error, build user notification
			InputStream errorStream = connection.getErrorStream();
			String message = "";
			String s = null;
			if (errorStream != null) {
				errorReader = new BufferedReader(new InputStreamReader(errorStream));

				while ((s = errorReader.readLine()) != null)
					message += s;
				// determine type of error
				if (status > 499)
					s = status + " Server Error";
				else if (status > 399)
					s = status + " Client Error";
				else
					s = status + " Connection Error";
				// report error to the user
				MainFrame.showMessageDialog(message.substring(message.indexOf("<html>")), s);
				// lastly, close any open streams, connections
				errorStream.close();
				connection.disconnect();

				return null;
			}
		} finally {
			if (errorReader != null)
				errorReader.close();
			if (connection != null && connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				connection.disconnect();
			}
		}

		return connection;
	}

}
