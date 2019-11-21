package io.wisoft.seminar.daewon.javainaction.chapter09.refactoring.strategy;

public class Client {

  public static void main(String[] args) {
    Validator numericValidator = new Validator(new IsNumeric());
    System.out.println(numericValidator.validate("aaaa"));

    Validator lowerCaseValidator = new Validator(new IsAllLowerCase());
    System.out.println(lowerCaseValidator.validate("bbbb"));
  }

}
