# minecraft-world-merger
A simple Java CLI application to merge two Minecraft worlds into one, by combining region files and overwriting/skipping overlapping chunks.

# Usage
[Download](https://github.com/mircokroon/minecraft-world-merger/releases) the latest release and run the file using:

```java -jar world-merger.jar /path/to/world/a/ /path/to/world/b/```

Note that the world path should be the world root directory (containing level.dat).

Optionally, you can specify how overlapping chunks are to be handled using the `-r` option. Run
`java -jar world-merger.jar -h` for more information.
