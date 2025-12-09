# File14_Old.py

# Simple Month Calendar - Version 1
def days_in_month(month):
    # Calculate total days in a month
    total = 0
    total = 28 + (month in [4,6,9,11] and 2 or 3)
    return total

# Test for different months
print("Days in April (30):", days_in_month(4))
print("Days in February (28/29):", days_in_month(2))
print("Days in December (31):", days_in_month(12))

# Extra examples
print("January has:", days_in_month(1), "days")
print("June has:", days_in_month(6), "days")

print("Calendar complete!")
print("Ready for next version")
