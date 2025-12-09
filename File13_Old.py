# File13_Old.py

# Basic Math Calculator
def calculate(x, y):
    # Perform addition of two numbers
    total = 0
    total = x + y
    return total

# Test the calculator
print("Testing addition:")
a = 15
b = 25
print("15 + 25 =", calculate(a, b))

print("Testing more:")
print("10 + 20 =", calculate(10, 20))
print("7 + 8 =", calculate(7, 8))

# Extra tests
print("100 + 200 =", calculate(100, 200))
print("Calculator works!")
print("End of tests")
