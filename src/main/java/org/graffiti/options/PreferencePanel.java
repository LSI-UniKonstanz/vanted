package org.graffiti.options;

import info.clearthought.layout.TableLayout;

import java.util.Map;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;
import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.editcomponent.ValueEditComponent;
import org.graffiti.util.InstanceCreationException;
import org.graffiti.util.InstanceLoader;

public class PreferencePanel extends JDialog {
	
	private static final long serialVersionUID = -8249992449173197911L;
	
	Logger logger = Logger.getLogger(PreferencePanel.class);
	
	JScrollPane scrollpane;
	
	public PreferencePanel() {
		JPanel mainpanel = new JPanel();
		
		mainpanel.setLayout(new BoxLayout(mainpanel, BoxLayout.Y_AXIS));
		
		Map<Class<? extends Displayable>, Class<? extends ValueEditComponent>> editComponents = MainFrame.getInstance()
				.getEditComponentManager().getEditComponents();
		Set<Class<? extends Displayable>> keySet = editComponents.keySet();
		for (Object unknowndisplayable : keySet) {
			
			Displayable displayable = null;
			try {
				displayable = (Displayable) InstanceLoader.createInstance((Class) unknowndisplayable);
			} catch (InstanceCreationException e) {
				
				e.printStackTrace();
			}
			
			ValueEditComponent unknownVEC = null;
			try {
				Object o = editComponents.get(unknowndisplayable);
				Class<?> c = (Class<?>) o;
				unknownVEC = (ValueEditComponent) InstanceLoader.createInstance(c, "org.graffiti.plugin.Displayable",
						null);
				unknownVEC.setDisplayable(displayable);
			} catch (InstanceCreationException e) {
				
				e.printStackTrace();
			}
			if (unknownVEC instanceof ValueEditComponent) {
				// ValueEditComponent valueEditComponent =
				// editComponents.get(unknowndisplayable);
				logger.debug("displayable: " + unknowndisplayable + " , ValueEditComponent: " + unknownVEC);
				
				try {
					JComponent component2 = unknownVEC.getComponent();
					
					JComponent get3Split = TableLayout.get3Split(new JLabel(displayable.getClass().getSimpleName()),
							null, component2, TableLayout.PREFERRED, 5, TableLayout.FILL);
					
					if (component2 != null)
						mainpanel.add(get3Split);
				} catch (Exception e) {
					
					e.printStackTrace();
				}
			} else {
				logger.debug("no VEC: " + unknownVEC);
			}
		}
		
		scrollpane = new JScrollPane(mainpanel);
		
		getContentPane().add(scrollpane);
		
		setSize(600, 800);
		
		setVisible(true);
		
		setLocationRelativeTo(MainFrame.getInstance());
	}
	
}
