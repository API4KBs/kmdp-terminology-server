package edu.mayo.kmdp.terms;

import static org.omg.spec.api4kp._20200801.Answer.firstDo;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.omg.spec.api4kp._20200801.Answer;
import org.omg.spec.api4kp._20200801.api.terminology.v4.server.TermsApiInternal;
import org.omg.spec.api4kp._20200801.id.Pointer;
import org.omg.spec.api4kp._20200801.services.KPComponent;
import org.omg.spec.api4kp._20200801.services.KPServer;
import org.omg.spec.api4kp._20200801.terms.model.ConceptDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@KPServer
@KPComponent(implementation = "broker")
@Component
public class TermsBrokerImpl implements TermsApiInternal, CompositeTermsServer {

  @Autowired
  @KPComponent(implementation = "enum")
  protected TermsApiInternal enumDrivenTerms;

  @Autowired
  @KPComponent(implementation = "fhir")
  protected TermsApiInternal fhirAssetDrivenTerms;

  public TermsBrokerImpl() {
    //
  }

  public TermsBrokerImpl(TermsFHIRFacade fhirAssetDrivenTerms, TermsProvider enumDrivenTerms) {
    this.enumDrivenTerms = enumDrivenTerms;
    this.fhirAssetDrivenTerms = fhirAssetDrivenTerms;
  }

  List<TermsApiInternal> providers() {
    return Arrays.asList(fhirAssetDrivenTerms, enumDrivenTerms);
  }

  @Override
  public Answer<Void> clearTerminologies() {
    return this.fhirAssetDrivenTerms.clearTerminologies();
  }

  @Override
  public Answer<List<Pointer>> listTerminologies() {
    return firstDo(providers(),
        TermsApiInternal::listTerminologies);
  }

  @Override
  public Answer<ConceptDescriptor> getTerm(UUID vocabularyId, String versionTag, String conceptId) {
    return firstDo(providers(),
        t -> t.getTerm(vocabularyId, versionTag, conceptId));
  }

  @Override
  public Answer<List<ConceptDescriptor>> getTerms(
      UUID vocabularyId, String versionTag,
      String labelFilter) {
    return firstDo(providers(),
        t -> t.getTerms(vocabularyId, versionTag, labelFilter));
  }

  @Override
  public Answer<ConceptDescriptor> lookupTerm(String conceptId) {
    return firstDo(providers(),
        t -> t.lookupTerm(conceptId));
  }

  @Override
  public TYPE getType() {
    return TYPE.BROKER;
  }

  @Override
  public String getSource() {
    return null;
  }

  @Override
  public Optional<TermsApiInternal> getEnumBasedComponent() {
    return Optional.of(enumDrivenTerms);
  }

  @Override
  public Optional<TermsApiInternal> getFHIRBasedComponent() {
    return Optional.of(fhirAssetDrivenTerms);
  }
}
