/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 16.06.2005 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;

import org.AttributeHelper;

/**
 * An extension of {@linkplain JLabel} to support interactive links.
 * 
 * @vanted.revision 2.7.0
 *
 */
public class JLabelHTMLlink extends JLabel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8236317065471341173L;
	String labelText;
	boolean defaultTooltip = false;

	/**
	 * Create a Swing HTML link.
	 * 
	 * @param label of the link
	 * @param url   of the link
	 * @vanted.revision 2.7.0
	 */
	public JLabelHTMLlink(String label, final String url) {
		this(label, null, new Runnable() {
			@Override
			public void run() {
				AttributeHelper.showInBrowser(url);
			}
		});
		setUrl(url);
	}

	public JLabelHTMLlink(String label, String tooltip, final Runnable runOnClick) {
		super("<html>" + label);
		labelText = label;
		defaultTooltip = tooltip == null;
		if (tooltip != null)
			setToolTipText(tooltip);
		setForeground(Color.BLUE);
		Cursor c = new Cursor(Cursor.HAND_CURSOR);
		setCursor(c);
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				runOnClick.run();
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				setText("<html><u>" + labelText);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				setText("<html>" + labelText);
			}
		});
	}

	public JLabelHTMLlink(String htmlText, final String url, String tooltip) {
		this(htmlText, url, tooltip, true);
	}

	public JLabelHTMLlink(String htmlText, final String url, String tooltip, final boolean highlight) {
		super(htmlText);
		defaultTooltip = tooltip == null;
		if (tooltip != null)
			setToolTipText(tooltip);
		setUrl(url);
		Cursor c = new Cursor(Cursor.HAND_CURSOR);
		setCursor(c);
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				AttributeHelper.showInBrowser(urlLink);
			}

			Color oldColor;
			boolean oldOpaque;

			@Override
			public void mouseEntered(MouseEvent e) {
				if (!highlight || url == null || url.length() <= 0)
					return;
				oldOpaque = isOpaque();
				setOpaque(true);
				oldColor = getBackground();
				setBackground(new Color(240, 240, 255));
			}

			@Override
			public void mouseExited(MouseEvent e) {
				if (!highlight)
					return;
				setOpaque(oldOpaque);
				setBackground(oldColor);
			}
		});
	}

	private String urlLink;

	public void setUrl(String url) {
		urlLink = url;
		if (defaultTooltip)
			setToolTipText("Open " + url);
	}

	public void setLabelText(String text) {
		labelText = text;
		setText(text);
	}

}
