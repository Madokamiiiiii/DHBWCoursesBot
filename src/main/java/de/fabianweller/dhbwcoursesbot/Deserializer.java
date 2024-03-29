package de.fabianweller.dhbwcoursesbot;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class Deserializer {
    private Deserializer() {}

    public static class DeserializeDate extends JsonDeserializer<Instant> {

        @Override
        public Instant deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
        {
            return Instant.parse(p.getText());
        }
    }

    public static class DeserializeDateOnly extends JsonDeserializer<LocalDate> {

        @Override
        public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            var timestamp= Instant.parse(p.getText());
            return LocalDate.ofInstant(timestamp, ZoneId.of(Statics.TIMEZONE));
        }
    }
}
