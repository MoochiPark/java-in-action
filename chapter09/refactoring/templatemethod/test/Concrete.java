package io.wisoft.seminar.daewon.javainaction.chapter09.refactoring.templatemethod.test;

public class Concrete extends TemplateMethod {

  public void use() {
    templateMethod();
    subMethod();
  }

  public void subMethod() {
    System.out.println("ttt");
  }

  public static void main(String[] args) {
    Concrete concrete = new Concrete();
    concrete.use();
  }

}

