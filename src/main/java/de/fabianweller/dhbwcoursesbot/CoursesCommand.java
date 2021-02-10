package de.fabianweller.dhbwcoursesbot;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageDecoration;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.awt.*;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CoursesCommand implements CommandExecutor {

    private static final String baseURL = "https://stuv-mosbach.de/survival/api.php?action=getLectures&course=";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

    public void init(TextChannel channel, String course) {
        var processedDay = DayOfWeek.MONDAY;
        execute(channel, course, processedDay, null, true);
    }

    public void execute(TextChannel channel, String course, DayOfWeek processedDay, CompletableFuture<Message> message, boolean firstRun) {
        while (true) {
            try {
                var today = LocalDate.now();
                var day = today.getDayOfWeek();

                // First run needs to start at sunday, if not sunday use sunday of last week
                if (firstRun) {
                    if (!day.equals(DayOfWeek.SUNDAY)) {
                        today = today.with(DayOfWeek.SUNDAY).minusWeeks(1L);
                    }
                }

                // Don't process same day again
                if (!day.equals(processedDay) || firstRun) {
                    // Get lectures from API and deserialize them
                    var lectureData = new ObjectMapper()
                            .readValue(new URL(baseURL + course), new TypeReference<List<Lecture>>() {});

                    if (lectureData.isEmpty()) {
                        channel.sendMessage("Kurs nicht gefunden.");
                        return;
                    }

                    // Filter data
                    LocalDate finalToday = today;
                    lectureData = lectureData.stream()
                            .filter(data -> data.getStartDate().isAfter(finalToday.minusDays(1L)))
                            .filter(data -> data.getStartDate().isBefore(finalToday.plusWeeks(1L)))
                            .collect(Collectors.toList());


                    // Create new message for new weeks
                    if (firstRun || day.equals(DayOfWeek.SUNDAY)) {

                        MessageBuilder messageToSend = createWeekMessage(lectureData);

                        try {
                            channel.sendMessage(new EmbedBuilder()
                                    .setTitle(course)
                                    .setDescription("Zeitraum: " + today.plusDays(1L).toString() + " bis " + today.plusDays(5L))
                                    .setColor(Color.GREEN));

                            messageToSend.send(channel);
                        } catch (Exception e) {
                            // Week has no lectures (yes, the check above should already prevent this. But better safe than sorry)
                            System.out.println(e.toString());
                        }

                        message = null;

                        processedDay = day;
                        firstRun = false;
                    } else {
                        // Don't do anything if a week has no lectures.
                        if (lectureData.isEmpty()) {
                            if (Objects.nonNull(message)) {
                                message.get().delete();
                            }
                            TimeUnit.HOURS.sleep(6);
                            continue;
                        }
                        // Edit message
                        if (Objects.nonNull(message)) {
                            message.get().delete();
                        }
                        var messageToSend = createMessage(today, lectureData);
                        if (Objects.nonNull(messageToSend)) {
                            message = messageToSend.send(channel);
                        }
                        processedDay = day;
                    }
                }
                TimeUnit.HOURS.sleep(2);

            } catch (Exception e) {
                channel.sendMessage("An unexpected error occured. \n Message: \n " + e.toString());
                channel.sendMessage("Terminating.");
            }
        }
    }

    private MessageBuilder createWeekMessage(List<Lecture> lectureData) {
        var messageToSend = new MessageBuilder();
        final LocalDate[] currDate = { lectureData.get(0).getStartDate() };

        lectureData.forEach(lecture -> {
            // Add additional blank line if new day
            if (!lecture.getStartDate().isEqual(currDate[0])) {
                messageToSend.appendNewLine();
                currDate[0] = lecture.getStartDate();
            }

            messageToSend.append(lecture.getStartDate().format(formatter)
                    + "     "
                    + lecture.getStartTime() + " - "
                    + lecture.getEndTime() + "     "
                    + lecture.getName())
                    .appendNewLine();
        });
        return messageToSend;
    }

    private MessageBuilder createMessage(LocalDate today, List<Lecture> lectureData) {
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

        todayLectures.forEach(lecture -> messageToSend.append(lecture.getStartDate().format(formatter)
                + "     "
                + lecture.getStartTime() + " - "
                + lecture.getEndTime() + "     "
                + lecture.getName())
                .appendNewLine());
        return messageToSend;
    }

    @Command(aliases = {"!begin"}, async = true, description = "Get lectures for current week.")
    public void onMessageCreate(TextChannel channel, Message message, User user, Server server) {
        if (server.isAdmin(user)) {
            var param = Arrays.asList(message.getContent().split(" "));
            if (param.size() == 2) {
                init(channel, param.get(1));
            } else {
                channel.sendMessage("Bitte einen Kursnamen eingeben.");
            }
        } else {
            channel.sendMessage("Nur ein Admin kann den Command benutzen. Sorry \uD83D\uDE22");
        }
    }
}
