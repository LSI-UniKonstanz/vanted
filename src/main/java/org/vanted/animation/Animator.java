package org.vanted.animation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.graffiti.graph.Graph;
import org.vanted.animation.data.TimePoint;

/**
 * @author - Patrick Shaw
 */
public class Animator {
	private Graph graph;
	private List<Animation<TimePoint>> animations = new ArrayList<Animation<TimePoint>>();
	private List<Animation<TimePoint>> unfinishedAnimations = new ArrayList<Animation<TimePoint>>();
	private List<AnimatorListener> listeners = new ArrayList<AnimatorListener>();
	private int fps = 60; // FPS 
	private int updateRate = (int) Math.ceil(1000.0 / (double) fps);
	private double loopDuration = -1; // The total amount of time that it takes for the next loop to start in milliseconds
	private double currentTime = 0; // The time elapsed since the animator started animating
	private double speedFactor = 1; // How fast the animations go.
	private int noLoops = -1; // Number of loops
	private int currentLoopNumber = 0;
	private boolean isAutoLoopDuration;
	/**
	 * The ExecuterService that calls update() on fixed intervals.
	 */
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private boolean isInTransaction;
	private boolean isStopRequest;
	
	/**
	 * Creates an animator will perform a single loop forever.
	 */
	public Animator(Graph graph)
	{
		setGraph(graph);
		setNoLoops(1);
		setLoopDuration(LoopDuration.INFINITY);
		setIsAutoLoopDuration(false);
	}
	
	/**
	 * Creates an animator that will automatically calculate the loop duration to be the same
	 * as the animation who's loop duration is largest
	 * 
	 * @param noLoops
	 *           The number of times the animator will perform.
	 */
	public Animator(Graph graph, int noLoops)
	{
		setGraph(graph);
		setNoLoops(noLoops);
		setIsAutoLoopDuration(true);
	}
	
	/**
	 * Creates an animator that will automatically calculate the loop duration to be the same
	 * as the animation who's loop duration is largest
	 * 
	 * @param noLoops
	 *           The number of times the animator will perform.
	 */
	public Animator(Graph graph, NumberOfLoops noLoops)
	{
		setGraph(graph);
		setNoLoops(noLoops);
		setIsAutoLoopDuration(true);
	}
	
	/**
	 * Creates an animator that needs to have its loop duration manually specified.
	 * 
	 * @param noLoops
	 *           The number of times the animator will perform.
	 * @param loopDuration
	 *           How long each loop in the animator will last.
	 */
	public Animator(Graph graph, int noLoops, double loopDuration)
	{
		setGraph(graph);
		setNoLoops(noLoops);
		setLoopDuration(loopDuration);
		setIsAutoLoopDuration(false);
	}
	
	/**
	 * Creates an animator that needs to have its loop duration manually specified.
	 * 
	 * @param noLoops
	 *           The number of times the animator will perform.
	 * @param loopDuration
	 *           The magnitude of the loop duration.
	 * @param timeUnit
	 *           The time unit of the loop duration.
	 */
	public Animator(Graph graph, int noLoops, int loopDuration, TimeUnit timeUnit)
	{
		setGraph(graph);
		setNoLoops(noLoops);
		setLoopDuration(loopDuration, timeUnit);
		setIsAutoLoopDuration(false);
	}
	
	/**
	 * Creates an animator that needs to have its loop duration manually specified.
	 * 
	 * @param noLoops
	 *           The number of times the animator will perform.
	 * @param loopDuration
	 *           How long each loop in the animator will last.
	 */
	public Animator(Graph graph, int noLoops, LoopDuration loopDuration)
	{
		setGraph(graph);
		setNoLoops(noLoops);
		setLoopDuration(loopDuration);
		setIsAutoLoopDuration(false);
	}
	
