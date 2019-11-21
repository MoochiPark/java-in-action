package io.wisoft.seminar.daewon.javainaction.chapter03.executearoundpattern;

import java.io.BufferedReader;
import java.io.IOException;

@FunctionalInterface
public interface BufferedReaderProcessor {

  String process(BufferedReader bufferedReader) throws IOException;

}
