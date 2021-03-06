/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 17.01.2005 by Christian Klukas
 * (c) 2005 IPK Gatersleben, Group Network Analysis
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.process_alternative_ids;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.xml_attribute.XMLAttribute;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ExperimentDataFileReader;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.OpenExcelFileDialogService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.TableData;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes.Experiment2GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

/**
 * @author Christian Klukas (c) 2006, 2007 IPK Gatersleben, Group Network
 *         Analysis
 */
public class AdditionalIdentifiersAlgorithm extends AbstractAlgorithm {
	
	private boolean ignoreFirstRow = false;
	
	@Override
	public String getName() {
		return "Add Alternative Data Identifiers";
	}
	
	@Override
	public String getDescription() {
		return "<html>"
				+ "This command reads (multiple) mapping tables and adds alternative<br>"
				+ "substance IDs to the mapped data. The same process could be per-<br>"
				+ "formed before data mapping from the Experiments Tab. In case, data<br>"
				+ "source file or network connection are unavailable and substance IDs<br>"
				+ "should be added, this command may come in handy.<br><br>"
				+ "Alternative substance IDs are processed by several commands<br>"
				+ "e.g. the ones available in &quot;Network &#8594; Hierarchy&quot;.<br>" + "<br>"
				+ "Existing alternative substance IDs are removed during this<br>"
				+ "process, and in case no new substance IDs are available for<br>"
				+ "given data mapping (different from the options in Experiments Tab).<br><br>" + "<br>"
				+ "Layout of input file (multiple IDs may span columns or be<br>"
				+ "specified in additional rows):<br><br>" + "<code>"
				+ "[Column A | Column B | [Column C] | ...]<br>" + "Main ID 1| alt ID 1 | alt ID 2 ...<br>"
				+ "Main ID 2| alt ID 1<br>" + "Main ID 1| alt ID 3<br>" + "</code><br><br>";
	}
	
	@Override
	public String getCategory() {
		return "Mapping.Alternative Identifiers";
	}
	
	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(Arrays.asList(Category.GRAPH, Category.ANNOTATION));
	}
	
	@Override
	public Parameter[] getParameters() {
		return new Parameter[] { new BooleanParameter(ignoreFirstRow, "Skip first row",
				"<html>" + "If enabled, the first row is not processed.<br>"
						+ "Useful in case the first row contains column headers.") };
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		ignoreFirstRow = ((BooleanParameter) params[i++]).getBoolean();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 */
	public void execute() {
		BackgroundTaskHelper.issueSimpleTask("Adding alternative identifiers", "working..", new Runnable() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see java.lang.Runnable#run()
			 */
			@Override
			public void run() {
				Collection<File> excelFiles = OpenExcelFileDialogService.getExcelFiles();
				if (excelFiles != null && excelFiles.size() > 0) {
					HashMap<String, Set<String>> id2alternatives = new HashMap<String, Set<String>>();
					
					for (File excelFile : excelFiles) {
						TableData myData = ExperimentDataFileReader.getExcelTableData(excelFile);
						
						int startRow = (ignoreFirstRow ? 2 : 1);
						
						for (int row = startRow; row <= myData.getMaximumRow(); row++) {
							String mainID = myData.getUnicodeStringCellData(1, row);
							for (int col = 2; col <= myData.getMaximumCol(); col++) {
								String alternativeID = myData.getUnicodeStringCellData(col, row);
								if (alternativeID != null && alternativeID.length() > 0) {
									if (!id2alternatives.containsKey(mainID))
										id2alternatives.put(mainID, new HashSet<String>());
									Set<String> alternatives = id2alternatives.get(mainID);
									alternatives.add(alternativeID);
								}
							}
						}
					}
					
					ArrayList<SubstanceInterface> documents = new ArrayList<SubstanceInterface>();
					for (Node n : getSelectedOrAllNodes()) {
						try {
							CollectionAttribute ca = (CollectionAttribute) n
									.getAttribute(Experiment2GraphHelper.mapFolder);
							XMLAttribute xa = (XMLAttribute) ca.getAttribute(Experiment2GraphHelper.mapVarName);
							Iterable<SubstanceInterface> mappingdata = xa.getMappedData();
							for (SubstanceInterface md : mappingdata) {
								documents.add(md);
								md.clearSynonyms();
							}
						} catch (Exception anfe) {
							// empty
						}
					}
					
					int idCnt = 0;
					for (SubstanceInterface xmlSubstanceNode : documents) {
						String name = xmlSubstanceNode.getName();
						Set<String> alternatives = id2alternatives.get(name);
						if (alternatives != null) {
							xmlSubstanceNode.setSynonyme(0, name);
							int i = 0;
							for (String alternativeID : alternatives) {
								xmlSubstanceNode.setSynonyme((++i), alternativeID);
								idCnt++;
							}
						}
					}
					MainFrame.showMessage("Additional Identifiers (" + idCnt + ")", MessageType.INFO);
				}
			}
		}, null);
	}
}
