/**
 * 
 */
package org.vanted.opacitytest;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.AttributeHelper;
import org.Vector2d;
import org.graffiti.graph.Edge; 
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractEditorAlgorithm;
import org.graffiti.plugin.view.View;
import org.vanted.animation.Animator;
import org.vanted.animation.NumberOfLoops;
import org.vanted.animation.animations.FillColorAnimation;
import org.vanted.animation.animations.FrameThicknessAnimation;
import org.vanted.animation.animations.LabelColorAnimation;
import org.vanted.animation.animations.OutlineColorAnimation;
import org.vanted.animation.animations.Position2DAnimation;
import org.vanted.animation.animations.SizeAnimation;
import org.vanted.animation.data.ColorMode;
import org.vanted.animation.data.ColorTimePoint;
import org.vanted.animation.data.DoubleTimePoint;
import org.vanted.animation.data.Point2DTimePoint;
import org.vanted.animation.interpolators.BezierInterpolator;
import org.vanted.animation.interpolators.CosineInterpolator;
import org.vanted.animation.interpolators.CubicInterpolator;
import org.vanted.animation.interpolators.LinearInterpolator;
import org.vanted.animation.loopers.ForwardLooper; 

/**
 * 
 * @author - Patrick Shaw
 * 
 */
public class RainbowSwappingTest extends AbstractEditorAlgorithm {
	
	@Override
	public boolean activeForView(View v) {
		return true;
	}
	
	@Override
	public String getName() {
		return "Test Rainbow Animation";
	}
	
	@Override
	public String getMenuCategory() {
		return "Network";
	}
	
