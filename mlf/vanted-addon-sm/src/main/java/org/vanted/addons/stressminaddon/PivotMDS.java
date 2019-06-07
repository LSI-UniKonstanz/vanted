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
        NodeValueMatrix squared = distances.clone().apply(x -> x*x, n, amountPivots);

        double  [][] results = c.getDataRef();
        for(int row = 0; row < n; row++){
            for (int col= 0; col < amountPivots; col++){

                int sumOne = 0;
                int sumTwo = 0;
                int sumThree = 0;

                //calculates sumOne
                for(int r = 0; r < n; r++){
                     sumOne += squared.get(r, col);

                     //calculate sumThree
                    for (int s = 0; s< amountPivots; s++){
                        sumThree += squared.get(r, s);

                    }
                }
                //calculate sumTwo
                for(int k = 0; k < amountPivots; k++){
                    sumTwo += squared.get(row, k);
                }


                results[row][col] = -0.5 *(squared.get(row, col) -(1.0/n)*sumOne - (1.0/amountPivots)* sumTwo +(1.0/(n*amountPivots)) * sumThree);
            }
        }
        return c;
    }



}
