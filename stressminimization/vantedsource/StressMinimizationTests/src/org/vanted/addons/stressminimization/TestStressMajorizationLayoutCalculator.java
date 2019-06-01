package org.vanted.addons.stressminimization;

import static org.junit.Assert.*;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.junit.Test;

public class TestStressMajorizationLayoutCalculator {

	private static double inf = Double.POSITIVE_INFINITY;
	
	// ===================================================
	// MARK: test construction
	// ===================================================
	
	@Test
	public void testConstructionNonSquareDistanceMatrix() {
		
		RealMatrix layout = new Array2DRowRealMatrix(new double[][] { 
			{0, 0}, 
			{0, 1}, 
		});
		RealMatrix distances = new Array2DRowRealMatrix(new double[][] { 
			{0, 1},  
		});
		RealMatrix weights = new Array2DRowRealMatrix(new double[][] {
			{inf, 1}, 
			{1, inf}, 
		});
		
		try {
			StressMajorizationLayoutCalculator layoutCalculator = new StressMajorizationLayoutCalculator(layout, distances, weights);
			fail();
		} catch (Exception ex) {
			assert(ex instanceof IllegalArgumentException);
		}
		
	}

	@Test
	public void testConstructionNonSquareWeightMatrix() {
		
		RealMatrix layout = new Array2DRowRealMatrix(new double[][] { 
			{0, 0}, 
			{0, 1}, 
		});
		RealMatrix distances = new Array2DRowRealMatrix(new double[][] { 
			{0, 1}, 
			{1, 0}
		});
		RealMatrix weights = new Array2DRowRealMatrix(new double[][] {
			{inf, 1}, 
			{1, inf}, 
			{1, inf}, 
		});
		
		try {
			StressMajorizationLayoutCalculator layoutCalculator = new StressMajorizationLayoutCalculator(layout, distances, weights);
			fail();
		} catch (Exception ex) {
			assert(ex instanceof IllegalArgumentException);
		}
		
	}
	
	@Test
	public void testConstructionMissmatchingDistanceAndWeightMatrix() {
		
		RealMatrix layout = new Array2DRowRealMatrix(new double[][] { 
			{0, 0}, 
			{0, 1}, 
		});
		RealMatrix distances = new Array2DRowRealMatrix(new double[][] { 
			{0, 1}, 
			{1, 0}
		});
		RealMatrix weights = new Array2DRowRealMatrix(new double[][] {
			{inf, 1, 1}, 
			{1, inf, 1}, 
			{1, 1, inf}, 
		});
		
		try {
			StressMajorizationLayoutCalculator layoutCalculator = new StressMajorizationLayoutCalculator(layout, distances, weights);
			fail();
		} catch (Exception ex) {
			assert(ex instanceof IllegalArgumentException);
		}
		
	}

	@Test
	public void testConstructionMissmatchingLayoutAndWeightMatrix() {
		
		RealMatrix layout = new Array2DRowRealMatrix(new double[][] { 
			{0, 0}, 
			{0, 1}, 
			{0, 1}, 
		});
		RealMatrix distances = new Array2DRowRealMatrix(new double[][] { 
			{0, 1}, 
			{1, 0}
		});
		RealMatrix weights = new Array2DRowRealMatrix(new double[][] {
			{inf, 1}, 
			{1, inf},  
		});
		
		try {
			StressMajorizationLayoutCalculator layoutCalculator = new StressMajorizationLayoutCalculator(layout, distances, weights);
			fail();
		} catch (Exception ex) {
			assert(ex instanceof IllegalArgumentException);
		}
		
	}

	@Test
	public void testConstructionMissmatchingLayoutAndDistanceMatrix() {
		
		RealMatrix layout = new Array2DRowRealMatrix(new double[][] { 
			{0, 0}, 
			{0, 1}, 
		});
		RealMatrix distances = new Array2DRowRealMatrix(new double[][] { 
			{0, 1, 1}, 
			{1, 0, 1},
			{1, 1, 0},  
		});
		RealMatrix weights = new Array2DRowRealMatrix(new double[][] {
			{inf, 1}, 
			{1, inf},  
		});
		
		try {
			StressMajorizationLayoutCalculator layoutCalculator = new StressMajorizationLayoutCalculator(layout, distances, weights);
			fail();
		} catch (Exception ex) {
			assert(ex instanceof IllegalArgumentException);
		}
		
	}

