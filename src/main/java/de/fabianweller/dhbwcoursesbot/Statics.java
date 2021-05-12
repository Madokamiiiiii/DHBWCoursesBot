package de.fabianweller.dhbwcoursesbot;

import java.time.format.DateTimeFormatter;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Statics {
    public static final String baseURL = "https://stuv-mosbach.de/survival/api.php?action=getLectures&course=";
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
}
