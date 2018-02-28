package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import org.AttributeHelper;
import org.ErrorMsg;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFCreationHelper;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.graffiti.editor.MainFrame;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

public class ExperimentDataFileWriter {

	private XSSFWorkbook xssfWorkbook;
	private XSSFSheet xssfWorksheet;
	private XSSFCreationHelper xssfCreationHelper;

	private XSSFCellStyle cellStyleHeadline;
	// private XSSFCellStyle cellStyleGrey;
	// private XSSFCellStyle cellStyleYellow;
	// private XSSFCellStyle cellStyleTurquoise;

	private XSSFCellStyle cellStyleData;
	private int rowStart;

	private ExperimentDataFileWriter() {
		initHSSFObjects();
		this.rowStart = 10;

	}

	/**
	 * Creates global objects for excel-java interaction, for instance cell styling:
	 * color, font.
	 */
	private void initHSSFObjects() {

		xssfWorkbook = new XSSFWorkbook();
		xssfWorksheet = xssfWorkbook.createSheet("Experiment");
		xssfCreationHelper = xssfWorkbook.getCreationHelper();

		// cellStyleGrey = xssfWorkbook.createCellStyle();
		// fill pattern should be "1" but it doesn't work
		// XSSFCellStyle.SOLID_FOREGROUND is set to "1" but this seems to be wrong
		// cellStyleGrey.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
		// cellStyleGrey.setFillPattern((short) 1);
		// cellStyleGrey.setFillBackgroundColor(new XSSFColor(new Color(228, 228,
		// 228)));

		// cellStyleYellow = xssfWorkbook.createCellStyle();
		// fill pattern should be "1" but it doesn't work
		// XSSFCellStyle.SOLID_FOREGROUND is set to "1" but this seems to be wrong
		// cellStyleYellow.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
		// cellStyleYellow.setFillPattern((short) 1);
		// cellStyleGrey.setFillBackgroundColor(new XSSFColor(new Color(255, 255,
		// 204)));

		// cellStyleTurquoise = xssfWorkbook.createCellStyle();
		// fill pattern should be "1" but it doesn't work
		// XSSFCellStyle.SOLID_FOREGROUND is set to "1" but this seems to be wrong
		// cellStyleTurquoise.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
		// cellStyleTurquoise.setFillPattern((short) 1);
		// cellStyleGrey.setFillBackgroundColor(new XSSFColor(new Color(204, 255,
		// 255)));

		cellStyleHeadline = xssfWorkbook.createCellStyle();
		cellStyleHeadline.setAlignment(XSSFCellStyle.ALIGN_CENTER);
		XSSFFont font = xssfWorkbook.createFont();
		font.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
		cellStyleHeadline.setFont(font);

		// data style
		cellStyleData = xssfWorkbook.createCellStyle();
		cellStyleData.setAlignment(XSSFCellStyle.ALIGN_CENTER);

		CellRangeAddress cellRangeAddress = new CellRangeAddress(0, 0, 0, 2);
		xssfWorksheet.addMergedRegion(cellRangeAddress);

	}

	private void autoFitCells() {
		// fit the content to cell size
		xssfWorksheet.autoSizeColumn(0);
		for (int i = 1; i < 5; i++)
			xssfWorksheet.setColumnWidth(i, 12 * 256);
	}

