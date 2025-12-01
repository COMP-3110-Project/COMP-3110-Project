public class file1{
    private int day;
    private int month;
    private int year;
    file1_old(int day, int month, int year) {
        this.day = day;
        this.month = month;
        this.year = year;
    }
    public file1_old(file1_old p2) {
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
}
