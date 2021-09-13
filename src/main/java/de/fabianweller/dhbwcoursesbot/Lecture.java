package de.fabianweller.dhbwcoursesbot;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "date",
        "startTime",
        "endTime",
        "name",
        "lecturer",
        "rooms"
})
public class Lecture {

    @JsonProperty("name")
    private String name;
    @JsonDeserialize(using = Deserializer.DeserializeDateOnly.class)
    @JsonProperty("date")
    private LocalDate date;
    @JsonDeserialize(using = Deserializer.DeserializeDate.class)
    @JsonProperty("startTime")
    private Instant startTime;
    @JsonDeserialize(using = Deserializer.DeserializeDate.class)
    @JsonProperty("endTime")
    private Instant endTime;
    @JsonProperty("lecturer")
    private String lecturer;
    @JsonProperty("rooms")
    private List<String> rooms;
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("date")
    public LocalDate getDate() {
        return date;
    }

    @JsonProperty("start_time")
    public Instant getStartTime() {
        return startTime;
    }

    @JsonProperty("end_time")
    public Instant getEndTime() {
        return endTime;
    }

    @JsonProperty("lecturer")
    public String getLecturer() {
        return lecturer;
    }

    @JsonProperty("rooms")
    public List<String> getRooms() {
        return rooms;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}