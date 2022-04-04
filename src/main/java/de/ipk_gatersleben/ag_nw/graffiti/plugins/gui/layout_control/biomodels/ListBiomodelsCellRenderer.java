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
 */
public class ListBiomodelsCellRenderer implements ListCellRenderer<SimpleModel> {
	
	JLabel entry;
	
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
		if (!Objects.equals(value.getId(), "")){
			entry.setText(
					"<html>" + "<strong>Model Name: " + value.getName() + "</strong><br/>" + "Model ID: " + value.getId());
		} else {
			entry.setText(
					"<html>" + "<strong>Model Name: " + value.getName() + "</strong><br/>" + "Model ID: " + value.getSubmissionId());
		}

		entry.setToolTipText(getTooltipText(value));
		
		return entry;
	}
	
	private static String getTooltipText(SimpleModel text) {
		StringBuilder str = new StringBuilder();
		
		str.append("<html>"  + "Identifier: " + text.getId() + "<br />" + "\n");
		str.append("Name: " + text.getName() + "<br />" + "\n");
		str.append("Publication: " + text.getPublicationId() + "<br />" + "\n");
		str.append("Authors:\n");
		for (String author : text.getAuthors()) {
			str.append("\t- " + author + "\n");
		}
		str.append("<br />" + "Last modified: " + text.getLastModificationDateStr() + "\n");
		
		return str.toString();
	}
}
