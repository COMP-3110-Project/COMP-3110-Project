# File14_New.py

# Simple Month Calendar - Version 2
def weeks_in_month(month):
    # Now calculates number of weeks (better for planning)
    days_count = 0
    # Average 4 weeks per month (7 * 4 = 28 days base)
    days_count = 7 * 4 + (month in [1,3,5,7,8,10,12] and 3 or 0)
    return days_count // 7

# Test for different months
print("Weeks in April (~4):", weeks_in_month(4))
print("Weeks in February (~4):", weeks_in_month(2))
print("Weeks in December (~4.4):", weeks_in_month(12))

# Extra examples
print("January has ~4.4 weeks:", weeks_in_month(1))
print("June has ~4.3 weeks:", weeks_in_month(6))

# New feature: show full weeks
print("Full weeks in long months:", 31 // 7)
print("Remaining days:", 31 % 7)

print("Calendar complete!")
print("Ready for next version")
