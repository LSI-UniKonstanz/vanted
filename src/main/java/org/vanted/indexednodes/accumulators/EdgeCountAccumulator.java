package org.vanted.indexednodes.accumulators;

import org.vanted.indexednodes.IndexedComponent;

/**
 * @since 2.8
 * @author Benjamin Moser
 */
public class EdgeCountAccumulator extends StatefulAccumulator<Integer, IndexedComponent> {
	public EdgeCountAccumulator(Integer init) {
		super(init);
	}
	
	@Override
	public void accept(IndexedComponent component) {
		this.state += component.edges.size();
	}
}
