/**
 * 
 */
package org.vanted.animation.tests;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.AttributeHelper;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractEditorAlgorithm;
import org.graffiti.plugin.view.View;
import org.vanted.animation.Animation;
import org.vanted.animation.Animator;
import org.vanted.animation.AnimatorData;
import org.vanted.animation.AnimatorListener;
import org.vanted.animation.animations.FillColorAnimation;
import org.vanted.animation.animations.Position2DAnimation;
import org.vanted.animation.data.ColorTimePoint;
import org.vanted.animation.data.Point2DTimePoint;
import org.vanted.animation.data.TimePoint;
import org.vanted.animation.loopers.StandardLooper;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.graph_generation.WattsStrogatzGraphGenerator;

/**
 * @author matthiak
 */
public class AnimatorTestSuit extends AbstractEditorAlgorithm implements AnimatorListener<Object> {

	Graph testGraph;

	List<AnimatorTest<?>> listAnimationTests;

	private Iterator<AnimatorTest<?>> iterator;
	AnimatorTest<Object> previousTest;

	Map<Node, Point2D> mapNodesLocationOriginal = new HashMap<Node, Point2D>();

	@Override
	public boolean activeForView(View v) {
		return true;
	}

	@Override
	public String getName() {
		return "Test Movement Animation";
	}

	@Override
	public String getMenuCategory() {
		return "Network";
	}

	@Override
	public void execute() {
		testGraph = WattsStrogatzGraphGenerator.createGraph(20, true, 5, 0.6);
		MainFrame.getInstance().showGraph(testGraph, null);
		graph = testGraph;
		listAnimationTests = new ArrayList<AnimatorTestSuit.AnimatorTest<?>>();

		/*
		 * store original node locations. They will be restored after each test
		 */
		for (Node node : getSelectedOrAllNodes()) {
			mapNodesLocationOriginal.put(node, AttributeHelper.getPosition(node));
		}
		/*
		 * Movement Animation test. this test will move all nodes by 100 pixels in x and
		 * y Check will be if expected position after animation ends is node-old-pos +
		 * 100px
		 */
		listAnimationTests.add(new MotionTest1());

		/*
		 * Same test with motion but this time it's a swing animator
		 */

		listAnimationTests.add(new MotionTest2());

		/*
		 * test the color animation
		 */
		listAnimationTests.add(new ColorTest());

		iterator = listAnimationTests.iterator();
		previousTest = (AnimatorTest<Object>) iterator.next();
		previousTest.runTest(this);
	}

	@Override
	public void onAnimationFinished(AnimatorData data, Animation<TimePoint<Object>> anim) {
		System.out.println("Animation finished: " + anim.toString());
	}

	@Override
	public void onNewAnimatorLoop(AnimatorData data) {
	}

	@Override
	public void onAnimatorStart(AnimatorData data) {
	}

	@Override
	public void onAnimatorStop(AnimatorData data) {
	}

	@Override
	public void onAnimatorReset(AnimatorData data) {
	}

	@Override
	public void onAnimatorRestart(AnimatorData data) {
	}

	@Override
	public void onAnimatorFinished(AnimatorData data) {
		System.out.println("running next animation test");
		previousTest.checkResult();

		// reset node location
		graph.getListenerManager().transactionStarted(this);
		for (Node node : mapNodesLocationOriginal.keySet()) {
			AttributeHelper.setPosition(node, mapNodesLocationOriginal.get(node));
		}
		graph.getListenerManager().transactionFinished(this);
		if (iterator.hasNext()) {
			AnimatorTest<Object> next = (AnimatorTest<Object>) iterator.next();
			previousTest = next;
			next.runTest(this);
		} else
			System.out.println("all tests finished");
	}

	interface AnimatorTest<T> {
		public void runTest(AnimatorListener<T> listener);

		public void checkResult();
	}

	class MotionTest1 implements AnimatorTest<Point2D> {

		Map<Node, Point2D> mapNodesLocationExpectedAfter = new HashMap<Node, Point2D>();

