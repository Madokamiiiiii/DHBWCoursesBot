package de.fabianweller.dhbwcoursesbot;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fabianweller.dhbwcoursesbot.exceptions.BadEndpointException;
import de.fabianweller.dhbwcoursesbot.exceptions.NoSuchCourseException;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageDecoration;

import java.net.URL;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

import static de.fabianweller.dhbwcoursesbot.Statics.BASE_URL;

public class LectureData {

    private LectureData() {}

    public static List<Lecture> getLectureData(String course, LocalDate today, int time) throws BadEndpointException, NoSuchCourseException {

        List<Lecture> lectureData;

        // Get lectures from API and deserialize them
        try {
            lectureData = new ObjectMapper()
                    .readValue(new URL(BASE_URL + course), new TypeReference<>() {});
        } catch (Exception e) {
            throw new BadEndpointException();
        }

        if (lectureData.isEmpty()) {
            throw new NoSuchCourseException();
        }

        // Filter data
        return lectureData.stream()
                .filter(data -> data.getDate().isAfter(today.minus(Period.ofDays(1))))
                .filter(data -> data.getDate().isBefore(today.plus(Period.ofDays(time * 7))))
                .collect(Collectors.toList());
    }

    public static MessageBuilder createWeekMessage(List<Lecture> lectureData) {
        var messageToSend = new MessageBuilder();
        final LocalDate[] currDate = { lectureData.get(0).getDate() };

        lectureData.forEach(lecture -> {
            // Add additional blank line if new day
            if (!lecture.getDate().equals(currDate[0])) {
                messageToSend.appendNewLine();
                currDate[0] = lecture.getDate();
            }

            messageToSend.append(Statics.DATE_FORMATTER.format(lecture.getDate())
                    + "     "
                    + Statics.TIME_FORMATTER.format(lecture.getStartTime()) + " - "
                    + Statics.TIME_FORMATTER.format(lecture.getEndTime()) + "   ---  "
                    + lecture.getRooms() + "   "
                    + lecture.getName())
                    .appendNewLine();
        });
        return messageToSend;
    }

    public static MessageBuilder createMessage(LocalDate today, List<Lecture> lectureData) {
        var messageToSend = new MessageBuilder();
        final List<Lecture> todayLectures = lectureData.stream()
                .filter(data -> data.getDate().compareTo(today) == 0)
                .collect(Collectors.toList());

        if (todayLectures.isEmpty()) {
            return null;
        }

        messageToSend
                .append("Heutige Vorlesung(en):", MessageDecoration.BOLD)
                .appendNewLine();

        todayLectures.forEach(lecture -> messageToSend.append(Statics.DATE_FORMATTER.format(lecture.getDate())
                + "     "
                + Statics.TIME_FORMATTER.format(lecture.getStartTime()) + " - "
                + Statics.TIME_FORMATTER.format(lecture.getEndTime()) + "   ---  "
                + lecture.getRooms() + "   "
                + lecture.getName())
                .appendNewLine());
        return messageToSend;
    }

}
