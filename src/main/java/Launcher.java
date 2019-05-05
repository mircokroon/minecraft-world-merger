import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Launcher {

    private static Map<String, IMergeRule> mergeRules = new HashMap<>();
    static {
        mergeRules.put("last-modified", new MergeRuleNewestChunks());
        mergeRules.put("force", new MergeRuleAlways());
    }

    public static void main(String[] args) throws IOException {
        Namespace ns = getArguments(args);

        RegionFinder f = new RegionFinder(ns.getString("world1"), ns.getString("world2"));
        f.mergeWorlds(mergeRules.get(ns.getString("rule")));
    }

    /**
     * Parse commandline arguments.
     */
    private static Namespace getArguments(String[] args) {
        ArgumentParser parser = ArgumentParsers.newFor("Launcher").build()
                .defaultHelp(true)
                .description("Merge two Minecraft worlds into one.");
        parser.addArgument("-r", "--rule")
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
        return ns;
    }
}

