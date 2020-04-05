package edu.mayo.kmdp.terms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import edu.mayo.kmdp.terms.impl.model.ConceptDescriptor;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import edu.mayo.ontology.taxonomies.kao.knowledgeassettype._20190801.KnowledgeAssetType;
import edu.mayo.ontology.taxonomies.propositionalconcepts.PropositionalConcepts;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.id.Pointer;

public class TermsProviderTest {

    private TermsProvider provider = new TermsProvider();

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
        Answer<List<ConceptDescriptor>> answer = provider.getTerms(UUID.fromString(PropositionalConcepts.SCHEME_ID),
                "20191201", "");
        assertEquals(578, answer.get().size());
    }

    /**
     * Verify the number of terms found in a different specific version of PropositionalConcepts
     */
    @Test
    void testGetTerms_PCv2() {
        Answer<List<ConceptDescriptor>> answer = provider.getTerms(UUID.fromString(PropositionalConcepts.SCHEME_ID),
                "20200109", "");
        assertEquals(598, answer.get().size());
    }

    /**
     * Verify an exception is thrown if the version does not exist
     */
    @Test
    void testGetTerms_PCvBad() {
        assertThrows(NullPointerException.class,
                ()->{Answer<List<ConceptDescriptor>> answer = provider.getTerms(UUID.fromString(PropositionalConcepts.SCHEME_ID),
                        "20191208", "");
                });
    }

    /**
     * Verify the number of terms found in a specific version of KnowledgeAssetType
     */
    @Test
    void testGetTerms_KAv1() {
        Answer<List<ConceptDescriptor>> answer = provider.getTerms(UUID.fromString(KnowledgeAssetType.SCHEME_ID),
                "20190801", "");
        assertEquals(38, answer.get().size());
    }

    /**
     * Get a term from PropositionalConcepts and verify contents
     * On_Angiotensin_Receptor_Neprilysin_Inhibitor(
     * "https://ontology.mayo.edu/taxonomies/clinicalsituations#f212fc95-964d-3c1a-b75c-d4ff0269e18c",
     * "f212fc95-964d-3c1a-b75c-d4ff0269e18c",
     * "c05e08cb-1ee6-4b0a-9dfa-e825f739734c",
     * Arrays.asList("c05e08cb-1ee6-4b0a-9dfa-e825f739734c"),
     * "On Angiotensin Receptor Neprilysin Inhibitor",
     * "http://ontology.mayo.edu/ontologies/clinicalsituationontology/c05e08cb-1ee6-4b0a-9dfa-e825f739734c",
     * new Term[0],
     * new Term[0])
     */
    @Test
    void testGetTerm_PCv1() {
        String conceptUUID = "f212fc95-964d-3c1a-b75c-d4ff0269e18c";
        String conceptId = "https://ontology.mayo.edu/taxonomies/clinicalsituations#f212fc95-964d-3c1a-b75c-d4ff0269e18c";
        String label = "On Angiotensin Receptor Neprilysin Inhibitor";
        String ref = "http://ontology.mayo.edu/ontologies/clinicalsituationontology/c05e08cb-1ee6-4b0a-9dfa-e825f739734c";
        String tag = "c05e08cb-1ee6-4b0a-9dfa-e825f739734c";
        String namespace = "https://ontology.mayo.edu/taxonomies/propositionalconcepts/20191201/";
        Answer<ConceptDescriptor> answer = provider.getTerm(UUID.fromString(PropositionalConcepts.SCHEME_ID),
                "20191201", conceptId);

        assertEquals(conceptUUID, answer.get().getUuid().toString());
        assertEquals(conceptId, answer.get().getResourceId().toString());
        assertEquals(label, answer.get().getName());
        assertEquals(ref, answer.get().getReferentId().toString());
        assertEquals(tag, answer.get().getTag());
        assertEquals(namespace, answer.get().getNamespaceUri().toString());
    }

    /**
     * Get a term from Knowledge Asset type which has relations
     *
     *  "https://ontology.mayo.edu/taxonomies/KAO/KnowledgeAssetType#5c742ccc-fb77-3f33-87f5-663c2d9d251c",
     *  "5c742ccc-fb77-3f33-87f5-663c2d9d251c",
     *  "MultiAgentDecisionTaskModel",
     *  Arrays.asList ("MultiAgentDecisionTaskModel"),
     *  "Multi-Agent Decision Task Model",
     *  "https://www.omg.org/spec/API4KP/api4kp-kao/MultiAgentDecisionTaskModel",
     *  new Term[]{Decision_Task_Model},
     *  new Term[]{Decision_Task_Model, Cognitive_Process_Model}),
     */
    @Test
    void testGetTerm_KAv1_HasRelations() {
        String conceptUUID = "5c742ccc-fb77-3f33-87f5-663c2d9d251c";
        String conceptId = "https://ontology.mayo.edu/taxonomies/KAO/KnowledgeAssetType#5c742ccc-fb77-3f33-87f5-663c2d9d251c";
        String label = "Multi-Agent Decision Task Model";
        String ref = "https://www.omg.org/spec/API4KP/api4kp-kao/MultiAgentDecisionTaskModel";
        String tag = "MultiAgentDecisionTaskModel";
        String namespace = "https://ontology.mayo.edu/taxonomies/KAO/KnowledgeAssetType/20190801/";
        Answer<ConceptDescriptor> answer = provider.getTerm(UUID.fromString(KnowledgeAssetType.SCHEME_ID),
            "20190801", conceptId);

        assertEquals(conceptUUID, answer.get().getUuid().toString());
        assertEquals(conceptId, answer.get().getResourceId().toString());
        assertEquals(label, answer.get().getName());
        assertEquals(ref, answer.get().getReferentId().toString());
        assertEquals(tag, answer.get().getTag());
        assertEquals(namespace, answer.get().getNamespaceUri().toString());
    }

    /**
     * Verify null is returned if conceptId is not found
     */
    @Test
    void testGetTerm_PCv1_ConIdNotFound() {
        String conceptId = "https://ontology.mayo.edu/taxonomies/clinicalsituations#notRealConceptId";
        Answer<ConceptDescriptor> answer = provider.getTerm(UUID.fromString(PropositionalConcepts.SCHEME_ID),
            "20191201", conceptId);
        assertNull(answer);
    }

    /**
     * Verify null is returned if conceptId is not found
     */
    @Test
    void testGetTerm_PCv1_VersionNotFound() {
        String conceptId = "https://ontology.mayo.edu/taxonomies/clinicalsituations#f212fc95-964d-3c1a-b75c-d4ff0269e18c";
        Answer<ConceptDescriptor> answer = provider.getTerm(UUID.fromString(PropositionalConcepts.SCHEME_ID),
            "19650724", conceptId);
        assertNull(answer);
    }

}
