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

import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.terms.ControlledTerm;
import edu.mayo.kmdp.terms.v4.server.TermsApiInternal;
import edu.mayo.ontology.taxonomies.kao.knowledgeassettype.KnowledgeAssetTypeSeries;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Named;
import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.collections.map.MultiKeyMap;
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.identifiers.ConceptIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.NamespaceIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.Pointer;
import org.omg.spec.api4kp._1_0.identifiers.URIIdentifier;
import org.omg.spec.api4kp._1_0.services.KPServer;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.reflections.Reflections;

@Named
@KPServer
public class TermsProvider implements TermsApiInternal {

  private MultiKeyMap multiKeyMap;
  private HashMap<String, URI> namesIds;

  public TermsProvider()  {
    super();

    multiKeyMap = new MultiKeyMap();
    namesIds = new HashMap<>();

    try {
      scan();
    } catch (IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException  e) {
      throw new RuntimeException(e);
    }
  }


  protected void scan() throws IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
    multiKeyMap = MultiKeyMap.decorate(new LinkedMap());
      Reflections reflections = new Reflections("edu.mayo.ontology.taxonomies");
      Set<Class<? extends ControlledTerm>> subTypes = reflections.getSubTypesOf(ControlledTerm.class);

      for(Class subtype:subTypes)  {
        Field namespace = null;
        try {
          namespace = subtype.getField("namespace");

          String name = subtype.getName();
          String version = ((NamespaceIdentifier)namespace.get(null)).getVersion();
          String schemeId = ((NamespaceIdentifier)namespace.get(null)).getTag();
          URI seriesId = ((NamespaceIdentifier)namespace.get(null)).getId();

          Class cls = Class.forName(name);
          Object obj = null;
          try {
            obj = cls.newInstance();
          } catch (InstantiationException e) {
            //e.printStackTrace();
          }

          Method method = cls.getDeclaredMethod("values");
          List<Term> terms =  Arrays.asList((Term[])method.invoke(obj, null));

          namesIds.put(name, seriesId);
          multiKeyMap.put(UUID.fromString(schemeId), version, terms);

        } catch (NoSuchFieldException e) {

        }
      }
  }

  @Override
  public Answer<ConceptIdentifier> getTerm(UUID uuid, String s, String s1) {
    return Answer.unsupported();
  }

  public Answer<List<ConceptIdentifier>> getTerms(UUID vocabularyId, String versionTag, String label) {
    return Answer.of(
            this.doGetTerms(vocabularyId, versionTag).stream()
                    .map(Term::asConcept)
                    .collect(Collectors.toList()));
  }


  protected List<Term> doGetTerms(UUID vocabularyId, String versionTag) {
    return (List<Term>)multiKeyMap.get(vocabularyId, versionTag);
  }

  @Override
  public Answer<KnowledgeCarrier> getVocabulary(UUID uuid, String s, String s1) {
    return Answer.unsupported();
  }

  @Override
  public Answer<Void> isMember(UUID uuid, String s, String s1) {
    return Answer.unsupported();
  }

  @Override
  public Answer<List<Pointer>> listTerminologies() {
    ArrayList<Pointer> pointers = new ArrayList<>();
    Collection<String> names = namesIds.keySet();

    for(String name:names) {
      Pointer ptr = new Pointer()
              .withName(name)
              .withType(KnowledgeAssetTypeSeries.Value_Set.getConceptId())
              .withEntityRef((new URIIdentifier()).withUri(namesIds.get(name)));
      pointers.add(ptr);
    }

    return Answer.of(pointers);
  }

  @Override
  public Answer<Void> relatesTo(UUID uuid, String s, String s1, String s2) {
    return Answer.unsupported();
  }
}
