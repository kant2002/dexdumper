package org.kant2002.dexdumper;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Properties;

import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.raw.util.DexAnnotator;
import org.jf.dexlib2.iface.DexFile;

public class Main {
    public static final String VERSION = loadVersion();

    @Parameter(names = {"--help", "-h", "-?"}, help = true,
            description = "Show usage information")
    private boolean help;

    @Parameter(names = {"--version", "-v"}, help = true,
            description = "Print the version of dexdumper and then exit")
    public boolean version;

    @Parameter(names = {"--source", "-s"},
            description = "Source DEX file from which dump metadata")
    public String source;

    public Main() {}

    public static void main(String[] args) {
        Main main = new Main();

        JCommander jc = new JCommander(main);
        jc.setProgramName("dexdumper");

        jc.parse(args);

        if (main.version) {
            version();
        }

        if (main.help) {
            jc.usage();
            return;
        }

        try {
            DexBackedDexFile dexFile = DexFileFactory.loadDexFile(main.source, null);
            Writer writer = new BufferedWriter(new OutputStreamWriter(System.out));

            try {
                PlainAnnotators annotator = new PlainAnnotators(dexFile, 120);
                annotator.writeAnnotations(writer);
                writer.flush();
            } finally {
                writer.close();
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
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