	private void addHeader(ExperimentInterface md) {
		ExperimentHeaderInterface header = md.getHeader();

		String title = "";
		if (MainFrame.getInstance() != null)
			title = " by " + MainFrame.getInstance().getTitle();
		XSSFRow row1 = xssfWorksheet.createRow((short) 0);
		XSSFCell cellA1 = row1.createCell(0, XSSFCell.CELL_TYPE_STRING);
		cellA1.setCellValue("Exported" + title + " on " + AttributeHelper.getDateString(new Date()));

		createHeadline(2, 0, "Experiment");

		// Start of Experiment
		// createCellIfNotExistsAndSet(3, 0, "Start of Experiment (Date)",
		// cellStyleGrey);
		createCellIfNotExistsAndSet(3, 0, "Start of Experiment (Date)", null);

		XSSFCell cell2 = createCellIfNotExists(3, 1);

		XSSFCellStyle cellStyle = xssfWorkbook.createCellStyle();
		cellStyle.setDataFormat(xssfCreationHelper.createDataFormat().getFormat("m/d/yy"));
		cellStyle.setAlignment(XSSFCellStyle.ALIGN_LEFT);
		// cellStyle.setFillBackgroundColor(XSSFColor.GOLD.index);
		cell2.setCellValue(header.getStartdate() == null ? new Date() : header.getStartdate());
		cell2.setCellStyle(cellStyle);

		// Remark*
		// createKeyValuePairStyled(4, 0, "Remark*", header.getRemark(), cellStyleGrey);
		createKeyValuePairStyled(4, 0, "Remark*", header.getRemark(), null);
		// createKeyValuePairStyled(5, 0, "ExperimentName (ID)",
		// header.getExperimentname(), cellStyleGrey);
		createKeyValuePairStyled(5, 0, "ExperimentName (ID)", header.getExperimentname(), null);
		// Coordinator
		// createKeyValuePairStyled(6, 0, "Coordinator", header.getCoordinator(),
		// cellStyleGrey);
		createKeyValuePairStyled(6, 0, "Coordinator", header.getCoordinator(), null);
		// Sequence-Name*
		// createKeyValuePairStyled(7, 0, "Sequence-Name*", header.getSequence(),
		// cellStyleGrey);
		createKeyValuePairStyled(7, 0, "Sequence-Name*", header.getSequence(), null);
		autoFitCells();
	}

	private void addImportInfo() {
		createHeadline(2, 4, "Help");
		// createCellIfNotExistsAndSet(3, 4, "- Fields with a * are optional",
		// cellStyleGrey);
		// createCellIfNotExistsAndSet(4, 4, "** These cells must contain numbers as 1,
		// 2, 3, ...", cellStyleGrey);
		// createCellIfNotExistsAndSet(5, 4, "*** These cells must correlate to the
		// numbers in **", cellStyleGrey);
		createCellIfNotExistsAndSet(3, 4, "- Fields with a * are optional", null);
		createCellIfNotExistsAndSet(4, 4, "** These cells must contain numbers as 1, 2, 3, ...", null);
		createCellIfNotExistsAndSet(5, 4, "*** These cells must correlate to the numbers in **", null);
		autoFitCells();
	}

	private void addInternalInfo(ExperimentInterface md) {
		createHeadline(2, 10, "Internal Info");
		createOrdinaryCell(3, 10, "V1.2");
	}

	private void addSpeciesInformation(ExperimentInterface md) {
		// fix headline
		createHeadline(rowStart, 0, "Plants/Genotypes**");

		// createCellIfNotExistsAndSet((rowStart + 1), 0, "Species", cellStyleGrey);
		// createCellIfNotExistsAndSet((rowStart + 2), 0, "Variety*", cellStyleGrey);
		// createCellIfNotExistsAndSet((rowStart + 3), 0, "Genotype", cellStyleGrey);
		// createCellIfNotExistsAndSet((rowStart + 4), 0, "Growth conditions*",
		// cellStyleGrey);
		// createCellIfNotExistsAndSet((rowStart + 5), 0, "Treatment*", cellStyleGrey);
		createCellIfNotExistsAndSet((rowStart + 1), 0, "Species", null);
		createCellIfNotExistsAndSet((rowStart + 2), 0, "Variety*", null);
		createCellIfNotExistsAndSet((rowStart + 3), 0, "Genotype", null);
		createCellIfNotExistsAndSet((rowStart + 4), 0, "Growth conditions*", null);
		createCellIfNotExistsAndSet((rowStart + 5), 0, "Treatment*", null);

		int actualRow = this.rowStart;
		int actualSubstance = 1;
		// prevents double entries of conditions.
		TreeSet<ConditionInterface> treesetOfConditions = new TreeSet<ConditionInterface>();
		for (SubstanceInterface substance : md) {
			for (ConditionInterface condition : substance) {
				if (!treesetOfConditions.contains(condition)) {
					treesetOfConditions.add(condition);
					createCellIfNotExistsAndSet(actualRow, actualSubstance, condition.getRowId(), cellStyleData);
					createCellIfNotExistsAndSet(actualRow + 1, actualSubstance, condition.getSpecies(), cellStyleData);
					createCellIfNotExistsAndSet(actualRow + 2, actualSubstance, condition.getVariety(), cellStyleData);
					createCellIfNotExistsAndSet(actualRow + 3, actualSubstance, condition.getGenotype(), cellStyleData);
					createCellIfNotExistsAndSet(actualRow + 4, actualSubstance, condition.getGrowthconditions(),
							cellStyleData);
					createCellIfNotExistsAndSet(actualRow + 5, actualSubstance, condition.getTreatment(),
							cellStyleData);
					actualSubstance++;
				}
			}
		}

		rowStart = rowStart + 9;
		autoFitCells();
	}

