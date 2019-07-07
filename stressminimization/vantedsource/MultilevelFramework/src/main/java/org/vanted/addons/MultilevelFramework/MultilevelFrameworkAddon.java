package org.vanted.addons.MultilevelFramework;

import org.graffiti.plugin.algorithm.Algorithm;
import org.vanted.addons.MultilevelFramework.BackgroundExecution.BackgroundExecutionAlgorithm;
import org.vanted.addons.MultilevelFramework.Coarsening.MatchingCoarseningAlgorithm;
import org.vanted.addons.MultilevelFramework.Coarsening.RandomNeighborCoarseningAlgorithm;
import org.vanted.addons.MultilevelFramework.Coarsening.SolarMergerCoarsening;
import org.vanted.addons.MultilevelFramework.GUI.AlgorithmListComponent;
import org.vanted.addons.MultilevelFramework.GUI.AlgorithmListParameter;
import org.vanted.addons.MultilevelFramework.MultilevelGraph.MultilevelParentGraphAttribute;
import org.vanted.addons.MultilevelFramework.MultilevelGraph.MultilevelParentNodeAttribute;
import org.vanted.addons.MultilevelFramework.Placement.RandomPlacementAlgorithm;
import org.vanted.addons.MultilevelFramework.Placement.SolarPlacement;
import org.vanted.addons.MultilevelFramework.Placement.ZeroPlacementAlgorithm;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.addons.AddonAdapter;

/**
 * AddonAdapter class for the Multilevel Framework
 */
public class MultilevelFrameworkAddon extends AddonAdapter {

	@Override
	protected void initializeAddon() {
		// no practical application for excluded algorithms except for bug fixing
		this.algorithms = new Algorithm[] {
				// new NullCoarseningAlgorithm(),
				// new RandomCoarseningAlgorithm(),
				new RandomNeighborCoarseningAlgorithm(), new RandomPlacementAlgorithm(), new ZeroPlacementAlgorithm(),
				new SolarMergerCoarsening(), new SolarPlacement(),
				new BackgroundExecutionAlgorithm(new MultilevelLayoutAlgorithm()),
				// new NullPlacementAlgorithm(),
				new MatchingCoarseningAlgorithm() };
		this.attributes = new Class[2];
		this.attributes[0] = MultilevelParentGraphAttribute.class;
		this.attributes[1] = MultilevelParentNodeAttribute.class;

		valueEditComponents.put(AlgorithmListParameter.class, AlgorithmListComponent.class);

		// We want to add force directed and stress minimization to the list of
		// available layouters
		LayoutersWhitelist.add(new ForceDirectedWrapper().getName());
		LayoutersWhitelist.add("Stress Minimization");
	}

}
