package io.wisoft.seminar.daewon.javainaction.chapter01;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class Example {

  private int compareUsingCustomerId(String inv1, String inv2) {
    return inv1.compareTo(inv2);
  }

  public int solution(int n) {
    return Arrays.stream(String.valueOf(n).split(""))
        .mapToInt(Integer::parseInt)
        .sum();
  }

  public static void main(String[] args) {

    List<String> songjangs = new ArrayList<>(List.of("2013UK0003", "2012US0002", "2015KR0001"));
    songjangs.sort(new Example()::compareUsingCustomerId);
    System.out.println(songjangs);
    List<String> songjangs1 = new ArrayList<>(List.of("2013UK0003", "2012US0002", "2015KR0001"));
    songjangs1.sort(Comparator.comparing(s -> s.substring(0, 4)));
    System.out.println(songjangs1);
  }


}
