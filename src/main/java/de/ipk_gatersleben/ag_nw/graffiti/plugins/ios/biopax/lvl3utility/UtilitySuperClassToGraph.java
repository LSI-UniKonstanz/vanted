package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3utility;

import java.util.Set;

import org.AttributeHelper;
import org.biopax.paxtools.model.level3.BindingFeature;
import org.biopax.paxtools.model.level3.BioSource;
import org.biopax.paxtools.model.level3.CatalysisDirectionType;
import org.biopax.paxtools.model.level3.CellularLocationVocabulary;
import org.biopax.paxtools.model.level3.ControlType;
import org.biopax.paxtools.model.level3.ConversionDirectionType;
import org.biopax.paxtools.model.level3.DeltaG;
import org.biopax.paxtools.model.level3.DnaReference;
import org.biopax.paxtools.model.level3.DnaRegionReference;
import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.model.level3.EntityFeature;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.Evidence;
import org.biopax.paxtools.model.level3.FragmentFeature;
import org.biopax.paxtools.model.level3.InteractionVocabulary;
import org.biopax.paxtools.model.level3.KPrime;
import org.biopax.paxtools.model.level3.ModificationFeature;
import org.biopax.paxtools.model.level3.PhenotypeVocabulary;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.ProteinReference;
import org.biopax.paxtools.model.level3.Provenance;
import org.biopax.paxtools.model.level3.PublicationXref;
import org.biopax.paxtools.model.level3.RelationshipXref;
import org.biopax.paxtools.model.level3.RnaReference;
import org.biopax.paxtools.model.level3.RnaRegionReference;
import org.biopax.paxtools.model.level3.Score;
import org.biopax.paxtools.model.level3.SequenceLocation;
import org.biopax.paxtools.model.level3.SequenceModificationVocabulary;
import org.biopax.paxtools.model.level3.SequenceRegionVocabulary;
import org.biopax.paxtools.model.level3.SmallMoleculeReference;
import org.biopax.paxtools.model.level3.Stoichiometry;
import org.biopax.paxtools.model.level3.UnificationXref;
import org.biopax.paxtools.model.level3.Xref;
import org.graffiti.graph.GraphElement;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.HelperClass;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.Messages;

/**
 * this class's functions sets attributes to each node
 * 
 * @author ricardo
 */
public class UtilitySuperClassToGraph extends HelperClass {
	protected static void setLabels(GraphElement elem, Entity i) {
		if (i.getDisplayName() != null) {
			AttributeHelper.setLabel(elem, i.getDisplayName());
		} else if (i.getStandardName() != null) {
			AttributeHelper.setLabel(elem, i.getStandardName());
		} else if (i.getName() != null) {
			for (String name : i.getName()) {
				AttributeHelper.setLabel(elem, name);
				break;
			}
		}
		setAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.126"), i.getClass().getName());
		
	}
	
	protected static void setAvailability(GraphElement elem, Set<String> availability) {
		setAttributeOfSetOfString(elem, Messages.getString("UtilitySuperClassToGraph.0"), availability); //$NON-NLS-1$
	}
	
	protected static void setCatalysisDirection(GraphElement elem, CatalysisDirectionType catalysisDirection) {
		if (catalysisDirection != null) {
			setAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.1"), catalysisDirection.name()); //$NON-NLS-1$
		}
	}
	
