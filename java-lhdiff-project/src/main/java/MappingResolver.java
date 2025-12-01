import java.util.*;

public class MappingResolver {
    private static final int CONTEXT_WINDOW_SIZE = 4;
    // ADJUSTED: Higher Content weight to preserve Exact Matches with bad context (Line 1).
    // ADJUSTED: Threshold to 0.65 to filter out "Sum" vs "Product" (Line 6).
    private static final double WEIGHT_CONTENT = 0.8;
    private static final double WEIGHT_CONTEXT = 0.2;
    private static final double SIMILARITY_THRESHOLD = 0.65; 
    
    private final SimhashGenerator contextExtractor = new SimhashGenerator();

    public static class SettingLineRecord {
        public int lineIndex;
        public String normalized;
        public String simhash;
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

    public Map<Integer, Integer> resolveCandidates(
            List<SettingLineRecord> oldRecords,
            List<SettingLineRecord> newRecords,
            List<String> oldRawLines,
            List<String> newRawLines,
            Map<Integer, List<Candidate>> candidateMap,
            Set<Integer> unmappedOld,
            Set<Integer> unmappedNew) {
        
        Map<Integer, Integer> step4Matches = new HashMap<>();
        
        for (int oldIdx : candidateMap.keySet()) {
            if (!unmappedOld.contains(oldIdx)) continue; 

            String oldLineNorm = oldRecords.get(oldIdx).normalized;
            // Skip fuzzy matching for BLANK_TOKEN
            if (oldLineNorm.equals("BLANK_TOKEN")) continue;

            String oldLineContext = contextExtractor.extractContext(oldRawLines, oldIdx, CONTEXT_WINDOW_SIZE); 
            
            List<Candidate> candidates = candidateMap.get(oldIdx);
            double bestScore = -1.0;
            int bestMatchIndex = -1;
            
            for (Candidate candidate : candidates) {
                int newIdx = candidate.newIndex;
                if (!unmappedNew.contains(newIdx)) continue; 
                
                String newLineNorm = newRecords.get(newIdx).normalized;
                if (newLineNorm.equals("BLANK_TOKEN")) continue;

                String newLineContext = contextExtractor.extractContext(newRawLines, newIdx, CONTEXT_WINDOW_SIZE);

                double contentSim = SimilarityMetrics.getContentSimilarity(oldLineNorm, newLineNorm);
                double contextSim = SimilarityMetrics.getContextSimilarity(newLineContext, oldLineContext); 
                
                double combinedScore = (WEIGHT_CONTENT * contentSim) + (WEIGHT_CONTEXT * contextSim);
                
                if (combinedScore > bestScore) {
                    bestScore = combinedScore;
                    bestMatchIndex = newIdx;
                }
            }
            
            if (bestScore >= SIMILARITY_THRESHOLD && bestMatchIndex != -1) {
                step4Matches.put(oldIdx, bestMatchIndex);
                unmappedOld.remove(oldIdx);
                unmappedNew.remove(bestMatchIndex);
            }
        }
        return step4Matches;
    }

    private class SimhashGenerator {
        public String extractContext(List<String> rawLines, int index, int windowSize) {
            StringBuilder context = new StringBuilder();
            int start = Math.max(0, index - windowSize);
            int end = Math.min(rawLines.size(), index + windowSize + 1);
            for (int i = start; i < end; i++) {
                if (i != index) context.append(rawLines.get(i)).append(" ");
            }
            return context.toString().trim();
        }
    }
}