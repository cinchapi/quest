package org.cinchapi.quest;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.cinchapi.concourse.util.Resources;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

/**
 * The {@link Quest} CLI application is used to manage Quest framework
 * applications. This CLI can be used to initialize applications and manage
 * their lifecycle (i.e. start, stop, etc) in development and production.
 * 
 * @author jeffnelson
 */
@SuppressWarnings({ "unchecked", "unused" })
public class Quest {

    /**
     * A collection of the arguments that are passed in by the user.
     */
    private static String[] inputArgs;

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
     * The list of actions that the program can execute
     */
    private static Map<String, Class<? extends QuestAction>> actions = Maps
            .newTreeMap();

    static {
        // Dynamically add all the QuestAction subclasses defined here within as
        // actions that can be invoked by the user.
        for (Class<?> clazz : Quest.class.getDeclaredClasses()) {
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
            StringBuilder sb = new StringBuilder();
            for (String action : actions.keySet()) {
                QuestAction instance = getInstance(actions.get(action));
                sb.append(MessageFormat.format("{0}\t\t\t{1}{2}", action,
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

    private static class TestAction extends QuestAction {

        @Override
        public void run() {
            System.out.println("Jeff Nelson is a cool guy");

        }

        @Override
        protected String getDescription() {
            return "this is just a dummy thing";
        }

    }

    /**
     * The {@link QuestAction} that initializes an application in the current
     * working directory.
     * 
     * @author jeffnelson
     */
    private static class InitAction extends QuestAction {

        /**
         * Initialize an application named {@code appName} in the directory
         * where
         * from the quest script was invoked.
         * 
         * @param appName
         */
        private static void init(String appName) {
            System.out.println("Initializing application named " + appName);
            try {
                URL url = Quest.class.getResource("Quest.class");
                String scheme = url.getProtocol();
                Preconditions.checkArgument(scheme.equalsIgnoreCase("jar"),
                        "Unsupported scheme: %s", scheme);
                JarURLConnection conn = (JarURLConnection) url.openConnection();
                JarFile jar = conn.getJarFile();
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if(!entry.getName().endsWith(".class")
                            && !entry.isDirectory()) { // skip
                                                       // class
                                                       // files
                        File destination = new File(entry.getName());
                        File parent = destination.getParentFile();
                        if(parent != null) {
                            parent.mkdirs();
                        }
                        File source = new File(Resources.get(
                                File.separator + entry.getName()).getFile());
                        Files.copy(source, destination);
                    }
                }
            }
            catch (IOException e) {
                throw Throwables.propagate(e);
            }
        }

        @Override
        public void run() {
            String appName;
            try {
                appName = inputArgs[0];
            }
            catch (ArrayIndexOutOfBoundsException e) {
                String[] pathSegments = System.getProperty("user.dir").split(
                        File.separator);
                appName = pathSegments[pathSegments.length - 1];
            }
            init(appName);
        }

        @Override
        protected String getDescription() {
            return "Initialize the application";
        }

    }

    /**
     * A {@link QuestAction} is a runnable that contains an implementation for a
     * Quest CLI action in its {@link #run()} method. Any subclass that is named
     * appropriately will automatically be registered in the Quest CLI's list of
     * runnable actions.
     * 
     * <p>
     * Each action can access user arguments that are passed into the program
     * via the {@link #inputArgs} variable.
     * </p>
     * 
     * @author jeffnelson
     */
    private static abstract class QuestAction implements Runnable {

        /**
         * Return a description that summarizes the action, its inputs and
         * output, etc.
         * 
         * @return the description of the action
         */
        protected abstract String getDescription();
    }

}
