package org.vanted.addons.stressminaddon;

import org.Vector2d;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealMatrixImpl;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.vanted.addons.stressminaddon.util.NodeValueMatrix;
import org.vanted.addons.stressminaddon.util.gui.EnableableNumberParameter;

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

    // default values
    /** Whether to use squared distances for the C matrix when double centering by default. */
    static final boolean USE_QUADRATIC_DOUBLE_CENTER_DEFAULT = false;
    /** The minimum number of pivots to be used by default. */
    static final int MINIMUM_AMOUNT_PIVOTS_DEFAULT = 50;
    /** The percentage of how nodes of the maximum will be used as pivots by default. */
    static final double PERCENT_PIVOTS_DEFAULT = 5.0;
    /** Whether to scale the graph after PivotMDS by default. */
    static final boolean USE_SCALING_DEFAULT = true;


    /** Whether to use squared distances for the C matrix when double centering. */
    boolean useQuadraticDoubleCenter = USE_QUADRATIC_DOUBLE_CENTER_DEFAULT;
    /** Whether to scale the graph after PivotMDS */
    boolean useScaling = USE_SCALING_DEFAULT;
    /** The percentage of how nodes of the maximum will be used as pivots. */
    double percentPivots = PERCENT_PIVOTS_DEFAULT;
    /** The minimum number of pivots to be used. */
    int minimumAmountPivots = MINIMUM_AMOUNT_PIVOTS_DEFAULT;



    /** Contains the parameters of this {@link StressMinimizationLayout}. */
    private Parameter[] parameters;

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

        final int numPivots = Math.max( (int) Math.ceil(nodes.size() * (percentPivots / 100)),
                Math.min(minimumAmountPivots, nodes.size())); // select at least minimumAmountPivots nodes but not more than there are.

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
        
        // create a list containing the new coordinates
        List<Vector2d> newPosList = new ArrayList<>();
        for(int i = 0; i < distances.getDimension(); i++){
            newPosList.add(new Vector2d(newX.getEntry(inversePivotTranslation[i], 0),
                                        newY.getEntry(inversePivotTranslation[i], 0)));

        }
        if (useScaling) {
            double maxEuclid = findMaxEuclidean(newPosList);
            if (maxEuclid == 0.0) {
                return newPosList;
            }
            double maxGraphDistance = distances.getMaximumValue();
            scaleInitialPos(maxEuclid, maxGraphDistance, newPosList);
        }

        return newPosList;
    }

    /**
     * finds the largest euclidean distance in a given list of nodes
     *
     * @param pos list of Vector2d  representing the current pos of all nodes
     * @return the biggest euclidean distance as double
     *
     * @author theo
     */
    double findMaxEuclidean(final List<Vector2d> pos ){
        double result = 0;
        for(Vector2d q : pos){
            for(Vector2d p : pos){
                double current = p.distance(q);
                if(result < current){
                    result = current;
                }
            }
        }
        return result;
    }


    
    /**
     * Scales the euclidean distance to the graph theoretical distance
     *
     * @param maxEuclidean largest euclidean distance in the graph
     * @param maxDistance largest grah theoretical distance
     * @param currentPos   Vector2d list containing the current node positions
     *
     * @author theo
     */
    private void scaleInitialPos(double maxEuclidean, double  maxDistance, List<Vector2d> currentPos ){
        double factor = maxDistance/maxEuclidean;
        for(Vector2d pos : currentPos){
            pos.x *= factor;
            pos.y *= factor;
        }
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
     * Calculates the eigenvalue for a Matrix and a corresponding eigenvector.
     *
     * @param matrix the matrix to find the eigenvalue for.
     * @param eigenVec the eigenvector of the eigenvalue to find.
     * @return the eigenvalue
     *
     * @author theo
     */
    double findEigenVal(final RealMatrix matrix, final RealMatrix eigenVec){

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
    double getEuclideanNorm(final RealMatrix vec){
        if(vec.getColumnDimension() >1){
            throw new IllegalArgumentException("Given vector must have only one column!");
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

        NodeValueMatrix processed;

        if (this.useQuadraticDoubleCenter) {
            processed = distances.clone().apply(x -> x*x, n-1, amountPivots-1, distanceTranslation);
        } else {
            processed = distances;
        }

        // Sum three is independent of the current position so it can be calculated only once
        double sumThree = 0;
        double[] allSumOnes = new double[amountPivots]; // holds 'sumOne's
        for (int col = 0; col < amountPivots; col++) {
            for (int row = 0; row < n; row++) {
                sumThree += processed.get(row, col, distanceTranslation); // for sumThree
                allSumOnes[col] += processed.get(row, col, distanceTranslation); // for sumOne
            }
            allSumOnes[col] /= n;
        }
        sumThree /= (n*amountPivots);

        for(int row = 0; row < n; row++){

            //calculate sumTwo
            double sumTwo = 0;
            for(int col = 0; col < amountPivots; col++){
                sumTwo += processed.get(row, col, distanceTranslation);
            }
            sumTwo/=amountPivots;

            //calculate result[row][col]
            for (int col= 0; col < amountPivots; col++){
                results[row][col] = -(processed.get(row, col, distanceTranslation)
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
     * @author Jannik, Theo
     * @see Algorithm#getParameters()
     */
    @Override
    public Parameter[] getParameters() {
        if (this.parameters == null) {
            this.parameters = getNewParameters();
        }
        return this.parameters;
    }

    /**
     * @return
     *      a new set of parameters to work with.
     * @author Jannik, theo
     */
    private Parameter[] getNewParameters() {

        return new Parameter[] {

                EnableableNumberParameter.alwaysEnabled(PERCENT_PIVOTS_DEFAULT, 0.0, 100.0, 1.0,
                        "Amount pivots in percent", "<html>Percent of the total nodes, which should be used as pivot elements.</html>" ),
                EnableableNumberParameter.alwaysEnabled(MINIMUM_AMOUNT_PIVOTS_DEFAULT, 1, Integer.MAX_VALUE, 1,
                        "Minimum pivots", "<html>Ensure at least this amount of pivots (if possible)<br>" +
                                "even if the percentage value would be smaller.</html>" ),
                new BooleanParameter(USE_QUADRATIC_DOUBLE_CENTER_DEFAULT, "Quadratic double center",
                        "<html> Whether the distances in PivotMDS double centering should be squared.<br>" +
                                "Pulls the initial layout far apart and thus should be used in tandem with scaling.<br>" +
                                "May result in a better initial layout.</html>"),
                new BooleanParameter(USE_SCALING_DEFAULT, "Scale result",
                        "<html> whether the graph should be scaled to the desired width as postprocessing.<br>" +
                                "Does not change the overall layout but should reduce number of iterations. </html>")
        };
    }
    /**
     * Provides a list of {@link Parameter}s that should be accepted by
     * the {@link PivotMDS}.<br>
     * This setter should only be called if the implementing class is not currently
     * executing something else.
     *
     * @param params the parameters to be set.
     * @author Jannik,  theo
     * @see Algorithm#setParameters(Parameter[])
     */
    @Override
    @SuppressWarnings("unchecked") // should never happen if parameters are kept in the same order
    public void setParameters(Parameter[] params) {

        EnableableNumberParameter<Double> doubleParameter;
        doubleParameter = (EnableableNumberParameter<Double>) params[0].getValue();
        EnableableNumberParameter<Integer> intParameter;
        intParameter = (EnableableNumberParameter<Integer>) params[1].getValue();
        percentPivots = doubleParameter.getValue();
        minimumAmountPivots = intParameter.getValue();
        useQuadraticDoubleCenter =  ((BooleanParameter) params[2]).getBoolean();
        useScaling =  ((BooleanParameter) params[3]).getBoolean();
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
