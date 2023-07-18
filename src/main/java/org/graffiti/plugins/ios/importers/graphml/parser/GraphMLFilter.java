// ==============================================================================
//
// GraphMLFilter.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: GraphMLFilter.java,v 1.11 2011/01/16 16:39:59 klukas Exp $

package org.graffiti.plugins.ios.importers.graphml.parser;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.AttributeHelper;
import org.ErrorMsg;
import org.PositionGridGenerator;
import org.graffiti.attributes.Attributable;
import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.AttributeExistsException;
import org.graffiti.attributes.LinkedHashMapAttribute;
import org.graffiti.attributes.SortedCollectionAttribute;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.graphics.AWTImageAttribute;
import org.graffiti.graphics.CoordinateAttribute;
import org.graffiti.graphics.EdgeGraphicAttribute;
import org.graffiti.graphics.EdgeLabelAttribute;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.graphics.LabelAttribute;
import org.graffiti.graphics.LineModeAttribute;
import org.graffiti.graphics.NodeLabelAttribute;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * Class <code>GraphMLFilter</code> processes the SAX events generated by the
 * parser and builds the graph according to the events. In that sense, this
 * class is the heart of the graphML reading library.
 *
 * @author ruediger
 */
public class GraphMLFilter extends XMLFilterImpl {
	// ~ Static fields/initializers =============================================

	/** The logger for this class. */
	private static final Logger logger = Logger.getLogger(GraphMLFilter.class.getName());

	// ~ Instance fields ========================================================

	/**
	 * The <code>Attributable</code> that is currently being decorated with
	 * attributes.
	 */
	private Attributable currentAttributable;

	/**
	 * The cache for keeping attributes before they are attatched to the
	 * corresponding <code>Attributable</code>.
	 */
	private AttributeCache attrCache;

	/** <code>AttributeCreator</code> to set default attribute values. */
	private AttributeCreator defaultCreator;

	/** The decorator used for creating edge attributes. */
	private AttributeCreator edgeAttributeCreator;

	/** The decorator used for creating graph attributes. */
	private AttributeCreator graphAttributeCreator;

	/** The decorator used for creating node attributes. */
	private AttributeCreator nodeAttributeCreator;

	/**
	 * Helper for creating bends attributes for edges. TODO remove this when
	 * attributes work as expected (see below).
	 */
	private EdgeBendsCreator edgeBendsCreator;

	/** The graph to which to add the read in data. */
	private Graph graph;

	/** The mapping for mapping ids to nodes in the graph. */
	private NodeMapping nodeMap;

	/** Indicates that we are inside a default declaration. */
	private boolean defaultDecl;

	/** Determines whether an edge is directed or undirected by default. */
	private boolean edgeDefault = true;

	PositionGridGenerator positionGen = new PositionGridGenerator(150, 150, 1000);

	/**
	 * Provide compatibility mapping GraphML names to built-in Attribute paths.
	 */
	private static HashMap<String, String> CompatPathMapping = null;
	private static HashMap<String, String> CompatValueMapping = null;

	// ~ Constructors ===========================================================

