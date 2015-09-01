package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.ppt_exporter;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComponent;

import org.AttributeHelper;
import org.ErrorMsg;
import org.LabelFrameSetting;
import org.StringManipulationTools;
import org.apache.poi.ddf.EscherOptRecord;
import org.apache.poi.ddf.EscherProperties;
import org.apache.poi.hslf.model.AutoShape;
import org.apache.poi.hslf.model.Fill;
import org.apache.poi.hslf.model.PPGraphics2D;
import org.apache.poi.hslf.model.Picture;
import org.apache.poi.hslf.model.Shape;
import org.apache.poi.hslf.model.ShapeGroup;
import org.apache.poi.hslf.model.Slide;
import org.apache.poi.hslf.model.TextBox;
import org.apache.poi.hslf.model.TextShape;
import org.apache.poi.hslf.usermodel.RichTextRun;
import org.apache.poi.hslf.usermodel.SlideShow;
import org.apache.poi.util.IOUtils;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Edge;
import org.graffiti.graph.GraphElement;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugins.attributecomponents.simplelabel.LabelComponent;
import org.graffiti.plugins.attributecomponents.simplelabel.ViewLabel;
import org.graffiti.plugins.views.defaults.AbstractGraphElementComponent;
import org.graffiti.plugins.views.defaults.EdgeComponent;
import org.graffiti.plugins.views.defaults.QuadCurveEdgeShape;
import org.graffiti.plugins.views.defaults.SmoothLineEdgeShape;
import org.graffiti.selection.Selection;
import org.graffiti.session.EditorSession;

import de.ipk_gatersleben.ag_nw.graffiti.FileHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.IPKGraffitiView;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.IPKnodeComponent;

/**
 * Provides a PPT writer.
 * 
 * @version $Revision$
 */
@SuppressWarnings("nls")
public class PPTAlgorithm extends AbstractAlgorithm {
	
	private static boolean exportCloneMarkers = false;
	private static boolean replaceCurvedEdges = true;
	
	public PPTAlgorithm() {
		super();
	}
	
	@Override
	public String getName() {
		return "Create PPT file";
	}
	
