package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.svg_exporter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.AttributeHelper;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.algorithm.Category;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.launch_gui.LaunchGui;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.ppt_exporter.PPTAlgorithm;

public class GraphicExport extends LaunchGui {
	
	@Override
	protected Collection<Algorithm> getAlgorithms() {
		ArrayList<Algorithm> res = new ArrayList<Algorithm>();
		res.add(new PngJpegAlgorithm(true));
		res.add(new PngJpegAlgorithm(false));
		// res.add(new PDFSVGAlgorithm());
		
		boolean addAutomaticallyBorder = false;
		if (graph != null)
			addAutomaticallyBorder = (Boolean) AttributeHelper.getAttributeValue(graph, "", "background_coloring",
					Boolean.valueOf(false), Boolean.valueOf(false), false);
		
		res.add(new SVGAlgorithm(addAutomaticallyBorder ? 50 : 0));
		res.add(new PDFAlgorithm(addAutomaticallyBorder ? 50 : 0));
		res.add(new PPTAlgorithm());
		return res;
	}
	
	@Override
	public String getName() {
		return "Network as Image";
	}
	
	@Override
	public String getCategory() {
		return "File.Export";
	}
	
	@Override
	public Set<Category> getSetCategory() {
		return null;
	}
	
}