	protected static void setCellularLocation(GraphElement elem, CellularLocationVocabulary cellularLocation) {
		/*
		 * left-out attributes: - Comments
		 */
		if (cellularLocation != null) {
			CellularLocationVocabulary cl = cellularLocation;
			setAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.2"), cl.getRDFId()); //$NON-NLS-1$
			setAttributeOfSetOfString(elem, Messages.getString("UtilitySuperClassToGraph.3"), cl.getTerm()); //$NON-NLS-1$
			
			int i = 1;
			for (Xref x : cl.getXref()) {
				setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.4"), i, //$NON-NLS-1$
						x.getRDFId());
				
				int j = 1;
				for (String c : x.getComment()) {
					setAttributeWithTwoInnerReplacements(elem, Messages.getString("UtilitySuperClassToGraph.5"), i, j, //$NON-NLS-1$
							c);
					j++;
				}
				setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.6"), i, //$NON-NLS-1$
						x.getDb());
				setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.7"), i, //$NON-NLS-1$
						x.getDbVersion());
				setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.8"), i, //$NON-NLS-1$
						x.getId());
				setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.9"), i, //$NON-NLS-1$
						x.getIdVersion());
				
				i++;
			}
		}
	}
	
	protected static void setCofactor(GraphElement elem, Set<PhysicalEntity> cofactor) {
		/*
		 * left-out attributes: - cellularLocation, feature, memberPhysicalEntity,
		 * notFeature, availability, comment, dataSource, evidence, name, xref
		 */
		int i = 1;
		for (PhysicalEntity P : cofactor) {
			setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.10"), i, //$NON-NLS-1$
					P.getDisplayName());
			setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.11"), i, //$NON-NLS-1$
					P.getRDFId());
			i++;
		}
	}
	
	protected static void setComment(GraphElement elem, Set<String> comment) {
		setAttributeOfSetOfString(elem, Messages.getString("UtilitySuperClassToGraph.12"), comment); //$NON-NLS-1$
	}
	
	protected static void setComponent(GraphElement elem, Set<PhysicalEntity> component) {
		/*
		 * left-out attributes: - cellularLocation, feature, memberPhysicalEntity,
		 * notFeature, availability, comment, dataSource, evidence, name, xref
		 */
		int i = 1;
		for (PhysicalEntity s : component) {
			setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.13"), i, //$NON-NLS-1$
					s.getDisplayName());
			setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.14"), i, //$NON-NLS-1$
					s.getRDFId());
			i++;
		}
	}
	
	protected static void setComponentStoichiometry(GraphElement elem, Set<Stoichiometry> componentStoichiometry) {
		/*
		 * left-out attributes: - comment, physical entity is only used from Complex ->
		 * gets written and read but isn't important to do on an edge because there are
		 * no
		 */
		int i = 1;
		for (Stoichiometry s : componentStoichiometry) {
			setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.15"), i, //$NON-NLS-1$
					s.getPhysicalEntity().getDisplayName());
			setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.16"), i, //$NON-NLS-1$
					s.getPhysicalEntity().getRDFId());
			setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.17"), i, //$NON-NLS-1$
					String.valueOf(s.getStoichiometricCoefficient()));
			setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.18"), i, //$NON-NLS-1$
					s.getRDFId());
			i++;
		}
	}
	
	protected static void setControlType(GraphElement elem, ControlType controlType) {
		/*
		 * left-out attributes: - comments, xref
		 */
		if (controlType != null) {
			setAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.19"), controlType.name()); //$NON-NLS-1$
		}
	}
	
	protected static void setConversionDirection(GraphElement elem, ConversionDirectionType conversionDirection) {
		if (conversionDirection != null) {
			setAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.20"), conversionDirection.name()); //$NON-NLS-1$
		}
	}
	
	protected static void setDataSource(GraphElement elem, Set<Provenance> dataSource) {
		int i = 1;
		for (Provenance p : dataSource) {
			/*
			 * left-out attributes: - comments, xref, standard name
			 */
			int j = 1;
			for (String name : p.getName()) {
				setAttributeWithTwoInnerReplacements(elem, Messages.getString("UtilitySuperClassToGraph.21"), i, j, //$NON-NLS-1$
						name);
				j++;
			}
			setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.22"), i, //$NON-NLS-1$
					p.getDisplayName());
			setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.23"), i, //$NON-NLS-1$
					p.getRDFId());
			i++;
		}
	}
	
	protected static void setDeltaG(GraphElement elem, Set<DeltaG> deltaG) {
		/*
		 * left-out attributes: - Comments
		 */
		int i = 1;
		for (DeltaG d : deltaG) {
			setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.24"), i, //$NON-NLS-1$
					String.valueOf(d.getDeltaGPrime0()));
			setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.25"), i, //$NON-NLS-1$
					String.valueOf(d.getIonicStrength()));
			setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.26"), i, //$NON-NLS-1$
					String.valueOf(d.getPh()));
			setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.27"), i, //$NON-NLS-1$
					String.valueOf(d.getPMg()));
			setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.28"), i, //$NON-NLS-1$
					String.valueOf(d.getTemperature()));
			setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.29"), i, //$NON-NLS-1$
					d.getRDFId());
			i++;
		}
	}
	
	protected static void setDeltaH(GraphElement elem, Set<Float> deltaH) {
		setAttributeOfSetOfFloat(elem, Messages.getString("UtilitySuperClassToGraph.30"), deltaH); //$NON-NLS-1$
	}
	
	protected static void setDeltaS(GraphElement elem, Set<Float> deltaS) {
		setAttributeOfSetOfFloat(elem, Messages.getString("UtilitySuperClassToGraph.31"), deltaS); //$NON-NLS-1$
	}
	
	protected static void setECNumber(GraphElement elem, Set<String> ecNumber) {
		setAttributeOfSetOfString(elem, Messages.getString("UtilitySuperClassToGraph.32"), ecNumber); //$NON-NLS-1$
	}
	
	protected static void setEntityReference(GraphElement elem, EntityReference entityReference) {
		/*
		 * left-out attributes: - entityFeature, entityReferenceType, evidence,
		 * memberEntityReference, name ,xref, many more of the subclasses
		 */
		if (entityReference != null) {
			if (entityReference instanceof DnaReference) {
				setAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.33"), entityReference.getRDFId()); //$NON-NLS-1$
				setAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.34"), //$NON-NLS-1$
						entityReference.getDisplayName());
			} else if (entityReference instanceof DnaRegionReference) {
				setAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.35"), entityReference.getRDFId()); //$NON-NLS-1$
				setAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.36"), //$NON-NLS-1$
						entityReference.getDisplayName());
			} else if (entityReference instanceof RnaReference) {
				setAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.37"), entityReference.getRDFId()); //$NON-NLS-1$
				setAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.38"), //$NON-NLS-1$
						entityReference.getDisplayName());
			} else if (entityReference instanceof RnaRegionReference) {
				setAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.39"), entityReference.getRDFId()); //$NON-NLS-1$
				setAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.40"), //$NON-NLS-1$
						entityReference.getDisplayName());
			} else if (entityReference instanceof SmallMoleculeReference) {
				setAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.41"), entityReference.getRDFId()); //$NON-NLS-1$
				setAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.42"), //$NON-NLS-1$
						entityReference.getDisplayName());
			} else if (entityReference instanceof ProteinReference) {
				setAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.43"), entityReference.getRDFId()); //$NON-NLS-1$
				setAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.44"), //$NON-NLS-1$
						entityReference.getDisplayName());
			} else {
				System.out.println("There was one unknown EntityReferenceType: " + entityReference.getRDFId()); //$NON-NLS-1$
			}
		}
	}
	
	protected static void setEvidence(GraphElement elem, Set<Evidence> evidence) {
		/*
		 * left-out attributes: - confidence, evidenceCode, experimentalForm, xref,
		 * comment
		 */
		int i = 1;
		for (Evidence e : evidence) {
			setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.46"), i, //$NON-NLS-1$
					e.getRDFId());
			i++;
		}
	}
	
	protected static void setFeature(GraphElement elem, Set<EntityFeature> feature) {
		/*
		 * left-out attributes: - evidence, featureLocation, featureLocationType,
		 * memberFeature, comment
		 */
		int i = 1;
		for (EntityFeature E : feature) {
			if (E instanceof BindingFeature) {
				setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.47"), i, //$NON-NLS-1$
						E.getRDFId());
			} else if (E instanceof FragmentFeature) {
				setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.48"), i, //$NON-NLS-1$
						E.getRDFId());
			} else if (E instanceof ModificationFeature) {
				setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.49"), i, //$NON-NLS-1$
						E.getRDFId());
				if (((ModificationFeature) E).getModificationType() != null) {
					SequenceModificationVocabulary MTV = ((ModificationFeature) E).getModificationType();
					setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.50"), i, //$NON-NLS-1$
							MTV.getRDFId());
					int j = 1;
					for (String term : MTV.getTerm()) {
						setAttributeWithTwoInnerReplacements(elem, Messages.getString("UtilitySuperClassToGraph.51"), i, //$NON-NLS-1$
								j, term);
						j++;
					}
					j = 1;
					for (String comment : MTV.getComment()) {
						setAttributeWithTwoInnerReplacements(elem, Messages.getString("UtilitySuperClassToGraph.52"), i, //$NON-NLS-1$
								j, comment);
						j++;
					}
				}
				if (((ModificationFeature) E).getFeatureLocation() != null) {
					SequenceLocation SL = ((ModificationFeature) E).getFeatureLocation();
					setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.53"), i, //$NON-NLS-1$
							SL.getRDFId());
					int j = 1;
					for (String comment : SL.getComment()) {
						setAttributeWithTwoInnerReplacements(elem, Messages.getString("UtilitySuperClassToGraph.54"), i, //$NON-NLS-1$
								j, comment);
						j++;
					}
				}
				if (((ModificationFeature) E).getFeatureLocationType() != null) {
					SequenceRegionVocabulary SRV = ((ModificationFeature) E).getFeatureLocationType();
					setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.55"), i, //$NON-NLS-1$
							SRV.getRDFId());
					int j = 1;
					for (String comment : SRV.getComment()) {
						setAttributeWithTwoInnerReplacements(elem, Messages.getString("UtilitySuperClassToGraph.56"), i, //$NON-NLS-1$
								j, comment);
						j++;
					}
					j = 1;
					for (String term : SRV.getTerm()) {
						setAttributeWithTwoInnerReplacements(elem, Messages.getString("UtilitySuperClassToGraph.57"), i, //$NON-NLS-1$
								j, term);
						j++;
					}
				}
				
			} else if (E.getRDFId() != null) {
				setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.58"), i, //$NON-NLS-1$
						E.getRDFId());
			}
			i++;
		}
	}
	
	protected static void setInteractionScore(GraphElement elem, Score interactionScore) {
		/*
		 * left-out attributes: - comment
		 */
		
		if (interactionScore != null) {
			if (interactionScore.getScoreSource() != null)
				setAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.59"), //$NON-NLS-1$
						interactionScore.getScoreSource().getDisplayName());
			setAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.60"), interactionScore.getValue()); //$NON-NLS-1$
			setAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.61"), interactionScore.getRDFId()); //$NON-NLS-1$
		}
	}
	
	protected static void setInteractionType(GraphElement elem, Set<InteractionVocabulary> interactionType) {
		/*
		 * left-out attributes: - xref, comment
		 */
		int x = 1;
		for (InteractionVocabulary i : interactionType) {
			int j = 1;
			for (String s : i.getTerm()) {
				setAttributeWithTwoInnerReplacements(elem, Messages.getString("UtilitySuperClassToGraph.62"), x, j, s); //$NON-NLS-1$
				j++;
			}
			setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.63"), x, //$NON-NLS-1$
					i.getRDFId());
			x++;
		}
	}
	
	protected static void setKPrime(GraphElement elem, Set<KPrime> keq) {
		int i = 1;
		for (KPrime k : keq) {
			int c = 1;
			for (String s : k.getComment()) {
				setAttributeWithTwoInnerReplacements(elem, Messages.getString("UtilitySuperClassToGraph.64"), i, c, s); //$NON-NLS-1$
				c++;
			}
			setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.65"), i, //$NON-NLS-1$
					String.valueOf(k.getIonicStrength()));
			setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.66"), i, //$NON-NLS-1$
					String.valueOf(k.getKPrime()));
			setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.67"), i, //$NON-NLS-1$
					String.valueOf(k.getPh()));
			setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.68"), i, //$NON-NLS-1$
					String.valueOf(k.getPMg()));
			setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.69"), i, //$NON-NLS-1$
					String.valueOf(k.getTemperature()));
			setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.70"), i, //$NON-NLS-1$
					k.getRDFId());
			i++;
		}
	}
	
	protected static void setMemberPhysicalEntity(GraphElement elem, Set<PhysicalEntity> memberPhysicalEntity) {
		/*
		 * left-out attributes: - cellularLocation, feature, memberPhysicalEntity,
		 * notFeature, availability, comment, dataSource, evidence, name, xref
		 */
		int i = 1;
		for (PhysicalEntity s : memberPhysicalEntity) {
			setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.71"), i, //$NON-NLS-1$
					s.getDisplayName());
			setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.72"), i, //$NON-NLS-1$
					s.getRDFId());
			i++;
		}
	}
	
	protected static void setName(GraphElement elem, Set<String> name) {
		setAttributeOfSetOfString(elem, Messages.getString("UtilitySuperClassToGraph.73"), name); //$NON-NLS-1$
	}
	
	protected static void setNotFeature(GraphElement elem, Set<EntityFeature> notFeature) {
		/*
		 * left-out attributes: - evidence, featureLocation, featureLocationType,
		 * memberFeature, comment
		 */
		int i = 1;
		for (EntityFeature E : notFeature) {
			setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.74"), i, //$NON-NLS-1$
					E.getRDFId());
			i++;
		}
	}
	
	protected static void setOrganism(GraphElement elem, BioSource organism) {
		/*
		 * left-out attributes: - name, xref, comment
		 */
		if (organism != null) {
			setAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.75"), organism.getDisplayName()); //$NON-NLS-1$
			setAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.76"), organism.getRDFId()); //$NON-NLS-1$
			if (organism.getCellType() != null) {
				setAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.77"), //$NON-NLS-1$
						organism.getCellType().getRDFId());
			}
			if (organism.getTissue() != null) {
				setAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.78"), //$NON-NLS-1$
						organism.getTissue().getRDFId());
			}
		}
	}
	
	protected static void setPhenotype(GraphElement elem, PhenotypeVocabulary phenotype) {
		/*
		 * left-out attributes: - xref, comment, term
		 */
		if (phenotype != null) {
			setAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.79"), phenotype.getPatoData()); //$NON-NLS-1$
			setAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.80"), phenotype.getRDFId()); //$NON-NLS-1$
		}
	}
	
	protected static void setRDFId(GraphElement elem, String rdfId) {
		setAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.82"), rdfId); //$NON-NLS-1$
	}
	
	protected static void setSpontaneous(GraphElement elem, Boolean spontaneous) {
		if (spontaneous != null)
			setAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.83"), spontaneous.toString()); //$NON-NLS-1$
	}
	
	protected static void setStandardName(GraphElement elem, String standardName) {
		setAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.84"), standardName); //$NON-NLS-1$
	}
	
	protected static void setXRef(GraphElement elem, Set<Xref> xref) {
		int i = 1;
		for (Xref x : xref) {
			if (x instanceof UnificationXref) {
				UnificationXref xx = (UnificationXref) x;
				setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.87"), i, //$NON-NLS-1$
						xx.getDb());
				setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.88"), i, //$NON-NLS-1$
						xx.getDbVersion());
				setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.89"), i, //$NON-NLS-1$
						xx.getId());
				setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.90"), i, //$NON-NLS-1$
						xx.getIdVersion());
				setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.91"), i, //$NON-NLS-1$
						xx.getRDFId());
			}
			if (x instanceof RelationshipXref) {
				RelationshipXref xx = (RelationshipXref) x;
				setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.92"), i, //$NON-NLS-1$
						xx.getRDFId());
				int t = 1;
				if (xx.getRelationshipType() != null)
					for (String s : xx.getRelationshipType().getTerm()) {
						setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.93"), i, //$NON-NLS-1$
								xx.getRelationshipType().getRDFId());
						setAttributeWithTwoInnerReplacements(elem, Messages.getString("UtilitySuperClassToGraph.94"), i, //$NON-NLS-1$
								t, s);
						t++;
					}
			}
			if (x instanceof PublicationXref) {
				PublicationXref xx = (PublicationXref) x;
				setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.95"), i, //$NON-NLS-1$
						xx.getRDFId());
				setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.96"), i, //$NON-NLS-1$
						xx.getDb());
				setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.97"), i, //$NON-NLS-1$
						xx.getDbVersion());
				setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.98"), i, //$NON-NLS-1$
						xx.getId());
				setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.99"), i, //$NON-NLS-1$
						xx.getIdVersion());
				
				setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.100"), i, //$NON-NLS-1$
						xx.getTitle());
				setAttributeWithOneInnerReplacement(elem, Messages.getString("UtilitySuperClassToGraph.101"), i, //$NON-NLS-1$
						String.valueOf(xx.getYear()));
				int j = 1;
				for (String a : xx.getAuthor()) {
					setAttributeWithTwoInnerReplacements(elem, Messages.getString("UtilitySuperClassToGraph.102"), i, j, //$NON-NLS-1$
							a);
					j++;
				}
				j = 1;
				for (String s : xx.getSource()) {
					setAttributeWithTwoInnerReplacements(elem, Messages.getString("UtilitySuperClassToGraph.103"), i, j, //$NON-NLS-1$
							s);
					j++;
				}
				
				j = 1;
				for (String u : xx.getUrl()) {
					setAttributeWithTwoInnerReplacements(elem, Messages.getString("UtilitySuperClassToGraph.104"), i, j, //$NON-NLS-1$
							u);
					j++;
				}
			}
			i++;
		}
	}
	
	public UtilitySuperClassToGraph() {
		super();
	}
	
}