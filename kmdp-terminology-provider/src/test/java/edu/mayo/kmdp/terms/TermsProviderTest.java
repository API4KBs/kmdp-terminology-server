package edu.mayo.kmdp.terms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.mayo.ontology.taxonomies.ws.responsecodes.ResponseCodeSeries;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._20200801.Answer;
import org.omg.spec.api4kp._20200801.id.Pointer;
import org.omg.spec.api4kp._20200801.id.Term;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeassettype.KnowledgeAssetType;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeoperation.KnowledgeProcessingOperation;
import org.omg.spec.api4kp._20200801.terms.model.ConceptDescriptor;

class TermsProviderTest {

    private final TermsProvider provider = new TermsProvider();

    /**
     * Verify the total number of terminologies
     */
    @Test
    void testProvider() {
        List<Pointer> termSystems = provider.listTerminologies().orElse(Collections.emptyList());
        assertEquals(39, termSystems.size());
    }

    /**
     * Verify the number of terms found in a specific version of KnowledgeProcessingOperation
     */
    @Test
    void testGetTerms_KPOv1() {
        Answer<List<ConceptDescriptor>> answer = provider.getTerms(UUID.fromString(
            KnowledgeProcessingOperation.SCHEME_ID),
                "20200801", "");
        assertEquals(80, answer.get().size());
    }