	// ===================================================
	// MARK: test stress calculation
	// ===================================================
	
	@Test
	public void testStressCalculationOptimalLayout1() {
		
		RealMatrix layout = new Array2DRowRealMatrix(new double[][] { 
			{0, 0}, 
			{0, 1}, 
		});
		RealMatrix distances = new Array2DRowRealMatrix(new double[][] { 
			{0, 1}, 
			{1, 0}, 
		});
		RealMatrix weights = new Array2DRowRealMatrix(new double[][] {
			{inf, 1}, 
			{1, inf}, 
		});
		
		StressMajorizationLayoutCalculator layoutCalculator = new StressMajorizationLayoutCalculator(layout, distances, weights);
		assert(layoutCalculator.calcStress(layout) == 0);
		assertArrayEquals(flat(layout.getData()), flat(layoutCalculator.calcOptimizedLayout().getData()), 1e-8);
		
	}
	
	@Test
	public void testStressCalculationOptimalLayout2() {
		
		// equilateral triangle
		RealMatrix layout = new Array2DRowRealMatrix(new double[][] { 
			{0, 0}, 
			{1, 0},
			{0.5, 0.8660254038}, 
		});
		RealMatrix distances = new Array2DRowRealMatrix(new double[][] { 
			{0, 1, 1}, 
			{1, 0, 1}, 
			{1, 1, 0}, 
		});
		RealMatrix weights = new Array2DRowRealMatrix(new double[][] {
			{inf, 1, 1}, 
			{1, inf, 1}, 
			{1, 1, inf}
		});
		
		StressMajorizationLayoutCalculator layoutCalculator = new StressMajorizationLayoutCalculator(layout, distances, weights);
		assertEquals(0.0, layoutCalculator.calcStress(layout), 1e-8);
		assertArrayEquals(flat(layout.getData()), flat(layoutCalculator.calcOptimizedLayout().getData()), 1e-8);
		
	}

	@Test
	public void testStressCalculationOptimalLayout3() {
		
		// line
		RealMatrix layout = new Array2DRowRealMatrix(new double[][] { 
			{0, 0}, 
			{0, 1},
			{0, 2}, 
			{0, 3}, 
			{0, 4}, 
		});
		RealMatrix distances = new Array2DRowRealMatrix(new double[][] { 
			{0, 1, 2, 3, 4}, 
			{1, 0, 1, 2, 3}, 
			{2, 1, 0, 1, 2}, 
			{3, 2, 1, 0, 1}, 
			{4, 3, 2, 1, 0}, 
		});
		RealMatrix weights = new Array2DRowRealMatrix(new double[][] {
			{inf, 1, 0.25, 1/9, 0.0625}, 
			{1, inf, 1, 0.25, 1/9}, 
			{0.25, 1, inf, 1, 0.25}, 
			{1/9, 0.25, 1, inf, 1}, 
			{0.0625, 1/9, 0.25, 1, inf}, 
		});
		
		StressMajorizationLayoutCalculator layoutCalculator = new StressMajorizationLayoutCalculator(layout, distances, weights);
		assertEquals(0.0, layoutCalculator.calcStress(layout), 1e-8);
		assertArrayEquals(flat(layout.getData()), flat(layoutCalculator.calcOptimizedLayout().getData()), 1e-8);
		
	}
	
	@Test
	public void testStressCalculationSingleNode() {
		
		RealMatrix layout = new Array2DRowRealMatrix(new double[][] { 
			{10, 7}, 
		});
		RealMatrix distances = new Array2DRowRealMatrix(new double[][] { 
			{0}, 
		});
		RealMatrix weights = new Array2DRowRealMatrix(new double[][] {
			{inf},
		});
		
		StressMajorizationLayoutCalculator layoutCalculator = new StressMajorizationLayoutCalculator(layout, distances, weights);
		assertEquals(0.0, layoutCalculator.calcStress(layout), 1e-8);
		
	}
	
	@Test
	public void testStressCalculationScaledLine() {
		
		RealMatrix layout = new Array2DRowRealMatrix(new double[][] { 
			{0, 0}, 
			{2, 0}, 
		});
		RealMatrix distances = new Array2DRowRealMatrix(new double[][] { 
			{0, 1}, 
			{1, 0}, 
		});
		RealMatrix weights = new Array2DRowRealMatrix(new double[][] {
			{inf, 1}, 
			{1, inf},
		});
		
		StressMajorizationLayoutCalculator layoutCalculator = new StressMajorizationLayoutCalculator(layout, distances, weights);
		assertEquals(1.0, layoutCalculator.calcStress(layout), 1e-8);
		
	}
	
