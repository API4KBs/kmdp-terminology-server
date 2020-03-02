<<<<<<< HEAD
<<<<<<< HEAD:terminology-indexer-plugin/src/main/java/edu/mayo/kmdp/terms/TerminologyModel.java
package edu.mayo.kmdp.terms;
=======
package org.omg.demo.terms;
>>>>>>> 33226 File path now being correctly set.  Added documentation.:terminology-indexer-plugin/src/main/java/edu/mayo/kmdp/terms/TerminologyModel.java
=======
package edu.mayo.kmdp.terms;
>>>>>>> File path now being correctly set.  Added documentation

import java.net.URI;

/**
 * This class defines the parts of the terminology being retrieved from the files.
 */
public class TerminologyModel {

    /**
     * The name of the terminology
     */
    private String name;
    /**
     * The version of the terminology
     */
    private String version;
    /**
     * Terminology identifier
     */
    private String schemeId;
    /**
     * URL of the terminology version
     */
    private URI seriesId;

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

    public URI getSeriesId() {
        return seriesId;
    }

    public void setSeriesId(URI seriesId) {
        this.seriesId = seriesId;
    }


    public TerminologyModel() {
        super();
    }

}
