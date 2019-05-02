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

import java.util.ArrayList;
import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEditSupport;

import org.AttributeHelper;
import org.graffiti.attributes.Attribute;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.GraphElement;
import org.graffiti.plugin.algorithm.AbstractEditorAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.ObjectListParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.view.View;

/**
 * Adds nice stars to graph-elements, demonstrating how to use new attributes,
 * attribute editors and Undo-funcitonality.
 * 
 * @author Hendrik Rohn, C. Klukas
 */
public class AddStarsToGraphElements extends AbstractEditorAlgorithm {
	
	private String stars = StarAttributeEditor.validStarSettings[1];
	
	public boolean activeForView(View v) {
		return v != null;
	}
	
	/**
	 * Add star attribute to the node. Please recognize, that you must
	 * explicitly set the correct Attributeobject. Otherwise it will be ignored.
	 * <p>
	 * Also demonstrates how to make this command undoable. To do it right is really complicated, as you have to take care of what is remembered and what may
	 * change.
	 */
	public void execute() {
		
		final List<GraphElement> selectedElements = selection.getElements();
		final String starnbr = stars;
		
		AbstractUndoableEdit addstarsCmd = new AbstractUndoableEdit() {
			private static final long serialVersionUID = 1L;
			
			private ArrayList<Attribute> addedStarAttributes = new ArrayList<Attribute>();
			
			@Override
			public String getPresentationName() {
				return "Add Stars";
			}
			
			@Override
			public String getRedoPresentationName() {
				return "Redo Add Stars";
			}
			
			@Override
			public String getUndoPresentationName() {
				return "Undo Add Stars";
			}
			
			@Override
			public void redo() throws CannotRedoException {
				addedStarAttributes = new ArrayList<Attribute>();
				for (GraphElement ge : selectedElements) {
					Attribute attr = new StarAttribute(StarAttribute.name, starnbr);
					AttributeHelper.setAttribute(ge, StarAttribute.path,
										StarAttribute.name, attr);
					addedStarAttributes.add(attr);
				}
			}
			
			@Override
			public void undo() throws CannotUndoException {
				for (Attribute attr : addedStarAttributes)
					attr.getParent().remove(attr);
			}
		};
		
		addstarsCmd.redo();
		
		UndoableEditSupport undo = MainFrame.getInstance().getUndoSupport();
		undo.beginUpdate();
		undo.postEdit(addstarsCmd);
		undo.endUpdate();
		
	}
	
	@Override
	public Parameter[] getParameters() {
		return new Parameter[] { new ObjectListParameter(stars, "Stars",
							"Number of stars", StarAttributeEditor.validStarSettings) };
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int idx = 0;
		stars = (String) ((ObjectListParameter) params[idx++]).getValue();
	}
	
	public String getName() {
		return "Add Stars";
	}
	
	@Override
	public void check() throws PreconditionException {
		super.check();
		if (selection == null || selection.getElements().size() < 1)
			throw new PreconditionException(
								"Please select at least one node");
	}
	
	@Override
	public String getCategory() {
		return "menu.edit";
	}
	
	@Override
	public boolean showMenuIcon() {
		return true;
	}
	
	@Override
	public String getDescription() {
		return "<html>Adds a star to selected nodes Stars can be switched on and off<br>" +
				"using the Network->Node tab.";
	}
}
