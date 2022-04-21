/**
 * 
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.biomodels;

import java.awt.Color;
import java.awt.Component;
import java.util.Objects;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;


/**
 * @author matthiak
 * @vanted.revision 2.8.3
 */
public class ListBiomodelsCellRenderer implements ListCellRenderer<SimpleModel> {
	
	final JLabel entry;
	
	/**
	 * 
	 */
	public ListBiomodelsCellRenderer() {
		entry = new JLabel();
		entry.setOpaque(true);
		entry.setBorder(new EmptyBorder(4, 4, 4, 4));
	}
	
	@Override
	public Component getListCellRendererComponent(JList<? extends SimpleModel> list, SimpleModel value, int index,
			boolean isSelected, boolean cellHasFocus) {
		
		if (cellHasFocus)
			entry.setBackground(new Color(200, 200, 200));
		else
			entry.setBackground(Color.WHITE);
		
		if (isSelected)
			entry.setBackground(new Color(200, 255, 200));
		else
			entry.setBackground(Color.WHITE);

			entry.setText("<html>" + "<strong>Model Name: " + value.getName()
					+ "</strong><br/>" + "Model ID: " + value.getId());

		entry.setToolTipText(getTooltipText(value));
		
		return entry;
	}
	
	private static String getTooltipText(SimpleModel text) {

		return "<html>" + "Identifier: " + text.getId() + "<br />" + "\n" +
				"Name: " + text.getName() + "<br />" + "\n" +
				"Submitter: " + text.getSubmitter() + "<br />" + "\n" +
				"Last modified: " + text.getLastModificationDateStr() + "\n";
	}
}
