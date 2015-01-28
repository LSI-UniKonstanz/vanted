/**
 * 
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.fastshapeview;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.font.GraphicAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import org.graffiti.attributes.Attribute;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.graphics.CoordinateAttribute;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.graphics.NodeGraphicAttribute;
import org.graffiti.options.GravistoPreferences;
import org.graffiti.plugin.gui.ToolButton;
import org.graffiti.plugin.tool.AbstractTool;
import org.graffiti.plugin.tool.Tool;
import org.graffiti.plugin.view.ZoomListener;
import org.graffiti.plugin.view.Zoomable;
import org.graffiti.plugins.modes.defaults.MegaTools;
import org.graffiti.plugins.modes.defaults.SelectionRectangle;
import org.graffiti.selection.SelectionEvent;
import org.graffiti.selection.SelectionListener;
import org.graffiti.session.Session;
import org.graffiti.session.SessionListener;

import scenario.ScenarioService;

/**
 * @author matthiak
 *
 */
public class FastShapeMoveTool extends MouseInputAdapter
implements Tool, SessionListener, SelectionListener, MouseWheelListener, FastShapeView.DrawCallback {

	enum STATE {
		IDLE,
		SELECTION,
		MOUSEOVER,
		MOVEELEMENTS
	}
	
	STATE curState;
	
	FastShapeView activeView;

	GraphElementContainer lastSelectedContainer;
	
	MouseEvent lastPressedMouseEvent;
	MouseEvent currentDragMouseEvent;
	MouseEvent currentMoveMouseEvent;
	MouseEvent lastDraggedMouseEvent;
	
	/** Flag set by <code>activate</code> and <code>deactivate</code>. */
	protected boolean isActive;


	SelectionRectangle selectionRectangle;
	
	Rectangle2D currentDragFrame;
	Rectangle2D oldDragFrame;
	/**
	 * 
	 */
	public FastShapeMoveTool() {
		if (!AbstractTool.knownTools.contains(this))
			AbstractTool.knownTools.add(this);
	}
	
	public void activate() {
		
		ScenarioService.postWorkflowStep(
							"Activate " + getToolName(),
							new String[] { "import org.graffiti.plugin.tool.AbstractTool;" },
							new String[] { "AbstractTool.activateTool(\"" + getToolName() + "\");" });
		
		// System.out.println("Activate "+toString());
		
		JComponent viewComponent = MainFrame.getInstance().getActiveSession().getActiveView().getViewComponent();
		if( ! (viewComponent instanceof FastShapeView))
			return;
		activeView = (FastShapeView)viewComponent;
		deactivateAll();
		//
		// Zoomable myView = MainFrame.getInstance().getActiveSession().getActiveView();
		// ZoomListener zoomView = MainFrame.getInstance().getActiveSession().getActiveView();
		// AffineTransform at = new AffineTransform();
		// at.setToScale(1, 1);
		// zoomView.zoomChanged(at);
		
		try {
			this.isActive = true;
			// logger.entering(this.toString(), "  activate");
			activeView.addMouseListener(this);
			activeView.addMouseMotionListener(this);
			activeView.addMouseWheelListener(this);
			
			activeView.addDrawAfterListener(this);
			
			curState = STATE.IDLE;
			
			currentDragFrame = new Rectangle2D.Float();
			
			activeView.repaint();
		} catch (Exception e) {
			isActive = false;
		}
		ToolButton.requestToolButtonFocus();
		
		selectionRectangle = new SelectionRectangle();
		
	}
	
	public void deactivateAll() {
		for (Iterator<Tool> it = AbstractTool.knownTools.iterator(); it.hasNext();) {
			Tool t = (Tool) it.next();
			t.deactivate();
		}
		for (Iterator<Tool> it = AbstractTool.knownTools.iterator(); it.hasNext();) {
			Tool t = (Tool) it.next();
			t.deactivate();
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				ToolButton.checkStatusForAllToolButtons();
			}
		});
	}

	
	
	@Override
	public void mouseClicked(MouseEvent e) {
		super.mouseClicked(e);
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if(activeView instanceof Zoomable) {
			AffineTransform zoom = ((Zoomable)activeView).getZoom();
			if(e.getWheelRotation() < 0) {
				zoom.setToScale(zoom.getScaleX()-0.01, zoom.getScaleY()-0.01);
			} else {
				zoom.setToScale(zoom.getScaleX()+0.01, zoom.getScaleY()+0.01);
				
			}
			((ZoomListener)activeView).zoomChanged(zoom);
			activeView.repaint();
		}
	}

	
	
	@Override
	public void mousePressed(MouseEvent e) {
		lastPressedMouseEvent = e;
		lastDraggedMouseEvent = e;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		currentDragMouseEvent = e;
		if(lastSelectedContainer == null) {
			curState = STATE.SELECTION;
			calculateDragRectangle();
			activeView.repaint();
		} else {
			int diffx = currentDragMouseEvent.getX() - lastDraggedMouseEvent.getX();
			int diffy = currentDragMouseEvent.getY() - lastDraggedMouseEvent.getY();
			Attribute attribute = lastSelectedContainer.graphElement.getAttribute(GraphicAttributeConstants.GRAPHICS);
			if(attribute instanceof NodeGraphicAttribute) {
			CoordinateAttribute coord = ((NodeGraphicAttribute)attribute).getCoordinate();
			coord.setCoordinate(coord.getX() + diffx, coord.getY() + diffy);
			}
		}
		lastDraggedMouseEvent = e;
	}

	
	@Override
	public void mouseMoved(MouseEvent e) {
		super.mouseMoved(e);
		curState = STATE.MOUSEOVER;
		GraphElementContainer foundContainer = ((FastShapeView)activeView).findContainer(e.getX(), e.getY());
		if(lastSelectedContainer != null) {
			lastSelectedContainer.isSelected = false;
			lastSelectedContainer.repaint();
		}
		if(foundContainer != null){

			lastSelectedContainer = foundContainer;
			lastSelectedContainer.isSelected = true;
			lastSelectedContainer.repaint();
		}else
			lastSelectedContainer = null;
//		System.out.println("mosue moved: "+e.getX()+"'"+e.getY());
	}

	
	
	@Override
	public void mouseReleased(MouseEvent e) {
		super.mouseReleased(e);
		curState = STATE.IDLE;
		
		activeView.repaint();
	}

	@Override
	public void drawBefore(Graphics g) {
	}

	@Override
	public void drawAfter(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		
		if(curState == STATE.SELECTION) {
			Graphics localGraphics;

			localGraphics = g2.create(
					(int)currentDragFrame.getX(),
					(int)currentDragFrame.getY(),
					(int)currentDragFrame.getWidth(),
					(int)currentDragFrame.getHeight());
			
			System.out.println((int)currentDragFrame.getX()+","+
					(int)currentDragFrame.getY()+","+
					(int)currentDragFrame.getWidth()+","+
					(int)currentDragFrame.getHeight());
			selectionRectangle.setBounds(
					0, 
					0, 
					(int)currentDragFrame.getWidth(), 
					(int)currentDragFrame.getHeight());
			selectionRectangle.paint(localGraphics);
			
		}
		if(curState == STATE.MOUSEOVER) {
			if(lastSelectedContainer != null) {
				
			}
		}
	}

	void calculateDragRectangle() {
		int x = Math.min(lastPressedMouseEvent.getX(), currentDragMouseEvent.getX());
		int y = Math.min(lastPressedMouseEvent.getY(), currentDragMouseEvent.getY());
		int w = Math.abs(lastPressedMouseEvent.getX() - currentDragMouseEvent.getX());
		int h = Math.abs(lastPressedMouseEvent.getY() - currentDragMouseEvent.getY());
//		System.out.println("x:"+x+",y:"+y+",w:"+w+",h:"+h);
		currentDragFrame.setFrame(
				x, 
				y, 
				w, 
				h);
	}
	
	@Override
	public void selectionChanged(SelectionEvent e) {
	}

	@Override
	public void selectionListChanged(SelectionEvent e) {
	}

	@Override
	public void sessionChanged(Session s) {
	}

	@Override
	public void sessionDataChanged(Session s) {
	}

	@Override
	public boolean isActive() {
		return isActive;
	}

	@Override
	public boolean isSelectionListener() {
		return true;
	}

	@Override
	public boolean isSessionListener() {
		return true;
	}

	@Override
	public boolean isViewListener() {
		return false;
	}

	@Override
	public void deactivate() {
	}

	@Override
	public void setGraph(Graph g) {
	}

	@Override
	public void setPrefs(GravistoPreferences p) {
	}

	@Override
	public void preProcessImageCreation() {
	}

	@Override
	public void postProcessImageCreation() {
	}

	@Override
	public String getToolName() {
		return "Fast Shape Move Tool";
	}
	
	
}
