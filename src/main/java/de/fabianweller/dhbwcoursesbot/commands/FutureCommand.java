package de.fabianweller.dhbwcoursesbot.commands;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import de.fabianweller.dhbwcoursesbot.Lecture;
import de.fabianweller.dhbwcoursesbot.LectureData;
import de.fabianweller.dhbwcoursesbot.exceptions.BadEndpointException;
import de.fabianweller.dhbwcoursesbot.exceptions.NoSuchCourseException;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.awt.Color;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class FutureCommand implements CommandExecutor {

    private static final Logger log = LogManager.getLogManager().getLogger("FutureCommand");

    @Command(aliases = {"!future"}, async = true, description = "Get lectures for course x the next y weeks.", usage = "Gets lectures for course x for the next y weeks. Default: y = 2. /future INF20A 2")
    public void onMessageCreate(TextChannel channel, Message message, User user, Server server) {
        var param = Arrays.asList(message.getContent().split(" "));
        if (param.size() == 2) {
            createMessage(channel, param.get(1), 2);
        } else if (param.size() == 3) {
            int num;
            try {
                num = Integer.parseInt(param.get(2));
            } catch (Exception e) {
                channel.sendMessage("Bitte gib eine Zahl ein. Ja, du schaffst das.\nIch glaube an dich.");
                return;
            }
            createMessage(channel, param.get(1), num);
        } else {
            channel.sendMessage("Bitte einen Kursnamen eingeben.");
        }
    }

    private void createMessage(TextChannel channel, String course, int time) {
        var today = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        List<Lecture> lectureData = null;

        // Get lectures from API and deserialize them
        try {
            lectureData = LectureData.getLectureData(course, today, time);
        } catch (BadEndpointException e) {
            channel.sendMessage("Endpunkt ist down :(.");
            return;
        } catch (NoSuchCourseException e) {
            channel.sendMessage("Der Kurs wurde nicht gefunden");
            return;
        }

        if (lectureData.isEmpty()) {
            log.info("Keine Vorlesungen in den Wochen gefunden.");
            return;
        }

        channel.sendMessage(new EmbedBuilder()
                .setTitle(course)
                .setDescription("Zeitraum: " + today.toString() + " bis " + today.plus(Duration.ofDays(time * 7L - 3L)))
                .setColor(Color.GREEN));

        for (int i = 1; i <= time; i++) {
            int finalI = i;
            var messageToSend = LectureData.createWeekMessage(lectureData.stream()
                    .filter(lecture -> lecture.getDate().isBefore(today.plus(Duration.ofDays(finalI * 7L)))
                            && lecture.getDate().isAfter(today.plus(Duration.ofDays((finalI - 1L) * 7L))))
                    .collect(Collectors.toList()));
            if (i != time) {
                messageToSend.append("---------------------\n");
            }
            messageToSend.send(channel);

        }
    }

}
