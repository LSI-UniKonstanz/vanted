package org.vanted.addons.MultilevelFramework.MultilevelGraph;

import java.util.ArrayList;
import java.util.List;

import org.AttributeHelper;
import org.graffiti.attributes.Attribute;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Graph;
import org.graffiti.selection.Selection;

/**
 * MultilevelGraph is the primary data structure for the Multilevel algorithm.
 * It contains a list of graphs and a list of selections, which are the levels.
 * Level 0 is the input Graph or the input selection. Nodes in the
 * MultilevelGraph contain an Attribute referring to the parent node in the
 * level above.
 */

public class MultilevelGraph {

	private int levelCount; // the number of levels of the Multilevel Graph

	protected List<Graph> levels; // list containing all graph levels
	protected List<Selection> selectionLevels; // list containing all selection levels

	/**
	 * Constructor for MultilevelGraph
	 * 
	 * @param inputGraph     Graph to layout (will be placed in level 0)
	 * @param inputSelection Selection of the graph to layout
	 */
	public MultilevelGraph(Graph inputGraph, Selection inputSelection) {
		levelCount = 0;
		levels = new ArrayList<Graph>();
		selectionLevels = new ArrayList<Selection>();
		addLevel(inputGraph, inputSelection);
	}

	/**
	 * Adds a new level to the list of levels.
	 * 
	 * @param g Graph to be added
	 * @param s Selection to be added
	 */
	private void addLevel(Graph g, Selection s) {
		levels.add(g);
		selectionLevels.add(s);
		g.addInteger("", "levelNumber", levelCount);
		if (levelCount > 0) {
			g.setName(levels.get(0).getName() + " - level " + Integer.toString(levelCount));
			s.setName(levels.get(0).getName() + " - level " + Integer.toString(levelCount));
			Attribute attr = new MultilevelParentGraphAttribute(MultilevelParentGraphAttribute.NAME, g, s);
			AttributeHelper.setAttribute(levels.get(levelCount - 1), MultilevelParentGraphAttribute.PATH,
					MultilevelParentGraphAttribute.NAME, attr);
		}
		levelCount++;
	}

	/**
	 * Adds a new empty level to the list of levels and selectionLevels.
	 */
	public void addEmptyLevel() {
		Graph emptyLevel = new AdjListGraph();
		Selection emptySelection = new Selection();
		addLevel(emptyLevel, emptySelection);
	}

	/**
	 * @return Returns the number of nodes of the top level selection.
	 */
	public int getTopLevelNodeCount() {
		return selectionLevels.get(levelCount - 1).getNumberOfNodes();
	}

	/**
	 * @return Returns total number of nodes summed over all levels.
	 */
	public int getTotalNodeCount() {
		int count = 0;
		for (Graph l : levels) {
			count += l.getNumberOfNodes();
		}
		return count;
	}

	/**
	 * @return Returns number of levels of the multilevel graph.
	 */
	public int getLevelCount() {
		return levelCount;
	}

	/**
	 * @return Returns the levels of the multilevel graph as a list of graphs.
	 */
	public List<Graph> getLevels() {
		return levels;
	}

	/**
	 * @return Returns the selection levels if the multilevel graph as a list of
	 *         selections.
	 */
	public List<Selection> getSelectionLevels() {
		return selectionLevels;
	}

}
