package bugfix;

import java.util.List;

/**
 * Stage 2: BugFixRules
 * ---------------------
 * Contains the rule set for detecting bug-fix messages.
 *
 * Purpose:
 *   - Defines the keywords and patterns that indicate a bug fix.
 *   - Keeps classification logic separate from parsing logic.
 *
 * Why separate?
 *   - Easy to extend or modify rules later.
 *   - Cleaner design following single-responsibility principle.
 *
 * Used by:
 *   - CommitClassifier (Stage 3)
 */
public class BugFixRules {

    /**
     * Keywords commonly used in bug-fix commits.
     * These match the definitions given in the assignment:
     *   "fix", "bug", "issue", "closes #...", etc.
     */
    private static final List<String> BUG_KEYWORDS = List.of(
            "fix", "fixed", "fixes",
            "bug",
            "issue",
            "resolve", "resolved", "resolves",
            "close #", "closes #"
    );

    /**
     * Words that imply the commit is documentation-related.
     * Depending on project policy, these may or may not be considered "true" bug fixes.
     */
    private static final List<String> DOC_KEYWORDS = List.of(
            "documentation", "readme", "typo", "docs"
    );

    /** Returns true if the commit message likely refers to fixing a bug. */
    public static boolean containsBugKeyword(String message) {
        String msg = message.toLowerCase();
        for (String key : BUG_KEYWORDS) {
            if (msg.contains(key)) {
                return true;
            }
        }
        return false;
    }

    /** Returns true if it's a documentation-only fix. */
    public static boolean isDocumentationFix(String message) {
        String msg = message.toLowerCase();
        for (String key : DOC_KEYWORDS) {
            if (msg.contains(key)) {
                return true;
            }
        }
        return false;
    }
}
