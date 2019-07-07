package org.vanted.addons.MultilevelFramework.Placement;

/**
 * The null placer does not move nodes at all, therefore suited for testing
 * coarsening algorithms
 */
public class NullPlacementAlgorithm extends AbstractPlacementAlgorithm {

	@Override
	public String getName() {
		return "Null Placer";
	}

	@Override
	public String getDescription() {
		return "Doesn't move nodes at all. The most relaxed of all placers.";
	}

	@Override
	public void execute() {
	}

}
