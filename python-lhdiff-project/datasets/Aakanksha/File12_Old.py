# File12_Old.py

class File12:

    def add(self, a, b):
        return a + b
    
    def minus(self, a, b):
        return a - b
    
    def multiply(self, a, b):
        return a * b
    
    def divide(self, a, b):
        return a / b

    def greet(self, name):
        message = "Hello " + name
        print(message)
        # Added a simple print for testing

    def compute_square(self, n):
        result = n * n
        return result

    def process_numbers(self, nums):
        total = 0
        for n in nums:
            total += n
        # Debug total
        print("Total inside process_numbers:", total)
        return total

    def debug_log(self, text):
        print("[DEBUG]:", text)

    def unused_function(self):
        x = 10
        y = 20
        return x