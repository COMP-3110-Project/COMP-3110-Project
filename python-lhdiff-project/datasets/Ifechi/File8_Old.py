public class File8 {
   int seconds;
    def Timer() {
        seconds = 0;
    }
    // tick
    def tick() {
        seconds+=seconds
    }
    def getTime() {
        return seconds;
    }
}