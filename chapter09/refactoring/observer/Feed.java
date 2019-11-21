package io.wisoft.seminar.daewon.javainaction.chapter09.refactoring.observer;

import java.util.ArrayList;
import java.util.List;

public class Feed implements Subject {
  //트윗을 받았을 때 알림을 보낼 옵저버 리스트를 유지.

  private final List<Observer> observers = new ArrayList<>();

  @Override
  public void registerObserver(Observer o) {
    this.observers.add(o);
  }

  @Override
  public void notifyObservers(String tweet) {
    observers.forEach(o -> o.notify(tweet));
  }

  public static void main(String[] args) {
    Feed f = new Feed();
//    f.registerObserver(new NYTimes());
//    f.registerObserver(new Guardian());
//    f.registerObserver(new LeMonde());
//    f.notifyObservers("The queen said her favorite book is Modern Java in Action!");
//  }
    f.registerObserver((String tweet) -> {
      if (tweet != null && tweet.contains("money")) {
        System.out.println("Breaking news in NY! " + tweet);
      }
    });
    f.registerObserver((String tweet) -> {
      if (tweet != null && tweet.contains("wine")) {
        System.out.println("Today is cheese, wine and news! " + tweet);
      }
    });
    f.notifyObservers("The queen said her favorite money.");
  }

}
