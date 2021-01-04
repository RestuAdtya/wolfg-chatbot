package com.wolfgang.chatbotsubmission.model;


import com.fasterxml.jackson.annotation.*;

@JsonIgnoreProperties(value = {
        "ratings",
        "added",
        "added_by_status",
        "platforms",
        "parent_platforms",
        "genres",
        "stores",
        "clip",
        "tags",
        "esrb_rating",
        "short_screenshots"
})
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "slug",
        "name",
        "released",
        "tba",
        "background_image",
        "rating",
        "rating_top",
        "ratings_count",
        "reviews_text_count",
        "metacritic",
        "playtime",
        "suggestions_count",
        "updated",
        "user_game",
        "reviews_count",
        "saturated_color",
        "dominant_color",
})
public class ModelGames {
    @JsonProperty("id")
    private int id;
    @JsonProperty("slug")
    private String slug;
    @JsonProperty("name")
    private String name;
    @JsonProperty("released")
    private String released;
    @JsonProperty("tba")
    private String tba;
    @JsonProperty("background_image")
    private String background_image;
    @JsonProperty("rating")
    private double rating;
    @JsonProperty("rating_top")
    private int rating_top;
    @JsonProperty("ratings_count")
    private int ratings_count;
    @JsonProperty("reviews_text_count")
    private int reviews_text_count;
    @JsonProperty("metacritic")
    private int metacritic;
    @JsonProperty("playtime")
    private int playtime;
    @JsonProperty("suggestions_count")
    private int suggestions_count;
    @JsonProperty("updated")
    private String updated;
    @JsonProperty("user_game")
    private String user_game;
    @JsonProperty("reviews_count")
    private int reviews_count;
    @JsonProperty("saturated_color")
    private String saturated_color;
    @JsonProperty("dominant_color")
    private String dominant_color;

    @JsonProperty("id")
    public int getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(int id) {
        this.id = id;
    }

    @JsonProperty("slug")
    public String getSlug() {
        return slug;
    }

    @JsonProperty("slug")
    public void setSlug(String slug) {
        this.slug = slug;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("released")
    public String getReleased() {
        return released;
    }

    @JsonProperty("released")
    public void setReleased(String released) {
        this.released = released;
    }

    @JsonProperty("tba")
    public String getTba() {
        return tba;
    }

    @JsonProperty("tba")
    public void setTba(String tba) {
        this.tba = tba;
    }

    @JsonProperty("background_image")
    public String getBackground_image() {
        return background_image;
    }

    @JsonProperty("background_image")
    public void setBackground_image(String background_image) {
        this.background_image = background_image;
    }

    @JsonProperty("rating")
    public double getRating() {
        return rating;
    }

    @JsonProperty("rating")
    public void setRating(double rating) {
        this.rating = rating;
    }

    @JsonProperty("rating_top")
    public int getRating_top() {
        return rating_top;
    }

    @JsonProperty("rating_top")
    public void setRating_top(int rating_top) {
        this.rating_top = rating_top;
    }

    @JsonProperty("ratings_count")
    public int getRatings_count() {
        return ratings_count;
    }

    @JsonProperty("ratings_count")
    public void setRatings_count(int ratings_count) {
        this.ratings_count = ratings_count;
    }

    @JsonProperty("reviews_text_count")
    public int getReviews_text_count() {
        return reviews_text_count;
    }

    @JsonProperty("reviews_text_count")
    public void setReviews_text_count(int reviews_text_count) {
        this.reviews_text_count = reviews_text_count;
    }

    @JsonProperty("metacritic")
    public int getMetacritic() {
        return metacritic;
    }

    @JsonProperty("metacritic")
    public void setMetacritic(int metacritic) {
        this.metacritic = metacritic;
    }

    @JsonProperty("playtime")
    public int getPlaytime() {
        return playtime;
    }

    @JsonProperty("playtime")
    public void setPlaytime(int playtime) {
        this.playtime = playtime;
    }

    @JsonProperty("suggestions_count")
    public int getSuggestions_count() {
        return suggestions_count;
    }

    @JsonProperty("suggestions_count")
    public void setSuggestions_count(int suggestions_count) {
        this.suggestions_count = suggestions_count;
    }

    @JsonProperty("updated")
    public String getUpdated() {
        return updated;
    }

    @JsonProperty("updated")
    public void setUpdated(String updated) {
        this.updated = updated;
    }

    @JsonProperty("user_game")
    public String getUser_game() {
        return user_game;
    }

    @JsonProperty("user_game")
    public void setUser_game(String user_game) {
        this.user_game = user_game;
    }

    @JsonProperty("reviews_count")
    public int getReviews_count() {
        return reviews_count;
    }

    @JsonProperty("reviews_count")
    public void setReviews_count(int reviews_count) {
        this.reviews_count = reviews_count;
    }

    @JsonProperty("saturated_color")
    public String getSaturated_color() {
        return saturated_color;
    }

    @JsonProperty("saturated_color")
    public void setSaturated_color(String saturated_color) {
        this.saturated_color = saturated_color;
    }

    @JsonProperty("dominant_color")
    public String getDominant_color() {
        return dominant_color;
    }

    @JsonProperty("dominant_color")
    public void setDominant_color(String dominant_color) {
        this.dominant_color = dominant_color;
    }
}
