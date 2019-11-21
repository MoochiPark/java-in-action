package io.wisoft.seminar.daewon.javainaction.chapter09.refactoring.anonymous;

public class AnonymousExample2 {

  public static void main(String[] args) {
    int a = 10;
    Runnable r1 = () -> {
//      int a = 2; //람다 표현식에서는 변수를 가릴 수 없다.
      System.out.println(a);
    };

    Runnable r2 = new Runnable() {
      @Override
      public void run() {
        int a = 2; //섀도 변수(shadow variable). 감싸고 있는 클래스의 변수를 가릴 수 있다.
        System.out.println(a);
      }
    };

    r1.run();
    r2.run();
  }

}
