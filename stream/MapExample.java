package io.wisoft.seminar.daewon.javainaction.stream;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class MapExample {

  public static void main(String[] args) {

    // Quiz 1
    List<Integer> numbers = List.of(1, 2, 3, 4, 5);
    List<Double> answer1 = numbers.stream()
        .map(n -> Math.pow(n, 2))
        .collect(toList());

    System.out.println(answer1);

    // Quiz 2
    List<Integer> numbers1 = List.of(1, 2, 3);
    List<Integer> numbers2 = List.of(3, 4);

    List<int[]> answer2 = numbers1.stream()
        .flatMap(n1 -> numbers2.stream().map(n2 -> new int[] {n1, n2}))
        .collect(toList());

    answer2.forEach(list -> System.out.println(list[0] + ", " + list[1]));

    // Quiz 3
    List<int[]> answer3 = numbers1.stream()
        .flatMap(n1 -> numbers2.stream()
            .map(n2 -> new int[] {n1, n2})
            .filter(n2 -> (n2[0] + n2[1]) % 3 == 0))
        .collect(toList());

    answer3.forEach(s -> System.out.println(s[0] + ", " + s[1]));
  }

}
