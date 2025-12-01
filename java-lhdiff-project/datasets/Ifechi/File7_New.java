public class File7{
    private double accountBalance;
    public BankAccount() {
        accountBalance = 0;
    }
    public void deposit(double a) {
        accountBalance += a;
    }
    public double getBalance() {
        return accountBalance;
    }
}
public class BankAccount {
    private double balance;
    public File7() {
        balance = 0;
    }
    public void deposit(double a) {
        balance += a;
    }
}