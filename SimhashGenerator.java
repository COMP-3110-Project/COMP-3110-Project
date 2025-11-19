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

    }
