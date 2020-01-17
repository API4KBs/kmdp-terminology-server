/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.mayo.kmdp.kdcaci.knew.terms;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.mayo.kmdp.util.FileUtil;
import edu.mayo.kmdp.util.Util;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

class CompatibilityTest {

  private static final String CSO_NS = "http://ontology.mayo.edu/ontologies/clinicalsituationontology/";
  private static final String CSV_NS = "https://ontology.mayo.edu/taxonomies/clinicalsituations#";
  private static final String PCV_NS = "https://ontology.mayo.edu/taxonomies/propositionalconcepts#";

  @Test
  public void testCSODocument() {
    InputStream input = CompatibilityTest.class
        .getResourceAsStream("/owl/edu/mayo/kmdp/vocabs/LATEST/cso.ttl");
    assertDoesNotThrow( () -> checkUTF8(input));
  }

  @Test
  public void testCSVDocument() {
    InputStream input = CompatibilityTest.class
        .getResourceAsStream("/skos/edu/mayo/kmdp/vocabs/sco.skos.rdf");
    assertDoesNotThrow( () -> checkUTF8(input));
  }

  @Test
  public void testPCVDocument() {
    InputStream input = CompatibilityTest.class
        .getResourceAsStream("/skos/edu/mayo/kmdp/vocabs/LATEST/pcv.skos.ttl");
    assertDoesNotThrow( () -> checkUTF8(input));
  }

  private void checkUTF8(InputStream input) throws UnsupportedEncodingException {
    Optional<String> x = FileUtil.read(input);
    assertTrue(x.isPresent());

    String s = x.orElse("");
    byte[] utf8Bytes = s.getBytes(StandardCharsets.US_ASCII);
    String roundtrip = new String(utf8Bytes);
    String delta = StringUtils.difference(s,roundtrip);
    if (!Util.isEmpty(delta)) {
      throw new UnsupportedEncodingException();
    }
  }
}
