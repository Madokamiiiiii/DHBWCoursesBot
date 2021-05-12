package de.fabianweller.dhbwcoursesbot.Commands;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import de.fabianweller.dhbwcoursesbot.Lecture;
import de.fabianweller.dhbwcoursesbot.LectureData;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
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
                    if (!day.equals(DayOfWeek.SUNDAY)) {
                        today = today.with(DayOfWeek.SUNDAY).minusWeeks(1L);
                    }
                }

                // Don't process same day again
                if (!day.equals(processedDay) || firstRun) {
                    List<Lecture> lectureData;
                    // Get lectures from API and deserialize them
                    try {
                        lectureData = LectureData.getLectureData(course, today, 1);
                    } catch (Exception e) {
                        channel.sendMessage("Kurs nicht gefunden.");
                        return;
                    }

                    if (lectureData.isEmpty()) {
                        TimeUnit.MINUTES.sleep(30);
                        log.info("No lectures found for week with day " + today);
                        continue;
                    }


                    // Create new message for new weeks
                    if (firstRun || day.equals(DayOfWeek.SATURDAY)) {

                        MessageBuilder messageToSend = LectureData.createWeekMessage(lectureData);

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
                channel.sendMessage("An unexpected error occured. \n Message: \n " + e.toString());
                channel.sendMessage("Terminating.");
                log.severe("An unexpected error occured.");
                log.severe(e.toString());
                log.severe(e.getCause().toString());
                return;
            }
        }
    }

    @Command(aliases = {"/begin"}, async = true, description = "Get lectures for current week.", usage = "Provide the course name to start the listener.")
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
