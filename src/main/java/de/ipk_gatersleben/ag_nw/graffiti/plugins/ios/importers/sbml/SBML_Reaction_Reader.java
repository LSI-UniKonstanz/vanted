/**
 * This class reads in Reactions
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.sbml;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.stream.XMLStreamException;

import org.AlignmentSetting;
import org.AttributeHelper;
import org.PositionGridGenerator;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SimpleSpeciesReference;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.ext.layout.BoundingBox;
import org.sbml.jsbml.ext.layout.Dimensions;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.LayoutModelPlugin;
import org.sbml.jsbml.ext.layout.Point;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceRole;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.KineticLawHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLLocalParameter;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLReactionHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLSpeciesHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBML_Constants;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBML_Logger;

public class SBML_Reaction_Reader {

	/**
	 * Method reads in function definitions and is called from class
	 * SBML_XML_Reader.java. Add reaction nodes and edges to the graph
	 * 
	 * @param g
	 *            the data structure for reading in the information
	 * @param reactionList
	 *            contains the reactions for the import
	 * @param modelID
	 *            is the id of the current model
	 * @param pgg
	 *            is needed for drawing the graph
	 */
	public void addReactions(Graph g, ListOf<Reaction> reactionList, String modelID, PositionGridGenerator pgg,
			SBMLReactionHelper reactionhelper) {
		SBMLReactionHelper reactionHelper = reactionhelper;
		String reactionID;
		String reactionName;
		Boolean reversible;
		String compartment;
		String sboTerm;
		String metaID;
		Boolean fast;
		Node reactionNode;
		ListOf<SpeciesReference> reactants;
		ListOf<SpeciesReference> products;
		ListOf<ModifierSpeciesReference> modifiers;
		String metaID2;
		String sboTerm2;
		String kineticFormula;
		KineticLaw kineticLaw;
		KineticLawHelper kineticLawHelper;
		List<LocalParameter> listLocalParameter;
		Iterator<LocalParameter> itLP;
		int countLocalParameter;
		LocalParameter localParameter;
		String internAttributeName;
		String presentedAttributeName;
		SBMLLocalParameter localParameterHelper;
		String id = SBML_Constants.EMPTY;
		String name;
		Double value;
		String unit;
		for (Reaction reaction : reactionList) {
			reactionID = reaction.getId();
			reactionName = reaction.getName();
			// reversible = false;
			// if (reaction.isSetReversible()) {
			reversible = reaction.getReversible();
			// }
			fast = reaction.getFast();
			compartment = reaction.getCompartment();
			sboTerm = reaction.getSBOTermID();
			metaID = reaction.getMetaId();

			// Determines the label of the reaction node
			/*
			 * String ex = null; if(!(reactionName == "")){ ex = reactionName; } else{ ex =
			 * reactionID; }
			 */
			reactionNode = g.addNode();
			AttributeHelper.setAttribute(reactionNode, SBML_Constants.SBML, SBML_Constants.SBML_ROLE,
					SBML_Constants.ROLE_REACTION);

			// CAUTION working with level 3 or higher CAUTION
			reactionHelper.setLabel(reactionNode, reactionName, reactionID, pgg);
			// setAttributes(reactionNode, Color.white, ex,
			// pgg.getNextPosition(), 8);
			AttributeHelper.setBorderWidth(reactionNode, 1d);
			// NodeTools.setClusterID(reactionNode, compartment);
			if (reaction.isSetCompartment()) {
				reactionHelper.setCompartment(reactionNode, compartment);
			}
			if (reaction.isSetId() && Reaction.isValidId(reactionID, reaction.getLevel(), reaction.getVersion())) {
				reactionHelper.setID(reactionNode, reactionID);
			}
			// if(reaction.isSetReversible()){
			reactionHelper.setReversible(reactionNode, reversible);
			// }
			if (reaction.isSetFast()) {
				reactionHelper.setFast(reactionNode, fast);
			}
			if (reaction.isSetSBOTerm()) {
				reactionHelper.setSBOTerm(reactionNode, sboTerm);
			}
			if (reaction.isSetMetaId()) {
				reactionHelper.setMetaID(reactionNode, metaID);
			}
			if (reaction.isSetNotes()) {
				try {
					reactionHelper.setNotes(reactionNode, reaction.getNotesString(), reaction.getNotes());
				} catch (XMLStreamException e) {
					
					e.printStackTrace();
				}
			}

			if (reaction.isSetAnnotation()) {
				if (reaction.getAnnotation().isSetRDFannotation()) {
					reactionHelper.setAnnotation(reactionNode, reaction.getAnnotation());
				}
				if (reaction.getAnnotation().isSetNonRDFannotation()) {
					reactionHelper.setNonRDFAnnotation(reactionNode, reaction.getAnnotation().getNonRDFannotation());
				}
			}

			// Adds the edges between reactant node and reaction node
			reactants = reaction.getListOfReactants();
			addReactants(g, reactants, reactionID, modelID, reactionNode, reaction.getReversible(), reactionHelper);

			// Adds the edges between product node and reaction node
			products = reaction.getListOfProducts();
			addProducts(g, products, reactionID, modelID, reactionNode, reaction.getReversible(), reactionHelper);

			// Adds the edges between modifier node and reaction node
			modifiers = reaction.getListOfModifiers();
			addModifier(g, modifiers, reactionNode, reactionHelper, reactionID);

			if (reaction.isSetKineticLaw()) {
				kineticLaw = reaction.getKineticLaw();
				kineticLawHelper = new KineticLawHelper(g, reactionHelper.getReactionClones());
				metaID2 = kineticLaw.getMetaId();
				sboTerm2 = kineticLaw.getSBOTermID();
				kineticFormula = "";
				try {
					if (kineticLaw.isSetMath()) {
						if (null != kineticLaw.getMath()) {
							kineticFormula = kineticLaw.getMath().toFormula();
						}
					}
				} catch (SBMLException e) {
					e.printStackTrace();
				}

				if (kineticLaw.isSetMath()) {
					kineticLawHelper.setFunction(reactionNode, kineticFormula);
				}
				if (kineticLaw.isSetSBOTerm()) {
					kineticLawHelper.setSBOTerm(reactionNode, sboTerm2);
				}
				if (kineticLaw.isSetMetaId()) {
					kineticLawHelper.setMetaId(reactionNode, metaID2);
				}
				if (kineticLaw.isSetNotes()) {
					try {
						kineticLawHelper.setNotes(reactionNode, kineticLaw.getNotesString(), kineticLaw.getNotes());
					} catch (XMLStreamException e) {
						
						e.printStackTrace();
					}
				}
				if (kineticLaw.isSetAnnotation()) {
					if (kineticLaw.getAnnotation().isSetRDFannotation()) {
						kineticLawHelper.setAnnotation(reactionNode, kineticLaw.getAnnotation());
					}
					if (kineticLaw.getAnnotation().isSetNonRDFannotation()) {
						kineticLawHelper.setNonRDFAnnotation(reactionNode,
								kineticLaw.getAnnotation().getNonRDFannotation());
					}
				}

				// Two ways to read in a Local Parameter. One way is deprecated.
				if (kineticLaw.isSetListOfLocalParameters() || kineticLaw.isSetListOfParameters()) {
					listLocalParameter = null;
					if (reaction.getModel().getLevel() == 3 && reaction.getModel().getVersion() == 1) {
						if (kineticLaw.isSetListOfLocalParameters()) {
							listLocalParameter = kineticLaw.getListOfLocalParameters();
						}
					} else {
						if (kineticLaw.isSetListOfParameters()) {
							listLocalParameter = kineticLaw.getListOfParameters();
						}
					}
					/*
					 * if(kineticLaw.isSetListOfParameters()){ listLocalParameter =
					 * kineticLaw.getListOfParameters(); } else
					 * if(kineticLaw.isSetListOfLocalParameters()){ listLocalParameter =
					 * kineticLaw.getListOfLocalParameters(); }
					 */
					if (listLocalParameter != null) {
						itLP = listLocalParameter.iterator();
						countLocalParameter = 1;
						while (itLP.hasNext()) {
							localParameter = itLP.next();
							internAttributeName = new StringBuffer(SBML_Constants.LOCAL_PARAMETER)
									.append(countLocalParameter).toString();
							presentedAttributeName = new StringBuffer(SBML_Constants.LOCALPARAMETER_HEADLINE)
									.append(countLocalParameter).toString();

							localParameterHelper = kineticLawHelper.addLocalParemeter(g, presentedAttributeName,
									internAttributeName);

							id = localParameter.getId();
							name = localParameter.getName();
							value = localParameter.getValue();
							if (Double.isNaN(value)) {
								SBML_Logger.addErrorMessage("Attribute value of reaction " + reactionID + " "
										+ presentedAttributeName + " is not a valid double value.");
							}
							unit = localParameter.getUnits();

							if (localParameter.isSetId()) {
								localParameterHelper.setID(reactionNode, id);
							}
							if (localParameter.isSetName()) {
								localParameterHelper.setName(reactionNode, name);
							}
							if (localParameter.isSetValue()) {
								localParameterHelper.setValue(reactionNode, value);
							}
							if (localParameter.isSetUnits()) {
								localParameterHelper.setUnits(reactionNode, unit);
							}
							if (localParameter.isSetMetaId()) {
								localParameterHelper.setMetaID(reactionNode, localParameter.getMetaId());
							}
							if (localParameter.isSetSBOTerm()) {
								localParameterHelper.setSBOTerm(reactionNode, localParameter.getSBOTermID());
							}
							if (localParameter.isSetNotes()) {
								try {
									localParameterHelper.setNotes(reactionNode, localParameter.getNotesString(),
											localParameter.getNotes());
								} catch (XMLStreamException e) {
									
									e.printStackTrace();
								}
							}
							if (localParameter.isSetAnnotation()) {
								if (localParameter.getAnnotation().isSetRDFannotation()) {
									localParameterHelper.setAnnotation(reactionNode, localParameter.getAnnotation());
								}
								if (localParameter.getAnnotation().isSetNonRDFannotation()) {
									localParameterHelper.setNonRDFAnnotation(reactionNode,
											localParameter.getAnnotation().getNonRDFannotation());
								}
							}

							countLocalParameter++;
						}
					}

				}
			}

			AttributeHelper.setLabel(AttributeHelper.getLabels(reactionNode).size(), reactionNode, reactionID, null,
					AlignmentSetting.HIDDEN.toGMLstring());

			if (SBML_Constants.isLayoutActive) {
				processLayoutInformation(reaction, reactionNode);
			}

		}
	}

	// old code, has been replaced by new code below on 09/09/2015
	//
	// private void processLayoutInformation(Graph g, Reaction reaction,
	// SBMLReactionHelper reactionHelper, Node reactionNode) {
	// LayoutModelPlugin layoutModel = (LayoutModelPlugin)
	// reaction.getModel().getExtension(SBMLHelper.SBML_LAYOUT_EXTENSION_NAMESPACE);
	// if (layoutModel != null) {
	//
	// Layout layout = layoutModel.getListOfLayouts().iterator().next();
	// ListOf<ReactionGlyph> currentReactionGlyphs = new ListOf<ReactionGlyph>();
	// currentReactionGlyphs.setLevel(layout.getLevel());
	// currentReactionGlyphs.setVersion(layout.getVersion());
	// Iterator<ReactionGlyph> reactionGlyphListIt =
	// layout.getListOfReactionGlyphs().iterator();
	// String reactionID = reaction.getId();
	// while (reactionGlyphListIt.hasNext()) {
	// ReactionGlyph nextReactionGlyph = reactionGlyphListIt.next();
	// if (nextReactionGlyph.getReaction().equals(reactionID)) {
	// currentReactionGlyphs.add(nextReactionGlyph);
	// }
	// }
	// // for (int i = 0; i < currentReactionGlyphs.size(); i++) {
	// for (int i = 0; i < 1; i++) {
	// ReactionGlyph reactionGlyph = currentReactionGlyphs.get(i);
	// if (i == 0) {
	// reactionHelper.addReactionCloneToList(reactionID, reactionNode);
	// }
	// if (i >= 1) {
	// Node reactionNodeClone = g.addNodeCopy(reactionNode);
	// reactionHelper.addReactionCloneToList(reactionID, reactionNodeClone);
	// reactionNode = reactionNodeClone;
	// }
	//
	// AttributeHelper.setSize(reactionNode, 40, 40);
	// BoundingBox boundingBox = null;
	// if (reactionGlyph != null)
	// boundingBox = reactionGlyph.getBoundingBox();
	// if (boundingBox != null) {
	// Dimensions dimensions = boundingBox.getDimensions();
	// if (dimensions != null) {
	// double width = dimensions.getWidth();
	// double height = dimensions.getHeight();
	// AttributeHelper.setSize(reactionNode, width, height);
	//
	// if (layout.getId() != null) {
	// AttributeHelper.setAttribute(reactionNode, SBML_Constants.SBML,
	// SBML_Constants.SBML_LAYOUT_ID, layout.getId());
	// }
	// else {
	// AttributeHelper.setAttribute(reactionNode, SBML_Constants.SBML,
	// SBML_Constants.SBML_LAYOUT_ID, SBML_Constants.EMPTY);
	// }
	//
	// } else
	// {
	// AttributeHelper.setSize(reactionNode, 34, 34);
	// // System.out.println("reaction id '" + reactionID + "' has no width/height
	// information.");
	// }
	// Point position = boundingBox.getPosition();
	// if (position != null) {
	// double x = position.getX();
	// double y = position.getY();
	// AttributeHelper.setPosition(reactionNode, x, y);
	// // TODO layout id might be set twice
	// AttributeHelper.setAttribute(reactionNode, SBML_Constants.SBML,
	// SBML_Constants.SBML_LAYOUT_ID, layout.getId());
	// } else {
	// System.out.println("species id '" + reactionID + "' has no x/y
	// information.");
	// }
	// }
	// }
	// }
	// }

	private static void processLayoutInformation(Reaction reaction, Node reactionNode) {

		LayoutModelPlugin layoutModel = (LayoutModelPlugin) reaction.getModel()
				.getExtension(SBMLHelper.SBML_LAYOUT_EXTENSION_NAMESPACE);
		if (layoutModel != null) {
			Layout layout = layoutModel.getListOfLayouts().iterator().next();
			Iterator<ReactionGlyph> reactionGlyphListIt = layout.getListOfReactionGlyphs().iterator();
			String reactionID = reaction.getId();

			ArrayList<ReactionGlyph> reactionGlyphsToBeAdded = new ArrayList<>();

			while (reactionGlyphListIt.hasNext()) {
				ReactionGlyph reactionGlyph = reactionGlyphListIt.next();
				if (reactionGlyph.getReaction().equals(reactionID)) {
					AttributeHelper.setAttribute(reactionNode, SBML_Constants.SBML, SBML_Constants.REACTION_GLYPH_ID,
							reactionGlyph.getId());
					BoundingBox boundingBox = reactionGlyph.getBoundingBox();
					if (boundingBox != null) {
						Dimensions dimensions = boundingBox.getDimensions();
						if (dimensions != null) {
							double width = dimensions.getWidth();
							double height = dimensions.getHeight();
							AttributeHelper.setSize(reactionNode, width, height);
						}
						Point position = boundingBox.getPosition();
						if (position != null) {
							double x = position.getX();
							double y = position.getY();
							AttributeHelper.setPosition(reactionNode, x, y);
							// set a dummy layout ID to have a SBML layout attribute on the node
							// to be able checking whether layout information has been available or not
							AttributeHelper.setAttribute(reactionNode, SBML_Constants.SBML,
									SBML_Constants.SBML_LAYOUT_ID, "dummyLayoutID");
						} else if (AttributeHelper.hasAttribute(reactionNode, SBML_Constants.SBML,
								SBML_Constants.SBML_LAYOUT_ID))
							AttributeHelper.deleteAttribute(reactionNode, SBML_Constants.SBML,
									SBML_Constants.SBML_LAYOUT_ID);
					}

					if (SBML_XML_Reader.isFixPath2Models())
						reactionGlyphsToBeAdded = fixPath2ModelsSBMLLayout(reaction, reactionNode, reactionGlyph);

				}
			}

			if (reactionGlyphsToBeAdded.size() > 0)
				layout.getListOfReactionGlyphs().addAll(reactionGlyphsToBeAdded);

		}
	}

	private static ArrayList<ReactionGlyph> fixPath2ModelsSBMLLayout(Reaction reaction, Node reactionNode,
			ReactionGlyph reactionGlyph) {

		// we have a problem with the Path2Models sbml files
		// reactions which should have two reaction glyphs are not stored correctly in
		// the layout section of the sbml files
		// each reaction has just one reaction glyph and all species glyphs exists twice
		// in its definition
		// TODO: currently only one clone of the reaction node and reaction glyph is
		// created

		ArrayList<ReactionGlyph> reactionGlyphsToBeAdded = new ArrayList<>();
		ListOf<SpeciesReferenceGlyph> speciesReferenceGlyphs = reactionGlyph.getListOfSpeciesReferenceGlyphs();

		// for each species store it's number of and a list of according species glyphs
		// in the definition of the reaction glyph
		HashMap<String, Integer> hmSpecies = new HashMap<>();
		HashMap<String, ArrayList<SpeciesReferenceGlyph>> hmSpeciesReferenceGlyphs = new HashMap<>();
		for (SpeciesReferenceGlyph speciesReferenceGlyph : speciesReferenceGlyphs) {
			SpeciesGlyph speciesGlyph = speciesReferenceGlyph.getSpeciesGlyphInstance();
			String speciesID = speciesGlyph.getSpecies();
			String speciesRole = speciesReferenceGlyph.getSpeciesReferenceRole().toString();
			if (reaction.isReversible() && (speciesRole.equals(SpeciesReferenceRole.SUBSTRATE.toString())
					|| speciesRole.equals(SpeciesReferenceRole.PRODUCT.toString())))
				speciesRole = SpeciesReferenceRole.SUBSTRATE.toString() + "_" + SpeciesReferenceRole.PRODUCT.toString();
			Integer numberOf = Integer.valueOf(1);
			if (hmSpecies.get(speciesRole + "_" + speciesID) != null)
				numberOf = Integer.valueOf(hmSpecies.get(speciesRole + "_" + speciesID).intValue() + 1);
			hmSpecies.put(speciesRole + "_" + speciesID, numberOf);
			ArrayList<SpeciesReferenceGlyph> _speciesReferenceGlyphs = new ArrayList<>();
			if (hmSpeciesReferenceGlyphs.get(speciesRole + "_" + speciesID) != null)
				_speciesReferenceGlyphs = hmSpeciesReferenceGlyphs.get(speciesRole + "_" + speciesID);
			_speciesReferenceGlyphs.add(speciesReferenceGlyph);
			hmSpeciesReferenceGlyphs.put(speciesRole + "_" + speciesID, _speciesReferenceGlyphs);
		}

		// reaction node and reaction glyph have to be cloned if all species have two or
		// more according species glyphs
		// and the number of species glyphs is the same for all species
		boolean toBeCloned = true;
		if (hmSpecies.size() > 0) {
			ArrayList<Integer> numberOf = new ArrayList<>(hmSpecies.values());
			// set number of species glyphs for first species as reference
			int reference = numberOf.get(0).intValue();
			if (reference < 2)
				// number of species glyphs < 2, no cloning necessary
				toBeCloned = false;
			else
				// compare reference with number of species glyphs for all other species
				for (int k = 1; k < numberOf.size(); k++)
					if (numberOf.get(k).intValue() != reference) {
						// number of species glyphs for this species != reference, no cloning
						toBeCloned = false;
						break;
					}
		} else
			toBeCloned = false;

		// clone the reaction node and the reaction glyph
		if (toBeCloned) {
			// clone the reaction node and reassign edges
			Graph graph = reactionNode.getGraph();
			Node clonedReactionNode = graph.addNodeCopy(reactionNode);
			String reactionGlyphID = reactionGlyph.getId();
			int idxClone = 1;
			AttributeHelper.setAttribute(clonedReactionNode, SBML_Constants.SBML, "reactionGlyphID",
					reactionGlyphID + "_clone_" + idxClone);
			if (AttributeHelper.hasAttribute(clonedReactionNode, SBML_Constants.SBML, SBML_Constants.SBML_LAYOUT_ID))
				AttributeHelper.deleteAttribute(clonedReactionNode, SBML_Constants.SBML, SBML_Constants.SBML_LAYOUT_ID);
			// TODO: check whether this correct!!! probably not
			for (Edge edge : reactionNode.getEdges())
				if (reactionNode.equals(edge.getSource()))
					graph.addEdgeCopy(edge, clonedReactionNode, edge.getTarget());
				else
					graph.addEdgeCopy(edge, edge.getSource(), clonedReactionNode);

			// clone the reaction glyph and reassign species glyphs
			// TODO: maybe distance is not the right measure for the decision to reassign a
			// species glyph
			// what should be done if no position is available (see KEGG overview maps, this
			// should be carefully debugged)
			// clone the reaction glyph
			ReactionGlyph clonedReactionGlyph = new ReactionGlyph();
			clonedReactionGlyph.setId(reactionGlyphID + "_clone_" + idxClone);
			clonedReactionGlyph.setVersion(reactionGlyph.getVersion());
			clonedReactionGlyph.setNamedSBase(reactionGlyph.getNamedSBaseInstance());
			clonedReactionGlyph.setLevel(reactionGlyph.getLevel());
			clonedReactionGlyph.setVersion(reactionGlyph.getVersion());

			Point reactionGlyphPos = null;
			if (reactionGlyph.getBoundingBox() != null)
				reactionGlyphPos = reactionGlyph.getBoundingBox().getPosition();
			ArrayList<Point> positions = new ArrayList<>();
			// get all positions from all species glyphs, ignore double occurance
			for (ArrayList<SpeciesReferenceGlyph> _speciesReferenceGlyphs : hmSpeciesReferenceGlyphs.values()) {
				ArrayList<SpeciesGlyph> alreadyProcessed = new ArrayList<>();
				for (SpeciesReferenceGlyph speciesReferenceGlyph : _speciesReferenceGlyphs) {
					SpeciesGlyph speciesGlyph = speciesReferenceGlyph.getSpeciesGlyphInstance();
					if (!alreadyProcessed.contains(speciesGlyph) && speciesGlyph.getBoundingBox() != null
							&& speciesGlyph.getBoundingBox().getPosition() != null) {
						positions.add(speciesGlyph.getBoundingBox().getPosition());
						alreadyProcessed.add(speciesGlyph);
					}
				}
			}

			// use the stored positions for k-means clustering
			Point oldCentre1 = new Point(0, 0, 0);
			Point newCentre1 = new Point(0, 0, 0);
			if (reactionGlyphPos != null) {
				oldCentre1 = reactionGlyphPos;
				newCentre1 = reactionGlyphPos;
			}

			Point oldCentre2 = new Point(0, 0, 0);
			Point newCentre2 = new Point(0, 0, 0);

			SortedMap<Double, Point> sortedPositions = new TreeMap<>();
			if (reactionGlyphPos != null)
				for (Point position : positions)
					sortedPositions.put(Double.valueOf(Math.hypot(position.getX() - reactionGlyphPos.getX(),
							position.getY() - reactionGlyphPos.getY())), position);

			if (positions.size() >= 2) {
				Random random = new Random();
				int idxRandom1 = -1;
				if (reactionGlyphPos == null) {
					idxRandom1 = random.nextInt(positions.size());
					newCentre1 = positions.get(idxRandom1);
					int idxRandom2 = idxRandom1;
					while (idxRandom2 == idxRandom1)
						idxRandom2 = random.nextInt(positions.size());
					newCentre2 = positions.get(idxRandom2);
				} else
					newCentre2 = sortedPositions.get(sortedPositions.lastKey());
			}

			while (((Math.abs(newCentre1.getX() - oldCentre1.getX()) > 0.001
					&& Math.abs(newCentre1.getY() - oldCentre1.getY()) > 0.001) || reactionGlyphPos != null)
					&& (Math.abs(newCentre2.getX() - oldCentre2.getX()) > 0.001
							&& Math.abs(newCentre2.getY() - oldCentre2.getY()) > 0.001)) {
				ArrayList<Point> positions1 = new ArrayList<>();
				ArrayList<Point> positions2 = new ArrayList<>();
				for (Point point : positions)
					if (Math.hypot(point.getX() - newCentre1.getX(), point.getY() - newCentre1.getY()) < Math
							.hypot(point.getX() - newCentre2.getX(), point.getY() - newCentre2.getY()))
						positions1.add(point);
					else
						positions2.add(point);

				if (reactionGlyphPos == null) {
					oldCentre1 = newCentre1;
					newCentre1 = calculateCentre(positions1);
				}

				oldCentre2 = newCentre2;
				newCentre2 = calculateCentre(positions2);
			}

			Point clonedReactionGlyphPos = null;
			if (newCentre2.getX() > 0.001 && newCentre2.getY() > 0.001) {
				clonedReactionGlyphPos = newCentre2;
				AttributeHelper.setPosition(clonedReactionNode, clonedReactionGlyphPos.getX(),
						clonedReactionGlyphPos.getY());
			}

			// reassign species glyphs based on their distance to the reaction glyph
			if (clonedReactionGlyphPos != null && reactionGlyphPos != null)
				for (ArrayList<SpeciesReferenceGlyph> _speciesReferenceGlyphs : hmSpeciesReferenceGlyphs.values())
					for (SpeciesReferenceGlyph speciesReferenceGlyph : _speciesReferenceGlyphs) {
						SpeciesGlyph speciesGlyph = speciesReferenceGlyph.getSpeciesGlyphInstance();
						if (speciesGlyph.getBoundingBox() != null
								&& speciesGlyph.getBoundingBox().getPosition() != null) {
							Point speciesGlyphPos = speciesGlyph.getBoundingBox().getPosition();
							if (Math.hypot(clonedReactionGlyphPos.getX() - speciesGlyphPos.getX(),
									clonedReactionGlyphPos.getY() - speciesGlyphPos.getY()) < Math.hypot(
											reactionGlyphPos.getX() - speciesGlyphPos.getX(),
											reactionGlyphPos.getY() - speciesGlyphPos.getY())) {
								speciesReferenceGlyphs.remove(speciesReferenceGlyph);
								clonedReactionGlyph.addSpeciesReferenceGlyph(speciesReferenceGlyph);
							}
						}
					}

			reactionGlyphsToBeAdded.add(clonedReactionGlyph);
		}

		return reactionGlyphsToBeAdded;

	}

	private static Point calculateCentre(ArrayList<Point> positions) {

		Point centre = new Point(0, 0, 0);
		double x = 0;
		double y = 0;
		for (Point position : positions) {
			x = x + position.getX();
			y = y + position.getY();
		}
		centre.setX(x / positions.size());
		centre.setY(y / positions.size());
		return centre;

	}

	/**
	 * The method reads in reactants, products and modifiers. It is called from the
	 * method addReactions
	 * 
	 * @param simpleRef
	 *            the reactant, product or modifier object for the import
	 * @param Edge
	 *            is the reaction node in the graph
	 * @param presentedHeadline
	 *            is visible for the user
	 * @param internHeadline
	 *            intern representation of the headline
	 */
	private void setSimpleSpeciesReferences(SimpleSpeciesReference simpleRef, Edge edge, String headline,
			SBMLReactionHelper reactionHelper) {
		/*
		 * String keySpecies = SBML_Constants.addToNiceIdList(presentedHeadline,
		 * "Species"); String keySpeciesReferenceID =
		 * SBML_Constants.addToNiceIdList(presentedHeadline, "Species Reference ID");
		 * String keySpeciesReferenceName =
		 * SBML_Constants.addToNiceIdList(presentedHeadline, "Species Reference Name");
		 * String keyMetaId = SBML_Constants.addToNiceIdList(presentedHeadline,
		 * "Meta ID"); String keySBOTerm =
		 * SBML_Constants.addToNiceIdList(presentedHeadline, "SBOTerm"); String
		 * keyToolTip = SBML_Constants.addToNiceIdList(presentedHeadline, "ToolTip");
		 */

		if (simpleRef instanceof org.sbml.jsbml.SpeciesReference) {
			/*
			 * String keyStoichiometry = SBML_Constants.addToNiceIdList(presentedHeadline,
			 * "Stoichiometry"); String keyConstant =
			 * SBML_Constants.addToNiceIdList(presentedHeadline, "Constant");
			 */
			if (((SpeciesReference) simpleRef).isSetStoichiometry()) {
				reactionHelper.setStoichiometry(edge, ((SpeciesReference) simpleRef).getStoichiometry());
			}
			if (((SpeciesReference) simpleRef).isSetConstant()) {
				reactionHelper.setConstant(edge, ((SpeciesReference) simpleRef).getConstant());
			}
		}
		if (simpleRef.isSetSpecies()) {
			reactionHelper.setSpecies(edge, simpleRef.getSpecies());
		}
		if (simpleRef.isSetId()) {
			reactionHelper.setID(edge, simpleRef.getId());
		}
		if (simpleRef.isSetName()) {
			reactionHelper.setName(edge, simpleRef.getName());
		}
		if (AttributeHelper.getSBMLrole(edge).equals(SBML_Constants.ROLE_REACTANT)) {
			if (simpleRef.isSetMetaId()) {
				reactionHelper.setMetaIDReactant(edge, simpleRef.getMetaId());
			}
			if (simpleRef.isSetSBOTerm()) {
				reactionHelper.setSBOTermReactant(edge, simpleRef.getSBOTermID());
			}
			if (simpleRef.isSetNotes()) {
				String notesString;
				try {
					notesString = simpleRef.getNotesString();
				} catch (XMLStreamException e) {
					
					e.printStackTrace();
					notesString = "";
				}
				reactionHelper.setNotesReactant(edge, notesString, simpleRef.getNotes());
			}
			if (simpleRef.isSetAnnotation()) {
				if (simpleRef.getAnnotation().isSetRDFannotation()) {
					reactionHelper.setAnnotationReactant(edge, simpleRef.getAnnotation());
				}
				if (simpleRef.getAnnotation().isSetNonRDFannotation()) {
					reactionHelper.setNonRDFAnnotationReactant(edge, simpleRef.getAnnotation().getNonRDFannotation());
				}
			}
		}
		if (AttributeHelper.getSBMLrole(edge).equals(SBML_Constants.ROLE_PRODUCT)) {
			if (simpleRef.isSetMetaId()) {
				reactionHelper.setMetaIDProduct(edge, simpleRef.getMetaId());
			}
			if (simpleRef.isSetSBOTerm()) {
				reactionHelper.setSBOTermProduct(edge, simpleRef.getSBOTermID());
			}
			if (simpleRef.isSetNotes()) {
				String notesString;
				try {
					notesString = simpleRef.getNotesString();
				} catch (XMLStreamException e) {
					
					e.printStackTrace();
					notesString = "";
				}
				reactionHelper.setNotesProduct(edge, notesString, simpleRef.getNotes());
			}
			if (simpleRef.isSetAnnotation()) {
				if (simpleRef.getAnnotation().isSetRDFannotation()) {
					reactionHelper.setAnnotationProduct(edge, simpleRef.getAnnotation());
				}
				if (simpleRef.getAnnotation().isSetNonRDFannotation()) {
					reactionHelper.setNonRDFAnnotationProduct(edge, simpleRef.getAnnotation().getNonRDFannotation());
				}
			}
		}
		if (AttributeHelper.getSBMLrole(edge).equals(SBML_Constants.ROLE_MODIFIER)) {
			if (simpleRef.isSetMetaId()) {
				reactionHelper.setMetaIDModifier(edge, simpleRef.getMetaId());
			}
			if (simpleRef.isSetSBOTerm()) {
				reactionHelper.setSBOTermModifier(edge, simpleRef.getSBOTermID());
			}
			if (simpleRef.isSetNotes()) {
				String notesString;
				try {
					notesString = simpleRef.getNotesString();
				} catch (XMLStreamException e) {
					
					e.printStackTrace();
					notesString = "";
				}
				reactionHelper.setNotesModifier(edge, notesString, simpleRef.getNotes());
			}
			if (simpleRef.isSetAnnotation()) {
				if (simpleRef.getAnnotation().isSetRDFannotation()) {
					reactionHelper.setAnnotationModifier(edge, simpleRef.getAnnotation());
				}
				if (simpleRef.getAnnotation().isSetNonRDFannotation()) {
					reactionHelper.setNonRDFAnnotationModifier(edge, simpleRef.getAnnotation().getNonRDFannotation());
				}
			}
		}
	}

	/**
	 * Adds an edge between an reaction node and a reactant
	 * 
	 * @param g
	 *            is the data structure for reading in the information.
	 * @param molecules
	 *            contains the reactants of the reaction
	 * @param reactionID
	 *            contains the reaction ID
	 * @param modelID
	 *            contains the model ID
	 * @param reactionNode
	 *            is the reaction node in the graph
	 * @param reversible
	 *            indicates if the reaction is reversible
	 */
	private void addReactants(Graph g, ListOf<SpeciesReference> molecules, String reactionID, String modelID,
			Node reactionNode, boolean reversible, SBMLReactionHelper reactionHelper) {
		Iterator<SpeciesReference> it = molecules.iterator();
		String stoichiometry;
		SpeciesReference ref;
		Edge newReactionEdge;
		Node reactantNode;
		while (it.hasNext()) {
			ref = it.next();
			reactantNode = SBMLSpeciesHelper.getSpeciesNode(ref.getSpecies());
			stoichiometry = Double.toString(ref.getStoichiometry());
			newReactionEdge = g.addEdge(reactantNode, reactionNode, true,
					AttributeHelper.getDefaultGraphicsAttributeForEdge(Color.BLACK, Color.BLACK, true));
			reactionHelper.addReactantCloneToList(reactionID, ref.getSpecies(), newReactionEdge);
			// System.out.println("In Reaction Reader: " + reactionID + " " +
			// ref.getSpecies());
			if (reversible) {
				AttributeHelper.setArrowtail(newReactionEdge, true);
			}
			if (Double.isNaN(ref.getStoichiometry())) {
				SBML_Logger.addErrorMessage("Attribute stochiometry of reaction " + reactionID + " species "
						+ ref.getSpecies() + " is not a valid double value.");
			} else {
				AttributeHelper.setLabel(newReactionEdge, stoichiometry);
				AttributeHelper.setAttribute(newReactionEdge, SBML_Constants.SBML, SBML_Constants.STOICHIOMETRY,
						stoichiometry);
			}
			AttributeHelper.setAttribute(newReactionEdge, SBML_Constants.SBML, SBML_Constants.SBML_ROLE,
					SBML_Constants.ROLE_REACTANT);
			// AttributeHelper.setAttribute(newReactionEdge, SBML_Constants.SBML,
			// SBML_Constants.REVERSIBLE, reversible);
			setSimpleSpeciesReferences(ref, newReactionEdge, SBML_Constants.SBML, reactionHelper);
		}
	}

	/**
	 * Adds an edge between an reaction node and a product
	 * 
	 * @param g
	 *            is the data structure for reading in the information
	 * @param molecules
	 *            contains the products of the reaction
	 * @param reactionID
	 *            contains the reaction ID
	 * @param modelID
	 *            contains the model ID
	 * @param reactionNode
	 *            is the reaction node in the graph
	 * @param reversible
	 *            indicates if the reaction is reversible
	 */
	private void addProducts(Graph g, ListOf<SpeciesReference> molecules, String reactionID, String modelID,
			Node reactionNode, boolean reversible, SBMLReactionHelper reactionHelper) {
		Iterator<SpeciesReference> it = molecules.iterator();
		Edge newReactionEdge;
		String stoichiometry;
		Node productNode;
		SpeciesReference ref;
		while (it.hasNext()) {
			ref = it.next();
			productNode = SBMLSpeciesHelper.getSpeciesNode(ref.getSpecies());
			stoichiometry = Double.toString(ref.getStoichiometry());
			newReactionEdge = g.addEdge(reactionNode, productNode, true,
					AttributeHelper.getDefaultGraphicsAttributeForEdge(Color.BLACK, Color.BLACK, true));
			reactionHelper.addProductCloneToList(reactionID, ref.getSpecies(), newReactionEdge);

			if (reversible) {
				AttributeHelper.setArrowtail(newReactionEdge, true);
			}

			if (Double.isNaN(ref.getStoichiometry())) {
				SBML_Logger.addErrorMessage("Attribute stochiometry of reaction " + reactionID + " species "
						+ ref.getSpecies() + " is not a valid double value.");
			} else {
				AttributeHelper.setLabel(newReactionEdge, stoichiometry);
				AttributeHelper.setAttribute(newReactionEdge, SBML_Constants.SBML, SBML_Constants.STOICHIOMETRY,
						stoichiometry);
			}
			AttributeHelper.setAttribute(newReactionEdge, SBML_Constants.SBML, SBML_Constants.SBML_ROLE,
					SBML_Constants.ROLE_PRODUCT);
			// AttributeHelper.setAttribute(newReactionEdge, SBML_Constants.SBML,
			// SBML_Constants.REVERSIBLE, reversible);
			setSimpleSpeciesReferences(ref, newReactionEdge, SBML_Constants.SBML, reactionHelper);
		}
	}

	/**
	 * Adds an edge between an reaction node and a modifier
	 * 
	 * @param g
	 *            is the data structure for reading in the information.
	 * @param molecules
	 *            contains the modifiers of the reaction.
	 * @param reactionNode
	 *            is the node that will be connected with the modifier
	 */
	private void addModifier(Graph g, ListOf<ModifierSpeciesReference> molecules, Node reactionNode,
			SBMLReactionHelper reactionHelper, String reactionID) {
		Iterator<ModifierSpeciesReference> it = molecules.iterator();
		Edge reactionEdge;
		ModifierSpeciesReference ref;
		Node modifierNode;
		while (it.hasNext()) {
			ref = it.next();
			modifierNode = SBMLSpeciesHelper.getSpeciesNode(ref.getSpecies());
			reactionEdge = g.addEdge(modifierNode, reactionNode, false,
					AttributeHelper.getDefaultGraphicsAttributeForEdge(Color.GRAY, Color.GRAY, true));

			AttributeHelper.setAttribute(reactionEdge, SBML_Constants.SBML, SBML_Constants.SBML_ROLE,
					SBML_Constants.ROLE_MODIFIER);
			reactionHelper.addModifierCloneToList(reactionID, ref.getSpecies(), reactionEdge);
			// AttributeHelper.setDashInfo(reactionEdge, 5, 5);
			// AttributeHelper.setBorderWidth(reactionEdge, 1d);
			setSimpleSpeciesReferences(ref, reactionEdge, SBML_Constants.SBML, reactionHelper);
		}
	}
}