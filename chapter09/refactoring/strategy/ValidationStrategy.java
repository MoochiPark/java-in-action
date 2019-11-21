package io.wisoft.seminar.daewon.javainaction.chapter09.refactoring.strategy;

public interface ValidationStrategy {

  boolean execute(String s);

}

//문자열 검증 인터페이스.