public class File4 {
    private double celsius;
    public Temperature(double c) {
        this.celsius = c;
    }
    public double toFahrenheit() {
        return (celsius * 9/5) + 32;
    }
}