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

import edu.mayo.kmdp.terms.impl.model.TerminologyScheme;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import edu.mayo.kmdp.terms.exceptions.TermIndexerException;
import org.omg.spec.api4kp._1_0.identifiers.NamespaceIdentifier;
import org.reflections.Reflections;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

/**
 * This class reads the terminology files for values which are defined in the TerminologyScheme.
 * It outputs those values in a json file.
 */
public class TerminologyIndexer {

    /**
     *  All the terminologies read from the taxonomies package
     */
    private Collection<TerminologyScheme> terminologyModels;

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
            if (f.getParentFile() == null) {
                throw new IllegalArgumentException("No parent folder detected, "
                    + "unable to write index file in a root dir " + path);
            }
            if (! f.getParentFile().exists()) {
                boolean pathInitialized = f.getParentFile().mkdirs();
                if (!pathInitialized) {
                    throw new IOException("Unable to create nested folders "
                        + f.getParentFile().getPath());
                }
            }
            if (!f.exists()) {
                boolean fileCreated = f.createNewFile();
                if (! fileCreated) {
                    throw new IOException("Unable to create new file" + f.getPath());
                }
            }
            writer.writeValue(f, terminologyModels);

        } catch (IOException | IllegalAccessException e) {
            throw new TermIndexerException();
        }
    }

    /**
     * Read the files and determine which is a terminology.
     * For each of the terminologies, get the metadata and store as TerminologyScheme.
     * Store all the terminologies in a Collection
     * @throws IllegalAccessException if there are issues getting the metadata from the file
     */
    protected void readFilesToFindTerminologies() throws IllegalAccessException {
        terminologyModels = new ArrayList<>();

        // Get the taxonomy files that extend ControlledTerm
        // TODO - Must move the base package name to a 'registry' class in kmdp-registry
        Reflections reflections = new Reflections("edu.mayo.ontology.taxonomies");
        Set<Class<? extends ControlledTerm>> subTypes = reflections.getSubTypesOf(ControlledTerm.class);

        // Read all the files.  If does not have namespace, is not a terminology and exception is ignored.
        for(Class<?> subtype:subTypes)  {
            try {
                TerminologyScheme terminology = new TerminologyScheme();

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

}
