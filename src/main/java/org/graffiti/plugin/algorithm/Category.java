/**
 * 
 */
package org.graffiti.plugin.algorithm;

/**
 * An enum containing a set of categories for algorithms These fixed categories
 * enable possiblities of grouping and searching algorithms
 * Algorithms can be part of multiple categories
 * 
 * @author matthiak
 */
public enum Category {
	
	/*
	 * special category for algorithms, that are present in Vanted and could be
	 * usefull for certain tasks but shouldnt appear in the menus. but the user
	 * still be able to see them, if he's an advanced user Currently, the idea is,
	 * to have a global preference setting, when enabled, the algorithms will appear
	 */
	HIDDEN("Hidden",
			"Algorihm is known to the system but hidden from the menu by standard (changeable through preferences)"),
	/*
	 * normal categories that algororithms should fall into
	 */
	GRAPH("Graph", "Algorithm working on graphs"), EDGE("Graph-Edges", "Algorithm working on graph edges"), NODE("Graph-Nodes",
			"Algorithm working on graph nodes"), CLUSTER("Cluster", "Algorithm performing clustering tasks on graphs or data"), COMPUTATION("Computation",
					"Algorithm performing computational related tasks on graphs, graph elements, or data"), ANALYSIS("Analysis",
							"Algorithm performs an analysis on a graph or data"), DATA("Data", "Algorithm working with numerical data attached to graph"), ANNOTATION(
									"Annotation",
									"Algorithm performing annotation tasks (labeling, hierarchies, ontologies) on graph, graph elements, or data"), MAPPING("Mapping",
											"Algorithm performing mapping tasks (in)between network and data domains (network-network / network-data / data-data)"), LAYOUT(
													"Layout", "Algorithm performing layout on networks or data"), VISUAL("Visual",
															"Algorithm applying/changing visual properties to graph elements or diagrams"), CHART("Chart",
																	"Algorithm performing creation or manipulation of charts"), UI("User Interface",
																			"Algorithm performing tasks involving the user interface"), SELECTION("Selection",
																					"Algorithm performing selection operations on network elements or data sets"), SEARCH("Search",
																							"Algorithm performs search operation on a graph or data"), IMAGING("Imaging",
																									"Algorithm creating images from graphs or data"), COMMUNICATION("Communication",
																											"Algorithm performing kinds of communication e.g. network sockets, mail, etc."), STATISTICS(
																													"Statistics",
																													"Algorithm creating statistical data based on experimental data or network properties"), IMPORT(
																															"Import", "Algorithm importing data"), EXPORT("Export",
																																	"Algorithm exporting data"), ENRICHMENT("Enrichment",
																																			"Algorithm does enrichment operations on hierarchical networks"), HIERARCHY(
																																					"Hierachy",
																																					"Algorithm creating hierarchical networks upon existing networks (Ontologies, Hierarchies, etc.)");
	
	private String name;
	private String description;
	
	private Category(String name, String description) {
		this.name = name;
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String toString() {
		return name;
	}
}
