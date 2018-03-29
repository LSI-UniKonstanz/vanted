package org.graffiti.plugin.parameter;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.ListCellRenderer;

/**
 * @author klukas
 * @version $Revision: 1.8 $
 */
public class ObjectListParameter extends AbstractSingleParameter {
	private Collection<?> possibleValues;
	private ListCellRenderer<Object> renderer;

	public ObjectListParameter(Object val, String name, String description, Collection<?> possibleValues) {
		super(val, name, description);
		this.possibleValues = possibleValues;
	}

	public ObjectListParameter(Object val, String name, String description, Object[] values) {
		super(val, name, description);
		ArrayList<Object> va = new ArrayList<Object>();
		for (Object o : values)
			va.add(o);
		this.possibleValues = va;
	}

	public Collection<?> getPossibleValues() {
		return possibleValues;
	}

	public void setRenderer(ListCellRenderer<Object> renderer) {
		this.renderer = renderer;
	}

	public ListCellRenderer<Object> getRenderer() {
		return renderer;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
