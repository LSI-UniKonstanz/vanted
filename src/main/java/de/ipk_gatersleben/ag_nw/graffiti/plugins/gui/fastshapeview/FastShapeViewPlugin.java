/**
 * 
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.fastshapeview;

import org.graffiti.plugin.EditorPluginAdapter;
import org.graffiti.plugin.gui.GraffitiComponent;
import org.graffiti.plugin.gui.ToolButton;
import org.graffiti.plugin.tool.Tool;
import org.graffiti.plugins.modes.defaultEditMode.DefaultEditMode;

/**
 * @author matthiak
 *
 */
public class FastShapeViewPlugin extends EditorPluginAdapter{

	private FastShapeMoveTool fastShapeMoveTool;
	private ToolButton toolButton;

	/**
	 * 
	 */
	public FastShapeViewPlugin() {
	
		fastShapeMoveTool = new FastShapeMoveTool();
		
		views = new String[]{
				FastShapeView.class.getName()
		};
		
		tools = new Tool[]{
				
		};
		
		toolButton = new ToolButton(fastShapeMoveTool,
				DefaultEditMode.sid,
				iBundle.getImageIcon("tool.megaMove"));

		toolButton.setToolTipText("FastShapeMoveTool");
		
		guiComponents = new GraffitiComponent[]{
				toolButton
		};

	}

	@Override
	public String getDefaultView() {
		return FastShapeView.class.getName();
	}

	
}
