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

import com.google.common.collect.Lists;
import edu.mayo.kmdp.comparator.Contrastor.Comparison;
import edu.mayo.kmdp.terms.exceptions.TermProviderException;
import edu.mayo.kmdp.terms.impl.model.TerminologyScheme;
import edu.mayo.kmdp.util.JSonUtil;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalQueries;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Named;
import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.collections.map.MultiKeyMap;
import org.omg.spec.api4kp._20200801.Answer;
import org.omg.spec.api4kp._20200801.api.terminology.v4.server.TermsApiInternal;
import org.omg.spec.api4kp._20200801.id.Pointer;
import org.omg.spec.api4kp._20200801.id.SemanticIdentifier;
import org.omg.spec.api4kp._20200801.id.Term;
import org.omg.spec.api4kp._20200801.id.VersionTagContrastor;
import org.omg.spec.api4kp._20200801.services.KPServer;
import org.omg.spec.api4kp._20200801.services.KnowledgeCarrier;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeassettype.KnowledgeAssetTypeSeries;
import org.omg.spec.api4kp._20200801.terms.ConceptTerm;
import org.omg.spec.api4kp._20200801.terms.model.ConceptDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  This class reads a terminology json file created by the terminology indexer.
 *  If the tests fail, be sure to run parent build first so file is created in target/classes
 *  Terminology metadata and terms are available through services.
 */
@Named
@KPServer
public class TermsProvider implements TermsApiInternal {

  private static final String TERMINOLOGY_INDEX = "terminologies.json";

  static Logger logger = LoggerFactory.getLogger(TermsProvider.class);

  /**
   *   A map using two keys to identify the TerminologyScheme value
   */
  private static MultiKeyMap multiKeyMap = readTerminologyJsonFileIntoTerminologyModels();

  private VersionTagContrastor contrastor = new VersionTagContrastor(this::toInstant);

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

    Collection<TerminologyScheme> schemes = multiKeyMap.values();

    for(TerminologyScheme scheme : schemes) {
      Pointer ptr = SemanticIdentifier.newIdAsPointer(
          scheme.getSeriesId(),
          scheme.getTag(),
          scheme.getName(),
          scheme.getVersion(),
          "",
          KnowledgeAssetTypeSeries.Value_Set.getVersionId(),
          null);
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

  public Answer<List<ConceptDescriptor>> getTerms(UUID vocabularyId, String versionTag, String label) {
    TerminologyScheme termModel = (TerminologyScheme)multiKeyMap.get(vocabularyId, versionTag);
    return Answer.of(Lists.newArrayList(termModel.getTerms().values()));
  }

  /**
   * Using the vocabularyId along with the version, a Term with the given conceptId
   * is returned.  If the term is not found, will return a status of NotFound.
   * @param vocabularyId - The id of the terminology system
   * @param versionTag - The version of the terminology
   * @param conceptId - The conceptId of the term
   * @return  retrieve a data payload that include terms, labels, definitions, relationships
   * for the concept identified by that ID
   */
  @Override
  public Answer<ConceptDescriptor> getTerm(UUID vocabularyId, String versionTag, String conceptId) {
    TerminologyScheme terminologyScheme = (TerminologyScheme)multiKeyMap.get(vocabularyId, versionTag);

    if(terminologyScheme != null) {
      Map<UUID,ConceptDescriptor> terms = terminologyScheme.getTerms();

      UUID conceptIdAsUuid = UUID.fromString(conceptId);

      if (terms.containsKey(conceptIdAsUuid)) {
        return Answer.of(terms.get(conceptIdAsUuid));
      }
    }

    return Answer.notFound();
  }

  @Override
  public Answer<ConceptDescriptor> lookupTerm(String conceptId) {
    UUID conceptIdAsUuid = UUID.fromString(conceptId);

    String latestVersion = null;
    ConceptDescriptor latestCD = null;

    for (Object value : multiKeyMap.values()) {
      TerminologyScheme ts = (TerminologyScheme) value;

      ConceptDescriptor cd = ts.getTerms().get(conceptIdAsUuid);
      if (cd != null) {
        String version = ts.getVersion();
        if (latestVersion == null
            || contrastor.contrast(version,latestVersion) == Comparison.BROADER) {
          latestVersion = version;
          latestCD = reconcile(cd, latestCD);
        }
      }
    }
    return Answer.of(latestCD);
  }

  private long toInstant(String version) {
    String timeComponent = "0";

    if (version.contains("-")) {
      int split = version.lastIndexOf("-");
      timeComponent = version.substring(split + 1);
      version = version.substring(0, split);
    }

    long base = DateTimeFormatter.ofPattern("yyyyMMdd")
        .parse(version, TemporalQueries.localDate())
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant().toEpochMilli();
    long detail = Long.parseLong(timeComponent);

    return base + detail;
  }




  private ConceptDescriptor reconcile(ConceptDescriptor cd1, ConceptDescriptor cd2) {
    if (!cd1.equals(cd2)) {
      logger.warn("Found two latest but different versions of the same Concept, "
          + "most likely due to labels or ancestors. "
          + "May have to implement a merge strategy, but requirements on performance "
          + "vs functionality are not clear at this point.");
    }
    return cd1;
  }

  /**
   * Reads the JSON file and populate the TerminologyModels
   * @return MultiKeyMap where id and version are the keys and TerminologyScheme is the value
   */
  private static MultiKeyMap readTerminologyJsonFileIntoTerminologyModels() {
    MultiKeyMap multiKeyMap = MultiKeyMap.decorate(new LinkedMap());

    try {
      // json file is stored in the classes directory during the build
      Optional<TerminologyScheme[]> optional = JSonUtil.readJson(
          TermsProvider.class.getResourceAsStream("/" + TERMINOLOGY_INDEX),
          TerminologyScheme[].class);
      if (optional.isEmpty()) {
        throw new TermProviderException();
      }
      TerminologyScheme[] terminologies = optional.get();

      // for each terminology, set the metadata and terms
      for (TerminologyScheme terminology : terminologies) {
        UUID id = UUID.fromString(terminology.getTag());
        String version = terminology.getVersion();

        multiKeyMap.put(id, version, setTerminologyMetadata(terminology));
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new TermProviderException();
    }

    return multiKeyMap;
  }

  /**
   * Set the terminology map using the terminologyId and version as keys.
   * Set the terms for the terminology.
   * @param terminology the TerminologyScheme to be populated
   */
  private static TerminologyScheme setTerminologyMetadata(TerminologyScheme terminology)
      throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Map<UUID,ConceptDescriptor> terms = getTermsFromTerminologyClass(terminology).stream().collect(Collectors.toMap(ConceptDescriptor::getUuid, x -> x));

    terminology.setTerms(terms);

    return terminology;
  }

  /**
   * Uses reflection to retrieve the terms from the terminology Class.
   * @param terminologyScheme the terminologyScheme
   * @return the terms for the terminology
   */
  private static List<ConceptDescriptor> getTermsFromTerminologyClass(TerminologyScheme terminologyScheme)
        throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    Class<?> cls = Class.forName(terminologyScheme.getName());
    Object obj = null;
    try {
      obj = cls.getDeclaredConstructor().newInstance();
    } catch(Exception e)  {
      // expected exception
    }
    Method method = cls.getDeclaredMethod("values");
    ConceptTerm<?>[] terms = (ConceptTerm<?>[])method.invoke(obj);

    return convertTermArrayToListOfDescriptors(terms);
  }

