package io.wisoft.seminar.daewon.javainaction.chapter09.refactoring.logger;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerExample {

  private static Logger logger = Logger.getGlobal();

  public static void main(String[] args) {
    logger.setLevel(Level.FINER);

//    if (logger.isLoggable(Level.FINER)) {
//      logger.info("test");
//    }

    logger.log(Level.FINER, "Problem");
    logger.log(Level.FINER, () -> "Problem");
  }

}
