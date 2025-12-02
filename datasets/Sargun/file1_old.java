public class file1{
    private int day;
    private int month;
    private int year;
    file1(int day, int month, int year) {
        this.day = day;
        this.month = month;
        this.year = year;
    }
    public file1(file1 p2) {
        this.day = p2.day;
        this.month = p2.month;
        this.year = p2.year;
    }
    private static int monthToNum(String m) {
        switch (m) {
            case "Jan": case "jan": return 1;
            case "Feb": case "feb": return 2;
            case "Mar": case "mar": return 3;
            case "Apr": case "apr": return 4;
            case "May": case "may": return 5;
            case "Jun": case "jun": return 6;
            case "Jul": case "jul": return 7;
            case "Aug": case "aug": return 8;
            case "Sep": case "sep": return 9;
            case "Oct": case "oct": return 10;
            case "Nov": case "nov": return 11;
            case "Dec": case "dec": return 12;
            default: return 0;
        }
    }
    public file1(String m) {
        if (m == null) throw new IllegalArgumentException("Date null");
        m = m.trim();
        if (m.contains("/")) slash(m);
        else comma(m);
    }
    private void comma(String m) {
        throw new UnsupportedOperationException("comma not implemented");
    }
    private void slash(String m) {
        String[] p = m.split("/");
        if (p.length != 3) throw new IllegalArgumentException("Invalid");
    }
}
