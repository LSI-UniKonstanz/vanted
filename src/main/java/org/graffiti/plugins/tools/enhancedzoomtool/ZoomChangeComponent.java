// ==============================================================================
//
// ZoomChangeComponent.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: ZoomChangeComponent.java,v 1.6 2010/12/22 13:06:20 klukas Exp $

package org.graffiti.plugins.tools.enhancedzoomtool;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;

import org.graffiti.plugin.gui.AbstractGraffitiComponent;
import org.graffiti.plugin.view.View;
import org.graffiti.plugin.view.ViewListener;
import org.graffiti.plugin.view.ZoomListener;
import org.graffiti.session.Session;
import org.graffiti.session.SessionListener;

/**
 * DOCUMENT ME!
 * @vanted.revision 2.7
 */
public class ZoomChangeComponent extends AbstractGraffitiComponent
		implements ActionListener, ViewListener, SessionListener {

	private static final long serialVersionUID = 1L;

	/** DOCUMENT ME! */
	private JButton combo;

	/** DOCUMENT ME! */
	private JPanel matrixPanel = new JPanel();

	/** DOCUMENT ME! */
	private Session activeSession;

	/** DOCUMENT ME! */
	private JButton okButton;

	/** DOCUMENT ME! */
	private JDialog dialog;

	/** DOCUMENT ME! */
	private JFormattedTextField m00 = new JFormattedTextField();

	/** DOCUMENT ME! */
	private JFormattedTextField m01 = new JFormattedTextField();

	/** DOCUMENT ME! */
	private JFormattedTextField m10 = new JFormattedTextField();

	/** DOCUMENT ME! */
	private JFormattedTextField m11 = new JFormattedTextField();

	public ZoomChangeComponent(String prefComp) {
		super(prefComp);

		m00.setValue(Integer.valueOf(1));
		m01.setValue(Integer.valueOf(0));

		m10.setValue(Integer.valueOf(0));

		m11.setValue(Integer.valueOf(1));

		matrixPanel.setLayout(new GridLayout(3, 3));
		matrixPanel.add(m00);
		matrixPanel.add(m01);

		matrixPanel.add(m10);

		matrixPanel.add(m11);

		matrixPanel.add(new JPanel());
		okButton = new JButton("OK");
		matrixPanel.add(okButton);

		okButton.addActionListener(this);

		combo = new JButton("Zoom: " + m00.getValue().toString() + ", " + m01.getValue().toString() + ", "
				+ m10.getValue().toString() + ", " + m11.getValue().toString());
		add(combo);

		combo.addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		if (combo.equals(e.getSource())) {
			if (dialog == null) {
				dialog = new JDialog();
				dialog.setModal(true);
				dialog.getContentPane().add(matrixPanel);
				dialog.pack();
			}

			dialog.setVisible(true);
		}

		if (okButton.equals(e.getSource())) {
			dialog.setVisible(false);

			ZoomListener zoomView = activeSession.getActiveView();
			AffineTransform at = new AffineTransform(((Number) m00.getValue()).doubleValue(),
					((Number) m10.getValue()).doubleValue(), ((Number) m01.getValue()).doubleValue(),
					((Number) m11.getValue()).doubleValue(), 0d, 0d);
			zoomView.zoomChanged(at);

			combo.setText("Zoom: " + m00.getValue().toString() + ", " + m01.getValue().toString() + ", "
					+ m10.getValue().toString() + ", " + m11.getValue().toString());
		}
	}

	public void sessionChanged(Session s) {
		activeSession = s;

		if (s != null) {
			viewChanged(s.getActiveView());
		}
	}

	public void sessionDataChanged(Session s) {
		activeSession = s;
		viewChanged(s.getActiveView());
	}

	public void viewChanged(View newView) {
		Object newZoom = newView.getZoom();
		if (newZoom instanceof AffineTransform) {
			AffineTransform at = (AffineTransform) newZoom;
			double[] matrix = new double[6];
			at.getMatrix(matrix);
			m00.setValue(Double.valueOf(matrix[0]));
			m10.setValue(Double.valueOf(matrix[1]));
			m01.setValue(Double.valueOf(matrix[2]));

			m11.setValue(Double.valueOf(matrix[3]));
		}
	}
}
