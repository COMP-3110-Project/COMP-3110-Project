package bugfix;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Stage 4: GitLogReader
 * -----------------------
 * Reads commit history directly from a Git repository using:
 *
 *   git log --pretty=format:%H|%s
 *
 * Why we need this:
 *   - Automatically gathers commit data for classification.
 *   - Separates shell command execution from classification logic.
 *
 * Output:
 *   - List<CommitRecord> representing all commits.
 */
public class GitLogReader {

    /**
     * Reads commit history from the given repository path.
     * @param repoPath path to the git repo (e.g., ".")
     */
    public static List<CommitRecord> readCommits(String repoPath) throws Exception {

        List<CommitRecord> commits = new ArrayList<>();

        // Build git log command
        ProcessBuilder pb = new ProcessBuilder(
                "git", "log", "--pretty=format:%H|%s"
        );
        pb.directory(new java.io.File(repoPath)); // run in the repo directory

        Process process = pb.start();

        // Read each line of output
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {

            String line;
            while ((line = reader.readLine()) != null) {

                // Format: <hash>|<message>
                String[] parts = line.split("\\|", 2);

                if (parts.length == 2) {
                    commits.add(new CommitRecord(parts[0], parts[1]));
                }
            }
        }

        process.waitFor();
        return commits;
    }
}
