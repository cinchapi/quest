package org.cinchapi.quest;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Map;

import org.reflections.Reflections;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

/**
 * The {@link Quest} CLI application is used to manage Quest framework
 * applications. This CLI can be used to initialize applications and manage
 * their lifecycle (i.e. start, stop, etc) in development and production.
 * 
 * @author jeffnelson
 */
public class Quest {

    /**
     * A collection of the arguments that are passed in by the user.
     */
    static String[] inputArgs;

    /**
     * Run the program...
     * 
     * @param args
     */
    public static void main(String... args) {
        if(args.length > 0) {
            String action = args[0].toLowerCase();
            inputArgs = Arrays.copyOfRange(args, 1, args.length);
            Class<? extends QuestAction> clazz = actions.get(action);
            if(clazz != null) {
                getInstance(clazz).run();
                exit(0);
            }
        }

        // If we make it here then something went wrong :-/
        new HelpAction().run();
        exit(1);
    }

    /**
     * Terminate this program and return back to the launching shell with the
     * specified status code.
     * 
     * @param status
     */
    private static void exit(int status) {
        System.exit(status);
    }

    /**
     * Print an ASCII Art banner to the console.
     */
    private static void displayBanner() {
        StringBuilder sb = new StringBuilder();
        sb.append("   ____                  __ ").append(
                System.getProperty("line.separator"));
        sb.append("  / __ \\__  _____  _____/ /_").append(
                System.getProperty("line.separator"));
        sb.append(" / / / / / / / _ \\/ ___/ __/").append(
                System.getProperty("line.separator"));
        sb.append("/ /_/ / /_/ /  __(__  ) /_").append(
                System.getProperty("line.separator"));
        sb.append("\\___\\_\\__,_/\\___/____/\\__/ ").append(
                System.getProperty("line.separator"));
        System.out.println(sb.toString());
    }

    /**
     * Print the copyright message.
     */
    private static void displayCopyright() {
        System.out.println("Copyright (c) 2014, Jeff "
                + "Nelson & Cinchapi Software "
                + "Collective. All Rights Reserved");
        System.out.println(System.getProperty("line.separator"));
    }

    /**
     * The list of actions that the program can execute
     */
    private static Map<String, Class<? extends QuestAction>> actions = Maps
            .newTreeMap();

    static {
        Reflections.log = null; // turn off reflection logging

        // Dynamically add all the QuestAction subclasses defined here within as
        // actions that can be invoked by the user.
        Reflections reflection = new Reflections("org.cinchapi.quest");
        for (Class<? extends QuestAction> clazz : reflection
                .getSubTypesOf(QuestAction.class)) {
            if(!Modifier.isAbstract(clazz.getModifiers())
                    && clazz.getSimpleName().endsWith("Action")
                    && QuestAction.class.isAssignableFrom(clazz)) {
                String name = clazz.getSimpleName().split("Action")[0]
                        .toLowerCase();
                actions.put(name, (Class<? extends QuestAction>) clazz);
            }
        }

    }

    /**
     * Given a subclass of {@link QuestAction}, return an instance.
     * 
     * @param clazz
     * @return an instance of {@code clazz}
     */
    private static QuestAction getInstance(Class<? extends QuestAction> clazz) {
        try {
            Constructor<?> c = clazz.getDeclaredConstructor();
            c.setAccessible(true);
            QuestAction action = (QuestAction) c.newInstance();
            return action;
        }
        catch (ReflectiveOperationException e) {
            throw Throwables.propagate(e);

        }
    }

    /**
     * The action that displays the HELP message.
     * 
     * @author jeffnelson
     */
    private static class HelpAction extends QuestAction {

        @Override
        public void run() {
            displayBanner();
//            displayCopyright();
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "This Quest CLI is used to manage Quest framework projects.")
                    .append(System.getProperty("line.separator"))
                    .append(System.getProperty("line.separator"));
            sb.append("Available actions:").append(
                    System.getProperty("line.separator"));
            for (String action : actions.keySet()) {
                QuestAction instance = getInstance(actions.get(action));
                sb.append(String.format("%-30.30s  %-30.30s%n", action,
                        instance.getDescription(),
                        System.getProperty("line.separator")));
            }
            System.out.println(sb.toString());
        }

        @Override
        protected String getDescription() {
            return "Display this message";
        }

    }

}