	@Override
	public String getCategory() {
		return "menu.file";
	}
	
	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(Arrays.asList(
				Category.GRAPH,
				Category.EXPORT,
				Category.IMAGING
				));
	}

	
	@Override
	public String getDescription() {
		return "<html>This function is <html><font color=\"red\">experimental</font>! Certain graphical features like<br>" +
				"gradients, charts and curved edges are not very well supported yet.";
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int idx = 0;
		exportCloneMarkers = ((BooleanParameter) params[idx++]).getBoolean().booleanValue();
		replaceCurvedEdges = ((BooleanParameter) params[idx++]).getBoolean().booleanValue();
	}
	
	@Override
	public Parameter[] getParameters() {
		BooleanParameter bpExportCloneMarkers = new BooleanParameter(
				exportCloneMarkers, "Export clone markers (only relevant for SBGN PD maps)",
				"<html>Gradients are not supported by the PowerPoint export.<br>" +
						"The clone marker of a glyph will be replaced by a<br>" +
						"background image in the glyph.");
		bpExportCloneMarkers.setLeftAligned(true);
		BooleanParameter bpReplaceCurvedEdges = new BooleanParameter(
				replaceCurvedEdges, "Replace curved edges by segmented edges",
				"<html>Curved edges are not supported by the PowerPoint export.<br>" +
						"Curved edges (<i>Smooth Line</i> and <i>Quadratic Spline</i>) can be<br>" +
						"replaced by segmented edges or are ignored during export.");
		bpReplaceCurvedEdges.setLeftAligned(true);
		return new Parameter[] { bpExportCloneMarkers, bpReplaceCurvedEdges };
	}
	
	@Override
	public void execute() {
		String filename = GravistoService.getInstance().getMainFrame().getActiveEditorSession().getGraph().getName();
		if (filename != null && filename.indexOf(".") > 0)
			filename = filename.substring(0, filename.lastIndexOf(".")) + ".ppt";
		try {
			execute(FileHelper.getFileName("ppt", "Image File", filename));
		} catch (Exception e) {
			ErrorMsg.addErrorMessage("Could not create PowerPoint file: " + e);
		}
	}
	
	public void execute(String filename) {
		
		if (filename == null)
			return;
		
		// int gradients = 0; // counter
		
		List<GraphElement> currentSelection = null;
		EditorSession session = MainFrame.getInstance().getActiveEditorSession();
		
		// before writing clear selection
		if (session.getGraph() != null) {
			currentSelection = session.getSelectionModel().getActiveSelection().getElements();
			session.getSelectionModel().setActiveSelection(new Selection("empty"));
			session.getSelectionModel().selectionChanged();
		}
		
		IPKGraffitiView theView = (IPKGraffitiView) session.getActiveView();
		JComponent viewerComponent = theView.getViewComponent();
		
		SlideShow ppt = new SlideShow();
		Dimension slideSize = ppt.getPageSize();
		Dimension viewSize = theView.getSize();
		
		double scale;
		
		if (slideSize.getWidth() / theView.getWidth() < slideSize.getHeight() / theView.getHeight()) {
			scale = slideSize.getWidth() / viewSize.getWidth();
		} else {
			scale = slideSize.getHeight() / viewSize.getHeight();
		}
		
		Slide slide = ppt.createSlide();
		int idx = -1;
		
		if (exportCloneMarkers) {
			// add background picture pict as workaround for bug 46288 https://issues.apache.org/bugzilla/show_bug.cgi?id=46288
			
			try {
				InputStream is = GravistoService.getResource(getClass(), "clonemarker.jpg").openStream();
				byte[] bytes = IOUtils.toByteArray(is);
				idx = ppt.addPicture(bytes, Picture.JPEG);
				// pict could be used as a workaround for gradients
				Picture pict = new Picture(idx);
				pict.setAnchor(new java.awt.Rectangle(0, 0, 0, 0));
				pict.setFillColor(Color.WHITE);
				slide.addShape(pict);
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
			}
			
		}
		
		// draw reverse to avoid wrong overlapping
		for (int i = viewerComponent.getComponentCount() - 1; i >= 0; i--) {
			
			Edge edge = null;
			String edgeBendStyle = null;
			// labels are not drawn correctly using the components print function. this is a little workaround
			if (!viewerComponent.getComponent(i).getClass().equals(LabelComponent.class)) {
				Component jc = viewerComponent.getComponent(i);
				ShapeGroup group = new ShapeGroup();
				
				// define position of the drawing in the slide
				Rectangle bounds = new java.awt.Rectangle((int) (jc.getBounds().getX() * scale), (int) (jc.getBounds().getY() * scale), (int) (jc.getBounds()
						.getWidth() * scale), (int) (jc.getBounds().getHeight() * scale));
				group.setAnchor(bounds);
				group.setCoordinates(bounds);
				slide.addShape(group);
				Graphics2D graphics = new PPGraphics2D(group);
				graphics.translate(bounds.getX(), bounds.getY());
				graphics.scale(scale, scale);
				
				double gradient = 0.0d;
				
				AbstractGraphElementComponent cp = null;
				
				// decide, if we are working with nodes or edges
				
				if (jc.getClass().equals(EdgeComponent.class)) {
					cp = (EdgeComponent) viewerComponent.getComponent(i);// .getAttributeComponents()
					
					if (cp.getShape().getClass().equals(QuadCurveEdgeShape.class) ||
							cp.getShape().getClass().equals(SmoothLineEdgeShape.class)) {
						if (replaceCurvedEdges) {
							edge = (Edge) cp.getGraphElement().getAttributes().getAttributable();
							edgeBendStyle = AttributeHelper.getEdgeBendStyle(edge);
							AttributeHelper.setEdgeBendStyle(edge, "poly");
						} else
							continue;
						
					}
					
				} else {
					// fix for attribute component, which are not node components
					if (viewerComponent.getComponent(i) instanceof IPKnodeComponent) {
						cp = (IPKnodeComponent) viewerComponent.getComponent(i);// .getAttributeComponents()
						
						// color shades are not drawn correctly using the components print function. this is a little workaround
						gradient = Double.parseDouble(
								AttributeHelper.getAttributeValue(cp.getGraphElement().getAttributes().getAttributable(),
										GraphicAttributeConstants.GRAPHICS, GraphicAttributeConstants.GRADIENT, null, null, false).toString());
						if (exportCloneMarkers && idx != -1 && gradient != 0) {
							Shape gradientShape = new AutoShape(org.apache.poi.sl.usermodel.ShapeTypes.Ellipse);
							gradientShape.setAnchor(
									new java.awt.Rectangle(
											(int) (cp.getX() * scale), (int) (cp.getY() * scale), (int) (cp.getWidth() * scale), (int) (cp.getHeight() * scale)));
							Fill fill = gradientShape.getFill();
							fill.setFillType(Fill.FILL_PICTURE);
							fill.setPictureData(idx);
							
							EscherOptRecord opt = (EscherOptRecord) Shape.getEscherChild(gradientShape.getSpContainer(), EscherOptRecord.RECORD_ID);
							Shape.setEscherProperty(opt, EscherProperties.LINESTYLE__LINEWIDTH, (int) (2 * Shape.EMU_PER_POINT * scale));
							
							slide.addShape(gradientShape);
							group.addShape(gradientShape);
						}
					}
				}
				
				if (cp == null)
					continue;
				
				// check for labels
				for (Object vCa : cp.getAttributeComponents()) {
					
					// JComponent jcp = (JComponent) vCa;
					
					if (vCa instanceof LabelComponent) {
						LabelComponent lC = new LabelComponent();
						lC = (LabelComponent) vCa;
						
						// print label via print()
						// lC.print(graphics);
						
						// print label manually
						if (lC.getComponent(0) instanceof ViewLabel) {
							ViewLabel lCv = (ViewLabel) lC.getComponent(0);
							if (lCv.getText().compareTo("") != 0) {
								int x = lC.getX();// cp.getX() + ( cp.getWidth() / 2 - lC.getWidth() / 2 ) - 3;
								int y = lC.getY();// cp.getY() + ( cp.getHeight() / 2 - lC.getHeight() / 2 ) - 3;
								
								int x_scaled = (int) (scale * x);
								int y_scaled = (int) (scale * y);
								
								// If a label is drawn with a visible border,
								// try to use it's print functionality.
								// make sure, that a getFrame function is provided
								// by class LabelComponent
								if (lC.getLabelFrameSetting() != LabelFrameSetting.NO_FRAME) {
									ShapeGroup groupLc = new ShapeGroup();
									// define position of the drawing in the slide
									Rectangle boundsLc = new java.awt.Rectangle(
											(int) (lC.getBounds().getX() * scale), (int) (lC.getBounds().getY() * scale), (int) (lC.getBounds().getWidth() * scale),
											(int) (lC.getBounds().getHeight() * scale));
									groupLc.setAnchor(boundsLc);
									slide.addShape(groupLc);
									group.addShape(groupLc);
									Graphics2D graphicsLc = new PPGraphics2D(groupLc);
									graphicsLc.translate(boundsLc.getX(), boundsLc.getY());
									graphicsLc.scale(scale, scale);
									
									// positioning of text is not working properly.
									// therefore text is drawn by this function and
									// should not be included by the component's print
									// function
									String tmpText = new String(lCv.getText());
									lCv.setText("");
									lCv.print(graphicsLc);
									lCv.setText(tmpText);
								}
								
								// TextBox coordinates
								String lText = new String(lCv.getText());
								lText = lText.replaceAll("<br>", "\n");
								Matcher m = Pattern.compile("&#(\\d*);").matcher(lText);
								if (m.find()) {
									lText = m.replaceAll(toUnicode(m.group(1)));
								}
								TextBox txt = new TextBox();
								txt.setText(StringManipulationTools.removeHTMLtags(lText));
								txt.setAnchor(
										new java.awt.Rectangle(
												x_scaled, y_scaled - 1, (int) Math.ceil(scale * (lC.getWidth())), (int) Math.ceil(scale * (lC.getHeight()))));
								txt.setWordWrap(TextShape.WrapNone);
								RichTextRun richTextRun = txt.getTextRun().getRichTextRuns()[0];
								richTextRun.setFontSize((int) (lCv.getFont().getSize() * scale));
								richTextRun.setFontName(lCv.getFont().getFontName());
								richTextRun.setAlignment(TextShape.AlignCenter);
								
								// TODO: redraw HTML-Code
								
								slide.addShape(txt);
								group.addShape(txt);
							}
						}
					}
				}
				jc.print(graphics);
			}
			if (edge != null) {
				AttributeHelper.setEdgeBendStyle(edge, edgeBendStyle);
				edge = null;
			}
		}
		
		if (session.getGraph() != null && currentSelection != null) {
			session.getSelectionModel().getActiveSelection().addAll(currentSelection);
			session.getSelectionModel().selectionChanged();
		}
		
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(filename);
			ppt.write(fos);
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private String toUnicode(String c) {
		
		if (c.compareTo("92") == 0) {
			
			return "\\\\";
		}
		return Character.toString((char) Integer.parseInt(c));
	}
	
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------