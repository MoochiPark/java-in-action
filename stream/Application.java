package io.wisoft.seminar.daewon.javainaction.stream;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class Application {

  public static void main(String[] args) {

    Trader raoul = new Trader("Raoul", "Cambridge");
    Trader mario = new Trader("Mario", "Milan");
    Trader alan = new Trader("Alan", "Cambridge");
    Trader brian = new Trader("Brain", "Cambridge");
    List<Transaction> transactions = Arrays.asList(
        new Transaction(brian, 2011, 300),
        new Transaction(raoul, 2012, 1000),
        new Transaction(raoul, 2011, 400),
        new Transaction(mario, 2012, 710),
        new Transaction(mario, 2012, 700),
        new Transaction(alan, 2012, 950)
    );

    //Quiz 1
    List<Transaction> transactions2011 = transactions.stream()
        .filter(t -> t.getYear() == 2011)
        .sorted(Comparator.comparing(Transaction::getValue))
        .collect(toList());

    transactions2011.forEach(s -> System.out.println(s.getValue()));
    System.out.println();

    //Quiz 2
    List<String> allCities = transactions.stream()
        .map(t -> t.getTrader().getCity())
        .distinct()
        .collect(toList());

    allCities.forEach(System.out::println);
    System.out.println();

    //Quiz 3
    List<String> cambWorkers = transactions.stream()
        .filter(t -> t.getTrader().getCity().equals("Cambridge"))
        .map(t -> t.getTrader().getName())
        .distinct()
        .sorted()
        .collect(toList());

    cambWorkers.forEach(System.out::println);

    IntStream.range(1, 10).forEach(System.out::println);
    IntStream.rangeClosed(1, 10).forEach(System.out::println);

  }

}
