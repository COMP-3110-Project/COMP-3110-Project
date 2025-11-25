// File08_New.java - Mixed Complex Changes
public class File08 {
    private int counter = 0;

    public void resetAndLog(int initialValue) { // Moved method
        this.counter = initialValue;
        // Line split
        String logMessage = "Counter reset to ";
        logMessage += initialValue + ". Logged at " + System.currentTimeMillis();
        System.out.println(logMessage);
    }

    public void increment() { // Moved method
        counter++;
        String currentCount = "Current count: " + counter; // Line merged
        System.out.println(currentCount); // Line merged
    }
}
