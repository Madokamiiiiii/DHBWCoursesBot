package de.fabianweller.dhbwcoursesbot;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
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
        "ID",
        "course",
        "name",
        "start_mysql",
        "start_date",
        "start_time",
        "end_mysql",
        "end_date",
        "end_time",
        "duration",
        "today",
        "over",
        "allDayEvent",
        "multipleDayEvent",
        "lecturer",
        "location",
        "lastModified_mysql"
})
public class Lecture {

    @JsonProperty("course")
    private String course;
    @JsonProperty("name")
    private String name;
    @JsonDeserialize(using = Deserializer.DeserializeDate.class)
    @JsonProperty("start_date")
    private LocalDate startDate;
    @JsonDeserialize(using = Deserializer.DeserializeTime.class)
    @JsonProperty("start_time")
    private LocalTime startTime;
    @JsonDeserialize(using = Deserializer.DeserializeDate.class)
    @JsonProperty("end_date")
    private LocalDate endDate;
    @JsonDeserialize(using = Deserializer.DeserializeTime.class)
    @JsonProperty("end_time")
    private LocalTime endTime;
    @JsonProperty("duration")
    private int duration;
    @JsonProperty("today")
    private boolean today;
    @JsonProperty("over")
    private boolean over;
    @JsonProperty("allDayEvent")
    private boolean allDayEvent;
    @JsonProperty("multipleDayEvent")
    private boolean multipleDayEvent;
    @JsonProperty("lecturer")
    private String lecturer;
    @JsonProperty("location")
    private String location;
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<>();


    @JsonProperty("course")
    public String getCourse() {
        return course;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("start_date")
    public LocalDate getStartDate() {
        return startDate;
    }

    @JsonProperty("start_time")
    public LocalTime getStartTime() {
        return startTime;
    }

    @JsonProperty("end_date")
    public LocalDate getEndDate() {
        return endDate;
    }

    @JsonProperty("end_time")
    public LocalTime getEndTime() {
        return endTime;
    }

    @JsonProperty("duration")
    public int getDuration() {
        return duration;
    }

    @JsonProperty("today")
    public boolean isToday() {
        return today;
    }

    @JsonProperty("over")
    public boolean isOver() {
        return over;
    }

    @JsonProperty("allDayEvent")
    public boolean isAllDayEvent() {
        return allDayEvent;
    }

    @JsonProperty("multipleDayEvent")
    public boolean isMultipleDayEvent() {
        return multipleDayEvent;
    }

    @JsonProperty("lecturer")
    public String getLecturer() {
        return lecturer;
    }

    @JsonProperty("location")
    public String getLocation() {
        return location;
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