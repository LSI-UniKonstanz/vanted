/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.cluster_colors;

import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.Colors;
import org.graffiti.attributes.StringAttribute;
import org.graffiti.event.AttributeEvent;

/**
 * All StringAttributes with the name id "chart_colors" will be converted
 * into a ChartColorAttribute (see ChartAttributePlugin, where the mapping
 * information from id to class type is initialized.
 * 
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class ClusterColorAttribute extends StringAttribute {
	/**
	 * 
	 */
	private static final int INDEX_CLUSTEROUTLINECOLOR = 1;
	/**
	 * 
	 */
	private static final int INDEX_CLUSTERCOLOR = 0;
	// format of color string:
	// color-bar1:outline-bar1;color-bar2:outline-bar2;col3:outline3 ...
	private String value;
	public static String attributeName = "cluster_colors";
	public static String attributeFolder = "";
	public static String desc = "<html>Modify the color (fill/first row and outline/second row)<br>of the nodes with cluster information and the color of the nodes in the cluster-graph";
	
	private String notSet = "undefined"; // "0,0,0,255:255,255,255,255;255,0,0,255:0,255,255,255;50,50,0,255:255,55,55,255";
	
	private ArrayList<String> listClusterNames;
	private ArrayList<Color> listClusterColors;
	
	private ArrayList<Color> listOutlineColors;
	
	public ClusterColorAttribute() {
		super(attributeName);
		init();
	}
	
	public ClusterColorAttribute(String id) {
		super(id);
		init();
	}
	
	public ClusterColorAttribute(String id, String value) {
		super(id);
		init();
		this.value = value;
		fillColorFields();
		
	}
	
	private void init() {
		setDescription(desc); // tooltip
		setDefaultValue();
	}
	
	@Override
	public void setDefaultValue() {
		value = notSet;
		listClusterColors = new ArrayList<Color>();
		listClusterNames = new ArrayList<String>();
		listOutlineColors = new ArrayList<Color>();
		
	}
	
	@Override
	public void setString(String value) {
		assert value != null;
		
		AttributeEvent ae = new AttributeEvent(this);
		callPreAttributeChanged(ae);
		
		this.value = value;
		fillColorFields();
		
		callPostAttributeChanged(ae);
	}
	
	@Override
	public String getString() {
		convertColorArrayToString();
		return value;
	}
	
	@Override
	public Object getValue() {
		convertColorArrayToString();
		return value;
	}
	
	@Override
	public Object copy() {
		convertColorArrayToString();
		return new ClusterColorAttribute(this.getId(), this.value);
	}
	
	@Override
	public String toString(int n) {
		convertColorArrayToString();
		return getSpaces(n) + getId() + " = \"" + value + "\"";
	}
	
	@Override
	public String toXMLString() {
		convertColorArrayToString();
		return getStandardXML(value);
	}
	
	public ArrayList<Color> getClusterColors() {
		return listClusterColors;
	}
	
	public ArrayList<Color> getClusterOutlineColors() {
		return listOutlineColors;
	}
	
	public Color getClusterColor(int clusterID) {
		return listClusterColors.get(clusterID);
	}
	
	public Color getClusterOutlineColor(int clusterID) {
		return listOutlineColors.get(clusterID);
	}
	
	/**
	 * That method must be called when the value is set.
	 * The value is a String object and needs to be parsed and
	 * put into the designated lists that keep the colors
	 * Those colors are not yet connected to clusters, since the original
	 * implementation only used a simple index, for which cluster color to use.
	 */
	private void fillColorFields() {
		listClusterColors = interpreteColorString(INDEX_CLUSTERCOLOR);
		listOutlineColors = interpreteColorString(INDEX_CLUSTEROUTLINECOLOR);
	}
	
	private void convertColorArrayToString() {
		value = notSet;
		ensureMinimumColorSelection(listClusterColors.size());
		
		for (int clusterID = 0; clusterID < listClusterColors.size(); clusterID++) {
			setColorString(INDEX_CLUSTERCOLOR, clusterID, listClusterColors.get(clusterID));
			setColorString(INDEX_CLUSTEROUTLINECOLOR, clusterID, listOutlineColors.get(clusterID));
		}
	}
	
	private ArrayList<Color> interpreteColorString(int type0bar_1outline) {
		if (value == null || value.equals(notSet))
			return null;
		else {
			String[] cols = value.split(";");
			ArrayList<Color> result = new ArrayList<Color>();
			for (int i = 0; i < cols.length; i++) {
				String barCol_outCol = cols[i];
				Color p;
				if (barCol_outCol.length() == 0)
					p = null;
				else {
					String colComp = barCol_outCol.split(":")[type0bar_1outline];
					if (colComp.equals("null") || colComp.equals(notSet))
						p = null;
					else {
						String[] rgba_s = colComp.split(",");
						int[] rgba = new int[rgba_s.length];
						for (int ir = 0; ir < rgba.length; ir++)
							rgba[ir] = Integer.parseInt(rgba_s[ir]);
						p = new Color(rgba[0], rgba[1], rgba[2], rgba[3]);
					}
				}
				if (p == null) {
					Color[] defCols = Colors.getColors(cols.length);
					if (type0bar_1outline == 0)
						p = defCols[i];
					else
						p = Color.BLACK;
				}
				result.add(p);
			}
			return result;
		}
	}
	
	private void setColorString(int idx0bar_1outline, int series, Paint newColor) {
		String[] cols = value.split(";");
		
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < cols.length; i++) {
			if (i != series) {
				if (result.length() > 0)
					result.append(";" + cols[i]);
				else
					result.append(cols[i]);
			} else {
				String barCol_outCol = cols[series];
				if (result.length() > 0)
					result.append(";");
				if (idx0bar_1outline == 0)
					result.append(getColorCode(newColor) + ":" + nullForEmpty(barCol_outCol.split(":")[1]));
				else
					result.append(nullForEmpty(barCol_outCol.split(":")[0]) + ":" + getColorCode(newColor));
			}
		}
		value = result.toString();
		// setValue(result);
	}
	
	/**
	 * @param string
	 * @return
	 */
	private String nullForEmpty(String string) {
		if (string == null || string.length() == 0)
			return "null";
		else
			return string;
	}
	
	private String getColorCode(Paint newColor) {
		if (newColor == null)
			return "null";
		else {
			if (newColor instanceof Color) {
				Color c = (Color) newColor;
				return c.getRed() + "," + c.getGreen() + "," + c.getBlue() + "," + c.getAlpha();
			} else
				return "null";
		}
	}
	
	private void ensureMinimumColorSelection(int clusterCount) {
		if (value.equals(notSet)) {
			value = "null:null";
		}
		while (value.length() - value.replaceAll(";", "").length() < clusterCount - 1) {
			value = value + ";null:null";
		}
		
	}
	
	/**
	 * Must be called on an existing ClusterColorAttribute to update the cluster information.
	 * Existing clusters not found in clusterNames will be deleted
	 * clusterNames not found in the Attribute Object will be added
	 * Nothing is changed, if the clusterlists are the same
	 * 
	 * @param clusterNames
	 */
	public void updateClusterList(Collection<String> clusterNames)
	{
		if (clusterNames == null || clusterNames.isEmpty()) {
			setDefaultValue();
			return;
		}
		
		/*
		 * If this attribute is created, only colors are known, since clusternames
		 * are not stored in GML or when this stringattriute is read.. Compatibiliy
		 * Sooo. we just need to connect names, to already set colors.
		 */
		if (listClusterNames.isEmpty()) {
			/*
			 * if graph is copied or the cluster color attribute was not updated
			 * the number of clustercolors can diverge from the actual present clusters, since
			 * clustercolors is a graph attribute ..
			 * e.g. copying from graph with two clusters.. copy one node (one cluster member)
			 * insert to new graph.. graph attribute still has two colors colors but only one cluster
			 * By calling updateClusterList again with the actuall number of present clusters
			 * the clustercolors graph attribute gets updated.
			 */
			listClusterNames.addAll(clusterNames);
			updateClusterList(clusterNames);
		} else {
			
			Color[] colors = Colors.getColors(clusterNames.size() * 2); //get some colors that won't match the existing ones
			int colorIdx = clusterNames.size() * 2 - 1;
			ArrayList<Color> newListClusterColors = new ArrayList<Color>();
			ArrayList<Color> newListOutlineColors = new ArrayList<Color>();
			ArrayList<String> newListClusterNames = new ArrayList<String>();
			int foundIdx;
			for (String curClusterName : clusterNames) {
				foundIdx = -1;
				for (int i = 0; i < listClusterNames.size(); i++) {
					if (curClusterName.equals(listClusterNames.get(i))) {
						foundIdx = i;
					}
				}
				
				/*
				 * if through copy and paste more cluster than cluster colors are present
				 * an indexoutofboundexception can happen, if we try to set up the lists
				 * See comment above.
				 * In that case, act like this is a new color.
				 */
				if (foundIdx >= listClusterColors.size())
					foundIdx = -1;
				
				if (foundIdx >= 0) {
					newListClusterColors.add(listClusterColors.get(foundIdx));
					newListOutlineColors.add(listOutlineColors.get(foundIdx));
					newListClusterNames.add(listClusterNames.get(foundIdx));
				} else {
					//new color
					newListClusterColors.add(colors[colorIdx]);
					newListOutlineColors.add(colors[colorIdx]);
					newListClusterNames.add(curClusterName);
					colorIdx--;
				}
			}
			
			listClusterColors = newListClusterColors;
			listOutlineColors = newListOutlineColors;
			listClusterNames = newListClusterNames;
		}
	}
	
	@Override
	protected void doSetValue(Object o) throws IllegalArgumentException {
		assert o != null;
		
		try {
			value = (String) o;
			
			fillColorFields();
			
		} catch (ClassCastException cce) {
			throw new IllegalArgumentException("Invalid value type.");
		}
	}
	
	public int getDefinedClusterColorCount() {
		/*
		 * if (value == null || value.equals(notSet))
		 * return 0;
		 * else
		 * return value.length() - value.replaceAll(";", "").length() + 1;
		 */
		return listClusterColors.size();
		
	}
	
	public void setClusterColor(int clusterID, Color color) {
//		ensureMinimumColorSelection(clusterID);
		listClusterColors.remove(clusterID);
		listClusterColors.add(clusterID, color);
//		setColorString(0, clusterID, color);
	}
	
	public void setClusterOutlineColor(int clusterID, Color color) {
//		ensureMinimumColorSelection(clusterID);
		listOutlineColors.remove(clusterID);
		listOutlineColors.add(clusterID, color);
//		setColorString(1, clusterID, color);
	}
	
	public static ClusterColorAttribute getDefaultValue(int clusterSize) {
		ClusterColorAttribute cca = new ClusterColorAttribute(attributeName);
		Color[] defCols = Colors.getColors(clusterSize);
		
		for (int i = 0; i < defCols.length; i++) {
			cca.listClusterColors.add(defCols[i]);
			cca.listOutlineColors.add(Color.BLACK);
		}
		return cca;
	}
	
	public static ClusterColorAttribute getDefaultValue(Collection<String> clusterNames) {
		ClusterColorAttribute cca = new ClusterColorAttribute(attributeName);
//		cca.ensureMinimumColorSelection(clusterNames.size());
		Color[] defCols = Colors.getColors(clusterNames.size());
		Iterator<String> clusternamesiterator = clusterNames.iterator();
		
		for (int i = 0; i < defCols.length; i++) {
			cca.listClusterNames.add(clusternamesiterator.next());
			cca.listClusterColors.add(defCols[i]);
			cca.listOutlineColors.add(Color.BLACK);
//			cca.setClusterColor(i, defCols[i]);
//			cca.setClusterOutlineColor(i, Color.BLACK);
		}
		return cca;
	}
	
}