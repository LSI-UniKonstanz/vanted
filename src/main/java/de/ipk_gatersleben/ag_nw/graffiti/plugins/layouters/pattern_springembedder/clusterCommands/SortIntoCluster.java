/**
 * 
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import org.AttributeHelper;
import org.apache.log4j.Logger;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.dialog.DefaultParameterDialog;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.parameter.DoubleParameter;
import org.graffiti.plugin.parameter.IntegerParameter;
import org.graffiti.plugin.parameter.ObjectListParameter;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.AttributePathNameSearchType;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.SearchAndSelecAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.SearchType;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

/**
 * Sorts graph elements into cluster. It takes the information to sort into
 * clusters from a selected attribute, that all graph elements have in common.
 * This can be e.g. the average value of experimental data or the correlation
 * coefficient value. The user can select the attribute in the dialog box when
 * the algorithm is executed.
 * 
 * @author matthiak
 * @vanted.revision 2.7.0 Moved to Set Cluster ID LaunchGui window
 */
public class SortIntoCluster extends AbstractAlgorithm {

	private enum EnumAttrType {
		STRING, NUMERIC
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
		return "Sort into Clusters by Attribute";
	}

	@Override
	public void setParameters(Parameter[] params) {
		super.setParameters(params);
		if (params == null || params.length == 0)
			return;

		Parameter paramSelAttribute = params[0];
		AttributePathNameSearchType value = (AttributePathNameSearchType) paramSelAttribute.getValue();
		selAttrName = value.getAttributeName();
		selAttrPath = value.getAttributePath();

	}

	@Override
	public String getDescription() {
		return "<html>Puts network nodes (selected or all) in<br/>cluster, based on the selected Attribute.";
	}

