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
package org.omg.demo.terms;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.terms.v4.server.TermsApiInternal;
import edu.mayo.ontology.taxonomies.kao.knowledgeassettype.KnowledgeAssetTypeSeries;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.collections.map.MultiKeyMap;
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.identifiers.ConceptIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.Pointer;
import org.omg.spec.api4kp._1_0.identifiers.URIIdentifier;
import org.omg.spec.api4kp._1_0.services.KPServer;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import javax.inject.Named;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Named
@KPServer
/**
 *  This class reads a terminology json file created by the terminology plugin.
 *  Terminology metadata and terms are available through services.
 */
public class TermsProvider implements TermsApiInternal {

  private static Logger logger = LoggerFactory.getLogger(TermsProvider.class);
  // a map using two keys to identify the TermModel value
  private static MultiKeyMap multiKeyMap = readJson();

  public TermsProvider()  {
    super();
  }

  private static MultiKeyMap readJson() {
    multiKeyMap = MultiKeyMap.decorate(new LinkedMap());
    try {
      ClassPathResource cpr = new ClassPathResource("terminologies.json");

      // json file is stored in the classes directory during the build
      TerminologyModel[] terminologies = new ObjectMapper().readValue(
              new ClassPathResource("terminologies.json").getInputStream(), TerminologyModel[].class);

      // for each terminology, set the metadata and terms
      for (TerminologyModel terminology : terminologies) {
        UUID id = UUID.fromString(terminology.getSchemeId());
        String version = terminology.getVersion();
        multiKeyMap.put(id, version, terminology);
        List<Term> terms = doGetTerms(id, version);
        terminology.setTerms(terms);
      }
    }catch (Exception e) {
      logger.error(e.getMessage(),e);
      throw new RuntimeException(e);
    }
    return multiKeyMap;
  }
;

  @Override
  public Answer<ConceptIdentifier> getTerm(UUID uuid, String s, String s1) {
    return null;
  }

  @Override
  public Answer<List<ConceptIdentifier>> getTerms(UUID vocabularyId, String versionTag, String label) {
    TerminologyModel termModel = (TerminologyModel)multiKeyMap.get(vocabularyId, versionTag);
    return Answer.of(
            termModel.getTerms().stream()
                    .map(Term::asConcept)
                    .collect(Collectors.toList())
    );
  }

  @Override
  public Answer<KnowledgeCarrier> getVocabulary(UUID uuid, String s, String s1) {
    return null;
  }

  @Override
  public Answer<Void> isMember(UUID uuid, String s, String s1) {
    return null;
  }

  /**
   * Uses relection to retrieve the terms from the terminology class.
   * @param vocabularyId
   * @param versionTag
   * @return
   * @throws Exception - thrown if cannot create an instance of the Class or retrieve the terms
   */
  protected static List<Term> doGetTerms(UUID vocabularyId, String versionTag) throws Exception {
    TerminologyModel term = (TerminologyModel)multiKeyMap.get(vocabularyId, versionTag);

    Class cls = Class.forName(term.getName());
    Object obj = null;
    try {
      obj = cls.newInstance();
    } catch(Exception e)  {
      // expected exception
    }

    Method method = cls.getDeclaredMethod("values");
    List<Term> terms = Arrays.asList((Term[])method.invoke(obj, null));

    return terms;
  }

  @Override
  public Answer<List<Pointer>> listTerminologies() {
    ArrayList<Pointer> pointers = new ArrayList<>();

    Collection<TerminologyModel> terms = multiKeyMap.values();

    for(TerminologyModel term:terms) {
      Pointer ptr = new Pointer()
              .withName(term.getName())
              .withType(KnowledgeAssetTypeSeries.Value_Set.getConceptId())
              .withEntityRef((new URIIdentifier()).withUri(term.getSeriesId()));
      pointers.add(ptr);
    }
    return Answer.of(pointers);
  }

  @Override
  public Answer<Void> relatesTo(UUID uuid, String s, String s1, String s2) {
    return null;
  }
}
