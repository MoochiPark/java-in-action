package io.wisoft.seminar.daewon.javainaction.chapter09.debugging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Debugging {

  public static void main(String[] args) {
    List<Point> points = Arrays.asList(new Point(12, 2), null);
    points.stream()
        .map(Point::getX)
        .forEach(System.out::println);
  }

  private static class Point {

    private int x;
    private int y;

    private Point(int x, int y) {
      this.x = x;
      this.y = y;
    }

    public int getX() {
      return x;
    }

    public void setX(int x) {
      this.x = x;
    }

  }

}
//메서드 참조 전
//디버깅에서의 이상한 문자는 람다 표현식 내부에서 에러가 발생했음을 가리킨다.
//람다 표현식은 이름이 없으므롤 컴파일러가 람다를 참조하는 이름을 만들어낸다.
//여러 클래스에서 람다 표현식이 있을 때는 꽤 골치 아픈 일이 벌어진다.

//참조 후
//메서드 참조를 사용해도 스택 트레이스는 메서드명이 나타나지 않는다.