	@Override
	public Parameter[] getParameters() {

		selectedOrAllNodes = getSelectedOrAllNodes();
		if (selectedOrAllNodes == null)
			return null;

		ArrayList<AttributePathNameSearchType> listAttributes = new ArrayList<>();
		SearchAndSelecAlgorithm.enumerateAttributes(listAttributes, selectedOrAllNodes,
				SearchType.getSetOfSearchTypes());

		ObjectListParameter paramListAttributes = new ObjectListParameter(null, "Select Attribute",
				"Select the attribute, that contains the value (String or Numeric), to cluster upon", listAttributes);

		Parameter[] returnParameters = new Parameter[1];
		returnParameters[0] = paramListAttributes;

		return returnParameters;
	}

//	@Override
//	public String getCategory() {
//		return "Network.Cluster";
//	}

	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(Arrays.asList(Category.CLUSTER, Category.COMPUTATION));
	}

	@Override
	public void execute() {

		/*
		 * get attribute type, where the clustering depends on
		 */

		Object attrValue = null;
		attrType = null;

		List<Node> nodesWithSelectedAttribute = new ArrayList<>();
		for (Node curNode : selectedOrAllNodes) {
			if ((attrValue = AttributeHelper.getAttributeValue(curNode, selAttrPath, selAttrName, null,
					null)) != null) {
				nodesWithSelectedAttribute.add(curNode);
			}
		}

		if (nodesWithSelectedAttribute.isEmpty())
			return;

		attrValue = AttributeHelper.getAttributeValue(nodesWithSelectedAttribute.get(0), selAttrPath, selAttrName, null,
				null);

		// attrValue should still be set with the last value of the last checked node
		if (attrValue instanceof String) {
			attrType = EnumAttrType.STRING;

		} else if (attrValue instanceof Long || attrValue instanceof Integer || attrValue instanceof Double
				|| attrValue instanceof Float) {
			attrType = EnumAttrType.NUMERIC;

		} else {
			/*
			 * we only support string and numberic values
			 */
			JOptionPane.showMessageDialog(MainFrame.getInstance(),
					"The Clustering only works with String or Numeric Values", "Communication Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		logger.debug(attrType);

		switch (attrType) {
		case STRING:
//			JOptionPane.showMessageDialog(MainFrame.getInstance(),
//					"<html>Creating cluster from selected String attribute.<br/>Nodes with the same string value will be put<br?> into the same cluster",
//					"Cluster Creation", JOptionPane.INFORMATION_MESSAGE);
			clusterByString(nodesWithSelectedAttribute);
			break;
		case NUMERIC:

			clusterByValue(nodesWithSelectedAttribute);
		}

	}

	/**
	 * 
	 */
	private void clusterByString(List<Node> nodesWithSelectedAttribute) {

		for (Node curNode : nodesWithSelectedAttribute) {
			String attributeValue = (String) AttributeHelper.getAttributeValue(curNode, selAttrPath, selAttrName, null,
					new String());
			NodeTools.setClusterID(curNode, attributeValue);
		}

	}

	/**
	 * clusters the nodes into three cluster. The values are the average
	 * experimental values of the node's experiment values. We calculate two Min /
	 * Max/ 0
	 */
	private void clusterByValue(final List<Node> nodesWithSelectedAttribute) {

		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;

		for (Node curNode : nodesWithSelectedAttribute) {
			Object attributeValue = AttributeHelper.getAttributeValue(curNode, selAttrPath, selAttrName, null, null);

			double value = 0;
			if (attributeValue instanceof Number)
				value = ((Number) attributeValue).doubleValue();
			if (value < min)
				min = value;
			if (value > max)
				max = value;
		}

		DefaultParameterDialog paramDialog;

		/*
		 * ask user, how many clusters he wants the numeric to be split into currently
		 * limited to 10
		 */
		Parameter[] numberOfSplitsParameter = new Parameter[] {
				new IntegerParameter(2, 2, 10, "Number of clusters", "Choose the number of clusters") };

		paramDialog = new DefaultParameterDialog(MainFrame.getInstance().getEditComponentManager(),
				MainFrame.getInstance(), numberOfSplitsParameter, selection, "Select Number of Cluster",
				"<html>Choose the number of clusters, in which the numeric<br/>" + "values will be sorted in", null,
				false);

		if (!paramDialog.isOkSelected()) {
			return;
		}

		int numClusters = ((IntegerParameter) paramDialog.getEditedParameters()[0]).getInteger();

		paramDialog = new DefaultParameterDialog(MainFrame.getInstance().getEditComponentManager(),
				MainFrame.getInstance(), getClusterSplitValueParameters(min, max, numClusters), selection,
				"Select Cluster split points",
				"<html>Select split points to seperate data into " + numClusters + " cluster.<br/><br/>"
						+ " Maximum value found: <strong>" + max + "</strong><br/>Minimum value found: <strong>" + min
						+ "</strong>.",
				null, false);

		if (!paramDialog.isOkSelected()) {
			return;
		}
		Parameter[] editedParameters = paramDialog.getEditedParameters();

		final double[] limits = new double[numClusters];

		for (int i = 0; i < numClusters - 1; i++) {
			limits[i] = ((DoubleParameter) editedParameters[i]).getDouble();
		}
		limits[numClusters - 1] = max; // last limit is the maximum (which should never be exceeded)

		final String[] clusterNames = new String[numClusters];

		int i = 0;
		clusterNames[i] = "Cluster between " + min + " and " + limits[i];
		i++;
		for (; i < numClusters; i++)
			clusterNames[i] = "Cluster between " + limits[i - 1] + " and " + limits[i];

		BackgroundTaskHelper.issueSimpleTask("Sorting into Clusters", null, new Runnable() {

			@Override
			public void run() {
				for (Node curNode : nodesWithSelectedAttribute) {
					Object attributeValue = AttributeHelper.getAttributeValue(curNode, selAttrPath, selAttrName, null,
							null);

					double value = 0;
					if (attributeValue instanceof Number)
						value = ((Number) attributeValue).doubleValue();
					else
						continue;

					int curIdx = 0;
					@SuppressWarnings("unused")
					double result;
					while ((result = value - limits[curIdx++]) > 0) {
					}

					NodeTools.setClusterID(curNode, clusterNames[curIdx - 1]); // +2 because undo the decrement (+1) and
																				// the splitpoint cluster name is
																				// splitpoint + 1 (+1)

				}
			}
		}, null);
	}

	/**
	 * returns numClusters-1 DoubleParameters (NumSplitpoints = Numclusters - 1) The
	 * initial values for the split points are calculated evenly based on the given
	 * max and min values
	 * 
	 * @param numClusters
	 * @return
	 */
	private static Parameter[] getClusterSplitValueParameters(double min, double max, int numClusters) {

		double increment = (max - min) / (double) numClusters;

		Parameter[] splitParameters = new Parameter[numClusters];

		for (int i = 0; i < numClusters - 1; i++) {
			splitParameters[i] = new DoubleParameter(min + increment * (i + 1), "Split point (" + (i + 1) + ")", null);

		}

		return splitParameters;
	}
}
