package edu.mayo.kmdp.terms;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.omg.spec.api4kp._20200801.AbstractCarrier.codedRep;
import static org.omg.spec.api4kp._20200801.AbstractCarrier.rep;
import static org.omg.spec.api4kp._20200801.id.IdentifierConstants.VERSION_ZERO;
import static org.omg.spec.api4kp._20200801.surrogate.SurrogateBuilder.newSurrogate;
import static org.omg.spec.api4kp._20200801.taxonomy.knowledgeassetcategory.KnowledgeAssetCategorySeries.Terminology_Ontology_And_Assertional_KBs;
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
import edu.mayo.kmdp.util.Util;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.UUID;
import org.omg.spec.api4kp._20200801.AbstractCarrier;
import org.omg.spec.api4kp._20200801.AbstractCarrier.Encodings;
import org.omg.spec.api4kp._20200801.Answer;
import org.omg.spec.api4kp._20200801.id.IdentifierConstants;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.id.SemanticIdentifier;
import org.omg.spec.api4kp._20200801.services.KnowledgeCarrier;
import org.omg.spec.api4kp._20200801.services.SyntacticRepresentation;
import org.omg.spec.api4kp._20200801.surrogate.KnowledgeAsset;
import org.omg.spec.api4kp._20200801.surrogate.Publication;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeassettype.KnowledgeAssetTypeSeries;

public class TermsTestUtil {

  public static void prepopulateWithKnownKMDTaxonomy(KnowledgeAssetRepositoryService kars) {
    URI conceptScheme = URI.create(
        "https://www.omg.org/spec/API4KP/20200801/taxonomy/KnowledgeAssetType#"
            + "243089c1-b6ab-318f-bec9-e1cfaf410992");
    URI conceptScheme2 = URI.create(
        "https://www.omg.org/spec/API4KP/20200801/taxonomy/ClinicalKnowledgeAssetType#"
            + "472ab418-8d62-3a72-9b4e-a7dc14530263");
    prepopulate(
        conceptScheme,
        "20210401",
        "/skos/edu/mayo/kmdp/vocabs/KnowledgeAssetTypes.skos.rdf",
        kars);
    prepopulate(
        conceptScheme2,
        "20210401",
        "/skos/edu/mayo/kmdp/vocabs/ClinicalKnowledgeAssetTypes.skos.rdf",
        kars);
  }

  public static void prepopulate(
      URI conceptScheme,
      String versionTag,
      String skosPath,
      KnowledgeAssetRepositoryService kars) {
    JenaRdfParser serializer = new JenaRdfParser();
    FHIR3Deserializer fhirSerializer = new FHIR3Deserializer();
    SyntacticRepresentation fhirRep = rep(FHIR_STU3, JSON, Charset.defaultCharset(), Encodings.DEFAULT);
    SKOStoCodeSystemTranscreator toCodeSystem = new SKOStoCodeSystemTranscreator();

    ResourceIdentifier skosAssetId = SemanticIdentifier.newId(
        UUID.fromString(conceptScheme.getFragment()),
        versionTag);

    InputStream skos = TermsFHIRBasedTest.class.getResourceAsStream(skosPath);
    Answer<KnowledgeCarrier> skosModel = Answer.of(
        AbstractCarrier.of(
            skos,
            rep(OWL_2, XML_1_1, Charset.defaultCharset(), Encodings.DEFAULT))
            .withAssetId(skosAssetId)
    );

    KnowledgeCarrier fhirScheme = skosModel
        .flatMap(
            kc -> serializer.applyLift(kc, Abstract_Knowledge_Expression, codedRep(OWL_2), null))
        .flatMap(
            kc -> toCodeSystem.applyTransrepresent(kc, codedRep(FHIR_STU3), null))
        .flatMap(
            kc -> fhirSerializer.applyLower(kc, Encoded_Knowledge_Expression, codedRep(fhirRep), null))
        .get()
        .withAssetId(skosAssetId)
        .withArtifactId(SemanticIdentifier.newId(Util.uuid(skosPath), VERSION_ZERO));

    KnowledgeAsset surrogate = newSurrogate(skosAssetId).get()
        .withFormalCategory(Terminology_Ontology_And_Assertional_KBs)
        .withFormalType(KnowledgeAssetTypeSeries.Lexicon)
        .withLifecycle(new Publication().withPublicationStatus(Published));

    Answer<Void> ans1 = kars.setKnowledgeAssetVersion(
        surrogate.getAssetId().getUuid(),
        surrogate.getAssetId().getVersionTag(),
        surrogate);
    assertTrue(ans1.isSuccess());
    Answer<Void> ans2 = kars.addKnowledgeAssetCarrier(surrogate.getAssetId().getUuid(),surrogate.getAssetId().getVersionTag(),fhirScheme);
    assertTrue(ans2.isSuccess());
  }

}
