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

    private static final List<String> courseNames = List.of("INF20A", "INF20B");
    private static final String baseURL = "https://stuv-mosbach.de/survival/api.php?action=getLectures&course=";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

    public void execute(TextChannel channel, String course) {
        var firstRun = true;
        CompletableFuture<Message> message = null;

        while (true) {
            final var today = LocalDate.now();

            var day = today.getDayOfWeek();
            try {

                // Get lectures from API and deserialize them
                var lectureData = new ObjectMapper()
                        .readValue(new URL(baseURL + course), new TypeReference<List<Lecture>>() {
                        });

                if (lectureData.isEmpty()) {
                    channel.sendMessage("Kurs nicht gefunden.");
                }

                // Filter data
                lectureData = lectureData.stream()
                        .filter(data -> data.getStartDate().isAfter(today.minusDays(1L)))
                        .filter(data -> data.getStartDate().isBefore(today.plusWeeks(1L)))
                        .collect(Collectors.toList());

                // Create new message for new weeks
                if (firstRun || day.equals(DayOfWeek.SUNDAY)) {

                    MessageBuilder messageToSend = createMessage(today, lectureData);

                    channel.sendMessage(new EmbedBuilder()
                            .setTitle(course)
                            .setDescription("Zeitraum: " + today.toString() + " bis " + today.plusDays(6))
                            .setColor(Color.GREEN));

                    message = messageToSend.send(channel);

                    firstRun = false;
                } else {
                    // Edit message
                    if (Objects.nonNull(message)) {
                        message.get().delete();
                        message = createMessage(today, lectureData).send(channel);
                    }
                }
                TimeUnit.HOURS.sleep(2); // Sleep two hours

            } catch (Exception e) {
                channel.sendMessage("An unexpected error occured. \n Message: \n " + e.getMessage());
                return;
            }
        }

    }

    private MessageBuilder createMessage(LocalDate today, List<Lecture> lectureData) {
        var messageToSend = new MessageBuilder();
        final LocalDate[] currDate = {today};

        lectureData.forEach(lecture -> {
            // Add additional blank line if new day
            if (!lecture.getStartDate().isEqual(currDate[0])) {
                messageToSend.appendNewLine();
                currDate[0] = lecture.getStartDate();
            }

            // Highlight if today
            if (lecture.getStartDate().isEqual(today)) {
                messageToSend.append(lecture.getStartDate().format(formatter) + "     "
                        + lecture.getStartTime() + " - "
                        + lecture.getEndTime() + "     "
                        + lecture.getName(), MessageDecoration.BOLD)
                        .appendNewLine();
            } else {
                messageToSend.append(lecture.getStartDate().format(formatter) + "     "
                        + lecture.getStartTime() + " - "
                        + lecture.getEndTime() + "     "
                        + lecture.getName())
                        .appendNewLine();
            }

        });
        return messageToSend;
    }

    @Command(aliases = {"!begin"}, async = true, description = "Get lectures for current week.")
    public void onMessageCreate(TextChannel channel, Message message) {
        var param = Arrays.asList(message.getContent().split(" "));
        if (param.size() == 2) {
            execute(channel, param.get(1));
        } else {
            channel.sendMessage("Bitte einen Kursnamen eingeben.");
        }
    }
}