		@Override
		public void runTest(AnimatorListener<Point2D> listener) {
			System.out.println("starting motion test");

			int duration = 1000;
			Animator animator = new Animator(graph, 1);
			animator.addListener(listener);
			// Map<Node, Point2D> mapNodesLocationBefore = new HashMap<Node, Point2D>();

			for (Node node : getSelectedOrAllNodes()) {
				Point2D position = AttributeHelper.getPosition(node);
				position.setLocation(position.getX() + 100, position.getY() + 100);
				mapNodesLocationExpectedAfter.put(node, position);

			}
			for (GraphElement ge : getSelectedOrAllNodes()) {
				List<Point2DTimePoint> listP2dTP = new ArrayList<>();
				Point2D position = AttributeHelper.getPosition((Node) ge);
				listP2dTP.add(new Point2DTimePoint(0, position));
				listP2dTP.add(new Point2DTimePoint(1000, mapNodesLocationExpectedAfter.get(ge)));
				Position2DAnimation posAnim = new Position2DAnimation((Node) ge, listP2dTP, duration);

				animator.addAnimation(posAnim);
			}

			animator.start();
		}

		@Override
		public void checkResult() {
			boolean fail = false;
			for (Node node : getSelectedOrAllNodes()) {
				Point2D position = AttributeHelper.getPosition(node);
				if (!position.equals(mapNodesLocationExpectedAfter.get(node)))
					fail = true;
			}
			if (fail)
				System.err.println("Motion test failed");
			else
				System.out.println("Motion test passed");
		}
	}

	class MotionTest2 implements AnimatorTest<Point2D> {

		Map<Node, Point2D> mapNodesLocationExpectedAfter = new HashMap<Node, Point2D>();

		@Override
		public void runTest(AnimatorListener<Point2D> listener) {
			System.out.println("starting motion2 test");
			int duration = 500;
			Animator animator = new Animator(graph, 1, 2 * duration);
			animator.addListener(listener);

			for (Node node : getSelectedOrAllNodes()) {
				Point2D position = AttributeHelper.getPosition(node);
				position.setLocation(position.getX() + 100, position.getY() + 100);
				mapNodesLocationExpectedAfter.put(node, position); // after the swing we expect the nodes to be were
																	// they were

			}
			for (GraphElement ge : getSelectedOrAllNodes()) {
				List<Point2DTimePoint> listP2dTP = new ArrayList<>();
				Point2D position = AttributeHelper.getPosition((Node) ge);
				listP2dTP.add(new Point2DTimePoint(0, position));
				Point2D.Double pointnew = new Point2D.Double(position.getX() + 100, position.getY() + 100);
				listP2dTP.add(new Point2DTimePoint(duration, pointnew));
				Position2DAnimation posAnim = new Position2DAnimation((Node) ge, listP2dTP, duration, 0, 2,
						new StandardLooper());

				animator.addAnimation(posAnim);
			}

			animator.start();
		}

		@Override
		public void checkResult() {
			boolean fail = false;
			for (Node node : getSelectedOrAllNodes()) {
				Point2D position = AttributeHelper.getPosition(node);
				if (!position.equals(mapNodesLocationExpectedAfter.get(node)))
					fail = true;
			}
			if (fail)
				System.err.println("Motion2 test failed");
			else
				System.out.println("Motion2 test passed");
		}
	}

	class ColorTest implements AnimatorTest<Color> {
		Map<Node, Color> mapNodesColorExpectedAfter = new HashMap<Node, Color>();

		@Override
		public void runTest(AnimatorListener<Color> listener) {

			int duration = 1000;
			Animator animator = new Animator(graph, 1, duration);
			animator.addListener(listener);

			for (Node node : getSelectedOrAllNodes()) {

				// position.setLocation(position.getX() + 100, position.getY() + 100);
				mapNodesColorExpectedAfter.put(node, Color.RED); // after the swing we expect the nodes to be were they
																	// were

			}
			for (GraphElement ge : getSelectedOrAllNodes()) {
				List<ColorTimePoint> listP2dTP = new ArrayList<>();
				Color curColor = AttributeHelper.getFillColor(ge);
				listP2dTP.add(new ColorTimePoint(0, curColor));
				listP2dTP.add(new ColorTimePoint(duration, Color.RED));
				FillColorAnimation colAnim = new FillColorAnimation((Node) ge, listP2dTP, duration, 0, 1);

				animator.addAnimation(colAnim);
			}

			animator.start();
		}

		@Override
		public void checkResult() {
			boolean fail = false;
			for (Node node : getSelectedOrAllNodes()) {
				Color curColor = AttributeHelper.getFillColor(node);
				if (!curColor.equals(mapNodesColorExpectedAfter.get(node)))
					fail = true;
			}
			if (fail)
				System.err.println("Fillcolor test failed");
			else
				System.out.println("Fillcolor test passed");
		}
	}
}
