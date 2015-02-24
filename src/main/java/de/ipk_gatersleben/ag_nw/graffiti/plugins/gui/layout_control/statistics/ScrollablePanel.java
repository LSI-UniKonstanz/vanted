/**
 * 
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.statistics;

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

	
	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return new Dimension(250, 400);
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
		return true;
	}
	@Override
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}


}