	/**
	 * Creates all constant Headlines for Measurement-Values. Prepares the Data in
	 * the given {@link ExperimentInterface} for easy creation in Excel-worksheet.
	 * Uses {@link DataRowExportExcel} for representation one row in
	 * Excel-worksheet.
	 * 
	 * @param md
	 */
	private void addGenoType(ExperimentInterface md) {
		final int ROW_TO_START = rowStart;

		// fix headlines
		createCellIfNotExistsAndSet(rowStart, 4, "Substance", cellStyleHeadline);
		createCellIfNotExistsAndSet((rowStart + 1), 4, "Meas.-Tool*", cellStyleHeadline);
		createCellIfNotExistsAndSet((rowStart + 2), 4, "Unit", cellStyleHeadline);

		createCellIfNotExistsAndSet((rowStart + 2), 0, "Plant/Genotype***", cellStyleHeadline);
		createCellIfNotExistsAndSet((rowStart + 2), 1, "Replicate #", cellStyleHeadline);
		createCellIfNotExistsAndSet((rowStart + 2), 2, "Time", cellStyleHeadline);
		createCellIfNotExistsAndSet((rowStart + 2), 3, "Unit (Time)", cellStyleHeadline);
		// big headline
		createHeadline(ROW_TO_START, 0, "Measurements");

		final int acutal_substance_column = 5;

		int acutal_substance = 0;
		ListOfDataRowsExcelExport orderedList = new ListOfDataRowsExcelExport();

		ArrayList<SubstanceInterface> substances = new ArrayList<SubstanceInterface>(md);

		if (substances.size() > 245) {
			MainFrame.showMessageDialog("<html>There are more than 245 substances available in this dataset.<br>"
					+ "As the excel export is experimental at the moment, only the first<br>"
					+ "245 substances will be written to the spreadsheet.", "Warning");
			for (int i = md.size() - 1; i >= 245; i--)
				substances.remove(i);
		}

		// prepare the Data to easily write in Excel-Worksheet
		for (SubstanceInterface substance : substances) {

			rowStart = ROW_TO_START;

			// because of start column = 4

			createCellIfNotExistsAndSet(rowStart, acutal_substance_column + acutal_substance, substance.getName(),
					cellStyleHeadline);

			for (ConditionInterface series : substance) {
				for (SampleInterface sample : series) {
					for (NumericMeasurementInterface meas : sample) {
						// try to get from orderedList the entry with the following keys: ConditionID,
						// TimeID, ReplicateID
						DataRowExcelExport data = orderedList.get(series.getConditionId(), sample.getTime(),
								meas.getReplicateID());
						if (null != data)
							data.addValue(substance.getName(), meas.getValue());
						// if entry does not exists create a new one
						else {
							DataRowExcelExport newDataRow = new DataRowExcelExport();
							newDataRow.setConditionID(series.getConditionId());
							newDataRow.setTimeID(sample.getTime());
							newDataRow.setTimeUnit(sample.getTimeUnit());
							newDataRow.setReplicateID(meas.getReplicateID());
							newDataRow.addValue(substance.getName(), meas.getValue());
							orderedList.add(newDataRow);
						}
						createCellIfNotExistsAndSet((rowStart + 1), acutal_substance_column + acutal_substance,
								sample.getMeasurementtool(), cellStyleData);
						createCellIfNotExistsAndSet((rowStart + 2), acutal_substance_column + acutal_substance,
								meas.getUnit(), cellStyleData);
						xssfWorksheet.autoSizeColumn(acutal_substance_column + acutal_substance);
					}
				}
			}
			// next substance - in excel worksheet next column
			++acutal_substance;
		}

		Collections.sort(orderedList, new Comparator<DataRowExcelExport>() {
			@Override
			public int compare(DataRowExcelExport o1, DataRowExcelExport o2) {
				if (Integer.valueOf(o1.conditionID).compareTo(Integer.valueOf(o2.conditionID)) == 0) {
					if (Integer.valueOf(o1.timeID).compareTo(Integer.valueOf(o2.timeID)) == 0)
						return Integer.valueOf(o1.replicateID).compareTo(Integer.valueOf(o2.replicateID));
					return Integer.valueOf(o1.timeID).compareTo(Integer.valueOf(o2.timeID));
				}
				return Integer.valueOf(o1.conditionID).compareTo(Integer.valueOf(o2.conditionID));
			}
		});

		// write the dataRows in Excel-Worksheet
		// write all DataRow-IDs
		int acual_mesurment = 0;
		Iterator<DataRowExcelExport> itDataRows = orderedList.iterator();
		for (int i = 0; i < orderedList.size(); i++) {
			DataRowExcelExport dataRow = itDataRows.next();
			createCellIfNotExistsAndSet((acual_mesurment + ROW_TO_START + 3), 0, dataRow.getConditionID(),
					cellStyleData);
			if (dataRow.getReplicateID() != -1)
				createCellIfNotExistsAndSet((acual_mesurment + ROW_TO_START + 3), 1, dataRow.getReplicateID(),
						cellStyleData);
			if (dataRow.getTimeID() != -1)
				createCellIfNotExistsAndSet((acual_mesurment + ROW_TO_START + 3), 2, dataRow.getTimeID(),
						cellStyleData);
			if (!dataRow.getTimeUnit().equals("-1"))
				createCellIfNotExistsAndSet((acual_mesurment + ROW_TO_START + 3), 3, dataRow.getTimeUnit(),
						cellStyleData);
			// write in every substance-column (excel-worksheet) the measurment-values
			HashMap<String, String> values = dataRow.getValues();
			acutal_substance = 0;
			for (SubstanceInterface substance : md) {

				String measureBySubstance = values.get(substance.getName());
				// if measure value for these substance is null - do not write any measure value
				// in these column
				if (measureBySubstance != null) {
					// createCellIfNotExistsAndSet((acual_mesurment + ROW_TO_START + 3),
					// acutal_substance_column + acutal_substance, measureBySubstance,
					// cellStyleData);
					createCellIfNotExistsAndSet((acual_mesurment + ROW_TO_START + 3),
							acutal_substance_column + acutal_substance, Double.parseDouble(measureBySubstance),
							cellStyleData);
					xssfWorksheet.autoSizeColumn(acutal_substance_column + acutal_substance);
					acutal_substance++;
				} else
					acutal_substance++;
			}

			acual_mesurment++;
		}
		autoFitCells();
	}

