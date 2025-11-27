/**
 * Executes Step 4: Resolves conflicts for candidate sets using the precise
 * Combined Similarity Score (Weighted Content/Context Similarity).
 * This class performs a greedy matching based on the highest score above a threshold.
 * It relies on SimilarityMetrics.java to calculate the scores.
 */
import java.util.*;

public class MappingResolver {
    
    // The context window size is crucial for consistent context extraction.
    private static final int CONTEXT_WINDOW_SIZE = 4;
    
    // Set the required threshold for a successful match. 

    private static final double SIMILARITY_THRESHOLD = 0.7; 
    private final SimhashGenerator contextExtractor = new SimhashGenerator();

    
    
    public static class SettingLineRecord {
        public int lineIndex;
        public String normalized;
        public String simhash;
        // Add other fields as necessary (e.g., raw line, tokens, etc.)
        public SettingLineRecord(int lineIndex, String normalized, String simhash) {
            this.lineIndex = lineIndex;
            this.normalized = normalized;
            this.simhash = simhash;
        }
    }

   
    public static class Candidate {
        public int newIndex;
        public int distance;
        public Candidate(int newIndex, int distance) {
            this.newIndex = newIndex;
            this.distance = distance;
        }
    }


    /**
     * Resolves conflicts among line candidates using the Combined Similarity Score.
     * @param oldRecords List of all SettingLineRecord objects for the old file.
     * @param newRecords List of all SettingLineRecord objects for the new file.
     * @param oldRawLines List of raw String lines for the old file (needed for context).
     * @param newRawLines List of raw String lines for the new file (needed for context).
     * @param candidateMap Output from Step 3: Map<Old Index, List<Candidate New Indices>>.
     * @param unmappedOld Set of old line indices still unmapped.
     * @param unmappedNew Set of new line indices still unmapped.
     * @return A map of confirmed matches: Old Line Index -> New Line Index from this step.
     */
    public Map<Integer, Integer> resolveCandidates(
            List<SettingLineRecord> oldRecords,
            List<SettingLineRecord> newRecords,
            List<String> oldRawLines,
            List<String> newRawLines,
            Map<Integer, List<Candidate>> candidateMap,
            Set<Integer> unmappedOld,
            Set<Integer> unmappedNew) {
        
        // This map stores the final matches confirmed in this step.
        Map<Integer, Integer> step4Matches = new HashMap<>();
        
        
        for (int oldIdx : candidateMap.keySet()) {
            // 1. Check if line 'oldIdx' is still unmapped. If not, skip it.
            if (!unmappedOld.contains(oldIdx)) continue; 

            
            String oldLineNorm = oldRecords.get(oldIdx).normalized;
            
            // Context is extracted from the raw lines list
            String oldLineContext = contextExtractor.extractContext(
                oldRawLines, oldIdx, CONTEXT_WINDOW_SIZE); 
            
            List<Candidate> candidates = candidateMap.get(oldIdx);
            double bestScore = -1.0;
            int bestMatchIndex = -1;
            
            // 2. Evaluate each candidate (newIdx) for the current old line
            for (Candidate candidate : candidates) {
                int newIdx = candidate.newIndex;
                
                // Only consider candidates that are still unmapped in the New File
                if (!unmappedNew.contains(newIdx)) continue; 
                
                
                String newLineNorm = newRecords.get(newIdx).normalized;
                String newLineContext = contextExtractor.extractContext(
                    newRawLines, newIdx, CONTEXT_WINDOW_SIZE);

                // Calculate the two similarity metrics using SimilarityMetrics.java
                
                double contentSim = SimilarityMetrics.getContentSimilarity(oldLineNorm, newLineNorm);
                double contextSim = SimilarityMetrics.getContextSimilarity(newLineContext, oldLineContext); 
                
                // 3. Calculate Combined Similarity Score (Weighted Average)
                // Score = (0.6 * Content Similarity) + (0.4 * Context Similarity)
                double combinedScore = (0.6 * contentSim) + (0.4 * contextSim);
                
                // 4. Track the best candidate match (Greedy selection)
                if (combinedScore > bestScore) {
                    bestScore = combinedScore;
                    bestMatchIndex = newIdx;
                }
            }
            
            // 5. Final Decision: Check threshold and confirm match
            if (bestScore >= SIMILARITY_THRESHOLD && bestMatchIndex != -1) {
                // Confirm the match and add it to the results
                step4Matches.put(oldIdx, bestMatchIndex);
                
              
                unmappedOld.remove(oldIdx);
                unmappedNew.remove(bestMatchIndex);
            }
        }
        return step4Matches;
    }

    /** * Placeholder for SimhashGenerator methods required by MappingResolver. 
     
     */
    private class SimhashGenerator {
        /** Assumed utility method to extract raw line context. */
        public String extractContext(List<String> rawLines, int index, int windowSize) {
            StringBuilder context = new StringBuilder();
            int start = Math.max(0, index - windowSize);
            int end = Math.min(rawLines.size(), index + windowSize + 1);
            
            for (int i = start; i < end; i++) {
                if (i != index) { // Do not include the line itself
                    context.append(rawLines.get(i)).append(" ");
                }
            }
            return context.toString().trim();
        }
    }
}