package io.wisoft.seminar.daewon.javainaction.chapter09.testing;

import java.util.Comparator;
import java.util.List;

import static java.util.Comparator.*;
import static java.util.stream.Collectors.*;

public class Point {

  private final int x;
  private final int y;

  public Point(final int x, final int y) {
    this.x = x;
    this.y = y;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public Point moveRightBy(final int x) {
    return new Point(this.x + x, this.y);
  }

  public final static Comparator<Point> compareByXAndThenY =
      comparing(Point::getX).thenComparing(Point::getY);

  public static List<Point> moveAllPointsRightBy(List<Point> points, int x) {
    return points.stream()
        .map(p -> new Point(p.getX() + x, p.getY()))
        .collect(toList());
  }

}