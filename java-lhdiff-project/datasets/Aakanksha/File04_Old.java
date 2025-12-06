// File04_Old.java
public class File04 {
    public void processConfiguration(String host, int port, String username, String password) {
        // A very long initialization line
        String connectionString = "jdbc:mysql://" + host + ":" + port + "/" + username + "?password=" + password + "&useSSL=false";
        System.out.println("Connecting to: " + connectionString);
        // More code...
    }
}
