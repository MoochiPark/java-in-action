package io.wisoft.seminar.daewon.javainaction.chapter09.refactoring.observer;

public interface Subject {

  void registerObserver(Observer o);

  void notifyObservers(String tweet);

}
