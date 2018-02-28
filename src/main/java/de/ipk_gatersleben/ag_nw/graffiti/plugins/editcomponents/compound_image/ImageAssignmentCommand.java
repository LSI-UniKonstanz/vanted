/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 * Copyright (c) 2017 Computational Life Sciences, Uni Konstanz, Germany
 *******************************************************************************/
/*
 * Created on 18.04.2007 by Christian Klukas
 * Extended on 19.10.2017 by Dimitar Garkov
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.compound_image;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JTextField;

import org.AttributeHelper;
import org.Release;
import org.ReleaseInfo;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Node;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.parameter.ObjectListParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.session.EditorSession;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;

public class ImageAssignmentCommand extends AbstractAlgorithm {

	private String imageUrl = "";
	private String imagePos = POS_AUTO_OUTSIDE;
	private JTextField tf;
	private JButton bt;

	public void execute() {
		if (imageUrl == null || imageUrl.length() <= 0)
			return;
		try {
			graph.getListenerManager().transactionStarted(this);
			for (Node n : getSelectedOrAllNodes()) {
				AttributeHelper.setAttribute(n, "image", "image_url", imageUrl);
				AttributeHelper.setAttribute(n, "image", "image_position", imagePos);
			}
		} finally {
			graph.getListenerManager().transactionFinished(this, true);
			GraphHelper.issueCompleteRedrawForGraph(graph);
		}
	}

	@Override
	public String getDescription() {
		return "<html>" + "<em>" + this.getName() + "</em> assigns an image file to previously selected nodes.<br><br>"
				+

				"Image files are only linked and not included in the graph file. Thus, you should specify<br>"
				+ "an accessible ressource, as the file is (down) loaded from the specified URL.<br>"
				+ "To enhance portability, the image should be referenced from directory,<br>"
				+ "relative to your graph's (e.g. sub-dir)." +
				/*
				 * + " The image files will be cached on disk to speed up later<br>" + "loading
				 * and processing of the graph.
				 */ "<br><br>" +

				"Use the Node Attribute Editor to change the positioning of the image.<br>"
				+ "Default position is outside of the nodes. In case the image position<br>"
				+ "is set to `centered', the node size will be increased to fit the image.";
	}

	@Override
	public Parameter[] getParameters() {
		String text = "";
		for (Node nd : selection.getNodes())
			if (AttributeHelper.hasAttribute(nd, "image", "image_url")) {
				text = (String) AttributeHelper.getAttributeValue(nd, "image", "image_url", "", "", false);
				break;
			}
		tf = new JTextField(text);
		bt = new JButton("Search");
		bt.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File f = org.OpenFileDialogService.getFile(new String[] { "png", "gif", "jpg" }, "Image Files");
				if (f != null)
					tf.setText(getShorterImagePath(f.getAbsolutePath()));
			}
		});

		return new Parameter[] {
				new org.graffiti.plugin.parameter.JComponentParameter(
						info.clearthought.layout.TableLayout.getSplit(tf, bt, -1.0d, -2.0), "Image URL",
						"A (web)-URL to an image file"),
				// new StringParameter("", "Image URL",
				// "A URL to a image file"),
				new ObjectListParameter(imagePos, "Initial Image Position",
						"You may change the image position at a later point in time from the Node side panel",
						translatePositionConstants(CompoundImagePositionAttributeEditor.getPosiblePositions(false))) };
	}

	@Override
	public void setParameters(Parameter[] params) {
		int i = 1;
		imageUrl = tf.getText();
		imagePos = getConstantValue((String) ((ObjectListParameter) params[i++]).getValue());
	}

	public String getName() {
		if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR)
			return "Embed Image";
		else
			return null;
	}

	@Override
	public String getCategory() {
		return "Network.Nodes";
	}

	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(Arrays.asList(Category.NODE, Category.IMAGING, Category.VISUAL));
	}

	/**
	 * To enhance portability, we take the shortest possible file path. Meaning, if
	 * the image path could be relative, instead of absolute, it would be.
	 * 
	 * @param abs
	 *            the absolute path
	 * @return image file path - relative or absolute
	 */
	private String getShorterImagePath(String abs) {
		String graphPath = ((EditorSession) MainFrame.getInstance().getActiveSession()).getFileNameFull();
		int lastSepIndex = graphPath.lastIndexOf(File.separatorChar);
		if (lastSepIndex > 0)
			graphPath = graphPath.substring(0, lastSepIndex);
		if (abs.startsWith(graphPath))
			return abs.replaceFirst(graphPath, "");

		return abs;
	}

	/**
	 * Translates GraphicAttributeConstants values into human readable form. We then
	 * translate back to our short notation form by using
	 * {@link ImageAssignmentCommand#getConstantValue()}. This is necessary, because
	 * of backwards compatibility.
	 * 
	 * @param positions
	 * @return
	 */
	private String[] translatePositionConstants(String[] positions) {
		for (int i = 0; i < positions.length; i++) {
			switch (positions[i]) {
			case GraphicAttributeConstants.CENTERED:
				positions[i] = POS_CENTERED;
				break;
			case GraphicAttributeConstants.CENTERED_FIT:
				positions[i] = POS_CENTERED_FIT;
				break;
			case GraphicAttributeConstants.LEFT:
				positions[i] = POS_LEFT;
				break;
			case GraphicAttributeConstants.RIGHT:
				positions[i] = POS_RIGHT;
				break;
			case GraphicAttributeConstants.ABOVE:
				positions[i] = POS_ABOVE;
				break;
			case GraphicAttributeConstants.BELOW:
				positions[i] = POS_BELOW;
				break;
			case GraphicAttributeConstants.AUTO_OUTSIDE:
				positions[i] = POS_AUTO_OUTSIDE;
				break;
			case GraphicAttributeConstants.HIDDEN:
				positions[i] = POS_HIDDEN;
				break;
			default:
				break;
			}
		}

		return positions;
	}

	/**
	 * Translates back from human readable from into internal constant.
	 * 
	 * @param value
	 * @return
	 */
	private String getConstantValue(String value) {
		switch (value) {
		case POS_CENTERED:
			return GraphicAttributeConstants.CENTERED;
		case POS_CENTERED_FIT:
			return GraphicAttributeConstants.CENTERED_FIT;
		case POS_LEFT:
			return GraphicAttributeConstants.LEFT;
		case POS_RIGHT:
			return GraphicAttributeConstants.RIGHT;
		case POS_ABOVE:
			return GraphicAttributeConstants.ABOVE;
		case POS_BELOW:
			return GraphicAttributeConstants.BELOW;
		case POS_AUTO_OUTSIDE:
			return GraphicAttributeConstants.AUTO_OUTSIDE;
		case POS_HIDDEN:
			return GraphicAttributeConstants.HIDDEN;
		default:
			return value;
		}
	}

	/* Human-readable form constants */
	private static final String POS_CENTERED = "centered";
	private static final String POS_CENTERED_FIT = "centered fit";
	private static final String POS_LEFT = "west";
	private static final String POS_RIGHT = "east";
	private static final String POS_ABOVE = "north";
	private static final String POS_BELOW = "south";
	private static final String POS_AUTO_OUTSIDE = "auto outside";
	private static final String POS_HIDDEN = "hidden";

}