	/**
	 * Sets whether the animator is automatically calculating the animator loop duration.
	 * 
	 * @param isAutoCalculatingLoopDuration
	 *           If True: The animator will always calculate the loop duration to be the same value
	 *           as the animation who's loop duration is the largest.<br>
	 *           If False: The animator loop duration needs to be set manually.
	 */
	public void setIsAutoLoopDuration(boolean isAutoCalculatingLoopDuration)
	{
		isAutoLoopDuration = isAutoCalculatingLoopDuration;
		tryAutoCalculateLoopDuration();
	}
	
	/**
	 * Sets whether the animator is automatically calculating the animator loop duration.
	 * 
	 * @return
	 *         If True: The animator will always calculate the loop duration to be the same value
	 *         as the animation who's loop duration is the largest.<br>
	 *         If False: The animator loop duration needs to be set manually.
	 */
	public boolean getIsAutoLoopDuration()
	{
		return isAutoLoopDuration;
	}
	
	/**
	 * Sets the animator graph
	 */
	private void setGraph(Graph graph)
	{
		this.graph = graph;
	}
	
	/**
	 * Auto calculates the loop duration to be the same value is the data point
	 * who's time value is the largest.
	 */
	public void tryAutoCalculateLoopDuration()
	{
		if (!isAutoLoopDuration) {
			return;
		}
		double maximum = 0;
		Iterator<Animation<TimePoint>> i = animations.iterator();
		while (i.hasNext())
		{
			double animationLoopDuration = i.next().loopDuration;
			maximum = maximum < animationLoopDuration ? animationLoopDuration : maximum;
		}
	}
	
	/**
	 * Sets the number of times animations are updated per second.
	 * 
	 * @param fps
	 */
	public void setFPS(int fps)
	{
		stop();
		this.fps = fps;
		updateRate = (int) ((double) TimeUnit.SECONDS.toMillis(1) / (double) fps);
		start();
	}
	
	/**
	 * Gets the number of times animations are updated per second.
	 * 
	 * @param fps
	 */
	public int getFPS()
	{
		return fps;
	}
	
	/**
	 * Get's the loop duration of the animation.
	 * 
	 * @return
	 *         The duration of the loop in milliseconds.
	 */
	public double getLoopDuration()
	{
		return loopDuration;
	}
	
	/**
	 * Sets the duration of a loop.
	 * 
	 * @param duration
	 *           The loop duration in milliseconds.
	 */
	public void setLoopDuration(double loopDuration)
	{
		this.loopDuration = loopDuration;
	}
	
	/**
	 * Sets the duration of a loop.
	 */
	public void setLoopDuration(LoopDuration loopDuration)
	{
		this.loopDuration = loopDuration.getValue();
	}
	
	/**
	 * Sets the duration of a loop.
	 * 
	 * @param duration
	 *           The time magnitude.
	 * @param The
	 *           unit of time being used.
	 */
	public void setLoopDuration(long time, TimeUnit timeUnit)
	{
		loopDuration = timeUnit.toMillis(time);
	}
	
	/**
	 * Gets the number of times the animator will perform a loop.
	 * 
	 * @param noLoops
	 *           -1 = Infinite loop<br>
	 *           0 = Doesn't do anything<br>
	 *           1 = Will do the animation once<Br>
	 *           2 = Will do the animation twice
	 */
	public int getNoLoops()
	{
		return this.noLoops;
	}
	
	/**
	 * Sets the number of times the animator will perform a loop.
	 * 
	 * @param noLoops
	 *           -1 = Infinite loop<br>
	 *           0 = Doesn't do anything<br>
	 *           1 = Will do the animation once<Br>
	 *           2 = Will do the animation twice
	 */
	public void setNoLoops(int noLoops)
	{
		this.noLoops = noLoops;
	}
	
	/**
	 * The number of times the animator will perform a loop.
	 */
	public void setNoLoops(NumberOfLoops noLoops)
	{
		this.noLoops = noLoops.getValue();
	}
	
