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
package org.vanted.addons.exampleaddon;

import javax.swing.ImageIcon;

import org.AttributeHelper;
import org.ErrorMsg;
import org.graffiti.attributes.AttributeDescription;
import org.graffiti.attributes.StringAttribute;
import org.graffiti.editor.GravistoService;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.gui.GraffitiComponent;
import org.graffiti.plugin.inspector.InspectorTab;
import org.graffiti.plugin.io.OutputSerializer;
import org.vanted.addons.exampleaddon.attribute_component.AddStarsToGraphElements;
import org.vanted.addons.exampleaddon.attribute_component.StarAttribute;
import org.vanted.addons.exampleaddon.attribute_component.StarAttributeComponent;
import org.vanted.addons.exampleaddon.attribute_component.StarAttributeEditor;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.addons.AddonAdapter;

/**
 * @author Hendrik Rohn
 *         Use {@link AddonAdapter} to indicate, that you are writing an addon.
 *         If an exception occurs during instantiation, a proper error message
 *         will be thrown and a standard addon icon will be used.
 */
public class ExemplaryAddon extends AddonAdapter {
	
	/**
	 * This class will automatically start all implemented Algorithms, views and
	 * other extensions written in your Add-on. A code formatting template
	 * (save_action_format.xml) is available in the "make" project of the VANTED
	 * CVS.
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected void initializeAddon() {
		// registers a (number of) Tabs in the sidepanel, on which you may add
		// any Jcomponent you want to
		this.tabs = new InspectorTab[] { new AddonTab() };
		
		// registers a number of algorithms which can manipulate graphs (and
		// views, if they are editoralgorithms)
		this.algorithms = new Algorithm[] {
				// menu-entry to run algorithms like layouting, clustering
				// etc.
				new DoNothingAlgorithm(),
				// right-click menu-entry to run algorithms
				new RightClickContextMenuAlgorithm(),
				// Layouter
				new MyCircleLayout(),
				// example algorithm which shows how to open a specific
				// type of
				// view
				new OpenViewAlgorithm(),
				// another algorithm adding nice stars to graphelements and
				// implements undi functionality
				new AddStarsToGraphElements()
		
		};
		
		// registers a number of views, which can be created from code or via
		// File->New View (you can also decide not to register, but just to create
		// such views from code.
		this.views = new String[] { MyFirstView.class.getName() };
		
		// registers a new Button in the Toolbar
		this.guiComponents = new GraffitiComponent[] {
				new ToolbarButton("defaultToolbar", WindowOrder.HORIZONTAL),
				new ToolbarButton("defaultToolbar", WindowOrder.VERTICAL),
				new ToolbarButton("defaultToolbar", WindowOrder.QUADRATIC) };
		
		// registers a new Attribute
		this.attributes = new Class[1];
		this.attributes[0] = StarAttribute.class;
		
		// registers this attribute as a special string attribute
		StringAttribute.putAttributeType(StarAttribute.name, StarAttribute.class);
		
		// registers, that the attribute with a certain name is instance of a
		// class and how it will be displayed in the AttributeEditor
		// note that "Stars: Stars" will lead to clustering all attributes
		// starting with
		// "Stars:" together. The attributename "starsize" may also be given with
		// the complete
		// name, e.g. ".graphics.stars"
		this.attributeDescriptions = new AttributeDescription[] {
				new AttributeDescription(StarAttribute.name,
						StarAttribute.class, "Stars: Stars", true, false, null), };
		
		// as an alternative to using attributedescriptions, one may also specify
		// a nice attribute description with this command:
		// AttributeHelper.setNiceId("starsize", "Stars: Size");
		
		// registers an editor for the star-Attribute
		valueEditComponents.put(StarAttribute.class, StarAttributeEditor.class);
		
		// registers a component for the attribute, to visualise the star
		attributeComponents.put(StarAttribute.class, StarAttributeComponent.class);
		
		// registers deleteable attributes, just hover over the name and click
		AttributeHelper.setDeleteableAttribute("." + StarAttribute.path + ".", StarAttribute.path);
		
		// this attribute will not serialized (saved in the gml file)
		// AttributeManager.getInstance().addUnwrittenAttribute("." + StarAttribute.path + "." + StarAttribute.name);
		
		// registers a serializer to write a graph-file to any format
		// inputserializers should extend AbstractInputSerializer
		outputSerializers = new OutputSerializer[] {
				new TestWriter()
		};
	}
	
	/**
	 * Here you may specify your own logo, which will appear in menus and tabs.
	 */
	@Override
	public ImageIcon getIcon() {
		try {
			ImageIcon icon = new ImageIcon(GravistoService.getResource(this.getClass(), "starspace", "png"));
			if (icon != null)
				return icon;
			else
				return super.getIcon();
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			return super.getIcon();
		}
	}
	
}
