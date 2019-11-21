package io.wisoft.seminar.daewon.javainaction.chapter07;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Test {

  public static void main(String[] args) {

    List<Integer> integers = new ArrayList<>();
    integers.add(1);
    integers.add(2);
    integers.add(3);
    integers.add(4);
    Iterator<Integer> iterator = integers.iterator();
    System.out.println(integers);

//    while (iterator.hasNext()) {
//      int number = iterator.next();
//      if (number == 1) {
//        integers.remove(0);
//      }
////      System.out.println(number);
//      System.out.println(integers);
//    }
//  }

    for (int i : integers) {
      integers.remove(i);
    }
  }
}
