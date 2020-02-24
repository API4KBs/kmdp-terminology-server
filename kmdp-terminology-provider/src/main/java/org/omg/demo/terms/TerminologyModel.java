package org.omg.demo.terms;

import edu.mayo.kmdp.id.Term;

import java.net.URI;
import java.util.List;

public class TerminologyModel {

    private String name;
    private String version;
    private String schemeId;
    private URI seriesId;
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

    public TerminologyModel(String name, String version, String schemeId, URI seriesId, List<Term>  terms) {
        this.name = name;
        this.version = version;
        this.schemeId = schemeId;
        this.seriesId = seriesId;
        this.terms = terms;
    }
}
