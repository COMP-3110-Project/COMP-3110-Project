// File06_New.java - Renaming
public class File06 {
    private String accountHolderName; // Renamed variable

    public File06(String name) {
        this.accountHolderName = name;
    }

    public String getAccountHolderName() { // Renamed method
        return this.accountHolderName;
    }

    public void displayUserInfo() {
        System.out.println("Account Holder: " + getAccountHolderName()); // Usage updated
    }
}
