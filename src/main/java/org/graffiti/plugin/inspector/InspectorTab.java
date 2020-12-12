// ==============================================================================
//
// InspectorTab.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: InspectorTab.java,v 1.15 2012/07/13 13:34:02 klapperipk Exp $

package org.graffiti.plugin.inspector;

import java.awt.Color;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.border.Border;

import org.ErrorMsg;
import org.apache.log4j.Logger;
import org.graffiti.graph.GraphElement;
import org.graffiti.plugin.view.View;
import org.graffiti.selection.SelectionListener;
import org.vanted.scaling.Toolbox;

/**
 * An <code>InspectorTab</code> is a generic component for an
 * <code>InspectorPlugin</code>.
 * 
 * @see JComponent
 * @see InspectorPlugin
 */
public abstract class InspectorTab extends JComponent implements ComponentListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6610404454815754499L;

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(InspectorTab.class);

	public static final int TAB_LEADING = Integer.MIN_VALUE;
	public static final int TAB_TRAILING = Integer.MAX_VALUE;
	public static final int TAB_RANDOM = 0;

	/*
	 * a list of standard preference keys for use with preferences standard naming
	 */
	public static final String PREFERENCE_TAB_SHOW = "Show";

	// ~ Instance fields ========================================================

	/**
	 * The panel that holds the table of the attributes and the buttons for adding
	 * and removing attributes as well as the "apply" button.
	 */
	public EditPanel editPanel;

	/**
	 * The title of the <code>InspectorTab</code> which will appear as the title of
	 * the tab.
	 */
	protected String title;

	private ImageIcon icon;

	private int preferredTabPosition = 0;
	
	// ~ Methods ================================================================

	/**
	 * 
	 */
	public InspectorTab() {
		addComponentListener(this);
	}

	/**
	 * Returns the EditPanel of this tab.
	 * 
	 * @return DOCUMENT ME!
	 */
	public EditPanel getEditPanel() {
		return this.editPanel;
	}

	/**
	 * Returns the title of the current <code>InspectorTab</code>.
	 * 
	 * @return the title of the current <code>InspectorTab</code>.
	 */
	public String getTitle() {
		return this.title;
	}

	@Override
	public String getName() {
		return getTitle();
	}

	public abstract boolean visibleForView(View v);

	private boolean currentlyHighlight = false;

	public void focusAndHighlight(final InspectorTab whenFinishedHighlight, final boolean highlight,
			final boolean cycleChildren) {
		final int time = 800;
		if (currentlyHighlight)
			return;
		currentlyHighlight = true;

		if (getParent() != null) {
			((JTabbedPane) getParent()).setSelectedComponent(this);

			final Border oldB = getBorder();
			final InspectorTab fit = this;
			if (whenFinishedHighlight != null)
				whenFinishedHighlight.focusAndHighlight(null, false, cycleChildren);
			if (highlight)
				setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.RED, 4), getTitle()));
			repaint();
			Runnable r = new Runnable() {
				public void run() {
					try {
						try {
							Thread.sleep(time);
						} catch (InterruptedException e) {
							ErrorMsg.addErrorMessage(e);
						}
						if (highlight)
							fit.setBorder(oldB);
						fit.repaint();
						if (whenFinishedHighlight != null) {
							whenFinishedHighlight.focusAndHighlight(null, highlight, cycleChildren);
							if (whenFinishedHighlight instanceof ContainsTabbedPane) {
								ContainsTabbedPane sh = (ContainsTabbedPane) whenFinishedHighlight;
								if (cycleChildren) {
									cycleHighlight(whenFinishedHighlight, highlight, oldB, sh);
								}
							}
						} else {
							if (cycleChildren && fit instanceof SubtabHostTab) {
								SubtabHostTab sh = (SubtabHostTab) fit;
								cycleHighlight(sh, highlight, oldB, sh);
							}
						}
					} finally {
						currentlyHighlight = false;
					}
				}

				private void cycleHighlight(final InspectorTab tab, final boolean highlight, final Border oldB,
						ContainsTabbedPane sh) {
					if (highlight)
						tab.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.RED, 4),
								tab.getTitle()));
					tab.repaint();
					JTabbedPane jtp = sh.getTabbedPane();
					for (int i = 0; i < jtp.getTabCount(); i++) {
						jtp.setSelectedIndex(i);
						try {
							Thread.sleep(time / jtp.getTabCount());
						} catch (InterruptedException e) {
							ErrorMsg.addErrorMessage(e);
						}
					}
					jtp.setSelectedIndex(0);
					if (highlight)
						tab.setBorder(oldB);
					tab.repaint();
				}
			};
			Thread t = new Thread(r);
			t.setName(getName());
			t.start();
			return;
		}
	}

	public static void focusAndHighlightComponent(final JComponent thisss, final String title,
			final InspectorTab whenFinishedHighlight, final boolean highlight, final boolean cycleChildren) {
		final int time = 800;
		JTabbedPane tp = (JTabbedPane) thisss.getParent();
		if (tp != null) {
			tp.setSelectedComponent(thisss);
			final Border oldB = thisss.getBorder();

			if (whenFinishedHighlight != null)
				whenFinishedHighlight.focusAndHighlight(null, false, cycleChildren);
			if (highlight)
				thisss.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.RED, 4), title));
			thisss.repaint();
			Runnable r = new Runnable() {
				public void run() {
					try {
						Thread.sleep(time);
					} catch (InterruptedException e) {
						ErrorMsg.addErrorMessage(e);
					}
					if (highlight)
						thisss.setBorder(oldB);
					thisss.repaint();
					if (whenFinishedHighlight != null) {
						whenFinishedHighlight.focusAndHighlight(null, highlight, cycleChildren);
						if (whenFinishedHighlight instanceof ContainsTabbedPane) {
							ContainsTabbedPane sh = (ContainsTabbedPane) whenFinishedHighlight;
							if (cycleChildren) {
								cycleHighlight(whenFinishedHighlight, highlight, oldB, sh);
							}
						}
					} else {
						if (cycleChildren && thisss instanceof SubtabHostTab) {
							SubtabHostTab sh = (SubtabHostTab) thisss;
							cycleHighlight(sh, highlight, oldB, sh);
						}
					}
				}

				private void cycleHighlight(final InspectorTab tab, final boolean highlight, final Border oldB,
						ContainsTabbedPane sh) {
					if (highlight)
						tab.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.RED, 4),
								tab.getTitle()));
					tab.repaint();
					JTabbedPane jtp = sh.getTabbedPane();
					for (int i = 0; i < jtp.getTabCount(); i++) {
						jtp.setSelectedIndex(i);
						try {
							Thread.sleep(time / jtp.getTabCount());
						} catch (InterruptedException e) {
							ErrorMsg.addErrorMessage(e);
						}
					}
					jtp.setSelectedIndex(0);
					if (highlight)
						tab.setBorder(oldB);
					tab.repaint();
				}
			};
			Thread t = new Thread(r);
			t.setName(title);
			t.start();
			return;
		}
	}

	public void setEditPanelInformation(Map<?, ?> valueEditComponents, Map<GraphElement, GraphElement> map) {
		if (getEditPanel() != null) {
			getEditPanel().setEditComponentMap(valueEditComponents);
			getEditPanel().setGraphElementMap(map);
		}
	}

	public void setEditPanelComponentMap(Map<?, ?> valueEditComponents) {
		if (getEditPanel() != null) {
			getEditPanel().setEditComponentMap(valueEditComponents);
		}
	}

	public void setEditPanelGraphElementMap(Map<GraphElement, GraphElement> map) {
		if (getEditPanel() != null) {
			getEditPanel().setGraphElementMap(map);
		}
	}

	public void setIcon(ImageIcon icon) {
		this.icon = icon;
	}

	public ImageIcon getIcon() {
		return icon;
	}

	/**
	 * returns a path string, that tells Vanted, where to put this tab. It is a
	 * dot-delimited string if it is not overridden, it'll return null and Vanted
	 * will put this tab on the root level If the path is not empty, Vanted puts
	 * this tab as child in the given tab hierarchy Example: return 'Network' and
	 * this tab is put as child in the Network Tab If the parent tab does not exist,
	 * it will be created
	 * 
	 * @return
	 */
	public String getTabParentPath() {
		return null;
	}

	/**
	 * Returns the preferred tab position in its parent tab. It can be
	 * InspectorTab.{LEADING,TRAILING,RANDOM,POSNUM} where POSNUM is the absolute
	 * position number This gives more control about the layout of subtabs
	 * 
	 * @return
	 */
	public int getPreferredTabPosition() {
		return preferredTabPosition;
	}

	public void setPreferredTabPosition(int preferredTabPosition) {
		this.preferredTabPosition = preferredTabPosition;
	}

	public boolean isSelectionListener() {
		return (this instanceof SelectionListener);
	}

	/**
	 * Override this method to trigger any action to be done, if this tab gains
	 * visibility. Then also call <code>super(e);</code> to enable DPI scaling. 
	 */
	@Override
	public void componentShown(ComponentEvent e) {
		Toolbox.scaleComponent(this, Toolbox.getDPIScalingRatio() * 0.5f, true);
	}

	@Override
	public void componentResized(ComponentEvent e) {
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}

}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
