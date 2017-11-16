package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.metacrop;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.io.resources.IOurl;

import sun.net.www.protocol.http.HttpURLConnection;

public class WebDirectoryFileListAccess {
	
	public static Collection<PathwayWebLinkItem> getMetaCropListItems() throws IOException {
		ArrayList<PathwayWebLinkItem> result = new ArrayList<PathwayWebLinkItem>();
		String address = "http://kim25.wwwdns.kim.uni-konstanz.de/vanted/addons/metacrop/";
		String pref = address;
		
		// Create a URL for the desired page
		URL url = new URL(address);
		
		// Read all the text returned by the server
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		String str;
		while ((str = in.readLine()) != null) {
			// str is one line of text; readLine() strips the newline character(s)
			String a = "<a href=\"";
			String b = "\">";
			String c1 = ".gml";
			String c2 = ".graphml";
			if (!str.contains(a) || !str.substring(str.indexOf(a)).contains(b) || (!str.contains(c1) && !str.contains(c2)))
				continue;
			String fileName = str.substring(str.indexOf(a) + a.length());
			fileName = fileName.substring(0, fileName.indexOf(b));
			result.add(new PathwayWebLinkItem(fileName, new IOurl(pref + fileName)));
		}
		in.close();
		
		return result;
	}
	
	public static Collection<PathwayWebLinkItem> getWebDirectoryFileListItems(String webAddress,
						String[] validExtensions, boolean showGraphExtensions) throws IOException {
		ArrayList<PathwayWebLinkItem> result = new ArrayList<PathwayWebLinkItem>();
		String address = webAddress;
		String pref = address;
		
		// Create a URL for the desired page
		URL url = new URL(address);
		HttpURLConnection connection = null;
		BufferedReader in = null;
		try {
			connection = (HttpURLConnection) url.openConnection();
			int code = connection.getResponseCode();
			InputStream stream = connection.getErrorStream();

			//There has been connection error, build user notification
			if (stream != null) {
				in = new BufferedReader(new InputStreamReader(stream));
				String message = "";
				String s = null;
				while ((s = in.readLine()) != null)
					message += s;
				//determine type of error
				if (code > 499)
					s = code + " Server Error";
				else if (code > 399)
					s = code + " Client Error";
				else
					s = code + " Connection Error";
				//report error to the user
				MainFrame.showMessageDialog(message.substring(message.indexOf("<html>")), s);
				//lastly, close any open streams, connections
				stream.close();
				connection.disconnect();
				
				return null;
			}
			
			stream = connection.getInputStream();
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
				PathwayWebLinkItem pwl = new PathwayWebLinkItem(fileName, new IOurl(pref + fileName), showGraphExtensions);
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
