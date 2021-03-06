package io.wisoft.seminar.daewon.javainaction.chapter06.ex05;

import static java.util.stream.Collector.Characteristics.IDENTITY_FINISH;
import static java.util.stream.Collectors.partitioningBy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class PartitionPrimeNumbers {

  public static void main(String ... args) {
    System.out.println("Numbers partitioned in prime and non-prime: " + partitionPrimes(100));
    System.out.println("Numbers partitioned in prime and non-prime: " + partitionPrimesWithCustomCollector(100));
  }

  public static Map<Boolean, List<Integer>> partitionPrimes(int n) {
    return IntStream.rangeClosed(2, n).boxed()
        .collect(partitioningBy(candidate -> isPrime(candidate)));
  }

  public static boolean isPrime(int candidate) {
    return IntStream.rangeClosed(2, candidate-1)
        .limit((long) Math.floor(Math.sqrt(candidate)) - 1)
        .noneMatch(i -> candidate % i == 0);
  }

  public static Map<Boolean, List<Integer>> partitionPrimesWithCustomCollector(int n) {
    return IntStream.rangeClosed(2, n).boxed().collect(new PrimeNumbersCollector());
  }

  public static boolean isPrime(List<Integer> primes, Integer candidate) {
    double candidateRoot = Math.sqrt(candidate);
    return primes.stream().takeWhile(i -> i <= candidateRoot).noneMatch(i -> candidate % i == 0);
  }

  public static class PrimeNumbersCollector
      implements Collector<Integer, Map<Boolean, List<Integer>>, Map<Boolean, List<Integer>>> {

    @Override
    public Supplier<Map<Boolean, List<Integer>>> supplier() {
      return () -> new HashMap<>() {{
        put(true, new ArrayList<>());
        put(false, new ArrayList<>());
      }};
    }

    @Override
    public BiConsumer<Map<Boolean, List<Integer>>, Integer> accumulator() {
      return (Map<Boolean, List<Integer>> acc, Integer candidate) -> {
        acc.get(isPrime(acc.get(true), candidate))
            .add(candidate);
      };
    }

    @Override
    public BinaryOperator<Map<Boolean, List<Integer>>> combiner() {
      return (Map<Boolean, List<Integer>> map1, Map<Boolean, List<Integer>> map2) -> {
        map1.get(true).addAll(map2.get(true));
        map1.get(false).addAll(map2.get(false));
        return map1;
      };
    }

    @Override
    public Function<Map<Boolean, List<Integer>>, Map<Boolean, List<Integer>>> finisher() {
      return i -> i;
    }

    @Override
    public Set<Characteristics> characteristics() {
      return Collections.unmodifiableSet(EnumSet.of(IDENTITY_FINISH));
    }

  }

  public Map<Boolean, List<Integer>> partitionPrimesWithInlineCollector(int n) {
    return Stream.iterate(2, i -> i + 1).limit(n)
        .collect(
            () -> new HashMap<>() {{
              put(true, new ArrayList<>());
              put(false, new ArrayList<>());
            }},
            (acc, candidate) -> {
              acc.get(isPrime(acc.get(true), candidate))
                  .add(candidate);
            },
            (map1, map2) -> {
              map1.get(true).addAll(map2.get(true));
              map1.get(false).addAll(map2.get(false));
            }
        );
  }

}
