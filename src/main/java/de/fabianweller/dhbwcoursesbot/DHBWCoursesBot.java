package de.fabianweller.dhbwcoursesbot;

import de.btobastian.sdcf4j.handler.JavacordHandler;
import de.fabianweller.dhbwcoursesbot.commands.CoursesCommand;
import de.fabianweller.dhbwcoursesbot.commands.FutureCommand;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.util.logging.FallbackLoggerConfiguration;


public class DHBWCoursesBot {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("No Bot-Token provided. Exiting.");
            return;
        }

        final var api = new DiscordApiBuilder().setToken(args[0])
                .login().join();
        System.out.println(api.createBotInvite());
        FallbackLoggerConfiguration.setDebug(true);

        final var handler = new JavacordHandler(api);
        handler.registerCommand(new CoursesCommand());
        handler.registerCommand(new FutureCommand());

    }
}
