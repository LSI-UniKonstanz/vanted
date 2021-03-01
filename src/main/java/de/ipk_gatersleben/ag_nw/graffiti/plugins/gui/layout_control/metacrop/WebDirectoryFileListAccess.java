package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.metacrop;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;

import org.graffiti.plugin.io.resources.IOurl;
import org.vanted.updater.HttpHttpsURL;

public class WebDirectoryFileListAccess {
	
	/**
	 * @return
	 * @throws IOException
	 * @vanted.revision 2.7.0
	 */
	public static Collection<PathwayWebLinkItem> getMetaCropListItems() throws IOException {
		ArrayList<PathwayWebLinkItem> result = new ArrayList<PathwayWebLinkItem>();
		String address = "http://kim25.wwwdns.kim.uni-konstanz.de/vanted/addons/metacrop/";
		// Read all the text returned by the server
		HttpHttpsURL url = new HttpHttpsURL(address);
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		String pref = url.getURL();
		String str;
		while ((str = in.readLine()) != null) {
			// str is one line of text; readLine() strips the newline character(s)
			String a = "<a href=\"";
			String b = "\">";
			String c1 = ".gml";
			String c2 = ".graphml";
			if (!str.contains(a) || !str.substring(str.indexOf(a)).contains(b)
					|| (!str.contains(c1) && !str.contains(c2)))
				continue;
			String fileName = str.substring(str.indexOf(a) + a.length());
			fileName = fileName.substring(0, fileName.indexOf(b));
			result.add(new PathwayWebLinkItem(fileName, new IOurl(pref + fileName)));
		}
		in.close();
		
		return result;
	}
	
	/**
	 * @param webAddress
	 * @param validExtensions
	 * @param showGraphExtensions
	 * @return
	 * @throws IOException
	 * @vanted.revision 2.7.0
	 */
	public static Collection<PathwayWebLinkItem> getWebDirectoryFileListItems(String webAddress,
			String[] validExtensions, boolean showGraphExtensions) throws IOException {
		ArrayList<PathwayWebLinkItem> result = new ArrayList<PathwayWebLinkItem>();
		String address = webAddress;
		HttpURLConnection connection = null;
		BufferedReader in = null;
		try {
			// Create a URL for the desired page
			HttpHttpsURL url = new HttpHttpsURL(address);
			connection = url.openConnection();
			if (connection == null)
				return null;
			String pref = url.getURL();
			InputStream stream = connection.getInputStream();
			// Read all the text returned by the server
			in = new BufferedReader(new InputStreamReader(stream));
			String str;
			while ((str = in.readLine()) != null) {
				// str is one line of text; readLine() strips the newline character(s)
				String a = "<a href=\"";
				String b = "\">";
				boolean containsExtension = false;
				for (String ext : validExtensions)
					if (str.contains(ext + "\">")) {
						containsExtension = true;
						break;
					}
				if (!str.contains(a) || !str.substring(str.indexOf(a)).contains(b) || !containsExtension)
					continue;
				String fileName = str.substring(str.indexOf(a) + a.length());
				fileName = fileName.substring(0, fileName.indexOf(b));
				PathwayWebLinkItem pwl = new PathwayWebLinkItem(fileName, new IOurl(pref + fileName),
						showGraphExtensions);
				result.add(pwl);
			}
		} finally {
			if (in != null)
				in.close();
			if (connection != null)
				connection.disconnect();
		}
		
		return result;
	}
}
