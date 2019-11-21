package io.wisoft.seminar.daewon.javainaction.chapter09.refactoring.anonymous;

public class TaskExample {

  public static void doSomething(Runnable r) { r.run(); }

  public static void doSomething(Task t) { t.execute(); }

  public static void main(String[] args) {
    doSomething(new Task() {
      @Override
      public void execute() {
        System.out.println("Danger! danger!! [Anonymous]");
      }
    });
//    doSomething(() -> System.out.println("Danger! danger!!"));
    doSomething((Task) () -> System.out.println("Danger! danger!! [Task]"));
    doSomething((Runnable) () -> System.out.println("Danger! danger!! [Runnable]"));
  }

}
