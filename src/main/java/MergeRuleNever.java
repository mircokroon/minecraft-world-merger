public class MergeRuleNever implements IMergeRule {

    @Override
    public boolean isAllowedToMerge(Chunk currentChunk, Chunk newChunk) {
        return false;
    }
}
