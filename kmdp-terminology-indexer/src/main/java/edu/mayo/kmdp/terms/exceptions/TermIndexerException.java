package edu.mayo.kmdp.terms.exceptions;

public class TermIndexerException extends RuntimeException {

  public TermIndexerException() {
    super("Unable to write the JSON file which leaves the application in unstable state.");
  }

}
