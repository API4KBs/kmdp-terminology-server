package edu.mayo.kmdp.terms;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for the retrieval of terminologies and the json output file.
 */
public class TerminologyIndexerTest {

    @TempDir
    public Path tmp;

    TerminologyIndexer provider = new TerminologyIndexer();

    /**
     * Test will fail if an exception is thrown when creating the JSON output file.
     */
    @Test
    void testWriteTerminologies() {
        try {
            File tmpFile = new File(tmp.toFile(), "test.json");
            provider.execute(tmpFile.getPath());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

}
