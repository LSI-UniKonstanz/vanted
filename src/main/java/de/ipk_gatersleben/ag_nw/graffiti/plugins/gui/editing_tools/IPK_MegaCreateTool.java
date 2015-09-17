/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.ErrorMsg;
import org.graffiti.editor.GraffitiFrame;
import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.view.View;
import org.graffiti.plugins.modes.defaults.MegaCreateTool;
import org.graffiti.plugins.modes.defaults.MegaTools;
import org.graffiti.plugins.views.defaults.GraffitiView;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.IPKGraffitiView;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.zoomfit.ZoomFitChangeComponent;

/**
 * A modified editing tool
 * 
 * @author Christian Klukas
 */
public class IPK_MegaCreateTool
		extends MegaCreateTool
		implements MouseWheelListener {
	
	/**
	 * Instance of DefaultContextMenuManager
	 */
	// DefaultContextMenuManager cmm = new DefaultContextMenuManager();
	
	JComponent mouseWheelComponent = null;
	
	@Override
	public void activate() {
		if (session == null || session.getActiveView() == null || session.getActiveView().getViewComponent() == null
				|| (!(session.getActiveView() instanceof GraffitiView))) {
			return;
		}
		super.activate();
		
		// try
		// {
		// JComponent view = session.getActiveView().getViewComponent();
		// super.activate();
		// } catch(Exception e) {
		// ErrorMsg.addErrorMessage(e);
		// }
		//
		if (MainFrame.getInstance().getActiveSession() != null &&
				(MainFrame.getInstance().getActiveSession().getActiveView() instanceof GraffitiView)) {
			GraffitiView gv = (GraffitiView) MainFrame.getInstance().getActiveSession().getActiveView();
			if (gv != null) {
				mouseWheelComponent = gv.getViewComponent();
				mouseWheelComponent.addMouseWheelListener(this);
				for (GraffitiFrame gf : gv.getDetachedFrames()) {
					// jf.addMouseWheelListener(this);
					gf.getComponent(0).addMouseWheelListener(this);
				}
			}
		}
	}
	
	@Override
	public void deactivate() {
		if (session == null || session.getActiveView() == null || session.getActiveView().getViewComponent() == null)
			return;
		super.deactivate();
		
		try {
			JComponent view = session.getActiveView().getViewComponent();
			if (view != null && view instanceof GraffitiView)
				super.deactivate();
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
		
		if (mouseWheelComponent != null)
			mouseWheelComponent.removeMouseWheelListener(this);
	}
	
	public long lastMove = Integer.MIN_VALUE;
	
	public void mouseWheelMoved(MouseWheelEvent e) {
		
		if (e.getWhen() <= lastMove)
			return;
		lastMove = e.getWhen();
		
		if (!MegaTools.MouseWheelZoomEnabled) {
			IPK_MegaMoveTool.processMouseWheelScrolling(e);
		} else {
			try {
				if (e.getModifiersEx() != 64 && e.getModifiersEx() != 128) {
					if (e.getWheelRotation() < 0)
						ZoomFitChangeComponent.zoomIn();
					else
						ZoomFitChangeComponent.zoomOut();
					e.consume();
					return;
				}
			} catch (Exception err) {
				// empty
			}
		}
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		if (!MegaTools.wasScrollPaneMovement() && SwingUtilities.isRightMouseButton(e) && !creatingEdge) {
			View activeView = MainFrame.getInstance().getActiveEditorSession().getActiveView();
			if (activeView instanceof IPKGraffitiView) {
				JPopupMenu popupmenu = new DefaultContextMenuManager().getContextMenu(
						MegaTools.getLastMouseE());
				popupmenu.show(activeView.getViewComponent(), (int) (e.getX() * activeView.getZoom().getScaleX()), (int) (e.getY() * activeView.getZoom()
						.getScaleY()));
			}
		}
		super.mouseReleased(e);
		
	}
	
	@Override
	public String getToolName() {
		return "IPK_MegaCreateTool";
	}
	
}
