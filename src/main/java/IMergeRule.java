public interface IMergeRule {
    boolean isAllowedToMerge(Chunk currentChunk, Chunk newChunk);
}
