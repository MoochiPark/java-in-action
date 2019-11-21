package io.wisoft.seminar.daewon.javainaction.chapter09.refactoring.strategy;

public class Validator {

  private final ValidationStrategy strategy;

  public Validator(ValidationStrategy v) {
    this.strategy = v;
  }

  public boolean validate(String s) {
    return strategy.execute(s);
  }

}
