package io.wisoft.seminar.daewon.javainaction.chapter06.ex04;

import io.wisoft.seminar.daewon.javainaction.chapter06.ex02.Dish;
import org.w3c.dom.ls.LSOutput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static io.wisoft.seminar.daewon.javainaction.chapter06.ex02.Dish.menu;
import static java.util.stream.Collector.Characteristics.*;
import static java.util.stream.Collectors.toList;


public class ToListCollector<T> implements Collector<T, List<T>, List<T>> {

  @Override
  public Supplier<List<T>> supplier() {
    return ArrayList::new;
  }

  @Override
  public BiConsumer<List<T>, T> accumulator() {
    return List::add;
  }

//  @Override
//  public Function<List<T>, List<T>> finisher() {
//    return i -> i;
//  }

  public Function<List<T>, List<T>> finisher() {
    return Function.identity();
  }

  @Override
  public BinaryOperator<List<T>> combiner() {
    return (list1, list2) -> {
      list1.addAll(list2);
      return list1;
    };
  }

  @Override
  public Set<Characteristics> characteristics() {
    return Collections.unmodifiableSet(EnumSet.of(IDENTITY_FINISH, CONCURRENT, UNORDERED));
  }

  public static void main(String[] args) {
    List<Dish> dishes = menu.stream()   //기존 팩토리 메서드 사용
        .collect(toList());

    List<Dish> toCollectorDishes = menu.stream()   //new로 인스턴스화
        .collect(new ToListCollector<>());

    System.out.println(dishes);
    System.out.println(toCollectorDishes);
    //결과가 완전히 같은 것은 아니지만 사소한 최적화를 제외하면 대체로 비슷하다.

    List<Dish> simpleDishes = menu.stream()   //IDENTITY_FiNISH한 수집 연산에서는 구현을 만들지 않고도 같은 결과를 얻을 수 있다.
        .collect(ArrayList::new,
                 List::add,
                 List::addAll);

    System.out.println(simpleDishes);
  }

}
