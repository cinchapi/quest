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

/**
 * The {@link QuestAction} that initializes an application in the current
 * working directory.
 * 
 * @author jeffnelson
 */
class InitAction extends QuestAction {

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