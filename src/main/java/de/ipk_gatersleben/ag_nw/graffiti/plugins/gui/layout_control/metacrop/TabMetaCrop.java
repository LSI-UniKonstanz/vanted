package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.metacrop;

import org.AttributeHelper;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.view.GraphView;
import org.graffiti.plugin.view.View;

@SuppressWarnings("nls")
public class TabMetaCrop extends PathwayWebLinkTab {
	
	private static final long serialVersionUID = 1L;
	
	public TabMetaCrop() {
		
		super("MetaCrop", "http://vanted.ipk-gatersleben.de/addons/metacrop/gml/", "pathways", "pathway", "http://metacrop.ipk-gatersleben.de/", false);
		
	}
	
	public TabMetaCrop(String title, String url, String content, String contentSingle, String infoURL, boolean ommitEmptyGroupItems, String downloadButtonText) {
		
		super(title, url, content, contentSingle, infoURL, ommitEmptyGroupItems, downloadButtonText);
		
	}
	
	@Override
	public void addAnnotationsToGraphElements(Graph graph) {
		
		// 'graph' should have set a reference url
		// this is just a workaround in case there is no reference url
		// try to create the reference url based on name first
		// fallback for reference url is http://metacrop.ipk-gatersleben.de/
		if (!AttributeHelper.hasAttribute(graph, "url")) {
			String refURL = getPathwayReference(graph.getName(true), "http://metacrop.ipk-gatersleben.de/");
			AttributeHelper.setReferenceURL(graph, refURL);
		}
		// each 'node' in a gml file should have set a reference url
		// 'sbmlID' should always be empty
		// the gml files shouldn't contain any sbml ids
		// this is just a workaround in case there is no reference url
		String prefURL = "http://bioinformatics.ipk-gatersleben.de/pls/htmldb_pgrc/f?p=metacrop:10:::NO::P10_PATHWAY_OBJECT_ID:";
		for (Node node : graph.getNodes()) {
			String sbmlID = AttributeHelper.getSBMLid(node);
			if (sbmlID != null && sbmlID.trim().length() > 0)
				AttributeHelper.setReferenceURL(node, prefURL + sbmlID);
		}
		
	}
	
	private String getPathwayReference(String paramPathwayName, String returnIfUnknown) {
		
		String[] knownNamesAndIDs = new String[] {
				"Alanine degradation;123",
				"Alanine, Valine, Leucine biosynthesis;12",
				"Arabinoxylan, Beta-Glucan, Cellulose biosynthesis;50",
				"Arginine biosynthesis;19",
				"Arginine degradation;122",
				"Ascorbate biosynthesis;24",
				"Ascorbate-Glutathione cycle;25",
				"Asparagine biosynthesis;74",
				"Asparagine degradation;146",
				"Aspartate degradation;145",
				"C4-metabolism (NADP-ME subtype);115",
				"Calvin cycle;37",
				"Calvin cycle (Zea mays);133",
				"Chlorogenic acid biosynthesis;28",
				"Cyclic photophosphorylation;132",
				"Cysteine degradation;141",
				"Fatty acid biosynthesis;35",
				"Fermentation;32",
				"Folate biosynthesis;112",
				"Fructan biosynthesis;30",
				"GDP sugars;129",
				"GS-GOGAT cycle;63",
				"Glutathione biosynthesis;26",
				"Glycine degradation;144",
				"Glycolysis, Gluconeogenesis;34",
				"Glyoxylate cycle;84",
				"Histidine biosynthesis;16",
				"Isoleucine biosynthesis;13",
				"Isoleucine degradation;126",
				"Leucine degradation;125",
				"Lysine biosynthesis;18",
				"Lysine degradation;147",
				"Methionine biosynthesis;88",
				"Methionine degradation;136",
				"Methionine recycling;58",
				"NAD+ NADP+ de novo biosynthesis;48",
				"Non-cyclic photophosphorylation;131",
				"Oxidative phosphorylation;113",
				"Pentose phosphate pathway;91",
				"Phenylalanine degradation;137",
				"Phenylalanine, Tyrosine, Tryptophan biosynthesis;10",
				"Photorespiration;148",
				"Photorespiration C4;134",
				"Proline biosynthesis;20",
				"Proline degradation;140",
				"Purine de novo biosynthesis;22",
				"Pyrimidine de novo biosynthesis;23",
				"Serine degradation;143",
				"Serine, Glycine, Cysteine biosynthesis;11",
				"Shikimate biosynthesis;9",
				"Starch metabolism (monocots);99",
				"Sucrose breakdown pathway (dicots);4",
				"Sucrose breakdown pathway (monocots);65",
				"Sugar metabolism;31",
				"TAG biosynthesis (simpl.);49",
				"TCA cycle;68",
				"Threonine biosynthesis;14",
				"Threonine degradation;142",
				"Tryptophan degradation;139",
				"Tyrosine degradation;138",
				"UDP sugars;128",
				"Valine degradation;124"
		};
		String prefURL = "http://bioinformatics.ipk-gatersleben.de/pls/htmldb_pgrc/f?p=metacrop:7:::NO::P7_PATHWAY_ID:";
		
		String pathwayName = paramPathwayName.toUpperCase();
		if (pathwayName.endsWith(".GML"))
			pathwayName = pathwayName.substring(0, pathwayName.length() - ".GML".length());
		for (String knownNameAndID : knownNamesAndIDs) {
			if (knownNameAndID.indexOf(";") < 0)
				continue;
			knownNameAndID = knownNameAndID.toUpperCase();
			String knownName = knownNameAndID.substring(0, knownNameAndID.lastIndexOf(";"));
			String knownID = knownNameAndID.substring(knownNameAndID.lastIndexOf(";") + ";".length());
			if (knownName.length() <= 0 || knownID.length() <= 0)
				continue;
			if (pathwayName.endsWith(knownName)) {
				return prefURL + knownID;
			}
		}
		return returnIfUnknown;
		
	}
	
	@Override
	public boolean visibleForView(View v) {
		
		return v == null || v instanceof GraphView;
		
	}
	
	@Override
	protected String[] getValidExtensions() {
		
		return new String[] { ".gml", ".graphml" };
		
	}
	
}
