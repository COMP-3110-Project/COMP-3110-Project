
package bugfix;

public class CommitRecord {
    private final String commitId;
    private final String message;

    public CommitRecord(String commitId, String message) {
        this.commitId = commitId;
        this.message = message;
    }

    public String getCommitId() { return commitId; }
    public String getMessage() { return message; }

    public String toString() {
        return commitId + ": " + message;
    }
}
