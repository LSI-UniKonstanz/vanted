/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.circle;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Category;

/**
 * @author Christian Klukas
 */
public class NullLayoutAlgorithm extends AbstractAlgorithm {

	/**
	 * Creates a new CircleLayouterAlgorithm object.
	 */
	public NullLayoutAlgorithm() {
		super();
	}

	@Override
	public void reset() {
		super.reset();
	}

	/**
	 * Returns the name of the algorithm.
	 * 
	 * @return the name of the algorithm
	 */
	public String getName() {
		return "Null-Layout";
	}

	public void execute() {
	}

	@Override
	public String getCategory() {
		return "Layout";
	}

	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(Arrays.asList(Category.GRAPH, Category.LAYOUT));
	}

	@Override
	public String getDescription() {
		return "<html>Does not change the graph layout at all. This command is only useful in some workflows";
	}

	@Override
	public boolean isLayoutAlgorithm() {
		return true;
	}
}
