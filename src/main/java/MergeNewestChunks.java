public class MergeNewestChunks implements MergeMethod {
    @Override
    public boolean shouldBeMerged(Chunk currentChunk, Chunk newChunk) {
        return newChunk.timestamp > currentChunk.timestamp;
    }
}
