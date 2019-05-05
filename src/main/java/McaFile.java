import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class McaFile {
    private final static int sectorSize = 4096;
    Map<Integer, Chunk> chunkMap;


    public McaFile(File file) throws IOException {
        chunkMap = readFile(file);
    }

    public void merge(McaFile other, MergeMethod method) {
        other.chunkMap.forEach((pos, newchunk) -> {
            if (this.chunkMap.containsKey(pos) && method.shouldBeMerged(this.chunkMap.get(pos), newchunk)) {
                this.chunkMap.put(pos, newchunk);
            }
        });

    }

    /**
     * Convert the MCA file into individual chunk data.
     * For details on the MCA file format: https://minecraft.gamepedia.com/Region_file_format
     */
    private static Map<Integer, Chunk> readFile(File mca) throws IOException {
        byte[] bytes = IOUtils.toByteArray(new FileInputStream(mca));

        byte[] locations = Arrays.copyOfRange(bytes, 0, 4096);
        byte[] timestamps = Arrays.copyOfRange(bytes, 4096, 8192);
        byte[] chunkDataArray = Arrays.copyOfRange(bytes, 8192, bytes.length);

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

        return chunkMap;
    }

    /**
     * Converts a number of bytes to a big-endian int
     */
    private static int bytesToInt(byte[] arr, int start, int end) {
        int res = 0;
        do {
            res |= (arr[start] & 0xFF) << (end - start) * 8;
        } while(start++ < end);

        return res;
    }
}
