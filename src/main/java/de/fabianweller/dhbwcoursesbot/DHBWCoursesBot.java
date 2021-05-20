package de.fabianweller.dhbwcoursesbot;

import de.btobastian.sdcf4j.handler.JavacordHandler;
import de.fabianweller.dhbwcoursesbot.Commands.CoursesCommand;
import de.fabianweller.dhbwcoursesbot.Commands.FutureCommand;
import org.javacord.api.DiscordApiBuilder;



public class DHBWCoursesBot {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("No Bot-Token provided. Exiting.");
            return;
        }

        final var api = new DiscordApiBuilder().setToken(args[0])
                .login().join();
        System.out.println(api.createBotInvite());

        final var handler = new JavacordHandler(api);
        handler.registerCommand(new CoursesCommand());
        handler.registerCommand(new FutureCommand());

    }
}
