package edu.mayo.kmdp.terms;

import edu.mayo.kmdp.health.datatype.ApplicationComponent;
import edu.mayo.kmdp.health.datatype.MiscProperties;
import edu.mayo.kmdp.health.datatype.Status;
import edu.mayo.kmdp.health.utils.MonitorUtil;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.omg.spec.api4kp._20200801.Answer;
import org.omg.spec.api4kp._20200801.api.terminology.v4.server.TermsApiInternal;
import org.omg.spec.api4kp._20200801.id.Pointer;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;

/**
 * Utility class to be used with kmdp-spring-monitoring
 * <p>
 * Diagnoses the health status of a Terminology Provider, regardless of its use as a standalone
 * application, or as an embedded component
 */
public class TermsHealthUtils {

  public static final String TERMS_TYPE = "edu.mayo.kmdp.terms.type";
  public static final String TERMS_SOURCE = "edu.mayo.kmdp.terms.source";

  private TermsHealthUtils() {
    // functions only
  }

  /**
   * Provides a health check function for a given Terminology Provider.
   * <p>
   * Supports {@link CompositeTermsServer} as {@link ApplicationComponent} with sub-components
   *
   * @param terms the {@link TermsApiInternal} implementation
   * @return a {@link Supplier} that assesses the health status of the Terminology provider when
   * called
   */
  public static Supplier<ApplicationComponent> diagnoseTermsServer(TermsApiInternal terms) {
    if (terms instanceof CompositeTermsServer) {
      CompositeTermsServer ct = (CompositeTermsServer) terms;
      switch (ct.getType()) {
        case BROKER:
          return () -> diagnoseComposite(ct);
        case ENUM:
          return () -> diagnoseEnumBased(ct);
        case FHIR:
          return () -> diagnoseFhirBased(ct);
        default:
          // fall back to default
      }
    }
    return () -> diagnoseGeneric(terms);
  }

  /**
   * @param terms a {@link CompositeTermsServer} of type BROKER
   * @return an {@link ApplicationComponent} that describes the health status of the server, with
   * nested sub-components
   */
  private static ApplicationComponent diagnoseComposite(CompositeTermsServer terms) {
    ApplicationComponent c = new ApplicationComponent();
    c.setName("Composite Terminology Provider");

    MiscProperties mp = new MiscProperties();
    mp.put(TERMS_TYPE, terms.getType().name());
    c.setDetails(mp);

    terms.getFHIRBasedComponent()
        .ifPresent(enumTerms -> c.addComponentsItem(diagnoseFhirBased(enumTerms)));
    terms.getEnumBasedComponent()
        .ifPresent(enumTerms -> c.addComponentsItem(diagnoseEnumBased(enumTerms)));

    if (c.getComponents().isEmpty()) {
      c.setStatus(Status.IMPAIRED);
      c.setStatusMessage("Terminology Broker with NO components");
    } else {
      c.setStatus(MonitorUtil.defaultAggregateStatus(c.getComponents()));
    }

    return c;
  }

  /**
   * @param terms an Enum-based Terminology Provider
   * @return an {@link ApplicationComponent} that describes the health status of the server
   */
  private static ApplicationComponent diagnoseEnumBased(TermsApiInternal terms) {
    return diagnoseGeneric(terms, "Enum Based Terminology Provider");
  }

  /**
   * @param terms a FHIR-based Terminology Provider
   * @return an {@link ApplicationComponent} that describes the health status of the server
   */
  private static ApplicationComponent diagnoseFhirBased(TermsApiInternal terms) {
    return diagnoseGeneric(terms, "FHIR Based Terminology Provider");
  }

  /**
   * Generic diagnosis method for Terminology Providers
   *
   * @param terms a Terminology Provider
   * @param name  the name of the Terminology Provider
   * @return an {@link ApplicationComponent} that describes the health status of the server
   */
  private static ApplicationComponent diagnoseGeneric(TermsApiInternal terms, String name) {
    ApplicationComponent c = new ApplicationComponent();
    c.setName(name);

    addDetails(terms, c);

    assessStatus(terms, c);
    return c;
  }

  /**
   * @param terms a generic Terminology Provider that is neither a broker, a fhir-based or
   *              enum-based server
   * @return an {@link ApplicationComponent} that describes the health status of the server
   */
  private static ApplicationComponent diagnoseGeneric(TermsApiInternal terms) {
    ApplicationComponent c = new ApplicationComponent();
    c.setName("Terminology Provider");
    if (terms == null) {
      c.setStatusMessage("No Terminology Provider Available");
      c.setStatus(Status.DOWN);
    } else {
      assessStatus(terms, c);
    }
    return c;
  }

  /**
   * Adds server-specific configuration properties to an {@link ApplicationComponent} descriptor
   *
   * @param terms the terminology provider
   * @param c     the health status descriptor to add {@link MiscProperties} details to
   */
  private static void addDetails(TermsApiInternal terms, ApplicationComponent c) {
    MiscProperties mp = new MiscProperties();
    if (terms instanceof CompositeTermsServer) {
      CompositeTermsServer ct = (CompositeTermsServer) terms;
      mp.put(TERMS_TYPE, ct.getType().name());
      mp.put(TERMS_SOURCE, ct.getSource());
    }
    c.setDetails(mp);
  }


  /**
   * Core method that assesses the health status of a Terminology provider.
   * <p>
   * Uses TermsApiInternal#listTerminologies to determine impairment, considering a server with no
   * loaded terminologies IMPAIRED
   *
   * @param terms the terminology provider
   * @param c     the health status descriptor to add {@link Status} details to
   */
  private static void assessStatus(TermsApiInternal terms, ApplicationComponent c) {
    Answer<List<Pointer>> terminologies = terms.listTerminologies();
    if (terminologies.isFailure()) {
      c.setStatusMessage("Unable to access vocabularies : " + terminologies.printExplanation());
      c.setStatus(Status.IMPAIRED);
    } else if (terminologies.get().isEmpty()) {
      c.setStatusMessage("No Terminologies available");
      c.setStatus(Status.IMPAIRED);
    } else {
      c.setStatusMessage("Active vocabularies : " + terminologies.get().stream()
          .map(ResourceIdentifier::getTag)
          .collect(Collectors.joining(","))
      );
      c.setStatus(Status.UP);
    }
  }
}
