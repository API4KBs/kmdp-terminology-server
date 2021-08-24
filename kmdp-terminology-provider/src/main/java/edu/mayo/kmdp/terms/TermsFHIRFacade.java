/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.mayo.kmdp.terms;

import static edu.mayo.kmdp.util.Util.isEmpty;
import static org.omg.spec.api4kp._20200801.AbstractCarrier.codedRep;
import static org.omg.spec.api4kp._20200801.id.SemanticIdentifier.newId;
import static org.omg.spec.api4kp._20200801.id.SemanticIdentifier.newKey;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.FHIR_STU3;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import edu.mayo.kmdp.util.DateTimeUtil;
import edu.mayo.kmdp.util.NameUtils;
import edu.mayo.kmdp.util.URIUtil;
import edu.mayo.kmdp.util.Util;
import edu.mayo.ontology.taxonomies.ws.responsecodes.ResponseCodeSeries;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.hl7.fhir.dstu3.model.CodeSystem;
import org.hl7.fhir.dstu3.model.CodeSystem.ConceptDefinitionComponent;
import org.hl7.fhir.dstu3.model.CodeSystem.ConceptDefinitionDesignationComponent;
import org.omg.spec.api4kp._20200801.AbstractCarrier;
import org.omg.spec.api4kp._20200801.Answer;
import org.omg.spec.api4kp._20200801.api.repository.asset.v4.KnowledgeAssetCatalogApi;
import org.omg.spec.api4kp._20200801.api.repository.asset.v4.KnowledgeAssetRepositoryApi;
import org.omg.spec.api4kp._20200801.api.terminology.v4.server.TermsApiInternal;
import org.omg.spec.api4kp._20200801.id.IdentifierConstants;
import org.omg.spec.api4kp._20200801.id.KeyIdentifier;
import org.omg.spec.api4kp._20200801.id.Pointer;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.id.Term;
import org.omg.spec.api4kp._20200801.services.KPComponent;
import org.omg.spec.api4kp._20200801.surrogate.KnowledgeAsset;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeassettype.KnowledgeAssetTypeSeries;
import org.omg.spec.api4kp._20200801.terms.model.ConceptDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@KPComponent(implementation = "fhir")
public class TermsFHIRFacade implements TermsApiInternal {

  static Logger logger = LoggerFactory.getLogger(TermsFHIRFacade.class);
  static IParser fhirParser = FhirContext.forDstu3().newJsonParser();

  @Value("${edu.mayo.kmdp.kasrs.repository.defaultRepoUrl:http://localhost:8080/kar}")
  protected String kasrURL;

  protected KnowledgeAssetCatalogApi cat;
  protected KnowledgeAssetRepositoryApi repo;
  boolean online;

  private final Map<KeyIdentifier, Pointer> schemePointers = new HashMap<>();
  private final Map<KeyIdentifier, CodeSystem> schemeIndex = new HashMap<>();
  private final Map<UUID, ConceptDescriptor> conceptIndex = new HashMap<>();

  public TermsFHIRFacade() {
    // nothing to do - @PostConstruct will initialize the data structures
  }

  public TermsFHIRFacade(KnowledgeAssetCatalogApi cat, KnowledgeAssetRepositoryApi repo) {
    // test constructor
    this.cat = cat;
    this.repo = repo;

    reindex();
  }

  @PostConstruct
  void init() {
    if (cat == null && repo == null) {
      cat = KnowledgeAssetCatalogApi.newInstance(kasrURL);
      repo = KnowledgeAssetRepositoryApi.newInstance(kasrURL);
    }

    reindex();
  }

  @Override
  public Answer<List<Pointer>> listTerminologies() {
    if (!online) {
      return Answer.unsupported();
    }
    return Answer.of(new ArrayList<>(schemePointers.values()));
  }

  @Override
  public Answer<Void> clearTerminologies() {
    if (! online) {
      return Answer.unsupported();
    }
    return reindex();
  }

