package org.vanted.animation.data;

import java.awt.geom.Point2D;

public class Point2DPoint extends TimePoint {
	 Point2D point;
	 public Point2DPoint(double time,double x, double y)
	 {
		 super(time);
		 this.point = new Point2D.Double(x,y);
	 }
	 public void setPos(double time, Point2D point)
	 {
		 this.point = point;
	 }
	 public double getX()
	 {
		 return this.point.getX();
	 }
	 public double getY()
	 {
		 return this.point.getY();
	 }
	 public Point2D getPoint()
	 {
		 return this.point;
	 }
	 public void setX(double x)
	 {
		 this.point.setLocation(x, this.getY());
	 }
	 public void setY(double y)
	 {
		 this.point.setLocation(this.getX(),y);
	 }
	 public void setPoint(Point2D point)
	 {
		 this.point = point;
	 }
	 @Override
	 public String toString()
	 {
		 return "Time: " + Double.toString(getTime())+ " , Position: " + point.toString();
	 }
}