	/**
	 * Constructs a new <code>GraphMLFilter</code>.
	 *
	 * @param parent the parent <code>XMLReader</code>.
	 * @param graph  the <code>Graph</code> to which to add the read in data.
	 */
	public GraphMLFilter(XMLReader parent, Graph graph) {
		super(parent);
		this.graph = graph;
		this.nodeMap = new NodeMapping();
		this.graphAttributeCreator = new GraphAttributeCreator();
		this.nodeAttributeCreator = new NodeAttributeCreator();
		this.edgeAttributeCreator = new EdgeAttributeCreator();
		this.attrCache = new AttributeCache();
		this.defaultDecl = false;

		if (CompatPathMapping == null) {
			CompatPathMapping = new HashMap<String, String>();
			// Position
			CompatPathMapping.put("x", GraphicAttributeConstants.COORDX_PATH);
			CompatPathMapping.put("y", GraphicAttributeConstants.COORDY_PATH);
			// Size
			CompatPathMapping.put("width", GraphicAttributeConstants.DIMW_PATH);
			CompatPathMapping.put("height", GraphicAttributeConstants.DIMH_PATH);
			// Colour
			CompatPathMapping.put("r",
					GraphicAttributeConstants.FILLCOLOR_PATH + Attribute.SEPARATOR + GraphicAttributeConstants.RED);
			CompatPathMapping.put("red",
					GraphicAttributeConstants.FILLCOLOR_PATH + Attribute.SEPARATOR + GraphicAttributeConstants.RED);
			CompatPathMapping.put("g",
					GraphicAttributeConstants.FILLCOLOR_PATH + Attribute.SEPARATOR + GraphicAttributeConstants.GREEN);
			CompatPathMapping.put("green",
					GraphicAttributeConstants.FILLCOLOR_PATH + Attribute.SEPARATOR + GraphicAttributeConstants.GREEN);
			CompatPathMapping.put("b",
					GraphicAttributeConstants.FILLCOLOR_PATH + Attribute.SEPARATOR + GraphicAttributeConstants.BLUE);
			CompatPathMapping.put("blue",
					GraphicAttributeConstants.FILLCOLOR_PATH + Attribute.SEPARATOR + GraphicAttributeConstants.BLUE);
			// Shape
			CompatPathMapping.put("shape", GraphicAttributeConstants.SHAPE_PATH);
		}

		if (CompatValueMapping == null) {
			CompatValueMapping = new HashMap<String, String>();
			// Rect shape
			CompatValueMapping.put("rect", GraphicAttributeConstants.RECTANGLE_CLASSNAME);
		}
	}

	// ~ Methods ================================================================

