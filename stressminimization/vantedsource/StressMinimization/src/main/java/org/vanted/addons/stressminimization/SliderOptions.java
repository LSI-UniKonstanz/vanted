package org.vanted.addons.stressminimization;

public class SliderOptions {
		//Range of the Slider and if that slider should have infinity at one end or not
		private int min;
		private int max;
		private int def;
		private boolean pos;
		private boolean neg;
		
		public SliderOptions(int min, int max, int def) {
			this.min = min;
			this.max = max;
			this.def = def;
			pos=false;
			neg=false;
		}
		
		public SliderOptions(int min, int max, int def, boolean pos, boolean neg) {
			this.min = min;
			this.max = max;
			this.def = def;
			this.pos = pos;
			this.neg = neg;
		}

		public int getMin() {
			return min;
		}

		public int getMax() {
			return max;
		}
		
		public int getDef() {
			return def;
		}

		public boolean isPos() {
			return pos;
		}

		public boolean isNeg() {
			return neg;
		}
}
