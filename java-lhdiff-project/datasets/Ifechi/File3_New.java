public class File3 {
    private int value;
    public File3() {
        value = 0;
    }
    public void reset() { // Added line
        value = 0;
    }
    public void increment() {
        value++;
    }
    public int getValue() {
        return value;
    }
}