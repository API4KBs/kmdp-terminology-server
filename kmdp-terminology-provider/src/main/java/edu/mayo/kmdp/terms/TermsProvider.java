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
<<<<<<< HEAD
package edu.mayo.kmdp.terms;
=======
package org.omg.demo.terms;
>>>>>>> 33226 File path now being correctly set.  Added documentation.

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
<<<<<<< HEAD
 *  If the tests fail, be sure to run parent build first so file is created in target/classes
=======
>>>>>>> 33226 File path now being correctly set.  Added documentation.
 *  Terminology metadata and terms are available through services.
 */
public class TermsProvider implements TermsApiInternal {

  private static Logger logger = LoggerFactory.getLogger(TermsProvider.class);
<<<<<<< HEAD
  /**
   *   A map using two keys to identify the TerminologyModel value
   */
  private static MultiKeyMap multiKeyMap = readTerminologyJsonFileIntoTerminologyModels();
=======
  // a map using two keys to identify the TerminologyModel value
  private static MultiKeyMap multiKeyMap = readJson();
>>>>>>> 33226 File path now being correctly set.  Added documentation.

  public TermsProvider()  {
    super();
  }

<<<<<<< HEAD

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
                    .map(Term::asConcept)
                    .collect(Collectors.toList())
    );
  }


  /**
   * Reads the JSON file and populate the TerminologyModels
   * @return MultiKeyMap where id and version are the keys and TerminologyModel is the value
   */
  private static MultiKeyMap readTerminologyJsonFileIntoTerminologyModels() {
=======
  private static MultiKeyMap readJson() {
>>>>>>> 33226 File path now being correctly set.  Added documentation.
    multiKeyMap = MultiKeyMap.decorate(new LinkedMap());
    try {
      // json file is stored in the classes directory during the build
      TerminologyModel[] terminologies = new ObjectMapper().readValue(
<<<<<<< HEAD
              new ClassPathResource("terminologies.json").getInputStream(), TerminologyModel[].class);

      // for each terminology, set the metadata and terms
      for (TerminologyModel terminology : terminologies) {
        setTerminologyMetadata(terminology);
=======
              new ClassPathResource("target/generated-sources/terminologies.json").getInputStream(), TerminologyModel[].class);

      // for each terminology, set the metadata and terms
      for (TerminologyModel terminology : terminologies) {
        UUID id = UUID.fromString(terminology.getSchemeId());
        String version = terminology.getVersion();
        multiKeyMap.put(id, version, terminology);
        List<Term> terms = doGetTerms(id, version);
        terminology.setTerms(terms);
>>>>>>> 33226 File path now being correctly set.  Added documentation.
      }
    }catch (Exception e) {
      logger.error(e.getMessage(),e);
      throw new RuntimeException(e);
    }
    return multiKeyMap;
  }

<<<<<<< HEAD
  /**
   * Set the terminology map using the terminologyId and version as keys.
   * Set the terms for the terminology.
   * @param terminology the TerminologyModel to be populated
   * @throws Exception - thrown if cannot create an instance of the Class or retrieve the terms
   */
  private static void setTerminologyMetadata(TerminologyModel terminology) throws Exception  {
    UUID id = UUID.fromString(terminology.getSchemeId());
    String version = terminology.getVersion();
    multiKeyMap.put(id, version, terminology);

    List<Term> terms = getTermsFromTerminologyClass(id, version);
    terminology.setTerms(terms);
  }

  /**
   * Uses reflection to retrieve the terms from the terminology Class.
   * @param vocabularyId the schemeId for the vocabulary
   * @param versionTag the version of the terminology
   * @return the terms for the terminology
   * @throws Exception - thrown if cannot create an instance of the Class or retrieve the terms
   */
  private static List<Term> getTermsFromTerminologyClass(UUID vocabularyId, String versionTag) throws Exception {
=======
  @Override
  public Answer<List<ConceptIdentifier>> getTerms(UUID vocabularyId, String versionTag, String label) {
    TerminologyModel termModel = (TerminologyModel)multiKeyMap.get(vocabularyId, versionTag);
    return Answer.of(
            termModel.getTerms().stream()
                    .map(Term::asConcept)
                    .collect(Collectors.toList())
    );
  }


  /**
   * Uses relection to retrieve the terms from the terminology class.
   * @param vocabularyId
   * @param versionTag
   * @return
   * @throws Exception - thrown if cannot create an instance of the Class or retrieve the terms
   */
  protected static List<Term> doGetTerms(UUID vocabularyId, String versionTag) throws Exception {
>>>>>>> 33226 File path now being correctly set.  Added documentation.
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

<<<<<<< HEAD

  //  The methods below will be implemented at a later date.

  /**
   * **** still under development - user story 40685 ***
   * Describes a concept
   * @param vocabularyId - The id of the terminology system
   * @param versionTag - The version of the terminology
   * @param conceptId - The conceptId of the term
   * @return  retrieve a data payload that include terms, labels, definitions, relationships
   * for the concept identified by that ID
   */
  @Override
  public Answer<ConceptIdentifier> getTerm(UUID vocabularyId, String versionTag, String conceptId) {
    TerminologyModel termModel = (TerminologyModel)multiKeyMap.get(vocabularyId, versionTag);
    List<Term> terms = termModel.getTerms();
    for(Term term:terms)  {
//      System.out.println(term.getConceptId());
      if(conceptId.equals("" +term.getConceptId()))  {
//        System.out.println("Found - " +term.getConceptUUID());
//        Answer<> conId = Answer.of(
//                termModel.getTerms().stream()
//                        .map(Term::asConcept));
//        conId.toString();
        break;
      }
    }
//    System.out.println("doing great");
    return null;
  }

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


=======
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

  @Override
  public Answer<KnowledgeCarrier> getVocabulary(UUID uuid, String s, String s1) {
    return null;
  }

  @Override
  public Answer<Void> isMember(UUID uuid, String s, String s1) {
    return null;
  }

  @Override
  public Answer<ConceptIdentifier> getTerm(UUID uuid, String s, String s1) {
    return null;
  }
>>>>>>> 33226 File path now being correctly set.  Added documentation.

}
