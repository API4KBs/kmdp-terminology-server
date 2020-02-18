package edu.mayo.kmdp.terms;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import edu.mayo.ontology.taxonomies.kao.publicationeventtype.snapshot.PublicationEventType;
import edu.mayo.ontology.taxonomies.propositionalconcepts.PropositionalConcepts;
import org.junit.jupiter.api.Test;
import org.omg.demo.terms.TermsProvider;
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.identifiers.ConceptIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.Pointer;

public class TermsProviderTest {

  TermsProvider provider = new TermsProvider();

  @Test
  void testProvider() {
    List<Pointer> termSystems = provider.listTerminologies()
        .orElse(Collections.emptyList());
    assertEquals(61, termSystems.size());
  }

  @Test
  void testGetTerms_PCv1() {
    Answer<List<ConceptIdentifier>> answer = provider.getTerms(UUID.fromString(PropositionalConcepts.SCHEME_ID),
            "20191201", "");
    assertEquals(578, answer.get().size());
  }

  @Test
  void testGetTerms_PCv2() {
    Answer<List<ConceptIdentifier>> answer = provider.getTerms(UUID.fromString(PropositionalConcepts.SCHEME_ID),
            "20200109", "");
    assertEquals(598, answer.get().size());
  }

//  @Test
//  void testGetTerms_PEv1() {
//      Answer<List<ConceptIdentifier>> answer = provider.getTerms(UUID.fromString(PublicationEventType.SCHEME_ID),
//              "20200211-144905", "");
//      assertEquals(7, answer.get().size());
//  }

}
