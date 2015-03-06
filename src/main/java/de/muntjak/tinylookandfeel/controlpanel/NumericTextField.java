/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * This file is part of the Tiny Look and Feel *
 * Copyright 2003 - 2008 Hans Bickel *
 * *
 * For licensing information and credits, please refer to the *
 * comment in file de.muntjak.tinylookandfeel.TinyLookAndFeel *
 * *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 */

package de.muntjak.tinylookandfeel.controlpanel;

import java.awt.Event;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

/**
 * NumericTextField
 * 
 * @version 1.0
 * @author Hans Bickel
 */
public class NumericTextField extends JTextField {

	private Vector listeners;
	private ActionEvent actionEvent;
	private int min, max, columns;
	private boolean resistUpdate;

	public NumericTextField(int columns, int value, int min, int max) {
		super(columns);

		this.columns = columns;
		this.min = min;
		this.max = max;
		setHorizontalAlignment(JTextField.RIGHT);

		setText("" + value);
		addKeyListener(new ArrowKeyAction(this, min, max));
		actionEvent = new ActionEvent(this, Event.ACTION_EVENT, "");
	}

	public int getValue() {
		if (getText().length() == 0)
			return 0;

		return Integer.parseInt(getText());
	}

	public void setValue(int newValue) {
		if (resistUpdate)
			return;

		setText(String.valueOf(newValue));
	}

	public void addActionListener(ActionListener l) {
		if (listeners == null) {
			listeners = new Vector();
		}

		if (listeners.contains(l))
			return;

		listeners.add(l);
	}

	public void removeActionListener(ActionListener l) {
		if (listeners == null)
			return;

		if (!listeners.contains(l))
			return;

		listeners.remove(l);
	}

	public void notifyActionListeners() {
		if (listeners == null)
			return;

		resistUpdate = true;

		Iterator ii = listeners.iterator();
		while (ii.hasNext()) {
			((ActionListener) ii.next()).actionPerformed(actionEvent);
		}

		resistUpdate = false;
	}

	protected Document createDefaultModel() {
		return new NumericDocument();
	}

	protected class NumericDocument extends PlainDocument {

		NumericDocument() {
			addDocumentListener(new KeyInputListener());
		}

		public void insertString(
							int offs,
							String str,
							AttributeSet a) throws BadLocationException {
			if (str == null || str.length() == 0)
				return;
			if (getLength() + str.length() > columns)
				return;

			if (!checkInput(str)) {
				Toolkit.getDefaultToolkit().beep();
				return;
			}

			String text = getText(0, getLength());

			if (offs == 0) {
				text = str + text;
			} else
				if (offs >= text.length()) {
					text += str;
				} else {
					text = text.substring(0, offs) + str + text.substring(offs);
				}

			int val = Integer.parseInt(text);
			boolean correct = false;

			if (val < min) {
				val = min;
				correct = true;
			} else
				if (val > max) {
					val = max;
					correct = true;
				}

			if (correct) {
				remove(0, getLength());
				super.insertString(0, String.valueOf(val), a);
			} else {
				super.insertString(offs, str, a);
			}
		}

		private boolean checkInput(String s) {
			for (int i = 0; i < s.length(); i++) {
				if (!Character.isDigit(s.charAt(i))) {
					return false;
				}
			}

			return true;
		}
	}

	class ArrowKeyAction extends KeyAdapter implements ActionListener {

		private JTextField theField;
		private javax.swing.Timer keyTimer;
		private int step;

		ArrowKeyAction(JTextField field, int min, int max) {
			theField = field;
			keyTimer = new javax.swing.Timer(20, this);
		}

		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == 38) { // up => decrease
				step = 1;
				if (e.getModifiers() == KeyEvent.SHIFT_MASK) {
					step = 10;
				}

				changeVal();
				keyTimer.setInitialDelay(300);
				keyTimer.start();
			} else
				if (e.getKeyCode() == 40) { // up => increase
					step = -1;
					if (e.getModifiers() == KeyEvent.SHIFT_MASK) {
						step = -10;
					}

					changeVal();
					keyTimer.setInitialDelay(300);
					keyTimer.start();
				}
		}

		public void keyReleased(KeyEvent e) {
			keyTimer.stop();
		}

		// the keyTimer action
		public void actionPerformed(ActionEvent e) {
			changeVal();
		}

		private void changeVal() {
			int val = Integer.parseInt(theField.getText()) + step;

			if (val > max)
				val = max;
			else
				if (val < min)
					val = min;

			// this should trigger insertUpdate()
			theField.setText("" + val);
		}
	}

	class KeyInputListener implements DocumentListener {
		public void changedUpdate(DocumentEvent e) {
		}

		public void insertUpdate(DocumentEvent e) {
			notifyActionListeners();
		}

		public void removeUpdate(DocumentEvent e) {
			notifyActionListeners();
		}
	}
}