package org.vanted.indexednodes.accumulators;

import java.util.function.Consumer;

/**
 * An object that holds an accumulating value of type T and is able to accept
 * parameters of type O and (potentially) aggregate them into a value of type T.
 * @param <T>
 * @param <O>
 * 
 * @since 2.8
 * @author Benjamin Moser
 * 
 */
public abstract class StatefulAccumulator<T,O> implements Consumer<O> {

    protected T state;
    StatefulAccumulator<?, O> next;

    public StatefulAccumulator(T init) {
        this.state = init;
    }

    /**
     * Interface method to apply a value to the accumulator, additionally calling
     * the next accumulator if set.
     * @param value
     */
    public void apply(O value) {
        // call the method specified by the consumer interface
        // it will be implemented by implementing child classes
        this.accept(value);
        if (next != null) {
            next.apply(value);
        }
    }

    /**
     * Compose with another accumulator
     * @param next
     * @return
     */
    public StatefulAccumulator<T, O> then(StatefulAccumulator<?,O> next) {
        this.next = next;
        return this;
    }

    public T get() {
        return this.state;
    }

}
