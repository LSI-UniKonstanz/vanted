package org.vanted.addons.stressminaddon;

import org.AttributeHelper;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Collection;



public class QualityMeasures {

    public static int lineCrossing(Graph graph){
        int result = 0;

        Collection<Edge> edges = graph.getEdges();
        for (Edge currentEdge : edges) {
            final Point2D sourcePos = AttributeHelper.getPositionVec2d(currentEdge.getSource()).getPoint2D();
            final Point2D targetPos = AttributeHelper.getPositionVec2d(currentEdge.getTarget()).getPoint2D();
            Line2D lineCurrent = new Line2D.Double(sourcePos, targetPos);

            for (Edge otherEdge : edges) {
                if (otherEdge == currentEdge)
                    continue;
                final Point2D otherSourcePos = AttributeHelper.getPositionVec2d(otherEdge.getSource()).getPoint2D();
                final Point2D otherTargetPos = AttributeHelper.getPositionVec2d(otherEdge.getTarget()).getPoint2D();
                if (currentEdge.getTarget() .equals(otherEdge.getTarget()) || currentEdge.getTarget().equals(otherEdge.getSource()) ||
                currentEdge.getSource().equals(otherEdge.getSource()) || currentEdge.getSource().equals(otherEdge.getTarget()))
                    continue;
                Line2D lineOther = new Line2D.Double(otherSourcePos, otherTargetPos);

                if (lineCurrent.intersectsLine(lineOther)) {
                    result++;
                }
            }

        }


        return result /2;
    }
}
