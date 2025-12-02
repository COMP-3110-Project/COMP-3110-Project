// File08_Old.java
public class File08 {
    private int counter = 0;

    public void increment() {
        counter++;
        System.out.println("Counter: " + counter); // This line
    }

    public void resetAndLog(int initialValue) {
        this.counter = initialValue;
        String logMessage = "Counter reset to " + initialValue + ".";
        System.out.println(logMessage);
    }
}
