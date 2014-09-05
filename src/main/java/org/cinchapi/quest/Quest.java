package org.cinchapi.quest;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.cinchapi.concourse.util.Resources;
import org.reflections.Reflections;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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
     * Terminate this program and return back to the launching shell with the
     * specified status code.
     * 
     * @param status
     */
    private static void exit(int status) {
        System.exit(status);
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
     * A collection of the arguments that are passed in by the user.
     */
    static String[] inputArgs;

    /**
     * The list of actions that the program can execute
     */
    private static Map<String, Class<? extends QuestAction>> actions = Maps
            .newTreeMap();

    static {
        Reflections.log = null; // turn off reflection logging

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
     * The action that displays the HELP message.
     * 
     * @author jeffnelson
     */
    private static class HelpAction extends QuestAction {

        @Override
        public void run() {
            displayBanner();
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
                // Copy resources to application directory
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

                // Make the gradle built tools executable
                String[] gradle = { ".gradlewh", ".gradlewh.bat" };
                Set<PosixFilePermission> perms = Sets.newHashSet(
                        PosixFilePermission.OWNER_EXECUTE,
                        PosixFilePermission.OWNER_READ,
                        PosixFilePermission.OWNER_WRITE);
                for (String tool : gradle) {
                    java.nio.file.Files.setPosixFilePermissions(
                            Paths.get(tool), perms);
                }

                // TODO change prefs file
                // TDOO create build.gradle that has all deps, etc
                // TODO create src directory and add main class that extends
                // AppRunner
                // TODO
            }
            catch (IOException e) {
                throw Throwables.propagate(e);
            }
        }

        @Override
        public void run() {
            String appName;
            try {
                appName = Quest.inputArgs[0];
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
