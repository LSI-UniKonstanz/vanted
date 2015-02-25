/**
 * 
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.statistics;

import info.clearthought.layout.TableLayout;

import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.Scrollable;

/**
 * A temporary compatibility solution for embedding inspector tab panel content into a 
 * scrollpane.
 * This panel makes sure that the content will always try to stretch to viewport width
 * @author matthiak
 *
 */
public class ScrollablePanel extends JPanel implements Scrollable {
	private static final long serialVersionUID = -1254006792188406703L;

	
	Dimension preferredScrollableViewportSize = new Dimension(300, 400);
	
	/**
	 * 
	 */
	public ScrollablePanel() {
		super();
	}
	
	/**
	 * @param tableLayout
	 */
	public ScrollablePanel(TableLayout tableLayout) {
		super(tableLayout);
	}
	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return preferredScrollableViewportSize;
	}
	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		return 50;
	}
	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		return 50;
	}
	@Override
	public boolean getScrollableTracksViewportWidth() {
		return getPreferredSize().getWidth() < getParent().getWidth();
	}
	@Override
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}


}
