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
