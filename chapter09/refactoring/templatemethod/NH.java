package io.wisoft.seminar.daewon.javainaction.chapter09.refactoring.templatemethod;

public class NH extends OnlineBanking {

  @Override
  void makeCustomerHappy(int id) {
    System.out.println(id + "번 고객님 안녕하세요.");
  }

  public static void main(String[] args) {
    new NH().processCustomer(1234);
  }

}