	/**
	 * Filter a character data event. This method assumes that the character data is
	 * not split into multiple events. Depending on whether the event belongs to a
	 * default attribute value declaration or a usual attribute value declaration
	 * the corrsponding attribute value will be set.
	 *
	 * @param ch     an array of characters.
	 * @param start  the starting position in the array.
	 * @param length the number of characters to use from the array.
	 */
	public void characters(char[] ch, int start, int length) {
		// determine the value
		String value = new String(ch, start, length);

		logger.fine(((length - start) < 50) ? new String(ch, start, length)
				: ((new String(ch, start, start + 40)) + "... (length: " + (length - start) + ") ..."
						+ value.substring(value.length() - 10, value.length())));

		String path = attrCache.getPath();
		assert path != null : "path is null.";

		try {

			// if there is a default declaration set the default value
			if (this.defaultDecl) {
				if (defaultCreator == null) {
					System.out.println("Ignored: " + value);
					return;
				}
				this.defaultCreator.addDefaultValue(value);

				return;
			}

			// the view will expect a LabelAttribute which requires some hacking
			// TODO see bug #111 for details.
			// this if branch should be removable once the bug is resolved.
			String[] labels = { ".label", ".capacity", ".weight" };

			for (int i = 0; i < labels.length; ++i) {
				if (path.startsWith(labels[i])) {
					try {
						String id = labels[i].substring(1);
						if (currentAttributable instanceof Node) {
							LabelAttribute lattr = new NodeLabelAttribute(id);
							currentAttributable.addAttribute(lattr, "");
						} else {
							if (currentAttributable instanceof Edge) {
								LabelAttribute lattr = new EdgeLabelAttribute(id);
								currentAttributable.addAttribute(lattr, "");
							} else {
								throw new RuntimeException("GraphMLFilter: Label can only be set on nodes or edges!");
							}
						}
					} catch (AttributeExistsException aee) {
					}
				}
			}

			// TODO attribute hack
			// handle special paths: this should not be necessary once the
			// attributes work as expected, in particular the methods
			// set[type](value)
			if (path.equals(".graphics.backgroundImage.image")) {
				byte[] val = Base64.decode(value);
				assert val.length > 0 : "byte encoding of image has length zero.";

				// TODO special treatment for .graphics.backgroundImage.image
				ByteArrayInputStream is = new ByteArrayInputStream(val);

				// BufferedInputStream bis = new BufferedInputStream(is);
				try {
					// BufferedImage buffImg = ImageIO.read(bis);
					BufferedImage buffImg = ImageIO.read(is);
					assert buffImg != null : "the created image is null.";

					AWTImageAttribute iattr = (AWTImageAttribute) currentAttributable.getAttribute(path);
					iattr.setImage(buffImg);

					logger.fine("background image has been read in.");
				} catch (IOException ioe) {
					logger.warning("could not read in image: " + ioe.getMessage());
				}
			}

			// TODO attribute hack
			// handling of attribute .graphics.linemode will have to be changed
			// once LineModeAttribute is implemented correctly, i.e. is a composite
			// attribute that contains a collection of float attributes.
			else if (path.equals(".graphics.linemode")) {
				LineModeAttribute lma = (LineModeAttribute) currentAttributable.getAttribute(path);

				logger.fine("value of attribute .graphics.linemode: " + value + ".");

				lma.setValue(value);
			}

			// TODO attribute hack
			// it should be possible to write the bends using the method
			// setDouble(".graphics.bends.bend<i>", value)
			else if (path.startsWith(".graphics.bends.bend")) {
				String[] ids = path.split("\\.");

				// CoordinateAttribute coords = null;
				String bend = ids[ids.length - 2];

				// make sure the preceeding attributes exist
				int no = Integer.parseInt(bend.replaceAll("bend", ""));

				if (ids[ids.length - 1].equals("x")) {
					edgeBendsCreator.addX(no, Double.parseDouble(value));
				} else {
					assert ids[ids.length - 1].equals("y");

					edgeBendsCreator.addY(no, Double.parseDouble(value));
				}
			} else {
				logger.fine("writing attribute with value " + value + "\n\tat path " + path + ".");
				String compath = CompatPathMapping.get(path.toLowerCase());
				String compatValue = CompatValueMapping.get(value.toLowerCase());
				switch (attrCache.getType()) {
				case AttributeCache.BOOLEAN:

					boolean booleanValue = Boolean.valueOf(value).booleanValue();
					logger.fine("writing boolean value " + booleanValue + " at path " + path + ".");
					currentAttributable.setBoolean(compath == null ? path : compath, booleanValue);

					break;

				case AttributeCache.INT:

					int intValue = Integer.parseInt(value);
					logger.fine("writing integer value " + intValue + " at path " + path + ".");
					currentAttributable.setInteger(compath == null ? path : compath, intValue);

					break;

				case AttributeCache.LONG:

					long longValue = Long.parseLong(value);
					logger.fine("writing long value " + longValue + " at path " + path + ".");
					currentAttributable.setLong(compath == null ? path : compath, longValue);

					break;

				case AttributeCache.FLOAT:

					float floatValue = Float.parseFloat(value);
					logger.fine("writing float value " + floatValue + " at path " + path + ".");
					currentAttributable.setFloat(compath == null ? path : compath, floatValue);

					break;

				case AttributeCache.DOUBLE:

					double doubleValue = Double.parseDouble(value);
					logger.fine("writing double value " + doubleValue + " at path " + path + ".");
					currentAttributable.setDouble(compath == null ? path : compath, doubleValue);
					break;

				case AttributeCache.STRING:
					logger.fine("writing string value " + value + " at path " + path + ".");
					currentAttributable.setString(compath == null ? path : compath,
							compatValue == null ? value : compatValue);

					break;

				default:
					logger.warning("internal error: type of attribute not set " + "correctly.");

					break;
				}
			}
		} catch (Exception e) {
			ErrorMsg.addErrorMessage("Could not add attribute to path: " + path + ", value: " + value
					+ ", graphelement: "
					+ (currentAttributable == null ? "null" : currentAttributable.getClass().getSimpleName()) + "<br>"
					+ "Eventually this attribute is pre-defined with a different attribute type. Try changing the attribute name and/or path.");
		}
	}

