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

import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.terms.v4.server.TermsApiInternal;
import edu.mayo.ontology.taxonomies.kao.knowledgeassettype.KnowledgeAssetTypeSeries;
import edu.mayo.kmdp.util.JSonUtil;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.collections.map.MultiKeyMap;
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.identifiers.ConceptIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.NamespaceIdentifier;
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
 *  If the tests fail, be sure to run parent build first so file is created in target/classes
 *  Terminology metadata and terms are available through services.
 */
public class TermsProvider implements TermsApiInternal {

   private static Logger logger = LoggerFactory.getLogger(TermsProvider.class);
  /**
   *   A map using two keys to identify the TerminologyModel value
   */
  private static MultiKeyMap multiKeyMap = readTerminologyJsonFileIntoTerminologyModels();

  public TermsProvider()  {
    super();
  }


  /**
   * Gets a list of the terminologies as Pointers containing name and URI
   * @return a list of the terminologies
   */
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

  /**
   * Get all the terms which are members of a specified version of a terminology.
   * @param vocabularyId the schemeId of the terminology
   * @param versionTag the specific version of the terminology
   * @param label the label of terminology
   * @return the terms within a specified version of terminology
   */
  @Override
  public Answer<List<ConceptIdentifier>> getTerms(UUID vocabularyId, String versionTag, String label) {
    TerminologyModel termModel = (TerminologyModel)multiKeyMap.get(vocabularyId, versionTag);
    return Answer.of(
            termModel.getTerms().stream()
                    .map(ConceptTerm::asConcept)
                    .collect(Collectors.toList())
    );
  }

  /**
   * Using the vocabularyId along with the version, a Term with the given conceptId
   * is returned.  If the term is not found, will return null.
   * @param vocabularyId - The id of the terminology system
   * @param versionTag - The version of the terminology
   * @param conceptId - The conceptId of the term
   * @return  retrieve a data payload that include terms, labels, definitions, relationships
   * for the concept identified by that ID
   */
  @Override
  public Answer<ConceptIdentifier> getTerm(UUID vocabularyId, String versionTag, String conceptId) {
    TerminologyModel termModel = (TerminologyModel)multiKeyMap.get(vocabularyId, versionTag);

    if(termModel != null) {
      List<ConceptTerm> terms = termModel.getTerms();

      for (ConceptTerm term : terms) {
        if (conceptId.equals(term.getConceptId().toString())) {
          Term[] ancestors = term.getAncestors();
          for(Term ancestor:ancestors)  {
            System.out.println(ancestor);
          }
          Term[] closures = term.getClosure();
          for(Term closure:closures)  {
            System.out.println(closure);
          }
          return Answer.of(term.asConcept());
        }
      }
    }
    return null;
  }

  /**
   * Reads the JSON file and populate the TerminologyModels
   * @return MultiKeyMap where id and version are the keys and TerminologyModel is the value
   */
  private static MultiKeyMap readTerminologyJsonFileIntoTerminologyModels() {
    multiKeyMap = MultiKeyMap.decorate(new LinkedMap());
    try {
      // json file is stored in the classes directory during the build
      Optional<TerminologyModel[]> optional = JSonUtil.readJson(new ClassPathResource("terminologies.json").getInputStream(), TerminologyModel[].class);
      TerminologyModel[] terminologies = optional.get();

      // for each terminology, set the metadata and terms
      for (TerminologyModel terminology : terminologies) {
        setTerminologyMetadata(terminology);
      }
    }catch (Exception e) {
      logger.error("Unable to read the JSON file which leaves the application in unstable state.");
      throw new RuntimeException(e);
    }
    return multiKeyMap;
  }

  /**
   * Set the terminology map using the terminologyId and version as keys.
   * Set the terms for the terminology.
   * @param terminology the TerminologyModel to be populated
   * @throws Exception - thrown if cannot create an instance of the Class or retrieve the terms
   */
  private static void setTerminologyMetadata(TerminologyModel terminology)
      throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    UUID id = UUID.fromString(terminology.getSchemeId());
    String version = terminology.getVersion();
    multiKeyMap.put(id, version, terminology);

    List<ConceptTerm> terms = getTermsFromTerminologyClass(id, version);
    terminology.setTerms(terms);
  }

  /**
   * Uses reflection to retrieve the terms from the terminology Class.
   * @param vocabularyId the schemeId for the vocabulary
   * @param versionTag the version of the terminology
   * @return the terms for the terminology
   * @throws Exception - thrown if cannot create an instance of the Class or retrieve the terms
   */
  private static List<ConceptTerm> getTermsFromTerminologyClass(UUID vocabularyId, String versionTag)
        throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    TerminologyModel term = (TerminologyModel)multiKeyMap.get(vocabularyId, versionTag);

    Class cls = Class.forName(term.getName());
    Object obj = null;
    try {
      obj = cls.newInstance();
    } catch(Exception e)  {
      // expected exception
    }
    Method method = cls.getDeclaredMethod("values");

    return Arrays.asList((ConceptTerm[])method.invoke(obj, null));
  }

  private ConceptIdentifier getConceptIdentifierFromConceptTerm(ConceptTerm term)  {
    ConceptIdentifier conId = new ConceptIdentifier();
    conId.setConceptId(term.getConceptId());
    conId.setConceptUUID(term.getConceptUUID());
    conId.setLabel(term.getLabel());
    conId.setNamespace((NamespaceIdentifier)(term.getNamespace()));
    conId.setRef(term.getRef());
    conId.setTag(term.getTag());
    return conId;
  }


  //  The methods below will be implemented at a later date.


  /**
   * Determines if two concepts are related - default by subsumption (isA)
   *
   * @param vocabularyId - The id of the terminology system
   * @param versionTag - The version of the terminology
   * @param conceptId - The conceptId of the term
   * @param relationshipId
   * @return
   */
  @Override
  public Answer<Void> relatesTo(UUID vocabularyId, String versionTag, String conceptId, String relationshipId) {
    return null;
  }

  /**
   * Returns a representation of this version of the vocabulary.
   * Supports content negotiation to handle e.g. RDF vs FHIR
   *
   * @param vocabularyId - The id of the terminology system
   * @param versionTag - The version of the terminology
   * @param xAccept
   * @return
   */
  @Override
  public Answer<KnowledgeCarrier> getVocabulary(UUID vocabularyId, String versionTag, String xAccept) {
    return null;
  }

  /**
   * Resolves a concept (expression) within a terminology system
   * Determines is a concept is a member of this vocabulary.
   * Implementations depend on whether the vocabulary is enumerated, vs having
   * a computable definition.
   * The client can provide either a concept identifier, or a post-coordinated expression
   * @param vocabularyId - The id of the terminology system
   * @param versionTag - The version of the terminology
   * @param conceptExpression
   * @return
   */
  @Override
  public Answer<Void> isMember(UUID vocabularyId, String versionTag, String conceptExpression) {
    return null;
  }



}
