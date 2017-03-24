package org.graffiti.options;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.UIManager;

import org.apache.log4j.Logger;
import org.graffiti.editor.EditComponentNotFoundException;
import org.graffiti.editor.MainFrame;
import org.graffiti.managers.EditComponentManager;
import org.graffiti.managers.PreferenceManager;
import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.editcomponent.ValueEditComponent;
import org.graffiti.plugin.parameter.Parameter;
import org.vanted.scaling.ScalingSlider;

public class ParameterOptionPane extends AbstractOptionPane{

	/**
	 * 
	 */
	private static final long serialVersionUID = 132078280776703961L;

	private static Logger logger = Logger.getLogger(ParameterOptionPane.class);
	
	List<Parameter> parameters; 
	
	List<ValueEditComponent> listValueEditComponents;
	
	Class<? extends PreferencesInterface> clazz;
	
	public ParameterOptionPane(String name, List<Parameter> parameters, Class<? extends PreferencesInterface> clazz) {
		super(name);
		
		this.parameters = parameters;
		
		this.clazz = clazz;
	}
	
	
	@Override
	public String getCategory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getOptionName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JComponent getOptionDialogComponent() {
		// TODO Auto-generated method stub
		return super.getOptionDialogComponent();
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return super.getName();
	}

	@Override
	protected void initDefault() {
		logger.debug("initdefault: ");
		EditComponentManager editComponentManager = MainFrame.getInstance().getEditComponentManager();
		
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		listValueEditComponents = new ArrayList<ValueEditComponent>();
		
		for(Parameter curParameter : parameters) {
			try {
				ValueEditComponent valueEditComponent = editComponentManager.getValueEditComponent(curParameter);
				listValueEditComponents.add(valueEditComponent);
				if(curParameter.getDescription() != null)
					addComponent(new JLabel(curParameter.getDescription()));
				addComponent(curParameter.getName(), valueEditComponent.getComponent());
				
				//insert the scaling slider right after the Look And Feel parameter
				if (curParameter.getName().toLowerCase().contains("look and feel")) {
					addScalingSlider();
					logger.debug("added Scaling Slider AFTER " + curParameter.getName());
				}
				logger.debug("added "+curParameter.getName()+" as valueeditcomponent");
			} catch (EditComponentNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * We only need the GUI Component and not the actual preferences value.
	 * This is stored separately and internally to the provided Slider. So,
	 * to avoid the useless storing of the Slider itself, we add it directly
	 * herein, right after the Look And Feel.
	 */
	private void addScalingSlider() {
		if (!UIManager.getLookAndFeel().getClass().getCanonicalName().contains("GTK")) {
			String description = "<html>Hi-DPI Support<sup>BETA</sup></html>";
			String name = "Emulated DPI";
			//first we add the label
			addComponent(new JLabel(description));
			//afterwards - the actual component
			addComponent(name, new ScalingSlider(MainFrame.getInstance()));
		}
	}
	
	@Override
	protected void saveDefault() {
		// TODO Auto-generated method stub
		logger.debug("savedefault");
		
		Preferences preferenceForClass = PreferenceManager.getPreferenceForClass(clazz);
		
		for(ValueEditComponent curVEC : listValueEditComponents) {
			Collection<Displayable> collAttr = new ArrayList<>();
			collAttr.add(curVEC.getDisplayable());
			curVEC.setValue(collAttr);
			String value =  curVEC.getDisplayable().getValue().toString();
			logger.debug("saving '"+curVEC.getDisplayable().getName()+"' with value:"+value);
			preferenceForClass.put(curVEC.getDisplayable().getName(), value);
		}
		
		PreferenceManager.updatePreferencesForClass(clazz, preferenceForClass);
	}

	
}
