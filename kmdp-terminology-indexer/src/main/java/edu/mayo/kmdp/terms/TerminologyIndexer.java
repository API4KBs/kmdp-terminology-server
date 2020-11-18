/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.mayo.kmdp.terms;


import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import edu.mayo.kmdp.terms.exceptions.TermIndexerException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.series.Series;
import org.omg.spec.api4kp._20200801.terms.TypedTerm;
import org.reflections.Reflections;

/**
 * This class reads the terminology files for values which are defined in the TerminologyScheme. It
 * outputs those values in a json file.
 */
public class TerminologyIndexer {

  /**
   * All the terminologies read from the taxonomies package
   */
  private Collection<TerminologySchemeDescr> terminologyModels;

  public TerminologyIndexer() {
    super();
  }

  /**
   * @param args the path for the output file
   */
  public static void main(String... args) {
    String path = args[0];
    String filter = null;
    if (args.length > 1) {
      filter = args[1];
    }
    new TerminologyIndexer().execute(path, filter);
  }

  /**
   * Gets the files and stores the terminology metadata as JSON Objects. Write the output file in
   * the path location. The method is called by the provider pom using mojo execute.
   *
   * @param path the path for the output file
   */
  public void execute(String path, String filter) {
    try {
      readFilesToFindTerminologies(filter);

      ObjectMapper mapper = new ObjectMapper();
      ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());

      File f = new File(path);
      if (f.getParentFile() == null) {
        throw new IllegalArgumentException("No parent folder detected, "
            + "unable to write index file in a root dir " + path);
      }
      if (!f.getParentFile().exists()) {
        boolean pathInitialized = f.getParentFile().mkdirs();
        if (!pathInitialized) {
          throw new IOException("Unable to create nested folders "
              + f.getParentFile().getPath());
        }
      }
      if (!f.exists()) {
        boolean fileCreated = f.createNewFile();
        if (!fileCreated) {
          throw new IOException("Unable to create new file" + f.getPath());
        }
      }
      writer.writeValue(f, terminologyModels);

    } catch (IOException | IllegalAccessException e) {
      throw new TermIndexerException();
    }
  }

  /**
   * Read the files and determine which is a terminology. For each of the terminologies, get the
   * metadata and store as TerminologyScheme. Store all the terminologies in a Collection
   *
   * @throws IllegalAccessException if there are issues getting the metadata from the file
   */
  protected void readFilesToFindTerminologies(String filter) throws IllegalAccessException {
    terminologyModels = new ArrayList<>();

    // Get the taxonomy files that extend ControlledTerm
    // TODO - Must move the base package name to a 'registry' class in kmdp-registry
    Set<Class<? extends TypedTerm>> subTypes =
        Stream.of("edu.mayo.ontology.taxonomies", "org.omg.spec.api4kp")
            .flatMap(pack -> new Reflections(pack)
                .getSubTypesOf(TypedTerm.class).stream())
            .filter(Class::isEnum)
            .filter(klass -> !Series.class.isAssignableFrom(klass))
            .collect(Collectors.toSet());

    // Read all the files.  If does not have namespace, is not a terminology and exception is ignored.
    for (Class<?> subtype : subTypes) {
      try {
        TerminologySchemeDescr terminology = new TerminologySchemeDescr();

        Field schemeFld = subtype.getField("schemeVersionIdentifier");
        ResourceIdentifier schemeVersionIdentifier = (ResourceIdentifier) schemeFld.get(null);

        Field seriesFld = subtype.getField("schemeSeriesIdentifier");
        ResourceIdentifier schemeSeriesIdentifier = (ResourceIdentifier) seriesFld.get(null);

        UUID key = schemeVersionIdentifier.getVersionUuid();
        terminology.setKey(key);

        UUID seriesUUID = schemeSeriesIdentifier.getUuid();
        terminology.setSchemeUUID(seriesUUID);

        String tag = schemeSeriesIdentifier.getTag();
        terminology.setTag(tag);

        String version = schemeVersionIdentifier.getVersionTag();
        terminology.setVersion(version);

        String name = subtype.getName();
        terminology.setName(name);

        String schemeId = schemeVersionIdentifier.getVersionId().toString();
        terminology.setSchemeId(schemeId);

        // 'resourceId' should be 'versionId' and 'namespace' should be 'resourceId'
        terminology.setSeriesId(schemeVersionIdentifier.getResourceId());

        if (filter == null || terminology.getSchemeId().matches(filter)) {
          terminologyModels.add(terminology);
        }

      } catch (NoSuchFieldException e) {
        // those files without namespace will be ignored
      }
    }
  }

  private class TerminologySchemeDescr {

    UUID key;
    UUID schemeUUID;
    String tag;
    String name;
    String version;
    String schemeId;
    URI seriesId;

    public UUID getKey() {
      return key;
    }

    public void setKey(UUID key) {
      this.key = key;
    }

    public UUID getSchemeUUID() {
      return schemeUUID;
    }

    public void setSchemeUUID(UUID schemeUUID) {
      this.schemeUUID = schemeUUID;
    }

    public String getTag() {
      return tag;
    }

    public void setTag(String tag) {
      this.tag = tag;
    }

    public URI getSeriesId() {
      return seriesId;
    }

    public void setSeriesId(URI seriesId) {
      this.seriesId = seriesId;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getVersion() {
      return version;
    }

    public void setVersion(String version) {
      this.version = version;
    }

    public String getSchemeId() {
      return schemeId;
    }

    public void setSchemeId(String schemeId) {
      this.schemeId = schemeId;
    }
  }
}
