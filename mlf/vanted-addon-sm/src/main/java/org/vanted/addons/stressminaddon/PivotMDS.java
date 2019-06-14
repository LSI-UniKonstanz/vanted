package org.vanted.addons.stressminaddon;

import org.Vector2d;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealMatrixImpl;
import org.graffiti.graph.Node;
import org.vanted.addons.stressminaddon.util.NodeValueMatrix;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

/**
 * A class that implements the Pivot Multidimensional Scaling algorithm
 * as an initial layout for some nodes.
 */
public class PivotMDS implements InitialPlacer {

    /** When to stop the power iteration. */
    private static final double EPSILON = 0.0000000001;
    /** A {@link Random} used to get random positions and vectors. */
    private static final Random RAND = new Random();

    /**
     * Executes the PivotMDS algorithm on a given set of nodes.
     *
     * @param nodes
     *      the nodes which will be layouted. The indices of the nodes in this list correspond to
     *      the indices used by {@code distances}.
     * @param distances
     *      the matrix containing the node theoretical distances between the nodes.
     *      The implementing class shall only read the values in this matrix.
     *
     * @return the new positions the nodes should be moved to.
     *
     * @author theo, Jannik
     */
    @Override
    public List<Vector2d> calculateInitialPositions(final List<Node> nodes, final NodeValueMatrix distances) {
        assert nodes.size() == distances.getDimension();

        final int numPivots = Math.min(100, distances.getDimension()); // TODO make configurable

        final int[] pivotTranslation = new int[distances.getDimension()];
        final int[] inversePivotTranslation = new int[distances.getDimension()];

        this.getPivots(distances, numPivots, pivotTranslation, inversePivotTranslation);

        //calculate the doubleCentered matrix
        RealMatrix c = doubleCenter(distances, numPivots, pivotTranslation);

        //calculate the two largest eigenVectors
        final RealMatrix eigenVecs = powerIterate(c);

        // get new XPos and YPos
        RealMatrix newX = c.multiply(eigenVecs.getColumnMatrix(0));
        RealMatrix newY = c.multiply(eigenVecs.getColumnMatrix(1));

        // create a list containing the new coordinates
        List<Vector2d> newPosList = new ArrayList<>();
        for(int i = 0; i < numPivots; i++){
            newPosList.add(new Vector2d(newX.getEntry(inversePivotTranslation[i], 0),
                                        newY.getEntry(inversePivotTranslation[i], 0)));
        }
        return newPosList;
    }

    /**
     * Gets pivots by doing a min-max-search on the distances of the nodes.
     * This tries to maximize the distances between chosen pivot nodes.
     * The nodes will be moved to the front of the {@link NodeValueMatrix} by
     * using a translation table (which must be provided as the return parameter
     * {@code table}). An inverse table {@code inverseTable} will also be generated.
     *
     * @param distances the distances to be used.
     * @param amountPivots
     *      the amount of pivots to get. Must not be bigger than the
     *      amount of nodes, but at least one.
     * @param table
     *      the array representing the table to save to.
     *      This array must have the same length as the dimension of the {@code distances}!<br>
     *      Each (node) index contains the actual position of this node in the {@code distances}
     *      matrix.<br>
     *      {@code inverseTable[table[i]] = i} always holds.<br>
     *      <br>
     *      This return parameter is used to be consistent because this method has two return values.
     *
     * @param inverseTable
     *      the array representing the inverse of the {@code table} table to save to.
     *      This array must have the same length as the dimension of the {@code distances}!<br>
     *      Each actual (node) index contains the translated position of this node in the {@code distances}
     *      matrix.<br>
     *      {@code inverseTable[table[i]] = i} always holds.<br>
     *      <br>
     *      This return parameter is used to be consistent because this method has two return values.
     *
     *
     * @return the parameter {@code table}.
     *
     * @see NodeValueMatrix#get(int, int, int[])
     * @see NodeValueMatrix#apply(java.util.function.DoubleUnaryOperator, int, int, int[])
     *
     * @author Jannik
     */
    int[] getPivots(final NodeValueMatrix distances, final int amountPivots,
                           int[] table, int[] inverseTable) {
        final int numNodes = distances.getDimension();
        assert amountPivots <= numNodes && amountPivots > 0
                && table.length == distances.getDimension() && inverseTable.length == distances.getDimension();

        int currentPivots = 0;
        HashSet<Integer> nonPivots = new HashSet<>(numNodes);
        double[] nonPivotsMinDistance = new double[numNodes];

        int lastPivot = RAND.nextInt(table.length);
        table[currentPivots++] = lastPivot; // get first pivot
        inverseTable[lastPivot] = 0;
        // fill non pivots and set first distance
        for (int nodeIdx = 0; nodeIdx < numNodes; nodeIdx++) {
            nonPivots.add(nodeIdx);
            nonPivotsMinDistance[nodeIdx] = Double.POSITIVE_INFINITY;
        } // add all nodes
        nonPivots.remove(lastPivot);

        double currentMax, currentMin, distance; // holds the distances to be maximized and minimized
        int maxNode; // the node that is the next candidate to be a pivot
        for (int pivotsGot = 1; pivotsGot <= amountPivots && nonPivots.size() > 0; pivotsGot++) {
            currentMax = Double.NEGATIVE_INFINITY; // maximize this
            maxNode = -1;

            // look at every node that is not already a pivot
            for (int possiblePivot : nonPivots) {
                distance = distances.get(possiblePivot, table[lastPivot]); // only the last pivot could change this value
                currentMin = nonPivotsMinDistance[possiblePivot];
                nonPivotsMinDistance[possiblePivot] = currentMin = // so update the value and minimize it
                        (distance < currentMin ? distance : currentMin);

                // only update if a connection was found
                if (currentMin != Double.POSITIVE_INFINITY && currentMin > currentMax) {
                    currentMax = currentMin;
                    maxNode = possiblePivot;
                }
            }

            // add new pivot
            if (currentMax == Double.NEGATIVE_INFINITY) { // no value found: Use last node looked at as good approximation
                maxNode = nonPivots.iterator().next();
            }
            table[currentPivots]  = maxNode;
            inverseTable[maxNode] = currentPivots++;
            nonPivots.remove(maxNode);
        }

        // copy the rest to the array
        for (Integer nonPivot : nonPivots) {
            table[currentPivots] = nonPivot;
            inverseTable[nonPivot] = currentPivots++;
        }

        return table;
    }


