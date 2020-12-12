package org.vanted.plugins.layout.stressminimization;

import static org.junit.Assert.*;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.junit.Test;

public class TestStressMajorizationLayoutCalculator {

	private static final double inf = Double.POSITIVE_INFINITY;

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

	@Test
	public void testConstructionNonSquareDistanceMatrix() {

		RealMatrix layout = new Array2DRowRealMatrix(new double[][]{
				{0, 0},
				{0, 1},
		});
		RealMatrix distances = new Array2DRowRealMatrix(new double[][]{
				{0, 1},
		});
		RealMatrix weights = new Array2DRowRealMatrix(new double[][]{
				{inf, 1},
				{1, inf},
		});

		try {
			new StressMajorizationLayoutCalculator(layout, distances, weights);
			fail();
		} catch (Exception ex) {
			assert (ex instanceof IllegalArgumentException);
		}

	}

	@Test
	public void testConstructionNonSquareWeightMatrix() {

		RealMatrix layout = new Array2DRowRealMatrix(new double[][]{
				{0, 0},
				{0, 1},
		});
		RealMatrix distances = new Array2DRowRealMatrix(new double[][]{
				{0, 1},
				{1, 0}
		});
		RealMatrix weights = new Array2DRowRealMatrix(new double[][]{
				{inf, 1},
				{1, inf},
				{1, inf},
		});

		try {
			new StressMajorizationLayoutCalculator(layout, distances, weights);
			fail();
		} catch (Exception ex) {
			assert (ex instanceof IllegalArgumentException);
		}

	}

	@Test
	public void testConstructionMissmatchingDistanceAndWeightMatrix() {

		RealMatrix layout = new Array2DRowRealMatrix(new double[][]{
				{0, 0},
				{0, 1},
		});
		RealMatrix distances = new Array2DRowRealMatrix(new double[][]{
				{0, 1},
				{1, 0}
		});
		RealMatrix weights = new Array2DRowRealMatrix(new double[][]{
				{inf, 1, 1},
				{1, inf, 1},
				{1, 1, inf},
		});

		try {
			new StressMajorizationLayoutCalculator(layout, distances, weights);
			fail();
		} catch (Exception ex) {
			assert (ex instanceof IllegalArgumentException);
		}

	}

	@Test
	public void testConstructionMissmatchingLayoutAndWeightMatrix() {

		RealMatrix layout = new Array2DRowRealMatrix(new double[][]{
				{0, 0},
				{0, 1},
				{0, 1},
		});
		RealMatrix distances = new Array2DRowRealMatrix(new double[][]{
				{0, 1},
				{1, 0}
		});
		RealMatrix weights = new Array2DRowRealMatrix(new double[][]{
				{inf, 1},
				{1, inf},
		});

		try {
			new StressMajorizationLayoutCalculator(layout, distances, weights);
			fail();
		} catch (Exception ex) {
			assert (ex instanceof IllegalArgumentException);
		}

	}

	@Test
	public void testConstructionMissmatchingLayoutAndDistanceMatrix() {

		RealMatrix layout = new Array2DRowRealMatrix(new double[][]{
				{0, 0},
				{0, 1},
		});
		RealMatrix distances = new Array2DRowRealMatrix(new double[][]{
				{0, 1, 1},
				{1, 0, 1},
				{1, 1, 0},
		});
		RealMatrix weights = new Array2DRowRealMatrix(new double[][]{
				{inf, 1},
				{1, inf},
		});

		try {
			new StressMajorizationLayoutCalculator(layout, distances, weights);
			fail();
		} catch (Exception ex) {
			assert (ex instanceof IllegalArgumentException);
		}

	}

