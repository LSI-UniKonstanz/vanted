package org.vanted.addons.stressminimization.parameters;

import java.util.Dictionary;

public class SliderOptions {
		//Range of the Slider, default value and if that slider should have infinity at one end or not
		private int min;
		private int max;
		private double def;
		private boolean pos;
		private boolean neg;
		
		//Labels for the slider
		Dictionary dict;
		
		

		//===========================
		// Constructors
		//===========================
		
		public SliderOptions(int min, int max, double def) {
			this.min = min;
			this.max = max;
			this.def = def;
			pos=false;
			neg=false;
			dict = null;
		}
		
		public SliderOptions(int min, int max, double def, boolean pos, boolean neg) {
			this.min = min;
			this.max = max;
			this.def = def;
			this.pos = pos;
			this.neg = neg;
			dict = null;
		}
		
		public SliderOptions(int min, int max, double def, boolean pos, boolean neg, Dictionary dict) {
			this.min = min;
			this.max = max;
			this.def = def;
			this.pos = pos;
			this.neg = neg;
			this.dict = dict;
		}

		public int getMin() {
			return min;
		}

		public int getMax() {
			return max;
		}
		
		public double getDef() {
			return def;
		}

		public boolean isPos() {
			return pos;
		}

		public boolean isNeg() {
			return neg;
		}
		
		public Dictionary getDict() {
			return dict;
		}
}
