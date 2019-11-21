package io.wisoft.seminar.daewon.javainaction.chapter09.refactoring.templatemethod;

import java.util.function.Consumer;

public class OnlineBankingLambda {

  public void processCustomer(int id, Consumer<Integer> makeCustomerHappy) {
    // 데이터베이스에서 id로 Customer를 가져오는 코드
    makeCustomerHappy.accept(id);
  }

}
