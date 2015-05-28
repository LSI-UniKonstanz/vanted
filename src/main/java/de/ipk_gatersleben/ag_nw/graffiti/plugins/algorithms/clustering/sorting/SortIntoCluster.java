/**
 * 
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.clustering.sorting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JOptionPane;

import org.AttributeHelper;
import org.StringManipulationTools;
import org.apache.log4j.Logger;
import org.graffiti.attributes.Attribute;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.dialog.DefaultParameterDialog;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.parameter.DoubleParameter;
import org.graffiti.plugin.parameter.ObjectListParameter;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.AttributePathNameSearchType;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.SearchAndSelecAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.SearchType;

/**
 * Sorts graph elements into cluster.
 * It takes the information to sort into clusters from a selected attribute, that all graph elements
 * have in common.
 * This can be e.g. the average value of experimental data or the correlation coefficient value.
 * The user can select the attribute in the dialog box when the algorithm is executed. 
 * @author matthiak
 *
 */
public class SortIntoCluster extends AbstractAlgorithm{

	private enum EnumAttrType {
		STRING,
		NUMERIC
	}

	static final Logger logger = Logger.getLogger(SortIntoCluster.class);

	private Collection<Node> selectedOrAllNodes;

	private String selAttrPath;
	private String selAttrName;

	private EnumAttrType attrType;

	double lowerLimit = -1;
	double upperLimit = 1;


	@Override
	public String getName() {
		return "Sort into Cluster";
	}



	@Override
	public void setParameters(Parameter[] params) {
		super.setParameters(params);
		if(params == null || params.length == 0)
			return;

		Parameter paramSelAttribute = params[0];
		AttributePathNameSearchType value = (AttributePathNameSearchType)paramSelAttribute.getValue();
		selAttrName = value.getAttributeName();
		selAttrPath = value.getAttributePath();

	}



	@Override
	public String getDescription() {
		return super.getDescription();
	}



	@Override
	public Parameter[] getParameters() {

		selectedOrAllNodes = getSelectedOrAllNodes();
		if(selectedOrAllNodes == null)
			return null;

		ArrayList<AttributePathNameSearchType> listAttributes = new ArrayList<>();
		SearchAndSelecAlgorithm.enumerateAttributes(listAttributes, selectedOrAllNodes, SearchType.getSetOfSearchTypes());

		ObjectListParameter paramListAttributes = new ObjectListParameter(
				null, 
				"Select Attribute", 
				"Select the attribute, that contains the value (String or Numeric), to cluster upon", 
				listAttributes);

		Parameter[] returnParameters = new Parameter[1];
		returnParameters[0] = paramListAttributes;

		return returnParameters;
	}



	@Override
	public String getCategory() {
		return "Network.Cluster";
	}



	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(Arrays.asList(
				Category.CLUSTER,
				Category.COMPUTATION
				));
	}



	@Override
	public void execute() {

		/*
		 * get attribute type, where the clustering depends on
		 */
		Object attrValue = AttributeHelper.getAttributeValue(selectedOrAllNodes.iterator().next(), selAttrPath, selAttrName, null, null);
		if(attrValue instanceof String) {
			attrType = EnumAttrType.STRING;

		}
		else if (
				attrValue instanceof Long ||
				attrValue instanceof Integer ||
				attrValue instanceof Double ||
				attrValue instanceof Float
				) {
			attrType = EnumAttrType.NUMERIC;

		}
		else {
			/*
			 * we only support string and numberic values
			 */
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "The Clustering only works with String or Numeric Values", "Communication Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		logger.debug(attrType);

		switch (attrType) {
		case STRING: 
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "<html>Creating cluster from selected String attribute.<br/>Nodes with the same string value will be put<br?> into the same cluster", "Cluster Creation", JOptionPane.INFORMATION_MESSAGE);
			clusterByString();
			break;
		case NUMERIC:

			clusterByValue();
		} 

	}



	/**
	 * 
	 */
	private void clusterByString() {


		for(Node curNode : getSelectedOrAllNodes()) {
			String attributeValue = (String)AttributeHelper.getAttributeValue(curNode, selAttrPath, selAttrName, null, new String());
			NodeTools.setClusterID(curNode, attributeValue);
		}


	}



	/**
	 * clusters the nodes into three cluster. 
	 * The values are the average experimental values of the node's experiment values.
	 * We calculate two 
	 * Min / Max/ 0
	 * 
	 */
	private void clusterByValue() {

		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;

		for(Node curNode : getSelectedOrAllNodes()) {
			Object attributeValue = AttributeHelper.getAttributeValue(curNode, selAttrPath, selAttrName, null, null);

			double value = 0;
			if(attributeValue instanceof Number)
				value = ((Number)attributeValue).doubleValue();
			if(value < min)
				min = value;
			if(value > max)
				max = value;
		}

		lowerLimit = min + (max - min) / 3;
		upperLimit = max - (max - min) / 3;

		DefaultParameterDialog paramDialog = new DefaultParameterDialog(MainFrame.getInstance().getEditComponentManager(), MainFrame.getInstance(), getValueClusterParameters(),
				selection, "Select Cluster bounds", "<html>Select bounds to seperate data into three clusters.<br/>"
						+ " Max value: " + max + "<br/>Min value: " + min + ".", null, false);

		if (!paramDialog.isOkSelected()) {
			return;
		}
		Parameter[] editedParameters = paramDialog.getEditedParameters();

		double setLowerLimit = ((DoubleParameter)editedParameters[0]).getDouble();
		double setUpperLimit = ((DoubleParameter)editedParameters[1]).getDouble();

		String clusterNameLow = "Cluster below " + setLowerLimit;
		String clusterNameMiddle = "Cluster between " + setLowerLimit + " and " + setUpperLimit;
		String clusterNameHigh = "Cluster above " + setUpperLimit;

		for(Node curNode : getSelectedOrAllNodes()) {
			Object attributeValue = AttributeHelper.getAttributeValue(curNode, selAttrPath, selAttrName, null, null);

			double value = 0;
			if(attributeValue instanceof Number)
				value = ((Number)attributeValue).doubleValue();

			if(value <= setLowerLimit) {
				NodeTools.setClusterID(curNode, clusterNameLow);
			} else if (value >= setUpperLimit) {
				NodeTools.setClusterID(curNode, clusterNameHigh);
			} else {
				NodeTools.setClusterID(curNode, clusterNameMiddle);
			}

		}

	}

	private Parameter[] getValueClusterParameters() {
		return new Parameter[] {
				new DoubleParameter(lowerLimit,
						"Lower Limit",
						"All nodes with a value below or equal this value will be grouped into the cluster 'down'."),
				new DoubleParameter(upperLimit,
						"Upper Limit",
						"All nodes with a value above or equal this value will be grouped into the cluster 'up'.") };


	}
}