	@Test
	public void testStressCalculationOptimalLayout1() {

		RealMatrix layout = new Array2DRowRealMatrix(new double[][]{
				{0, 0},
				{0, 1},
		});
		RealMatrix distances = new Array2DRowRealMatrix(new double[][]{
				{0, 1},
				{1, 0},
		});
		RealMatrix weights = new Array2DRowRealMatrix(new double[][]{
				{inf, 1},
				{1, inf},
		});

		StressMajorizationLayoutCalculator layoutCalculator = new StressMajorizationLayoutCalculator(layout, distances, weights);
		assert (layoutCalculator.calcStress() == 0);
		assertArrayEquals(flat(layout.getData()), flat(layoutCalculator.calcOptimizedLayout().getData()), 1e-8);

	}

	@Test
	public void testStressCalculationOptimalLayout2() {

		// equilateral triangle
		RealMatrix layout = new Array2DRowRealMatrix(new double[][]{
				{0, 0},
				{1, 0},
				{0.5, 0.8660254038},
		});
		RealMatrix distances = new Array2DRowRealMatrix(new double[][]{
				{0, 1, 1},
				{1, 0, 1},
				{1, 1, 0},
		});
		RealMatrix weights = new Array2DRowRealMatrix(new double[][]{
				{inf, 1, 1},
				{1, inf, 1},
				{1, 1, inf}
		});

		StressMajorizationLayoutCalculator layoutCalculator = new StressMajorizationLayoutCalculator(layout, distances, weights);
		assertEquals(0.0, layoutCalculator.calcStress(), 1e-8);
		assertArrayEquals(flat(layout.getData()), flat(layoutCalculator.calcOptimizedLayout().getData()), 1e-8);

	}

	@Test
	public void testStressCalculationOptimalLayout3() {

		// line
		RealMatrix layout = new Array2DRowRealMatrix(new double[][]{
				{0, 0},
				{0, 1},
				{0, 2},
				{0, 3},
				{0, 4},
		});
		RealMatrix distances = new Array2DRowRealMatrix(new double[][]{
				{0, 1, 2, 3, 4},
				{1, 0, 1, 2, 3},
				{2, 1, 0, 1, 2},
				{3, 2, 1, 0, 1},
				{4, 3, 2, 1, 0},
		});
		RealMatrix weights = new Array2DRowRealMatrix(new double[][]{
				{inf, 1, 0.25, 1 / 9, 0.0625},
				{1, inf, 1, 0.25, 1 / 9},
				{0.25, 1, inf, 1, 0.25},
				{1 / 9, 0.25, 1, inf, 1},
				{0.0625, 1 / 9, 0.25, 1, inf},
		});

		StressMajorizationLayoutCalculator layoutCalculator = new StressMajorizationLayoutCalculator(layout, distances, weights);
		assertEquals(0.0, layoutCalculator.calcStress(), 1e-8);
		assertArrayEquals(flat(layout.getData()), flat(layoutCalculator.calcOptimizedLayout().getData()), 1e-8);

	}

	@Test
	public void testStressCalculationSingleNode() {

		RealMatrix layout = new Array2DRowRealMatrix(new double[][]{
				{10, 7},
		});
		RealMatrix distances = new Array2DRowRealMatrix(new double[][]{
				{0},
		});
		RealMatrix weights = new Array2DRowRealMatrix(new double[][]{
				{inf},
		});

		StressMajorizationLayoutCalculator layoutCalculator = new StressMajorizationLayoutCalculator(layout, distances, weights);
		assertEquals(0.0, layoutCalculator.calcStress(), 1e-8);

	}

	@Test
	public void testStressCalculationScaledLine() {

		RealMatrix layout = new Array2DRowRealMatrix(new double[][]{
				{0, 0},
				{2, 0},
		});
		RealMatrix distances = new Array2DRowRealMatrix(new double[][]{
				{0, 1},
				{1, 0},
		});
		RealMatrix weights = new Array2DRowRealMatrix(new double[][]{
				{inf, 1},
				{1, inf},
		});

		StressMajorizationLayoutCalculator layoutCalculator = new StressMajorizationLayoutCalculator(layout, distances, weights);
		assertEquals(1.0, layoutCalculator.calcStress(), 1e-8);

	}

