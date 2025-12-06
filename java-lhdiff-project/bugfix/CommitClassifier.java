package bugfix;

/**
 * Stage 3: CommitClassifier
 * --------------------------
 * Uses BugFixRules to classify commits into:
 *   - BUG_FIX
 *   - DOC_FIX
 *   - NOT_A_FIX
 *
 * Role in system:
 *   - This is the "brain" of the bug-fix detector.
 *   - Determines the type of each commit based solely on its message.
 *
 * Used by:
 *   - BugFixDetector main tool (Stage 5)
 */
public class CommitClassifier {

    /** Different categories a commit can belong to. */
    public enum Classification {
        BUG_FIX,
        DOC_FIX,
        NOT_A_FIX
    }

    /**
     * Classifies a commit according to the rules.
     * Steps:
     *   1. Check if it contains bug keywords.
     *   2. If yes, decide if it’s a documentation fix.
     */
    public static Classification classify(CommitRecord commit) {
        String msg = commit.getMessage();

        // If the commit doesn't look like a bug fix → classify as NOT_A_FIX
        if (!BugFixRules.containsBugKeyword(msg)) {
            return Classification.NOT_A_FIX;
        }

        // If it is a bug keyword but only affects docs → DOC_FIX
        if (BugFixRules.isDocumentationFix(msg)) {
            return Classification.DOC_FIX;
        }

        // Otherwise it is a code-related bug fix
        return Classification.BUG_FIX;
    }
}
