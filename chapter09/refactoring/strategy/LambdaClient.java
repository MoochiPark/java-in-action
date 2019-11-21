package io.wisoft.seminar.daewon.javainaction.chapter09.refactoring.strategy;

public class LambdaClient {

  public static void main(String[] args) {
    Validator numericValidator = new Validator((String s) -> s.matches("[a-z]+"));
    System.out.println(numericValidator.validate("aaaa"));

    Validator lowerCaseValidator = new Validator((String s) -> s.matches("\\d+"));
    System.out.println(lowerCaseValidator.validate("bbbb"));

    // ValidationStrategy는 함수형  인터페이스며 Predicate<String>과 같은 함수 디스크립터를 갖고 있다.
    // 때문에 다양한 전략을 구현하는 클래스를 구현할 필요없이 람다 표현식을 직접 전달하면 코드가 간결해진다.
  }

}
