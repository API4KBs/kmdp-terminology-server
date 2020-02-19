package edu.mayo.kmdp.terms;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

/**
 * Tests for the retrieval of terminologies and the json output file.
 */
public class TerminologyIndexerTest {

  TerminologyIndexer provider = new TerminologyIndexer();

  /**
   * Test will fail if an exception is thrown when creating the JSON output file.
   */
  @Test
  void testWriteTerminologies() {
    try {
      provider.execute("test.json");
    } catch (Exception e) {
      Assertions.fail(e.getMessage());
    }
  }

}
