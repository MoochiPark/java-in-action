# Chapter 07. 병렬 데이터 처리와 성능

> ***이 장의 내용***
>
> - 병렬 스트림으로 데이터를 병렬 처리하기
> - 병렬 스트림의 성능 분석
> - 포크/조인 프레임워크
> - Spliterator로 스트림 데이터 쪼개기



자바 7 이전에는 데이터 컬렉션을 병렬로 처리하기 어려웠다. 

> 1. 우선 데이터를 서브파트로 분할한다.
> 2. 서브파트를 각각의 스레드로 할당한다.
> 3. 의도치 않은 경쟁 조건<sup>race condition</sup>이 발생하지 않도록 적절한 동기화를 추가한다.
> 4. 부분 결과를 합친다.

자바 7부터 더 쉽게 병렬화와 에러를 최소화할 수 있는 **포크/조인 프레임워크<sup>fork/join framework</sup>** 를 제공한다.

스트림을 이용하면 순차 스트림을 병렬 스트림으로 자연스럽게 바꿀 수 있는데, 어떻게 이것들이 가능한지, 
포크/조인 프레임워크와 내부적인 병렬 처리는 어떤 관계가 있는지 이 장에서 배울 것이다.

병렬 스트림이 내부적으로 어떻게 처리되는지 알아야만 스트림을 잘못 사용하는 상황을 피할 수 있다.



## 7.1 병렬 스트림

병렬 스트림이란 각각의 스레드에서 처리할 수 있도록 스트림 요소를 여러 청크<sup>데이터 조각</sup>로 분할한 스트림이다.



> *1부터 n까지 모든 숫자의 합계를 구하는 코드*

```java
public long sequentialSum(long n) {
  return Stream.iterate(1L, i -> i + 1) {
    					 .limit(n)
               .reduce(0L, Long::sum);
  }
}
```

위 코드에서 n이 커진다면 병렬로 처리하는 것이 좋을 것이다. 결과 변수 동기화, 스레드의 수, 어떻게 더할지 등
다양한 문제점을 병렬 스트림을 이용하면 걱정 없이 모든 문제를 쉽게 해결할 수 있다.



### 7.1.1 순차 스트림을 병렬 스트림으로 변환하기

---

> *1부터 n까지 모든 숫자의 합계를 병렬처리로 구하는 코드*

```java
public long sequentialSum(long n) {
  return Stream.iterate(1L, i -> i + 1) {
    					 .limit(n)
               .parallel()
               .reduce(0L, Long::sum);
  }
}
```

이전의 코드와 다른 점은 스트림이 여러 청크로 분할되어 있다는 것이다.

