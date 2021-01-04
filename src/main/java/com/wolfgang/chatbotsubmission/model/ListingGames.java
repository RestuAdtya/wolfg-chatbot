package com.wolfgang.chatbotsubmission.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonIgnoreProperties(value = {
        "seo_description",
        "seo_keywords",
        "seo_h1",
        "noindex",
        "nofollow",
        "description",
        "filters",
        "nofollow_collections"
})
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "count",
        "next",
        "previous",
        "results",
        "seo_title"
})

public class ListingGames {
    @JsonProperty("count")
    private int count;

    @JsonProperty("next")
    private String next;

    @JsonProperty("previous")
    private String previous;

    @JsonProperty("seo_title")
    private String seo_title;

    @JsonProperty("results")
    private List<ModelGames> results = null;

    @JsonProperty("count")
    public int getCount() {
        return count;
    }

    @JsonProperty("count")
    public void setCount(int count) {
        this.count = count;
    }

    @JsonProperty("next")
    public String getNext() {
        return next;
    }

    @JsonProperty("next")
    public void setNext(String next) {
        this.next = next;
    }

    @JsonProperty("previous")
    public String getPrevious() {
        return previous;
    }

    @JsonProperty("previous")
    public void setPrevious(String previous) {
        this.previous = previous;
    }

    @JsonProperty("results")
    public List<ModelGames> getResults() {
        return results;
    }

    @JsonProperty("results")
    public void setResults(List<ModelGames> resulst) {
        this.results = results;
    }

    @JsonProperty("seo_title")
    public String getSeo_title() {
        return seo_title;
    }

    @JsonProperty("seo_title")
    public void setSeo_title(String seo_title) {
        this.seo_title = seo_title;
    }
}
