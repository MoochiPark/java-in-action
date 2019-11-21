package io.wisoft.seminar.daewon.javainaction.chapter09.refactoring.chainofresponsibility;

public class HeaderTextProcessing extends ProcessingObject<String> {

  @Override
  public String handleWork(String text) {
    return "From Raoul, Mario and Alan: " + text;
  }

}
