/*************************************************************************************
 * The Exemplary Add-on is (c) 2008-2011 Plant Bioinformatics Group, IPK Gatersleben,
 * http://bioinformatics.ipk-gatersleben.de
 * The source code for this project, which is developed by our group, is
 * available under the GPL license v2.0 available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html. By using this
 * Add-on and VANTED you need to accept the terms and conditions of this
 * license, the below stated disclaimer of warranties and the licenses of
 * the used libraries. For further details see license.txt in the root
 * folder of this project.
 ************************************************************************************/
package org.vanted.addons.exampleaddon.attribute_component;

import javax.swing.JComboBox;
import javax.swing.JComponent;

import org.graffiti.attributes.Attribute;
import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.editcomponent.AbstractValueEditComponent;
import org.graffiti.plugins.views.defaults.GradientFillAttributeEditor;

/**
 * The editor will be shown in the Network->{Graph,Node,Edge} tab. It allows to
 * nicely visualise and set the attribute. See the {@link GradientFillAttributeEditor} for such a nice editor-component
 * 
 * @author Hendrik Rohn, C. Klukas
 */
public class StarAttributeEditor extends AbstractValueEditComponent {
	
	private final JComboBox checkbox;
	public static String[] validStarSettings = new String[] { "0", "1", "2", "3",
						"4", "5" };
	
	public StarAttributeEditor(Displayable disp) {
		super(disp);
		checkbox = new JComboBox(validStarSettings);
		checkbox.setOpaque(false);
		checkbox.setSelectedItem(disp.getValue());
	}
	
	public JComponent getComponent() {
		return checkbox;
	}
	
	public void setEditFieldValue() {
		// if more than one elements are selected and the attribute values are different, then show the "~"; otherwise the attribute value
		if (showEmpty)
			checkbox.setSelectedItem(EMPTY_STRING);
		else
			checkbox.setSelectedItem((getDisplayable().getValue()));
	}
	
	/**
	 * Will be triggered, if you change the value in the tab and press enter.
	 */
	public void setValue() {
		// we just want to set the attribute value, if there is no "~"
		if (getDisplayable() instanceof Attribute)
			if (!((String) checkbox.getSelectedItem()).equals(EMPTY_STRING))
				((Attribute) getDisplayable()).setValue(checkbox
								.getSelectedItem());
	}
	
}
