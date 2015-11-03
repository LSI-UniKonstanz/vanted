package org.vanted.animation.tests;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.vanted.animation.*;
import org.vanted.animation.animations.FillColorAnimation;
import org.vanted.animation.animations.Position2DAnimation;
import org.vanted.animation.animations.VisibilityAnimation;
import org.vanted.animation.data.BooleanTimePoint;
import org.vanted.animation.data.ColorTimePoint;
import org.vanted.animation.data.Point2DTimePoint;
import org.vanted.animation.data.TimePoint;
import org.vanted.animation.interpolators.CosineInterpolator;
import org.vanted.animation.interpolators.CubicInterpolator;
import org.vanted.animation.interpolators.LinearInterpolator;
import org.vanted.animation.interpolators.SigmoidInterpolator;
import org.vanted.animation.loopers.StandardLooper;

class Point
{
	private int x;
	private int y;
	public Point(int x, int y){this.x = x; this.y = y;}
	public int getY(){return y;}
	public int getX(){return x;}
	public void setLocation(int x, int y){this.x = x; this.y= y;}
}
class ParagraphSentencer
{
	public static Letter charToLG(char l)
	{
		switch(l)
		{
			case 'a':
				return new LetterA();
			case 'b':
				return new LetterB();
			case 'c':
				return new LetterC();
			case 'd':
				return new LetterD();
			case 'e':
				return new LetterE();
			case 'f':
				return new LetterF();
			case 'g':
				return new LetterG();
			case 'h':
				return new LetterH();
			case 'i':
				return new LetterI();
			case 'j':
				return new LetterJ();
			case 'k':
				return new LetterK();
			case 'l':
				return new LetterL();
			case 'm':
				return new LetterM();
			case 'n':
				return new LetterN();
			case 'o':
				return new LetterO();
			case 'p':
				return new LetterP();
			case 'q':
				return new LetterQ();
			case 'r':
				return new LetterR();
			case 's':
				return new LetterS();
			case 't':
				return new LetterT();
			case 'u':
				return new LetterU();
			case 'v':
				return new LetterV();
			case 'w':
				return new LetterW();
			case 'x':
				return new LetterX();
			case 'y':
				return new LetterY();
			case 'z':
				return new LetterZ();
			case ' ':
				return new LetterSpace();
			case '.':
				return new LetterFullStop();
		}
		return null;
	}
	public static List<Animation> sentencesToNodeInfo(Graph graph, List<Node> realNodes,List<String> sentences)
	{
		List<Animation> animations = new ArrayList<Animation>();
		ArrayList<List<Point>> sentenceEdges = new ArrayList<List<Point>>();
		ArrayList<List<Point2D.Double>> sentenceNodes = new ArrayList<List<Point2D.Double>>();
		for(int i = 0; i < sentences.size();i++)
		{
			double offset = 0;
			int characterCountLine = 0;
			int line = 0;
			List<Point2D.Double> nodes = new ArrayList<Point2D.Double>();
			List<Point> edges = new ArrayList<Point>(); 
			for(int q = 0; q < sentences.get(i).length(); q++)
			{ 
				Letter tempLetter = charToLG(sentences.get(i).charAt(q));
				List<Point2D.Double> letterNodes = tempLetter.getNodes();
				List<Point> letterEdges = tempLetter.getEdges();
				offset += tempLetter.getWidth();
				offset += letterNodes.size() == 0 ? 0.1 : 0.5;
				for(int j = 0 ; j < letterNodes.size(); j++)
				{
					letterNodes.get(j).x = letterNodes.get(j).getX()+offset;
					letterNodes.get(j).y = 0.5+letterNodes.get(j).getY() + line * 1.5;
				} 
				for(int j = 0; j < letterEdges.size(); j++)
				{
					letterEdges.get(j).setLocation(letterEdges.get(j).getX() + nodes.size(), letterEdges.get(j).getY() + nodes.size());
					/*System.out.print("From: ");
					System.out.print(letterEdges.get(j).getX());
					System.out.print(" -> ");
					System.out.println(letterEdges.get(j).getY());*/
				}
				nodes.addAll(letterNodes);
				edges.addAll(letterEdges);
				characterCountLine++;
				if(characterCountLine >= 7 && letterNodes.size() == 0)
				{
					characterCountLine = 0;
					offset = 0;
					line++;
				}
			}
			sentenceEdges.add(edges);
			sentenceNodes.add(nodes);
		}
		int nodesNeeded = 0;
		for(int i = 0 ; i < sentenceNodes.size();i++)
		{
			int nodesInSentence = sentenceNodes.get(i).size();
			if(nodesNeeded<nodesInSentence)
			{
				nodesNeeded = nodesInSentence;
			}
		}
		System.out.print("Nodes needed: ");
		System.out.println(nodesNeeded);
		for(int i = 0; i < sentenceNodes.size();i++)
		{
			Point2D.Double lastNode = null;
			if(sentenceNodes.get(i).size() == 0)
			{
				lastNode = new Point2D.Double(0,0);
			}
			else
				{
				lastNode = sentenceNodes.get(i).get(sentenceNodes.get(i).size() - 1);
				}
			//System.out.println("sentenceNodes[" + Integer.toString(i) + "] size: " + Integer.toString(sentenceNodes.get(i).size()) );
			for(int q = sentenceNodes.get(i).size(); q < nodesNeeded;q++)
			{
				sentenceNodes.get(i).add(new Point2D.Double(lastNode.getX(), lastNode.getY()));
			}
		}
		//System.out.println("Length: " + Integer.toString(sentenceNodes.size()));
		System.out.println("Nodes: " + Integer.toString(sentenceNodes.get(0).size()));

		final double nodeDelay = 25;
		// TO ANIMATION:
		ArrayList<ArrayList<Point2DTimePoint>> pointPoints = new ArrayList<ArrayList<Point2DTimePoint>>();
		for(int i = 0; i < sentenceNodes.get(0).size();i++)
		{
			ArrayList<Point2DTimePoint> points = new ArrayList<Point2DTimePoint>(); 
			double currentTime = nodeDelay * i; 
			for(int q = 0; q < sentenceNodes.size();q++)
			{
				Point2DTimePoint positionPoint = null;
				if (points.size() == 0)
				{positionPoint = new Point2DTimePoint(0,sentenceNodes.get(q).get(i));
				}
				else
				{positionPoint = new Point2DTimePoint(currentTime,sentenceNodes.get(q).get(i));
				}
				points.add(positionPoint);
				currentTime += pauseTime;
				Point2D.Double copy = sentenceNodes.get(q).get(i);
				copy = new Point2D.Double(copy.getX(), copy.getY());
				Point2DTimePoint positionPoint2 = new Point2DTimePoint(currentTime,copy);
				points.add(positionPoint2);
				currentTime += transitionTime;
			}
			pointPoints.add(points);
		}
		for(ArrayList<Point2DTimePoint> pointPoint : pointPoints)
		{
			for (Point2DTimePoint point: pointPoint)
			{
				point.setX(point.getX() * 60);
				point.setY(point.getY() * 90);
			}
		}
		for(int i =0 ;i < realNodes.size();i++)
		{
			List<Point2DTimePoint> posPoints = pointPoints.get(i);
			List<ColorTimePoint> colourPoints = new ArrayList<ColorTimePoint>();
			double inputRate = 0.10;
			double colourPointsPerNode =  1785.0 / (double)realNodes.size();
			double colourChangeRate =  colourPointsPerNode * 6; 
			int inputOffset = 512;
			double input = inputRate*i * 2*colourPointsPerNode + 1785+inputOffset;//i * colourPointsPerNode;
			int jTime = 0;
			for(int j = 0; j < sentences.size();j++)
			{
				colourPoints.add(new ColorTimePoint(jTime+ nodeDelay * i,TestRainbowAlgorithm.CalcRainbowColour(input)));
				jTime += pauseTime;
				colourPoints.add(new ColorTimePoint(jTime+ nodeDelay * i,TestRainbowAlgorithm.CalcRainbowColour(input)));
				jTime += transitionTime;
				input += colourChangeRate;
			}
			for(int j = i ; j < realNodes.size(); j++){
				ArrayList<BooleanTimePoint> edgeVisibility = new ArrayList<BooleanTimePoint>();
				boolean oneEdgeVis = false;
				double currentTime = 0;
				for (int q =0 ; q < sentenceEdges.size(); q++)
				{
						boolean isHidden = true;
						for(int p = 0; p < sentenceEdges.get(q).size();p++)
						{
							if(
									(sentenceEdges.get(q).get(p).getX() == i && sentenceEdges.get(q).get(p).getY() == j)
									||
									(sentenceEdges.get(q).get(p).getY() == i && sentenceEdges.get(q).get(p).getX() == j)
								)
							{
								/*System.out.println("Found (" + Integer.toString(sentenceEdges.get(q).get(p).getX()) 
								+ ", " + sentenceEdges.get(q).get(p).getY() + ") with (" + Integer.toString(i) + ", " + Integer.toString(j) + ")");
								*/isHidden = false;
								oneEdgeVis = true;
								break;
							}
						} 
						if (edgeVisibility.size() == 0)
						{
							edgeVisibility.add(new BooleanTimePoint(0,isHidden));
						}
						edgeVisibility.add(new BooleanTimePoint(currentTime+ nodeDelay * i - transitionTime/2.0,isHidden));
						currentTime += pauseTime + transitionTime;
					}
				double lastPoint = edgeVisibility.get(edgeVisibility.size() - 1).getTime() + pauseTime + transitionTime;
				//System.out.println(lastPoint);
				ArrayList<Edge> nodeEdges =new ArrayList<Edge>(graph.getEdges(realNodes.get(i), realNodes.get(j)));
				if (nodeEdges.size() != 0)
					if(oneEdgeVis)
						animations.add(new VisibilityAnimation(nodeEdges.get(0), edgeVisibility, lastPoint, 0, 1));
					else
						animations.add(new VisibilityAnimation(nodeEdges.get(0), Arrays.asList(new BooleanTimePoint[]{new BooleanTimePoint(0,true)}), lastPoint, 0, 1));
				
			}  
			animations.add(new Position2DAnimation(realNodes.get(i),posPoints,posPoints.get(posPoints.size() - 1).getTime(), 0, 1, new StandardLooper(), new SigmoidInterpolator()));
			animations.add(new FillColorAnimation(realNodes.get(i),colourPoints,posPoints.get(posPoints.size() - 1).getTime(), 0, 1, new StandardLooper(), new CosineInterpolator()));
			}
		return animations;
	}
	final static double pauseTime = 6000;
	final static double transitionTime = 500;
}
class Letter
{
	protected Point2D.Double nodes[];
	protected Point edges[];
	protected double width = 1;
	protected double height = 1.5;
	public double getHeight(){return height;}
	public double getWidth()
	{
		return this.width;
	}
	public void setWidth(double width)
	{
		this.width = width;
	}
	public List<Point> getEdges()
	{
		return Arrays.asList(edges);
	}
	public List<Point2D.Double> getNodes()
	{
		return Arrays.asList(nodes);
	}
}
class LetterSpace extends Letter
{
	public LetterSpace()
	{
		width = 1;
		nodes = new Point2D.Double[]{};
		edges = new Point[]{};
	}
}
class LetterA extends Letter
{
	public LetterA()
	{
		nodes =new Point2D.Double[]{new Point2D.Double(0,1), new Point2D.Double(0.2,2.0/3.0),new Point2D.Double(0.5,0),new Point2D.Double(0.8,2.0/3.0),new Point2D.Double(1,1)};
		edges = new Point[]{new Point(0,1), new Point(1,2), new Point(2,3), new Point(3,4), new Point(1,3)};
	}
}
class LetterCoil extends Letter
{
	public LetterCoil()
	{
		nodes = new Point2D.Double[]
				{
						new Point2D.Double(0,1),
						new Point2D.Double(0,1),
						new Point2D.Double(0,1),
						new Point2D.Double(0,1),
						new Point2D.Double(0,1),
						new Point2D.Double(0,1),
						new Point2D.Double(0,1),
						new Point2D.Double(0,1),
						new Point2D.Double(0,1),
						new Point2D.Double(0,1),
						new Point2D.Double(0,1),
						new Point2D.Double(0,1),
						new Point2D.Double(0,1),
						new Point2D.Double(0,1),
						new Point2D.Double(0,1)
				};
	}
}
class LetterB extends Letter
{
	public LetterB()
	{
		nodes =new Point2D.Double[]{new Point2D.Double(0,1), new Point2D.Double(0,0), new Point2D.Double(1,0.25), new Point2D.Double(1.0/3.0,0.5), new Point2D.Double(1,0.75), new Point2D.Double(2.0/3.0,1)};
		edges = new Point[]{new Point(0,1), new Point(1,2), new Point(2,3), new Point(3,4), new Point(4,5), new Point(1,4)};
	}
}
class LetterC extends Letter
{
	public LetterC()
	{
		nodes =new Point2D.Double[]{new Point2D.Double(1,0.1), new Point2D.Double(0.5,0), new Point2D.Double(0,0.5), new Point2D.Double(0.5,1), new Point2D.Double(1,0.9)};
		edges = new Point[]{new Point(0,1), new Point(1,2), new Point(2,3), new Point(3,4)};
	}
}
class LetterD extends Letter
{
	public LetterD()
	{
		nodes =new Point2D.Double[]{new Point2D.Double(0,0),new Point2D.Double(1,0.5),new Point2D.Double(0,1)};
		edges = new Point[]{new Point(0,1),new Point(1,2),new Point(2,0)};
	}
}
class LetterE extends Letter
{
	public LetterE()
	{
		nodes =new Point2D.Double[]{new Point2D.Double(1,0),new Point2D.Double(0,0),new Point2D.Double(0,0.5),new Point2D.Double(1,0.5),new Point2D.Double(0,1),new Point2D.Double(1,1)};
		edges = new Point[]{new Point(0,1), new Point(1,2), new Point(2,3), new Point(2,4), new Point(4,5)};
	}
}
class LetterF extends Letter
{
	public LetterF()
	{
		nodes =new Point2D.Double[]{new Point2D.Double(1,0), new Point2D.Double(0,0), new Point2D.Double(0,0.5), new Point2D.Double(0,1), new Point2D.Double(1,0.5)};
		edges = new Point[]{new Point(0,1), new Point(1,2), new Point(2,3), new Point(2,4)};
	}
}
class LetterG extends Letter
{
	public LetterG()
	{
		nodes =new Point2D.Double[]{new Point2D.Double(1,0.1), new Point2D.Double(0.5,0), new Point2D.Double(0,0.5), new Point2D.Double(0.5,1), new Point2D.Double(1,0.9), new Point2D.Double(1,0.5), new Point2D.Double(0.5,0.5)};
		edges = new Point[]{new Point(0,1), new Point(1,2), new Point(2,3), new Point(3,4), new Point(4,5), new Point(5,6)};
	}
}
class LetterH extends Letter
{
	public LetterH()
	{
		nodes =new Point2D.Double[]{new Point2D.Double(0,0), new Point2D.Double(0,0.5), new Point2D.Double(1,0.5), new Point2D.Double(1,1), new Point2D.Double(1,0), new Point2D.Double(0,1)};
		edges = new Point[]{new Point(0,1), new Point(1,2), new Point(2,3), new Point(2,4), new Point(1,5)};
	}
}
class LetterI extends Letter
{
	public LetterI()
	{
		nodes =new Point2D.Double[]{new Point2D.Double(0.5,0), new Point2D.Double(0.5,1)};
		edges = new Point[]{new Point(0,1)};
	}
}
class LetterJ extends Letter
{
	public LetterJ()
	{
		nodes =new Point2D.Double[]{new Point2D.Double(0,0.9), new Point2D.Double(0.5,1), new Point2D.Double(1,0.9), new Point2D.Double(1,0)};
		edges = new Point[]{new Point(0,1), new Point(1,2), new Point(2,3)};
	}
}
class LetterK extends Letter
{
	public LetterK()
	{
		nodes =new Point2D.Double[]{new Point2D.Double(0,0), new Point2D.Double(0,0.5), new Point2D.Double(1,1), new Point2D.Double(1,0), new Point2D.Double(0,1)};
		edges = new Point[]{new Point(0,1), new Point(1,2), new Point(1,3), new Point(1,4)};
	}
}
class LetterL extends Letter
{
	public LetterL()
	{
		nodes =new Point2D.Double[]{new Point2D.Double(0,0), new Point2D.Double(0,1), new Point2D.Double(1,1)};
		edges = new Point[]{new Point(0,1), new Point(1,2)};
	}
}
class LetterM extends Letter
{
	public LetterM()
	{
		width = 1.5;
		nodes =new Point2D.Double[]{new Point2D.Double(0,1), new Point2D.Double(0,0), new Point2D.Double(0.5,1), new Point2D.Double(1,0), new Point2D.Double(1,1)};
		edges = new Point[]{new Point(0,1), new Point(1,2), new Point(2,3), new Point(3,4)};
	}
}
class LetterN extends Letter
{
	public LetterN()
	{
		nodes =new Point2D.Double[]{new Point2D.Double(0,1), new Point2D.Double(0,0), new Point2D.Double(1,1), new Point2D.Double(1,0)};
		edges = new Point[]{new Point(0,1), new Point(1,2), new Point(2,3)};
	}
}
class LetterO extends Letter
{
	public LetterO()
	{
		nodes =new Point2D.Double[]{new Point2D.Double(0,0.5), new Point2D.Double(0.5,0), new Point2D.Double(1,0.5), new Point2D.Double(0.5,1)};
		edges = new Point[]{new Point(0,1), new Point(1,2), new Point(2,3), new Point(3,0)};
	}
}
class LetterP extends Letter
{
	public LetterP()
	{
		nodes =new Point2D.Double[]{new Point2D.Double(0,1), new Point2D.Double(0,0.5), new Point2D.Double(0,0), new Point2D.Double(1,0.25)};
		edges = new Point[]{new Point(0,1), new Point(1,2), new Point(2,3), new Point(3,1)};
	}
}
class LetterQ extends Letter
{
	public LetterQ()
	{
		nodes =new Point2D.Double[]{new Point2D.Double(0,0.5), new Point2D.Double(0.5,0), new Point2D.Double(1,0.5), new Point2D.Double(5.0/6.0,5.5/6.0), new Point2D.Double(0.5,1), new Point2D.Double(1 + 1.0/7.0,1 + 1.0 + 1.0 / 6.0)};
		edges = new Point[]{new Point(0,1), new Point(1,2), new Point(2,3), new Point(3,4), new Point(3,5)};
	}
}
class LetterR extends Letter
{
	public LetterR()
	{
		nodes =new Point2D.Double[]{new Point2D.Double(0,1), new Point2D.Double(0,0), new Point2D.Double(1,0.25), new Point2D.Double(0,0.5), new Point2D.Double(1,1)};
		edges = new Point[]{new Point(0,1), new Point(1,2), new Point(2,3), new Point(3,4)};
	}
}
class LetterS extends Letter
{
	public LetterS()
	{
		nodes =new Point2D.Double[]{new Point2D.Double(1.0,0.1), new Point2D.Double(0.5,0), new Point2D.Double(0,0.25), new Point2D.Double(1,0.75), new Point2D.Double(0.5,1), new Point2D.Double(0,0.9)};
		edges = new Point[]{new Point(0,1), new Point(1,2), new Point(2,3), new Point(3,4), new Point(4,5)};
	}
}
class LetterT extends Letter
{
	public LetterT()
	{
		nodes =new Point2D.Double[]{new Point2D.Double(0,0), new Point2D.Double(0.5,0), new Point2D.Double(1,0), new Point2D.Double(0.5,1)};
		edges = new Point[]{new Point(0,1), new Point(1,2), new Point(1,3)};
	}
}
class LetterU extends Letter
{
	public LetterU()
	{
		nodes =new Point2D.Double[]{new Point2D.Double(0,0), new Point2D.Double(0, 2.0/3.0), new Point2D.Double(0.5,1), new Point2D.Double(1, 2.0/3.0), new Point2D.Double(1,0)};
		edges = new Point[]{new Point(0,1), new Point(1,2), new Point(2,3), new Point(3,4)};
	}
}
class LetterV extends Letter
{
	public LetterV()
	{
		nodes =new Point2D.Double[]{new Point2D.Double(0,0), new Point2D.Double(0.5,1), new Point2D.Double(1,0)};
		edges = new Point[]{new Point(0,1), new Point(1,2)};
	}
}
class LetterW extends Letter
{
	public LetterW()
	{
		width = 1.5;
		nodes =new Point2D.Double[]{new Point2D.Double(0,0), new Point2D.Double(0.25,1), new Point2D.Double(0.5,0), new Point2D.Double(0.75,1), new Point2D.Double(1,0)};
		edges = new Point[]{new Point(0,1), new Point(1,2), new Point(2,3), new Point(3,4)};
	}
}
class LetterX extends Letter
{
	public LetterX()
	{
		nodes =new Point2D.Double[]{new Point2D.Double(0,0), new Point2D.Double(0.5,0.5), new Point2D.Double(1,0), new Point2D.Double(0,1), new Point2D.Double(1,1)};
		edges = new Point[]{new Point(0,1), new Point(1,2), new Point(1,3), new Point(1,4)};
	}
}
class LetterY extends Letter
{
	public LetterY()
	{
		nodes =new Point2D.Double[]{new Point2D.Double(0,0), new Point2D.Double(0.5,0.5), new Point2D.Double(1,0), new Point2D.Double(0.5,1)};
		edges = new Point[]{new Point(0,1), new Point(1,2), new Point(1,3)};
	}
}
class LetterZ extends Letter
{
	public LetterZ()
	{
		nodes =new Point2D.Double[]{new Point2D.Double(0,0), new Point2D.Double(1,0), new Point2D.Double(0,1), new Point2D.Double(1,1)};
		edges = new Point[]{new Point(0,1), new Point(1,2), new Point(2,3)};
	}
}
class LetterFullStop extends Letter
{
	public LetterFullStop()
	{
		nodes = new Point2D.Double[]{new Point2D.Double(0.5,1)};
		edges = new Point[]{};
	}
}