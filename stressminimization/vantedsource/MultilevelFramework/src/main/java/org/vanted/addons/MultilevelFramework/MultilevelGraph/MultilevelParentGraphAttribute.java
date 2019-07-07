package org.vanted.addons.MultilevelFramework.MultilevelGraph;

import org.graffiti.attributes.AbstractAttribute;
import org.graffiti.attributes.Attribute;
import org.graffiti.graph.Graph;
import org.graffiti.selection.Selection;

/**
 * Attribute to be attached to the levels (these are graphs) of the Multilevel
 * Framework. Stores the level "above" i.e. the level that resulted from merging
 * the graph.
 */
public class MultilevelParentGraphAttribute extends AbstractAttribute {
	public static final String PATH = "parentGraph";
	public static final String NAME = "value";
	public static final String FULLPATH = PATH + Attribute.SEPARATOR + NAME;

	protected Graph parentGraph;
	protected Selection parentSelection;

	protected MultilevelParentGraphAttribute(String id) {
		super(id);
	}

	public MultilevelParentGraphAttribute(String id, Graph parentGraph, Selection parentSelection) {
		super(id);
		this.parentGraph = parentGraph;
		this.parentSelection = parentSelection;
	}

	public MultilevelParentGraphAttribute() {
		super();
	}

	@Override
	public void setDefaultValue() {
		parentGraph = null;
		parentSelection = null;
	}

	@Override
	public Object getValue() {
		return new Object[] { parentGraph, parentSelection };
	}

	@Override
	public Object copy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void doSetValue(Object v) throws IllegalArgumentException {
		Object[] list = (Object[]) v;
		parentGraph = (Graph) list[0];
		parentSelection = (Selection) list[1];
	}

}
