package edu.mayo.kmdp.terms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import edu.mayo.ontology.taxonomies.kao.knowledgeassettype._20190801.KnowledgeAssetType;
import edu.mayo.ontology.taxonomies.propositionalconcepts.PropositionalConcepts;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.identifiers.ConceptIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.Pointer;

public class TermsProviderTest {

    TermsProvider provider = new TermsProvider();

    /**
     * Verify the total number of terminologies
     */
    @Test
    void testProvider() {
        List<Pointer> termSystems = provider.listTerminologies().orElse(Collections.emptyList());
        assertEquals(60, termSystems.size());
    }

    /**
     * Verify the number of terms found in a specific version of PropositionalConcepts
     */
    @Test
    void testGetTerms_PCv1() {
        Answer<List<ConceptIdentifier>> answer = provider.getTerms(UUID.fromString(PropositionalConcepts.SCHEME_ID),
                "20191201", "");
        assertEquals(578, answer.get().size());
    }

    /**
     * Verify the number of terms found in a different specific version of PropositionalConcepts
     */
    @Test
    void testGetTerms_PCv2() {
        Answer<List<ConceptIdentifier>> answer = provider.getTerms(UUID.fromString(PropositionalConcepts.SCHEME_ID),
                "20200109", "");
        assertEquals(598, answer.get().size());
    }

    /**
     * Verify an exception is thrown if the version does not exist
     */
    @Test
    void testGetTerms_PCvBad() {
        assertThrows(NullPointerException.class,
                ()->{Answer<List<ConceptIdentifier>> answer = provider.getTerms(UUID.fromString(PropositionalConcepts.SCHEME_ID),
                        "20191208", "");
                });
    }

    /**
     * Verify the number of terms found in a specific version of KnowledgeAssetType
     */
    @Test
    void testGetTerms_KAv1() {
        Answer<List<ConceptIdentifier>> answer = provider.getTerms(UUID.fromString(KnowledgeAssetType.SCHEME_ID),
                "20190801", "");
        assertEquals(38, answer.get().size());
    }

    /**
     *
     */
//    @Test
//    void testGetTerm_PCv1() {
//        Answer<ConceptIdentifier> answer = provider.getTerm(UUID.fromString(PropositionalConcepts.SCHEME_ID),
//                "20191201", "f212fc95-964d-3c1a-b75c-d4ff0269e18c");
//
//    }

}
