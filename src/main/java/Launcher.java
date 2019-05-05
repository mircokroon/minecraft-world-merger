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
        mergeRules.put("always", new MergeRuleAlways());
        mergeRules.put("never", new MergeRuleNever());
    }

    public static void main(String[] args) throws IOException {
        Namespace ns = getArguments(args);

        RegionFinder f = new RegionFinder(ns.getString("target_world"), ns.getString("source_world"));
        f.mergeWorlds(mergeRules.get(ns.getString("rule")));

        System.out.println("Done!");
    }

    /**
     * Parse commandline arguments.
     */
    private static Namespace getArguments(String[] args) {
        ArgumentParser parser = ArgumentParsers.newFor("Launcher").build()
                .defaultHelp(true)
                .description("Merge two Minecraft worlds into one.");
        parser.addArgument("-r", "--rule")
                .choices("last-modified", "always", "never").setDefault("last-modified")
                .help("Set the method used to merge overlapping chunks.");
        parser.addArgument("target_world").help("World to for chunks to be merged into. WARNING: chunks in this world may be overwritten.");
        parser.addArgument("source_world").help("World to be merged from. Chunks in this world will remain unchanged.");

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

