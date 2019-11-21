package io.wisoft.seminar.daewon.javainaction.chapter09.refactoring.methodreference;

import io.wisoft.seminar.daewon.javainaction.chapter06.ex02.Dish;

import java.util.List;
import java.util.Map;

import static io.wisoft.seminar.daewon.javainaction.chapter06.ex02.Dish.menu;
import static io.wisoft.seminar.daewon.javainaction.chapter06.ex02.Grouping.*;
import static java.util.stream.Collectors.groupingBy;

public class MethodReferenceExample {

  private static Map<CaloricLevel, List<Dish>> groupDishesByCaloricLevel() {
    return menu.stream().collect(
        groupingBy(dish -> {
          if (dish.getCalories() <= 400) {
            return CaloricLevel.DIET;
          } else if (dish.getCalories() <= 700) {
            return CaloricLevel.NORMAL;
          } else {
            return CaloricLevel.FAT;
          }
        })
    );
  }

  private static Map<CaloricLevel, List<Dish>> groupDishesByCaloricLevelUseMethodReference() {
    return menu.stream().collect(groupingBy(Dish::getCaloricLevel));
  }

  public static void main(String[] args) {
    System.out.println("Dishes grouped by caloric level: " + groupDishesByCaloricLevel());
    System.out.println("Dishes grouped by caloric level use method Reference : " + groupDishesByCaloricLevelUseMethodReference());
  }

}
