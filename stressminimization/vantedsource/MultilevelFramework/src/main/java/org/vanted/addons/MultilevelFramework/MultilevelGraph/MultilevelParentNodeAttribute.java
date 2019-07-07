package org.vanted.addons.MultilevelFramework.MultilevelGraph;

import org.graffiti.attributes.AbstractAttribute;
import org.graffiti.attributes.Attribute;
import org.graffiti.graph.Node;

/**
 * Attribute to be attached to the nodes of the Multilevel Graph. Stores the
 * parent node i.e. the node in the level above the child got merged into.
 **/
public class MultilevelParentNodeAttribute extends AbstractAttribute {
	public static final String PATH = "parentNode";
	public static final String NAME = "value";
	public static final String FULLPATH = PATH + Attribute.SEPARATOR + NAME;

	protected Node parentNode;

	protected MultilevelParentNodeAttribute(String id) {
		super(id);
	}

	public MultilevelParentNodeAttribute(String id, Node parentNode) {
		super(id);
		this.parentNode = parentNode;
	}

	public MultilevelParentNodeAttribute() {
		super();
	}

	@Override
	public void setDefaultValue() {
		parentNode = null;
	}

	@Override
	public Object getValue() {
		return parentNode;
	}

	@Override
	public Object copy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void doSetValue(Object v) throws IllegalArgumentException {
		parentNode = (Node) v;
	}

}
