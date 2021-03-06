/**
 * Basic class for holding chunk objects. Chunk data is not parsed as this is not required.
 */
public class Chunk {
    int timestamp;
    int location;
    int size;
    byte[] chunkData;

    public Chunk(int timestamp, int location, int size, byte[] chunkData) {
        this.timestamp = timestamp;
        this.location = location;
        this.size = size;
        this.chunkData = chunkData;
    }
}
