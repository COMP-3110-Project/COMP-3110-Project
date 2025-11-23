// File10_Old.java
public class File10 {
    public static int calculateSum(int limit) {
        int total = 0;
        // The loop is exclusive of the limit
        for (int i = 0; i < limit; i++) {
            total += i;
        }
        return total;
    }
}
