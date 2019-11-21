package io.wisoft.seminar.daewon.javainaction.chapter03.executearoundpattern;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ExecuteAroundPattern {
//
//  public static String processFile(BufferedReaderProcessor p) throws IOException {
//    try (BufferedReader br =
//             new BufferedReader(new FileReader("C:\\Users\\Admin\\workspace\\Java in Action\\src\\main\\java\\io\\wisoft\\seminar\\daewon\\javainaction\\chapter03\\executearoundpattern\\data.txt"))) {
//      return p.process(br);
//    }
//  }

  public static String processFile(BufferedReaderProcessor bufferedReaderProcessor) throws IOException {
    try (BufferedReader br =
             new BufferedReader(new FileReader("src/main/resources/data.txt"))) {
      return bufferedReaderProcessor.process(br);
    }
  }

  public static void main(String[] args) throws IOException {
    String oneLine = processFile(br -> br.readLine());
    String twoLine = processFile(br -> br.readLine() + br.readLine());
//    FileWriter fileWriter = new FileWriter("test.txt");
//    fileWriter.write("첫 번째");
//    fileWriter.write("두 번째");
//    fileWriter.close();
    System.out.println(oneLine);
    System.out.println(twoLine);

  }

}
