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


import static edu.mayo.ontology.taxonomies.propositionalconcepts._20190801.PropositionalConcepts.BRCA_Mutation_Kind_Of;
import static edu.mayo.ontology.taxonomies.propositionalconcepts._20190801.PropositionalConcepts.Breast_Cancer_Risk_UB_Coefficient_Score_Value_Of;
import static edu.mayo.ontology.taxonomies.propositionalconcepts._20190801.PropositionalConcepts.Clinical_Tumor_CT_Stage_Kind_Of;
import static edu.mayo.ontology.taxonomies.propositionalconcepts._20190801.PropositionalConcepts.Cumulative_Breast_Cancer_Risk_Value_Of;
import static edu.mayo.ontology.taxonomies.propositionalconcepts._20190801.PropositionalConcepts.Cumulative_Ovarian_Cancer_Risk_Score_Value_Of;
import static edu.mayo.ontology.taxonomies.propositionalconcepts._20190801.PropositionalConcepts.Has_Alcohol_Abuse_Or_Substance_Abuse_Is;
import static edu.mayo.ontology.taxonomies.propositionalconcepts._20190801.PropositionalConcepts.Has_Elevated_Systolic_Blood_Pressure_Is;
import static edu.mayo.ontology.taxonomies.propositionalconcepts._20190801.PropositionalConcepts.History_Of_Alcohol_Or_Drug_Usage_Is;
import static edu.mayo.ontology.taxonomies.propositionalconcepts._20190801.PropositionalConcepts.On_Medications_That_May_Result_In_Abnormal_Cholesterol_And_Or_Triglyceride_Levels_Is;
import static edu.mayo.ontology.taxonomies.propositionalconcepts._20190801.PropositionalConcepts.Prior_Major_Bleeding_Or_Predisposition_To_Bleeding_Is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.util.NameUtils;
import edu.mayo.kmdp.util.Util;
import edu.mayo.ontology.taxonomies.clinicalsituations._20190801.ClinicalSituation;
import edu.mayo.ontology.taxonomies.propositionalconcepts._20190801.PropositionalConcepts;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class PropositionalConceptsTest {

  Set<PropositionalConcepts> knownOrphans =
      new HashSet<>(Arrays.asList(
          // Cancer concepts
          BRCA_Mutation_Kind_Of,
          Clinical_Tumor_CT_Stage_Kind_Of,

          // More cancer-related scores - TBD
          Breast_Cancer_Risk_UB_Coefficient_Score_Value_Of,
          Cumulative_Breast_Cancer_Risk_Value_Of,
          Cumulative_Ovarian_Cancer_Risk_Score_Value_Of,

          // 'or' concepts
          On_Medications_That_May_Result_In_Abnormal_Cholesterol_And_Or_Triglyceride_Levels_Is,
          Prior_Major_Bleeding_Or_Predisposition_To_Bleeding_Is,
          History_Of_Alcohol_Or_Drug_Usage_Is,
          Has_Alcohol_Abuse_Or_Substance_Abuse_Is
      ));

  @Test
  public void testClinicalSituationGeneration() {
    ClinicalSituation pcv = ClinicalSituation.Has_Hypertension;

    assertEquals(
        "http://ontology.mayo.edu/ontologies/clinicalsituationontology/6bf941a5-3ac4-443a-a9a2-531117cbd3f4",
        pcv.getRef().toString());
    assertEquals(
        "https://ontology.mayo.edu/taxonomies/clinicalsituations#ec218719-088c-3af5-ad4a-189f3069f8be",
        pcv.getConceptId().toString());

    Set<String> nonUUIDtags = Arrays.stream(ClinicalSituation.values())
        .map(ClinicalSituation::getTag)
        .filter(t -> !Util.ensureUUID(t).isPresent())
        .collect(Collectors.toSet());
    assertEquals(Collections.emptySet(), nonUUIDtags);
  }

  @Test
  public void testPCVGeneration() {

    PropositionalConcepts pcv = PropositionalConcepts.Most_Recent_TSH_Value_Of;

    assertEquals(
        ClinicalSituation.Most_Recent_TSH.getRef(),
        pcv.getRef());
    assertEquals(
        "f8be45db-22fc-4a17-96ad-6c1b3dfd7ba7",
        pcv.getTag());
    assertEquals(
        pcv.getTag(),
        NameUtils.getTrailingPart(pcv.getConceptId().toString()));

    assertEquals("https://ontology.mayo.edu/taxonomies/propositionalconcepts#f8be45db-22fc-4a17-96ad-6c1b3dfd7ba7",
        pcv.getConceptId().toString());
  }


  @Test
  public void testPCVPopulation() {
    assertNotNull(PropositionalConcepts.On_Fenofibrate_Is);
    assertNotNull(PropositionalConcepts.Most_Recent_TSH_Value_Of);
    assertNotNull(PropositionalConcepts.Has_Alcohol_Abuse_Is);
    assertNotNull(PropositionalConcepts.Has_Labile_INR_Is);
    assertNotNull(Has_Elevated_Systolic_Blood_Pressure_Is);
  }

  @Test
  public void testPCVLabels() {
    Set<PropositionalConcepts> pcsTrimmable = Arrays.stream(PropositionalConcepts.values())
        .filter(pc -> ! pc.getLabel().equals(pc.getLabel().trim()))
        .collect(Collectors.toSet());

    assertEquals(Collections.emptySet(),pcsTrimmable);
  }

  @Test
  public void testPCVTags() {
    Set<PropositionalConcepts> pcs = Arrays.stream(PropositionalConcepts.values())
        .collect(Collectors.toSet());

    Set<PropositionalConcepts> pcsWithUUIDTag = Arrays.stream(PropositionalConcepts.values())
        .filter(pc -> Util.isUUID(pc.getTag()))
        .collect(Collectors.toSet());

    assertEquals(pcs,pcsWithUUIDTag);
  }

  @Test
  public void testPCVConceptIDs() {
    Set<PropositionalConcepts> pcs = Arrays.stream(PropositionalConcepts.values())
        .collect(Collectors.toSet());

    Set<PropositionalConcepts> pcsWithUUID = Arrays.stream(PropositionalConcepts.values())
        .filter(pc -> Util.isUUID(NameUtils.getTrailingPart(pc.getConceptId().toString())))
        .collect(Collectors.toSet());

    pcs.removeAll(pcsWithUUID);
    assertEquals(Collections.emptySet(),pcs);
  }

  @Test
  public void testPCVConsistency() {
    assertTrue(PropositionalConcepts.values().length > ClinicalSituation.values().length);

    Set<URI> css = Arrays.stream(ClinicalSituation.values())
        .map(Term::getConceptId)
        .collect(Collectors.toSet());

    Set<URI> resolvedCss = new HashSet<>();
    for (ClinicalSituation cs : ClinicalSituation.values()) {
      PropositionalConcepts.resolve(cs.getTag()).ifPresent(
          csAsPc -> resolvedCss.add(csAsPc.getConceptId())
      );
    }
    css.removeAll(resolvedCss);

    assertEquals(new HashSet<>(), css);
  }

  @Test
  public void testClinicalSituationIds() {
    Set<String> nonMatchingTags = Arrays.stream(ClinicalSituation.values())
        .filter(c -> !(c.getRef().toString().endsWith(c.getTag())))
        .map(Term::getLabel)
        .collect(Collectors.toSet());
    assertEquals(Collections.emptySet(), nonMatchingTags);
  }

  @Test
  public void testClinicalSituationURIs() {

    Set<String> nonMatchingTags = Arrays.stream(ClinicalSituation.values())
        .filter(c -> !(c.getRef().toString()
            .startsWith("http://ontology.mayo.edu/ontologies/clinicalsituationontology/")))
        .map(Term::getLabel)
        .collect(Collectors.toSet());
    nonMatchingTags.forEach(System.out::println);
    assertEquals(Collections.emptySet(), nonMatchingTags);
  }


  @Test
  public void testClinicalSituationURIsInPCV() {
    Set<String> missingTags = Arrays.stream(PropositionalConcepts.values())
        .filter(c -> !(c.getRef().toString()
            .startsWith("http://ontology.mayo.edu/ontologies/clinicalsituationontology/")))
        .filter(c -> (c.getRef().toString()
            .startsWith("https://ontology.mayo.edu/taxonomies")))
        .map(Term::getLabel)
        .collect(Collectors.toSet());

    Set<String> orphanTags = knownOrphans.stream()
        .map(PropositionalConcepts::getLabel)
        .collect(Collectors.toSet());

    missingTags.stream()
        .filter(c -> ! orphanTags.contains(c))
        .forEach(System.out::println);
    // known concepts whose parents have not yet been modelledd
    assertEquals(orphanTags, missingTags);
  }


  @Test
  public void testPropositionalConceptsHaveParent() {
    Set<PropositionalConcepts> pcs = Arrays.stream(PropositionalConcepts.values())
        .filter(c -> (c.getConceptId().toString()
            .startsWith("https://ontology.mayo.edu/taxonomies/propositionalconcepts")))
        .filter(c -> c.getAncestors().length == 0)
        .collect(Collectors.toSet());

    pcs.stream()
        .filter(c -> ! knownOrphans.contains(c))
        .forEach(System.out::println);
    // known concepts whose parents have not yet been modelledd
    assertEquals(knownOrphans, pcs);
  }

  @Test
  public void testClinicalSituationNotations() {

    Set<String> tags = Arrays.stream(ClinicalSituation.values())
        .map(Term::getTag)
        .collect(Collectors.toSet());
    assertEquals(ClinicalSituation.values().length, tags.size());
  }

  @Test
  public void testClinicalSituationConsistency() {
    Arrays.stream(ClinicalSituation.values()).forEach(
        c -> assertSame(c, ClinicalSituation.resolve(c.getTag()).orElse(null))
    );

  }

  @Test
  public void testKnownClinicalSituations() {
    assertNotNull(ClinicalSituation.Currently_Dehydrated);
    assertNotNull(ClinicalSituation.History_Of_Beta_Blocker_Therapy);
    assertNotNull(ClinicalSituation.Current_Cardiac_Status);
  }

  @Test
  public void testKnownClinicalSituationsOnUUID() {
    assertTrue(ClinicalSituation.resolveUUID(
        UUID.fromString("e29fe493-e874-34db-8579-34c1054f4249")).isPresent());
    assertNotNull(ClinicalSituation.On_Carvedilol);
  }

}
