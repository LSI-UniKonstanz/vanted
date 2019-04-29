package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.svg_exporter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.graffiti.editor.GravistoService;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.parameter.IntegerParameter;
import org.graffiti.plugin.parameter.ObjectListParameter;
import org.graffiti.plugin.parameter.Parameter;

public class PDFSVGAlgorithm extends AbstractAlgorithm {

	private static ExportType type = ExportType.PDF;
	private static int border = 0;

	@Override
	public Parameter[] getParameters() {
		return new Parameter[] {
				new ObjectListParameter(type, "Vectorgraphics type", "Choose the type of vector graphics to be used",
						ExportType.values()),
				new IntegerParameter(border, "Add image border (pixel)",
						"<html>Adds free space to the right and lower border of the image.") };

	}

	@Override
	public void setParameters(Parameter[] params) {
		int idx = 0;
		type = (ExportType) params[idx++].getValue();
		border = ((IntegerParameter) params[idx++]).getInteger();
	}

	@Override
	public void execute() {
		switch (type) {
		case PDF:
			GravistoService.getInstance().runAlgorithm(new PDFAlgorithm(border), null);
			break;
		case SVG:
			GravistoService.getInstance().runAlgorithm(new SVGAlgorithm(border), null);
			break;
		}

	}

	@Override
	public String getName() {
		return "Create PDF/SVG";
	}

	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(Arrays.asList(Category.IMAGING, Category.EXPORT));
	}

	private enum ExportType {
		PDF, SVG
	}

}