	@Test
	public void testStressCalculationTriangle() {
		
		RealMatrix layout = new Array2DRowRealMatrix(new double[][] { 
			{0, 0}, 
			{1, 0}, 
			{0, 1}, 
		});
		RealMatrix distances = new Array2DRowRealMatrix(new double[][] { 
			{0, 1, 1}, 
			{1, 0, 1}, 
			{1, 1, 0}, 
		});
		RealMatrix weights = new Array2DRowRealMatrix(new double[][] {
			{inf, 1, 1}, 
			{1, inf, 1},
			{1, 1, inf}
		});
		
		StressMajorizationLayoutCalculator layoutCalculator = new StressMajorizationLayoutCalculator(layout, distances, weights);
		assertEquals(0.1715728753, layoutCalculator.calcStress(layout), 1e-8);
		
	}

	@Test
	public void testStressCalculationSquare1() {
		
		RealMatrix layout = new Array2DRowRealMatrix(new double[][] { 
			{0, 0}, 
			{1, 0}, 
			{0, 1},
			{1, 1},  
		});
		RealMatrix distances = new Array2DRowRealMatrix(new double[][] { 
			{0, 1, 1, 2}, 
			{1, 0, 2, 1}, 
			{1, 2, 0, 1}, 
			{2, 1, 1, 0}, 
		});
		RealMatrix weights = new Array2DRowRealMatrix(new double[][] {
			{inf, 1, 1, 0.25}, 
			{1, inf, 0.25, 1},
			{1, 0.25, inf, 1},
			{0.25, 1, 1, inf},
		});
		
		StressMajorizationLayoutCalculator layoutCalculator = new StressMajorizationLayoutCalculator(layout, distances, weights);
		assertEquals(0.1715728753, layoutCalculator.calcStress(layout), 1e-8);
		
	}
	
	@Test
	public void testStressCalculationSquare2() {
		
		RealMatrix layout = new Array2DRowRealMatrix(new double[][] { 
			{0, 0}, 
			{1, 0}, 
			{0, 1},
			{1, 1},  
		});
		RealMatrix distances = new Array2DRowRealMatrix(new double[][] { 
			{0, 1, 1, 1}, 
			{1, 0, 1, 1}, 
			{1, 1, 0, 1}, 
			{1, 1, 1, 0}, 
		});
		RealMatrix weights = new Array2DRowRealMatrix(new double[][] {
			{inf, 1, 1, 1}, 
			{1, inf, 1, 1},
			{1, 1, inf, 1},
			{1, 1, 1, inf},
		});
		
		StressMajorizationLayoutCalculator layoutCalculator = new StressMajorizationLayoutCalculator(layout, distances, weights);
		assertEquals(0.3431457505, layoutCalculator.calcStress(layout), 1e-8);
		
	}
	
	@Test
	/**
	 * Paper Graph Drawing by Stress Majorization fixes
	 * the the first nodes position at 0,0
	 */
	public void testFixesFirstNodeAtZero() {
		
		RealMatrix layout = new Array2DRowRealMatrix(new double[][] { 
			{10, 7}, 
		});
		RealMatrix distances = new Array2DRowRealMatrix(new double[][] { 
			{0}, 
		});
		RealMatrix weights = new Array2DRowRealMatrix(new double[][] {
			{inf},
		});
		
		StressMajorizationLayoutCalculator layoutCalculator = new StressMajorizationLayoutCalculator(layout, distances, weights);
		assertArrayEquals(new double[] {0, 0}, flat(layoutCalculator.calcOptimizedLayout().getData()), 1e-8);
		
	}
	
	// ===================================================
	// MARK: util
	// ===================================================
	
	private static double[] flat(double[][] in) {
		
		if (in.length == 0) {
			return new double[0];
		}
		
		double[] out = new double[in.length * in[0].length];
		for (int i = 0; i < in.length; i += 1) {
			for (int j = 0; j < in[0].length; j += 1) {
				out[i * in[0].length + j] = in[i][j];
			}
		}
		return out;
	}
	
}
