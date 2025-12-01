# File03_New.py - Methods Reordered

class File03:
    def add(self, a, b):  # This method is now first
        return a + b

    def multiply(self, a, b):  # This method is now second
        return a * b

    def display_result(self, result):
        print("Final Result:", result)  # Slight modification