    /**
     * Verify an exception is thrown if the version does not exist
     */
    @Test
    void testGetTerms_KPOvBad() {
        UUID uuid = UUID.fromString(KnowledgeProcessingOperation.SCHEME_ID);
        assertThrows(NullPointerException.class,
            () -> provider.getTerms(uuid, "20191208", "")
        );
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
     * Get a term from KnowledgeProcessingOperation and verify contents
     */
    @Test
    void testGetTerm_KPOv1() {
        String conceptUUID = "c6e34990-85d9-31b2-8a33-f46e0e9f8b33";
        String conceptId = "https://www.omg.org/spec/API4KP/20200801/taxonomy/KnowledgeOperation#c6e34990-85d9-31b2-8a33-f46e0e9f8b33";
        String label = "knowledge base building task";
        String ref = "https://www.omg.org/spec/API4KP/api4kp-ops/KnowledgeBaseBuildingTask";
        String tag = "KnowledgeBaseBuildingTask";
        String namespace = "https://www.omg.org/spec/API4KP/20200801/taxonomy/KnowledgeOperation";
        String ancestorName = "knowledge resource assembly task";

        Answer<ConceptDescriptor> answer = provider.getTerm(UUID.fromString(KnowledgeProcessingOperation.SCHEME_ID),
                "20200801", conceptId);
        assertTrue(answer.isSuccess());
        ConceptDescriptor cd = answer.get();

        assertEquals(conceptUUID, cd.getUuid().toString());
        assertEquals(conceptId, cd.getResourceId().toString());
        assertEquals(label, cd.getName());
        assertEquals(ref, cd.getReferentId().toString());
        assertEquals(tag, cd.getTag());
        assertEquals(namespace, cd.getNamespaceUri().toString());
        assertEquals(1, cd.getAncestors().length);
        Term[] terms = cd.getAncestors();
        for(Term term:terms) {
            assertEquals(ancestorName, term.getName());
        }
    }

    /**
     * Get a term from Knowledge Asset type which has relations
     */
    @Test
    void testGetTerm_KAv1() {
        String conceptUUID = "5c742ccc-fb77-3f33-87f5-663c2d9d251c";
        String conceptId = "https://www.omg.org/spec/API4KP/20200801/taxonomy/KnowledgeAssetType#5c742ccc-fb77-3f33-87f5-663c2d9d251c";
        String label = "Multi-Agent Decision Task Model";
        String ref = "https://www.omg.org/spec/API4KP/api4kp-kao/MultiAgentDecisionTaskModel";
        String tag = "MultiAgentDecisionTaskModel";
        String namespace = "https://www.omg.org/spec/API4KP/20200801/taxonomy/KnowledgeAssetType";
        String ancestorName = "Decision Task Model";

        Answer<ConceptDescriptor> answer = provider.getTerm(UUID.fromString(KnowledgeAssetType.SCHEME_ID),
            "20190801", conceptId);
        assertTrue(answer.isSuccess());
        ConceptDescriptor cd = answer.get();

        assertEquals(conceptUUID, cd.getUuid().toString());
        assertEquals(conceptId, cd.getResourceId().toString());
        assertEquals(label, cd.getName());
        assertEquals(ref, cd.getReferentId().toString());
        assertEquals(tag, cd.getTag());
        assertEquals(namespace, cd.getNamespaceUri().toString());
        assertEquals(1, cd.getAncestors().length);
        Term[] terms = cd.getAncestors();
        for(Term term:terms) {
            assertEquals(ancestorName, term.getName());
        }
    }

    /**
     * Verify null is returned if conceptId is not found
     */
    @Test
    void testGetTerm_KPOv1_ConIdNotFind() {
        String conceptId = "https://ontology.mayo.edu/taxonomies/clinicalsituations#notRealConceptId";
        Answer<ConceptDescriptor> answer = provider.getTerm(UUID.fromString(
            KnowledgeProcessingOperation.SCHEME_ID),
            "20190801", conceptId);
        assertNotNull(answer);
        assertTrue(answer.isFailure());
        assertTrue(ResponseCodeSeries.NotFound.sameAs(answer.getOutcomeType()));
    }

    /**
     * Verify null is returned if conceptId is not found
     */
    @Test
    void testGetTerm_KPOv1_VersionNotFound() {
        String conceptId = "https://ontology.mayo.edu/taxonomies/clinicalsituations#f212fc95-964d-3c1a-b75c-d4ff0269e18c";
        Answer<ConceptDescriptor> answer = provider.getTerm(UUID.fromString(KnowledgeProcessingOperation.SCHEME_ID),
            "19650724", conceptId);
        assertNotNull(answer);
        assertTrue(answer.isFailure());
        assertTrue(ResponseCodeSeries.NotFound.sameAs(answer.getOutcomeType()));
    }

    /**
     * Get a term from Knowledge Asset type which has relations
     */
    @Test
    void testGetAncestors_KPOv1() {
        String conceptId = "https://www.omg.org/spec/API4KP/20200801/taxonomy/KnowledgeOperation#d76a9299-4e72-36c1-a261-2265afe11582";
        String[] ancestorNames = {"selection task"};

        Answer<ConceptDescriptor> answer = provider.getTerm(UUID.fromString(KnowledgeProcessingOperation.SCHEME_ID),
            "20200801", conceptId);
        assertTrue(answer.isSuccess());
        ConceptDescriptor cd = answer.get();

        assertEquals(1, cd.getAncestors().length);
        Term[] terms = cd.getAncestors();
        for (Term term : terms) {
            assertTrue(Arrays.asList(ancestorNames)
                .contains(term.getName()));
        }
    }

    /**
     * Verify that a concept is an ancestor
     */
    @Test
    void testIsAncestor_KPOv1() {
        String conceptId = "https://www.omg.org/spec/API4KP/20200801/taxonomy/KnowledgeOperation#d76a9299-4e72-36c1-a261-2265afe11582";
        String ancestorConceptId = "https://www.omg.org/spec/API4KP/20200801/taxonomy/KnowledgeOperation#a5628370-845c-350f-b0e7-6cab66aac127";

        Answer<Boolean> answer = provider.isAncestor(UUID.fromString(KnowledgeProcessingOperation.SCHEME_ID),
            "20200801", conceptId, ancestorConceptId);
        assertTrue(answer.isSuccess() && answer.orElse(false));
    }

    /**
     * Verify that a concept is an ancestor
     */
    @Test
    void testIsNotAncestor_KPOv1() {
        String conceptId = "https://www.omg.org/spec/API4KP/20200801/taxonomy/KnowledgeOperation#d76a9299-4e72-36c1-a261-2265afe11582";
        String ancestorConceptId = "https://www.omg.org/spec/API4KP/20200801/taxonomy/KnowledgeOperation#a5628370-845c-350f-b0e7";

        Answer<Boolean> answer = provider.isAncestor(UUID.fromString(KnowledgeProcessingOperation.SCHEME_ID),
            "20200801", conceptId, ancestorConceptId);
        assertFalse(answer.isSuccess() && answer.orElse(false));
    }

}
