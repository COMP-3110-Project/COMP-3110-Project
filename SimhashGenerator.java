public class SimhashGenerator{
    public static class LineSimhash {
        public final int index;
        public final long ContentSimhash;//captures the line itself
        public final long ContextSimhash;// captures the surrounding normalized lines 
        public final long CombinedSimhash;// Combines both  ContentSimhash and CombinedSimhash forming a single value 64bits 
        /*---------Constructor----------*/
        public LineSimhash(int index, long contentSimhash, long contextSimhash) {
            this.index = index;
            this.ContentSimhash = contentSimhash; 
            this.ContextSimhash = contextSimhash; 
            this.CombinedSimhash = contentSimhash ^ contextSimhash;// combines two hashes XOR (^)combines two Simhashes without increasing size which later to be used to find Hamming distance 
        }
    
    //Used to store a “possible  match of files”
    public static class Candidate {
        public final int newIndex;//will store index of line in  NEW file might match that old line 
        public final int hammingDistance;//how similar they are
//Creating  a Candidate object for storing the index and similarity distance 
        public Candidate(int newIndex, int hammingDistance) {
            this.newIndex = newIndex;
            this.hammingDistance = hammingDistance;
        }
        @Override
        public String toString() {
            return "Candidate{newIndex=" + newIndex + ", distance=" + hammingDistance + "}";
        }
    }
        /* We normalize again in Step 3 because Simhash is very sensitive to tiny differences.
    Even though Step 1 already cleaned the file globally, Simhash needs its own
          line-level normalization so it can compare lines   detect similarity*/
        
    public String normalizeLine(String line) {
    // if line is empty then it returns an empty string to avoid errors 
        if (line == null) {
            return "";
        }
     // convert everything to lowercase
        String normalized = line.toLowerCase(Locale.ROOT);
        // collapse multiple spaces/tabs into a single space and trim
        normalized = normalized.replaceAll("\\s+", " ").trim();
        return normalized;
    }}
    
    
