// File02_Old.java
public class File02 {
    public void processData(String[] data) {
        System.out.println("Starting data processing...");
        for (String item : data) {
            System.out.println("Processing: " + item);
            // Intermediate step 1
            if (item.contains("error")) {
                System.err.println("Error detected for: " + item);
            }
            // Intermediate step 2
        }
        System.out.println("Data processing finished.");
    }
}
