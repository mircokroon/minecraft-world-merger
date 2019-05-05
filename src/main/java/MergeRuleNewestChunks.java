public class MergeRuleNewestChunks implements IMergeRule {
    @Override
    public boolean isAllowedToMerge(Chunk currentChunk, Chunk newChunk) {
        return newChunk.timestamp > currentChunk.timestamp;
    }
}
