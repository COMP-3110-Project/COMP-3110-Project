public class file3 {
    // Main method: sets up array and calls helpers
    public static void main(String[] args) {
        System.out.println("File3 demo starting.");
        int[] numbers = {2, 4, 6, 8, 10};
        printArray(numbers);
        int total = sum(numbers);              // uses wrapper
        System.out.println("Total: " + total);
        int target = 6;
        analyzeArray(numbers, target);         // merged logic
        System.out.println("File3 demo finished.");
    }
    // Prints each value with its index
    private static void printArray(int[] array) {
        for (int index = 0; index < array.length; index++) {
            System.out.println("Value " + index + ": " + array[index]);
        }
    }
    // Wrapper method that delegates to the loop implementation
    private static int sum(int[] array) {
        return sumLoop(array);
    }
    // Split-out loop that actually accumulates the sum
    private static int sumLoop(int[] array) {
        int total = 0;
        for (int value : array) {
            total += value;
        }
        return total;
    }
    // Merged method: computes average and finds target index
    private static void analyzeArray(int[] array, int target) {
        int total = sum(array);
        double average = total / (double) array.length;
        System.out.println("Average: " + average);
        int foundIndex = -1;
        for (int i = 0; i < array.length; i++) {
            if (array[i] == target) {
                foundIndex = i;
                break;
            }
        }
        System.out.println("Index of " + target + ": " + foundIndex);
    }
    // End of File3 helper demo
    // Contains one split method and one merged method
    // Ready to be used for line mapping in Task 2
    // Total lines in this file: 50
}
