import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Main {
    static Map<String, MergeMethod> mergeMethods = new HashMap<>();
    static {
        mergeMethods.put("last-modified", new MergeNewestChunks());
        mergeMethods.put("force", new MergeAlways());
    }

    public static void main(String[] args) throws IOException {
        ArgumentParser parser = ArgumentParsers.newFor("Main").build()
                .defaultHelp(true)
                .description("Merge two Minecraft worlds into one");
        parser.addArgument("-m", "--mode")
                .choices("last-modified", "force").setDefault("last-modified")
                .help("Set the method used to merge overlapping chunks.");
        parser.addArgument("world1");
        parser.addArgument("world2");

        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
        } catch(ArgumentParserException ex) {
            parser.handleError(ex);
            System.exit(1);
        }

        RegionFinder f = new RegionFinder(ns.getString("world1"), ns.getString("world2"));
        f.mergeWorlds(mergeMethods.get(ns.getString("mode")));
    }
}

