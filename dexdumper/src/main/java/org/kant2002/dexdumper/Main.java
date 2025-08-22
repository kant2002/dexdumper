package org.kant2002.dexdumper;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Main {
    public static final String VERSION = loadVersion();

    @Parameter(names = {"--help", "-h", "-?"}, help = true,
            description = "Show usage information")
    private boolean help;

    @Parameter(names = {"--version", "-v"}, help = true,
            description = "Print the version of dexdumper and then exit")
    public boolean version;

    public Main() {}

    public static void main(String[] args) {
        Main main = new Main();

        JCommander jc = new JCommander(main);
        jc.setProgramName("dexdumper");

        jc.parse(args);

        if (main.version) {
            version();
        }

        if (jc.getParsedCommand() == null || main.help) {
            jc.usage();
            return;
        }
    }

    protected static void version() {
        System.out.println("dexdumper " + VERSION);
        System.exit(0);
    }

    private static String loadVersion() {
        InputStream propertiesStream = Main.class.getClassLoader().getResourceAsStream("dexdumper.properties");
        String version = "[unknown version]";
        if (propertiesStream != null) {
            Properties properties = new Properties();
            try {
                properties.load(propertiesStream);
                version = properties.getProperty("application.version");
            } catch (IOException ex) {
                // ignore
            }
        }
        return version;
    }
}
