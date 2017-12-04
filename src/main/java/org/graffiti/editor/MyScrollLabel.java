/*
 * Created on 21.09.2005 by Christian Klukas
 */
package org.graffiti.editor;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.vanted.scaling.Toolbox;
import org.vanted.scaling.scalers.component.JTextComponentScaler;

public class MyScrollLabel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	public MyScrollLabel(String msg) {
		this.setLayout(new BorderLayout());
		this.setPreferredSize(new Dimension(500, 300));
		final JScrollPane jsp = new JScrollPane(this.getJEditorPane(msg));
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				jsp.getVerticalScrollBar().setValue(0);
			}
		});		
		add(jsp, BorderLayout.CENTER);
		validate();
	}
	
	public MyScrollLabel(String msg, double width, double height) {
		this.setLayout(TableLayout.getLayout(width, height));
		final JScrollPane jsp = new JScrollPane(this.getJEditorPane(msg));
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				jsp.getVerticalScrollBar().setValue(0);
			}
		});
		add(jsp, "0,0");
		validate();
	}
	
	private JEditorPane getJEditorPane(String msg) {
		JEditorPane jep = new JEditorPane("text/html", msg);
		jep.setEditable(false);
		JTextComponentScaler.alignJEP(jep);
		Toolbox.scaleJEditorPaneUnorderedLists(jep, Toolbox.UL_TYPE_DISC, null);
		
		return jep;
	}
}
