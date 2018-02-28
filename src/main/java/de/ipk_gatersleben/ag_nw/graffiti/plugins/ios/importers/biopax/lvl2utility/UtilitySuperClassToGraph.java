package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.biopax.lvl2utility;

import java.util.Set;

import org.AttributeHelper;
import org.biopax.paxtools.model.level2.ControlType;
import org.biopax.paxtools.model.level2.Direction;
import org.biopax.paxtools.model.level2.SpontaneousType;
import org.biopax.paxtools.model.level2.bioSource;
import org.biopax.paxtools.model.level2.chemicalStructure;
import org.biopax.paxtools.model.level2.dataSource;
import org.biopax.paxtools.model.level2.deltaGprimeO;
import org.biopax.paxtools.model.level2.entity;
import org.biopax.paxtools.model.level2.evidence;
import org.biopax.paxtools.model.level2.kPrime;
import org.biopax.paxtools.model.level2.openControlledVocabulary;
import org.biopax.paxtools.model.level2.physicalEntityParticipant;
import org.biopax.paxtools.model.level2.publicationXref;
import org.biopax.paxtools.model.level2.relationshipXref;
import org.biopax.paxtools.model.level2.unificationXref;
import org.biopax.paxtools.model.level2.xref;
import org.graffiti.graph.GraphElement;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.HelperClass;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.Messages;

/**
 * this class's functions sets attributes to each node
 * 
 * @author ricardo
 * 
 */
public class UtilitySuperClassToGraph extends HelperClass {
	protected static void setLabels(GraphElement elem, entity i) {
		if (i.getNAME() != null) {
			AttributeHelper.setLabel(elem, i.getNAME());
		} else if (i.getSHORT_NAME() != null) {
			AttributeHelper.setLabel(elem, i.getSHORT_NAME());
		} else if (i.getSYNONYMS() != null) {
			for (String name : i.getSYNONYMS()) {
				AttributeHelper.setLabel(elem, name);
				break;
			}
		}
		setAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.126"), i.getClass().getName());
	}

	protected static void setAvailability(GraphElement elem, Set<String> availability) {
		setAttributeOfSetOfString(elem, Messages.getString("UtilitySuperClassToGraph.0"), availability); //$NON-NLS-1$
	}

	protected static void setCatalysisDirection(GraphElement elem, Direction direction) {
		if (direction != null) {
			setAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.1"), direction.name()); //$NON-NLS-1$
		}
	}

	protected static void setChemicalFormula(GraphElement elem, String chemical_FORMULA) {
		if (chemical_FORMULA != null)
			setAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.105"), chemical_FORMULA); //$NON-NLS-1$
	}

