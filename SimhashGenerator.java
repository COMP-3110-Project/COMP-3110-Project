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
    }
//  since Context is enough so we ExtracContext that we later feed Simhash to help match lines more accurately.
    //lines = all lines of the file
    //index = the position of the target line
    //windowSize = how many lines above and below to look at
    //String extractContext: Returns a single String containing all neighbor lines
    public String extractContext(List<String> lines, int index, int windowSize) {
        int n = lines.size();
        //lines we want to look below and above the target line 
        int start = Math.max(0, index - windowSize);
        int end = Math.min(n - 1, index + windowSize);

        StringBuilder sb = new StringBuilder();//StringBuilder  one BIg String :  will contain all neighbor lines joined together.

        for (int i = start; i <= end; i++) {
            if (i == index) {
                continue; // skipping the target line, only neighbors are context
            }
            String normalized = normalizeLine(lines.get(i));// Clean the neighbor line using normalizeLine
            if (!normalized.isEmpty()) {//Add the context string ONLY if it is not empty
                if (sb.length() > 0) {// if line already has text 
                    sb.append(' ');// put space  before adding any other text 
                }
                sb.append(normalized);
            }
        }
        return sb.toString();//sb returns all neighbor lines joined together as String
    }
//  This method computes a 64-bit SimHash for a piece of text:similar texts produce similar 64-bit values
    public long computeSimhash(String text) {
    	//If string is missing or empty, the method returns 0 because no hash can be computed 
        if (text == null || text.isEmpty()) {//
            return 0L;
        }
/*even though we have method extractContext save the line in sb(as one big line with spaces  ) 
we still split into tokens : each words needs to treated independently as SimHash is designed to combine many small token-hashes, not one giant hash.
Each token contributes to the bitVector)*/
        String[] tokens = text.split("\\s+");
        int[] bitVector = new int[64];// indices to represent the bit positions (0–63) of the hash
//Each token is hashed into a 64-bit hash value using hash64(token)
        for (String token : tokens) {
            if (token.isEmpty()) {
                continue;
            }
            long hash = hash64(token); // Using .util.* library: string gets turned into a 64 bit number we get the numeric fingerprint” of that word.
 //*-----SimHashlook does not read all 64 bits at once so looked at each bit at time so it  needs to update the vector one bit at a time.--------/
         for (int bit = 0; bit < 64; bit++) {
     
        	// : 1L is just 1 stored in a long type
        	 //  << (left shift operator)take the bits of a and shift them to the left by n positions
    
        	// A bitmask → creating a  number that has only one bit turned on all other values are zero other than one 1
        	 long bitmask = 1L << bit;
                if ((hash & bitmask) != 0) {//   
                	/* It compares each bit: AND keeps only the bit that both numbers have ON for not zero
                	  f AND is non-zero → that bit is 1  If AND is zero → that bit is 0.	 */
                    bitVector[bit] += 1;  //If the bit was 1: increase  weights stored inside the index 
                } else {
                    bitVector[bit] -= 1;  // If the bit was 0:Decrease the weight stored at this bit index
             
                }
              
            }}
/*--------- final 64-bit SimHash-----------*/
        long simhash = 0L;//empty SimHash all bits = 0
        for (int bit = 0; bit < 64; bit++) {//Loop through all indices (bit positions)
            if (bitVector[bit] > 0) {//If the value at that index is positive then SimHash bit should be 1
            	// at the empty SimHash all bits = 0 and push the left side using <<
            	simhash |= (1L << bit);// creates a mask with only this bit = 1  * |= sets that bit in simhash to 1
            	
            }
        }
        return simhash;
    }
    
    









        
    }
