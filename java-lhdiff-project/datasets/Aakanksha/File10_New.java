// File10_New.java
public class File10 {
    public static int calculateSum(int limit) {
        int total = 0;
        // The loop is now inclusive of the limit
        for (int i = 0; i <= limit; i++) { // Modified: < changed to <=
            total += i;
        }
        return total;
    }
}