	/**
	 * Creates for given index the row in the actual worksheet.
	 * 
	 * @param rowIndex
	 * @return
	 */
	private XSSFRow createRowIfNotExists(int rowIndex) {
		XSSFRow row = xssfWorksheet.getRow(rowIndex);
		if (null == row)
			row = xssfWorksheet.createRow(rowIndex);
		return row;
	}

	/**
	 * creates the worksheet cell for given row and column index.
	 * 
	 * @param rowIndex
	 * @param columnIndex
	 * @return
	 */
	private XSSFCell createCellIfNotExists(int rowIndex, int columnIndex) {
		XSSFRow row = createRowIfNotExists(rowIndex);
		XSSFCell cell = row.getCell(columnIndex);
		if (null == cell)
			cell = row.createCell(columnIndex);
		return cell;
	}

	/**
	 * creates the worksheet cell for given row and column, fills it with the given
	 * text and the given style. If style is null, the cell is styled default
	 * 
	 * @param rowIndex
	 * @param columnIndex
	 * @param text
	 *            - Text of cell
	 * @param style
	 *            - CellStyle - if null - default style.
	 */
	private void createCellIfNotExistsAndSet(int rowIndex, int columnIndex, String text, XSSFCellStyle style) {
		XSSFCell cell = createCellIfNotExists(rowIndex, columnIndex);
		cell.setCellValue(text);
		if (style != null)
			cell.setCellStyle(style);

	}

