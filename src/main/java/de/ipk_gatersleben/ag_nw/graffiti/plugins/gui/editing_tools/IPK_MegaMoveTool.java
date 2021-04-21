/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools;

import java.awt.Component;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.AttributeHelper;
import org.ErrorMsg;
import org.Vector2d;
import org.graffiti.editor.GraffitiFrame;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.tool.AbstractTool;
import org.graffiti.plugin.tool.Tool;
import org.graffiti.plugin.view.View;
import org.graffiti.plugins.modes.defaults.MegaMoveTool;
import org.graffiti.plugins.modes.defaults.MegaTools;
import org.graffiti.plugins.views.defaults.GraffitiView;
import org.graffiti.selection.Selection;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.IPKGraffitiView;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.IPKnodeComponent;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.plugin_settings.PreferencesDialog;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.rotate.RotateAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.zoomfit.ZoomFitChangeComponent;

/**
 * A modified editing tool.
 * 
 * @author Christian Klukas
 * @vanted.revision 2.6.5
 */
public class IPK_MegaMoveTool extends MegaMoveTool implements MouseWheelListener {
	
	RotateAlgorithm ra = new RotateAlgorithm();
	
	public IPK_MegaMoveTool() {
		super();
		final Tool thisTool = this;
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				thisTool.deactivateAll();
				thisTool.activate();
				AbstractTool.lastActiveTool = thisTool;
			}
		});
	}
	
	/**
	 * override this method, which is already implemented in the superclass This
	 * must be done to clear the parameter list again, because this class would have
	 * the same parameters than the super class but they wouldn't do anything here
	 * and confuse the user
	 */
	@Override
	public List<Parameter> getDefaultParameters() {
		return null;
	}
	
	@Override
	protected void postProcessVisibilityChange(GraphElement sourceElementGUIinteraction) {
		super.postProcessVisibilityChange(sourceElementGUIinteraction);
		if (sourceElementGUIinteraction == null)
			return;
		if (PreferencesDialog.activeStartLayoutButton != null
				&& PreferencesDialog.activeStartLayoutButton.isEnabled()) {
			
			Vector2d oldPosition = null;
			if (sourceElementGUIinteraction instanceof Node)
				oldPosition = AttributeHelper.getPositionVec2d((Node) sourceElementGUIinteraction);
			
			MainFrame.getInstance().getActiveEditorSession().getSelectionModel()
					.setActiveSelection(new Selection("empty"));
			PreferencesDialog.activeStartLayoutButton.doClick(100);
			
			Vector2d newPosition = null;
			if (sourceElementGUIinteraction instanceof Node) {
				newPosition = AttributeHelper.getPositionVec2d((Node) sourceElementGUIinteraction);
				GraphHelper.moveGraph(sourceElementGUIinteraction.getGraph(), oldPosition.x - newPosition.x,
						oldPosition.y - newPosition.y);
			}
			
			Selection ss = new Selection("selection");
			ss.add(sourceElementGUIinteraction);
			MainFrame.getInstance().getActiveEditorSession().getSelectionModel().setActiveSelection(ss);
			MainFrame.getInstance().getActiveEditorSession().getSelectionModel().selectionChanged();
			MainFrame.showMessage("Layout has been updated, select the Null-Layout to disable automatic re-layout",
					MessageType.INFO);
		}
		
	}
	
	private long lastClick_ipk = Long.MIN_VALUE;
	
	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getWhen() <= lastClick_ipk)
			return;
		lastClick_ipk = e.getWhen();
		synchronized (GravistoService.getInstance().selectionSyncObject) {
			// cmm.ensureActiveSession(e);
			GravistoService.getInstance().pluginSetMoveAllowed(false);
			super.mousePressed(e);
		}
	}
	
	JComponent mouseWheelComponent = null;
	
	@Override
	public void activate() {
		if (session == null || session.getActiveView() == null || session.getActiveView().getViewComponent() == null
				|| (!(session.getActiveView() instanceof GraffitiView))) {
			return;
		}
		super.activate();
		// gif.addMouseWheelListener(this);
		if (MainFrame.getInstance().getActiveSession() != null
				&& (MainFrame.getInstance().getActiveSession().getActiveView() instanceof GraffitiView)) {
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
		super.deactivate();
		if (mouseWheelComponent != null)
			mouseWheelComponent.removeMouseWheelListener(this);
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		synchronized (GravistoService.getInstance().selectionSyncObject) {
			GravistoService.getInstance().pluginSetMoveAllowed(true);
			super.mouseReleased(e);
		}
	}
	
	public long lastMove = Integer.MIN_VALUE;
	
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (e.getWhen() <= lastMove)
			return;
		
		lastMove = e.getWhen();
		if (!MegaTools.MouseWheelZoomEnabled) {
			processMouseWheelScrolling(e);
		} else {
			try {
				if (e.getModifiersEx() != 64 && e.getModifiersEx() != 128) {
					if (e.getPreciseWheelRotation() < 0) {
						// ZoomFitChangeComponent.zoomIn();
						ZoomFitChangeComponent.zoomToPoint(
								MainFrame.getInstance().getActiveEditorSession().getActiveView(), e.getPoint(), 0.1);
					} else {
						// ZoomFitChangeComponent.zoomOut();
						ZoomFitChangeComponent.zoomToPoint(
								MainFrame.getInstance().getActiveEditorSession().getActiveView(), e.getPoint(), -0.1);
					}
					e.consume();
					return;
				}
				e.consume();
			} catch (NullPointerException npe) {
				// ignore
			}
		}
	}
	
	public static void processMouseWheelScrolling(MouseWheelEvent e) {
		Object o = e.getSource();
		if (o != null && o instanceof JComponent) {
			JComponent jc = (JComponent) o;
			JScrollPane jsp = (JScrollPane) ErrorMsg.findParentComponent(jc, JScrollPane.class);
			JScrollBar jsb = null;
			if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) == 1)
				jsb = jsp.getHorizontalScrollBar();
			if (e.getModifiersEx() == 0)
				jsb = jsp.getVerticalScrollBar();
			if (jsb != null) {
				int v = jsb.getValue();
				v += e.getUnitsToScroll() * 15;
				if (v < jsb.getMinimum())
					v = jsb.getMinimum();
				if (v > jsb.getMaximum())
					v = jsb.getMaximum();
				jsb.setValue(v);
			}
		}
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		super.mouseMoved(e);
		Component src = getFoundComponent();
		if (src != null) {
			if (src instanceof IPKnodeComponent) {
				IPKnodeComponent nci = (IPKnodeComponent) src;
				int mx = e.getX();
				int my = e.getY();
				nodeCursor = myMoveCursor;
				if (resizeHit_TL(mx, my, nci.getX(), nci.getY(), nci.getWidth(), nci.getHeight()))
					nodeCursor = myResize_TL_Cursor;
				if (resizeHit_TR(mx, my, nci.getX(), nci.getY(), nci.getWidth(), nci.getHeight()))
					nodeCursor = myResize_TR_Cursor;
				if (resizeHit_BR(mx, my, nci.getX(), nci.getY(), nci.getWidth(), nci.getHeight()))
					nodeCursor = myResize_BR_Cursor;
				if (resizeHit_BL(mx, my, nci.getX(), nci.getY(), nci.getWidth(), nci.getHeight()))
					nodeCursor = myResize_BL_Cursor;
			}
		}
		
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		super.mouseClicked(e);
		if (!MegaTools.wasScrollPaneMovement() && SwingUtilities.isRightMouseButton(e)) {
			View activeView = MainFrame.getInstance().getActiveEditorSession().getActiveView();
			
			if (activeView instanceof IPKGraffitiView) {
				JPopupMenu popupmenu = new DefaultContextMenuManager().getContextMenu(MegaTools.getLastMouseE());
				popupmenu.show(activeView.getViewComponent(), (int) (e.getX() * activeView.getZoom().getScaleX()),
						(int) (e.getY() * activeView.getZoom().getScaleY()));
			}
		}
	}
	
	@Override
	public String getToolName() {
		return "IPK_MegaMoveTool";
	}
}
