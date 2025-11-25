// File04_New.java - Line Splitting
public class File04 {
    public void processConfiguration(String host, int port, String username, String password) {
        // The long initialization line is now split
        String connectionString = "jdbc:mysql://" + host + ":" + port + "/";
        connectionString += username + "?password=" + password + "&useSSL=false";
        System.out.println("Connecting to: " + connectionString);
        // More code...
    }
}
