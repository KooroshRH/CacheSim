import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;


class Main{
    HashMap<Integer, Integer> memory = new HashMap<>();//suspicious
    enum WritePolicy{

        WRITE_BACK("wb"),
        WRITE_THROUGH("wt");

        private String value;
        private static HashMap map = new HashMap<>();

        private WritePolicy(String value){
            this.value = value;
        }
        static{
            for (WritePolicy writePolicy : WritePolicy.values()) {
                map.put(writePolicy.value, writePolicy);
            }
        }
        public static WritePolicy valueof(String writePolicy) {
            return (WritePolicy) map.get(writePolicy);
        }
        public String getValue(){
            return value;
        }
    }

    enum AllocatePolicy{

        WRITE_ALLOCATION("wa"),
        NO_WRITE_ALLOCATION("nw");

        private String value;
        private static HashMap map = new HashMap<>();

        private AllocatePolicy(String value){
            this.value = value;
        }
        static{
            for (AllocatePolicy allocatePolicy : AllocatePolicy.values()) {
                map.put(allocatePolicy.value, allocatePolicy);
            }
        }
        public static AllocatePolicy valueof(String allocatePolicy) {
            return (AllocatePolicy) map.get(allocatePolicy);
        }
        public String getValue(){
            return value;
        }
    }

    enum CacheMode{

        UNIFIED(0),
        SEPARATED(1);

        private int value;
        private static HashMap map = new HashMap<>();

        private CacheMode(int value){
            this.value = value;
        }
        static{
            for (CacheMode cacheMode : CacheMode.values()) {
                map.put(cacheMode.value, cacheMode);
            }
        }
        public static CacheMode valueOf(int cacheMode) {
            return (CacheMode) map.get(cacheMode);
        }
        public int getValue(){
            return value;
        }
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

        HashMap<Integer, LinkedList<Block>> dataCache;
        HashMap<Integer, LinkedList<Block>> instructionCache;

        private boolean isWriting;

        public Cache(int cacheMode, int cacheDataSize, int cacheInstructionSize, int blockSize, int associativity, String writePolicy, String allocatePolicy){
            this.cacheMode = CacheMode.valueOf(cacheMode);
            this.cacheDataSize = cacheDataSize;
            this.cacheInstructionSize = cacheInstructionSize;
            this.blockSize =  blockSize;
            this.associativity = associativity;
            this.writePolicy = WritePolicy.valueof(writePolicy);
            this.allocatePolicy = AllocatePolicy.valueof(allocatePolicy);

            dataCache = new HashMap<Integer, LinkedList<Block>>(); //use LinkedHashmap to make it faster
            if(cacheMode == CacheMode.SEPARATED.value){
                instructionCache = new HashMap<Integer, LinkedList<Block>>();
            }

            instructionsAccesses = 0;
            instructionsMisses = 0;
            instructionsReplaces = 0;

            dataAccesses = 0;
            dataMisses = 0;
            dataReplaces = 0;

            allFetch = 0;
            allCopies = 0;

            isWriting = false;
        }

