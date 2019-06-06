package org.vanted.addons.stressminaddon;

import org.Vector2d;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealMatrixImpl;
import org.graffiti.graph.Node;
import org.vanted.addons.stressminaddon.util.NodeValueMatrix;

import java.util.List;

public class PivotMDS implements InitialPlacer {


    @Override
    public List<Vector2d> calculateInitialPositions(final List<Node> nodes, final NodeValueMatrix distances) {
        assert nodes.size() == distances.getDimension();

        return null;
    }

    private List<RealMatrix> powerIterate(final RealMatrix matrix) {

        //TODO
        return null;
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
