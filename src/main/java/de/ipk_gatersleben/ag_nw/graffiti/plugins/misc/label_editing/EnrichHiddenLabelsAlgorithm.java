/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 17.01.2005 by Christian Klukas
 * (c) 2005 IPK Gatersleben, Group Network Analysis
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.label_editing;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.AlignmentSetting;
import org.AttributeHelper;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Edge;
import org.graffiti.graph.GraphElement;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.io.resources.FileSystemHandler;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.MultiFileSelectionParameter;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ExperimentDataFileReader;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.OpenExcelFileDialogService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.TableData;

/**
 * @author Christian Klukas (c) 2006, 2007 IPK Gatersleben, Group Network
 *         Analysis
 */
public class EnrichHiddenLabelsAlgorithm extends AbstractAlgorithm {
	
	private boolean ignoreFirstRow = true;
	private boolean removeallhiddenlabels;
	private boolean considerHiddenLabels;
	private boolean ignoreCase;
	private ArrayList<IOurl> urls = new ArrayList<IOurl>();
	
	public String getName() {
		return "Add hidden labels using file tables...";
	}
	
	@Override
	public String getDescription() {
		return "<html>" + "This command reads (multiple) mapping table files<br>"
				+ "and uses these to add hidden labels (=annotations),<br>"
				+ "e.g. in order to prepare networks for more flexible<br>" + "data mapping.<br>"
				+ "Layout of input file:<br><br>" + "<code>" + "[Label in Graph | hidden Label 1 | ...]<br>"
				+ "Graph label 1| hidden label| ...<br>" + "Graph label 2| hidden label| ...<br>"
				+ "Graph label 3| hidden label| ...<br>" + "</code><br><br>";
	}
	
