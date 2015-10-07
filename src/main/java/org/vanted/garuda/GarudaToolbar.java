package org.vanted.garuda;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;

import org.graffiti.plugin.gui.GraffitiComponent;
import org.graffiti.plugin.view.View;
import org.graffiti.plugin.view.ViewListener;
import org.graffiti.session.Session;
import org.graffiti.session.SessionListener;

public class GarudaToolbar extends JToolBar
implements ActionListener, GraffitiComponent, ViewListener, SessionListener
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -490490099356252793L;

	String preferedComponent;
	
	VantedGarudaExtension garudaExtension;

	private JButton buttonGaruda;
	
	public GarudaToolbar(String preferedComponent) {
		this.preferedComponent = preferedComponent;
		garudaExtension = new VantedGarudaExtension();

		ImageIcon icon = new ImageIcon(getClass().getResource("garudaDiscoverIcon.png"));
		buttonGaruda = new JButton(icon);
		buttonGaruda.addActionListener(this);
		add(buttonGaruda);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		garudaExtension.execute();
	}

	@Override
	public String getPreferredComponent() {
		// TODO Auto-generated method stub
		return preferedComponent;
	}

	@Override
	public void sessionChanged(Session s) {
		if(s == null)
			buttonGaruda.setVisible(false);
		else
			buttonGaruda.setVisible(true);
	}

	@Override
	public void sessionDataChanged(Session s) {
		if(s == null)
			buttonGaruda.setVisible(false);
		else
			buttonGaruda.setVisible(true);
	}

	@Override
	public void viewChanged(View newView) {
		if(newView == null)
			buttonGaruda.setVisible(false);
		else
			buttonGaruda.setVisible(true);
	}

	
	
}