        private void insertIntoCache(String hexAddress, int request){
            // 0 for request means data read and 2 means instruction read
            if(request == 0){
                dataAccesses++;
            } else{
                instructionsAccesses++;
            }
            
            if(request == 0){
                int intAddress = Integer.parseInt(hexAddress, 16);

                int tag = (int)Math.floor((double)(intAddress / blockSize));
                int index = tag % (cacheDataSize / (blockSize * associativity));
                Block block = new Block(true, false, tag);
                if(writePolicy.equals(WritePolicy.WRITE_BACK) && allocatePolicy.equals(AllocatePolicy.WRITE_ALLOCATION) && isWriting){
                    block.setDirty(true);
                }

                if(dataCache.keySet().contains(index)){
                    for (Block bl : dataCache.get(index)) {
                        if(bl.getTag() == tag){ // here hit occures
                            dataCache.get(index).remove(bl);
                            dataCache.get(index).addLast(bl);
                            return;
                        }
                    }
                    
                    // in this if else we face miss in read
                    if(dataCache.get(index).size() == associativity){
                        dataMisses++;
                        dataReplaces++;
                        allFetch++;
                        Block deletingBlock = dataCache.get(index).removeFirst();
                        if(writePolicy.equals(WritePolicy.WRITE_BACK)){
                            if(deletingBlock.isDirty()){
                                allCopies += (blockSize/4);
                                deletingBlock.setDirty(false);
                            }
                        }
                        dataCache.get(index).addLast(block);
                    } else if(dataCache.get(index).size() < associativity){
                        dataMisses++;
                        allFetch++;
                        dataCache.get(index).addLast(block);
                    }
                } else {// when totally address doesn't exsits 
                    dataMisses++;
                    allFetch++;
                    dataCache.put(index, new LinkedList<Block>());
                    dataCache.get(index).add(block);
                }
            } else {
                if(cacheMode.value == 1){
                    int intAddress = Integer.parseInt(hexAddress, 16);

                    int tag = (int)Math.floor((double)(intAddress / blockSize));
                    int index = tag % (cacheInstructionSize / (blockSize * associativity));
                    Block block = new Block(true, false, tag);
                    if(writePolicy.equals(WritePolicy.WRITE_BACK) && allocatePolicy.equals(AllocatePolicy.WRITE_ALLOCATION) && isWriting){
                        block.setDirty(true);
                    }

                    if(instructionCache.keySet().contains(index)){
                        for (Block bl : instructionCache.get(index)) {
                            if(bl.getTag() == tag){// here hit occures
                                instructionCache.get(index).remove(bl);
                                instructionCache.get(index).addLast(bl);
                                return;
                            }
                        }
                        
                        // in this if else we face miss in read
                        if(instructionCache.get(index).size() == associativity){
                            instructionsMisses++;
                            instructionsReplaces++;
                            allFetch++;
                            Block deletingBlock = instructionCache.get(index).removeFirst();
                            if(writePolicy.equals(WritePolicy.WRITE_BACK)){
                                if(deletingBlock.isDirty()){
                                    allCopies += (blockSize/4);
                                    deletingBlock.setDirty(false);
                                }
                            }
                            instructionCache.get(index).addLast(block);
                        } else if(instructionCache.get(index).size() < associativity){
                            instructionsMisses++;
                            allFetch++;
                            instructionCache.get(index).addLast(block);
                        }
                    } else {// when totally address doesn't exsits
                        instructionsMisses++;
                        allFetch++;
                        instructionCache.put(index, new LinkedList<Block>());
                        instructionCache.get(index).add(block);
                    }
                } else {
                    int intAddress = Integer.parseInt(hexAddress, 16);

                    int tag = (int)Math.floor((double)(intAddress / blockSize));
                    int index = tag % (cacheDataSize / (blockSize * associativity));
                    Block block = new Block(true, false, tag);
                    if(writePolicy.equals(WritePolicy.WRITE_BACK) && allocatePolicy.equals(AllocatePolicy.WRITE_ALLOCATION) && isWriting){
                        block.setDirty(true);
                    }

                    if(dataCache.keySet().contains(index)){
                        for (Block bl : dataCache.get(index)) {
                            if(bl.getTag() == tag){// here hit occures
                                dataCache.get(index).remove(bl);
                                dataCache.get(index).addLast(bl);
                                return;
                            }
                        }
                        
                        // in this if else we face miss in read
                        if(dataCache.get(index).size() == associativity){
                            instructionsMisses++;
                            instructionsReplaces++;
                            allFetch++;
                            Block deletingBlock = dataCache.get(index).removeFirst();
                            if(writePolicy.equals(WritePolicy.WRITE_BACK)){
                                if(deletingBlock.isDirty()){
                                    allCopies += (blockSize/4);
                                    deletingBlock.setDirty(false);
                                }
                            }
                            dataCache.get(index).addLast(block);
                        } else if(dataCache.get(index).size() < associativity){
                            instructionsMisses++;
                            allFetch++;
                            dataCache.get(index).addLast(block);
                        }
                    } else {// when totally address doesn't exsits
                        instructionsMisses++;
                        allFetch++;
                        dataCache.put(index, new LinkedList<Block>());
                        dataCache.get(index).add(block);
                    }
                }
            }
            
        }

        private boolean hit(String hexAddress){//can be used in insertInToCache or be just for write
            int intAddress = Integer.parseInt(hexAddress, 16);

            int tag = (int)Math.floor((double)(intAddress / blockSize));
            int index = tag % (cacheDataSize / (blockSize * associativity));

            if(dataCache.keySet().contains(index)){
                for (Block bl : dataCache.get(index)) {
                    if(bl.getTag() == tag){//hit
                        return true;
                    }
                }
                return false;
            } else {//miss
                return false;
            }

        }

        public void readCacheLine(String hexAddress, int request){
            if(request == 1){
                writeIntoCache(hexAddress);
            } else {
                insertIntoCache(hexAddress, request);
            }
        }

