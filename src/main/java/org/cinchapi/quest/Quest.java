package org.cinchapi.quest;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.cinchapi.concourse.util.Resources;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.io.Files;

public class Quest {

    public static void main(String... args) {
        if(args.length > 0) {
            String action = args[0];
            if(action.equalsIgnoreCase("init")) {
                String appName;
                try {
                    appName = args[1];
                }
                catch (ArrayIndexOutOfBoundsException e) {
                    String[] pathSegments = System.getProperty("user.dir")
                            .split(File.separator);
                    appName = pathSegments[pathSegments.length - 1];
                }
                init(appName);
            }
            exit(0);
        }

        // If we make it here then something went wrong :-/
        displayHelp();
        exit(1);

    }

    /**
     * Initialize an application named {@code appName} in the directory where
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
                if(!entry.getName().endsWith(".class") && !entry.isDirectory()) { // skip
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

    /**
     * Print the general help message to the console.
     */
    private static void displayHelp() {
        System.out.println("I need help");
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

}
