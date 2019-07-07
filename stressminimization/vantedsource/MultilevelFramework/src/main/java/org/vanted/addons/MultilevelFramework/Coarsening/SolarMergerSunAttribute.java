package org.vanted.addons.MultilevelFramework.Coarsening;

import org.graffiti.attributes.AbstractAttribute;
import org.graffiti.attributes.Attribute;
import org.graffiti.graph.Node;

/**
 * Attribute containing a node which was a sun in the solarmerger
 */
public class SolarMergerSunAttribute extends AbstractAttribute {

	public static final String path = "SunNode";
	public static final String name = "val";
	public static final String fullpath = path + Attribute.SEPARATOR + name;
	protected Node sunNode;

	protected SolarMergerSunAttribute(String id) {
		super(id);
	}

	public SolarMergerSunAttribute(String id, Node sun) {
		super(id);
		this.sunNode = sun;
	}

	public SolarMergerSunAttribute() {
		super();
	}

	@Override
	public void setDefaultValue() {
		sunNode = null;

	}

	@Override
	public Object getValue() {
		return sunNode;
	}

	@Override
	public Object copy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void doSetValue(Object v) throws IllegalArgumentException {
		sunNode = (Node) v;

	}

}
