public class Simhash{
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
    }}

