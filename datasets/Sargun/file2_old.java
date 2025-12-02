public class file2{
    public static void main(String[] args) {
        System.out.println("File2 demo starting.");
        exampleOne();
        exampleTwo();
        exampleThree();
        System.out.println("File2 demo finished.");
    }
    private static void exampleOne() {
        int a = 5;
        int b = 10;
        int sum = a + b;
        System.out.println("Sum: " + sum);
    }
    private static void exampleTwo() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            sb.append("*");
            System.out.println("Stars: " + sb.toString());
        }
    }
    private static void exampleThree() {
        int[] data = {3, 1, 4, 1, 5};
        int max = data[0];
        int min = data[0];
        int sum = 0;
        for (int value : data) {
            if (value > max) {
                max = value;
            }
            if (value < min) {
                min = value;
            }
            sum += value;
        }
        double average = sum / (double) data.length;
        System.out.println("Max: " + max);
        System.out.println("Min: " + min);
        System.out.println("Average: " + average);
    }
}
