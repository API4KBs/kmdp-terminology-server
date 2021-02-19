package edu.mayo.kmdp.terms;

import static edu.mayo.kmdp.terms.TermsTestUtil.prepopulateWithKnownKMDTaxonomy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.omg.spec.api4kp._20200801.AbstractCarrier.codedRep;
import static org.omg.spec.api4kp._20200801.AbstractCarrier.rep;
import static org.omg.spec.api4kp._20200801.surrogate.SurrogateBuilder.newSurrogate;
import static org.omg.spec.api4kp._20200801.taxonomy.knowledgeassetcategory.KnowledgeAssetCategorySeries.Terminology_Ontology_And_Assertional_KBs;
import static org.omg.spec.api4kp._20200801.taxonomy.knowledgeassettype.KnowledgeAssetTypeSeries.Clinical_Rule;
import static org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormatSeries.JSON;
import static org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormatSeries.XML_1_1;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.FHIR_STU3;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.OWL_2;
import static org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevelSeries.Abstract_Knowledge_Expression;
import static org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevelSeries.Encoded_Knowledge_Expression;
import static org.omg.spec.api4kp._20200801.taxonomy.publicationstatus.PublicationStatusSeries.Published;

import edu.mayo.kmdp.language.parsers.fhir.stu3.FHIR3Deserializer;
import edu.mayo.kmdp.language.parsers.rdf.JenaRdfParser;
import edu.mayo.kmdp.language.translators.skos.SKOStoCodeSystemTranscreator;
import edu.mayo.kmdp.repository.asset.KnowledgeAssetRepositoryService;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.javers.core.JaversBuilder;
import org.javers.core.diff.Diff;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._20200801.AbstractCarrier;
import org.omg.spec.api4kp._20200801.AbstractCarrier.Encodings;
import org.omg.spec.api4kp._20200801.Answer;
import org.omg.spec.api4kp._20200801.api.repository.asset.v4.KnowledgeAssetCatalogApi;
import org.omg.spec.api4kp._20200801.api.repository.asset.v4.KnowledgeAssetRepositoryApi;
import org.omg.spec.api4kp._20200801.id.KeyIdentifier;
import org.omg.spec.api4kp._20200801.id.Pointer;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.id.SemanticIdentifier;
import org.omg.spec.api4kp._20200801.id.Term;
import org.omg.spec.api4kp._20200801.services.KnowledgeCarrier;
import org.omg.spec.api4kp._20200801.services.SyntacticRepresentation;
import org.omg.spec.api4kp._20200801.surrogate.KnowledgeAsset;
import org.omg.spec.api4kp._20200801.surrogate.Publication;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeassettype.KnowledgeAssetTypeSeries;
import org.omg.spec.api4kp._20200801.terms.model.ConceptDescriptor;


class TermsFHIRBasedTest {

  static TermsProvider refServer;
  static TermsFHIRFacade server;

  @BeforeAll
  static void init() {
    KnowledgeAssetRepositoryService kars = KnowledgeAssetRepositoryService.selfContainedRepository();
    prepopulateWithKnownKMDTaxonomy(kars);
    refServer = TermsProvider.newTermsProvider();
    server = new TermsFHIRFacade(
        KnowledgeAssetCatalogApi.newInstance(kars),
        KnowledgeAssetRepositoryApi.newInstance(kars));
  }

  @Test
  void testIntegrity() {
    List<Pointer> ptr = refServer.listTerminologies().orElse(Collections.emptyList());
    assertFalse(ptr.isEmpty());
  }

  @Test
  void testListTerminologies() {
    List<Pointer> termSystems = server.listTerminologies().orElseGet(Assertions::fail);
    assertEquals(1, termSystems.size());

    KeyIdentifier key = termSystems.get(0).asKey();
    List<Pointer> allTerms = refServer.listTerminologies().orElseGet(Assertions::fail);
    boolean match = allTerms
        .stream()
        .anyMatch(ptr -> ptr.asKey().equals(key));
    assertTrue(match);
  }

  @Test
  void testGetTerms() {
    UUID uuid = UUID.fromString("243089c1-b6ab-318f-bec9-e1cfaf410992");
    String versionTag = "20190801";

    List<ConceptDescriptor> terms1 = server.getTerms(uuid, versionTag).orElseGet(Assertions::fail);
    List<ConceptDescriptor> terms2 = refServer.getTerms(uuid, versionTag).orElseGet(Assertions::fail);

    assertEquals(KnowledgeAssetTypeSeries.values().length, terms1.size());
    // the legacy server indexes using both the old 'ontology' vs 'taxonomy' UUID, which predates the decision to unify the two
    assertEquals(2 * KnowledgeAssetTypeSeries.values().length, terms2.size());

    ConceptDescriptor cd1 = terms1.stream().filter(cd -> cd.getUuid().equals(Clinical_Rule.getUuid())).findFirst()
        .orElseGet(Assertions::fail);
    ConceptDescriptor cd2 = terms2.stream().filter(cd -> cd.getUuid().equals(Clinical_Rule.getUuid())).findFirst()
        .orElseGet(Assertions::fail);

    Diff diff = JaversBuilder.javers().build().compare(cd1, cd2);
    assertTrue(diff.getChanges().isEmpty());
  }

  @Test
  void testGetTerm() {
    UUID uuid = UUID.fromString("243089c1-b6ab-318f-bec9-e1cfaf410992");
    String versionTag = "20190801";
    Term t = Clinical_Rule;

    ConceptDescriptor cd1 = server.getTerm(uuid, versionTag, t.getUuid().toString())
        .orElseGet(Assertions::fail);
    ConceptDescriptor cd11 = server.getTerm(uuid, versionTag, t.getTag())
        .orElseGet(Assertions::fail);
    ConceptDescriptor cd12 = server.getTerm(uuid, versionTag, t.getResourceId().toString())
        .orElseGet(Assertions::fail);

    ConceptDescriptor cd2 = refServer.getTerm(uuid, versionTag, t.getUuid().toString())
        .orElseGet(Assertions::fail);

    Diff diff = JaversBuilder.javers().build().compare(cd1, cd2);
    assertTrue(diff.getChanges().isEmpty());

    assertSame(cd1, cd11);
    assertSame(cd1, cd12);
  }


  @Test
  void testLookupTerm() {
    Term t = Clinical_Rule;

    ConceptDescriptor cd1 = server.lookupTerm(t.getUuid().toString())
        .orElseGet(Assertions::fail);
    ConceptDescriptor cd11 = server.lookupTerm(t.getTag())
        .orElseGet(Assertions::fail);
    ConceptDescriptor cd12 = server.lookupTerm(t.getResourceId().toString())
        .orElseGet(Assertions::fail);

    ConceptDescriptor cd2 = refServer.lookupTerm(t.getUuid().toString())
        .orElseGet(Assertions::fail);

    Diff diff = JaversBuilder.javers().build().compare(cd1, cd2);
    assertTrue(diff.getChanges().isEmpty());

    assertSame(cd1, cd11);
    assertSame(cd1, cd12);
  }

  @Test
  void testLookupFailure() {
    String random = UUID.randomUUID().toString();
    assertTrue(server.lookupTerm(random).isNotFound());
    assertTrue(refServer.lookupTerm(random).isNotFound());
  }

}
