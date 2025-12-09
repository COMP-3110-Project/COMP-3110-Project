# File13_New.py

# Basic Math Calculator - Updated
def calculate(x, y):
    # Now performs multiplication
    result = 0
    # Perform multiplication of two numbers
    result = x * y
    return result

# Test the calculator
print("Testing multiplication:")
a = 15
b = 25
print("15 * 25 =", calculate(a, b))

print("Testing more:")
print("10 * 20 =", calculate(10, 20))
print("7 * 8 =", calculate(7, 8))

# Extra tests
print("100 * 200 =", calculate(100, 200))
print("Now testing power:")
print("5 ** 3 =", 5 ** 3)
print("2 ** 10 =", 2 ** 10)

print("Calculator upgraded to multiplication!")
print("All tests passed successfully!")
