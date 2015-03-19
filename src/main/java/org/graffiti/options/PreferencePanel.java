package org.graffiti.options;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;
import org.graffiti.attributes.Attribute;
import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.editcomponent.ValueEditComponent;
import org.graffiti.plugin.view.AttributeComponent;
import org.graffiti.util.InstanceCreationException;
import org.graffiti.util.InstanceLoader;

public class PreferencePanel  extends JDialog{

	private static final long serialVersionUID = -8249992449173197911L;


	Logger logger = Logger.getLogger(PreferencePanel.class);

	JScrollPane scrollpane;

	public PreferencePanel() {
		JPanel mainpanel = new JPanel();

		mainpanel.setLayout(new BoxLayout(mainpanel,  BoxLayout.Y_AXIS));


		Map<Class<? extends Displayable>, Class<? extends ValueEditComponent>> editComponents = MainFrame.getInstance().getEditComponentManager().getEditComponents();
		Set<Class<? extends Displayable>> keySet = editComponents.keySet();
		for(Object unknowndisplayable : keySet) {
			/*
			if(unknowndisplayable instanceof Displayable) {
				Displayable displayable = (Displayable)unknowndisplayable;
			} else {
			}
			*/
			ValueEditComponent unknownVEC = null;
			try {
				Object o = editComponents.get(unknowndisplayable);
				Class<?> c = (Class<?>)o;
				unknownVEC = (ValueEditComponent)InstanceLoader.createInstance(c,"org.graffiti.plugin.Displayable", null);
			} catch (InstanceCreationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(unknownVEC instanceof ValueEditComponent) {
//				ValueEditComponent valueEditComponent = editComponents.get(unknowndisplayable);
				logger.debug("displayable: " + unknowndisplayable + " , ValueEditComponent: "+unknownVEC);
	
				mainpanel.add(unknownVEC.getComponent());
			} else {
				logger.debug("no VEC: " + unknownVEC);
			}
		}

		scrollpane = new JScrollPane(mainpanel);


		getContentPane().add(scrollpane);

		setSize(600,800);

		setVisible(true);

		setLocationRelativeTo(MainFrame.getInstance());
	}

}