	@Override
	public String getCategory() {
		return null;// "Elements";
	}
	
	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(Arrays.asList(Category.GRAPH, Category.ANNOTATION));
	}
	
	@Override
	public String getMenuCategory() {
		return "edit.Change Label";
	}
	
	@Override
	public Parameter[] getParameters() {
		urls.clear();
		return new Parameter[] {
				new BooleanParameter(ignoreFirstRow, "Skip first row",
						"<html>" + "If enabled, the first row is not processed.<br>"
								+ "Useful in case the first row contains column headers."),
				new BooleanParameter(removeallhiddenlabels, "Remove old labels",
						"<html>if enabled, all existing hidden labels will be deleted before adding new labels.<br>"
								+ "Otherwise the new labels will be appended"),
				new BooleanParameter(considerHiddenLabels, "Consider alternative Labels",
						"<html>if enabled, the algorithm will also check existing alternative identifiers<br>"
								+ "for the matching operation."),
				new BooleanParameter(ignoreCase, "Ignore Case",
						"<html>if enabled, the algorithm will be case insensitive"),
				new MultiFileSelectionParameter(urls, "Table Files",
						"Select the list of mapping table files to be used",
						OpenExcelFileDialogService.EXCELFILE_EXTENSIONS,
						OpenExcelFileDialogService.SPREADSHEET_DESCRIPTION, true) };
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		ignoreFirstRow = ((BooleanParameter) params[i++]).getBoolean();
		removeallhiddenlabels = ((BooleanParameter) params[i++]).getBoolean();
		considerHiddenLabels = ((BooleanParameter) params[i++]).getBoolean();
		ignoreCase = ((BooleanParameter) params[i++]).getBoolean();
		
		urls = ((MultiFileSelectionParameter) params[i++]).getFileList();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 */
	public void execute() {
		Collection<File> excelFiles = new ArrayList<File>();
		for (IOurl u : urls)
			excelFiles.add(FileSystemHandler.getFile(u));
		if (excelFiles != null && excelFiles.size() > 0) {
			HashMap<String, ArrayList<String>> id2alternatives = new HashMap<String, ArrayList<String>>();
			
			for (File excelFile : excelFiles) {
				TableData myData = ExperimentDataFileReader.getExcelTableData(excelFile);
				
				int startRow = (ignoreFirstRow ? 2 : 1);
				
				for (int row = startRow; row <= myData.getMaximumRow(); row++) {
					String currentName = myData.getUnicodeStringCellData(1, row);
					if (ignoreCase)
						currentName = currentName.toUpperCase();
					int cntCols = 2;
					String cell = myData.getUnicodeStringCellData(cntCols, row);
					while (cell != null) {
						if (!id2alternatives.containsKey(currentName))
							id2alternatives.put(currentName, new ArrayList<String>());
						id2alternatives.get(currentName).add(cell);
						cell = myData.getUnicodeStringCellData(++cntCols, row);
						
					}
				}
			}
			boolean tooManyAnnotations = false;
			int addedCnt = 0, deletedCnt = 0, graphelementcnt = 0;
			for (GraphElement ge : getSelectedOrAllGraphElements()) {
				
				boolean match = false;
				// boolean doBreakFuzzy = false;
				// get fuzzy label, which contains a set of the original label + label without
				// HTML
				String label2 = AttributeHelper.getLabel(ge, null);
				if (ignoreCase)
					label2 = label2.toUpperCase();
				HashSet<String> fuzzyLabels = AttributeHelper.getFuzzyLabels(label2);
				
				// add all the alternative identifiers, if selected as parameter, but without
				// fuzz.
				if (considerHiddenLabels) {
					for (String altLabel : AttributeHelper.getLabels(ge))
						if (altLabel != null && !altLabel.isEmpty()) {
							if (ignoreCase)
								altLabel = altLabel.trim().toUpperCase();
							fuzzyLabels.add(altLabel);
						}
				}
				if (removeallhiddenlabels) {
					for (int k = 1; k < 100; k++)
						if (AttributeHelper.hasAttribute(ge,
								GraphicAttributeConstants.LABELGRAPHICS + String.valueOf(k))) {
							ge.removeAttribute(GraphicAttributeConstants.LABELGRAPHICS + String.valueOf(k));
							deletedCnt++;
						}
				}
				fuzzy: for (String label : fuzzyLabels) {
					if (id2alternatives.containsKey(label)) {
						match = true;
						/*
						 * labels are not necessarily adjacent. skip filled slots
						 */
						int k = 1;
						while (AttributeHelper.hasAttribute(ge,
								GraphicAttributeConstants.LABELGRAPHICS + String.valueOf(k)))
							k++;
						
						if (ge instanceof Edge) {
							AttributeHelper.setLabel(ge, id2alternatives.get(label).get(0));
							break fuzzy;
						} else {
							if (id2alternatives.get(label).size() > 0)
								for (String s : id2alternatives.get(label)) {
									if (k > 99) {
										tooManyAnnotations = true;
										break;
									}
									AttributeHelper.setLabel(k++, ge, s, null, AlignmentSetting.HIDDEN.toGMLstring());
									addedCnt++;
									// doBreakFuzzy = true;
								}
						}
					}
					// if (doBreakFuzzy)
					// break fuzzy;
				}
				if (match)
					graphelementcnt++;
				
			}
			if (tooManyAnnotations)
				MainFrame.showMessageDialog("<html>Some labels were skipped, as it is not allowed to<br>"
						+ "add more than 99 additional labels to graphelements.<br>"
						+ "If you need more annotations, you have to annotate<br>"
						+ "your experiment instead of the graph.", "Too many hidden labels");
			MainFrame.showMessageDialog(
					"<html>" + (deletedCnt > 0 ? deletedCnt + " hidden labels have been deleted,<br>" : "") + addedCnt
							+ " labels have been added to " + graphelementcnt + " graphelements in graph<p><i>"
							+ graph.getName(),
					"Information");
		}
	}
	
	@Override
	public boolean mayWorkOnMultipleGraphs() {
		return true;
	}
	
}
