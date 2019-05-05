public interface MergeMethod {
    boolean shouldBeMerged(Chunk currentChunk, Chunk newChunk);
}
