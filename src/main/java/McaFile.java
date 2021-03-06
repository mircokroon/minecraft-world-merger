import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class McaFile {
    private final static int sectorSize = 4096;
    Map<Integer, Chunk> chunkMap;
    Path filePath;
    byte[] input;

    /**
     * Class to parse and write MCA files
     * @param file the MCA file to be used
     * @throws IOException
     */
    public McaFile(File file) throws IOException {
        chunkMap = readFile(file);
        filePath = Paths.get(file.getAbsolutePath());
    }

    /**
     * Merge changes from the given MCA into this ones
     * @param other a different MCA file
     * @param rule the rule to be used for mering
     */
    public void merge(McaFile other, IMergeRule rule) {
        other.chunkMap.forEach((pos, newchunk) -> {
            if (this.chunkMap.containsKey(pos)) {
                if (rule.isAllowedToMerge(this.chunkMap.get(pos), newchunk)) {
                    this.chunkMap.put(pos, newchunk);
                }
            } else {
                this.chunkMap.put(pos, newchunk);
            }
        });
    }

    /**
     * Write the MCA file to the given path. Should be called after merge.
     * @param path the path to write to
     * @throws IOException
     */
    public void write(Path path) throws IOException {
        byte[] locations = new byte[sectorSize];
        byte[] timestamps = new byte[sectorSize];
        Map<Integer, byte[]> chunkDataList = new HashMap<>();
        final int[] maxpos = {0};

        updateChunkLocations(chunkMap);

        chunkMap.forEach((pos, chunk) -> {
            setLocation(locations, pos, chunk);
            setTimestamp(timestamps, pos, chunk);
            setChunkData(chunkDataList, chunk);

            int bytePosition = (chunk.size + chunk.location - 2) * sectorSize;
            if (bytePosition > maxpos[0]) {
                maxpos[0] = bytePosition;
            }
        });

        byte[] toWrite = join(locations, timestamps, chunkDataList, maxpos[0]);
        Files.write(path, toWrite);
    }

    /**
     * Update the chunk positions in the chunkdata section of the file. Different files may have
     * have overlapping locations even if the coordinates are different, so we need to recompute
     * the locations before saving.
     * @param chunkMap the chunkmap to set the positions for
     */
    private void updateChunkLocations(Map<Integer, Chunk> chunkMap) {
        AtomicInteger currentAddress = new AtomicInteger(2);
        chunkMap.forEach((pos, chunk) -> {
            chunk.location = currentAddress.get();
            currentAddress.addAndGet(chunk.size);
        });
    }

    private void setChunkData(Map<Integer, byte[]> chunkDataList, Chunk chunk) {
        chunkDataList.put(chunk.location, chunk.chunkData);
    }

    private void setTimestamp(byte[] timestamp, int pos, Chunk chunk) {
        timestamp[pos] = (byte) (chunk.timestamp >>> 24);
        timestamp[pos+1] = (byte) (chunk.timestamp >>> 16);
        timestamp[pos+2] = (byte) (chunk.timestamp >>> 8);
        timestamp[pos+3] = (byte) chunk.timestamp;
    }

    private void setLocation(byte[] locations, Integer pos, Chunk chunk) {
        locations[pos] = (byte) (chunk.location >>> 16);
        locations[pos+1] = (byte) (chunk.location >>> 8);
        locations[pos+2] = (byte) chunk.location;
        locations[pos+3] = (byte) chunk.size;
    }

    /**
     * Join the various parts of the byte array to be saved into one.
     * @param maxpos the largest byte sector address we will need, depending on the amount of data we
     *               are saving.
     * @return the final byte array
     */
    private byte[] join(byte[] locations, byte[] timestamps, Map<Integer, byte[]> datalist, int maxpos) {
        int totalBytes = locations.length + timestamps.length + maxpos;

        byte[] res = new byte[totalBytes];
        System.arraycopy(locations, 0, res, 0, locations.length);
        System.arraycopy(timestamps, 0, res, sectorSize, timestamps.length);

        datalist.forEach((i, data) -> {
            int pos = i * sectorSize;
            System.arraycopy(data, 0, res, pos, data.length);
        });

        return res;
    }

    /**
     * Convert the MCA file into individual chunk data.
     * For details on the MCA file format: https://minecraft.gamepedia.com/Region_file_format
     */
    private Map<Integer, Chunk> readFile(File mca) throws IOException {
        byte[] bytes = IOUtils.toByteArray(new FileInputStream(mca));

        byte[] locations = Arrays.copyOfRange(bytes, 0, sectorSize);
        byte[] timestamps = Arrays.copyOfRange(bytes, sectorSize, sectorSize*2);
        byte[] chunkDataArray = Arrays.copyOfRange(bytes, sectorSize*2, bytes.length);

        HashMap<Integer, Chunk> chunkMap = new HashMap<>();

        for (int i = 0; i < locations.length; i += 4) {
            int timestamp = bytesToInt(timestamps, i, i+3);
            int location = bytesToInt(locations, i, i+2);
            int size = locations[i+3] & 0xFF;

            if (size == 0) { continue; }

            // chunk location includes first location/timestamp sections so we need to lower the addresses by 2 sectors
            int chunkDataStart = (location - 2) * sectorSize ;
            int chunkDataEnd = (location + size - 2) * sectorSize;

            byte[] chunkData = Arrays.copyOfRange(chunkDataArray, chunkDataStart,  chunkDataEnd);

            // i is the unique identifier of this chunk within the file, based on coordinates thus consistent
            chunkMap.put(i, new Chunk(timestamp, location, size, chunkData));
        }

        this.input = bytes;
        return chunkMap;
    }

    /**
     * Converts a number of bytes to a big-endian int.
     * @param arr the total array of bytes
     * @param start the first byte to use (inclusive)
     * @param end the last byte to use (INCLUSIVE)
     * @return the integer created from the bytes
     */
    private static int bytesToInt(byte[] arr, int start, int end) {
        int res = 0;
        do {
            res |= (arr[start] & 0xFF) << (end - start) * 8;
        } while(start++ < end);

        return res;
    }
}