	protected static void setCofactor(GraphElement elem, Set<physicalEntityParticipant> cofactor) {
		/*
		 * left-out attributes: - cellularLocation, feature, memberPhysicalEntity,
		 * notFeature, availability, comment, dataSource, evidence, name, xref
		 */
		int i = 1;
		for (physicalEntityParticipant P : cofactor) {
			setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.10"), i, //$NON-NLS-1$
					P.getPHYSICAL_ENTITY().getNAME());
			setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.11"), i, //$NON-NLS-1$
					P.getPHYSICAL_ENTITY().getRDFId());
			i++;
		}
	}

	protected static void setComment(GraphElement elem, Set<String> comment) {
		setAttributeOfSetOfString(elem, Messages.getString("UtilitySuperClassToGraph.12"), comment); //$NON-NLS-1$
	}

	protected static void setComponent(GraphElement elem, Set<physicalEntityParticipant> components) {
		/*
		 * left-out attributes: - cellularLocation, feature, memberPhysicalEntity,
		 * notFeature, availability, comment, dataSource, evidence, name, xref
		 */
		int i = 1;
		for (physicalEntityParticipant s : components) {
			setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.13"), i, //$NON-NLS-1$
					s.getPHYSICAL_ENTITY().getNAME());
			setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.14"), i, //$NON-NLS-1$
					s.getPHYSICAL_ENTITY().getRDFId());
			i++;
		}
	}

	protected static void setControlType(GraphElement elem, ControlType control_TYPE) {
		/*
		 * left-out attributes: - comments, xref
		 */
		if (control_TYPE != null) {
			setAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.19"), control_TYPE.name()); //$NON-NLS-1$
		}
	}

	protected static void setDataSource(GraphElement elem, Set<dataSource> data_SOURCE) {
		int i = 1;
		for (dataSource p : data_SOURCE) {
			/*
			 * left-out attributes: - comments, xref, standard name
			 */
			int j = 1;
			for (String name : p.getNAME()) {
				setAttributeWithTwoInnerReplacements(elem, Messages.getString("UtilitySuperClassToGraph.21"), i, j, //$NON-NLS-1$
						name);
				j++;
			}
			setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.23"), i, //$NON-NLS-1$
					p.getRDFId());
			i++;
		}
	}

	protected static void setDeltaG(GraphElement elem, Set<deltaGprimeO> delta_G) {
		/*
		 * left-out attributes: - Comments
		 */
		int i = 1;
		for (deltaGprimeO d : delta_G) {
			setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.24"), i, //$NON-NLS-1$
					String.valueOf(d.getDELTA_G_PRIME_O()));
			setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.25"), i, //$NON-NLS-1$
					String.valueOf(d.getIONIC_STRENGTH()));
			setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.26"), i, //$NON-NLS-1$
					String.valueOf(d.getPH()));
			setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.27"), i, //$NON-NLS-1$
					String.valueOf(d.getPMG()));
			setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.28"), i, //$NON-NLS-1$
					String.valueOf(d.getTEMPERATURE()));
			setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.29"), i, //$NON-NLS-1$
					d.getRDFId());
			i++;
		}
	}

	protected static void setDeltaH(GraphElement elem, Set<Double> delta_H) {
		setAttributeOfSetOfDouble(elem, Messages.getString("UtilitySuperClassToGraph.30"), delta_H); //$NON-NLS-1$
	}

	protected static void setDeltaS(GraphElement elem, Set<Double> delta_S) {
		setAttributeOfSetOfDouble(elem, Messages.getString("UtilitySuperClassToGraph.31"), delta_S); //$NON-NLS-1$
	}

	protected static void setECNumber(GraphElement elem, Set<String> ec_NUMBER) {
		setAttributeOfSetOfString(elem, Messages.getString("UtilitySuperClassToGraph.32"), ec_NUMBER); //$NON-NLS-1$
	}

	protected static void setEvidence(GraphElement elem, Set<evidence> evidence) {
		/*
		 * left-out attributes: - confidence, evidenceCode, experimentalForm, xref,
		 * comment
		 */
		int i = 1;
		for (evidence e : evidence) {
			setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.46"), i, //$NON-NLS-1$
					e.getRDFId());
			i++;
		}
	}

	protected static void setInteractionType(GraphElement elem, Set<openControlledVocabulary> interaction_TYPE) {
		/*
		 * left-out attributes: - xref, comment
		 */
		int x = 1;
		for (openControlledVocabulary i : interaction_TYPE) {
			int j = 1;
			for (String s : i.getTERM()) {
				setAttributeWithTwoInnerReplacements(elem, Messages.getString("UtilitySuperClassToGraph.62"), x, j, s); //$NON-NLS-1$
				j++;
			}
			setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.63"), x, //$NON-NLS-1$
					i.getRDFId());
			x++;
		}
	}

	protected static void setKPrime(GraphElement elem, Set<kPrime> keq) {
		int i = 1;
		for (kPrime k : keq) {
			int c = 1;
			for (String s : k.getCOMMENT()) {
				setAttributeWithTwoInnerReplacements(elem, Messages.getString("UtilitySuperClassToGraph.64"), i, c, s); //$NON-NLS-1$
				c++;
			}
			setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.65"), i, //$NON-NLS-1$
					String.valueOf(k.getIONIC_STRENGTH()));
			setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.66"), i, //$NON-NLS-1$
					String.valueOf(k.getK_PRIME()));
			setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.67"), i, //$NON-NLS-1$
					String.valueOf(k.getPH()));
			setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.68"), i, //$NON-NLS-1$
					String.valueOf(k.getPMG()));
			setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.69"), i, //$NON-NLS-1$
					String.valueOf(k.getTEMPERATURE()));
			setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.70"), i, //$NON-NLS-1$
					k.getRDFId());
			i++;
		}
	}

	protected static void setMolecularWeight(GraphElement elem, double molecular_WEIGHT) {
		setAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.106"), String.valueOf(molecular_WEIGHT)); //$NON-NLS-1$
	}

	protected static void setName(GraphElement elem, String name) {
		setAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.107"), name); //$NON-NLS-1$
	}

	protected static void setOrganism(GraphElement elem, bioSource organism) {
		/*
		 * left-out attributes: - name, xref, comment
		 */
		if (organism != null) {
			setAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.75"), organism.getNAME()); //$NON-NLS-1$
			setAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.76"), organism.getRDFId()); //$NON-NLS-1$
			if (organism.getCELLTYPE() != null) {
				setAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.77"), //$NON-NLS-1$
						organism.getCELLTYPE().getRDFId());
			}
			if (organism.getTISSUE() != null) {
				setAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.78"), //$NON-NLS-1$
						organism.getTISSUE().getRDFId());
			}
		}
	}

	protected static void setRDFId(GraphElement elem, String rdfId) {
		setAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.82"), rdfId); //$NON-NLS-1$
	}

	protected static void setSequence(GraphElement elem, String sequence) {
		setAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.108"), sequence); //$NON-NLS-1$
	}

	protected static void setShortName(GraphElement elem, String short_NAME) {
		setAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.109"), short_NAME); //$NON-NLS-1$
	}

	protected static void setSpontaneous(GraphElement elem, SpontaneousType spontaneous) {
		if (spontaneous != null)
			setAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.83"), spontaneous.toString()); //$NON-NLS-1$
	}

	protected static void setStructure(GraphElement elem, Set<chemicalStructure> structure) {
		/*
		 * left-out attributes: - comment
		 */
		int i = 1;
		for (chemicalStructure cS : structure) {
			setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.110"), i, //$NON-NLS-1$
					cS.getSTRUCTURE_FORMAT());
			setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.111"), i, //$NON-NLS-1$
					cS.getSTRUCTURE_DATA());
			setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.112"), i, //$NON-NLS-1$
					cS.getRDFId());
			i++;
		}
	}

	protected static void setSynonyms(GraphElement elem, Set<String> synonyms) {
		setAttributeOfSetOfString(elem, Messages.getString("UtilitySuperClassToGraph.113"), synonyms); //$NON-NLS-1$
	}

	protected static void setXRef(GraphElement elem, Set<xref> xref) {
		int i = 1;
		for (xref x : xref) {
			if (x instanceof unificationXref) {
				unificationXref xx = (unificationXref) x;
				setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.87"), i, //$NON-NLS-1$
						xx.getDB());
				setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.88"), i, //$NON-NLS-1$
						xx.getDB_VERSION());
				setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.89"), i, //$NON-NLS-1$
						xx.getID());
				setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.90"), i, //$NON-NLS-1$
						xx.getID_VERSION());
				setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.91"), i, //$NON-NLS-1$
						xx.getRDFId());
			}
			if (x instanceof relationshipXref) {
				relationshipXref xx = (relationshipXref) x;
				setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.92"), i, //$NON-NLS-1$
						xx.getRDFId());
			}
			if (x instanceof publicationXref) {
				publicationXref xx = (publicationXref) x;
				setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.95"), i, //$NON-NLS-1$
						xx.getRDFId());
				setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.96"), i, //$NON-NLS-1$
						xx.getDB());
				setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.97"), i, //$NON-NLS-1$
						xx.getDB_VERSION());
				setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.98"), i, //$NON-NLS-1$
						xx.getID());
				setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.99"), i, //$NON-NLS-1$
						xx.getID_VERSION());

				setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.100"), i, //$NON-NLS-1$
						xx.getTITLE());
				setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.101"), i, //$NON-NLS-1$
						String.valueOf(xx.getYEAR()));
				int j = 1;
				for (String a : xx.getAUTHORS()) {
					setAttributeWithTwoInnerReplacements(elem, Messages.getString("UtilitySuperClassToGraph.102"), i, j, //$NON-NLS-1$
							a);
					j++;
				}
				j = 1;
				for (String s : xx.getSOURCE()) {
					setAttributeWithTwoInnerReplacements(elem, Messages.getString("UtilitySuperClassToGraph.103"), i, j, //$NON-NLS-1$
							s);
					j++;
				}

				j = 1;
				for (String u : xx.getURL()) {
					setAttributeWithTwoInnerReplacements(elem, Messages.getString("UtilitySuperClassToGraph.104"), i, j, //$NON-NLS-1$
							u);
					j++;
				}
			}
			i++;
		}

	}
}
