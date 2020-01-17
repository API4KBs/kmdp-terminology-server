package edu.mayo.kmdp.terms;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.omg.demo.terms.TermsProvider;
import org.omg.spec.api4kp._1_0.identifiers.Pointer;

public class TermsProviderTest {

  TermsProvider provider = new TermsProvider();

  @Test
  void testProvider() {
    List<Pointer> termSystems = provider.listTerminologies()
        .orElse(Collections.emptyList());
    assertEquals(1,termSystems.size());
  }

}
