import java.util.*;

public class Step5LineSplitDetector {
    private static final double THRESHOLD = 0.5;

    public Map<Integer, List<Integer>> detectSplits(
            List<LinesMapping.SettingLineRecord> oldRecords,
            List<LinesMapping.SettingLineRecord> newRecords,
            Set<Integer> unmappedOld,
            Set<Integer> unmappedNew) {

        Map<Integer, List<Integer>> splitMatches = new HashMap<>();
        List<Integer> sortedOldIndices = new ArrayList<>(unmappedOld);
        Collections.sort(sortedOldIndices);

        for (int oldIdx : sortedOldIndices) {
            String oldNorm = oldRecords.get(oldIdx).normalized;
            // Skip splitting for BLANK_TOKEN
            if (oldNorm.isEmpty() || oldNorm.equals("BLANK_TOKEN")) continue;

            double bestScore = -1.0;
            List<Integer> bestSequence = null;

            List<Integer> sortedNewIndices = new ArrayList<>(unmappedNew);
            Collections.sort(sortedNewIndices);

            for (int i = 0; i < sortedNewIndices.size(); i++) {
                int startNewIdx = sortedNewIndices.get(i);
                
                StringBuilder combinedContent = new StringBuilder(newRecords.get(startNewIdx).normalized);
                List<Integer> currentSequence = new ArrayList<>();
                currentSequence.add(startNewIdx);

                double currentSim = SimilarityMetrics.getContentSimilarity(oldNorm, combinedContent.toString());
                int offset = 1;

                while (true) {
                    int nextIdx = startNewIdx + offset;
                    if (nextIdx >= newRecords.size() || !unmappedNew.contains(nextIdx)) break; 

                    String nextLineContent = newRecords.get(nextIdx).normalized;
                    String testCombinedContent = combinedContent.toString() + " " + nextLineContent;
                    double nextSim = SimilarityMetrics.getContentSimilarity(oldNorm, testCombinedContent);

                    if (nextSim > currentSim) {
                        currentSim = nextSim;
                        combinedContent.append(" ").append(nextLineContent);
                        currentSequence.add(nextIdx);
                        offset++;
                    } else {
                        break;
                    }
                }

                if (currentSim > bestScore && currentSim >= THRESHOLD) {
                    bestScore = currentSim;
                    bestSequence = new ArrayList<>(currentSequence);
                }
            }

            if (bestSequence != null && !bestSequence.isEmpty()) {
                splitMatches.put(oldIdx, bestSequence);
                for (int usedIdx : bestSequence) unmappedNew.remove(usedIdx);
            }
        }
        for (int oldIdx : splitMatches.keySet()) unmappedOld.remove(oldIdx);
        return splitMatches;
    }
}