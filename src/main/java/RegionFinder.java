import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class RegionFinder {
    private final static String regionPath = "region";

    String world1;
    String world2;

    public RegionFinder(String world1, String world2) {
        this.world1 = world1;
        this.world2 = world2;
    }

    public void mergeWorlds(MergeMethod method) throws IOException {
        List<FilePair> filesToCopy = new ArrayList<>();
        List<FilePair> filesToMerge = new ArrayList<>();

        Map<String, File> f1 = listMcaFiles(world1);
        Map<String, File> f2 = listMcaFiles(world2);

        f2.forEach((name, file) -> {
            if (f1.containsKey(name)) {
                filesToMerge.add(new FilePair(file, f1.get(name)));
            } else {
                filesToCopy.add(new FilePair(file, Paths.get(world1, regionPath, name).toFile()));
            }
        });

        promptContinue(filesToCopy, filesToMerge);

        copy(filesToCopy);
        merge(filesToMerge, method);

    }

    private void copy(List<FilePair> files) {
        files.forEach(pair -> {
            try {
                FileUtils.copyFile(pair.from, pair.to);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void merge(List<FilePair> files, MergeMethod method) {
        files.forEach(pair -> {
            try {
                McaFile target = new McaFile(pair.to);
                McaFile source = new McaFile(pair.from);

                target.merge(source, method);

                // TODO: write file
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void promptContinue(List<FilePair> copy, List<FilePair> merge) {
        if (copy.size() > 0) {
            System.out.println("Region files to copy: " + copy.size());
            copy.forEach(el -> System.out.println("\tCopy: " + el.from.getName()));
        }

        if (merge.size() > 0) {
            System.out.println("Region files to merge: " + merge.size());
            merge.forEach(el -> System.out.println("\tMerge: " + el.to.getName()));
            System.out.println("WARNING: merging may be DESTRUCTIVE and can overwrite chunks!");
        }

        if (merge.size() == 0 && copy.size() == 0) {
            System.out.println("Nothing to merge.");
            System.exit(0);
        }

        System.out.println("Do you want to continue? [Y/n]");
        Scanner s = new Scanner(System.in);
        if (!s.nextLine().equals("Y")) {
            System.exit(0);
        }

    }

    public Map<String, File> listMcaFiles(String world) throws IOException {
        Path p = Paths.get(world, regionPath);

        Map<String, File> map = new HashMap<>();

        Files.walk(p)
                .filter(el -> el.getFileName().toString().endsWith(".mca"))
                .map(Path::toFile)
                .filter(el -> el.length() > 0)
                .forEach(el -> map.put(el.getName(), el));

        return map;
    }

    private void getMcaUnion() {

    }

}

class FilePair {
    File from;
    File to;

    public FilePair(File from, File to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public String toString() {
        return "FilePair{" +
                "from=" + from +
                ", to=" + to +
                '}';
    }
}
