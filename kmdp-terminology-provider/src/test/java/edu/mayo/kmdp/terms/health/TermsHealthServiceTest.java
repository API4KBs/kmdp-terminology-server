package edu.mayo.kmdp.terms.health;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import edu.mayo.kmdp.health.datatype.ApplicationComponent;
import edu.mayo.kmdp.health.datatype.Status;
import edu.mayo.kmdp.health.service.HealthService;
import edu.mayo.kmdp.terms.CompositeTermsServer;
import edu.mayo.kmdp.terms.CompositeTermsServer.TYPE;
import java.util.List;
import java.util.Optional;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.junit.jupiter.api.Assertions;
import org.omg.spec.api4kp._20200801.Answer;
import org.omg.spec.api4kp._20200801.api.terminology.v4.server.TermsApiInternal;
import org.omg.spec.api4kp._20200801.id.Pointer;

class TermsHealthServiceTest {

  public static final String TAG_1 = "tag1";
  public static final String TAG_2 = "tag2";
  public static final String SOURCE = "source";

  @Test
  void testNonCompositeSuccess() {

    TermsApiInternal termsApiInternal = Mockito.mock(TermsApiInternal.class);

    List<Pointer> pointerList = Lists.newArrayList();
    Pointer pointer1 = new Pointer().withTag(TAG_1);
    pointerList.add(pointer1);
    Answer<List<Pointer>> terminologiesList = Answer.of(pointerList);
    when(termsApiInternal.listTerminologies()).thenReturn(terminologiesList);

    TermsHealthService termsHealthService = new TermsHealthService(termsApiInternal);

    ApplicationComponent applicationComponent = termsHealthService.assessHealth();

    Assertions.assertEquals(Status.UP, applicationComponent.getStatus());
    Assertions.assertNotNull(applicationComponent.getDetails().get(HealthService.EXECUTION_TIME_MS));
    Assertions.assertEquals(TermsHealthService.ACTIVE_VOCABULARIES + TAG_1, applicationComponent.getStatusMessage());
    Assertions.assertNotNull(applicationComponent.getDetails().get(HealthService.EXECUTION_TIME_MS));

  }

  @Test
  void testBrokerFailureImpaired() {

    CompositeTermsServer termsApiInternal = Mockito.mock(CompositeTermsServer.class);
    when(termsApiInternal.getType()).thenReturn(TYPE.BROKER);

    List<Pointer> pointerList = Lists.newArrayList();
    Pointer pointer1 = new Pointer().withTag(TAG_1);
    pointerList.add(pointer1);
    Answer<List<Pointer>> terminologiesList = Answer.of(pointerList);
    when(termsApiInternal.listTerminologies()).thenReturn(terminologiesList);

    TermsHealthService termsHealthService = new TermsHealthService(termsApiInternal);

    ApplicationComponent applicationComponent = termsHealthService.assessHealth();

    Assertions.assertEquals(Status.IMPAIRED, applicationComponent.getStatus());
    Assertions.assertNotNull(applicationComponent.getDetails().get(HealthService.EXECUTION_TIME_MS));
    Assertions.assertEquals(TermsHealthService.TERMINOLOGY_BROKER_HAS_NO_COMPONENTS, applicationComponent.getStatusMessage());

  }

  @Test
  void testBrokerComposite() {

    CompositeTermsServer termsApiInternal = Mockito.mock(CompositeTermsServer.class);
    when(termsApiInternal.getType()).thenReturn(TYPE.BROKER);

    List<Pointer> pointerList = Lists.newArrayList();
    Pointer pointer1 = new Pointer().withTag(TAG_1);
    pointerList.add(pointer1);
    Answer<List<Pointer>> terminologiesList = Answer.of(pointerList);
    when(termsApiInternal.listTerminologies()).thenReturn(terminologiesList);



    CompositeTermsServer enumTermsApiInternal = Mockito.mock(CompositeTermsServer.class);
    when(enumTermsApiInternal.getType()).thenReturn(TYPE.ENUM);
    Optional<TermsApiInternal> enumTermsApiInternalOptional = Optional.of(enumTermsApiInternal);
    when(termsApiInternal.getEnumBasedComponent()).thenReturn(enumTermsApiInternalOptional);

    List<Pointer> pointerList2 = Lists.newArrayList();
    Pointer pointer2 = new Pointer().withTag(TAG_2);
    pointerList2.add(pointer2);
    Answer<List<Pointer>> terminologiesList2 = Answer.of(pointerList2);
    when(enumTermsApiInternal.listTerminologies()).thenReturn(terminologiesList2);



    TermsHealthService termsHealthService = new TermsHealthService(termsApiInternal);

    ApplicationComponent applicationComponent = termsHealthService.assessHealth();

    Assertions.assertEquals(Status.UP, applicationComponent.getStatus());
    Assertions.assertNotNull(applicationComponent.getDetails().get(HealthService.EXECUTION_TIME_MS));
//    Assertions.assertEquals(TermsHealthService.TERMINOLOGY_BROKER_HAS_NO_COMPONENTS, applicationComponent.getStatusMessage());

  }

}
