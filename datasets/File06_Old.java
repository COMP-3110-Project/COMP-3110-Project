// File06_Old.java
public class File06 {
    private String userName; // Variable to be renamed

    public File06(String name) {
        this.userName = name;
    }

    public String getUserName() { // Method to be renamed
        return this.userName;
    }

    public void displayUserInfo() {
        System.out.println("User: " + getUserName());
    }
}
