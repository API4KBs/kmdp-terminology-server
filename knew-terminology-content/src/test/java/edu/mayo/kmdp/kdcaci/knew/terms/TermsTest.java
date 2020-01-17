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
import edu.mayo.ontology.taxonomies.propositionalconcepts.PropositionalConcepts;
import edu.mayo.ontology.taxonomies.propositionalconcepts.PropositionalConceptsSeries;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.identifiers.URIIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.UUIDentifier;

public class TermsTest {

	@Test
	public void testUUID() {
		try {
			String s = ClinicalSituationSeries.Has_Sleep_Apnea.getTag();

			UUID u = UUID.fromString( "375ca9b7-ab37-4994-aaa7-664c38dc975d" );
			URI x = URI.create( Registry.BASE_UUID_URN + u.toString() );

			UUIDentifier uid = new UUIDentifier().withTag( s );
			assertEquals( u, uid.getUUID() );

			Optional<UUIDentifier> castUid = DatatypeHelper.toUUIDentifier( ClinicalSituationSeries.Has_Sleep_Apnea.asConcept() );
			assertTrue( castUid.isPresent() );
			assertEquals( uid.getUUID(), castUid.map( edu.mayo.kmdp.id.UUIDentifier::getUUID ).orElse( null ) );

			assertEquals( x, castUid.flatMap( DatatypeHelper::toURIIDentifier )
			                        .map( URIIdentifier::getUri )
			                        .orElse( null ) );

		} catch ( Exception e ) {
			fail( e.getMessage() );
		}
	}

	@Test
	public void testKnownVocabularies() {
		assertNotNull(ClinicalTaskSeries.Discuss_Recommendations_For_HFrEF);
	}



	@Test
	void testPCid() {
		System.out.println(PropositionalConceptsSeries.History_Of_Vascular_Disease_Is.getConceptUUID().toString()); //64a1b962-cafe-467e-845b-c9f78031c94e
		System.out.println(PropositionalConceptsSeries.On_Angiotensin_II_Receptor_Blockers_Is.getConceptUUID().toString()); //d3c43b90-4e81-4d06-920d-26315d879cfc
		Optional<PropositionalConcepts> concept1 = PropositionalConceptsSeries.resolveUUID(PropositionalConceptsSeries.History_Of_Vascular_Disease_Is.getConceptUUID());
		Optional<PropositionalConcepts> concept2 = PropositionalConceptsSeries.resolveUUID(PropositionalConceptsSeries.On_Angiotensin_II_Receptor_Blockers_Is.getConceptUUID());
		assertTrue(concept1.isPresent());
		assertTrue(concept2.isPresent());
	}
}
