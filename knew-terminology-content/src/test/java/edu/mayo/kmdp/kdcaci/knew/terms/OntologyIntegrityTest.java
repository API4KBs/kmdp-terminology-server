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

import static edu.mayo.ontology.taxonomies.clinicalsituations._20190801.ClinicalSituation.Has_Biventricular_Implantable_Cardioverter_Defibrillator;
import static edu.mayo.ontology.taxonomies.clinicalsituations._20190801.ClinicalSituation.Has_Implantable_Cardioverter_Defibrillator;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import edu.mayo.kmdp.terms.Taxonomic;
import edu.mayo.kmdp.util.JenaUtil;
import edu.mayo.ontology.taxonomies.clinicalsituations.snapshot.ClinicalSituation;
import edu.mayo.ontology.taxonomies.propositionalconcepts.snapshot.PropositionalConcepts;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.ProfileRegistry;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class OntologyIntegrityTest {

  private static final String CSO_QUERY = ""
      + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
      + "SELECT ?subject ?label\n"
      + "WHERE {\n"
      + "   ?subject rdfs:subClassOf* <http://snomed.info/id/243796009> ;\n"
      + "           rdfs:label ?label .\n"
      + "} ORDER BY ?label";

  private static final String CSV_QUERY = ""
      + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
      + "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n"
      + "SELECT ?subject ?label\n"
      + "WHERE {\n"
      + "\t\t?subject a skos:Concept ;\n"
      + "           rdfs:label ?label .\n"
      + "} ORDER BY ?label";

  private static final String SUBCLASS_QUERY = ""
      + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
      + "SELECT ?subject ?object \n"
      + "WHERE {\n"
      + "   ?object rdfs:subClassOf+ <http://snomed.info/id/243796009> ;"
      + "           rdfs:label ?ol .\n"
      + "   ?subject rdfs:subClassOf+ ?object ;\n"
      + "            rdfs:label ?sl .\n"
      + "FILTER (?object != ?subject && ?object != <http://snomed.info/id/243796009>) \n"
      + "}";

  private static final String BROADER_QUERY = ""
      + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
      + "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n"
      + "SELECT ?subject ?object \n"
      + "WHERE {\n"
      + "   ?subject skos:broader+ ?object ;"
      + "           skos:prefLabel ?sl .\n"
      + "   ?object skos:prefLabel ?ol .\n"
      + "FILTER (?object != <https://ontology.mayo.edu/taxonomies/clinicalsituations#e58dcb4b-79ec-3f70-b960-d34b31497901>) \n"
      + "FILTER (?object != <https://ontology.mayo.edu/taxonomies/propositionalconcepts#647218dc-1371-43b3-83b9-740546225e7f>) \n"
      + "}";


  private static final String CSO_NS = "http://ontology.mayo.edu/ontologies/clinicalsituationontology/";
  private static final String CSV_NS = "https://ontology.mayo.edu/taxonomies/clinicalsituations#";
  private static final String PCV_NS = "https://ontology.mayo.edu/taxonomies/propositionalconcepts#";

  private static Set<Resource> knownSituations;
  private static Set<Resource> knownConcepts;
  private static Set<Pair<RDFNode, RDFNode>> hierarchy;
  private static Set<Pair<RDFNode, RDFNode>> taxonomy;
  private static Set<Pair<RDFNode, RDFNode>> extTaxonomy;

  @BeforeAll
  static void init() {
    InputStream input = OntologyIntegrityTest.class
        .getResourceAsStream("/owl/edu/mayo/kmdp/vocabs/LATEST/cso.ttl");
    InputStream input2 = OntologyIntegrityTest.class
        .getResourceAsStream("/skos/edu/mayo/kmdp/vocabs/sco.skos.rdf");
    InputStream input22 = OntologyIntegrityTest.class
        .getResourceAsStream("/skos/edu/mayo/kmdp/vocabs/sco.skos.rdf");
    InputStream input3 = OntologyIntegrityTest.class
        .getResourceAsStream("/skos/edu/mayo/kmdp/vocabs/LATEST/pcv.skos.ttl");

    OntModel cso = (OntModel) newOntModel().read(input, CSO_NS, "TTL");

    OntModel csv = (OntModel) newOntModel().read(input2, CSV_NS);

    OntModel pcv = newOntModel();
    pcv.read(input22, PCV_NS);
    pcv.read(input3, PCV_NS, "TTL");

    knownSituations = JenaUtil.askQuery(cso, QueryFactory.create(CSO_QUERY));
    knownConcepts = JenaUtil.askQuery(csv, QueryFactory.create(CSV_QUERY));

    hierarchy = JenaUtil.askBinaryQuery(cso, QueryFactory.create(SUBCLASS_QUERY));
    taxonomy = JenaUtil.askBinaryQuery(csv, QueryFactory.create(BROADER_QUERY));
    extTaxonomy = JenaUtil.askBinaryQuery(pcv, QueryFactory.create(BROADER_QUERY));

  }

  private static OntModel newOntModel() {
    OntModelSpec spec = new OntModelSpec(ModelFactory.createMemModelMaker(), null, null,
        ProfileRegistry.OWL_LANG);
    spec.getDocumentManager().setProcessImports(false);
    return ModelFactory
        .createOntologyModel(spec);
  }

  @Test
  void testCSOIntegrity() {
    Set<String> labels = knownSituations.stream()
        .map(this::getLabel).collect(Collectors.toSet());
    assertTrue(labels.contains("Has Implantable Cardioverter Defibrillator"));
    assertTrue(labels.contains("Has Syncope"));
    assertTrue(labels.contains("On Bisoprolol"));

    Set<String> knownIds = knownSituations.stream()
        .map(Resource::getURI)
        .collect(Collectors.toSet());
    assertTrue(knownIds.contains(CSO_NS + "ee2da7d3-5130-4354-ac1b-befed1704aa0"));
    assertTrue(knownIds.contains(CSO_NS + "85f2196f-a3e1-47db-ac19-506927b143ef"));
    // SNOMED for 'situation with explicit context'
    assertTrue(knownIds.contains("http://snomed.info/id/243796009"));
  }

  @Test
  void testConsistency() {

    // Assuming the (preferred) labels are preserved in the OWL->SKOS transformation
    Set<Pair<String, String>> labeledHierarchy = resolveLabels(hierarchy);
    Set<Pair<String, String>> labeledTaxonomy = resolveLabels(taxonomy);

    Set<Pair<String, String>> unmatchedHierarchy = labeledTaxonomy.stream()
        .filter(pair -> !labeledHierarchy.contains(pair))
        .collect(Collectors.toSet());
    assertEquals(new HashSet<Pair<String, String>>(), unmatchedHierarchy);

    Set<Pair<String, String>> unmatchedHierarchyInverse = labeledHierarchy.stream()
        .filter(pair -> !labeledTaxonomy.contains(pair))
        .collect(Collectors.toSet());
    assertEquals(new HashSet<Pair<String, String>>(), unmatchedHierarchyInverse);

    Set<Pair<RDFNode, RDFNode>> unmatchedTaxonomy = taxonomy.stream()
        .filter(pair -> !extTaxonomy.contains(pair))
        .collect(Collectors.toSet());
    assertEquals(new HashSet<Pair<RDFNode, RDFNode>>(), unmatchedTaxonomy);
  }


  @Test
  void testNumbers() {
    // CSV has a 'Top Concept' to match sct:Situation w/ explicit context
    assertEquals(knownConcepts.size(), knownSituations.size());
    assertEquals(hierarchy.size(), taxonomy.size());
    assertEquals(knownConcepts.size() - 1, ClinicalSituation.values().length);
  }

  private Set<Pair<String, String>> resolveLabels(Set<Pair<RDFNode, RDFNode>> hierarchy) {
    return hierarchy.stream()
        .map(p -> Pair.of(
            getLabel((Resource) p.getLeft()),
            getLabel((Resource) p.getRight())))
        .collect(Collectors.toSet());
  }

  @Test
  void testKnownClassHierarchy() {
    assertTrue(Has_Biventricular_Implantable_Cardioverter_Defibrillator.getAncestors().length > 0);
    assertTrue(hasAncestor(Has_Biventricular_Implantable_Cardioverter_Defibrillator,
        Has_Implantable_Cardioverter_Defibrillator));

  }

  private boolean hasAncestor(Taxonomic t1, Taxonomic t2) {
    if (t1 == null || t2 == null) {
      return false;
    }
    return Arrays.stream(t1.getClosure())
        .anyMatch(t -> t == t2);
  }

  @Test
  void testClassHierarchy() {
    taxonomy.forEach(
        pair -> {
          ClinicalSituation s1 = ClinicalSituation.resolveId(pair.getLeft().toString())
              .orElse(null);
          ClinicalSituation s2 = ClinicalSituation.resolveId(pair.getRight().toString())
              .orElse(null);
          if (!hasAncestor(s1, s2)) {
            fail("Hierarchical relationship not reflected in the CS taxonomy " + s1 + " vs " + s2);
          }
          ;
        }
    );
  }

  @Test
  void testConceptHierarchy() {
    Set<Pair<PropositionalConcepts, PropositionalConcepts>> brokenPairs = new HashSet<>();
    extTaxonomy.forEach(
        pair -> {
          PropositionalConcepts s1 = PropositionalConcepts
              .resolveId(pair.getLeft().toString()).orElse(null);
          PropositionalConcepts s2 = PropositionalConcepts
              .resolveId(pair.getRight().toString()).orElse(null);
          if (!hasAncestor(s1, s2)) {
            brokenPairs.add(Pair.of(s1, s2));
          }
        }
    );
    brokenPairs.forEach(p ->
        System.out.println(
            "Broken pair |" + p.getLeft().getLabel() + "| vs |" + p.getRight().getLabel() + "|"));
    assertTrue(brokenPairs.isEmpty());
  }

  private String getLabel(Resource res) {
    return res.getProperty(RDFS.label).getObject().asLiteral().getString();
  }

}
