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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import edu.mayo.kmdp.id.helper.DatatypeHelper;
import edu.mayo.kmdp.registry.Registry;
import edu.mayo.ontology.taxonomies.clinicalsituations.ClinicalSituationSeries;
import edu.mayo.ontology.taxonomies.clinicaltasks.ClinicalTaskSeries;
import edu.mayo.ontology.taxonomies.propositionalconcepts.PropositionalConceptsSeries;
import edu.mayo.ontology.taxonomies.propositionalconcepts._20190801.PropositionalConcepts;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.identifiers.URIIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.UUIDentifier;

public class VersionSpecificTest {

	@Test
	public void testAug19() {
		assertNotNull(edu.mayo.ontology.taxonomies.propositionalconcepts._20190801.PropositionalConcepts.On_NSAIDs_Contraindicated_For_HFrEF);
		assertNotNull(edu.mayo.ontology.taxonomies.propositionalconcepts._20190801.PropositionalConcepts.On_NSAIDs_Contraindicated_For_HFrEF_Is);
	}

	@Test
	public void testDec19() {
		assertNotNull(
				edu.mayo.ontology.taxonomies.propositionalconcepts._20191201.PropositionalConcepts.On_NSAIDs_Contraindicated_For_HFrEF_Is);
	}

	@Test
	public void testJan20() {
		assertNotNull(edu.mayo.ontology.taxonomies.propositionalconcepts._20200109.PropositionalConcepts.On_NSAIDS_Contraindicated_In_HFrEF);
		assertNotNull(edu.mayo.ontology.taxonomies.propositionalconcepts._20200109.PropositionalConcepts.On_NSAIDs_Contraindicated_In_HFrEF_Is);
	}

}
