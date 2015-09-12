package org.vanted.animation;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.AttributeHelper;
import org.Vector2d;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.vanted.animation.animators.FillColourAnimation;
import org.vanted.animation.animators.FrameThicknessAnimation;
import org.vanted.animation.animators.LabelColourAnimation;
import org.vanted.animation.animators.OutlineColourAnimation;
import org.vanted.animation.animators.Position2DAnimation;
import org.vanted.animation.animators.SizeAnimation;
import org.vanted.animation.animators.TestRainbowAlgorithm;
import org.vanted.animation.data.ColourPoint;
import org.vanted.animation.data.DoublePoint;
import org.vanted.animation.data.Point2DPoint;
import org.vanted.animation.data.TimePoint;
import org.vanted.animation.interpolators.CosignInterpolator;
import org.vanted.animation.interpolators.CubicHermiteInterpolator;
import org.vanted.animation.interpolators.CubicInterpolator;
import org.vanted.animation.interpolators.Interpolator;
import org.vanted.animation.interpolators.LinearInterpolator;
import org.vanted.animation.interpolators.SigmoidInterpolator;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public class Animator {
	private double endTime = 56;
	private double currentTime =0;
	private double speedFactor = 1;
	private int noLoops = -1;
	private List<Animation<TimePoint>> animations = new ArrayList<Animation<TimePoint>>();
	private Graph graph;
	public Animator(Graph graph, int noLoops)
	{
		this.graph = graph;
		this.noLoops = noLoops;
		startAnimation();
	}
	public int getIsLooping()
	{
		return this.noLoops;
	}
	public void setIsLooping(int noLoops)
	{
		this.noLoops = noLoops;
	}
	public void initialliseTestAnimation()
	{
		List<Node> nodes = graph.getNodes();
		endTime = 56 * nodes.size();
		// 0 -> 2
		// 2 -> 4
		// 4 -> 1
		// 1 -> 3
		// 3 -> 0
		// 0
		double inputRate = 0.1;
		double colourPointsPerNode =  1785.0 / (double)nodes.size();
		double colourChangeRate =  colourPointsPerNode;
		double labelColourPointsPerNode = 255.0/(double)nodes.size();
		int inputOffset = 512;
		for(int i = 0; i < nodes.size();i++)
		{
			Node node = nodes.get(i);
			int nextNodeIndex = i;
			Point2DPoint pointsArray[] = new Point2DPoint[nodes.size()];
			ColourPoint fillArray[] = new ColourPoint[nodes.size()];
			ColourPoint outlineArray[] = new ColourPoint[nodes.size()];
			ColourPoint labelColourArray[] = new ColourPoint[nodes.size()];
			Point2DPoint sizeArray[] = new Point2DPoint[nodes.size()];
			DoublePoint thicknessArray[] = new DoublePoint[nodes.size()];
			double input = inputRate*i * 2*colourPointsPerNode + 1785+inputOffset;//i * colourPointsPerNode;
			double outlineInput = input/2.0;   
			double sinX = 0;
			for(int q = 0; q < nodes.size();q++)
			{
				nextNodeIndex += 2;
				nextNodeIndex = nextNodeIndex % nodes.size();
				Node nextNode = nodes.get(nextNodeIndex);
				Point2D nextPos = AttributeHelper.getPosition(nextNode);
				double percentageTime = ((double)(q)) / ((double)nodes.size());
				pointsArray[q] = (new Point2DPoint(percentageTime*endTime,nextPos.getX(),nextPos.getY()));
				fillArray[q] = new ColourPoint(percentageTime * endTime,TestRainbowAlgorithm.CalcRainbowColour(input));
				Color outlineColour = TestRainbowAlgorithm.CalcRainbowColour(outlineInput);
				outlineColour = new Color(outlineColour.getRed() / 2, outlineColour.getGreen() / 2, outlineColour.getBlue() / 2);
				outlineArray[q] = new ColourPoint(percentageTime * endTime, outlineColour );
				int shade = (int)(123 + 122*Math.sin(sinX));
				labelColourArray[q] = new ColourPoint(percentageTime * endTime, new Color(shade,shade,shade));
				Vector2d size = AttributeHelper.getSize(node);
				double sizeFactor = ((double)(nodes.size() - nextNodeIndex)/(double)nodes.size())*1.5 +0.7;
				double randomSizeFactor = 1;//Math.random()*0.5 + 0.75;
				sizeArray[q] = new Point2DPoint(percentageTime*endTime, randomSizeFactor*size.x * sizeFactor, randomSizeFactor*size.y * sizeFactor);
				thicknessArray[q] = new DoublePoint(percentageTime * endTime,1 * (Math.random()*5.5 + 1));

				sinX += Math.PI / 2.0;
				input += colourChangeRate;
				outlineInput-=colourChangeRate;
				}
			Position2DAnimation  posAnim = new Position2DAnimation(node,endTime,new CubicInterpolator(LoopType.forward),Arrays.asList(pointsArray));
			FillColourAnimation fillAnim = new FillColourAnimation(node,endTime,new LinearInterpolator(LoopType.forward), Arrays.asList(fillArray));
			SizeAnimation sizeAnim = new SizeAnimation(node,endTime, new CubicInterpolator(LoopType.forward), Arrays.asList(sizeArray));
			OutlineColourAnimation outlineColourAnim = new OutlineColourAnimation(node,endTime,new LinearInterpolator(LoopType.forward), Arrays.asList(outlineArray));
			LabelColourAnimation labelColourAnim = new LabelColourAnimation(node,endTime,new LinearInterpolator(LoopType.forward),Arrays.asList(labelColourArray),-1);
			FrameThicknessAnimation thicknessAnim = new FrameThicknessAnimation(node,endTime,new LinearInterpolator(LoopType.forward),Arrays.asList(thicknessArray));
			//System.out.println(posAnim.toString());
			addAnimation(posAnim);
			addAnimation(fillAnim);
			//addAnimation(sizeAnim);
			addAnimation(outlineColourAnim);
			addAnimation(labelColourAnim);
			//addAnimation(thicknessAnim);
		}
		List<Edge> edges = (List<Edge>) graph.getEdges();
		double colourPointsPerEdge = 1*colourPointsPerNode * (double)nodes.size() / (double)edges.size();
		for(int i = 0; i<edges.size();i++)
		{
			Edge edge = edges.get(i);
			ColourPoint colours[] = new ColourPoint[nodes.size()];
			DoublePoint thickness[] = new DoublePoint[nodes.size()];
			int time = 0;
			double input1 = inputRate*i * 2*colourPointsPerEdge + 1785+inputOffset;//i * colourPointsPerNode;
			for(int q = 0 ;q<nodes.size();q++)
			{
				colours[q] = new ColourPoint(time,TestRainbowAlgorithm.CalcRainbowColour(input1)); 
				thickness[q] = new DoublePoint(time, Math.random() * 2 + 0.5);
				time+=56;
				input1 += colourChangeRate;
			}
			OutlineColourAnimation outlineColourAnim = new OutlineColourAnimation(edge,endTime,new LinearInterpolator(LoopType.forward),Arrays.asList(colours));
			FrameThicknessAnimation thicknessAnim = new FrameThicknessAnimation(edge,endTime,new CosignInterpolator(LoopType.forward), Arrays.asList(thickness));
			//addAnimation(thicknessAnim);
			addAnimation(outlineColourAnim);
		}
	}
	private boolean notInit = true;
	private final ScheduledExecutorService scheduler =
		     Executors.newScheduledThreadPool(1);

		   public void startAnimation() {
		     final Runnable animatorService = new Runnable() {
		       public void run()
		       { 
			    	   update(); 
		    	 if(currentTime == 20 && notInit)
		    	 {
		    		 initialliseTestAnimation();
		    		 notInit = false;
		    	 }
		       }
		     };
		     final ScheduledFuture<?> animatorHandle =
		       scheduler.scheduleAtFixedRate(animatorService, 10, 17, TimeUnit.MILLISECONDS);
		   }
	/**
	 * Call this method any time you want to add an animation.
	 * Can be called while animations are taking place.
	 * Call it whenever you want.
	 */
	public void addAnimation(Animation animation)
	{
		animations.add(animation);
	}
	/**
	 * Call this method any time you want to remove an animation.
	 * Can be called while animations are taking place. However,
	 * the attribute will not reset to it's original value prior
	 * to the animation if removed.
	 */
	public void removeAnimation(Animation animation)
	{
		animations.remove(animation);
	}
	private void update()
	{
		graph.getListenerManager().transactionStarted(this);
		Iterator<Animation<TimePoint>> animIterator = animations.iterator();
		while(animIterator.hasNext())
		{
			animIterator.next().animate(currentTime);
		}
		graph.getListenerManager().transactionFinished(this);
		currentTime += 1 * speedFactor;
		double oldTime = currentTime;
		currentTime %= endTime;
		if(oldTime != currentTime)
		{
			for(int i = 0 ; i < animations.size();i++)
			{
				animations.get(i).reset();
			}
		}
	}
}