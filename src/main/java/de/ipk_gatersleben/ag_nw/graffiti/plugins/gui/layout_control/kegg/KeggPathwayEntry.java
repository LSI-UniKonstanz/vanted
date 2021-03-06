/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 24.11.2003
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.ErrorMsg;
import org.StringManipulationTools;
import org.Vector2d;

/**
 * @author Christian Klukas To change the template for this generated type
 *         comment go to Window>Preferences>Java>Code Generation>Code and
 *         Comments
 */
public class KeggPathwayEntry implements Comparable<Object> {
	private URL pathwayURL;
	
	private String pathwayName;
	private String mapName;
	
	private boolean stripOrganismName;
	private boolean colorEnzymesAndUseReferencePathway;
	private String mappingCount = "";
	
	private String[] group;
	
	private InputStream openInputStream = null;
	
	private Vector2d targetPosition;
	
	public KeggPathwayEntry(String name, boolean stripOrganismName, String mapName, String[] group) {
		setPathwayName(name.trim());
		setStripOrganismName(stripOrganismName);
		setMapName(mapName);
		setGroupName(group);
	}
	
	public KeggPathwayEntry(InputStream inputStream) {
		this.openInputStream = inputStream;
	}
	
	public KeggPathwayEntry(KeggPathwayEntry copyThisEntry, boolean colorEnzymesAndUseReferencePathway) {
		this(copyThisEntry.getPathwayName(), copyThisEntry.isStripOrganismName(), copyThisEntry.getMapName(),
				copyThisEntry.getGroupName());
		setColorEnzymesAndUseReferencePathway(colorEnzymesAndUseReferencePathway);
	}
	
	public String[] getGroupName() {
		return group;
	}
	
	public void setGroupName(String[] group) {
		this.group = group;
	}
	
	public String getPathwayURLstring() {
		if (pathwayURL == null)
			return null;
		// if (!FileDownloadCache.isCacheURL(pathwayURL))
		// pathwayURL = FileDownloadCache.getCacheURL(pathwayURL, mapName);
		
		if (pathwayURL == null)
			return null;
		else
			return pathwayURL.toString();
	}
	
	public URL getPathwayURL() {
		return getPathwayURL(false);
	}
	
	public URL getPathwayURL(boolean useReferencePathwayURL) {
		if (useReferencePathwayURL)
			return getReferencePathwayURLfromURL(pathwayURL);
		else {
			if (!FileDownloadCache.isCacheURL(pathwayURL))
				pathwayURL = FileDownloadCache.getCacheURL(pathwayURL, mapName);
			
			return pathwayURL;
		}
	}
	
	// public Object getPathwayURLstring(boolean returnOrganismSpecificURL) {
	// if (returnOrganismSpecificURL) {
	// if (!FileDownloadCache.isCacheURL(pathwayURL))
	// pathwayURL = FileDownloadCache.getCacheURL(pathwayURL, mapName);
	// return pathwayURL;
	// }
	// else
	// return getPathwayURL();
	// }
	
	private URL getReferencePathwayURLfromURL(URL pathwayURL) {
		try {
			String organismName3letters = mapName.substring(0, 3);
			String file = pathwayURL.getFile();
			file = StringManipulationTools.stringReplace(file, "/" + organismName3letters + "/" + organismName3letters,
					"/map/map");
			URL url = new URL(pathwayURL.getProtocol(), pathwayURL.getHost(), pathwayURL.getPort(), file);
			if (!FileDownloadCache.isCacheURL(url))
				url = FileDownloadCache.getCacheURL(url, mapName);
			return url;
		} catch (MalformedURLException e) {
			ErrorMsg.addErrorMessage(e);
			return null;
		}
	}
	
	@Override
	public String toString() {
		String tempPathwayName = getPathwayName();
		if (isStripOrganismName() && tempPathwayName.lastIndexOf(" -") > 0 && tempPathwayName.length() > 0)
			tempPathwayName = tempPathwayName.substring(0, tempPathwayName.lastIndexOf(" -"));
		if (mappingCount.equalsIgnoreCase(""))
			return tempPathwayName + "                         ";
		else
			return tempPathwayName + " (" + mappingCount + ")";
	}
	
	public void setMappingCount(String mappingCount) {
		this.mappingCount = mappingCount;
	}
	
	public InputStream getOpenInputStream() throws IOException {
		if (openInputStream != null)
			return openInputStream;
		else {
			if (!isColorEnzymesAndUseReferencePathway()) {
				URL url = getPathwayURL();
				if (url != null)
					return url.openStream();
				else
					return null;
			} else
				return getPathwayURL(true).openStream();
		}
	}
	
	/**
	 * @param string
	 * @return
	 */
	public String getMappingCountDescription(String pre) {
		if (mappingCount != null && mappingCount.length() > 0)
			return pre + mappingCount;
		else
			return "";
	}
	
	public void setPathwayName(String pathwayName) {
		this.pathwayName = pathwayName;
	}
	
	public String getPathwayName() {
		return pathwayName;
	}
	
	public void setMapName(String mapName) {
		this.mapName = mapName;
	}
	
	public String getMapName() {
		return mapName;
	}
	
	private void setStripOrganismName(boolean stripOrganismName) {
		this.stripOrganismName = stripOrganismName;
	}
	
	private boolean isStripOrganismName() {
		return stripOrganismName;
	}
	
	public void setColorEnzymesAndUseReferencePathway(boolean colorEnzymesAndUseReferencePathway) {
		this.colorEnzymesAndUseReferencePathway = colorEnzymesAndUseReferencePathway;
	}
	
	public boolean isColorEnzymesAndUseReferencePathway() {
		return colorEnzymesAndUseReferencePathway;
	}
	
	public boolean isReferencePathway() {
		String organismName3letters = getOrganismLetters();
		return organismName3letters.equalsIgnoreCase("map") || organismName3letters.equalsIgnoreCase("ko");
	}
	
	public String getOrganismLetters() {
		return getOrganismLettersFromMapId(mapName);
	}
	
	public static String getOrganismLettersFromMapId(String mapName) {
		String id = mapName;
		if (id.length() >= 2) {
			char[] name = id.toCharArray();
			int lastDigit = id.length() - 1;
			while (lastDigit >= 0 && Character.isDigit(name[lastDigit]))
				lastDigit--;
			if (!Character.isDigit(name[lastDigit]))
				lastDigit++;
			if (lastDigit < id.length())
				id = id.substring(lastDigit);
			else
				id = id.substring(0, lastDigit);
		}
		return id;
	}
	
	public Vector2d getTargetPosition() {
		return targetPosition;
	}
	
	public void setTargetPosition(Vector2d targetPosition) {
		this.targetPosition = targetPosition;
	}
	
	public int compareTo(Object o) {
		KeggPathwayEntry kpe = (KeggPathwayEntry) o;
		if (getOrganismLetters().equals("map") && kpe.getOrganismLetters().equals("ko"))
			return -1;
		if (getOrganismLetters().equals("ko") && kpe.getOrganismLetters().equals("map"))
			return 1;
		if (getOrganismLetters().equals("map") && !kpe.getOrganismLetters().equals("map"))
			return -1;
		if (getOrganismLetters().equals("ko") && !kpe.getOrganismLetters().equals("ko"))
			return -1;
		return getMapName().compareTo(kpe.getMapName());
	}
}
