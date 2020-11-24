package org.vanted.indexednodes.accumulators;

import org.vanted.indexednodes.IndexedComponent;
import org.vanted.indexednodes.IndexedNodeSet;

/**
 * 
 * @since 2.8
 * @author Benjamin Moser
 *
 */
public class IndexedNodeSetAccumulator extends StatefulAccumulator<IndexedNodeSet,
        IndexedComponent> {
    public IndexedNodeSetAccumulator(IndexedNodeSet init) {
        super(init);
    }

    @Override
    public void accept(IndexedComponent component) {
        this.state.union(component.nodes);
    }
}
