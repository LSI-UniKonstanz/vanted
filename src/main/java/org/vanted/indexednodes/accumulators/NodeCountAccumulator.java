package org.vanted.indexednodes.accumulators;

import org.vanted.indexednodes.IndexedComponent;

/**
 * @since 2.8
 * @author Benjamin Moser
 */
public class NodeCountAccumulator extends StatefulAccumulator<Integer, IndexedComponent> {
	public NodeCountAccumulator(Integer init) {
		super(init);
	}
	
	@Override
	public void accept(IndexedComponent component) {
		this.state += component.nodes.size();
	}
}
