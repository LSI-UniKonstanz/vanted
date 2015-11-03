package org.vanted.animation;
/**
 * Represents the number of times an animation is repeated.<br>
 * Primarily exists to prevent confusion about setting noLoops to infinity (-1).<br>
 * @author - Patrick Shaw
 *
 */
public enum NumberOfLoops {
	INFINITY(-1),
	ONE(1),
	TWO(2),
	THREE(3);
	int noLoops; 
   NumberOfLoops(int noLoops) { this.noLoops = noLoops; }
   public int getValue() { return noLoops; }
}