	/**
	 * Filter an end element event. Depending on the kind of element certain states
	 * of the filter will be reset.
	 *
	 * @param uri       the element's Namespace URI, or the empty string.
	 * @param localName the element's local name, or the empty string.
	 * @param qName     the element's qualified (prefixed) name, or the empty
	 *                  string.
	 */
	public void endElement(String uri, String localName, String qName) {
		logger.fine("processing end-element </" + qName + ">.");

		if (qName.equals("graphml")) {
			// nothing specific required to be done
		} else if (qName.equals("key")) {
			assert this.defaultCreator != null : "default attribute creator " + "is null";

			// reset the default attribute creator
			this.defaultCreator = null;
		} else if (qName.equals("default")) {
			this.defaultDecl = false;
		} else if (qName.equals("graph")) {
			this.currentAttributable = null;
		} else if (qName.equals("node")) {
			// current attributable is again the graph
			this.currentAttributable = this.graph;
		} else if (qName.equals("edge")) {
			// get the list of bends and add them
			SortedCollectionAttribute bends = edgeBendsCreator.getBends();
			EdgeGraphicAttribute ega = (EdgeGraphicAttribute) currentAttributable.getAttribute("graphics");
			ega.setBends(bends);

			// current attributable is again the graph
			this.currentAttributable = this.graph;
		} else if (qName.equals("data")) {
			// cache is no longer needed and can be reset
			attrCache.reset();
		} else {
			logger.warning("don't know how to handle element </" + qName + ">.");
		}
	}

	/**
	 * Filter a start element event. Depending on the kind of attribute either an
	 * element will be added to the graph or the filter is prepared to modify the
	 * graph appropriately depending on the next coming events.
	 *
	 * @param uri       the element's Namespace URI, or the empty string.
	 * @param localName the element's local name, or the empty string.
	 * @param qName     the element's qualified (prefixed) name, or the empty
	 *                  string.
	 * @param atts      the element's attributes.
	 * @exception SAXException the client may throw an exception during processing.
	 */
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		logger.fine("processing element <" + qName + ">.");

		// graphml declaration
		if (qName.equals("graphml")) {
			logger.fine("sending element <graphml> to the parent parser");
			super.startElement(uri, localName, qName, atts);
		}

		// key declaration
		else if (qName.equals("key")) {
			String forDecl = atts.getValue("for");
			assert forDecl != null : "no for-attribute at key declaration.";
			logger.fine("processing key declaration for " + forDecl + ".");

			// get id, attr.name and attr.type from the attribute declarations
			// if attr.name is not specified we use the id instead.
			String id = atts.getValue("id");
			String name = atts.getValue("attr.name");

			if (name == null) {
				assert id != null : "attribute id is null as well as " + "attr.name.";
				name = id;
			}

			// get the type declaration - if not present use string
			String type = atts.getValue("attr.type");

			if (type == null) {
				type = "string";
			}

			assert this.defaultCreator == null : "defaultCreator is not null.";

			if (forDecl.equals("graph")) {
				this.graphAttributeCreator.addKeyDeclaration(id, name, type);
				this.defaultCreator = this.graphAttributeCreator;
			} else if (forDecl.equals("node")) {
				this.nodeAttributeCreator.addKeyDeclaration(id, name, type);
				this.defaultCreator = this.nodeAttributeCreator;
			} else if (forDecl.equals("edge")) {
				this.edgeAttributeCreator.addKeyDeclaration(id, name, type);
				this.defaultCreator = this.edgeAttributeCreator;
			} else if (forDecl.equals("all")) {
				this.nodeAttributeCreator.addKeyDeclaration(id, name, type);
				this.nodeAttributeCreator.setDefault(name, type);
				this.edgeAttributeCreator.addKeyDeclaration(id, name, type);
				this.edgeAttributeCreator.setDefault(name, type);
				this.defaultCreator = this.nodeAttributeCreator;
			} else {
				logger.warning("key declaration with unknown for-attribute " + forDecl + " - ignored.");

				return;
			}

			this.defaultCreator.setDefault(name, type);
		}