	/**
	 * Call this method any time you want to add an animation.<br>
	 * Can be called while animations are taking place.<br>
	 * Call it whenever you want.
	 */
	public void addAnimation(Animation animation)
	{
		animations.add(animation);
		if (!scheduler.isShutdown())
		{
			unfinishedAnimations.add(animation);
		}
		if (isAutoLoopDuration)
		{
			loopDuration = loopDuration < animation.getLoopDuration() ? animation.getLoopDuration() : loopDuration;
		}
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
		try {
			unfinishedAnimations.remove(animation);
		} catch (Exception ex)
		{
			
		}
		tryAutoCalculateLoopDuration();
	}
	
	public void addListener(AnimatorListener listener)
	{
		listeners.add(listener);
	}
	
	public void removeListener(AnimatorListener listener)
	{
		listeners.remove(listener);
	}
	
	/**
	 * Recalculates the currentLoopNumber
	 * 
	 * @param time
	 *           The total amount of time that has elapsed since the animator first started.
	 *           without being reset.
	 */
	private void updateLoopNumber(double time)
	{
		boolean isLastLoop = isOnLastLoop();
		if (loopDuration == -1)
		{
			currentLoopNumber = 0;
			return;
		}
		int oldLoopNumber = currentLoopNumber;
		currentLoopNumber = (int) (time / Math.abs(loopDuration));
		if (oldLoopNumber != currentLoopNumber)
		{
			OnNextLoop();
			if (!isLastLoop)
			{
				unfinishedAnimations.clear();
				for (Animation animation : animations)
				{
					unfinishedAnimations.add(animation);
				}
			}
		}
	}
	
	/**
	 * Calls listeners onAnimationFinished and
	 * removes the animation from the list of animations to update
	 * 
	 * @param finishedAnimation
	 *           The animation that just finished
	 */
	private void onAnimationFinished(Animation finishedAnimation)
	{
		Iterator<AnimatorListener> i = listeners.iterator();
		while (i.hasNext())
		{
			i.next().onAnimationFinished(toAnimatorData(), finishedAnimation);
		}
		unfinishedAnimations.remove(finishedAnimation);
	}
	
	/**
	 * Calls listeners onAnimatorFinished
	 */
	private void onAnimatorFinished()
	{
		Iterator<AnimatorListener> i = listeners.iterator();
		while (i.hasNext())
		{
			i.next().onAnimatorFinished(toAnimatorData());
		}
	}
	
	/**
	 * Calls listeners onNewAnimatorLoop
	 */
	private void OnNextLoop()
	{
		Iterator<AnimatorListener> i = listeners.iterator();
		AnimatorData animatorData = this.toAnimatorData();
		while (i.hasNext())
		{
			i.next().onNewAnimatorLoop(animatorData);
		}
	}
	
	/**
	 * Creates an AnimatorData object out of the animator's own information.
	 * 
	 * @return
	 *         The animator data.
	 */
	private AnimatorData toAnimatorData()
	{
		return new AnimatorData(currentTime, currentLoopNumber, loopDuration, noLoops, isAutoLoopDuration);
	}
	
	/**
	 * @return
	 *         Whether this is the last loop before the animator stops.
	 */
	private boolean isOnLastLoop()
	{
		if (noLoops == -1) {
			return false;
		}
		return currentLoopNumber >= noLoops - 1;
	}
	
