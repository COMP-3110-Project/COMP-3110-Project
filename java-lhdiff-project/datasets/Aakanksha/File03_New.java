// File03_New.java - Methods Reordered
public class File03 {
    public int add(int a, int b) { // This method is now first
        return a + b;
    }

    public int multiply(int a, int b) { // This method is now second
        return a * b;
    }

    public void displayResult(int result) {
        System.out.println("Final Result: " + result); // Slight modification
    }
}
