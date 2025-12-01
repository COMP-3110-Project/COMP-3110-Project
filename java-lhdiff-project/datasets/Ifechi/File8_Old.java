public class File8 {
    private int seconds;
    public Timer() {
        seconds = 0;
    }
    // tick
    public void tick() {
        seconds++;
    }
    public int getTime() {
        return seconds;
    }
}