	/**
	 * creates the worksheet cell for given row and column, fills it with the given
	 * text and the given style. If style is null, the cell is styled default
	 * 
	 * @param rowIndex
	 * @param columnIndex
	 * @param text
	 *            - Text of cell
	 * @param style
	 *            - CellStyle - if null - default style.
	 */
	private void createCellIfNotExistsAndSet(int rowIndex, int columnIndex, Integer text, XSSFCellStyle style) {
		XSSFCell cell = createCellIfNotExists(rowIndex, columnIndex);
		cell.setCellValue(text);
		if (style != null)
			cell.setCellStyle(style);
	}

	/**
	 * creates the worksheet cell for given row and column, fills it with the given
	 * text and the given style. If style is null, the cell is styled default
	 * 
	 * @param rowIndex
	 * @param columnIndex
	 * @param text
	 *            - Text of cell
	 * @param style
	 *            - CellStyle - if null - default style.
	 */
	private void createCellIfNotExistsAndSet(int rowIndex, int columnIndex, Double text, XSSFCellStyle style) {
		XSSFCell cell = createCellIfNotExists(rowIndex, columnIndex);
		cell.setCellValue(text);
		if (style != null)
			cell.setCellStyle(style);
	}

	/**
	 * creates the worksheet cell for given row and column, fills it with the given
	 * text.
	 * 
	 * @param rowIndex
	 * @param columnIndex
	 * @param text
	 *            - Text of cell
	 */
	private void createOrdinaryCell(int rowIndex, int columnIndex, String text) {
		XSSFCell cell = createCellIfNotExists(rowIndex, columnIndex);
		cell.setCellValue(text);
	}

	/**
	 * Creates two cells in direct neighborhood. Use these for (key,value)-pairs
	 * 
	 * @param rowIndex
	 * @param columnIndex
	 * @param key
	 * @param value
	 * @param cellStyle
	 */
	private void createKeyValuePairStyled(int rowIndex, int columnIndex, String key, String value,
			XSSFCellStyle cellStyle) {
		XSSFCell cell = createCellIfNotExists(rowIndex, columnIndex);
		cell.setCellValue(key);

		XSSFCell cell2 = createCellIfNotExists(rowIndex, columnIndex + 1);
		cell2.setCellValue(value);
		if (null != cellStyle)
			cell.setCellStyle(cellStyle);

	}

	/**
	 * Creates a cell as headline. The Headline-Style is fix codes in method
	 * initHSSFObjects().
	 * 
	 * @param rowIndex
	 * @param columnIndex
	 * @param text
	 */
	private void createHeadline(int rowIndex, int columnIndex, String text) {
		XSSFCell cell = createCellIfNotExists(rowIndex, columnIndex);
		cell.setCellValue(text);
		XSSFCellStyle cellStyle = xssfWorkbook.createCellStyle();
		XSSFFont font = xssfWorkbook.createFont();
		font.setFontHeightInPoints((short) 12);
		font.setFontName("Gothic L");
		font.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
		cellStyle.setFont(font);
		cell.setCellStyle(cellStyle);
		autoFitCells();
	}

