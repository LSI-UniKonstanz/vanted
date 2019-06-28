package org.vanted.addons.stressminaddon;

import org.Vector2d;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealMatrixImpl;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.parameter.Parameter;
import org.vanted.addons.stressminaddon.util.NodeValueMatrix;

import java.util.*;

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

        if (nodes.size() == 1) {
            return Collections.singletonList(new Vector2d(0.0, 0.0));
        }

        final int numPivots = Math.min(50, distances.getDimension()); // TODO make configurable

        final int[] pivotTranslation = new int[distances.getDimension()];
        final int[] inversePivotTranslation = new int[distances.getDimension()];

        this.getPivots(distances, numPivots, pivotTranslation, inversePivotTranslation);

        //calculate the doubleCentered matrix
        RealMatrix c = doubleCenter(distances, numPivots, pivotTranslation);
        //C^T * C
        RealMatrix cTc = c.transpose().multiply(c);

        //calculate the largest eigenVector
        final RealMatrix firstEigenVec = powerIterate(cTc, getRandomVector(numPivots));

        // calculate second largest eigenVector
        final RealMatrix secondEigenVec = powerIterate(deflateMatrix(cTc, firstEigenVec, findEigenVal(cTc, firstEigenVec)), getRandomVector(numPivots));

        // get new XPos and YPos
        RealMatrix newX = c.multiply(firstEigenVec.getColumnMatrix(0));
        RealMatrix newY = c.multiply(secondEigenVec.getColumnMatrix(0));
        
        double toAdd = largerAbS(findMinInMatrix(newX), findMinInMatrix(newY));

        // create a list containing the new coordinates
        List<Vector2d> newPosList = new ArrayList<>();
        for(int i = 0; i < distances.getDimension(); i++){
            newPosList.add(new Vector2d(newX.getEntry(inversePivotTranslation[i], 0) + toAdd,
                                        newY.getEntry(inversePivotTranslation[i], 0) + toAdd));
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
                distance = distances.get(possiblePivot, lastPivot); // only the last pivot could change this value
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
     * Returns the absolute value of a and b. a and b need to be negative
     *
     * @param a value 1 (needs to be negative)
     * @param b value 2 (needs to be negative)
     * @return double containing the larger absolute value of value a and b
     * 
     * @author theo
     */
    private double largerAbS(double a , double b){
        assert (a <= 0 && b <= 0) : "Values need to be negative.";
        return (a < b) ? -a : -b;
    }

    /**
     * find the min entry in a matrix. if min> 0 return 0
     *
     * @param matrix where the min of is needed
     * @return double containing the smallest entry in Matrix
     * 
     * @author theo
     */
    private  double findMinInMatrix(RealMatrix matrix){
         double min =0;
         for(int row = 0; row<matrix.getRowDimension(); row++){
             for(int col = 0; col<matrix.getColumnDimension(); col++){

                 if(matrix.getEntry(row, col)< min){
                     min = matrix.getEntry(row,  col);
                 }
             }
         }
        if(min< 0 ){
            return min;
        }
        return 0;
    }


    /**
     * Calculates the eigenvalue for a Matrix and a corresponding eigenvector.
     *
     * @param matrix the matrix to find the eigenvalue for.
     * @param eigenVec the eigenvector of the eigenvalue to find.
     * @return the eigenvalue
     *
     * @author theo
     */
    private double findEigenVal(final RealMatrix matrix, final RealMatrix eigenVec){

        RealMatrix tmp = matrix.multiply(eigenVec);

        double result;
        if (tmp.getEntry(0, 0) != 0 || eigenVec.getEntry(0, 0) != 0.0) {
            result = tmp.getEntry(0, 0)/eigenVec.getEntry(0,0);
        } else {
            result = 0.0;
        }

        return result;
    }


    /**
     * Deflates a given Matrix by an corresponding pair of eigenvalue and eigenvector.
     *
     * @param matrix  the matrix that shall get deflated
     * @param firstEigenVec  eigenVec from the eigenspaces of the eigenvalue
     * @param firstEigenVal the largest eigenValue of the matrix
     * @return RealMatrix containing the deflated matrix c'
     *
     * @author theo
     */
    private RealMatrix deflateMatrix(final RealMatrix matrix, final RealMatrix firstEigenVec, final double firstEigenVal){

        //matrix - eigenVal * (eigenVec *eigenVec^T)
        return matrix.subtract(firstEigenVec.multiply(firstEigenVec.transpose()).scalarMultiply(firstEigenVal));
    }


    /**
     * Creates two random Vectors.
     *
     * @param dimension the dimension the vectors shall have.
     * @return a {@code dimension x 2} RealMatrixIml containing the two vectors
     *
     * @author theo
     */
    private RealMatrixImpl getRandomVector(final int dimension) {

        RealMatrixImpl result = new RealMatrixImpl(dimension, 1);
        double[][] resultArray = result.getDataRef();

        for(int row =0; row<dimension; row++){
            resultArray[row][0] = PivotMDS.RAND.nextInt();
        }
        return result;
    }


    /**
     * Calculates the difference between the two arrays. For this we compare the values of all cells
     * and sum the differences together to get the total difference.
     *
     * @param a first double-array
     * @param b second double-array
     * @return a double representing the difference
     *
     * @author theo
     */
    private double getDifference(final RealMatrix a, final RealMatrix b){
        double aVal;
        double bVal;
        double result = 0;
        for(int row = 0; row < a.getRowDimension(); row++){
            for(int col= 0; col< a.getColumnDimension(); col++){
                aVal= Math.abs(a.getEntry(row, col));
                bVal = Math.abs(b.getEntry(row, col));

                result += Math.abs(aVal -bVal);
            }
        }
        return result;
    }

    /**
     * Calculates the euclidean norm for a given vector
     *
     * @param vec RealMatrix with 1 column
     * @return double representing the euclidean norm
     *
     * @author theo
     */
    private double getEuclideanNorm(final RealMatrix vec){
        if(vec.getColumnDimension() >1){
            throw new IndexOutOfBoundsException();
        }
        double result = 0;
        for(int i = 0; i< vec.getRowDimension(); i++){
            double tmp = vec.getEntry(i, 0);
            result += tmp *tmp;
        }

        return Math.sqrt(result);
    }


    /**
     * Calculate the two eigen values using powerIteration.
     *
     * @param cTc with C being doubleCentred distance values, this
     *            parameter should contain the result of the multiplication
     *            C<sup>T</sup>C.
     * @param vec the vector to start the iteration from.
     * @return a doubleArray containing the two eigenvalues.
     *
     * @author theo
     */
     RealMatrix powerIterate(final RealMatrix cTc, RealMatrix vec) {

         RealMatrix eigenVec = vec;

        double change = 1;

        //stop, when the change is smaller 1e-10
        while(change > EPSILON){

            // remember old values
            RealMatrixImpl oldEigenVecs = (RealMatrixImpl) eigenVec.copy();

            //cTc * eigenVec
            eigenVec = cTc.multiply(eigenVec);

            // calculate new eigenVec
            double euclideanNorm = getEuclideanNorm(eigenVec);
            if (euclideanNorm != 0.0) {
                eigenVec = eigenVec.scalarMultiply(1/euclideanNorm);
            }

            //calculate the difference between the oldEigenVecs and the new eigenVecs
            change = Math.abs(getDifference(oldEigenVecs, eigenVec));
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
    RealMatrix doubleCenter(final NodeValueMatrix distances, final int amountPivots,
                                   final int[] distanceTranslation) {

        final int n = distances.getDimension();
        RealMatrixImpl c = new RealMatrixImpl(n, amountPivots);
        double  [][] results = c.getDataRef();

        // Sum three is independent of the current position so it can be calculated only once
        double sumThree = 0;
        double[] allSumOnes = new double[amountPivots]; // holds 'sumOne's
        for (int col = 0; col < amountPivots; col++) {
            for (int row = 0; row < n; row++) {
                sumThree += distances.get(row, col, distanceTranslation); // for sumThree
                allSumOnes[col] += distances.get(row, col, distanceTranslation); // for sumOne
            }
            allSumOnes[col] /= n;
        }
        sumThree /= (n*amountPivots);

        for(int row = 0; row < n; row++){

            //calculate sumTwo
            double sumTwo = 0;
            for(int col = 0; col < amountPivots; col++){
                sumTwo += distances.get(row, col, distanceTranslation);
            }
            sumTwo/=amountPivots;

            //calculate result[row][col]
            for (int col= 0; col < amountPivots; col++){
                results[row][col] = -(distances.get(row, col, distanceTranslation)
                        -allSumOnes[col] - sumTwo + sumThree)/2;
            }
        }
        return c;
    }

    /**
     * Gets a list of {@link Parameter}s this {@link PivotMDS}
     * provides.
     *
     * @return gets a list of Parameters this class provides.
     * @author Jannik
     * @see Algorithm#getParameters()
     */
    @Override
    public Parameter[] getParameters() {
        // TODO implement settings e.g. setting number of pivots
        return null; // maybe cache the parameters for reuse
    }

    /**
     * Provides a list of {@link Parameter}s that should be accepted by
     * the {@link PivotMDS}.<br>
     * This setter should only be called if the implementing class is not currently
     * executing something else.
     *
     * @param parameters the parameters to be set.
     * @author Jannik
     * @see Algorithm#setParameters(Parameter[])
     */
    @Override
    public void setParameters(Parameter[] parameters) {
        // TODO implement settings e.g. setting number of pivots
    }

    /**
     * @return the name of the this class.
     *         This may be be used to represent this class to the user.
     * @author Jannik
     */
    @Override
    public String getName() {
        return "PivotMDS";
    }

    /**
     * @return the description of the this class.
     *         This may be be used to explain the behaviour of this class to the user.
     * @author Jannik
     */
    @Override
    public String getDescription() {
        return "<html>Performs PivotMDS as initial layout. This is in most cases slower than using<br>" +
                "a random layout but creates a layout that results in less iterations being made<br>" +
                " and thus a hopefully faster running time.<br>" +
                "Running time <code>3*amountPivots*amountNodes</code>.</html>";
    }
}