	@Test
	public void testStressCalculationTriangle() {

		RealMatrix layout = new Array2DRowRealMatrix(new double[][]{
				{0, 0},
				{1, 0},
				{0, 1},
		});
		RealMatrix distances = new Array2DRowRealMatrix(new double[][]{
				{0, 1, 1},
				{1, 0, 1},
				{1, 1, 0},
		});
		RealMatrix weights = new Array2DRowRealMatrix(new double[][]{
				{inf, 1, 1},
				{1, inf, 1},
				{1, 1, inf}
		});

		StressMajorizationLayoutCalculator layoutCalculator = new StressMajorizationLayoutCalculator(layout, distances, weights);
		assertEquals(0.1715728753, layoutCalculator.calcStress(), 1e-8);

	}

	@Test
	public void testStressCalculationTranslatedTriangle() {

		RealMatrix layout = new Array2DRowRealMatrix(new double[][]{
				{-0.5, 0},
				{0.5, 0},
				{-0.5, 1},
		});
		RealMatrix distances = new Array2DRowRealMatrix(new double[][]{
				{0, 1, 1},
				{1, 0, 1},
				{1, 1, 0},
		});
		RealMatrix weights = new Array2DRowRealMatrix(new double[][]{
				{inf, 1, 1},
				{1, inf, 1},
				{1, 1, inf}
		});

		StressMajorizationLayoutCalculator layoutCalculator = new StressMajorizationLayoutCalculator(layout, distances, weights);
		assertEquals(0.1715728753, layoutCalculator.calcStress(), 1e-8);

	}

	@Test
	public void testStressCalculationSquare1() {

		RealMatrix layout = new Array2DRowRealMatrix(new double[][]{
				{0, 0},
				{1, 0},
				{0, 1},
				{1, 1},
		});
		RealMatrix distances = new Array2DRowRealMatrix(new double[][]{
				{0, 1, 1, 2},
				{1, 0, 2, 1},
				{1, 2, 0, 1},
				{2, 1, 1, 0},
		});
		RealMatrix weights = new Array2DRowRealMatrix(new double[][]{
				{inf, 1, 1, 0.25},
				{1, inf, 0.25, 1},
				{1, 0.25, inf, 1},
				{0.25, 1, 1, inf},
		});

		StressMajorizationLayoutCalculator layoutCalculator = new StressMajorizationLayoutCalculator(layout, distances, weights);
		assertEquals(0.1715728753, layoutCalculator.calcStress(), 1e-8);

	}

	@Test
	public void testStressCalculationSquare2() {

		RealMatrix layout = new Array2DRowRealMatrix(new double[][]{
				{0, 0},
				{1, 0},
				{0, 1},
				{1, 1},
		});
		RealMatrix distances = new Array2DRowRealMatrix(new double[][]{
				{0, 1, 1, 1},
				{1, 0, 1, 1},
				{1, 1, 0, 1},
				{1, 1, 1, 0},
		});
		RealMatrix weights = new Array2DRowRealMatrix(new double[][]{
				{inf, 1, 1, 1},
				{1, inf, 1, 1},
				{1, 1, inf, 1},
				{1, 1, 1, inf},
		});

		StressMajorizationLayoutCalculator layoutCalculator = new StressMajorizationLayoutCalculator(layout, distances, weights);
		assertEquals(0.3431457505, layoutCalculator.calcStress(), 1e-8);

	}

	@Test
	public void testStressDecliningAfterLayoutCalculationStarOnGrid() {

		// tests layout calculation for a star (n = 4) graph
		// that is initially placed on a grid

		RealMatrix layout = new Array2DRowRealMatrix(new double[][]{
				{0, 0},
				{1, 0},
				{0, 1},
				{1, 1},
		});
		RealMatrix distances = new Array2DRowRealMatrix(new double[][]{
				{0, 1, 1, 1},
				{1, 0, 2, 2},
				{1, 2, 0, 2},
				{1, 2, 2, 0},
		});
		RealMatrix weights = new Array2DRowRealMatrix(new double[][]{
				{inf, 1, 1, 1},
				{1, inf, 0.25, 0.25},
				{1, 0.25, inf, 0.25},
				{1, 0.25, 0.25, inf},
		});

		StressMajorizationLayoutCalculator layoutCalculator = new StressMajorizationLayoutCalculator(layout, distances, weights);

		double stress1 = layoutCalculator.calcStress();
		layoutCalculator.calcOptimizedLayout();
		double stress2 = layoutCalculator.calcStress();

		// initial layout is not optimal,
		// so stress needs to decline
		assert (stress2 < stress1);

	}

