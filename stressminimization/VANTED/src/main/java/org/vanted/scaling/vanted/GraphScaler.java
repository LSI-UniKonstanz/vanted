package org.vanted.scaling.vanted;

import java.awt.Toolkit;
import java.util.HashSet;

import javax.swing.JFrame;
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
import org.vanted.scaling.scalers.BasicScaler;
import org.vanted.scaling.scalers.component.WindowScaler;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.zoomfit.ZoomFitChangeComponent;

/**
 * Responsible for the synchronization between scaling and the established graph
 * zooming, which acts as graph scaling.
 * 
 * @author dim8
 *
 */
public class GraphScaler implements SessionListener, ChangeListener {

	private static HashSet<Integer> scaledSessions = new HashSet<>();
	private static HashSet<Integer> scaledDetachedFrames = new HashSet<>();
	private static int oldValueZooming;
	private static GraphScaler instance;
	private BasicScaler scaler = new BasicScaler(Toolbox.getDPIScalingRatio());
	private static float oldValueDPI = DPIHelper
			.processEmulatedDPIValue(DPIHelper.managePreferences(DPIHelper.VALUE_DEFAULT, DPIHelper.PREFERENCES_GET));

	public GraphScaler() {
		instance = this;

		// Add scaling of current active graph elements through zooming.
		ScalingSlider.registerChangeListeners(new ChangeListener[] { this });

		// default value
		scaledDetachedFrames.add(0);
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
		int dfCode = 0;
		try {
			dfCode = MainFrame.getInstance().getActiveDetachedFrame().hashCode();
		} catch (NullPointerException e) {
		}

		if (s == null || scaledSessions.contains(s.hashCode()))
			if (scaledDetachedFrames.contains(dfCode))
				return; // already scaled (zoomed)

		if (dfCode != 0) {
			if (scaledDetachedFrames.contains(dfCode))
				return;

			scaledDetachedFrames.add(dfCode);
			scaledSessions.remove(s.hashCode());
		} else
			scaledSessions.add(s.hashCode());

		new Thread(new Runnable() {

			@Override
			public void run() {
				// postpone until completely loaded
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				try {
					handleZooming(0);
					scaleGraphFrame(s);
				} catch (NullPointerException e) {
					// have loaded a pretty big graph &
					// tried to zoom too early - go again.
					this.run();
				}
			}
		}).start();

	}

	@Override
	public void sessionDataChanged(Session s) {
	}

	private void processZooming() {
		int newValue = ScalingSlider.getSliderValue();
		if (newValue == oldValueZooming)
			return; // no scaling change!

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

		scaleGraphFrame(null);

		MainFrame.getInstance().setActiveSession(active, activeView);
		oldValueZooming = newValue;
	}

	private static void handleZooming(int newValue) {
		if ((newValue == ScalingSlider.min || newValue == ScalingSlider.median)
				&& oldValueZooming == ScalingSlider.median)
			return; // no scaling change!

		int diff = (newValue == ScalingSlider.min) ? ScalingSlider.median - oldValueZooming
				: oldValueZooming - newValue;
		if (diff == 0)
			return;

		if (diff > 0) { // lower DPI, bigger size
			while (diff > 0) {
				ZoomFitChangeComponent.zoomIn();
				diff--;
			}
		} else { // higher DPI, smaller size
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

	private void scaleGraphFrame(Session session) {
		float ratio = Toolbox.getDPIScalingRatio();
		if (ratio == 1f && WindowScaler.getPreviousRatio() == -1f)
			return;

		JInternalFrame[] frames = MainFrame.getInstance().getDesktop().getAllFrames();
		BasicScaler s2 = new BasicScaler(Toolbox.getDPIScalingRatio());
		if (session != null) { // we have new view only
			for (JInternalFrame f : frames)
				if (((GraffitiInternalFrame) f).getView().equals(session.getActiveView())) {
					f.setFrameIcon(scaler.modifyIcon(null, f.getFrameIcon()));
					WindowScaler.resizeWindow(f, true);
				}

			if (MainFrame.getInstance().getActiveDetachedFrame() != null)
				JFrame.setDefaultLookAndFeelDecorated(true);

		} else { // scale all, we have new DPI
			for (JInternalFrame f : frames) {
				// reset and scale icon
				f.setFrameIcon(s2.modifyIcon(null, scaler.modifyIcon(null, f.getFrameIcon())));
				WindowScaler.resizeWindow(f, true);
			}

			scaler = s2;
		}
	}

	/**
	 * To subsequent new ScalingSlider instance.
	 */
	public static void reAddChangeListener() {
		// re-register listener for next slider instance
		if (ScalingSlider.getRegisteredChangeListeners() == null
				|| !ScalingSlider.getRegisteredChangeListeners().contains(instance))
			ScalingSlider.registerChangeListeners(new ChangeListener[] { instance });
	}
}
