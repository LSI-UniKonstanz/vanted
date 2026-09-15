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


/**
 * Renders a {@link SimpleModel} in the results list, showing the model name, identifier,
 * format and a curation badge.
 *
 * @author matthiak
 * @vanted.revision 2.8.3
 */
public class ListBiomodelsCellRenderer implements ListCellRenderer<SimpleModel> {

	private final JLabel entry;

	public ListBiomodelsCellRenderer() {
		entry = new JLabel();
		entry.setOpaque(true);
		entry.setBorder(new EmptyBorder(6, 8, 6, 8));
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends SimpleModel> list, SimpleModel value, int index,
			boolean isSelected, boolean cellHasFocus) {

		if (isSelected) {
			entry.setBackground(list.getSelectionBackground());
			entry.setForeground(list.getSelectionForeground());
		} else {
			entry.setBackground(index % 2 == 0 ? Color.WHITE : new Color(245, 247, 250));
			entry.setForeground(list.getForeground());
		}

		String badgeColor = value.isCurated() ? "#1a7f37" : "#9a6700";
		String badge = "<span style='color:" + badgeColor + ";'>● " + value.getCurationStatus() + "</span>";
		String format = (value.getFormat() == null || value.getFormat().isEmpty())
				? "" : " &nbsp;·&nbsp; " + value.getFormat();

		entry.setText("<html><b>" + escape(value.getName()) + "</b><br/>"
				+ "<span style='color:gray;'>" + escape(value.getId()) + "</span>"
				+ format + " &nbsp;·&nbsp; " + badge + "</html>");

		entry.setToolTipText(getTooltipText(value));
		return entry;
	}

	private static String getTooltipText(SimpleModel model) {
		return "<html>"
				+ "<b>" + escape(model.getName()) + "</b><br/>"
				+ "Identifier: " + escape(model.getId()) + "<br/>"
				+ "Format: " + escape(nullToDash(model.getFormat())) + "<br/>"
				+ "Curation: " + model.getCurationStatus() + "<br/>"
				+ "Submitter: " + escape(nullToDash(model.getSubmitter())) + "<br/>"
				+ "Last modified: " + escape(nullToDash(model.getLastModificationDateStr()))
				+ "</html>";
	}

	private static String nullToDash(String s) {
		return (s == null || s.isEmpty()) ? "—" : s;
	}

	private static String escape(String s) {
		if (s == null)
			return "";
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}
}
