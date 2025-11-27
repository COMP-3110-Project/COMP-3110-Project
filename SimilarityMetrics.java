//----------------------------------------------------------------------
//Resolve Conflict (Combined Similarity Score)
// 1. Content Similarity: Normalized Levenshtein Distance (Weight: 0.6)
// ----------------------------------------------------------------------
import java.util.*;

public class SimilarityMetrics {
  private static int calculateLevenshteinDistance(String s1, String s2) {
        // Implements the standard dynamic programming approach for Levenshtein Distance
        int m = s1.length();
        int n = s2.length();
        if (m == 0) return n;
        if (n == 0) return m;

        int[] costs = new int[n + 1];
        for (int j = 0; j <= n; j++) costs[j] = j;

        for (int i = 1; i <= m; i++) {
            costs[0] = i;
            int lastValue = i - 1;
            for (int j = 1; j <= n; j++) {
                int temp = costs[j];
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                // Min(cost to substitute, cost to insert, cost to delete)
                costs[j] = Math.min(costs[j], Math.min(costs[j - 1] + 1, lastValue + cost));
                lastValue = temp;
            }
        }
        return costs[n];
    }
    /**
     * Calculates the Content Similarity score (0.0 to 1.0) using normalized LD.
     * Formula: 1 - (Distance / Max Length)
     */
    public static double getContentSimilarity(String s1, String s2) {
        if (s1.isEmpty() && s2.isEmpty()) return 1.0;
        int maxLen = Math.max(s1.length(), s2.length());
        if (maxLen == 0) return 1.0; 

        int distance = calculateLevenshteinDistance(s1, s2);
        
        // This score is high when distance is low (strings are similar)
        return 1.0 - ((double) distance / maxLen);
    }
    // ----------------------------------------------------------------------
    // 2. Context Similarity: Cosine Similarity (Weight: 0.4)
    // ----------------------------------------------------------------------

    /** Converts a text (context string) into a term frequency vector. */
    private static Map<String, Integer> getTermFrequencyVector(String text) {
        Map<String, Integer> vector = new HashMap<>();
        // Split context text into tokens (words)
        String[] words = text.split("\\s+");
        for (String word : words) {
            if (!word.isEmpty()) {
                vector.put(word, vector.getOrDefault(word, 0) + 1);
            }
        }
        return vector;
    }

    /**
     * Calculates Context Similarity using Cosine Similarity between two context strings.
     */
    public static double getContextSimilarity(String context1, String context2) {
        if (context1.isEmpty() || context2.isEmpty()) return 0.0;
        
        Map<String, Integer> vec1 = getTermFrequencyVector(context1);
        Map<String, Integer> vec2 = getTermFrequencyVector(context2);

        Set<String> intersection = new HashSet<>(vec1.keySet());
        intersection.retainAll(vec2.keySet());
        
        // Calculate Dot Product (Numerator: measures common terms)
        double dotProduct = 0.0;
        for (String term : intersection) {
            dotProduct += vec1.get(term) * vec2.get(term);
        }
        
        // Calculate Magnitude (Denominators: measures the length of each vector)
        double magnitude1 = 0.0;
        for (int count : vec1.values()) {
            magnitude1 += count * count;
        }
        double magnitude2 = 0.0;
        for (int count : vec2.values()) {
            magnitude2 += count * count;
        }
        
        if (magnitude1 == 0.0 || magnitude2 == 0.0) return 0.0;
        
        // Final Cosine Similarity calculation
        // 

[Image of Cosine Similarity in Vector Space]

        return dotProduct / (Math.sqrt(magnitude1) * Math.sqrt(magnitude2));
    }
}