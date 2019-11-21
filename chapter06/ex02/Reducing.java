package io.wisoft.seminar.daewon.javainaction.chapter06.ex02;

import java.util.Map;

import static io.wisoft.seminar.daewon.javainaction.chapter06.ex02.Dish.menu;
import static java.util.stream.Collectors.*;


public class Reducing {

  public String shortMenu() {
    return menu.stream()
        .collect(reducing("", Dish::getName,( (d1, d2) -> d1 + d2)) );

  }

  public static void main(String[] args) {
    }

}
