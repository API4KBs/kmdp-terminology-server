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

import static edu.mayo.ontology.taxonomies.propositionalconcepts.PropositionalConcepts.SCHEME_NAME;
import static edu.mayo.ontology.taxonomies.propositionalconcepts.PropositionalConcepts.seriesUri;

import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.terms.ConceptScheme;
import edu.mayo.kmdp.terms.v3.server.TermsApiInternal;
import edu.mayo.ontology.taxonomies.kao.knowledgeassettype.KnowledgeAssetTypeSeries;
import edu.mayo.ontology.taxonomies.propositionalconcepts.PropositionalConcepts;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Named;
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.identifiers.ConceptIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.Pointer;
import org.omg.spec.api4kp._1_0.services.KPServer;

@Named
@KPServer
public class TermsProvider implements TermsApiInternal {

  @Override
  public Answer<List<ConceptIdentifier>> getTerms(UUID vocabularyId, String versionTag,
      String label) {
    return Answer.of(
        Arrays.stream(
            edu.mayo.ontology.taxonomies.propositionalconcepts._20200109.PropositionalConcepts
                .values())
            .map(Term::asConcept)
            .collect(Collectors.toList())
    );
  }

  @Override
  public Answer<List<Pointer>> listTerminologies() {
    Pointer ptr = new Pointer()
        .withName(SCHEME_NAME)
        .withType(KnowledgeAssetTypeSeries.Value_Set.getConceptId())
        .withEntityRef(seriesUri);
    return Answer.of(Collections.singletonList(ptr));
  }
}
