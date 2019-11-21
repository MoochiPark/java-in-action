package io.wisoft.seminar.daewon.javainaction.chapter09.refactoring.strategy;

public class IsNumeric implements ValidationStrategy {

  @Override
  public boolean execute(String s) {
    return s.matches("\\d+");
  }

}