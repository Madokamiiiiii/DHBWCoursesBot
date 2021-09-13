package de.fabianweller.dhbwcoursesbot;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Statics {
    private Statics() {}
    public static final String BASE_URL = "https://stuv.hardtke.host/rapla/lectures/";
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM")
            .withLocale(Locale.GERMAN)
            .withZone(ZoneId.systemDefault());

    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")
            .withLocale(Locale.GERMAN)
            .withZone(ZoneId.systemDefault());

}
