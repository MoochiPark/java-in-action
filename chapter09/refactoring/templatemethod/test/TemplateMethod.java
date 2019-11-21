package io.wisoft.seminar.daewon.javainaction.chapter09.refactoring.templatemethod.test;

abstract class TemplateMethod {

  public void templateMethod() {
    System.out.println("test");
  }

  protected void subMethod() {
    System.out.println("sub-test");
  }

}
