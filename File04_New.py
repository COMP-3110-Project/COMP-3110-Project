# File04_New.py

def ubc_safe_divide(a, b):
    try:
        result = a / b
        return result
    except:
        return 0
print(ubc_safe_divide(10, 0))
print("No crash!")
