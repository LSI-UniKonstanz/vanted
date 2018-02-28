/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 11.08.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.xml_data_tree_table_model;

/**
 * @author Christian Klukas (c) 2004 IPK-Gatersleben
 */
public class SpecialTableValue {
	public double doubleValue;
	public Object otherValue;
	public boolean isWhiteCell;
	public boolean shiftRight;
	private String startString;

	public SpecialTableValue(double doubleValue, Object otherValue, boolean isWhiteCell, boolean shiftRight,
			String startString) {
		this.doubleValue = doubleValue;
		this.otherValue = otherValue;
		this.isWhiteCell = isWhiteCell;
		this.shiftRight = shiftRight;
		this.startString = startString;
	}

	@Override
	public String toString() {
		if (Double.valueOf(doubleValue).isNaN()) {
			if (otherValue != null)
				return startString + otherValue.toString();
			else
				return startString + Double.valueOf(doubleValue).toString();
		} else {
			if (shiftRight)
				return startString + Integer.valueOf((int) Math.round(doubleValue)).toString() + " ";
			else
				return startString + Double.valueOf(doubleValue).toString();
		}
	}

	/**
	 * @return
	 */
	public boolean getIsDouble() {
		return !Double.valueOf(doubleValue).isNaN();
	}
}
