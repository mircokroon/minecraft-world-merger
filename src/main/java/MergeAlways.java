public class MergeAlways implements MergeMethod {

    @Override
    public boolean shouldBeMerged(Chunk currentChunk, Chunk newChunk) {
        return true;
    }
}
