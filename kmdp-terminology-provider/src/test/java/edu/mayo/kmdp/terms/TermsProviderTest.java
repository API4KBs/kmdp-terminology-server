package edu.mayo.kmdp.terms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.terms.impl.model.ConceptDescriptor;
import edu.mayo.ontology.taxonomies.api4kp.knowledgeoperations._20190801.KnowledgeProcessingOperation;
import edu.mayo.ontology.taxonomies.kao.knowledgeassettype._20190801.KnowledgeAssetType;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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
        assertEquals(50, termSystems.size());
    }

    /**
     * Verify the number of terms found in a specific version of KnowledgeProcessingOperation
     */
    @Test
    void testGetTerms_KPOv1() {
        Answer<List<ConceptDescriptor>> answer = provider.getTerms(UUID.fromString(
            KnowledgeProcessingOperation.SCHEME_ID),
                "20190801", "");
        assertEquals(97, answer.get().size());
    }

    /**
     * Verify an exception is thrown if the version does not exist
     */
    @Test
    void testGetTerms_KPOvBad() {
        assertThrows(NullPointerException.class,
                ()->{Answer<List<ConceptDescriptor>> answer = provider.getTerms(UUID.fromString(KnowledgeProcessingOperation.SCHEME_ID),
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
     * Get a term from KnowledgeProcessingOperation and verify contents
     */
    @Test
    void testGetTerm_KPOv1() {
        String conceptUUID = "b589ab25-df70-3a5f-9ff9-b2fe36728e79";
        String conceptId = "https://ontology.mayo.edu/taxonomies/API4KP/KnowledgeOperations#b589ab25-df70-3a5f-9ff9-b2fe36728e79";
        String label = "create working knowledge base task";
        String ref = "https://www.omg.org/spec/API4KP/api4kp-ops/CreateWorkingKnowledgeBaseTask";
        String tag = "CreateWorkingKnowledgeBaseTask";
        String namespace = "https://ontology.mayo.edu/taxonomies/API4KP/KnowledgeOperations/20190801/";
        String ancestorName = "knowledge base building task";

        Answer<ConceptDescriptor> answer = provider.getTerm(UUID.fromString(KnowledgeProcessingOperation.SCHEME_ID),
                "20190801", conceptId);

        assertEquals(conceptUUID, answer.get().getUuid().toString());
        assertEquals(conceptId, answer.get().getResourceId().toString());
        assertEquals(label, answer.get().getName());
        assertEquals(ref, answer.get().getReferentId().toString());
        assertEquals(tag, answer.get().getTag());
        assertEquals(namespace, answer.get().getNamespaceUri().toString());
        assertEquals(1, answer.get().getAncestors().length);
        Term[] terms = answer.get().getAncestors();
        for(Term term:terms) {
            assertEquals(ancestorName, ((KnowledgeProcessingOperation)term).getName());
        }
    }

    /**
     * Get a term from Knowledge Asset type which has relations
     */
    @Test
    void testGetTerm_KAv1() {
        String conceptUUID = "5c742ccc-fb77-3f33-87f5-663c2d9d251c";
        String conceptId = "https://ontology.mayo.edu/taxonomies/KAO/KnowledgeAssetType#5c742ccc-fb77-3f33-87f5-663c2d9d251c";
        String label = "Multi-Agent Decision Task Model";
        String ref = "https://www.omg.org/spec/API4KP/api4kp-kao/MultiAgentDecisionTaskModel";
        String tag = "MultiAgentDecisionTaskModel";
        String namespace = "https://ontology.mayo.edu/taxonomies/KAO/KnowledgeAssetType/20190801/";
        String ancestorName = "Decision Task Model";

        Answer<ConceptDescriptor> answer = provider.getTerm(UUID.fromString(KnowledgeAssetType.SCHEME_ID),
            "20190801", conceptId);

        assertEquals(conceptUUID, answer.get().getUuid().toString());
        assertEquals(conceptId, answer.get().getResourceId().toString());
        assertEquals(label, answer.get().getName());
        assertEquals(ref, answer.get().getReferentId().toString());
        assertEquals(tag, answer.get().getTag());
        assertEquals(namespace, answer.get().getNamespaceUri().toString());
        assertEquals(1, answer.get().getAncestors().length);
        Term[] terms = answer.get().getAncestors();
        for(Term term:terms) {
            assertEquals(ancestorName, ((KnowledgeAssetType)term).getName());
        }
    }

    /**
     * Verify null is returned if conceptId is not found
     */
    @Test
    void testGetTerm_KPOv1_ConIdNotFind() {
        String conceptId = "https://ontology.mayo.edu/taxonomies/clinicalsituations#notRealConceptId";
        Answer<ConceptDescriptor> answer = provider.getTerm(UUID.fromString(KnowledgeProcessingOperation.SCHEME_ID),
            "20190801", conceptId);
        assertNull(answer);
    }

    /**
     * Verify null is returned if conceptId is not found
     */
    @Test
    void testGetTerm_KPOv1_VersionNotFound() {
        String conceptId = "https://ontology.mayo.edu/taxonomies/clinicalsituations#f212fc95-964d-3c1a-b75c-d4ff0269e18c";
        Answer<ConceptDescriptor> answer = provider.getTerm(UUID.fromString(KnowledgeProcessingOperation.SCHEME_ID),
            "19650724", conceptId);
        assertNull(answer);
    }

    /**
     * Get a term from Knowledge Asset type which has relations
     */
    @Test
    void testGetAncestors_KPOv1() {
        String conceptId = "https://ontology.mayo.edu/taxonomies/API4KP/KnowledgeOperations#d76a9299-4e72-36c1-a261-2265afe11582";
        String[] ancestorNames = {"selection task", "syntactic knowledge processing task"};

        Answer<ConceptDescriptor> answer = provider.getTerm(UUID.fromString(KnowledgeProcessingOperation.SCHEME_ID),
            "20190801", conceptId);

        assertEquals(2, answer.get().getAncestors().length);
        Term[] terms = answer.get().getAncestors();
        for (int i = 0; i < terms.length; i++) {
            assertEquals(ancestorNames[i], ((KnowledgeProcessingOperation)terms[i]).getName());
        }
    }

    /**
     * Verify that a concept is an ancestor
     */
    @Test
    void testIsAncestor_KPOv1() {
        String conceptId = "https://ontology.mayo.edu/taxonomies/API4KP/KnowledgeOperations#d76a9299-4e72-36c1-a261-2265afe11582";
        String ancestorConceptId = "https://ontology.mayo.edu/taxonomies/API4KP/KnowledgeOperations#a5628370-845c-350f-b0e7-6cab66aac127";

        Answer<Boolean> answer = provider.isAncestor(UUID.fromString(KnowledgeProcessingOperation.SCHEME_ID),
            "20190801", conceptId, ancestorConceptId);
        assertTrue(answer.get().booleanValue());
    }

    /**
     * Verify that a concept is an ancestor
     */
    @Test
    void testIsNotAncestor_KPOv1() {
        String conceptId = "https://ontology.mayo.edu/taxonomies/API4KP/KnowledgeOperations#d76a9299-4e72-36c1-a261-2265afe11582";
        String ancestorConceptId = "https://ontology.mayo.edu/taxonomies/API4KP/KnowledgeOperations#a5628370-845c-350f-b0e7";

        Answer<Boolean> answer = provider.isAncestor(UUID.fromString(KnowledgeProcessingOperation.SCHEME_ID),
            "20190801", conceptId, ancestorConceptId);
        assertFalse(answer.get().booleanValue());
    }

}