    /**
     * Creates two random Vectors.
     *
     * @param dimension the dimension the vectors shall have.
     * @return a {@code dimension x 2} RealMatrixIml containing the two vectors
     *
     * @author theo
     */
    private RealMatrixImpl getRandomVectors(final int dimension) {

        Random r = PivotMDS.RAND;

        RealMatrixImpl result = new RealMatrixImpl(dimension, 2);
        double[][] resultArray = result.getDataRef();

        for(int row =0; row<dimension; row++){
            resultArray[row][0] = r.nextInt();
            resultArray[row][1] = r.nextInt();
        }
        return result;
    }


    /**
     * Returns the min of a, b.
     *
     * @param a value 1
     * @param b value 2
     * @return b if b > a else a
     *
     * @author theo
     */
    private double getMin(final double a, final double b ){
        return ((a < b) ? a : b);
    }


    /**
     * Calculates the difference between the two arrays. For this, the values of a and b are summed,
     * and then the smaller one is divided by the larger one
     *
     * @param a first double-array
     * @param b second double-array
     * @return a double representing the difference
     *
     * @author theo
     */
    private double getDifference(final double[] a, final double[] b){
        double sumA = 0;
        double sumB = 0;
        for (int i = 0; i < a.length; i++) {
            sumA += a[i];
            sumB += b[i];
        }

        if(sumA > sumB){
            return sumB/sumA;
        }
        return sumA/sumB;
    }


    /**
     * Calculate the two eigen values using powerIteration.
     *
     * @param matrix containing the doubleCentred distance values.
     * @return a doubleArray containing the two eigen values.
     *
     * @author theo
     */
    public RealMatrix powerIterate(final RealMatrix matrix) {

        //C^T * C
        RealMatrixImpl c = (RealMatrixImpl) matrix.transpose().multiply(matrix);
        final int dimension = c.getRowDimension();

        //create 2  random vectors
        RealMatrixImpl eigenVec = getRandomVectors(dimension);

        double change = 1;

        //stop, when the change is smaller 1e-10
        while(change > EPSILON){

            // remember old values
            RealMatrixImpl oldEigenVecs = (RealMatrixImpl) eigenVec.copy();

            //c * eigenVec
            eigenVec = (RealMatrixImpl) c.multiply(eigenVec);

            // calculate new eigenVec
            eigenVec = (RealMatrixImpl) eigenVec.scalarMultiply(1/eigenVec.getNorm());

            //calculate the difference between the oldEigenVecs and the new eigenVecs
            double newDiff;
            for(int i = 0; i < 2; i++){
                newDiff = Math.abs(getDifference(oldEigenVecs.getColumn(i), eigenVec.getColumn(i)));
                change = getMin(change, newDiff);
            }
        }
        return eigenVec;
    }

    /**
     * Calculates the double centered Matrix C.
     *
     * @param distances NodeValueMatrix containing all shortest distances
     * @param amountPivots the amount of pivot elements we want to use
     * @param distanceTranslation
     *      the translation that shall be used that the pivots are in the first
     *      {@code amountPivots} positions of the matrix.
     *
     * @return  {@code n x amountPivots} RealMatrix containing the calculated values
     *
     * @author theo, Jannik
     *
     */
    public RealMatrix doubleCenter(final NodeValueMatrix distances, final int amountPivots,
                                    final int[] distanceTranslation) {

        final int n = distances.getDimension();

        RealMatrixImpl c = new RealMatrixImpl(n, amountPivots);
        NodeValueMatrix squared = distances.clone().apply(x -> x*x, n-1, amountPivots-1, distanceTranslation);

        double  [][] results = c.getDataRef();

        // Sum three is independent of the current position so it can be calculated only once
        double sumThree = 0;
        for (int r = 0; r < n; r++) {
            for (int s = 0; s < amountPivots; s++) {
                sumThree += squared.get(r, s, distanceTranslation);
            }
        }
        sumThree *= (1.0/(n*amountPivots));

        for(int row = 0; row < n; row++){
            for (int col= 0; col < amountPivots; col++){

                int sumOne = 0;
                int sumTwo = 0;

                //calculates sumOne
                for(int r = 0; r < n; r++){
                     sumOne += squared.get(r, col, distanceTranslation);
                }
                //calculate sumTwo
                for(int k = 0; k < amountPivots; k++){
                    sumTwo += squared.get(row, k, distanceTranslation);
                }


                results[row][col] = -0.5 *(squared.get(row, col, distanceTranslation)
                        -(1.0/n)*sumOne - (1.0/amountPivots)* sumTwo + sumThree);
            }
        }
        return c;
    }
}
