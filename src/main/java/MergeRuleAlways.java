public class MergeRuleAlways implements IMergeRule {

    @Override
    public boolean isAllowedToMerge(Chunk currentChunk, Chunk newChunk) {
        return true;
    }
}
