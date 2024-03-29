package de.fabianweller.dhbwcoursesbot;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Statics {
    private Statics() {}

    public static final String TIMEZONE = "Europe/Berlin";

    public static final String BASE_URL = "https://api.stuv.app/rapla/lectures/";
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM")
            .withLocale(Locale.GERMAN)
            .withZone(ZoneId.of(TIMEZONE));

    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")
            .withLocale(Locale.GERMAN)
            .withZone(ZoneId.of(TIMEZONE));

}
