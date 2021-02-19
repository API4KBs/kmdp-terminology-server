package edu.mayo.kmdp.terms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.omg.spec.api4kp._20200801.taxonomy.knowledgeassettype.KnowledgeAssetTypeSeries.Clinical_Rule;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._20200801.Answer;
import org.omg.spec.api4kp._20200801.api.terminology.v4.server.TermsApiInternal;
import org.omg.spec.api4kp._20200801.services.KPComponent;
import org.omg.spec.api4kp._20200801.terms.model.ConceptDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = {TermsTestConfig.class})
class CombinedTermsTest {

  @Autowired
  @KPComponent(implementation = "broker")
  TermsApiInternal terms;

  @Autowired
  @KPComponent(implementation = "enum")
  TermsApiInternal enumTerms;

  @Autowired
  @KPComponent(implementation = "fhir")
  TermsApiInternal fhirTerms;

  @Test
  void testInit() {
    assertNotNull(terms);
    assertTrue(terms instanceof TermsBrokerImpl);
    assertTrue(((TermsBrokerImpl) terms).providers().stream()
        .anyMatch(TermsFHIRFacade.class::isInstance));
    assertTrue(((TermsBrokerImpl) terms).providers().stream()
        .anyMatch(TermsProvider.class::isInstance));
  }

  @Test
  void testLookup() {
    Answer<ConceptDescriptor> cd1 = terms.lookupTerm(Clinical_Rule.getUuid().toString());
    assertTrue(cd1.isSuccess());

    ConceptDescriptor cd2 = terms.lookupTerm("14e929a2-a22a-3107-8a38-692107b0f7c5")
        .orElseGet(Assertions::fail);
    assertEquals("Educate", cd2.getName());

    Answer<ConceptDescriptor> cd0 = enumTerms.lookupTerm("14e929a2-a22a-3107-8a38-692107b0f7c5");
    assertTrue(cd0.isNotFound());
  }
}