	@Override
	public void execute() {
		Animator animator = new Animator(graph, 1);
		List<Node> nodes = new ArrayList<Node>();
		for(Node node : getSelectedOrAllNodes())
			nodes.add(node);
		double animationDuration = TimeUnit.SECONDS.toMillis(1 * nodes.size());
		double inputRate = 0.1;
		double colourPointsPerNode =  1785.0 / (double)nodes.size();
		double colourChangeRate =  colourPointsPerNode;
		double labelColourPointsPerNode = 255.0/(double)nodes.size();
		int inputOffset = 512;
		int pointSize = nodes.size();
		for(int i = 0; i < nodes.size();i++)
		{
			Node node = nodes.get(i);
			int nextNodeIndex = i;
			Point2DTimePoint pointsArray[] = new Point2DTimePoint[pointSize];
			ColorTimePoint fillArray[] = new ColorTimePoint[pointSize];
			ColorTimePoint outlineArray[] = new ColorTimePoint[pointSize];
			ColorTimePoint labelColourArray[] = new ColorTimePoint[pointSize];
			Point2DTimePoint sizeArray[] = new Point2DTimePoint[pointSize];
			DoubleTimePoint thicknessArray[] = new DoubleTimePoint[pointSize];
			double input = inputRate*i * 2*colourPointsPerNode + 1785+inputOffset;//i * colourPointsPerNode;
			double outlineInput = input/2.0;   
			double sinX = 0;
			
			for(int q = 0; q < pointSize;q++)
			{
				nextNodeIndex += 2;
				nextNodeIndex = nextNodeIndex % nodes.size();
				Node nextNode = nodes.get(nextNodeIndex);
				Point2D nextPos = AttributeHelper.getPosition(nextNode);
				double percentageTime = ((double)(q)) / ((double)nodes.size());
				pointsArray[q] = (new Point2DTimePoint(percentageTime*animationDuration,nextPos.getX()*(Math.random() * 0+1),nextPos.getY()*(Math.random() * 0+1)));
				Color fillColour = TestRainbowAlgorithm.CalcRainbowColour(input);
				fillArray[q] = new ColorTimePoint(percentageTime * animationDuration,fillColour);
				Color outlineColour = TestRainbowAlgorithm.CalcRainbowColour(outlineInput);
				outlineColour = new Color(outlineColour.getRed() / 2, outlineColour.getGreen() / 2, outlineColour.getBlue() / 2);
				outlineArray[q] = new ColorTimePoint(percentageTime * animationDuration, outlineColour);
				int shade = (int)(123 + 122*Math.sin(sinX));
				labelColourArray[q] = new ColorTimePoint(percentageTime * animationDuration, new Color(shade,shade,shade));
				Vector2d size = AttributeHelper.getSize(node);
				double sizeFactor = ((double)(nodes.size() - nextNodeIndex)/(double)nodes.size())*1.5 +0.7;
				double randomSizeFactor = 1;//Math.random()*0.5 + 0.75;
				sizeArray[q] = new Point2DTimePoint(percentageTime*animationDuration, randomSizeFactor*size.x * sizeFactor, randomSizeFactor*size.y * sizeFactor);
				thicknessArray[q] = new DoubleTimePoint(percentageTime * animationDuration,1 * (Math.random()*5.5 + 1));

				sinX += Math.PI / 2.0;
				input += colourChangeRate;
				outlineInput-=colourChangeRate;
				}
			Position2DAnimation  posAnim = new Position2DAnimation(node,Arrays.asList(pointsArray),animationDuration, 0, 2, new ForwardLooper(),new CubicInterpolator());
			FillColorAnimation fillAnim = new FillColorAnimation(node,Arrays.asList(fillArray), animationDuration, 0, 2, new ForwardLooper());
			SizeAnimation sizeAnim = new SizeAnimation(node, Arrays.asList(sizeArray), animationDuration, 0, 2, new ForwardLooper(), new CubicInterpolator());
			OutlineColorAnimation outlineColourAnim = new OutlineColorAnimation(node, Arrays.asList(outlineArray), animationDuration, 0, 2, new ForwardLooper());
			LabelColorAnimation labelColourAnim = new LabelColorAnimation(node,Arrays.asList(labelColourArray), animationDuration, 0, 2, new ForwardLooper(), new LinearInterpolator(), ColorMode.HSB, 1);
			FrameThicknessAnimation thicknessAnim = new FrameThicknessAnimation(node, Arrays.asList(thicknessArray), animationDuration, 0, 2, new ForwardLooper());
			//System.out.println(posAnim.toString());
			animator.addAnimation(posAnim);
			animator.addAnimation(fillAnim);
			animator.addAnimation(sizeAnim);
			animator.addAnimation(outlineColourAnim);
			animator.addAnimation(labelColourAnim);
			animator.addAnimation(thicknessAnim);
		}
		
		// This doesn't work
		List<Edge> edges = (List<Edge>) graph.getEdges(); 
		double colourPointsPerEdge = 1*colourPointsPerNode * (double)nodes.size() / (double)edges.size();
		for(int i = 0; i<edges.size();i++)
		{
			Edge edge = edges.get(i);
			ColorTimePoint colours[] = new ColorTimePoint[nodes.size()];
			DoubleTimePoint thickness[] = new DoubleTimePoint[nodes.size()];
			int time = 0;
			double input1 = inputRate*i * 2*colourPointsPerEdge + 1785+inputOffset;//i * colourPointsPerNode;
			for(int q = 0 ;q<nodes.size();q++)
			{
				colours[q] = new ColorTimePoint(time,TestRainbowAlgorithm.CalcRainbowColour(input1)); 
				thickness[q] = new DoubleTimePoint(time, Math.random() * 2 + 0.5);
				time+=56;
				input1 += colourChangeRate;
			}
			OutlineColorAnimation outlineColourAnim = new OutlineColorAnimation(edge,Arrays.asList(colours), animationDuration, 0, 2, new ForwardLooper());
			FrameThicknessAnimation thicknessAnim = new FrameThicknessAnimation(edge,Arrays.asList(thickness), animationDuration, 0, 2, new ForwardLooper());
			//animator.addAnimation(thicknessAnim);
			//animator.addAnimation(outlineColourAnim);
		}
		animator.start();
	}
	
}
