public class File1 {
    double disabilityAmount() {
    if (seniority < 2) {//content changed 
        return 0;
    }
    if (monthsDisabled > 12) { // lines deleted because of change in line above
        return 0;
    }
    if (isPartTime) {
        return 0;
    }
    // Compute the disability amount.
    // ...
    }
}