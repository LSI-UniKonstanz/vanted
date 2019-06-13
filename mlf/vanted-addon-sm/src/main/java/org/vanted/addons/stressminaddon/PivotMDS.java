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

public class PivotMDS implements InitialPlacer {

    static final double EPSILON = 0.0000000001;
    /** A {@link Random} used to get random positions and vectors. */
    private static final Random RAND = new Random();

    /**
     * executes PivotMDS
     *
     * @param nodes list of nodes, the algorithm should get executed on
     * @param distances NodeValueMatrix storing all distances for the nodes
     * @return list containing the new positions
     *
     * @author theo, jannik
     */
    @Override
    public List<Vector2d> calculateInitialPositions(final List<Node> nodes, final NodeValueMatrix distances) {
        assert nodes.size() == distances.getDimension();

        final int numPivots = Math.min(100, distances.getDimension()); // TODO make configurable

        final int[] pivotTranslation = this.getPivots(distances, numPivots);

        //calculate the doubleCentered matrix
        RealMatrix c = doubleCenter(distances, numPivots, pivotTranslation);

        //calculate the two largest eigenVectors
        final RealMatrix eigenVecs = powerIterate( c);

        // get new XPos and YPos
        RealMatrix newX = c.multiply(eigenVecs.getColumnMatrix(0));
        RealMatrix newY = c.multiply(eigenVecs.getColumnMatrix(1));

        // create a list containing the new coordinates
        List<Vector2d> newPosList = new ArrayList<>();
        for(int i = 0; i < numPivots; i++){
            newPosList.add(new Vector2d(newX.getEntry(i, 0), newY.getEntry(i, 0)));
        }
        // TODO reapply translated nodes to insure correctness!

        return newPosList;
    }

    /**
     * Gets pivots by doing a min-max-search on the distances of the nodes.
     * This tries to maximize the distances between chosen pivot nodes.
     * The nodes will be moved to the front of the {@link NodeValueMatrix} by
     * using a translation table.
     *
     * @param distances the distances to be used.
     * @param amountPivots
     *      the amount of pivots to get. Must not be bigger than the
     *      amount of nodes, but at least one.
     *
     * @return
     *      the translation table used to move the nodes to the front of the distance
     *      matrix.
     *
     * @see NodeValueMatrix#get(int, int, int[])
     * @see NodeValueMatrix#apply(java.util.function.DoubleUnaryOperator, int, int, int[])
     *
     * @author Jannik
     */
    public int[] getPivots(final NodeValueMatrix distances, final int amountPivots) {
        final int numNodes = distances.getDimension();
        assert amountPivots <= numNodes && amountPivots > 0;

        int[] result = new int[numNodes];
        int currentPivots = 0;
        HashSet<Integer> nonPivots = new HashSet<>(numNodes);
        for (int nodeIdx = 0; nodeIdx < numNodes; nodeIdx++) { nonPivots.add(nodeIdx); } // add all nodes

        result[currentPivots++] = RAND.nextInt(result.length); // get first
        nonPivots.remove(result[0]);

        double currentMax, currentMin, distance; // holds the distances to be maximized and minimized
        int maxNode; // the node that is the next candidate to be a pivot
        for (int pivotsGot = 1; pivotsGot <= amountPivots && nonPivots.size() > 0; pivotsGot++) {
            currentMax = Double.NEGATIVE_INFINITY; // maximize this
            maxNode = -1;

            // look at every node that is not already a pivot
            for (int possiblePivot : nonPivots) {
                currentMin = Double.POSITIVE_INFINITY; // minimize this
                // get minimum distance to pivots
                for (int pivot = 0; pivot < currentPivots; pivot++) {
                    distance = distances.get(possiblePivot, result[pivot]);
                    if (distance < currentMin) { // check whether
                        currentMin = distance;
                    }
                }
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
            result[currentPivots++] = maxNode;
            nonPivots.remove(maxNode);
        }

        // copy the rest to the array
        for (Integer nonPivot : nonPivots) {
            result[currentPivots++] = nonPivot;
        }

        return result;
    }


    /**
     * Creates to random Vectors.
     *
     * @param dimension the dimension the vectors  shall have
     * @return a RealMatrixIml dimension x 2 containing the two vectors
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
     *returns the min of a,b
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
     * calculates the difference between the two arrays. For this, the values of a and b are summed,
     * and then the smaller one is divided by the larger one
     *
     * @param a first double-array
     * @param b second double-array
     * @return a double representing the difference
     *
     * @author theo
     */
    private double getdifference(final double[] a, final double[] b){
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
     * Calculating the two eigen values using powerIteration.
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

        //stopp, whenn the change is smaller 1e-10
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
                newDiff = Math.abs(getdifference(oldEigenVecs.getColumn(i), eigenVec.getColumn(i)));
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
        NodeValueMatrix squared = distances.clone().apply(x -> x*x, n, amountPivots, distanceTranslation);

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
