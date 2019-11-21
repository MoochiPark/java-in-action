package io.wisoft.seminar.daewon.javainaction.chapter09.refactoring.templatemethod;

abstract class OnlineBanking {

  public void processCustomer(int id) {
    // 데이터베이스에서 id로 Customer를 가져오는 코드
    makeCustomerHappy(id);
  }

  abstract void makeCustomerHappy(int id);

}
