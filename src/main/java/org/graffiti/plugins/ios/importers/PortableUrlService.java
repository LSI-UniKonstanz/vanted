package org.graffiti.plugins.ios.importers;

import java.io.File;

import org.AttributeHelper;
import org.ErrorMsg;
import org.graffiti.attributes.Attributable;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Node;


/**
 * A service-class that increases portability of rich-attribute graph across
 * platforms. The axis of modification is Unix-Windows systems, due to their
 * inherently different Url separation.
 *  
 * @author Dimitar Garkov
 *
 */
public class PortableUrlService {

	private PortableUrlService() {}
	
	private static final String SEPARATOR = System.getProperty("file.separator");
	private static final String WINDOWS_SEP = "\\";
	private static final String UNIX_SEP = "/";
	
	private static String graphPath = "";
	
	public static void processUrlAttributes(Attributable ge) {
		if (ge instanceof Node) {
			processImageUrlAttribute((Node) ge);
		} else {
			
		}
	}

	/**
	 * It applies some processing steps that enhance portability on image URL
	 * attribute.
	 * @param node to be tested and subsequently modified in terms of its
	 * image_url attribute.
	 */
	private static void processImageUrlAttribute(Node node) {
		if (!AttributeHelper.hasAttribute(node, "image", "image_url"))
			return;
		
		try {
			String imageUrl = (String) AttributeHelper.getAttributeValue(node, "image", "image_url", "", "");
			if (!imageUrl.equals("")) {
				imageUrl = fixUrl(imageUrl);
				AttributeHelper.setAttribute(node, "image", "image_url", imageUrl);
				checkUrlExistance(imageUrl, "image_url");
			}
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
	}
	
	/**
	 * Fixes any faulty separators in URL. Such could exist after porting
	 * graphs to OS on the axis Unix-Windows, since the actual URL are 
	 * written down.
	 * 
	 * @param url to fix
	 * @return the now separator-fixed URL
	 */
	private static String fixUrl(String url) {
		if (!url.contains(SEPARATOR)) {
			String sep = SEPARATOR.equals(UNIX_SEP) 
					// double to escape it within RegEx 
					? WINDOWS_SEP+WINDOWS_SEP 
					: UNIX_SEP;
			return url.replaceAll(sep, SEPARATOR);
			
		}
		
		return url;
	}
	
	/**
	 * Checks whether an URL, specified in the graph's source file, exists.
	 * If not, it announces that to the user, so the s/he could check it out.
	 * It checks for both, relative (according to graph's path) and absolute
	 * paths.
	 * 
	 * @param url to check on
	 * @param attributeName attribute name, for information purposes
	 */
	private static void checkUrlExistance(String url, String attributeName) {
		String attr = "<code>" + attributeName + "</code>";
		String gPath = getGraphPath();
		int lastSepIndex = gPath.lastIndexOf(File.separatorChar);
		boolean sepFlag = false;
		if (lastSepIndex > 0)
			gPath = gPath.substring(0, lastSepIndex);
		//graphPath does not end with separator (after our little pre-processing)
		//so, make sure there is at least one between the path and the url
		if (!url.startsWith(String.valueOf(File.separatorChar))) {
			url = File.separatorChar + url;
			sepFlag = true;
		}
		
		if (!new File(url).exists() && !new File(gPath + url).exists())
			MainFrame.showMessageDialog("<html>The resource for attribute " + attr
					+ " could not be found. Please, edit your graph file.<br/> <br/>"
					+ "Faulty " + attr + "'s value &#10148; "
					+ (sepFlag ? url.substring(1) : url), "Check your graph");
	}
	
	/**
	 * Set the path of, presumably, currently loading graph.
	 * 
	 * @param path
	 */
	public static void setGraphPath(String path) {
		graphPath = path;
	}
	
	/**
	 * @return previously set path or empty string otherwise
	 */
	public static String getGraphPath() {
		return graphPath;
	}

}
