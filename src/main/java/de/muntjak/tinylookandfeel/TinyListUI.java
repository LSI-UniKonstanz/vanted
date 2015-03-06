/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * This file is part of the Tiny Look and Feel *
 * Copyright 2003 - 2008 Hans Bickel *
 * *
 * For licensing information and credits, please refer to the *
 * comment in file de.muntjak.tinylookandfeel.TinyLookAndFeel *
 * *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 */

package de.muntjak.tinylookandfeel;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicListUI;

/**
 * TinyListUI
 * 
 * @version 1.3
 * @author Hans Bickel
 */
public class TinyListUI extends BasicListUI {

	private JComponent list;

	public TinyListUI() {
		super();
	}

	public TinyListUI(JComponent list) {
		super();
		this.list = list;
	}

	public static ComponentUI createUI(JComponent list) {
		return new TinyListUI(list);
	}
}