		// default attribute values
		else if (qName.equals("default")) {
			assert this.defaultCreator != null : "default creator is null.";
			this.defaultDecl = true;
		}

		// graph declaration
		else if (qName.equals("graph")) {
			// set the default for the direction of edges
			if (atts.getIndex("edgedefault") >= 0)
				this.edgeDefault = atts.getValue("edgedefault").equals("directed");
			else
				this.edgeDefault = true;
			logger.fine("processing declaration for " + (this.edgeDefault ? "" : "un") + "directed graph.");

			this.graph.setDirected(edgeDefault);

			addDefaultAttributeValues(this.graph, graphAttributeCreator.getDefaults());

			// add the default attributes
			// graph.addAttribute(graphAttributeCreator.getDefaultAttributes(), "");
			// set the current attributable to the graph
			assert this.currentAttributable == null : "current attributable " + "is not null.";
			this.currentAttributable = this.graph;
		}

		// node declaration
		else if (qName.equals("node")) {
			String id = atts.getValue("id");
			assert id != null : "id of node is null.";
			assert !id.equals("") : "id of node is empty string.";
			logger.fine("processing node with id " + id + ".");

			// create a new node, add it to the node mapping and attatch some
			// default attributes
			// Node n = this.graph.addNode(nodeAttributeCreator.getDefaultAttributes());
			Node n = this.graph.addNode();
			AttributeHelper.setAttribute(n, "", "graphml_id", id);
			AttributeHelper.setNiceId("graphml_id", "Node Attributes:ID");
			this.nodeMap.addNode(id, n);

			n.addAttribute(nodeAttributeCreator.getDefaultAttributes(), "");
			AttributeHelper.setPosition(n, positionGen.getNextPosition());
			AttributeHelper.setLabel(n, "");
			addDefaultAttributeValues(n, nodeAttributeCreator.getDefaults());

			// set current attributable to add attributes
			assert this.currentAttributable == this.graph : "current " + "attributable is not the graph.";
			this.currentAttributable = n;
		}

		// edge declaration
		else if (qName.equals("edge")) {
			logger.fine("processing edge declaration for.");

			// determine the source of the edge from the declaration
			String sourceId = atts.getValue("source");
			assert sourceId != null : "id of edge source is null.";
			assert !sourceId.equals("") : "id of edge source is empty string.";

			Node source = this.nodeMap.getNode(sourceId);

			// determine the target of the edge from the declaration
			String targetId = atts.getValue("target");
			assert targetId != null : "id of edge target is null.";
			assert !targetId.equals("") : "id of edge target is empty string.";

			Node target = this.nodeMap.getNode(targetId);

			// determine whether the edge is directed
			String directed = atts.getValue("directed");
			boolean dir;

			if (directed == null) {
				dir = this.edgeDefault;
			} else {
				dir = Boolean.valueOf(directed).booleanValue();
			}

			// create a new edge and attatch some default attributes
			Edge e = this.graph.addEdge(source, target, dir); // ,
			// AttributeHelper.getDefaultGraphicsAttributeForEdge(Color.BLACK,
			// Color.BLACK,
			// dir));
			e.addAttribute(edgeAttributeCreator.getDefaultAttributes(), "");
			// addDefaultAttributeValues(e, edgeAttributeCreator.getDefaults());

			// set current attributable to add attributes
			assert this.currentAttributable == this.graph : "current " + "attributable is not the graph.";
			this.currentAttributable = e;

			// prepare the bends attribute which will be added after all the
			// edge attributes are processed
			this.edgeBendsCreator = new EdgeBendsCreator();
		}

