package edu.mayo.kmdp.kdcaci.knew.terms;

import static edu.mayo.kmdp.id.helper.DatatypeHelper.uri;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.metadata.annotations.SimpleApplicability;
import edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset;
import edu.mayo.kmdp.util.JSonUtil;
import edu.mayo.ontology.taxonomies.clinicalsituations.ClinicalSituationSeries;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class ClinicalMetadataTest {


  @Test
  void testApplicability() {
    Term t = ClinicalSituationSeries.Has_Allergy_To_Statins;
    KnowledgeAsset ks = new KnowledgeAsset()
        .withAssetId(uri("http://foo.bar", "142412"))
        .withApplicableIn(new SimpleApplicability()
            .withSituation(t.asConcept())
        );
    ks = checkRoundTrip(ks);
    Term appSit = ((SimpleApplicability)ks.getApplicableIn()).getSituation();
    assertNotNull(appSit);
    Term t2 = t.asConcept();
    assertEquals(t2.getConceptId(),appSit.getConceptId());
    assertEquals(t2.getConceptUUID(),appSit.getConceptUUID());
  }


  private KnowledgeAsset checkRoundTrip(KnowledgeAsset ks) {
    Optional<String> str = JSonUtil.writeJsonAsString(ks);
    assertTrue(str.isPresent());
    Optional<KnowledgeAsset> asset = str
        .flatMap(s -> JSonUtil.readJson(s,KnowledgeAsset.class));
    assertTrue(asset.isPresent());
    return asset.orElse(new KnowledgeAsset());
  }

}
