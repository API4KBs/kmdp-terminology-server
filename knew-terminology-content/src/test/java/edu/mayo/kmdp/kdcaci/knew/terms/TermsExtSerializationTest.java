/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.mayo.kmdp.kdcaci.knew.terms;

import static edu.mayo.ontology.taxonomies.clinicalsituations.ClinicalSituationSeries.Has_Hypertension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.mayo.kmdp.util.JSonUtil;
import edu.mayo.kmdp.util.JaxbUtil;
import edu.mayo.kmdp.util.Util;
import edu.mayo.kmdp.util.XMLUtil;
import edu.mayo.ontology.taxonomies.clinicalsituations.ClinicalSituation;
import java.util.Collections;
import java.util.Optional;
import javax.xml.bind.annotation.XmlRootElement;
import org.junit.jupiter.api.Test;

public class TermsExtSerializationTest {

  @Test
  public void testXML() {
    String xml = JaxbUtil.marshallToString(Collections.singleton(Foo.class),
        new Foo(Has_Hypertension),
        JaxbUtil.defaultProperties());
    assertTrue(xml.contains(Has_Hypertension.getTag()));
    assertTrue(xml.contains(Has_Hypertension.getLabel()));
    assertTrue(xml.contains(Has_Hypertension.getRef().toString()));

    Optional<Foo> f2 = XMLUtil.loadXMLDocument(xml.getBytes())
        .flatMap(dox ->
            JaxbUtil.unmarshall(Collections.singleton(Foo.class), Foo.class, dox));

    assertTrue(f2.isPresent());
    assertEquals(Has_Hypertension, f2.orElse(new Foo()).getPc());
  }

  @Test
  public void testJSON() {
    String json = JSonUtil.writeJson(new Foo(Has_Hypertension)).flatMap(Util::asString)
        .orElse("");

    assertTrue(json.contains(Has_Hypertension.getConceptUUID().toString()));
    assertTrue(json.contains(Has_Hypertension.getLabel()));

    Optional<Foo> f2 = JSonUtil.readJson(json.getBytes(), Foo.class);

    assertTrue(f2.isPresent());
    assertEquals(Has_Hypertension, f2.orElse(new Foo()).getPc());
  }


  @XmlRootElement
  public static class Foo {

    private ClinicalSituation pc;

    public Foo() {

    }

    public Foo(ClinicalSituation pc) {
      this.pc = pc;
    }

    public ClinicalSituation getPc() {
      return pc;
    }

    public void setPc(ClinicalSituation pc) {
      this.pc = pc;
    }
  }
}
