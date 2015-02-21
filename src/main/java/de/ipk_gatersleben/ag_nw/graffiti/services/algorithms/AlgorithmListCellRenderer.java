/**
 * 
 */
package de.ipk_gatersleben.ag_nw.graffiti.services.algorithms;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.graffiti.plugin.algorithm.Algorithm;

/**
 * @author matthiak
 *
 */
public class AlgorithmListCellRenderer 
extends JLabel implements ListCellRenderer<Algorithm>{

	private static final Color selectedColor = new Color(200,230,200);

	private static final Color focusColor = new Color(230,230,0);

	/**
	 * 
	 */
	public AlgorithmListCellRenderer() {
		setIconTextGap(12);
	}
	
	
	@Override
	public Component getListCellRendererComponent(
			JList<? extends Algorithm> list, Algorithm value, int index,
			boolean isSelected, boolean cellHasFocus) {
		
		if(isSelected) {
			setBackground(selectedColor);
		}
		if(cellHasFocus) {
			setBackground(focusColor);
		}
		
		setText(value.getName());	
		
		return this;
	}

	
}