	@Test
	public void testStressDecliningAfterLayoutCalculationStarOnGrid2() {

		// tests layout calculation for a star (n = 20) graph
		// that is initially placed on a non symetric grid

		RealMatrix layout = new Array2DRowRealMatrix(new double[][]{
				{0, 0}, {0, 1}, {0, 2}, {0, 3}, {0, 4},
				{1, 0}, {1, 1}, {1, 2}, {1, 3}, {1, 4},
				{2, 0}, {2, 1}, {2, 2}, {2, 3}, {2, 4},
				{3, 0}, {3, 1}, {3, 2}, {3, 3}, {3, 4},
		});
		RealMatrix distances = new Array2DRowRealMatrix(new double[][]{
				{0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,},
				{1, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,},
				{1, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,},
				{1, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,},
				{1, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,},
				{1, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,},
				{1, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,},
				{1, 2, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,},
				{1, 2, 2, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,},
				{1, 2, 2, 2, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,},
				{1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2,},
				{1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2,},
				{1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2,},
				{1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2,},
				{1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2,},
				{1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2,},
				{1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 2, 2, 2,},
				{1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 2, 2,},
				{1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 2,},
				{1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0,},
		});
		RealMatrix weights = new Array2DRowRealMatrix(new double[][]{
				{inf, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,},
				{1, inf, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25,},
				{1, 0.25, inf, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25,},
				{1, 0.25, 0.25, inf, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25,},
				{1, 0.25, 0.25, 0.25, inf, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25,},
				{1, 0.25, 0.25, 0.25, 0.25, inf, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25,},
				{1, 0.25, 0.25, 0.25, 0.25, 0.25, inf, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25,},
				{1, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, inf, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25,},
				{1, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, inf, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25,},
				{1, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, inf, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25,},
				{1, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, inf, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25,},
				{1, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, inf, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25,},
				{1, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, inf, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25,},
				{1, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, inf, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25,},
				{1, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, inf, 0.25, 0.25, 0.25, 0.25, 0.25,},
				{1, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, inf, 0.25, 0.25, 0.25, 0.25,},
				{1, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, inf, 0.25, 0.25, 0.25,},
				{1, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, inf, 0.25, 0.25,},
				{1, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, inf, 0.25,},
				{1, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, inf,},
		});

		StressMajorizationLayoutCalculator layoutCalculator = new StressMajorizationLayoutCalculator(layout, distances, weights);

		double stress1 = layoutCalculator.calcStress();
		layoutCalculator.calcOptimizedLayout();
		double stress2 = layoutCalculator.calcStress();

		// initial layout is not optimal,
		// so stress needs to decline
		assert (stress2 < stress1);

	}

	@Test
	public void testStressDecliningAfterLayoutCalculationLineOnStar() {

		// tests layout calculation for a line (n = 4) graph
		// that is initially placed on a star graph

		RealMatrix layout = new Array2DRowRealMatrix(new double[][]{
				{0, 0},
				{1.5, 0},
				{0.77, 0.86},
				{0.77, 1.86},
		});
		RealMatrix distances = new Array2DRowRealMatrix(new double[][]{
				{0, 1, 2, 3},
				{1, 0, 1, 2},
				{2, 1, 0, 1},
				{3, 2, 1, 0},
		});
		RealMatrix weights = new Array2DRowRealMatrix(new double[][]{
				{inf, 1, 0.25, 1 / 9},
				{1, inf, 1, 0.25},
				{0.25, 1, inf, 1},
				{1 / 9, 0.25, 1, inf},
		});

		StressMajorizationLayoutCalculator layoutCalculator = new StressMajorizationLayoutCalculator(layout, distances, weights);

		double stress1 = layoutCalculator.calcStress();
		layoutCalculator.calcOptimizedLayout();
		double stress2 = layoutCalculator.calcStress();

		// initial layout is not optimal,
		// so stress needs to decline
		assert (stress2 < stress1);

	}