		// data declarations
		else if (qName.equals("data")) {
			assert this.currentAttributable != null;

			String key = atts.getValue("key");
			logger.fine("processing data declaration for key \"" + key + "\".");

			// determine path and type of the attribute
			AttributeCreator acrtr = null;

			if (currentAttributable instanceof Graph) {
				acrtr = graphAttributeCreator;
			} else if (currentAttributable instanceof Node) {
				acrtr = nodeAttributeCreator;
			} else if (currentAttributable instanceof Edge) {
				acrtr = edgeAttributeCreator;
			} else {
				logger.warning("currentAttributable is neither graph nor " + "node nor edge");
			}

			assert acrtr != null : "attribute creator is null.";

			String path = acrtr.getName(key);
			String type = acrtr.getType(key);

			// if there is no path declared we use the (unique) key-id
			if ((path == null) || path.equals("")) {
				path = key;
			}

			assert (path != null) && !path.equals("") : "illegal value " + path + " for path.";
			assert (type != null) && !type.equals("") : "illegal value " + type + " for type.";
			assert attrCache.isReset() : "attribute cache is not reset.";

			if (path != null && type != null) {
				if (type.equals("boolean")) {
					attrCache.prepare(path, AttributeCache.BOOLEAN);
				} else if (type.equals("int")) {
					attrCache.prepare(path, AttributeCache.INT);
				} else if (type.equals("long")) {
					attrCache.prepare(path, AttributeCache.LONG);
				} else if (type.equals("float")) {
					attrCache.prepare(path, AttributeCache.FLOAT);
				} else if (type.equals("double")) {
					attrCache.prepare(path, AttributeCache.DOUBLE);
				} else if (type.equals("string")) {
					attrCache.prepare(path, AttributeCache.STRING);
				} else {
					assert false : "unknown type " + type + ".";
				}
			}
		} else {
			logger.warning("don't know how to handle element <" + qName + ">.");
		}
	}

	/**
	 * Sets the default attribute value for each path and value object in the
	 * specified <code>Map</code>.
	 *
	 * @param attbl the <code>Attributable</code> at which to set the attribute
	 *              value.
	 * @param map   the <code>Map</code> containing a mapping from attribute path to
	 *              value objects.
	 */
	private void addDefaultAttributeValues(Attributable attbl, Map map) {
		for (Iterator itr = map.keySet().iterator(); itr.hasNext();) {
			String path = (String) itr.next();
			Object attr = map.get(path);

			logger.fine("adding default attribute  at path " + path + " with value " + attr.toString() + ".");

			if (attr instanceof Boolean) {
				boolean value = ((Boolean) attr).booleanValue();
				attbl.setBoolean(path, value);
			} else if (attr instanceof Integer) {
				int value = ((Integer) attr).intValue();
				attbl.setInteger(path, value);
			} else if (attr instanceof Long) {
				long value = ((Long) attr).longValue();
				attbl.setLong(path, value);
			} else if (attr instanceof Float) {
				float value = ((Float) attr).floatValue();
				attbl.setFloat(path, value);
			} else if (attr instanceof Double) {
				double value = ((Double) attr).doubleValue();
				attbl.setDouble(path, value);
			} else if (attr instanceof String) {
				String value = (String) attr;
				attbl.setString(path, value);
			} else {
				logger.warning("could not set attribute value of type" + attr.getClass().getName());
			}

			// attbl.addAttribute(attr, addPath);
		}
	}

	// ~ Inner Classes ==========================================================

	/**
	 * Class <code>EdgeBendsCreator</code> manages the bends of edges. TODO this
	 * inner class should become superflous once the set[type]() methods for setting
	 * values of attributes work as expected (cf. above).
	 */
	private class EdgeBendsCreator {
		/** The list containing the bends. */
		private List<CoordinateAttribute> coordList;

		/**
		 * Constructs a new <code>EdgeBendsCreator</code>.
		 */
		EdgeBendsCreator() {
			this.coordList = new ArrayList<CoordinateAttribute>();
		}

		/**
		 * Returns a <code>SortedCollectionAttribute</code> containing all the
		 * <code>CoordinateAttribute</code>s that are not <code>null</code>.
		 *
		 * @return a <code>SortedCollectionAttribute</code> containing all the
		 *         <code>CoordinateAttribute</code>s that are not <code>null</code>.
		 */
		SortedCollectionAttribute getBends() {
			SortedCollectionAttribute bends = new LinkedHashMapAttribute("bends");

			for (Iterator<CoordinateAttribute> itr = coordList.iterator(); itr.hasNext();) {
				CoordinateAttribute c = (CoordinateAttribute) itr.next();

				if (c != null) {
					bends.add(c);
				}
			}

			return bends;
		}

		/**
		 * Sets the x-coordinate of the coordinate attribute at position pos to the
		 * specified value.
		 *
		 * @param pos   the position of the corresponding coordinate attribute.
		 * @param value the value to be set.
		 */
		void addX(int pos, double value) {
			assert pos >= 0 : "negative index of bend.";

			// check if the list is long enough and fill in nulls if not
			// this prevents us from getting IndexOutOfBoundsExceptions and
			// enables us to preserve the ordering of the bends
			if (coordList.size() <= pos) {
				for (int i = coordList.size(); i <= (pos + 1); ++i) {
					coordList.add(i, null);
				}
			}

			assert pos < coordList.size() : "position: " + pos + ", size: " + coordList.size() + ".";

			CoordinateAttribute ca = (CoordinateAttribute) coordList.get(pos);

			if (ca == null) {
				ca = new CoordinateAttribute("bend" + pos);
				ca.setX(value);
				coordList.set(pos, ca);
			} else {
				ca.setX(value);
			}
		}

		/**
		 * Sets the y-coordinate of the coordinate attribute at position pos to the
		 * specified value.
		 *
		 * @param pos   the position of the corresponding coordinate attribute.
		 * @param value the value to be set.
		 */
		void addY(int pos, double value) {
			assert pos >= 0 : "negative index of bend.";

			// check if the list is long enough and fill in nulls if not
			// this prevents us from getting IndexOutOfBoundsExceptions and
			// enables us to preserve the ordering of the bends
			if (coordList.size() <= pos) {
				for (int i = coordList.size(); i <= pos; ++i) {
					coordList.add(i, null);
				}
			}

			CoordinateAttribute ca = (CoordinateAttribute) coordList.get(pos);

			if (ca == null) {
				ca = new CoordinateAttribute("bend" + pos);
				ca.setY(value);
				coordList.set(pos, ca);
			} else {
				ca.setY(value);
			}
		}
	}

	/**
	 * This class provides a mapping from <code>Node</code>s to <code>String</code>
	 * identifiers.
	 */
	private class NodeMapping {
		/** The map that contains the mapping. */
		private Map<String, Node> nodeMap2;

		/**
		 * Constructs a new <code>NodeMapping</code>.
		 */
		NodeMapping() {
			this.nodeMap2 = new Hashtable<String, Node>();
		}

		/**
		 * Returns the <code>Node</code> for the specified identifier, <code>null</code>
		 * if there is none.
		 *
		 * @param id the identifier for which to return the <code>Node</code>.
		 * @return the <code>Node</code> for the specified identifier, <code>null</code>
		 *         if there is none.
		 */
		Node getNode(String id) {
			return this.nodeMap2.get(id);
		}

		/**
		 * Adds a <code>Node</code> with the specified identifier to the mapping.
		 *
		 * @param id   the identifier for the <code>Node</code>.
		 * @param node the <code>Node</code> to be added.
		 */
		void addNode(String id, Node node) {
			this.nodeMap2.put(id, node);
		}
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
