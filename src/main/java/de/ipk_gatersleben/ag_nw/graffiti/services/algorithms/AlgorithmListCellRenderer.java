/**
 * 
 */
package de.ipk_gatersleben.ag_nw.graffiti.services.algorithms;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
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

	private static final Color selectedColor = new Color(250,250,150);


	private static final Color defaultColor = new Color(255,255,255);

	/**
	 * 
	 */
	public AlgorithmListCellRenderer() {
		setIconTextGap(12);
		setOpaque(true);
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	}
	
	
	@Override
	public Component getListCellRendererComponent(
			JList<? extends Algorithm> list, Algorithm value, int index,
			boolean isSelected, boolean cellHasFocus) {
		setBackground(defaultColor);
		if(isSelected) {
			setBackground(selectedColor);
		}
		
		setText(value.getName());	
		
		return this;
	}

	
}