  /**
   * Determines if two concepts are related - default by subsumption (isA)
   *
   * @param vocabularyId - The id of the terminology system
   * @param versionTag - The version of the terminology
   * @param conceptId - The conceptId of the term
   * @return a list of any ancestors
   */
  @Override
  public Answer<List<ConceptDescriptor>> listAncestors(UUID vocabularyId, String versionTag, String conceptId) {
    Answer<ConceptDescriptor> conceptDescriptor = getTerm(vocabularyId, versionTag, conceptId);
    return Answer.of(convertTermArrayToListOfDescriptors(conceptDescriptor.get().getAncestors()));
  }

  /**
   * Finds out if a concept is an ancestor of another concept
   * @param vocabularyId - The id of the terminology system
   * @param versionTag - the tag for the terminology
   * @param conceptId - the id of the concept who is looking to find if another is an ancestor
   * @param testConceptId - the id of the possible ancestor
   * @return a boolean indicating if the testConceptId is an ancestor
   */
  @Override
  public Answer<Boolean> isAncestor(UUID vocabularyId, String versionTag, String conceptId, String testConceptId)  {
    Answer<ConceptDescriptor> conceptDescriptor = getTerm(vocabularyId, versionTag, conceptId);
    Term[] ancestors = conceptDescriptor.get().getAncestors();
    if(ancestors != null) {
      for (int i = 0; i < ancestors.length; i++) {
        if (testConceptId.equals(ancestors[i].getUuid().toString())) {
          return Answer.of(Boolean.TRUE);
        }
      }
    }
    return Answer.of(Boolean.FALSE);
  }

  /**
   * Converts an array of Terms to a List of ConceptDescriptors
   * @param terms the array of Terms
   * @return the List of ConceptDescriptors
   */
  private static List<ConceptDescriptor> convertTermArrayToListOfDescriptors(Term[] terms)  {
    ArrayList<ConceptDescriptor> descriptors = new ArrayList<>();
    if(terms!= null) {
      for (Term term : terms) {
        ConceptDescriptor descriptor = ConceptDescriptor.toConceptDescriptor((ConceptTerm<?>) term);
        descriptors.add(descriptor);
      }
    }
    return descriptors;
  }

  //  The methods below will be implemented at a later date.


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
    return Answer.unsupported();
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
    return Answer.unsupported();
  }
}
