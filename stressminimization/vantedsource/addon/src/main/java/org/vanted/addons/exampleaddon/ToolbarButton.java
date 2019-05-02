/*************************************************************************************
 * The Exemplary Add-on is (c) 2008-2011 Plant Bioinformatics Group, IPK Gatersleben,
 * http://bioinformatics.ipk-gatersleben.de
 * The source code for this project, which is developed by our group, is
 * available under the GPL license v2.0 available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html. By using this
 * Add-on and VANTED you need to accept the terms and conditions of this
 * license, the below stated disclaimer of warranties and the licenses of
 * the used libraries. For further details see license.txt in the root
 * folder of this project.
 ************************************************************************************/
package org.vanted.addons.exampleaddon;

import info.clearthought.layout.TableLayout;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JToolBar;

import org.ErrorMsg;
import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.gui.GraffitiComponent;
import org.graffiti.plugin.view.View;
import org.graffiti.plugin.view.ViewListener;
import org.graffiti.session.EditorSession;
import org.graffiti.session.Session;
import org.graffiti.session.SessionListener;

/**
 * This {@link Component} allows to specify a toolbar, where you can add
 * anything you want, such as JCheckBoxes, JButtons and so on. This one here
 * will be shown, when a seesion is open.
 * 
 * @author Hendrik Rohn
 */
public class ToolbarButton extends JToolBar
					implements
					GraffitiComponent,
					SessionListener,
					ViewListener {
	private static final long serialVersionUID = 1L;

	private JButton arrange;
	private String prefComp;

	private WindowOrder order;

	/**
	 * This is the actual Toolbar (Component).
	 * 
	 * @param prefComp
	 */
	public ToolbarButton(String prefComp, WindowOrder order) {
		super("Arranging");
		this.prefComp = prefComp;

		this.order = order;
		arrange = new JButton(order.toString());
		// arrange.setRolloverEnabled(true);
		arrange.setOpaque(false);
		arrange
							.setToolTipText("<html>Arranges windows.<br>"
												+ "Press Shift key and click here to restore minimized windows.<br>"
												+ "Press Ctrl key and click here to maximize the active window and to minimize all others.");

		setOpaque(false);
		double size[][] = { { 30 }, { 30 } };
		setLayout(new TableLayout(size));
		add(arrange, "0,0");

		arrange.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				arrangeIt(actionEvent);
			}
		});

		validate();
	}

	public void sessionChanged(Session s) {
		if (s != null)
			viewChanged(s.getActiveView());
		else {
			arrange.setVisible(getVis());
		}
	}

	public void sessionDataChanged(Session s) {
		sessionChanged(s);
	}

	public void viewChanged(View newView) {
		if (newView != null) {
			arrange.setVisible(getVis());
		}
	}

	/**
	 * @return
	 */
	private boolean getVis() {
		switch (order) {
			case HORIZONTAL:
				return MainFrame.getInstance().getDesktop().getAllFrames().length > 1;
			case VERTICAL:
				return MainFrame.getInstance().getDesktop().getAllFrames().length > 1;
			case QUADRATIC:
				return MainFrame.getInstance().getDesktop().getAllFrames().length > 2;
		}
		return true;
	}

	public String getPreferredComponent() {
		return prefComp;
	}

	/**
	 * Arranges all opened windows by accessing the Desktop and getting its size
	 * and frames.
	 */
	private void arrangeIt(ActionEvent actionEvent) {
		for (EditorSession es : MainFrame.getEditorSessions()) {
			if (es.getGraph() != null) {
				if ((actionEvent.getModifiers() & ActionEvent.SHIFT_MASK) > 0)
					restoreFrames();
				if ((actionEvent.getModifiers() & ActionEvent.CTRL_MASK) > 0)
					iconizeFrames();

				Dimension desktopdim = MainFrame.getInstance().getDesktop()
									.getSize();

				int number = getOpenFrameCnt(), cnt = 0;

				if (number == 0)
					number = 1;

				// calculate frame positions

				switch (order) {
					case HORIZONTAL:
						for (JInternalFrame jf : MainFrame.getInstance()
											.getDesktop().getAllFrames()) {
							try {
								jf.setMaximum(false);
							} catch (PropertyVetoException e) {
							}
							jf.setBounds(desktopdim.width * (cnt++) / number,
												0, desktopdim.width / number,
												desktopdim.height);
						}
						break;
					case VERTICAL:
						for (JInternalFrame jf : MainFrame.getInstance()
											.getDesktop().getAllFrames()) {
							try {
								jf.setMaximum(false);
							} catch (PropertyVetoException e) {
							}
							jf.setBounds(0, desktopdim.height * (cnt++)
												/ number, desktopdim.width,
												desktopdim.height / number);
						}
						break;
					case QUADRATIC:
						int inRow = (int) Math.ceil(Math.sqrt(number));
						int rows = (int) Math.ceil((double) number
											/ (double) inRow);
						int row = 0,
						col = 0;
						for (JInternalFrame jf : MainFrame.getInstance()
											.getDesktop().getAllFrames()) {
							try {
								jf.setMaximum(false);
							} catch (PropertyVetoException e) {
							}
							jf.setBounds(desktopdim.width * (col + 0) / inRow,
												(row + 0) * desktopdim.height / rows,
												desktopdim.width / inRow, desktopdim.height
																	/ rows);
							col++;
							if (col >= inRow) {
								col = 0;
								row++;
							}
						}
						break;
				}

			}
		}
	}

	/**
	 * 
	 */
	private void iconizeFrames() {
		JInternalFrame self = MainFrame.getInstance().getDesktop()
							.getSelectedFrame();
		if (self != null) {
			for (JInternalFrame jf : MainFrame.getInstance().getDesktop()
								.getAllFrames()) {
				if (jf != self) {
					try {
						jf.setIcon(true);
					} catch (PropertyVetoException e) {
						ErrorMsg.addErrorMessage(e);
					}
				}
			}
		}
	}

	/**
	 * @return Number of not iconified windows
	 */
	private int getOpenFrameCnt() {
		int res = 0;
		for (JInternalFrame jf : MainFrame.getInstance().getDesktop()
							.getAllFrames()) {
			if (!jf.isIcon())
				res++;
		}
		return res;
	}

	/**
	 * De-iconify and de-maximize all frames.
	 */
	private void restoreFrames() {

		JInternalFrame[] frames = MainFrame.getInstance().getDesktop()
							.getAllFrames();

		for (int i = 0; i < frames.length; i++)
			try {
				frames[i].setMaximum(false);
				frames[i].setIcon(false);
			} catch (PropertyVetoException e) {
				ErrorMsg.addErrorMessage(e);
			}
	}

}