package edu.mayo.kmdp.terms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import edu.mayo.kmdp.health.datatype.ApplicationComponent;
import edu.mayo.kmdp.health.datatype.Status;
import edu.mayo.kmdp.util.Util;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._20200801.api.terminology.v4.server.TermsApiInternal;
import org.omg.spec.api4kp._20200801.services.KPComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = {TermsTestConfig.class})
class TermsHealthTest {

  @Autowired
  @KPComponent(implementation = "broker")
  TermsApiInternal provider;

  @Test
  void testHealthDescriptor() {
    ApplicationComponent ac = TermsHealthUtils.diagnoseTermsServer(provider).get();
    assertNotNull(ac);

    assertEquals(Status.UP, ac.getStatus());
    assertEquals(1, ac.getDetails().size());

    assertEquals(2, ac.getComponents().size());
    for (ApplicationComponent sub : ac.getComponents()) {
      assertEquals(2, sub.getDetails().size());
      assertEquals(Status.UP, sub.getStatus());
      assertFalse(Util.isEmpty(sub.getStatusMessage()));
    }
  }
}
