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
 * All StringAttributes with the name id "chart_colors" will be converted into a
 * ChartColorAttribute (see ChartAttributePlugin, where the mapping information
 * from id to class type is initialized.
 * 
 * @author Christian Klukas (c) 2004 IPK-Gatersleben
 * @vanted.revision 2.7.0 Cluster recolouring.
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
	
	private String notSet = "undefined";
	
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
	 * That method must be called when the value is set. The value is a String
	 * object and needs to be parsed and put into the designated lists that keep the
	 * colors Those colors are not yet connected to clusters, since the original
	 * implementation only used a simple index, for which cluster color to use.
	 */
	private void fillColorFields() {
		ArrayList<Color> colorstringCluster = interpreteColorString(INDEX_CLUSTERCOLOR);
		ArrayList<Color> colorstringOutline = interpreteColorString(INDEX_CLUSTEROUTLINECOLOR);
		if (colorstringCluster != null)
			listClusterColors = colorstringCluster;
		if (colorstringOutline != null)
			listOutlineColors = colorstringOutline;
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
				if (barCol_outCol.equals(notSet))
					barCol_outCol = " : ";
				if (result.length() > 0)
					result.append(";");
				
				String colorCode;
				if (newColor == null)
					colorCode = "null";
				else {
					if (newColor instanceof Color) {
						Color c = (Color) newColor;
						colorCode = c.getRed() + "," + c.getGreen() + "," + c.getBlue() + "," + c.getAlpha();
					} else
						colorCode = "null";
				}
				
				String s;
				if (idx0bar_1outline == 0) {
					s = barCol_outCol.split(":")[1];
					s = (s == null || s.length() == 0) ? "null" : s;
					result.append(colorCode + ":" + s);
				} else {
					s = barCol_outCol.split(":")[0];
					s = (s == null || s.length() == 0) ? "null" : s;
					result.append(s + ":" + colorCode);
				}
			}
		}
		value = result.toString();
		// setValue(result);
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
	 * Must be called on an existing ClusterColorAttribute to update the cluster
	 * information. Clusters will be recoloured given there is a change, because
	 * there is no reasonable mapping of additional colours to the existing
	 * (unknown) colour model. Given there is no change, cluster colours won't be
	 * modified.
	 * 
	 * @param clusterNames
	 * @vanted.revision 2.7.0 Bugfixing of bugfixing...
	 */
	public void updateClusterList(Collection<String> clusterNames) {
		if (clusterNames == null || clusterNames.isEmpty()) {
			setDefaultValue();
			return;
		}
		
		/*
		 * If this attribute is created, only colors are known, since clusternames are
		 * not stored in GML or when this string attribute is read.. Compatibility Sooo.
		 * we just need to connect names, to already set colors.
		 */
		if (listClusterNames.isEmpty()) {
			/*
			 * if graph is copied or the cluster color attribute was not updated the number
			 * of clustercolors can diverge from the actual present clusters, since
			 * clustercolors is a graph attribute .. e.g. copying from graph with two
			 * clusters.. copy one node (one cluster member) insert to new graph.. graph
			 * attribute still has two colors colors but only one cluster By calling
			 * updateClusterList again with the actual number of present clusters the
			 * clustercolors graph attribute gets updated.
			 */
			listClusterNames.addAll(clusterNames);
			updateClusterList(clusterNames);
		} else {
			if (listClusterNames.size() == listClusterColors.size())
				return;
			
			Color[] colors = Colors.getColors(clusterNames.size());
			int colorIdx = clusterNames.size() - 1;
			ArrayList<Color> newListClusterColors = new ArrayList<Color>();
			ArrayList<Color> newListOutlineColors = new ArrayList<Color>();
			ArrayList<String> newListClusterNames = new ArrayList<String>();
			for (String curClusterName : clusterNames) {
				newListClusterColors.add(colors[colorIdx--]);
				newListOutlineColors.add(Color.BLACK);
				newListClusterNames.add(curClusterName);
			}
			
			listClusterColors = newListClusterColors;
			listOutlineColors = newListOutlineColors;
			listClusterNames = newListClusterNames;
		}
	}
	
	@Override
	protected void doSetValue(Object o) throws IllegalArgumentException {
		try {
			setString((String) o);
			
		} catch (ClassCastException cce) {
			throw new IllegalArgumentException("Invalid value type.");
		}
	}
	
	public int getDefinedClusterColorCount() {
		/*
		 * if (value == null || value.equals(notSet)) return 0; else return
		 * value.length() - value.replaceAll(";", "").length() + 1;
		 */
		return listClusterColors.size();
		
	}
	
	public void setClusterColor(int clusterID, Color color) {
		// ensureMinimumColorSelection(clusterID);
		listClusterColors.remove(clusterID);
		listClusterColors.add(clusterID, color);
		// setColorString(0, clusterID, color);
	}
	
	public void setClusterOutlineColor(int clusterID, Color color) {
		// ensureMinimumColorSelection(clusterID);
		listOutlineColors.remove(clusterID);
		listOutlineColors.add(clusterID, color);
		// setColorString(1, clusterID, color);
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
		// cca.ensureMinimumColorSelection(clusterNames.size());
		Color[] defCols = Colors.getColors(clusterNames.size());
		Iterator<String> clusternamesiterator = clusterNames.iterator();
		
		for (int i = 0; i < defCols.length; i++) {
			cca.listClusterNames.add(clusternamesiterator.next());
			cca.listClusterColors.add(defCols[i]);
			cca.listOutlineColors.add(Color.BLACK);
			// cca.setClusterColor(i, defCols[i]);
			// cca.setClusterOutlineColor(i, Color.BLACK);
		}
		return cca;
	}
	
}