// File3 helper demo
public class file3 {
    public static void main(String[] args) {
        System.out.println("File3 demo starting.");
        int[] numbers = {2, 4, 6, 8, 10};
        printArray(numbers);
        int total = sum(numbers);
        double avg = average(numbers);
        System.out.println("Total: " + total);
        System.out.println("Average: " + avg);
        int target = 6;
        int index = indexOf(numbers, target);
        System.out.println("Index of " + target + ": " + index);
    }
    private static void printArray(int[] values) {
        for (int i = 0; i < values.length; i++) {
            System.out.println("Value " + i + ": " + values[i]);
        }
    }
    private static int sum(int[] values) {
        int result = 0;
        for (int value : values) {
            result += value;
        }
        return result;
    }
    private static double average(int[] values) {
        if (values.length == 0) {
            return 0.0;
        }
        int result = sum(values);
        return result / (double) values.length;
    }
    private static int indexOf(int[] values, int target) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] == target) {
                return i;
            }
        }
        return -1;
    }
}
