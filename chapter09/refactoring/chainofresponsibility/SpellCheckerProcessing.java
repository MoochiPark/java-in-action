package io.wisoft.seminar.daewon.javainaction.chapter09.refactoring.chainofresponsibility;

public class SpellCheckerProcessing extends ProcessingObject<String> {

  @Override
  String handleWork(String text) {
    return text.replaceAll("labda", "lambda");
  }

}