  @Override
  public Answer<ConceptDescriptor> getTerm(UUID vocabularyId, String versionTag, String conceptId) {
    if (!online) {
      return Answer.unsupported();
    }
    KeyIdentifier key = newId(vocabularyId, versionTag).asKey();
    return schemeIndex.containsKey(key)
        ? lookupTerm(conceptId)
        : Answer.notFound();
  }

  @Override
  public Answer<List<ConceptDescriptor>> getTerms(
      UUID vocabularyId, String versionTag,
      String labelFilter) {
    if (!online) {
      return Answer.unsupported();
    }
    KeyIdentifier key = newKey(vocabularyId, versionTag);
    return Answer.of(schemeIndex.get(key))
        .map(cs -> cs.getConcept().stream()
            .map(cd -> codeToUUID(cd.getCode()))
            .map(conceptIndex::get)
            .collect(Collectors.toList()));
  }

  @Override
  public Answer<ConceptDescriptor> lookupTerm(String conceptId) {
    if (!online) {
      return Answer.unsupported();
    }
    UUID uuid = Util.ensureUUID(conceptId)
        .or(() -> Util.ensureUUID(NameUtils.getTrailingPart(conceptId)))
        .orElseGet(() -> Util.uuid(conceptId));
    return Answer.ofNullable(conceptIndex.get(uuid));
  }

  Answer<Void> reindex() {
    online = cat.getKnowledgeAssetCatalog().isSuccess();
    if (!online) {
      logger.error(
          "TermsFHIRFacade reindex was not successful.  Unable to access KAC.  Content was not updated.");
      return Answer.of(ResponseCodeSeries.NotFound)
          .withExplanation(
              "TermsFHIRFacade reindex was not successful.  Unable to access KAC.  Content was not updated.");
    }

    Index collector = new Index();
    Answer<Void> ans = cat
        .listKnowledgeAssets(KnowledgeAssetTypeSeries.Lexicon.getTag(), null, null, 0, -1)
        .forEach(Pointer.class, ptr -> indexCodeSystemAsset(ptr, collector));
    if (!ans.isSuccess()) {
      logger.error("TermsFHIRFacade reindex was not successful. Original content unchanged.");
      ans.withExplanation(
          "TermsFHIRFacade reindex was not successful. Original content unchanged.");
    }
    transferContentToPrimary(collector);
    return ans;
  }

  private void indexCodeSystemAsset(Pointer karsPointer, Index collector) {
    Answer<KnowledgeAsset> ans =
        cat.getKnowledgeAssetVersion(karsPointer.getUuid(), karsPointer.getVersionTag());
    ans.ifPresent(asset -> {
      if (asset.getSecondaryId().isEmpty()) {
        logger.warn("Missing secondary ID for asset {} - {}",
            asset.getAssetId().getUuid(), asset.getName());
        logger.trace("Asset primary ID should be SemVer-based : {}, "
                + " while missing secondary ID should be date-based",
            asset.getAssetId().getVersionTag());
      }
      Optional<CodeSystem> artf = fetchCodeSystemArtifact(asset);
      // secondary ID : Taxonomies use date-based versioning - future reconsider?
      Pointer taxonomyPtr = asset.getSecondaryId().isEmpty()
          ? asset.getAssetId().toPointer()
          : asset.getSecondaryId().get(0).toPointer();
      KeyIdentifier key = taxonomyPtr.asKey();
      collector.tempSchemePointers.put(key, taxonomyPtr);
      artf.ifPresent(cs -> indexCodeSystem(key, cs, collector));
    });
  }

  private void indexCodeSystem(KeyIdentifier key, CodeSystem cs, Index collector) {
    collector.tempSchemeIndex.put(key, cs);
    cs.getConcept().stream()
        .map(cd -> toConceptDescriptor(cd, cs))
        .forEach(cd -> collector.tempConceptIndex.put(cd.getUuid(), cd));
  }

