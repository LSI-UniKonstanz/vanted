package org.vanted.indexednodes.accumulators;

import org.vanted.indexednodes.IndexedComponent;
import org.vanted.indexednodes.IndexedEdgeList;

/**
 * 
 * @since 2.8
 * @author Benjamin Moser
 *
 */
public class IndexedEdgeListAccumulator extends StatefulAccumulator<IndexedEdgeList,
        IndexedComponent> {
    public IndexedEdgeListAccumulator(IndexedEdgeList init) {
        super(init);
    }

    public IndexedEdgeListAccumulator() {
        this(new IndexedEdgeList());
    }

    @Override
    public void accept(IndexedComponent component) {
        this.state.addAll(component.edges);
    }
}
