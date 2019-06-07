package org.vanted.addons.stressminaddon;

import org.Vector2d;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealMatrixImpl;
import org.graffiti.graph.Node;
import org.vanted.addons.stressminaddon.util.NodeValueMatrix;

import java.util.List;
import java.util.Random;

public class PivotMDS implements InitialPlacer {

    static final double EPSILON = 1 - 0.0000000001;


    @Override
    public List<Vector2d> calculateInitialPositions(final List<Node> nodes, final NodeValueMatrix distances) {
        assert nodes.size() == distances.getDimension();



        return null;
    }


    /**
     * creates to random Vectors
     * @param dimension the domension the vectors  shall have
     * @return a realMatrixIml dimension x 2 constaing the two vectors
     *
     * @author theo
     */
    private RealMatrixImpl getRandomVectors(final int dimension) {

        Random r = new Random();

        RealMatrixImpl result = new RealMatrixImpl(dimension, 2);
        double[][] resultArray = result.getDataRef();

        for(int row =0; row<dimension; row++){
            resultArray[row][0] = r.nextInt();
            resultArray[row][1] = r.nextInt();
        }
        return result;
    }


    /**
     *
     * @param a value 1
     * @param b value 2
     * @return b if b > a else a
     *
     * @author theo
     */
    private double updateMin(final double a, final double b ){
        return ((b> a) ? b : a);
    }


    /**
     * Multiplies the i-th elements of the two Arrays and adds up all products
     *
     * @param a first double-array
     * @param b second double-array
     * @return a double value
     *
     * @author theo
     */
    private double prod(final double[] a, final double[] b){

        double result = 0;
        for (int i = 0; i < a.length; i++) {
            result += a[i] * b[i];
        }
        return result;
    }


    /**
     * Calculating the two eigenValues using powerIteration
     *
     * @param matrix containing the doubleCentred distance values
     * @return a doubleArray containing the two eigeValues
     *
     * @author theo
     */
    private double[] powerIterate(final RealMatrix matrix) {

        double r = 0;

        double[] eigenVal = new double[2];

        //C^T * C
        RealMatrixImpl c = (RealMatrixImpl) matrix.transpose().multiply(matrix);


        final int dimension = c.getRowDimension();

        //create 2  random vectors
        RealMatrixImpl eigenVec = getRandomVectors(dimension);

        while(r < EPSILON){

            // remember old values
            RealMatrixImpl tmpOld = (RealMatrixImpl) eigenVec.copy();

            //c * eigenVec
            eigenVec = (RealMatrixImpl) c.multiply(eigenVec);

            // calculate new eigenVec
            eigenVec = (RealMatrixImpl) eigenVec.scalarMultiply(1/eigenVec.getNorm());

            //create norm of the two eigenVec
            eigenVal[0] = eigenVec.getSubMatrix(0,dimension-1, 0, 0).getNorm();
            eigenVal[1] = eigenVec.getSubMatrix(0,dimension-1, 1, 1).getNorm();


            // calculate new r
            r = 1;
            for(int i = 0; i < 2; i++){

                r = updateMin(r, Math.abs(prod(eigenVec.getColumn(i), tmpOld.getColumn(i))));
            }
        }
        return eigenVal;
    }

    /**
     *
     * calculates the double doublecenter Matrix C
     *
     * @param distances NodeValueMatrix containing all shortes distances
     * @param amountPivots the amount of pivot elements we want to use
     * @return  n x amountPivot Realmatrix containing the calculated values
     *
     * @author theo
     *
     */
    private RealMatrix doubleCenter(final NodeValueMatrix distances, final int amountPivots) {

        final int n = distances.getDimension();

        RealMatrixImpl c = new RealMatrixImpl(n, amountPivots);
        double  [][] results = c.getDataRef();
        for(int row = 0; row < n; row++){
            for (int col= 0; col < amountPivots; col++){

                int sumOne = 0;
                int sumTwo = 0;
                int sumThree = 0;

                //calculates sumOne
                for(int r = 0; r < n; r++){
                     sumOne += Math.pow(distances.get(r, col), 2);

                     //calculate sumThree
                    for (int s = 0; s< amountPivots; s++){
                        sumThree += Math.pow(distances.get(r, s), 2);

                    }
                }
                //calculate sumTwo
                for(int k = 0; k < amountPivots; k++){
                    sumTwo += Math.pow(distances.get(row, k), 2);
                }


                results[row][col] = 0.5 *(Math.pow(distances.get(row, col), 2) -(1/n)*sumOne - (1/amountPivots)* sumTwo +(1/(n*amountPivots)) * sumThree);
            }
        }
        return c;
    }



}
