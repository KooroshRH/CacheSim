class Main{
    enum WritePolicy{
        WRITE_BACK, WRITE_THROUGH;
    }

    enum AllocatePolicy{
        WRITE_ALLOCATION, NO_WRITE_ALLOCATION;
    }

    enum CacheMode{
        UNIFIED, SEPARATED;
    }

    class Cache{
        private CacheMode cacheMode;
        private int cacheDataSize;
        private int cacheInstructionSize;
        private int blockSize;
        private int associativity;
        private WritePolicy writePolicy;
        private AllocatePolicy allocatePolicy;

        private int instructionsAccesses;
        private int instructionsMisses;
        private int instructionsReplaces;
        
        private int dataAccesses;
        private int dataMisses;
        private int dataReplaces;

        private int allFetch;
        private int allCopies;

        public Cache(CacheMode cacheMode, int cacheDataSize, int cacheInstructionSize, int blockSize, int associativity, WritePolicy writePolicy, AllocatePolicy allocatePolicy){
            this.cacheMode = cacheMode;
            this.cacheDataSize = cacheDataSize;
            this.cacheInstructionSize = cacheInstructionSize;
            this.associativity = associativity;
            this.writePolicy = writePolicy;
            this.allocatePolicy = allocatePolicy;

            instructionsAccesses = 0;
            instructionsMisses = 0;
            instructionsReplaces = 0;

            dataAccesses = 7;
            dataMisses = 5;
            dataReplaces = 0;

            allFetch = 12;
            allCopies = 2;
        }

        /**
         * @return the cacheMode
         */
        public CacheMode getCacheMode() {
            return cacheMode;
        }

        /**
         * @return the cacheDataSize
         */
        public int getCacheDataSize() {
            return cacheDataSize;
        }

        /**
         * @return the cacheInstructionSize
         */
        public int getCacheInstructionSize() {
            return cacheInstructionSize;
        }

        /**
         * @return the blockSize
         */
        public int getBlockSize() {
            return blockSize;
        }

        /**
         * @return the associativity
         */
        public int getAssociativity() {
            return associativity;
        }

        /**
         * @return the writePolicy
         */
        public WritePolicy getWritePolicy() {
            return writePolicy;
        }

        /**
         * @return the allocatePolicy
         */
        public AllocatePolicy getAllocatePolicy() {
            return allocatePolicy;
        }

        @Override
        public String toString() {
            String result = "***CACHE SETTINGS***\n";
            if(cacheMode.equals(CacheMode.UNIFIED)){
                result = result + "Unified I- D-cache\n" + "Size: " + cacheDataSize + "\n";
            } else {
                result = result + "Split I- D-cache\n" + "I-cache size: " + cacheInstructionSize + "\n" +
                "D-cache size: " + cacheDataSize + "\n";
            }
            result = result + "Associativity: " + associativity + "\n";
            result = result + "Block size: " + blockSize + "\n";
            if(writePolicy.equals(WritePolicy.WRITE_BACK)){
                result = result + "Write policy: WRITE BACK\n";
            } else {
                result = result + "Write policy: WRITE THROUGH\n";
            }
            if(allocatePolicy.equals(AllocatePolicy.WRITE_ALLOCATION)){
                result = result + "Allocation policy: WRITE ALLOCATE\n\n";
            } else {
                result = result + "Allocation policy: WRITE NO ALLOCATE\n\n";
            }
            result = result + "***CACHE STATISTICS***\n";
            result = result + "INSTRUCTIONS\n";
            result = result + "accesses: " + instructionsAccesses + "\n";
            result = result + "misses: " + instructionsMisses + "\n";
            if(instructionsAccesses != 0){
                result = result + "miss rate: " + String.format("%.4f ", ((double)instructionsMisses)/((double)instructionsAccesses)) + "(hit rate " + String.format("%.4f)", 1 - ((double)instructionsMisses)/((double)instructionsAccesses)) + "\n";
            } else {
                result = result + "miss rate: 0.0000 (hit rate 0.0000)\n";
            }
            result = result + "replace: " + instructionsReplaces + "\n";
            result = result + "DATA\n";
            result = result + "accesses: " + dataAccesses + "\n";
            result = result + "misses: " + dataMisses + "\n";
            result = result + "miss rate: " + String.format("%.4f ", ((double)dataMisses)/((double)dataAccesses)) + "(hit rate " + String.format("%.4f)", 1 - ((double)dataMisses)/((double)dataAccesses)) + "\n";
            result = result + "replace: " + dataReplaces + "\n";
            result = result + "TRAFFIC (in words)\n";
            result = result + "demand fetch: " + allFetch + "\n";
            result = result + "copies back: " + allCopies + "\n";

            return result;
        }
    }
    public static void main(String[] args) {
        Main main = new Main();
        Cache testCache = main.new Cache(CacheMode.UNIFIED, 256, 0, 32, 16, WritePolicy.WRITE_THROUGH, AllocatePolicy.NO_WRITE_ALLOCATION);
        System.err.println(testCache);
    }
}