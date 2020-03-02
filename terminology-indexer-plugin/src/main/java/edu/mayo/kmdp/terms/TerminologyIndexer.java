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
<<<<<<< HEAD:terminology-indexer-plugin/src/main/java/edu/mayo/kmdp/terms/TerminologyIndexer.java
package edu.mayo.kmdp.terms;
=======
package org.omg.demo.terms;
>>>>>>> 33226 File path now being correctly set.  Added documentation.:terminology-indexer-plugin/src/main/java/edu/mayo/kmdp/terms/TerminologyIndexer.java

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
<<<<<<< HEAD:terminology-indexer-plugin/src/main/java/edu/mayo/kmdp/terms/TerminologyIndexer.java
=======
import edu.mayo.kmdp.terms.ControlledTerm;
>>>>>>> 33226 File path now being correctly set.  Added documentation.:terminology-indexer-plugin/src/main/java/edu/mayo/kmdp/terms/TerminologyIndexer.java
import org.omg.spec.api4kp._1_0.identifiers.NamespaceIdentifier;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

/**
<<<<<<< HEAD:terminology-indexer-plugin/src/main/java/edu/mayo/kmdp/terms/TerminologyIndexer.java
 * This class reads the terminology files for values which are defined in the TerminologyModel.
 * It outputs those values in a json file.
=======
 *
>>>>>>> 33226 File path now being correctly set.  Added documentation.:terminology-indexer-plugin/src/main/java/edu/mayo/kmdp/terms/TerminologyIndexer.java
 */
public class TerminologyIndexer {
  private static Logger logger = LoggerFactory.getLogger(TerminologyIndexer.class);

  /**
   *  All the terminologies read from the taxonomies package
   */
  Collection<TerminologyModel> terminologyModels;

  public TerminologyIndexer()  {
    super();
  }

  /**
   *
   * @param args the path for the output file
   */
  public static void main(String... args) {
    String path = args[0];
    new TerminologyIndexer().execute(path);
  }

  /**
   * Gets the files and stores the terminology metadata as JSON Objects.
   * Write the output file in the path location.
   * The method is called by the provider pom using mojo execute.
   * @param path the path for the output file
   */
  public void execute(String path) {
    try {
      readFilesToFindTerminologies();

      ObjectMapper mapper = new ObjectMapper();
      ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());

      File f = new File(path);
      f.createNewFile();
      writer.writeValue(f, terminologyModels);

    } catch (IOException | IllegalAccessException e) {
        logger.error(e.getMessage(),e);
        throw new RuntimeException(e);
    }
  }

  /**
   * Read the files and determine which is a terminology.
   * For each of the terminologies, get the metadata and store as TerminologyModel.
   * Store all the terminologies in a Collection
   * @throws IllegalAccessException if there are issues getting the metadata from the file
   */
  protected void readFilesToFindTerminologies() throws IllegalAccessException {
    terminologyModels = new ArrayList<>();

    // Get the taxonomy files that extend ControlledTerm
    Reflections reflections = new Reflections("edu.mayo.ontology.taxonomies");
    Set<Class<? extends ControlledTerm>> subTypes = reflections.getSubTypesOf(ControlledTerm.class);

    // Read all the files.  If does not have namespace, is not a terminology and exception is ignored.
    for(Class subtype:subTypes)  {
      try {
        TerminologyModel terminology = new TerminologyModel();

        Field namespace = subtype.getField("namespace");

        String name = subtype.getName();
        terminology.setName(name);

        String version = ((NamespaceIdentifier)namespace.get(null)).getVersion();
        terminology.setVersion(version);

        String schemeId = ((NamespaceIdentifier)namespace.get(null)).getTag();
        terminology.setSchemeId(schemeId);

        terminology.setSeriesId(((NamespaceIdentifier)namespace.get(null)).getId());

        terminologyModels.add(terminology);

      } catch (NoSuchFieldException e) {
          // those files without namespace will be ignored
      }
    }
  }

<<<<<<< HEAD:terminology-indexer-plugin/src/main/java/edu/mayo/kmdp/terms/TerminologyIndexer.java
=======
  public static void main(String... args) {
    String path = args[0];
    new TerminologyIndexer().execute(path);
  }


  public void execute(String path) {
    try {
      scan();

      ObjectMapper mapper = new ObjectMapper();
      ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());

      writer.writeValue(new File(path), terminologyModels);

    } catch (IOException | IllegalAccessException e) {
      logger.error(e.getMessage(),e);
      throw new RuntimeException(e);
    }
  }
>>>>>>> 33226 File path now being correctly set.  Added documentation.:terminology-indexer-plugin/src/main/java/edu/mayo/kmdp/terms/TerminologyIndexer.java
}
