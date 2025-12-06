# File12_New.py

class File12:

    def sum_values(self, a, b):  # renamed from add
        return a + b

    def debug_log(self, text):   # moved up
        print("[DEBUG]:", text)

    def multiply(self, a, b):    # modified
        result = a * b
        self.debug_log(f"Multiplying {a} * {b} = {result}")
        return result

    def divide(self, a, b):      # modified to handle zero division
        if b == 0:
            self.debug_log("Attempted division by zero!")
            return None
        return a / b

    def process_numbers(self, nums):  # moved up + modified
        total = 0
        for n in nums:
            total += n
        self.debug_log(f"Processed numbers total: {total}")
        return total

    def greet(self, name):            # modified
        message = f"Hi, {name}!"     # changed greeting format
        print(message)

    def minus(self, a, b):            # moved down
        return a - b