	@Test
	public void testStressDecliningAfterLayoutCalculationDistoredGrid() {

		// tests layout calculation for a grid (n = 6) graph
		// with initially distorted layout

		RealMatrix layout = new Array2DRowRealMatrix(new double[][]{
				{0, 0}, {0, 0.75}, {0, 2},
				{1, -0.25}, {1.25, 1.25}, {1, 1.75}
		});
		RealMatrix distances = new Array2DRowRealMatrix(new double[][]{
				{0, 1, 2, 1, 2, 3},
				{1, 0, 1, 2, 1, 2},
				{2, 1, 0, 3, 2, 1},
				{1, 2, 3, 0, 1, 2},
				{2, 1, 2, 1, 0, 1},
				{3, 2, 1, 2, 1, 0},
		});
		RealMatrix weights = new Array2DRowRealMatrix(new double[][]{
				{inf, 1, 0.25, 1, 0.25, 1 / 9},
				{1, inf, 1, 0.25, 1, 0.25},
				{0.25, 1, inf, 1 / 9, 0.25, 1},
				{1, 0.25, 1 / 9, inf, 1, 0.25},
				{0.25, 1, 0.25, 1, inf, 1},
				{1 / 9, 0.25, 1, 0.25, 1, inf},
		});

		StressMajorizationLayoutCalculator layoutCalculator = new StressMajorizationLayoutCalculator(layout, distances, weights);

		double stress1 = layoutCalculator.calcStress();
		layoutCalculator.calcOptimizedLayout();
		double stress2 = layoutCalculator.calcStress();

		// initial layout is not optimal,
		// so stress needs to decline
		assert (stress2 < stress1);

	}

