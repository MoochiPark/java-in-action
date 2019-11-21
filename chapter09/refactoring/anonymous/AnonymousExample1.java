package io.wisoft.seminar.daewon.javainaction.chapter09.refactoring.anonymous;

public class AnonymousExample1 {

  public static void main(String[] args) {
    Runnable r1 = new Runnable() {
      @Override
      public void run() {
        System.out.println("Hello, Anonymous.");
      }
    };
    Runnable r2 = () -> System.out.println("Hello, lambda.");
    r1.run();
    r2.run();
  }

}
