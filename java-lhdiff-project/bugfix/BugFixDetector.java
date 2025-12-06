package bugfix;

import java.util.List;

/**
 * Stage 5: BugFixDetector (Main Program)
 * ---------------------------------------
 * This is the command-line interface for the bonus task.
 *
 * What it does:
 *   1. Reads the commit log from a Git repo.
 *   2. Classifies each commit.
 *   3. Prints the commits that are detected as code bug fixes.
 *
 * Usage:
 *   java bugfix.BugFixDetector <repo-path>
 *
 * Example output:
 *   [BUG_FIX] a91f34d — Fix login error (#123)
 */
public class BugFixDetector {

    public static void main(String[] args) throws Exception {

        // Ensure repo path is given
        if (args.length < 1) {
            System.err.println("Usage: BugFixDetector <repo-path>");
            System.exit(1);
        }

        String repo = args[0];

        System.out.println("Reading commits from: " + repo);

        // Load commit history
        List<CommitRecord> commits = GitLogReader.readCommits(repo);

        System.out.println("Bug-fix commits:");
        for (CommitRecord commit : commits) {

            CommitClassifier.Classification type =
                    CommitClassifier.classify(commit);

            // Only print actual code bug fixes
            if (type == CommitClassifier.Classification.BUG_FIX) {
                System.out.println("[BUG_FIX] " +
                        commit.getCommitId() + " — " +
                        commit.getMessage());
            }
        }
    }
}
