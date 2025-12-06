public class File8 {
    private int seconds;
    public Timer() {
        seconds = 0;
    }
    // advance one second
    public void tick() {
        seconds++;
    }
    // return seconds
    public int getTime() {
        return seconds;
    }
}