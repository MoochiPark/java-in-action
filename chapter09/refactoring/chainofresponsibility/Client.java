package io.wisoft.seminar.daewon.javainaction.chapter09.refactoring.chainofresponsibility;

import java.util.function.Function;
import java.util.function.UnaryOperator;

public class Client {

  public static void main(String[] args) {
//    ProcessingObject<String> p1 = new HeaderTextProcessing();
//    ProcessingObject<String> p2 = new SpellCheckerProcessing();
//    p1.setSuccessor(p2); // 두 작업 처리 객체를 연결한다.
//    String result = p1.handle("Aren't labdas really sexy?!!");
    // -> 이 패턴은 함수 체인(함수 조합 ex:andThen)과 비슷해보인다.
    UnaryOperator<String> headerProcessing =
        (String text) -> "From Raoul, Mario and Alan: " + text;
    UnaryOperator<String> spellCheckerProcessing =
        (String text) -> text.replaceAll("labda", "lambda");
    Function<String, String> pipeline =
        headerProcessing.andThen(spellCheckerProcessing);
    String result = pipeline.apply("Aren't labdas really sexy!!?");
    System.out.println(result);
  }
}
