<<<<<<< HEAD
package edu.mayo.kmdp.terms;
=======
package org.omg.demo.terms;
>>>>>>> 33226 File path now being correctly set.  Added documentation.

import edu.mayo.kmdp.id.Term;

import java.net.URI;
import java.util.List;

<<<<<<< HEAD
/**
 * This class defines the parts of the terminology available for the service
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
    /**
     * The terms found in the terminology
     */
=======
public class TerminologyModel {

    private String name;
    private String version;
    private String schemeId;
    private URI seriesId;
>>>>>>> 33226 File path now being correctly set.  Added documentation.
    private List<Term> terms;

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

    public List<Term> getTerms() {
        return terms;
    }

    public void setTerms(List<Term>  terms) {
        this.terms = terms;
    }

    public TerminologyModel() {
        super();
    }

<<<<<<< HEAD
=======
    public TerminologyModel(String name, String version, String schemeId, URI seriesId, List<Term>  terms) {
        this.name = name;
        this.version = version;
        this.schemeId = schemeId;
        this.seriesId = seriesId;
        this.terms = terms;
    }
>>>>>>> 33226 File path now being correctly set.  Added documentation.
}
