package org.vanted.animation;
import java.awt.Color;
import java.awt.color.ColorSpace;
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
import org.vanted.animation.animations.FillColorAnimation;
import org.vanted.animation.animations.FrameThicknessAnimation;
import org.vanted.animation.animations.LabelColorAnimation;
import org.vanted.animation.animations.OutlineColorAnimation;
import org.vanted.animation.animations.Position2DAnimation;
import org.vanted.animation.animations.SizeAnimation;
import org.vanted.animation.animations.TestRainbowAlgorithm;
import org.vanted.animation.data.ColorMode;
import org.vanted.animation.data.ColorTimePoint;
import org.vanted.animation.data.DoubleTimePoint;
import org.vanted.animation.data.Point2DTimePoint;
import org.vanted.animation.data.TimePoint;
import org.vanted.animation.interpolators.BezierInterpolator;
import org.vanted.animation.interpolators.CosineInterpolator; 
import org.vanted.animation.interpolators.CubicInterpolator; 
import org.vanted.animation.interpolators.LinearInterpolator;
import org.vanted.animation.interpolators.SigmoidInterpolator;
import org.vanted.animation.loopers.ForwardLooper;
import org.vanted.animation.loopers.StandardLooper;
import org.vanted.animation.loopers.ForwardLooper;
import org.vanted.animation.loopers.ForwardLooper;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public class Animator { 
	private int fps = 60; // FPS 
	private int updateRate = (int)Math.ceil(1000.0/(double)fps);
	private double loopDuration = 56; // The total amount of time that it takes for the next loop to start in milliseconds
	private double currentTime =0; // The time elapsed since the animator started animating
	private double speedFactor = 1; // How fast the animations go.
	private int noLoops = -1; // Number of loops
	private int currentLoopNumber = 0;
	private int startTimeOfUpdate = 0;
	private int endTimeOfUpdate = 0;
	private List<Animation<TimePoint>> animations = new ArrayList<Animation<TimePoint>>();
	private Graph graph;
	public Animator(Graph graph, int noLoops)
	{
		this.graph = graph;
		this.noLoops = noLoops;
	}
	public void setFPS(int fps)
	{
		this.fps = fps;
		updateRate = (int)((double)TimeUnit.SECONDS.toMillis(1)/(double)fps);
	}
	public int getFPS()
	{
		return fps;
	}
	public double getLoopDuration()
	{
		return loopDuration;
	}
	public void setLoopDuration(long time,TimeUnit timeUnit)
	{
		loopDuration =   timeUnit.toMillis(time);
	} 
	public int getNoLoops()
	{
		return this.noLoops;
	}
	/**
	 * 
	 * @param noLoops
	 * -1 = Infinite loop<br>
	 * 0 = Doesn't do anything<br>
	 * 1 = Will do the animation once<Br>
	 * 2 = Will do the animation twice
	 */
	public void setNoLoops(int noLoops)
	{
		this.noLoops = noLoops;
	}
	/**
	 * Creates Patrick's test data for a rainbow animation effect.
	 */
	public void initialliseTestAnimation()
	{
		List<Node> nodes = graph.getNodes();
		double animationDuration = TimeUnit.SECONDS.toMillis(1 * nodes.size());
		loopDuration = -1;
		noLoops = -1;
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
				pointsArray[q] = (new Point2DTimePoint(percentageTime*animationDuration,nextPos.getX()*(Math.random() * 0.2+0.8),nextPos.getY()*(Math.random() * 0.2+0.8)));
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
			Position2DAnimation  posAnim = new Position2DAnimation(node,0,animationDuration ,new CubicInterpolator(),Arrays.asList(pointsArray), -1, new ForwardLooper());
			FillColorAnimation fillAnim = new FillColorAnimation(node,0,animationDuration ,new BezierInterpolator(), Arrays.asList(fillArray), -1, new ForwardLooper(), ColorMode.RGB);
			SizeAnimation sizeAnim = new SizeAnimation(node,0,animationDuration, new CubicInterpolator(), Arrays.asList(sizeArray), -1, new ForwardLooper());
			OutlineColorAnimation outlineColourAnim = new OutlineColorAnimation(node,0,animationDuration,new LinearInterpolator(), Arrays.asList(outlineArray), -1, new ForwardLooper(), ColorMode.RGB);
			LabelColorAnimation labelColourAnim = new LabelColorAnimation(node,0,animationDuration,new LinearInterpolator(),Arrays.asList(labelColourArray),-1, -1, new ForwardLooper(),ColorMode.HSB);
			FrameThicknessAnimation thicknessAnim = new FrameThicknessAnimation(node,0,animationDuration,new LinearInterpolator(),Arrays.asList(thicknessArray), -1, new ForwardLooper());
			//System.out.println(posAnim.toString());
			addAnimation(posAnim);
			addAnimation(fillAnim);
			//addAnimation(sizeAnim);
			addAnimation(outlineColourAnim);
			//addAnimation(labelColourAnim);
			//addAnimation(thicknessAnim);
		}
		
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
			OutlineColorAnimation outlineColourAnim = new OutlineColorAnimation(edge,0,animationDuration,new LinearInterpolator(),Arrays.asList(colours), -1, new ForwardLooper(),ColorMode.RGB);
			FrameThicknessAnimation thicknessAnim = new FrameThicknessAnimation(edge,0,animationDuration,new CosineInterpolator(), Arrays.asList(thickness), - 1, new ForwardLooper());
			//addAnimation(thicknessAnim);
			//addAnimation(outlineColourAnim);
		}
	}
	private final ScheduledExecutorService scheduler =
		     Executors.newScheduledThreadPool(1);

		   public void start() {
		     final Runnable animatorService = new Runnable() {
		       public void run()
		       {  
			    	  update(); 
		       }
		     };
		     final ScheduledFuture<?> animatorHandle =
		       scheduler.scheduleAtFixedRate(animatorService, 0, updateRate, TimeUnit.MILLISECONDS);
		   }
	/**
	 * Call this method any time you want to add an animation.<br>
	 * Can be called while animations are taking place.<br>
	 * Call it whenever you want.
	 */
	public void addAnimation(Animation animation)
	{
		animations.add(animation);
	}
	/**
	 * Call this method any time you want to remove an animation.<br>
	 * Can be called while animations are taking place. However,<br>
	 * the attribute will not reset to it's original value prior<br>
	 * to the animation if removed.
	 */
	public void removeAnimation(Animation animation)
	{
		animations.remove(animation);
	}
	private void updateLoopNumber(double time)
	{
		if (noLoops == -1)
		{
			currentLoopNumber = 0;
			return;
		}
		int oldLoopNumber = currentLoopNumber;
		currentLoopNumber = (int) (time/loopDuration);
		if(oldLoopNumber != currentLoopNumber)
		{  
			OnNextLoop(); 
		}
	}
	protected void OnNextLoop()
	{
	}
	private void update()
	{
		Iterator<Animation<TimePoint>> animIterator = animations.iterator();
		double timeUsedForAnimations = loopDuration != -1 ? currentTime % loopDuration : currentTime; 
		// Render graph...
		graph.getListenerManager().transactionStarted(this);
		while(animIterator.hasNext())
		{
			animIterator.next().update(timeUsedForAnimations);
		} 
		graph.getListenerManager().transactionFinished(this);
		// Check if the current loop has finished 
		updateLoopNumber(currentTime); 
		// Check if we have looped enough time
		if(noLoops != -1)
		{
			if(noLoops < currentLoopNumber)
			{
				stop();
			}
		}
		// Increase the time
		currentTime += (updateRate) * speedFactor; 
	}
	public void stop()
	{
		scheduler.shutdown();
	}
	public void restart()
	{
		currentTime = 0;
		currentLoopNumber = 0;
		for(int i = 0; i < animations.size();i++)
		{
			animations.get(i).reset();
		}
		start();
	}
	public void reset()
	{
		restart();
		stop();
	}
}