	@Test
	public void testStressDecliningAfterLayoutCalculationSierpiskyTriangle() {

		// tests layout calculation for a sierpinsky triangle of order 3
		// that is initially placed like a rectangular triangle

		RealMatrix layout = new Array2DRowRealMatrix(new double[][]{
				{0, 0}, // 1
				{1, 0}, // 2
				{0, 1}, // 3
				{2, 0}, // 4
				{1, 1}, // 5
				{0, 2}, // 6
				{3, 0}, // 7
				{2, 1}, // 8
				{1, 2}, // 9
				{0, 3}, // 10
				{4, 0}, // 11
				{3, 1}, // 12
				{2, 2}, // 13
				{1, 3}, // 14
				{0, 4}, // 15
		});
		RealMatrix distances = new Array2DRowRealMatrix(new double[][]{
				{0, 1, 1, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4},
				{1, 0, 1, 1, 1, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4},
				{1, 1, 0, 2, 1, 1, 3, 3, 2, 2, 4, 4, 3, 3, 3},
				{2, 1, 2, 0, 1, 2, 1, 1, 3, 3, 2, 2, 2, 3, 4},
				{2, 1, 1, 1, 0, 1, 2, 2, 2, 2, 3, 3, 3, 3, 3},
				{2, 2, 1, 2, 1, 0, 3, 3, 1, 1, 4, 3, 2, 2, 2},
				{3, 2, 3, 1, 2, 3, 0, 1, 3, 4, 1, 1, 2, 3, 4},
				{3, 2, 3, 1, 2, 3, 1, 0, 2, 3, 2, 1, 1, 2, 3},
				{3, 3, 2, 3, 2, 1, 3, 2, 0, 1, 3, 2, 1, 1, 2},
				{3, 3, 2, 3, 2, 1, 4, 3, 1, 0, 4, 3, 2, 1, 1},
				{4, 3, 4, 2, 3, 4, 1, 2, 3, 4, 0, 1, 2, 3, 4},
				{4, 3, 4, 2, 3, 3, 1, 1, 2, 3, 1, 0, 1, 2, 3},
				{4, 3, 3, 2, 3, 2, 2, 1, 1, 2, 2, 1, 0, 1, 2},
				{4, 4, 3, 3, 3, 2, 3, 2, 1, 1, 3, 2, 1, 0, 1},
				{4, 4, 3, 4, 3, 2, 4, 3, 2, 1, 4, 3, 2, 1, 0},
		});
		RealMatrix weights = new Array2DRowRealMatrix(new double[][]{
				{inf, 1, 1, 0.25, 0.25, 0.25, 1 / 9, 1 / 9, 1 / 9, 1 / 9, 0.0625, 0.0625, 0.0625, 0.0625, 0.0625},
				{1, inf, 1, 1, 1, 0.25, 0.25, 0.25, 1 / 9, 1 / 9, 1 / 9, 1 / 9, 0.0625, 0.0625, 0.0625},
				{1, 1, inf, 0.25, 1, 1, 1 / 9, 1 / 9, 0.25, 0.25, 0.0625, 0.0625, 1 / 9, 1 / 9, 1 / 9},
				{0.25, 1, 0.25, inf, 1, 0.25, 1, 1, 1 / 9, 1 / 9, 0.25, 0.25, 0.25, 1 / 9, 0.0625},
				{0.25, 1, 1, 1, inf, 1, 0.25, 0.25, 0.25, 0.25, 1 / 9, 1 / 9, 1 / 9, 1 / 9, 1 / 9},
				{0.25, 0.25, 1, 0.25, 1, inf, 1 / 9, 1 / 9, 1, 1, 0.0625, 1 / 9, 0.25, 0.25, 0.25},
				{1 / 9, 0.25, 1 / 9, 1, 0.25, 1 / 9, inf, 1, 1 / 9, 0.0625, 1, 1, 0.25, 1 / 9, 0.0625},
				{1 / 9, 0.25, 1 / 9, 1, 0.25, 1 / 9, 1, inf, 0.25, 1 / 9, 0.25, 1, 1, 0.25, 1 / 9},
				{1 / 9, 1 / 9, 0.25, 1 / 9, 0.25, 1, 1 / 9, 0.25, inf, 1, 1 / 9, 0.25, 1, 1, 0.25},
				{1 / 9, 1 / 9, 0.25, 1 / 9, 0.25, 1, 0.0625, 1 / 9, 1, inf, 0.0625, 1 / 9, 0.25, 1, 1},
				{0.0625, 1 / 9, 0.0625, 0.25, 1 / 9, 0.0625, 1, 0.25, 1 / 9, 0.0625, inf, 1, 0.25, 1 / 9, 0.0625},
				{0.0625, 1 / 9, 0.0625, 0.25, 1 / 9, 1 / 9, 1, 1, 0.25, 1 / 9, 1, inf, 1, 0.25, 1 / 9},
				{0.0625, 1 / 9, 1 / 9, 0.25, 1 / 9, 0.25, 0.25, 1, 1, 0.25, 0.25, 1, inf, 1, 0.25},
				{0.0625, 0.0625, 1 / 9, 1 / 9, 1 / 9, 0.25, 1 / 9, 0.25, 1, 1, 1 / 9, 0.25, 1, inf, 1},
				{0.0625, 0.0625, 1 / 9, 0.0625, 1 / 9, 0.25, 0.0625, 1 / 9, 0.25, 1, 0.0625, 1 / 9, 0.25, 1, inf},
		});

		StressMajorizationLayoutCalculator layoutCalculator = new StressMajorizationLayoutCalculator(layout, distances, weights);

		double stress1 = layoutCalculator.calcStress();
		layoutCalculator.calcOptimizedLayout();
		double stress2 = layoutCalculator.calcStress();

		// initial layout is not optimal,
		// so stress needs to decline
		assert (stress2 < stress1);

	}

}
