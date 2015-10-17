package org.vanted.animation.data;

import java.awt.geom.Point2D;

public class Point2DTimePoint extends TimePoint<Point2D> {
	 public Point2DTimePoint(double time,double x, double y)
	 {
		 super(time, new Point2D.Double(x,y));
	 }
	 public Point2DTimePoint(double time, Point2D point)
	 {
		 super(time, point);
	 }
	 public double getX()
	 {
		 return dataValue.getX();
	 }
	 public double getY()
	 {
		 return dataValue.getY();
	 }
	 public void setX(double x)
	 {
		 dataValue.setLocation(x, this.getY());
	 }
	 public void setY(double y)
	 {
		 dataValue.setLocation(this.getX(),y);
	 }
	 @Override
	 public String toString()
	 {
		 return "Time: " + Double.toString(getTime())+ " , Position: " + dataValue.toString();
	 }
	@Override
	public double[] getDoubleValues() {
		return new double[] {dataValue.getX(),dataValue.getY()};
	}
	@Override
	public Point2D toDataValue(double[] doubleValues) {
		return new Point2D.Double(doubleValues[0],doubleValues[1]);
	}
}