  private Optional<CodeSystem> fetchCodeSystemArtifact(KnowledgeAsset asset) {
    if (asset.getCarriers().isEmpty()) {
      return Optional.empty();
    } else if (asset.getCarriers().size() == 1) {
      return repo.getKnowledgeAssetVersionCanonicalCarrier(
          asset.getAssetId().getUuid(),
          asset.getAssetId().getVersionTag(),
          codedRep(FHIR_STU3))
          .flatOpt(AbstractCarrier::asBinary)
          .map(ByteArrayInputStream::new)
          .map(bais -> fhirParser.parseResource(CodeSystem.class, bais))
          .getOptionalValue();
    } else {
      return asset.getCarriers().stream()
          .map(carrier -> fetchCodeSystemArtifact(asset.getAssetId(), carrier.getArtifactId()))
          .flatMap(Answer::trimStream)
          .reduce(this::mergeCodeSystems);
    }
  }

  private CodeSystem mergeCodeSystems(CodeSystem cs, CodeSystem cs2) {
    cs.getConcept().addAll(cs2.getConcept());
    return cs;
  }

  private Answer<CodeSystem> fetchCodeSystemArtifact(
      ResourceIdentifier assetId, ResourceIdentifier artifactId) {
    return repo.getKnowledgeAssetCarrierVersion(
        assetId.getUuid(), assetId.getVersionTag(),
        artifactId.getUuid(), artifactId.getVersionTag(),
        codedRep(FHIR_STU3))
        .flatOpt(AbstractCarrier::asBinary)
        .map(ByteArrayInputStream::new)
        .map(bais -> fhirParser.parseResource(CodeSystem.class, bais));
  }

  private synchronized void transferContentToPrimary(Index collector) {
    schemePointers.clear();
    schemePointers.putAll(collector.tempSchemePointers);

    schemeIndex.clear();
    schemeIndex.putAll(collector.tempSchemeIndex);

    conceptIndex.clear();
    conceptIndex.putAll(collector.tempConceptIndex);

    collector.clear();
  }

  private ConceptDescriptor toConceptDescriptor(ConceptDefinitionComponent cd, CodeSystem cs) {
    ConceptDescriptor descr = new ConceptDescriptor();
    descr.withLabels(mapDesignations(cd))
        .withTag(cd.getCode())
        .withResourceId(conceptId(cs.getUrl(), codeToUUID(cd.getCode()).toString()))
        .withUuid(codeToUUID(cd.getCode()))
        .withName(cd.getDisplay())
        .withVersionTag(cs.getVersion())
        .withVersionId(conceptId(cs.getUrl(), cs.getVersion(), codeToUUID(cd.getCode()).toString()))
        .withNamespaceUri(URIUtil.normalizeURI(URI.create(cs.getUrl())))
        .withEstablishedOn(DateTimeUtil.parseDate(cs.getVersion(), "yyyyMMdd"))
        .withReferentId(isEmpty(cd.getDefinition()) ? null : URI.create(cd.getDefinition()));
    descr.setAncestors(new Term[0]);
    descr.setClosure(new Term[0]);
    return descr;
  }

  private Map<String,String> mapDesignations(ConceptDefinitionComponent cd) {
    return cd.getDesignation().stream()
        .collect(Collectors.toMap(
            dx -> dx.getUse().getCode(),
            ConceptDefinitionDesignationComponent::getValue));
  }

  private UUID codeToUUID(String code) {
    return Util.ensureUUID(code).orElseGet(() -> Util.uuid(code));
  }

  private URI conceptId(String url, String version, String code) {
    String ns = url;
    int idx = url.lastIndexOf('#');
    if (idx > 0) {
      ns = ns.substring(0, idx);
    }
    if (Util.isNotEmpty(version)) {
      ns = ns + IdentifierConstants.VERSIONS + version;
    }
    if (Util.isNotEmpty(code)) {
      ns = ns + "#" + code;
    }
    return URI.create(ns);
  }

  private URI conceptId(String url, String code) {
    return conceptId(url, null, code);
  }


  private static class Index {
    final Map<KeyIdentifier, Pointer> tempSchemePointers = new HashMap<>();
    final Map<KeyIdentifier, CodeSystem> tempSchemeIndex = new HashMap<>();
    final Map<UUID, ConceptDescriptor> tempConceptIndex = new HashMap<>();

    public void clear() {
      tempConceptIndex.clear();
      tempSchemeIndex.clear();
      tempConceptIndex.clear();
    }
  }
}
