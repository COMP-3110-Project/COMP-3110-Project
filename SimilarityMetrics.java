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