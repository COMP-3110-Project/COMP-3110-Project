public class File7 {
    private double balance;
    public BankAccount() {
        balance = 0;
    }
    public void deposit(double a) {
        balance += a;
    }
    public double getBalance() {
        return balance;
    }
}
