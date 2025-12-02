# File10_New.py

class File10:
    @staticmethod
    def calculate_sum(limit):
        total = 0
        # The loop is now inclusive of the limit
        for i in range(limit + 1):  # Modified: range goes up to limit
            total += i
        return total