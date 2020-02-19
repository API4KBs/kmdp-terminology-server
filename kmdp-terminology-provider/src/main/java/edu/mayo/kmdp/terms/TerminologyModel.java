package edu.mayo.kmdp.terms;

import edu.mayo.kmdp.id.Term;

import java.net.URI;
import java.util.List;

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

}
