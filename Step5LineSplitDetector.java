import java.util.*;

public class Step5LineSplitDetector {

    private static final double THRESHOLD = 0.5; // Threshold to accept a split mapping

    /**
     * Executes Step 5: Detect Line Splits.
     * Maps a single line in Old File to multiple contiguous lines in New File.
     */
    public Map<Integer, List<Integer>> detectSplits(
            List<LinesMapping.SettingLineRecord> oldRecords,
            List<LinesMapping.SettingLineRecord> newRecords,
            Set<Integer> unmappedOld,
            Set<Integer> unmappedNew) {

        Map<Integer, List<Integer>> splitMatches = new HashMap<>();
        
        // We iterate through unmapped old lines to find if they split into multiple new lines
        // We convert the Set to a List to iterate deterministically
        List<Integer> sortedOldIndices = new ArrayList<>(unmappedOld);
        Collections.sort(sortedOldIndices);

        for (int oldIdx : sortedOldIndices) {
            String oldNorm = oldRecords.get(oldIdx).normalized;
            if (oldNorm.isEmpty()) continue;

            double bestScore = -1.0;
            List<Integer> bestSequence = null;

            // Search through unmapped new lines as potential starting points
            List<Integer> sortedNewIndices = new ArrayList<>(unmappedNew);
            Collections.sort(sortedNewIndices);

            for (int i = 0; i < sortedNewIndices.size(); i++) {
                int startNewIdx = sortedNewIndices.get(i);
                
                // --- The Algorithm from Slide 42 ---
                
                // 1. Start with one line
                StringBuilder combinedContent = new StringBuilder(newRecords.get(startNewIdx).normalized);
                List<Integer> currentSequence = new ArrayList<>();
                currentSequence.add(startNewIdx);

                // Calculate initial Normalized Levenshtein Distance (as Similarity)
                // Slide says: LD(R20, L40)
                double currentSim = SimilarityMetrics.getContentSimilarity(oldNorm, combinedContent.toString());

                // 2. Try adding subsequent lines
                int offset = 1;
                while (true) {
                    // Check if the next line (start + offset) is available in the specific file order
                    // Note: Split lines must be contiguous in the new file.
                    int nextIdx = startNewIdx + offset;

                    // Ensure nextIdx exists in file and is currently unmapped
                    if (nextIdx >= newRecords.size() || !unmappedNew.contains(nextIdx)) {
                        break; 
                    }

                    // Prepare the combined string: R20 + R21
                    String nextLineContent = newRecords.get(nextIdx).normalized;
                    String testCombinedContent = combinedContent.toString() + " " + nextLineContent;

                    // Calculate new similarity: LD(R20+21, L40)
                    double nextSim = SimilarityMetrics.getContentSimilarity(oldNorm, testCombinedContent);

                    // Slide Logic: "Similarity increases, so add another line"
                    if (nextSim > currentSim) {
                        currentSim = nextSim;
                        combinedContent.append(" ").append(nextLineContent);
                        currentSequence.add(nextIdx);
                        offset++;
                    } else {
                        // "Similarity decreases, so stop"
                        break;
                    }
                }

                // Keep the best sequence found for this specific Old Line
                if (currentSim > bestScore && currentSim >= THRESHOLD) {
                    bestScore = currentSim;
                    bestSequence = new ArrayList<>(currentSequence);
                }
            }

            // If a valid split was found
            if (bestSequence != null && !bestSequence.isEmpty()) {
                splitMatches.put(oldIdx, bestSequence);
                
                // Important: Remove these used lines from availability so other old lines don't claim them
                // This is a greedy approach.
                for (int usedIdx : bestSequence) {
                    unmappedNew.remove(usedIdx);
                }
            }
        }

        // Remove the matched old lines from the unmapped set
        for (int oldIdx : splitMatches.keySet()) {
            unmappedOld.remove(oldIdx);
        }

        return splitMatches;
    }
}
