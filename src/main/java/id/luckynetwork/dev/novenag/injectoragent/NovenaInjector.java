package id.luckynetwork.dev.novenag.injectoragent;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

public class NovenaInjector {

    private static final List<JarFileInfo> loadedJars = new ArrayList<>();
    private static final String SPLITTER;
    public static Instrumentation instrumentation;

    static {
        // Checking if the operating system is windows or not.
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            SPLITTER = "\\";
        } else {
            SPLITTER = "/";
        }
    }

    public static void premain(String agentArgs, Instrumentation inst) {
        NovenaInjector.instrumentation = inst;
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        NovenaInjector.instrumentation = inst;
    }

    /**
     * If the instrumentation object is not null, then append the jar file to the system class loader.
     *
     * @param file The jar file to append to the system class loader.
     */
    public static void appendJarFile(JarFile file) {
        if (instrumentation != null) {
            if (canLoad(file)) {
                instrumentation.appendToSystemClassLoaderSearch(file);
            }
        }
    }

    /**
     * If the jar file is already loaded, check if the version is higher than the one already loaded. If it is, don't load
     * it
     * <p>
     * AN OVER-ENGINEERED PIECE OF SHIT
     *
     * @param file The JarFile that is being loaded
     * @return A boolean value.
     */
    private static boolean canLoad(JarFile file) {
        // mongo-java-driver-3.12.11.jar = 3.12.11
        // jedis-3.7.0.jar = 3.7.0
        // gson-2.9.0.jar = 2.9.0

        String fileName = file.getName().substring(file.getName().lastIndexOf(SPLITTER) + 1);

        String version = fileName.substring(fileName.lastIndexOf("-") + 1);
        version = removeSuffixFromVersionStringUsingRegex(version);

        boolean unknownVersion = version.equals("") || version.matches("[a-zA-Z]+");

        String jarName;
        if (unknownVersion) jarName = fileName.replace(".jar", "");
        else jarName = fileName.split(version)[0].replace("-", "");

        JarFileInfo jarFileInfo = loadedJars.stream().filter(it -> it.getJarName().equalsIgnoreCase(jarName)).findFirst().orElse(null);
        if (jarFileInfo != null) {
            if (unknownVersion) {
                System.out.println("[NovenaInjector] Failed to get version for duplicate loading jar: " + fileName);

                loadedJars.add(new JarFileInfo(jarName));
                return false;
            }

            if (jarFileInfo.getVersion() == null) {
                jarFileInfo.setVersion(version);

                System.out.println("[NovenaInjector] Failed to get version for duplicate loaded jar: " + fileName);
                return false;
            }

            // compare major, minor version
            // 3.12.11

            String[] loadedVersion = jarFileInfo.getVersion().split("\\.");
            String[] fileVersion = version.split("\\.");

            if (loadedVersion.length != fileVersion.length) {
                System.out.println("[NovenaInjector] Failed to compare version for duplicate loaded jar: " + jarName);

                loadedJars.add(new JarFileInfo(jarName));
                return false;
            }

            for (int i = 0; i < loadedVersion.length; i++) {
                if (Integer.parseInt(loadedVersion[i]) > Integer.parseInt(fileVersion[i])) {
                    System.out.println("[NovenaInjector] Duplicate loaded jar version is higher than the one from the JarFile: " + jarName);
                    return false;
                }
            }

            System.out.println("[NovenaInjector] Duplicate loaded jar version is lower than the one from the JarFile: " + jarName);
            return false;
        }

        // check if version is empty or does not contain any number
        if (unknownVersion) {
            System.out.println("[NovenaInjector] Failed to get version for new loading jar: " + jarName);
            loadedJars.add(new JarFileInfo(jarName));
            System.out.println("[NovenaInjector] Loaded jarFile " + jarName + " with version: UNKNOWN");
            return true;
        }

        loadedJars.add(new JarFileInfo(jarName, version));
        System.out.println("[NovenaInjector] Loaded jarFile " + jarName + " with version: " + version);
        return true;
    }

    /**
     * > Remove all non-digit and non-period characters from the end of the string
     *
     * @param s the string to remove the suffix from
     * @return The version number of the jar file.
     */
    private static String removeSuffixFromVersionStringUsingRegex(String s) {
        // 1.0.0.Final.Jar -> 1.0.0
        // 1.0.0.jar -> 1.0.0

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (Character.isDigit(s.charAt(i)) || s.charAt(i) == '.') {
                sb.append(s.charAt(i));
            }
        }

        while (sb.length() > 0 && sb.charAt(sb.length() - 1) == '.') {
            sb.deleteCharAt(sb.length() - 1);
        }

        return sb.toString();
    }

    /**
     * It's a class that holds a JarFile, the name of the JarFile, and the version of the JarFile
     */
    static class JarFileInfo {
        private final String jarName;
        private String version;

        JarFileInfo(String jarName, String version) {
            this.jarName = jarName;
            this.version = version;
        }

        JarFileInfo(String jarName) {
            this.jarName = jarName;
        }

        public String getVersion() {
            return version;
        }

        public String getJarName() {
            return jarName;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }

}
