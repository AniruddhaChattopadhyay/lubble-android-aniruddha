package in.lubble.app.models.search;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SearchResultData {

    @SerializedName("exhaustiveNbHits")
    @Expose
    private boolean exhaustiveNbHits;
    @SerializedName("hits")
    @Expose
    private List<Hit> hits = null;
    @SerializedName("hitsPerPage")
    @Expose
    private int hitsPerPage;
    @SerializedName("nbHits")
    @Expose
    private int nbHits;
    @SerializedName("nbPages")
    @Expose
    private int nbPages;
    @SerializedName("page")
    @Expose
    private int page;
    @SerializedName("params")
    @Expose
    private String params;
    @SerializedName("processingTimeMS")
    @Expose
    private int processingTimeMS;
    @SerializedName("query")
    @Expose
    private String query;

    public boolean isExhaustiveNbHits() {
        return exhaustiveNbHits;
    }

    public void setExhaustiveNbHits(boolean exhaustiveNbHits) {
        this.exhaustiveNbHits = exhaustiveNbHits;
    }

    public List<Hit> getHits() {
        return hits;
    }

    public void setHits(List<Hit> hits) {
        this.hits = hits;
    }

    public int getHitsPerPage() {
        return hitsPerPage;
    }

    public void setHitsPerPage(int hitsPerPage) {
        this.hitsPerPage = hitsPerPage;
    }

    public int getNbHits() {
        return nbHits;
    }

    public void setNbHits(int nbHits) {
        this.nbHits = nbHits;
    }

    public int getNbPages() {
        return nbPages;
    }

    public void setNbPages(int nbPages) {
        this.nbPages = nbPages;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public int getProcessingTimeMS() {
        return processingTimeMS;
    }

    public void setProcessingTimeMS(int processingTimeMS) {
        this.processingTimeMS = processingTimeMS;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

}