	public static void writeExcel(File excelfile, ExperimentInterface md) {
		writeExcel(excelfile, md, false);
	}

	/**
	 * Export the given {@link ExperimentInterface} to the given {@link File}.
	 * 
	 * @param excelfile
	 * @param md
	 * @return
	 */
	public static void writeExcel(File excelfile, ExperimentInterface md, boolean transposed) {
		if (excelfile != null) {
			try {
				md = checkAndRemoveDoubleEntries(md);
				ExperimentDataFileWriter edfw = new ExperimentDataFileWriter();
				edfw.addHeader(md);
				edfw.addImportInfo();
				edfw.addInternalInfo(md);
				edfw.addSpeciesInformation(md);
				edfw.addGenoType(md);
				edfw.write(excelfile);
			} catch (Exception err) {
				ErrorMsg.addErrorMessage(err);
			}
		}
	}

	public static TableData getTableData(ExperimentInterface md) {
		md = checkAndRemoveDoubleEntries(md);
		ExperimentDataFileWriter edfw = new ExperimentDataFileWriter();
		edfw.addHeader(md);
		edfw.addImportInfo();
		edfw.addInternalInfo(md);
		edfw.addSpeciesInformation(md);
		edfw.addGenoType(md);
		return edfw.getTableData();
	}

	/**
	 * Some files contain double tuples (conditionID,sampleID,replicateID), which
	 * results in drop of some measurement values. we fix this by adjusting the
	 * replicateid for all measurements of one sample
	 * 
	 * @param md
	 * @return
	 */
	private static ExperimentInterface checkAndRemoveDoubleEntries(ExperimentInterface md) {
		md = md.clone();
		for (SubstanceInterface s : md)
			for (ConditionInterface c : s)
				for (SampleInterface sam : c) {
					HashMap<Integer, NumericMeasurementInterface> replid2meas = new HashMap<Integer, NumericMeasurementInterface>();
					for (NumericMeasurementInterface m : sam) {
						while (replid2meas.containsKey(m.getReplicateID()))
							m.setReplicateID(m.getReplicateID() + 1);
						replid2meas.put(m.getReplicateID(), m);
					}
				}

		return md;
	}

	/**
	 * Writes the given {@link File} in a buffered {@link FileOutputStream}.
	 * 
	 * @param excelfile
	 * @throws Exception
	 */
	private void write(File excelfile) throws Exception {
		BufferedOutputStream outStream = new BufferedOutputStream(new FileOutputStream(excelfile));
		try {
			xssfWorkbook.write(outStream);
			outStream.flush();
		} finally {
			outStream.close();
		}
	}

	private TableData getTableData() {
		TableData td = new TableData();

		for (int row = xssfWorksheet.getFirstRowNum(); row < xssfWorksheet.getLastRowNum(); row++) {
			XSSFRow r = xssfWorksheet.getRow(row);
			if (r != null)
				for (int cell = r.getFirstCellNum(); cell < r.getLastCellNum(); cell++) {
					XSSFCell c = r.getCell(cell);
					if (c != null) {
						int cellType = c.getCellType();
						if (cellType == XSSFCell.CELL_TYPE_STRING)
							td.addCellData(cell, row, c.getStringCellValue());
						else if (cellType == XSSFCell.CELL_TYPE_NUMERIC)
							td.addCellData(cell, row, c.getNumericCellValue());
						else if (cellType == XSSFCell.CELL_TYPE_FORMULA)
							td.addCellData(cell, row, c.getCellFormula());
						else if (cellType == XSSFCell.CELL_TYPE_ERROR)
							td.addCellData(cell, row, c.getErrorCellValue());
						else if (cellType == XSSFCell.CELL_TYPE_BOOLEAN)
							td.addCellData(cell, row, c.getBooleanCellValue());
					}
				}
		}
		return td;
	}
}