        private void writeIntoCache(String hexAddress){
            if(writePolicy.equals(WritePolicy.WRITE_BACK) && allocatePolicy.equals(AllocatePolicy.WRITE_ALLOCATION)){
                writeBackAllocate(hexAddress);
            } else if(writePolicy.equals(WritePolicy.WRITE_BACK) && allocatePolicy.equals(AllocatePolicy.NO_WRITE_ALLOCATION)){
                writeBackNoAllocate(hexAddress);
            } else if(writePolicy.equals(WritePolicy.WRITE_THROUGH) && allocatePolicy.equals(AllocatePolicy.WRITE_ALLOCATION)){
                writeThroughAllocate(hexAddress);
            } else if(writePolicy.equals(WritePolicy.WRITE_THROUGH) && allocatePolicy.equals(AllocatePolicy.NO_WRITE_ALLOCATION)){
                writeThroughNoAllocate(hexAddress);
            }
        }

        private void writeBackAllocate(String hexAddress){           
            int intAddress = Integer.parseInt(hexAddress, 16);

            int tag = (int)Math.floor((double)(intAddress / blockSize));
            int index = tag % (cacheDataSize / (blockSize * associativity));

            if(hit(hexAddress)){
                for (Block block : dataCache.get(index)) {
                    if(block.getTag() == tag){//hit
                        block.setDirty(true);
                        break;
                    }
                }
            }
            isWriting = true;
            insertIntoCache(hexAddress, 0);
            isWriting = false;
        }

        private void writeBackNoAllocate(String hexAddress){
            int intAddress = Integer.parseInt(hexAddress, 16);

            int tag = (int)Math.floor((double)(intAddress / blockSize));
            int index = tag % (cacheDataSize / (blockSize * associativity));

            if(hit(hexAddress)){
                for (Block block : dataCache.get(index)) {
                    if(block.getTag() == tag){//hit
                        block.setDirty(true);
                        break;
                    }
                }
                insertIntoCache(hexAddress, 0);
            } else {
                dataAccesses++;
                dataMisses++;
                allCopies++;
            }
        }

        private void writeThroughAllocate(String hexAddress){
            allCopies++;
            insertIntoCache(hexAddress, 0);
        }

        private void writeThroughNoAllocate(String hexAddress){
            allCopies++;
            if(!hit(hexAddress)){
                dataAccesses++;
                dataMisses++;
            } else {
                insertIntoCache(hexAddress, 0);
            }
        }

        public void finalCheck(){
            for (LinkedList<Block> list : dataCache.values()) {
                for (Block block : list) {
                    if(block.isDirty()){
                        allCopies += (blockSize/4);
                    }
                }
            }
            if(cacheMode.equals(CacheMode.SEPARATED.value)){
                for (LinkedList<Block> list : instructionCache.values()) {
                    for (Block block : list) {
                        if(block.isDirty()){
                            allCopies += (blockSize/4);
                        }
                    }
                }
            }
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
            result = result + "demand fetch: " + allFetch*(blockSize/4) + "\n";
            result = result + "copies back: " + allCopies + "\n";

            return result;
        }
    }

    class Block{
        private boolean isValid;
        private boolean isDirty;
        private int tag;

        public Block(boolean isValid, boolean isDirty, int tag){
            this.isValid = isValid;
            this.isDirty = isDirty;
            this.tag = tag;
        }

        public int getTag() {
            return tag;
        }

        public boolean isDirty() {
            return isDirty;
        }

        public boolean isValid() {
            return isValid;
        }

        public void setDirty(boolean isDirty) {
            this.isDirty = isDirty;
        }

        public void setTag(int tag) {
            this.tag = tag;
        }

        public void setValid(boolean isValid) {
            this.isValid = isValid;
        }
    }
    public static void main(String[] args) {
        Main main = new Main();
        Scanner scan = new Scanner(System.in);
        String[] firstLine = scan.nextLine().split("-");
        String[] secondLine = scan.nextLine().split("-");
        Cache cache;
        
        if(Integer.parseInt(firstLine[1].trim()) == 0){
            cache = main.new Cache(Integer.parseInt(firstLine[1].trim()), Integer.parseInt(secondLine[0].trim()), 0, Integer.parseInt(firstLine[0].trim()), Integer.parseInt(firstLine[2].trim()), firstLine[3].trim(), firstLine[4].trim());
        } else{
            cache = main.new Cache(Integer.parseInt(firstLine[1].trim()), Integer.parseInt(secondLine[1].trim()), Integer.parseInt(secondLine[0].trim()), Integer.parseInt(firstLine[0].trim()), Integer.parseInt(firstLine[2].trim()), firstLine[3].trim(), firstLine[4].trim());
        }

        String input;
        while(scan.hasNextLine()){
            input = scan.nextLine();
            if(!input.equals("")){
                cache.readCacheLine(input.split(" ")[1], Integer.parseInt(input.split("")[0]));
            } else {
                break;
            }
        }
        scan.close();
        cache.finalCheck();
        System.out.println(cache);       
    }
}