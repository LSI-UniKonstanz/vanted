/**
 * 
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.biomodels;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;

import uk.ac.ebi.biomodels.ws.SimpleModel;

/**
 * @author matthiak
 *
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

		entry.setText(
				"<html>" + "<strong>Model Name: " + value.getName() + "</strong><br/>" + "Model ID: " + value.getId());
		entry.setToolTipText(getTooltipText(value));

		return entry;
	}

	private String getTooltipText(SimpleModel text) {
		StringBuilder str = new StringBuilder();

		str.append("Identifier: " + text.getId() + "\n");
		str.append("Name: " + text.getName() + "\n");
		str.append("Publication: " + text.getPublicationId() + "\n");
		str.append("Authors:\n");
		for (String author : text.getAuthors()) {
			str.append("\t- " + author + "\n");
		}
		str.append("Last modified: " + text.getLastModificationDateStr() + "\n");

		return str.toString();
	}
}
