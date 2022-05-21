package id.luckynetwork.dev.novenag.injectoragent;

import java.lang.instrument.Instrumentation;
import java.util.jar.JarFile;

public class NovenaInjector {

    public static Instrumentation instrumentation;
    public static String splitter;

    static {
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            splitter = "\\";
        } else {
            splitter = "/";
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
            String strippedJarName = file.getName().substring(file.getName().lastIndexOf(splitter) + 1);
            System.out.println("[NovenaInjector] Appending jar file to system class loader: " + strippedJarName);

            instrumentation.appendToSystemClassLoaderSearch(file);
        }
    }

}
