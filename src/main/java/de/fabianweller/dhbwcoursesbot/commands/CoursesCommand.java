package de.fabianweller.dhbwcoursesbot.commands;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import de.fabianweller.dhbwcoursesbot.exceptions.BadEndpointException;
import de.fabianweller.dhbwcoursesbot.exceptions.NoSuchCourseException;
import de.fabianweller.dhbwcoursesbot.Lecture;
import de.fabianweller.dhbwcoursesbot.LectureData;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.awt.Color;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class CoursesCommand implements CommandExecutor {

    private static final Logger log = LogManager.getLogManager().getLogger("CoursesCommand");

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
                    today = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
                }

                // Don't process same day again
                if (!day.equals(processedDay) || firstRun) {
                    List<Lecture> lectureData;
                    // Get lectures from API and deserialize them
                    try {
                        lectureData = LectureData.getLectureData(course, today, 1);
                    } catch (BadEndpointException e) {
                        channel.sendMessage("Endpunkt ist down :( \n Ich versuche es in 15 Minuten erneut.");
                        TimeUnit.MINUTES.sleep(15);
                        continue;
                    } catch (NoSuchCourseException e) {
                        channel.sendMessage("Der Kurs wurde nicht gefunden");
                        return;
                    }

                    // Create new message for new weeks
                    if (firstRun || day.equals(DayOfWeek.SATURDAY)) {

                        MessageBuilder messageToSend = LectureData.createWeekMessage(lectureData);

                            channel.sendMessage(new EmbedBuilder()
                                    .setTitle(course)
                                    .setDescription("Zeitraum: " + today.plus(Period.ofDays(1)).toString() + " bis " + today.plus(Period.ofDays(5)))
                                    .setColor(Color.GREEN));

                            messageToSend.send(channel);

                        message = null;

                        processedDay = day;
                        firstRun = false;
                    } else {
                        // Edit message
                        if (Objects.nonNull(message)) {
                            message.get().delete();
                        }
                        var messageToSend = LectureData.createMessage(today, lectureData);
                        if (Objects.nonNull(messageToSend)) {
                            message = messageToSend.send(channel);
                        }
                        processedDay = day;
                    }
                }
                TimeUnit.HOURS.sleep(2);

            } catch (Exception e) {
                channel.sendMessage("An unexpected error occured. \n Message: \n " + e);
                channel.sendMessage("Terminating.");
                log.severe("An unexpected error occurred.");
                log.severe(e.toString());
                log.severe(e.getCause().toString());
                Thread.currentThread().interrupt();
            }
        }
    }

    @Command(aliases = {"!begin"}, async = true, description = "Get lectures for current week.", usage = "Provide the course name to start the listener.")
    public void onMessageCreate(TextChannel channel, Message message, User user, Server server) {
        if (server.isAdmin(user) || server.hasAnyPermission(user, PermissionType.ADMINISTRATOR) || user.isBotOwner()) {
            var param = Arrays.asList(message.getContent().split(" "));
            if (param.size() == 2) {
                init(channel, param.get(1));
            } else {
                channel.sendMessage("Bitte einen Kursnamen eingeben.");
            }
        } else {
            channel.sendMessage("Nur ein Admin den Command benutzen. Sorry \uD83D\uDE22");
        }
    }
}
