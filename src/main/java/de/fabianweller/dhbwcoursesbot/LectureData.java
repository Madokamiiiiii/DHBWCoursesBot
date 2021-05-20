package de.fabianweller.dhbwcoursesbot;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageDecoration;

import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static de.fabianweller.dhbwcoursesbot.Statics.baseURL;

public class LectureData {
    public static List<Lecture> getLectureData(String course, LocalDate today, int time) throws Exception {
        // Get lectures from API and deserialize them
        var lectureData = new ObjectMapper()
                .readValue(new URL(baseURL + course), new TypeReference<List<Lecture>>() {});

        if (lectureData.isEmpty()) {
            throw new Exception("Course not found");
        }

        // Filter data
        return lectureData.stream()
                .filter(data -> data.getStartDate().isAfter(today.minusDays(1L)))
                .filter(data -> data.getStartDate().isBefore(today.plusWeeks(time)))
                .collect(Collectors.toList());
    }

    public static MessageBuilder createWeekMessage(List<Lecture> lectureData) {
        var messageToSend = new MessageBuilder();
        final LocalDate[] currDate = { lectureData.get(0).getStartDate() };

        lectureData.forEach(lecture -> {
            // Add additional blank line if new day
            if (!lecture.getStartDate().isEqual(currDate[0])) {
                messageToSend.appendNewLine();
                currDate[0] = lecture.getStartDate();
            }

            messageToSend.append(lecture.getStartDate().format(Statics.formatter)
                    + "     "
                    + lecture.getStartTime() + " - "
                    + lecture.getEndTime() + "     "
                    + (lecture.getName().equals("Prakt. Datenverarbeitung") ? "PDA (Praktische Datenarbeitung)" : lecture.getName()))
                    .appendNewLine();
        });
        return messageToSend;
    }

    public static MessageBuilder createMessage(LocalDate today, List<Lecture> lectureData) {
        var messageToSend = new MessageBuilder();
        final List<Lecture> todayLectures = lectureData.stream()
                .filter(data -> data.getStartDate().isEqual(today))
                .collect(Collectors.toList());

        if (todayLectures.isEmpty()) {
            return null;
        }

        messageToSend
                .append("Heutige Vorlesung(en):", MessageDecoration.BOLD)
                .appendNewLine();

        todayLectures.forEach(lecture -> messageToSend.append(lecture.getStartDate().format(Statics.formatter)
                + "     "
                + lecture.getStartTime() + " - "
                + lecture.getEndTime() + "     "
                + (lecture.getName().equals("Prakt. Datenverarbeitung") ? "PDA (Praktische Datenarbeitung)" : lecture.getName()))
                .appendNewLine());
        return messageToSend;
    }

}