![image](https://user-images.githubusercontent.com/43429667/73665731-f1b6d380-46e4-11ea-932c-1a7fb64d8c07.png)

사실 순차 스트림에 `parallel()`을 호출해도 스트림 자체에는 아무 변화도 일어나지 않는다. 내부적으로 이후 연산이 병렬로 수행해야 함을 의미하는 `boolean` 플래그가 설정된다. 반대로 `sequential()`로 병렬 스트림을 순차 스트림으로 바꿀 수 있다.



### 7.1.2 스트림 성능 측정

---

병렬화를 이용하면 순차나 반복 형식에 비해 성능이 더 좋아질 것이라고 추측했다. 하지만 소프트웨어 공학에서
추측은 위험한 방법이다. 따라서 측정을 위해 **자바 마이크로벤치마크 하니스<sup>Java Microbenchmark Harness</sup>(JMH)**라는 
라이브러리를 통해 작은 벤치마크를 구현해보자. JMH는 간단하고 어노테이션 방식을 지원하며 안정적으로 자바 프로그램이나 JVM을 대상으로 하는 다른 언어용 벤치마크를 구현할 수 있다.



> *sequentialSum (순차 리듀싱)*

```java
package jmh;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(value = 2, jvmArgs = {"-Xms4G", "Xmx4G"})
public class ParallelStreamBenchmark {

  private static final long N = 10_000_000L;

  @Benchmark
  public long sequentialSum() {
    return Stream.iterate(1L, i -> i + 1)
        .limit(N)
        .reduce(0L, Long::sum);
  }

  @TearDown(Level.Invocation)
  public void tearDown() {
    System.gc();
  }

}
```

![image](https://user-images.githubusercontent.com/43429667/73676653-19fbfd80-46f8-11ea-9878-1d011e14d929.png)



for 루프를 사용해 반복하는 방법이 더 저수준으로 동작할 뿐 아니라 기본값의 박싱, 언박싱이 일어나지 않으므로
더 빠를 것이라 예상할 수 있다.

> *iterativeSum (for loop)*

```java
@Benchmark
  public long iterativeSum() {
    long result = 0;
    for (long i = 1L; i <= N; i++) {
      result += i;
    }
    return result;
  }
```

![image](https://user-images.githubusercontent.com/43429667/73676700-2bdda080-46f8-11ea-9be8-70325fd40e05.png)

예상대로 순차적 스트림을 사용하는 버전에 비해 거의 40배가 빠르다는 것을 확인할 수 있다.



> *parallelSum (병렬 스트림)*

![image](https://user-images.githubusercontent.com/43429667/73676752-43b52480-46f8-11ea-83f5-ccb26308ccee.png)



병렬 스트림이 순차 스트림에 비해 다섯 배나 느린 실망스런 결과가 나왔다. 여기서 두 가지 문제를 발견할 수 있다.

- 반복 결과로 박싱된 객체가 만들어지므로 숫자를 더하려면 언박싱을 해야한다.
- 반복 작업은 병렬로 수행할 수 있는 독립 단위로 나누기가 어렵다.



우리에겐 병렬로 수행될 수 있는 스트림 모델이 필요하기 때문에 두 번째 문제는 예사롭게 넘길 수 없다.

![image](https://user-images.githubusercontent.com/43429667/73676783-52034080-46f8-11ea-84b3-8b9d5ce26e2a.png)

위 그림처럼 이전 연산의 결과에 따라 다음 함수의 입력이 달라지기 iterate 연산은 청크로 분할하기가 어렵다.

이와 같은 상황은 리듀싱 연산이 수행되지 않는다. (병렬 리듀싱 연산 그림 참고) 리듀싱 과정을 시작하는 시점에 전체 숫자 리스트가 준비되어있지 않았기 때문이다. 결국 순차처리 방식과 크게 다른 점이 없어 스레드를 할당하는 오버헤드만 증가하는 결과를 초래한다. 이처럼 병렬 프로그래밍은 까다롭기 때문에 내부 구조를 이해해야 한다.



#### 더 특화된 메서드 사용

> ***LongStream.rangeClosed***

- LongStream.rangeClosed는 기본형 long을 직접 사용하므로 박싱과 언박싱 오버헤드가 사라진다.
- LongStream.rangeClosed는 쉽게 청크로 분할할 수 있는 숫자 범위를 생산한다. 예를 들어 1-20 범위의 숫자를 각각 1-5, 6-10, 11-15, 16-20 범위의 숫자로 분할할 수 있다.



> *parallelRangedSum (병렬 특화 스트림)*

```java
@Benchmark
public long parallelRangedSum() {
  return LongStream.rangeClosed(1, N)
    							 .parallel()
    							 .reduce(0L, Long::sum);
}
```

![image](https://user-images.githubusercontent.com/43429667/73676814-5def0280-46f8-11ea-8c99-bf7dba5c1a12.png)



드디어 실질적으로 리듀싱 연산이 병렬로 실행되어 순차 실행보다 빠른 성능을 갖게 되었다. 

하지만 병렬화가 완전 공짜는 아니라는 사실을 기억하자. 병렬화를 이용하려면 스트림을 재귀적으로 분할해야하고, 각 서브스트림을 서로 다른 스레드의 리듀싱 연산으로 할당하고, 이들 결과를 하나의 값으로 합쳐야 한다.
멀티코어 간의 데이터 전송 시간보다 훨씬 오래걸리는 작업만 병렬로 다른 코어에서 수행하는 것이 바람직하다.



### 7.1.3 병렬 스트림의 올바른 사용법

병렬 스트림을 잘못 사용하면서 발생하는 많은 문제는 공유된 상태를 바꾸는 알고리즘을 사용하기 때문에 생긴다.

```java
  public long sideEffectSum(long n) {
    Accumulator accumulator = new Accumulator();
    LongStream.rangeClosed(1, n).forEach(accumulator::add);
    return accumulator.total;
  }
  
  public class Accumulator {
    public long total = 0;

    public void add(long value) {
      total += value;
    }
  }
```

코드에 문제는 없어보이지만 위 코드는 본질적으로 순차 실행할 수 있도록 구현되어 있으므로 병렬로 실행하면 
참사가 일어난다. 특히 `total`을 접근할 때마다 데이터 레이스 문제가 일어난다. 이러한 동기화 문제를 고치다보면 결국 병렬화라는 특성이 없어져 버릴 것이다. 

```java
  public long sideEffectParallelSum(long n) {
    Accumulator accumulator = new Accumulator();
    LongStream.rangeClosed(1, n).parallel().forEach(accumulator::add);
    return accumulator.total;
  }

    System.out.println("SideEffect parallel sum done in: " + measurePerf(ParallelStreams::sideEffectParallelSum, 10_000_000L) + " msecs" );
```

![image](https://user-images.githubusercontent.com/43429667/73677597-f3d75d00-46f9-11ea-895f-a3f1e4284c68.png)

올바른 결과값(50000005000000)과 다른 결과가 나온다. 여러 스레드에서 동시에 누적자, 즉 `total += value`를 실행하면서 이런 문제가 발생한다.  병렬 스트림을 사용할 땐 상태 공유에 따른 부작용을 피해야만 한다.

 

### 7.1.4 병렬 스트림 효과적으로 사용하기

`천 개 이상의 요소가 있을 때만 병렬 스트림을 사용하자`같이 양을 기준으로 병렬 스트림의 사용을 결정하는 것은 적절하지 않다. 정해진 기기에서 정해진 연산을 수행할 때는 가능하지만 상황이 달라지면 이와 같은 기준이 제 역할을 하지 못하기 때문이다.

- 확신이 서지 않는다면 직접 측정하라.

- 박싱을 주의하라. 기본형 특화 스트림을 이용하는 것이 좋다.

- 순차 스트림보다 병렬 스트림에서 성능이 떨어지는 연산이 있다. *ex) `limit`, `findFirst` 등*

- 스트림에서 수행하는 전체 파이프라인 연산 비용을 고려하라.

  *처리해야 할 요소 수가 N이고 하나의 요소를 처리하는데 드는 비용이  Q이면 전체 처리 비용을 NxQ로 예상할 수 있다. Q가 높아진다는 것은 병렬 스트림으로 성능을 개선할 수 있는 가능성이 있음을 의미*

- 소량의 데이터에서는 병렬 스트림이 도움 되지 않는다.

- 스트림의 특성과 파이프라인의 중간 연산이 스트림의 특성을 어떻게 바꾸는지에 따라 분해 과정의 성능이 달라질 수 있다. Ranged 스트림은 정확히 같은 크기의 두 스트림으로 분할 할 수 있어 효과적으로 스트림을 병렬 처리 할 수 있지만 만약 필터 연산이 있으면 스트림의 길이를 예측할 수 없어 성능 또한 예측할 수 없게 된다.

- 최종 연산의 병합 과정 비용을 살펴보라. 병합 과정의 비용이 비싸다면 병렬 스트림으로 얻는 성능의 이익이 서브스트림의 부분 결과를 합치는 과정에서 상쇄된다. *ex) `Collector`의 `combiner()`*

- 스트림을 구성하는 자료구조가 적절한지 확인하라.

![image-20200204031023722](C:\Users\Admin\AppData\Roaming\Typora\typora-user-images\image-20200204031023722.png)





## 7.2 포크/조인 프레임워크

포크/조인 프레임워크는 병렬화할 수 있는 작업을 재귀적으로 작은 작업으로 분할한 다음에 서브태스크 각각의 결과를 합쳐서 전체 결과를 만들도록 설계되었다.



### 7.2.1. RecursiveTask 활용

스레드 풀을 이용하려면 **RecursiveTask<R>**의 서브클래스를 만들어야 한다. RecursiveTask를 정의하려면 추상 메서드 **compute를 구현해야 한다.**

```java
protected abstract R compute();
```

- **R** : 병렬화된 태스크가 생성하는 결과 형식 또는 결과가 없을 때는 RecursiveAction 형식이다.



**compute 메서드는** 태스크를 서브태스크로 분할하는 로직과 더 이상 분할할 수 없을 때 개별 서브태스크의 결과를 생산할 알고리즘을 정의한다.

- **compute 메서드 구현 의사코드**

  ```java
  if (태스크가 충분히 적거나 더 이상 분할할 수 없으면) {
    순차적으로 태스크 계산
  } else {
    태스크를 두 서브태스크로 분할
    태스크가 다시 서브태스크로 분할되도록 이 메서드를 재귀적으로 호출함
    모든 서브태스크의 연산이 완료될 때까지 기다림
    각 서브태스크의 결과를 합침
  }
  ```

  - 이 알고리즘은 **분할 후 정복<sup>divide-and-conquer</sup>** 알고리즘의 병렬화 버전이다.



- **포크/조인 과정**

  ![img](https://camo.githubusercontent.com/cfe2a17bcc9374ccfec7c18e9abc2d21a4fba47a/68747470733a2f2f74312e6461756d63646e2e6e65742f6366696c652f746973746f72792f323234414444333735383844393134313230)



>*포크/조인 프레임워크를 이용해서 병렬 합계 수행*

```java
                                     // RecursiveTask 를 상속받아 포크/조인 프레임워크에서 사용할
// 태스크를 생성한다.
public class ForkJoinSumCalculator extends java.util.concurrent.RecursiveTask<Long> {

  private final long[] numbers;  // 더할 숫자 배열
  private final int start;       // 이 서브태스크에서 처리할 배열의 초기 위치 
  private final int end;         // 최종 위치
  public static final long THRESHOLD = 10_000;  // 이 값 이하의 서브태스크는 더 이상 분할X

  // 메인 태스크를 생성할 때 사용할 공개 생성자
  public ForkJoinSumCalculator(long[] numbers) {
    this(numbers, 0, numbers.length);
  }

  // 메인 태스크의 서브태스크를 재귀적으로 만들 때 사용할 비공개 생성자
  private ForkJoinSumCalculator(long[] numbers, int start, int end) {
    this.numbers = numbers;
    this.start = start;
    this.end = end;
  }

  // RecursiveTask 의 추상 메서드 오버라이드
  @Override
  protected Long compute() {
    int length = end - start;       // 이 태스크에서 더할 배열의 길이

    if (length <= THRESHOLD) {
      // 기준값과 같거나 작으면 순차적으로 결과를 계산한다.
      return computeSequentially();
    }

    // 배열의 첫 번째 절반을 더하도록 서브태스크를 생성한다.  
    ForkJoinSumCalculator leftTask =
      new ForkJoinSumCalculator(numbers, start, start + length/2);
    leftTask.fork();  // ForkJoinPool 의 다른 스레드로 새로 생성한 태스크를 비동기로 실행

    // 배열의 나머지 절반을 더하도록 서브태스크를 생성
    ForkJoinSumCalculator rightTask =
      new ForkJoinSumCalculator(numbers, start + length/2, end);
    // 두 번째 서브태스크를 동기 실행한다.
    // 이때 추가로 분할이 일어날 수 있다.
    Long rightResult = rightTask.compute();
    // 첫 번째 서브태스크의 결과를 읽거나
    // 아직 결과가 없으면 기다린다.
    Long leftResult = leftTask.join();

    // 두 서브태스크의 결과를 조합한 값이 태스크의 결과다
    return leftResult + rightResult;
  }

  // 더 분할할 수 없을 때 서브태스크의 결과를 계산
  private long computeSequentially() {
    long sum = 0;
    for (int i = start; i < end; i++) {
      sum += numbers[i];
    }
    return sum;
  }

}
```

- **n 까지의 배열을 생성해 계산하는 메소드**

  ```java
  public static long forkJoinSum(long n) {
    // n까지의 자연수를 포함하는 배열 생성
    long[] numbers = LongStream.rangeClosed(1, n).toArray();
    // 배열을 생성자로 전달해 태스크 생성
    ForkJoinTask<Long> task = new ForkJoinSumCalculator(numbers);
    // invoke 메서드를 통해 태스크의 결과를 반환
    return new ForkJoinPool().invoke(task);
  }
  ```

일반적으로 애플리케이션에서는 둘 이상의 ForkJoinPool을 사용하지 않는다. 즉, 소프트웨어의 필요한 곳에서 
언제든 가져다 쓸 수 있도록 ForkJoinPool을 한 번만 인스턴스화해서 정적 필드에 **싱글턴**으로 저장한다.



#### ForkJoinSumCalculator 실행

1. ForkJoinSumCalculator를 ForkJoinPool로 전달하면 풀의 스레드가 ForkJoinSumCalculator의 compute 메서드를 실행하면서 작업 수행
2. compute 메서드는 태스크를 주어진 조건을 만족할 때(항목 10000 이하)까지 반으로 나누면서 두 개의 새로운 ForkJoinSumCalculator로 할당하는 것을 재귀적으로 실행한다.
3. 나눠진 각 서브태스크는 순차적으로 처리되며 포킹 프로세스로 만들어진 이진트리의 태스크를 루트에서 역순으로 방문한다(계산한다).
4. 서브태스크의 부분 결과를 합쳐서 태스크의 최종 결과를 계산한다.



- **포크/조인 알고리즘**

[![img](https://github.com/LeeSM0518/modern-java/raw/master/src/main/java/capture/%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7%202019-09-24%20%EC%98%A4%ED%9B%84%208.45.23.png)](https://github.com/LeeSM0518/modern-java/blob/master/src/main/java/capture/스크린샷 2019-09-24 오후 8.45.23.png)

JMH로 포크/조인 프레임워크의 합계 메서드 성능을 확인해보면,

```java
System.out.println("ForkJoin sum done in: " + measureSumPerf(
	ForkJoinSumCalculator::forkJoinSum, 10_000_000) + " msecs" );
```

![image](https://user-images.githubusercontent.com/43429667/73713434-b9e57580-4750-11ea-9420-744230d5261e.png)

병렬 스트림을 이용할 때보다 성능이 나빠졌지만, 이것은 ForkJoinSumCalculator 태스크에서 사용할 수 있도록
전체 스트림을 long[]으로 변환하는 시간이 포함되었기 때문이다.



### 7.2.2. 포크/조인 프레임워크를 제대로 사용하는 방법

포크/조인 프레임워크는 쉽게 사용할 수 있지만 항상 주의를 기울여야 한다.

- join 메서드를 태스크에 호출하면 태스크가 생산하는 결과가 준비될 때까지 호출자를 블록시킨다. 따라서 두 서브태스크가 모두 시작된 다음에 join을 호출해야 한다. 그렇지 않으면 각각의 서브태스크를 다른 태스크가 끝나길 기다리는 일이 발생하며 순차 알고리즘보다 느리고 복잡한 프로그램이 되버린다.
  

- RecursiveTask 내에서는 ForkJoinPool의 invoke 메서드를 사용하지 말아야 한다. 대신 compute나 fork 메서드를 직접 호출할 수 있다. 순차 코드에서 병렬 계산을 시작할 때만 invoke를 사용한다.
  
- 서브태스크에 fork 메서드를 호출해서 ForkJoinPool의 일정을 조절할 수 있다. 양쪽 작업 모두에서 fork를 호출하는 것보다는 한쪽에서는 fork를, 다른 한쪽에서는 compute를 호출하는 것이 효율적이다. 두 서브 태스크의 한 태스크에는 같은 스레드를 재사용할 수 있어 풀에서 불필요한 할당을 하는 오버헤드를 피할 수 있다.
  
- 포크/조인 프레임워크를 이용하는 병렬 계산은 디버깅하기 어렵다. 포크/조인 프레임워크에서는 fork라 불리는 다른 스레드에서 compute를 호출하므로 스택 트레이스<sup>stack trace</sup>가 도움이 안된다.
  
- 포크/조인 프레임워크를 사용하는 것이 순차 처리보다 무조건 빠르지는 않다. 각 서브태스크의 실행시간은 새로운 태스크를 포킹하는 데 드는 시간보다 길어야 한다.



### 7.2.3. 작업 훔치기

이전 예제보다 복잡한 시나리오가 사용되는 현실에서는 각각의 서브태스크의 작업완료 시간이 크게 달라질 수 있다. 분할 기법이 효율적이지 않았기 때문일 수도 있고 아니면 디스크 접근 속도가 저하되었거나 외부 서비스와 협력하는 과정에서 지연이 생길 수도 있다.

포크/조인 프레임워크에서는 **작업 훔치기<sup>work stealing</sup>**라는 기법으로 이 문제를 해결한다.

작업 훔치기 기법에서는 ForkJoinPool의 모든 스레드를 거의 공정하게 분할한다. 각각의 스레드는 자신에게 할당된 태스크가 끝날 때마다 큐의 헤드에서 다른 태스크를 가져와서 작업을 처리한다. 그리고 할 일이 없어진 스레드는 다른 스레드 큐의 꼬리<sup>tail</sup>에서 다른 태스크를 가져와서 작업을 처리한다. 모든 태스크가 끝날 때 까지, 즉 모든 큐가 이 과정을 반복한다.

풀에 있는 작업자 스레드의 태스크를 재분배하고 균형을 맞출 때 작업 훔치기 알고리즘을 사용한다.



- **포크/조인 프레임워크에서 사용하는 작업 훔치기 알고리즘**

  ![img](https://github.com/LeeSM0518/modern-java/raw/master/src/main/java/capture/%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7%202019-09-24%20%EC%98%A4%ED%9B%84%209.26.20.png)

작업자의 큐에 있는 태스크를 두 개의 서브 태스크로 분할했을 때 둘 중 하나의 태스크를 다른 유휴 작업자가 가져갈 수 있다. 주어진 태스크를 순차 실행할 단계가 될 때까지 이 과정을 재귀적으로 반복한다.

## 7.3 Spliterator 인터페이스

**Spliterator는 '분할할 수 있는 반복자<sup>splitable iterator</sup>' 라는 의미다.**

Spliterator는 병렬 작업에 특화되어 있다. 커스텀 Spliterator를 꼭 직접 구현해야하는 것은 아니지만 어떻게 동작하는지 이해한다면 병렬 스트림 동작과 관련된 이해를 얻을 수 있다. 컬렉션은 spliterator라는 메서드를 제공하는 Spliterator 인터페이스를 구현한다. 다음처럼 Spliterator 인터페이스는 여러 메서드를 정의한다.



> ***Spliterator 인터페이스***

```java
public interface Spliterator<T> {
  boolean tryAdvance(Consumer<? super T> action);
  Spliterator<T> trySplit();
  long estimateSize();
  int characteristics();
}
```

- **T** : Spliterator에서 탐색하는 요소의 형식
- **tryAdvance()** : Spliterator의 요소를 하나씩 순차적으로 소비하면서 탐색해야 할 요소가 남아있으면 참을 반환한다(Iterator와 비슷).
- **trySplit()** : Spliterator의 일부 요소(자신이 반환한 요소)를 분할해서 두 번째 Spliterator를 생성하는 메서드.
- **estimateSize()** : Spliterator에서 탐색해야 할 요소 수 정보



### 7.3.1. 분할 과정

> *재귀 분할 과정*

[![img](https://github.com/LeeSM0518/modern-java/raw/master/src/main/java/capture/%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7%202019-09-24%20%EC%98%A4%ED%9B%84%209.39.18.png)](https://github.com/LeeSM0518/modern-java/blob/master/src/main/java/capture/스크린샷 2019-09-24 오후 9.39.18.png)

1. 첫 번째 Spliterator에 trySplit을 호출하면 두 번째 Spliterator가 생성된다.
2. 두 개의 Spliterator에 trySplit를 다시 호출하면 두 배인 네 개의 Spliterator가 생성된다.

> 이처럼 trySplit의 결과가 null이 될 때까지, 즉 더 이상 분할할 수 없을 때까지 실행된다.



### Spliterator 특성

Spliterator는 characteristics라는 추상 메서드도 정의한다. **Characteristics 메서드 Spliterator 자체의 특성 집합을 포함하는 int를 반환한다.**



- **Spliterator 특성**

| 특성       | 의미                                                         |
| ---------- | ------------------------------------------------------------ |
| ORDERED    | 리스트처럼 요소에 정해진 순서가 있으므로 Spliterator는 요소를 탐색하고 분할할 때 이 순서에 유의해야 한다. |
| DISTINCT   | x, y 두 요소를 방문했을 때, x.equals(y)는 항상 false를 반환한다. |
| SORTED     | 탐색된 요소는 미리 정의된 정렬 순서를 따른다.                |
| SIZED      | 크기가 알려진 소스로 Spliterator를 생성했으므로 estimatedSize()는 정확한 값을 반환한다. |
| NON-NULL   | 탐색하는 모든 요소는 null이 아니다.                          |
| IMMUTABLE  | 이 Spliterator의 소스는 불변이다. 즉, 요소를 탐색하는 동안 요소를 추가하거나, 삭제하거나, 고칠 수 없다. |
| CONCURRENT | 동기화 없이 Spliterator의 소스를 여러 스레드에서 동시에 고칠 수 있다. |
| SUBSIZED   | 이 Spliterator 그리고 분할되는 모든 Spliterator는 SIZED 특성을 갖는다. |



## 7.3.2. 커스텀 Spliterator 구현하기

- **반복형으로 단어 수를 세는 메서드**

  ```java
  public int countWordsIteratively(String s) {
    int counter = 0;
    boolean lastSpace = true;
    for (char c : s.toCharArray()) {
      if (Character.isWhitespace(c)) {
        lastSpace = true;
      } else {
        if (lastSpace) counter++;
        lastSpace = false;
      }
    }
    return counter;
  }
  ```

- **Main**

  ```java
  public static void main(String[] args) {
    final String SENTENCE = "There is no requirement that a new or distinct result be returned each " +
      "time the supplier is invoked.";
    System.out.println("Found " + countWordsIteratively(SENTENCE) + " words");
  }
  ```

- **실행 결과**

  ```java
  Found 18 words
  ```

반복형 대신 함수형을 이용하면 직접 스레드를 동기화하지 않고도 **병렬 스트림으로 작업을 병렬화할 수 있다.**



### 함수형으로 단어 수를 세는 메서드 재구현하기

우선 String을 스트림으로 변환 한다. 스트림은 `int,long, double`기본형만 제공하므로 Stream<Character>를 사용해야 한다.

```java
Stream<Character> stream = IntStream.range(0, SENTENCE.length())
  .mapToObj(SENTENCE::charAt);
```



스트림에 리듀싱 연산을 실행하면서 단어 수를 계산할 수 있다. 이들 변수 상태를 캡슐화하는 새로운 클래스 WordCounter를 만들어야 한다.

> ***WordCounter.java***

```java
public class WordCounter {

  private final int counter;
  private final boolean lastSpace;

  public WordCounter(int counter, boolean lastSpace) {
    this.counter = counter;
    this.lastSpace = lastSpace;
  }

  // 반복 알고리즘처럼 accumulate 메서드는
  // 문자열의 문자를 하나씩 탐색한다.
  public WordCounter accumulate(Character c) {
    if (Character.isWhitespace(c)) {
      return lastSpace ?
          this : new WordCounter(counter, true);
    } else {
      return lastSpace ?
          // 문자를 하나씩 탐색하다 공백 문자를 만나면
          // 지금까지 탐색한 문자를 단어로 간주하여
          // 단어 수를 증가시킨다.
          new WordCounter(counter + 1, false) :
          this;
    }
  }

  public WordCounter combine(WordCounter wordCounter) {
    // 두 WordCounter 의 counter 값을 더한다.
    return new WordCounter(counter + wordCounter.counter,
        // counter 값만 더할 것이므로 마지막 공백은 신경 쓰지 않는다.
        wordCounter.lastSpace);
  }

  public int getCounter() {
    return counter;
  }

}
```

- accumulate 메서드는 WordCounter의 상태를 어떻게 바꿀 것인지, 또는 엄밀히 WordCounter는 불변 클래스이므로 새로운 WordCounter 클래스를 어떤 상태로 생성할 것인지 정의한다.
- 스트림을 탐색하면서 새로운 문자를 찾을 때마다 accumulate 메서드를 호출한다.



- **새로운 문자 c를 탐색했을 때 WordCounter의 상태 변화**

  ![img](https://github.com/LeeSM0518/modern-java/raw/master/src/main/java/capture/%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7%202019-09-25%20%EC%98%A4%ED%9B%84%202.12.09.png)



이제  다음 코드처럼 **리듀싱 연산을 스트림을 사용해 직관적으로 구현할 수 있다.**

```java
private static int countWords(Stream<Character> stream) {
  WordCounter wordCounter = stream.reduce(new WordCounter(0, true),
   		                                        WordCounter::accumulate, 								     																					 WordCounter::combine);
  return wordCounter.getCounter();
}
```



- **Main**

  ```java
  public static void main(String[] args) {
    final String SENTENCE = "There is no requirement that a new or distinct result be returned each " +
      "time the supplier is invoked.";
    Stream<Character> stream = IntStream.range(0, SENTENCE.length())
      .mapToObj(SENTENCE::charAt);
    System.out.println("Found " + countWords(stream) + " words");
  }
  ```



- **실행 결과**

  ```java
  Found 18 words
  ```

하지만 원래 목적이 병렬 수행이었음을 잊으면 안 된다. 이번에는 어떻게 병렬로 수행할 수 있는지 보자.



### WordCounter 병렬로 수행하기

단어 수를 계산하는 연산을 병렬 스트림으로 처리하자.

- **Main**

  ```java
  System.out.println("Found " + countWords(stream.parallel()) + " words");
  ```

- **실행 결과**

  ```java
  Found 57 words
  ```

  > 18이 아닌 57이 나왔음을 알 수 있다. 즉, 잘못된 값이 나옴을 알 수 있다.



원래 문자열을 임의의 위치에서 둘로 나누다보니 예상치 못하게 하나의 단어를 둘로 계산하는 상황이 발생할 수 있다.

즉, **순차 스트림을 병렬 스트림으로 바꿀 때 스트림 분할 위치에 따라 잘못된 결과가 나올 수 있다.**

문자열을 임의의 위치에서 분할하지 말고 단어가 끝나는 위치에서만 분할하는 방법으로 이 문제를 해결할 수 있다. 그러면 **단어 끝에서 문자열을 분할하는 문자 Spliterator가 필요하다.**



- **문자 Spliterator를 구현한 다음에 병렬 스트림으로 전달하는 코드**

  ```java
  public class WordCounterSpliterator implements Spliterator<Character> {
  
    private final String string;
    private int currentChar = 0;
  
    public WordCounterSpliterator(String string) {
      this.string = string;
    }
  
    @Override
    public boolean tryAdvance(Consumer<? super Character> action) {
      action.accept(string.charAt(currentChar++));   // 현재 문자를 소비한다.
      return currentChar < string.length();          // 소비할 문자가 남아있으면 true 
    }
  
    @Override
    public Spliterator<Character> trySplit() {
      int currentSize = string.length() - currentChar;
      if (currentSize < 10) {
        // 피싱할 문자열을 순차 처리할 수 있을 만큼
        // 충분히 작아졌음을 알리는 null 을 반환한다.
        return null;
      }
      // 피싱할 문자열의 중간을 분할 위치로 설정한다.
      for (int splitPos = currentSize / 2 + currentChar; splitPos < string.length(); splitPos++) {
        // 다음 공백이 나올 때까지 분할 위치를 뒤로 이동 시킨다.
        if (Character.isWhitespace(string.charAt(splitPos))) {
          // 처음부터 분할 위치까지 문자열을 파싱할
          // 새로운 WordCounterSpliterator 를 생성한다.
          Spliterator<Character> spliterator =
              new WordCounterSpliterator(string.substring(currentChar,
                  splitPos));
          // 이 WordCounterSpliterator 의 시작 위치를 분할 위치로 설정한다.
          currentChar = splitPos;
          // 공백을 찾았고 문자열을 분리했으므로 루프를 종료한다.
          return spliterator;
        }
      }
      return null;
    }
  
    @Override
    public long estimateSize() {
      return string.length() - currentChar;
    }
  
    @Override
    public int characteristics() {
      return ORDERED + SIZED + SUBSIZED + NONNULL + IMMUTABLE;
    }
    
  }
  ```

  - 분석 대상 문자열로 Spliterator를 생성한 다음에 현재 탐색 중인 문자를 가리키는 인덱스를 이용해서 모든 문자를 반복 탐색한다.
  - tryAdvance 메서드는 문자열에서 현재 인덱스에 해당하는 문자를 Consumer에 제공한 다음에 인덱스를 증가시킨다. 인수로 전달된 Consumer는 스트림을 탐색하면서 적용해야 하는 함수 집합이 작업을 처리할 수 있도록 소비한 문자를 전달하는 자바 내부 클래스다.
  - trySplit은 반복될 자료구조를 분할하는 로직을 포함하므로 Spliterator에서 가장 중요한 메서드다. 우선 분할 동작을 중단할 한계를 설정해야 한다.
  - 탐색해야 할 요소의 개수(estimatedSize)는 Spliterator가 파싱할 문자열 전체 길이(string.length())와 현재 반복 중인 위치(currentChar)의 차다.
  - characteristics 메서드는 프레임워크에 Spliterator가 ORDERED (문자열의 문자 등장 순서), SIZED (estimatedSize 메서드의 반환값이 정확), SUBSIZED (trySplit으로 생성된 Spliterator도 정확한 크기를 가짐) , NONNULL (문자열에는 null 문자가 존재하지 않음), IMMUTABLE (문자열 자체가 불변 클래스이므로 문자열을 파싱하면서 속성이 추가되지 않음) 등의 특성임을 알려준다.



### WordCounterSpliterator 활용

- **WordCounterSpliterator를 병렬 스트림에 사용**

  ```java
  Spliterator<Character> spliterator = new WordCounterSpliterator(SENTENCE);
  Stream<Character> stream = StreamSupport.stream(spliterator, true);
  ```

  - StreamSupport.stream 팩토리 메서드로 전달한 두 번째 불리언 인수는 병렬 스트림 생성 여부를 지시한다.

- **실행**

  ```java
  System.out.println("Found " + countWords(stream) + " words");
  ```

- **결과**

  ```
  Found 18 words
  ```



Spliterator는 첫 번째 탐색 시점, 첫 번째 분할 시점, 또는 첫 번째 예상 크기(estimatedSize) 요청 시점에 요소의 소스를 바인딩할 수 있다. 이와 같은 동작을 늦은 바인딩 Spliterator라고 부른다.



## 7.4 마치며

- 내부 반복을 이용하면 명시적으로 다른 스레드를 사용하지 않고도 스트림을 병렬로 처리할 수 있다.
- 스트림을 병렬로 처리하는 것이 항상 빠른 것은 아니다.
- 병렬 스트림으로 데이터 집합을 병렬 실행할 때 특히 처리해야 할 데이터가 아주 많거나 각 요소를 처리하는 데 오랜 시간이 걸릴 때 성능을 높일 수 있다.
- 가능하면 기본형 특화 스트림을 사용하여 병렬 처리하는 것이 좋다.
- 포크/조인 프레임워크에서는 병렬화할 수 있는 태스크를 작은 태스크로 분할한 다음에 분할된 태스크를 각각의 스레드로 실행하며 서브태스크 각각의 결과를 합쳐서 최종 결과를 생산한다.
- Spliterator는 탐색하려는 데이터를 포함하는 스트림을 어떻게 병렬화할 것인지 정의한다.