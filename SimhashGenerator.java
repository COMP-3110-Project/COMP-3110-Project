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
    
    //Used to store a “possible  match with the other file”
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
    }
