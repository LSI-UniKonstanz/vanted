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
		
		str.append("<html>" + "Identifier: ").append(text.getId()).append("<br />").append("\n");
		str.append("Name: ").append(text.getName()).append("<br />").append("\n");
		str.append("Publication: ").append(text.getPublicationId()).append("<br />").append("\n");
		str.append("Authors:\n");
		for (String author : text.getAuthors()) {
			str.append("\t- ").append(author).append("\n");
		}
		str.append("<br />" + "Last modified: ").append(text.getLastModificationDateStr()).append("\n");
		
		return str.toString();
	}
}
