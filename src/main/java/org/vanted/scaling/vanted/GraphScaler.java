package org.vanted.scaling.vanted;

import java.awt.Toolkit;
import java.util.HashSet;

import javax.swing.JInternalFrame;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.graffiti.editor.GraffitiInternalFrame;
import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.view.View;
import org.graffiti.session.Session;
import org.graffiti.session.SessionListener;
import org.vanted.scaling.DPIHelper;
import org.vanted.scaling.ScalingSlider;
import org.vanted.scaling.Toolbox;
import org.vanted.scaling.scaler.BasicScaler;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.zoomfit.ZoomFitChangeComponent;

/**
 * Responsible for the synchronization between scaling and the established
 * graph zooming, which acts as graph scaling.
 * 
 * @author dim8
 *
 */
public class GraphScaler implements SessionListener, ChangeListener {

	private static HashSet<Integer> scaledSessions = new HashSet<>();
	private static int oldValueZooming;
	private static GraphScaler instance;
	private BasicScaler scaler = new BasicScaler(Toolbox.getDPIScalingRatio());
	private static float oldValueDPI = DPIHelper.processEmulatedDPIValue(
			DPIHelper.managePreferences(DPIHelper.VALUE_DEFAULT, DPIHelper.PREFERENCES_GET));
	
	public GraphScaler() {
		instance = this;
		
		//Add scaling of current active graph elements through zooming.
		ScalingSlider.registerChangeListeners(new ChangeListener[] {this});
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (!((JSlider) e.getSource()).getValueIsAdjusting()) {
			scaler = new BasicScaler(oldValueDPI / Toolkit.getDefaultToolkit().getScreenResolution());
			processZooming();
			oldValueDPI = DPIHelper.processEmulatedDPIValue(ScalingSlider.getSliderValue());
		}
	}
	
	@Override
	public void sessionChanged(final Session s) {
		if (s == null || scaledSessions.contains(s.hashCode()))
			return; //already scaled (zoomed)
		
		scaledSessions.add(s.hashCode());
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				//postpone until completely loaded (probabilistically)
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				try {
					handleZooming(0);
					scaleGraphFrameIcon(s);
				} catch (NullPointerException e) {
					//have loaded a pretty big graph & 
					//tried to zoom too early - go again.
					this.run();
				}
			}
		}).start();

	}

	@Override
	public void sessionDataChanged(Session s) {}
	
	private void processZooming() {
		int newValue = ScalingSlider.getSliderValue();
		if (newValue == oldValueZooming)
			return;  //no scaling change!
		
		Session active = MainFrame.getInstance().getActiveSession();
		if (active == null) {
			oldValueZooming = newValue;
			return;
		}
		
		View activeView = active.getActiveView();
		for (Session session : MainFrame.getSessions()) {
			for (final View view : session.getViews()) {
				MainFrame.getInstance().setActiveSession(session, view);
				handleZooming(newValue);
			}
			scaledSessions.add(session.hashCode());
		}
		
		scaleGraphFrameIcon(null);
		
		MainFrame.getInstance().setActiveSession(active, activeView);
		oldValueZooming = newValue;
	}

	private static void handleZooming(int newValue) {
		if ((newValue == 0 || newValue == 50) && oldValueZooming == 50)
			return;  //no scaling change!

		int diff = (newValue == 0) ? 50 - oldValueZooming : oldValueZooming - newValue;
		if (diff == 0)
			return;
		
		if (diff > 0) {  //lower DPI, bigger size
			while (diff > 0) {
				ZoomFitChangeComponent.zoomIn();
				diff--;
			}
		} else {  //higher DPI, smaller size
			diff = Math.abs(diff);
			while (diff > 0) {
				ZoomFitChangeComponent.zoomOut();
				diff--;
			}
		}
	}
	
	public static void registerSessionListener() {
		MainFrame.getInstance().addSessionListener(instance);		
	}
	
	public static void registerSessionListenerPostponed() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				try {
					registerSessionListener();
				} catch (NullPointerException e) {
					this.run();
				}
			}
		}).start();
	}
	
	public static int getOldValueZooming() {
		return oldValueZooming;
	}

	public static void setOldValueZooming(int oldValue) {
		GraphScaler.oldValueZooming = oldValue;
	}
	
	private void scaleGraphFrameIcon(Session session) {
		float ratio = Toolbox.getDPIScalingRatio();
		if (ratio == 1f)
			return;
		
		JInternalFrame[] frames = MainFrame.getInstance().getDesktop().getAllFrames();
		BasicScaler s2 = new BasicScaler(Toolbox.getDPIScalingRatio());
		if (session != null) { //we have new view only
			for (JInternalFrame f : frames)
				if (((GraffitiInternalFrame) f).getView().equals(session.getActiveView()))
					f.setFrameIcon(scaler.modifyIcon(null, f.getFrameIcon()));
			} else { //scale all, we have new DPI
				for (JInternalFrame f : frames)
					//reset and scale icon
					f.setFrameIcon(s2.modifyIcon(null, scaler.modifyIcon(null, f.getFrameIcon())));
				
				scaler = s2;
		}
	}
	
	/**
	 * To subsequently new ScalingSlider instance. 
	 */
	public static void readdChangeListener() {
		//re-register listener for next slider instance
		if (ScalingSlider.getRegisteredChangeListeners() == null ||
				!ScalingSlider.getRegisteredChangeListeners().contains(instance))
			ScalingSlider.registerChangeListeners(new ChangeListener[] {instance});	
	}
}
