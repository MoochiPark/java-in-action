package io.wisoft.seminar.daewon.javainaction.chapter09.refactoring.templatemethod;

public class Kakao {

  public static void main(String[] args) {
    new OnlineBankingLambda().processCustomer(1337, (Integer id) -> System.out.println("Hello " + id));
  }

}