	/**
	 * The method that is called on fixed intervals.
	 */
	private void update()
	{
		double timeUsedForAnimations = Double.NaN;
		if (loopDuration == -1)
		{
			timeUsedForAnimations = currentTime;
		}
		else
		{
			if (!isOnLastLoop())
			{
				timeUsedForAnimations = currentTime % loopDuration;
			}
			else
			{
				timeUsedForAnimations = currentTime - (loopDuration * (noLoops - 1));
			}
			
		}
		Iterator<Animation<TimePoint>> animIterator = unfinishedAnimations.iterator();
		// Render graph...
		graph.getListenerManager().transactionStarted(this);
		isInTransaction = true;
		while (animIterator.hasNext())
		{
			Animation anim = animIterator.next();
			anim.update(timeUsedForAnimations, false);
		}
		isInTransaction = false;
		graph.getListenerManager().transactionFinished(this);
		animIterator = unfinishedAnimations.iterator();
		//System.out.println(currentTime);
		for (int i = unfinishedAnimations.size() - 1; i >= 0; i--)
		{
			Animation animation = unfinishedAnimations.get(i);
			if (animation.isFinished(timeUsedForAnimations))
			{
				onAnimationFinished(animation);
			}
		}
		// Check if the current loop has finished 
		updateLoopNumber(currentTime);
		// Check if we have looped enough time  
		if (noLoops != -1)
		{
			if (currentLoopNumber >= noLoops)
			{
				//System.out.println(currentLoopNumber);
				// One last check to ensure we reach the end of the animation
				graph.getListenerManager().transactionStarted(this);
				isInTransaction = true;
				for (Animation<TimePoint> animation : unfinishedAnimations) {
					animation.update(loopDuration, true);
				}
				isInTransaction = false;
				graph.getListenerManager().transactionFinished(this);
				stop(false);
				onAnimatorFinished();
			}
		}
		// Increase the time
		currentTime += (updateRate) * speedFactor;
	}
	
	/**
	 * Starts the animator.
	 * 
	 * @param activateListener
	 *           Specifies whether to activate the onAnimatorStart callback.
	 */
	private void start(boolean activateListeners) {
		
		Iterator<Animation<TimePoint>> animIterator = animations.iterator();
		unfinishedAnimations.clear();
		while (animIterator.hasNext())
		{
			unfinishedAnimations.add(animIterator.next());
		}
		final Runnable animatorService = new Runnable() {
			public void run()
			{
				update();
			}
		};
		final ScheduledFuture<?> animatorHandle =
				scheduler.scheduleAtFixedRate(animatorService, 0, updateRate, TimeUnit.MILLISECONDS);
		Iterator<AnimatorListener> i = listeners.iterator();
		while (i.hasNext())
		{
			i.next().onAnimatorStart(toAnimatorData());
		}
	}
	
	/**
	 * Starts the animator
	 */
	public void start()
	{
		start(true);
	}
	
	/**
	 * Stops the animator.
	 */
	public void stop()
	{
		stop(true);
	}
	
	/**
	 * Stops the animation
	 * 
	 * @param activateListeners
	 *           If true, the onAnimatorStop method will be called.
	 */
	private void stop(boolean activateListeners)
	{
		unfinishedAnimations.clear();
		
		//in case the scheduler is stopped during a transaction
		if (isInTransaction) {
//			System.out.println("finishing transaction in stop()");
			graph.getListenerManager().transactionFinished(this);
		}
		scheduler.shutdown();
		if (activateListeners)
		{
			Iterator<AnimatorListener> i = listeners.iterator();
			while (i.hasNext())
			{
				i.next().onAnimatorStop(toAnimatorData());
			}
		}
	}
	
	/**
	 * Resets the animation and starts it again.
	 */
	public void restart()
	{
		reset(false);
		Iterator<AnimatorListener> i = listeners.iterator();
		while (i.hasNext())
		{
			i.next().onAnimatorRestart(toAnimatorData());
		}
		start(false);
	}
	
	/**
	 * Stops the animation and rests it.
	 */
	public void reset()
	{
		reset(true);
	}
	
	/**
	 * Stops the animation and resets it.
	 * 
	 * @param activateListeners
	 *           Specifies whether to activate the onAnimatorReset callback
	 */
	private void reset(boolean activateListeners)
	{
		stop(false);
		currentTime = 0;
		currentLoopNumber = 0;
		for (int i = 0; i < animations.size(); i++)
		{
			animations.get(i).reset();
		}
		if (activateListeners)
		{
			Iterator<AnimatorListener> i = listeners.iterator();
			while (i.hasNext())
			{
				i.next().onAnimatorReset(toAnimatorData());
			}
		}
	}
}