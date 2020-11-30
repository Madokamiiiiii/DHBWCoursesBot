package de.fabianweller.dhbwcoursesbot;

import de.btobastian.sdcf4j.handler.JavacordHandler;
import org.javacord.api.DiscordApiBuilder;

public class DHBWCoursesBot {

    private final static String TOKEN = "NzgyNjk3MjU5NzIzNzg0MjAy.X8P9oA.8UJWN7cCo0hO2jtIuqlboZ2SgXY";

    public static void main(String[] args) {

        final var api = new DiscordApiBuilder().setToken(TOKEN)
                .login().join();
        System.out.println(api.createBotInvite());

        final var handler = new JavacordHandler(api);
        handler.registerCommand(new CoursesCommand());

    }
}
